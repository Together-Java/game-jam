import os
import shutil
import struct
import time
import tkinter as tk
from collections import deque
from tkinter import ttk, filedialog, messagebox, simpledialog


# ----------------------------
# constants / file markers
# ----------------------------

# 32-bit sentinel for empty
EMPTY_TILE = 0xFFFFFFFF

# file signatures
TMAP2_MAGIC = b"tmap2\n"  # legacy v2 (prop layer stored after tile paths)
TMAP3_MAGIC = b"tmap3\n"  # new v3 (prop layer stored in header with w/h/etc)

MARKER_A = b"markers"
MARKER_A_ALT = b"marker"  # accept legacy example
MARKER_ANIM = b"animated"
MARKER_B = b"tuvalutorture"
END_MARKER = b"end"


# ----------------------------
# helpers
# ----------------------------

def _script_dir() -> str:
    return os.path.dirname(os.path.abspath(__file__))


def _to_forward_slashes(path: str) -> str:
    return path.replace("\\", "/")


def _prefix_dot_slash(rel: str) -> str:
    rel = rel.replace("\\", "/")
    if rel == ".":
        return "./"
    if rel.startswith("./") or rel.startswith("../"):
        return rel
    return "./" + rel


def _abs_to_rel_base(abs_path: str, base_dir: str) -> str:
    base = os.path.abspath(base_dir)
    rel = os.path.relpath(os.path.abspath(abs_path), base)
    return _to_forward_slashes(_prefix_dot_slash(rel))


def _resolve_path(path_in_file: str, map_file_path: str | None, base_dir: str) -> str:
    """
    paths inside the map are stored with '/' separators.

    requested behavior:
      - relative paths resolve relative to a chosen "root folder" (base_dir).
    compatibility fallbacks:
      - if that fails and we have a map path, also try relative to the map file folder.
      - then try the python script folder.

    returns the first existing candidate; otherwise returns the base_dir candidate.
    """
    p = path_in_file.replace("/", os.sep)

    if os.path.isabs(p):
        return p

    base_dir = os.path.abspath(base_dir)

    # prefer chosen root folder
    cand_root = os.path.normpath(os.path.join(base_dir, p))
    if os.path.exists(cand_root):
        return cand_root

    # fallback: map dir (portable exports / older content)
    if map_file_path:
        base_map = os.path.dirname(os.path.abspath(map_file_path))
        cand_map = os.path.normpath(os.path.join(base_map, p))
        if os.path.exists(cand_map):
            return cand_map

    # fallback: script dir
    base_script = _script_dir()
    cand_script = os.path.normpath(os.path.join(base_script, p))
    if os.path.exists(cand_script):
        return cand_script

    return cand_root


def _read_line(data: bytes, pos: int) -> tuple[bytes, int]:
    nl = data.find(b"\n", pos)
    if nl == -1:
        raise ValueError("missing newline")
    return data[pos:nl], nl + 1


def _safe_attr(s: str) -> str:
    s = s.strip()
    if not s:
        return ""
    if "," in s or "\n" in s or "\r" in s:
        raise ValueError("attributes cannot contain commas or newlines")
    return s


def _parse_int_prefix(line: bytes) -> tuple[int | None, list[str]]:
    """
    parse lines like:
      b"123,foo,bar"
    returns (123, ["foo","bar"]) or (None, [])
    """
    try:
        text = line.decode("utf-8")
    except Exception:
        return None, []
    parts = [p for p in text.split(",")]
    if not parts:
        return None, []
    head = parts[0].strip()
    if not head.isdigit():
        return None, []
    idx = int(head)
    rest = [p.strip() for p in parts[1:] if p.strip()]
    return idx, rest


def _flip_layer_y(layer: list[int], w: int, h: int) -> list[int]:
    """
    flips rows vertically in a row-major layer buffer.
    """
    if w <= 0 or h <= 0:
        return list(layer)
    out = [EMPTY_TILE] * (w * h)
    for y in range(h):
        src_y = y
        dst_y = (h - 1 - y)
        src0 = src_y * w
        dst0 = dst_y * w
        out[dst0:dst0 + w] = layer[src0:src0 + w]
    return out


# ----------------------------
# dialogs
# ----------------------------

class NewMapDialog(simpledialog.Dialog):
    def __init__(self, parent, max_w: int, max_h: int):
        self._max_w = int(max_w)
        self._max_h = int(max_h)
        super().__init__(parent)

    def body(self, master):
        self.title("new map")

        ttk.Label(master, text="tile width (1-255)").grid(row=0, column=0, sticky="w")
        ttk.Label(master, text="tile height (1-255)").grid(row=1, column=0, sticky="w")
        ttk.Label(master, text=f"drawable width in tiles (1-{min(255, self._max_w)})").grid(row=2, column=0, sticky="w")
        ttk.Label(master, text=f"drawable height in tiles (1-{min(255, self._max_h)})").grid(row=3, column=0, sticky="w")
        ttk.Label(master, text="layers (1-255)").grid(row=4, column=0, sticky="w")
        ttk.Label(master, text=f"max canvas is {self._max_w}x{self._max_h} (save pads dead space to this)").grid(
            row=5, column=0, columnspan=2, sticky="w", pady=(8, 0)
        )

        self.tw = ttk.Entry(master)
        self.th = ttk.Entry(master)
        self.mw = ttk.Entry(master)
        self.mh = ttk.Entry(master)
        self.layers = ttk.Entry(master)

        self.tw.insert(0, "16")
        self.th.insert(0, "16")
        self.mw.insert(0, str(min(40, self._max_w)))
        self.mh.insert(0, str(min(30, self._max_h)))
        self.layers.insert(0, "2")

        self.tw.grid(row=0, column=1, sticky="ew", padx=6, pady=2)
        self.th.grid(row=1, column=1, sticky="ew", padx=6, pady=2)
        self.mw.grid(row=2, column=1, sticky="ew", padx=6, pady=2)
        self.mh.grid(row=3, column=1, sticky="ew", padx=6, pady=2)
        self.layers.grid(row=4, column=1, sticky="ew", padx=6, pady=2)

        master.columnconfigure(1, weight=1)
        return self.tw

    def validate(self):
        try:
            tw = int(self.tw.get())
            th = int(self.th.get())
            mw = int(self.mw.get())
            mh = int(self.mh.get())
            layers = int(self.layers.get())
            for v in (tw, th, mw, mh, layers):
                if v < 1 or v > 255:
                    raise ValueError

            if mw > self._max_w or mh > self._max_h:
                raise ValueError(f"drawable size must be <= max canvas ({self._max_w}x{self._max_h})")

            self.result = (tw, th, mw, mh, layers)
            return True
        except Exception as e:
            messagebox.showerror("invalid", f"{e}\n\nall values must be integers within range")
            return False


class StartDialog(tk.Toplevel):
    """
    startup wizard: pick root folder, choose open/new, set flip-y preference before loading.
    also sets the "max canvas size" used for padding/centering.
    """
    def __init__(self, parent: "TileMapEditor"):
        super().__init__(parent)
        self.parent = parent
        self.title("start")
        self.resizable(False, False)
        self.protocol("WM_DELETE_WINDOW", self._on_quit)

        self._root_var = tk.StringVar(value=parent.root_dir)
        self._max_w_var = tk.StringVar(value=str(parent.max_map_w))
        self._max_h_var = tk.StringVar(value=str(parent.max_map_h))

        pad = 10
        frm = ttk.Frame(self, padding=pad)
        frm.pack(fill="both", expand=True)

        ttk.Label(frm, text="root folder (paths stored relative to this)").grid(row=0, column=0, sticky="w")
        root_row = ttk.Frame(frm)
        root_row.grid(row=1, column=0, sticky="ew", pady=(4, 10))
        self._root_entry = ttk.Entry(root_row, textvariable=self._root_var, width=52)
        self._root_entry.pack(side="left", fill="x", expand=True)
        ttk.Button(root_row, text="choose...", command=self._choose_root).pack(side="left", padx=(8, 0))

        max_row = ttk.Frame(frm)
        max_row.grid(row=2, column=0, sticky="ew", pady=(0, 10))
        ttk.Label(max_row, text="max canvas size (tiles)").pack(side="left")
        ttk.Label(max_row, text="w").pack(side="left", padx=(10, 2))
        self._max_w_entry = ttk.Entry(max_row, textvariable=self._max_w_var, width=6)
        self._max_w_entry.pack(side="left")
        ttk.Label(max_row, text="h").pack(side="left", padx=(10, 2))
        self._max_h_entry = ttk.Entry(max_row, textvariable=self._max_h_var, width=6)
        self._max_h_entry.pack(side="left")
        ttk.Label(max_row, text="(save pads dead space up to this size)").pack(side="left", padx=(10, 0))

        self._flip_chk = ttk.Checkbutton(
            frm,
            text="flip y in file (fix upside-down maps in some renderers)",
            variable=parent.flip_y_in_file
        )
        self._flip_chk.grid(row=3, column=0, sticky="w", pady=(0, 10))

        btns = ttk.Frame(frm)
        btns.grid(row=4, column=0, sticky="ew")
        ttk.Button(btns, text="open map...", command=self._open).pack(side="left", fill="x", expand=True)
        ttk.Button(btns, text="new map...", command=self._new).pack(side="left", fill="x", expand=True, padx=(8, 8))
        ttk.Button(btns, text="quit", command=self._on_quit).pack(side="left")

        frm.columnconfigure(0, weight=1)

        self.transient(parent)
        self.grab_set()
        self.update_idletasks()

        # center over parent
        try:
            px = parent.winfo_rootx()
            py = parent.winfo_rooty()
            pw = parent.winfo_width()
            ph = parent.winfo_height()
            w = self.winfo_width()
            h = self.winfo_height()
            self.geometry(f"+{px + (pw - w)//2}+{py + (ph - h)//2}")
        except Exception:
            pass

        self._root_entry.focus_set()

    def _choose_root(self):
        folder = filedialog.askdirectory(
            title="select root folder (all stored paths will be relative to this)"
        )
        if not folder:
            return
        folder = os.path.abspath(folder)
        self._root_var.set(folder)
        self.parent.root_dir = folder
        self.parent._update_status("root folder set")

    def _apply_max_size(self) -> bool:
        try:
            mw = int(self._max_w_var.get().strip())
            mh = int(self._max_h_var.get().strip())
            if mw < 1 or mw > 255 or mh < 1 or mh > 255:
                raise ValueError
            self.parent.max_map_w = mw
            self.parent.max_map_h = mh
            return True
        except Exception:
            messagebox.showerror("invalid", "max canvas size must be integers from 1 to 255", parent=self)
            return False

    def _open(self):
        # ensure root var applied
        self.parent.root_dir = os.path.abspath(self._root_var.get().strip() or self.parent.root_dir)
        if not self._apply_max_size():
            return

        path = filedialog.askopenfilename(
            title="open map",
            filetypes=[("tile map", "*.tmap *.bin *.map *.*"), ("all files", "*.*")]
        )
        if not path:
            return
        try:
            self.parent._load_map_file(path)
        except Exception as e:
            messagebox.showerror("open failed", str(e), parent=self)
            return
        self.destroy()

    def _new(self):
        self.parent.root_dir = os.path.abspath(self._root_var.get().strip() or self.parent.root_dir)
        if not self._apply_max_size():
            return
        self.parent._new_map()
        self.destroy()

    def _on_quit(self):
        # quit the whole app
        try:
            self.parent.destroy()
        except Exception:
            pass


# ----------------------------
# editor app
# ----------------------------

class TileMapEditor(tk.Tk):
    def __init__(self):
        super().__init__()

        self.title("tilemap editor")
        self.geometry("1100x780")

        # path root (all stored paths are relative to this)
        self.root_dir = _script_dir()

        # max canvas size (tiles) used for padding/centering
        self.max_map_w = 40
        self.max_map_h = 30

        # drawable area (tiles) and its top-left within the max canvas
        self.draw_w = 40
        self.draw_h = 30
        self.draw_x0 = 0
        self.draw_y0 = 0

        # toggles
        self.anim_enabled = tk.BooleanVar(value=False)
        # this affects BOTH load/save/export; keep it consistent per project
        self.flip_y_in_file = tk.BooleanVar(value=True)

        # map state
        self.tile_w = 16
        self.tile_h = 16

        # NOTE: map_w/map_h are the MAX canvas dimensions (what gets saved)
        self.map_w = 0
        self.map_h = 0
        self.num_layers = 0

        self.tile_paths: list[str] = []   # stored relative to root_dir (with ./ prefix)
        self.tile_images: list[tk.PhotoImage] = []
        self.selected_tile: int | None = None
        self.selected_tiles: set[int] = set()  # multi-select set

        # layers store 32-bit indices (or EMPTY_TILE)
        self.layers: list[list[int]] = []
        self.cell_items: list[list[int | None]] = []  # canvas item ids per layer per cell

        self.layer_visible: list[bool] = []
        self.current_layer = 0
        self.prop_layer = EMPTY_TILE  # sentinel means none

        self.attributes: dict[int, list[str]] = {}  # tile index -> list[str]

        # animations: idx -> (folder_rel, duration_ms, frame_count)
        self.animations: dict[int, tuple[str, int, int]] = {}
        self.animation_frames: dict[int, list[tk.PhotoImage]] = {}
        self.animated_items: dict[int, set[int]] = {}  # idx -> set(canvas_item_id)

        self._anim_start_time = time.monotonic()
        self._anim_job: str | None = None

        self.map_path: str | None = None

        # palette mode (list/grid)
        self.palette_mode = tk.StringVar(value="list")
        self._tile_grid_cols = 1
        self._tile_grid_cell_w = 1
        self._tile_grid_cell_h = 1
        self._tile_grid_sel_rect: int | None = None
        self._tile_grid_thumbs: list[tk.PhotoImage] = []  # keep refs alive
        self._tile_grid_last_clicked: int | None = None

        # main canvas zoom (integer ratios using tk zoom/subsample)
        self._zoom_levels: list[tuple[int, int]] = [
            (1, 4),
            (1, 3),
            (1, 2),
            (1, 1),
            (2, 1),
            (3, 1),
            (4, 1),
        ]
        self._zoom_i = 3  # (1,1)
        self.scale_n, self.scale_d = self._zoom_levels[self._zoom_i]
        self.view_tile_w = self.tile_w
        self.view_tile_h = self.tile_h
        self._scaled_tile_cache: dict[tuple[int, int, int], tk.PhotoImage] = {}
        self._scaled_anim_cache: dict[tuple[int, int, int, int], tk.PhotoImage] = {}

        # palette grid thumbnail zoom
        self._pal_zoom_levels: list[tuple[int, int]] = [
            (1, 4),
            (1, 3),
            (1, 2),
            (1, 1),
            (2, 1),
            (3, 1),
            (4, 1),
        ]
        self._pal_zoom_i = 3
        self._pal_thumb_cache: dict[tuple[int, int, int], tk.PhotoImage] = {}
        self._pal_anim_thumb_cache: dict[tuple[int, int, int, int], tk.PhotoImage] = {}

        # tools
        self.tool_mode = tk.StringVar(value="brush")  # brush / rect / fill / line
        self._rect_start: tuple[int, int] | None = None
        self._rect_end: tuple[int, int] | None = None
        self._rect_preview_id: int | None = None
        self._rect_value: int = EMPTY_TILE

        self._line_start: tuple[int, int] | None = None
        self._line_end: tuple[int, int] | None = None
        self._line_preview_id: int | None = None

        # undo
        self.undo_stack: list[list[tuple[int, int, int, int]]] = []  # (layer_i, cell_idx, old, new)
        self._undo_current: dict[tuple[int, int], tuple[int, int]] | None = None  # (layer,idx)->(old,new)
        self._undo_in_progress = False
        self._undo_max = 50

        self._build_ui()
        self._bind_canvas()
        self._bind_tool_hotkeys()

        self._apply_zoom(self._zoom_i, keep_view=True)
        self._update_status()

        # startup wizard (root + open/new)
        self.after(0, self._startup_wizard)

    def _startup_wizard(self):
        # if the app was destroyed before running, bail
        if not self.winfo_exists():
            return
        dlg = StartDialog(self)
        try:
            self.wait_window(dlg)
        except Exception:
            return

        # if the user quit, the window is gone
        if not self.winfo_exists():
            return

        # if they closed dialog in some weird way without creating/loading a map, default to new map
        if self.map_w <= 0 or self.map_h <= 0 or self.num_layers <= 0:
            self._new_map()

    # ---------------- drawable/max helpers ----------------

    def _recompute_draw_origin_centered(self):
        # center drawable within max
        if self.map_w <= 0 or self.map_h <= 0:
            self.draw_x0 = 0
            self.draw_y0 = 0
            return
        self.draw_w = max(1, min(int(self.draw_w), int(self.map_w)))
        self.draw_h = max(1, min(int(self.draw_h), int(self.map_h)))
        self.draw_x0 = max(0, (self.map_w - self.draw_w) // 2)
        self.draw_y0 = max(0, (self.map_h - self.draw_h) // 2)

    def _in_drawable(self, cx: int, cy: int) -> bool:
        return (
                self.draw_x0 <= cx < (self.draw_x0 + self.draw_w) and
                self.draw_y0 <= cy < (self.draw_y0 + self.draw_h)
        )

    def _drawable_bounds(self):
        x0 = self.draw_x0
        y0 = self.draw_y0
        x1 = self.draw_x0 + self.draw_w - 1
        y1 = self.draw_y0 + self.draw_h - 1
        return x0, y0, x1, y1

    def _clear_outside_drawable(self):
        if self.map_w <= 0 or self.map_h <= 0:
            return
        x0, y0, x1, y1 = self._drawable_bounds()
        cell_count = self.map_w * self.map_h
        for li in range(self.num_layers):
            layer = self.layers[li]
            if len(layer) != cell_count:
                continue
            for cy in range(self.map_h):
                row0 = cy * self.map_w
                for cx in range(self.map_w):
                    if x0 <= cx <= x1 and y0 <= cy <= y1:
                        continue
                    idx = row0 + cx
                    layer[idx] = EMPTY_TILE

    def _make_padded_layers(self, target_w: int, target_h: int, center: bool = True) -> list[list[int]]:
        """
        creates new layer buffers at target_w x target_h and copies current drawable content into it.
        dead space is EMPTY_TILE.
        """
        target_w = int(target_w)
        target_h = int(target_h)
        if target_w < 1 or target_w > 255 or target_h < 1 or target_h > 255:
            raise ValueError("target size must be 1..255")

        out_layers: list[list[int]] = []
        out_cell_count = target_w * target_h

        # source drawable
        src_dw = int(self.draw_w)
        src_dh = int(self.draw_h)
        src_x0 = int(self.draw_x0)
        src_y0 = int(self.draw_y0)

        # target placement (center drawable)
        if center:
            dst_x0 = max(0, (target_w - src_dw) // 2)
            dst_y0 = max(0, (target_h - src_dh) // 2)
        else:
            dst_x0 = 0
            dst_y0 = 0

        copy_w = max(0, min(src_dw, target_w - dst_x0))
        copy_h = max(0, min(src_dh, target_h - dst_y0))

        for li in range(self.num_layers):
            buf = [EMPTY_TILE] * out_cell_count
            src_layer = self.layers[li]
            if not src_layer:
                out_layers.append(buf)
                continue
            for y in range(copy_h):
                sy = src_y0 + y
                dy = dst_y0 + y
                if sy < 0 or sy >= self.map_h or dy < 0 or dy >= target_h:
                    continue
                for x in range(copy_w):
                    sx = src_x0 + x
                    dx = dst_x0 + x
                    if sx < 0 or sx >= self.map_w or dx < 0 or dx >= target_w:
                        continue
                    sidx = sy * self.map_w + sx
                    didx = dy * target_w + dx
                    buf[didx] = src_layer[sidx]
            out_layers.append(buf)

        return out_layers

    # ---------------- path root ----------------

    def _set_root_folder(self):
        folder = filedialog.askdirectory(title="select root folder (all stored paths will be relative to this)")
        if not folder:
            return
        self.root_dir = os.path.abspath(folder)
        self._update_status("root folder set")
        # try to reload assets so current map updates instantly
        if self.tile_paths:
            try:
                self._reload_assets_from_paths()
            except Exception as e:
                messagebox.showerror("root folder", str(e))

    def _clear_root_folder(self):
        self.root_dir = _script_dir()
        self._update_status("root folder reset to script folder")
        if self.tile_paths:
            try:
                self._reload_assets_from_paths()
            except Exception as e:
                messagebox.showerror("root folder", str(e))

    def _reload_assets_from_paths(self):
        # reload tile images
        new_imgs: list[tk.PhotoImage] = []
        for p in self.tile_paths:
            real = _resolve_path(p, self.map_path, self.root_dir)
            if not os.path.exists(real):
                raise ValueError(
                    f"missing tile:\n{p}\nresolved to:\n{real}\n\ncurrent root folder:\n{self.root_dir}"
                )
            img = tk.PhotoImage(file=real)
            if img.width() != self.tile_w or img.height() != self.tile_h:
                raise ValueError(
                    f"tile size mismatch for:\n{p}\nexpected {self.tile_w}x{self.tile_h}, got {img.width()}x{img.height()}"
                )
            new_imgs.append(img)
        self.tile_images = new_imgs

        # reload animations
        self.animation_frames = {}
        for idx, (folder_rel, ms, cnt) in list(self.animations.items()):
            if not (0 <= idx < len(self.tile_images)):
                continue
            frames = self._load_animation_frames(folder_rel, cnt, self.map_path)
            self.animation_frames[idx] = frames

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self._refresh_tile_list()
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        self._sync_preview_and_attrs()
        self._ensure_anim_loop()
        self._update_status("assets reloaded")

    # ---------------- ui ----------------

    def _build_ui(self):
        self._build_menu()

        # paned layout so you can resize sidebars horizontally
        self.panes = ttk.Panedwindow(self, orient="horizontal")
        self.panes.pack(fill="both", expand=True)

        self.left = ttk.Frame(self.panes, padding=6)
        self.center = ttk.Frame(self.panes, padding=6)
        self.right = ttk.Frame(self.panes, padding=6)

        self.panes.add(self.left, weight=0)
        self.panes.add(self.center, weight=1)
        self.panes.add(self.right, weight=0)

        # sane minimum sizes
        try:
            self.panes.paneconfigure(self.left, minsize=280)
            self.panes.paneconfigure(self.right, minsize=240)
            self.panes.paneconfigure(self.center, minsize=360)
        except Exception:
            pass

        # palette
        ttk.Label(self.left, text="tiles").pack(anchor="w")

        mode_row = ttk.Frame(self.left)
        mode_row.pack(fill="x", pady=(2, 4))

        ttk.Radiobutton(
            mode_row, text="list", value="list",
            variable=self.palette_mode, command=self._update_palette_mode
        ).pack(side="left")

        ttk.Radiobutton(
            mode_row, text="grid", value="grid",
            variable=self.palette_mode, command=self._update_palette_mode
        ).pack(side="left", padx=(8, 8))

        # palette grid zoom buttons
        ttk.Button(mode_row, text="grid -", width=7, command=self._pal_zoom_out).pack(side="left")
        ttk.Button(mode_row, text="grid +", width=7, command=self._pal_zoom_in).pack(side="left", padx=(6, 0))
        ttk.Button(mode_row, text="grid 1:1", width=8, command=self._pal_zoom_reset).pack(side="left", padx=(6, 0))

        self.palette_stack = ttk.Frame(self.left)
        self.palette_stack.pack(fill="x")

        # list view (multi-select enabled)
        self.tile_list = tk.Listbox(self.palette_stack, height=16, exportselection=False, selectmode="extended")
        self.tile_list.pack(fill="x")
        self.tile_list.bind("<<ListboxSelect>>", self._on_tile_select)

        # grid view
        self.tile_grid_frame = ttk.Frame(self.palette_stack)
        self.tile_grid = tk.Canvas(self.tile_grid_frame, height=290, background="#111111", highlightthickness=0)
        self.tile_grid_vbar = ttk.Scrollbar(self.tile_grid_frame, orient="vertical", command=self.tile_grid.yview)
        self.tile_grid.configure(yscrollcommand=self.tile_grid_vbar.set)
        self.tile_grid.grid(row=0, column=0, sticky="nsew")
        self.tile_grid_vbar.grid(row=0, column=1, sticky="ns")
        self.tile_grid_frame.rowconfigure(0, weight=1)
        self.tile_grid_frame.columnconfigure(0, weight=1)

        self.tile_grid.bind("<Button-1>", self._on_tile_grid_click)
        self.tile_grid.bind("<Configure>", self._on_tile_grid_configure)

        # wheel scroll for the grid
        self.tile_grid.bind("<MouseWheel>", self._on_tile_grid_wheel)      # windows/mac
        self.tile_grid.bind("<Button-4>", self._on_tile_grid_wheel_linux)  # linux up
        self.tile_grid.bind("<Button-5>", self._on_tile_grid_wheel_linux)  # linux down

        # ctrl+wheel zoom for the grid thumbnails
        self.tile_grid.bind("<Control-MouseWheel>", self._on_tile_grid_zoom_wheel)
        self.tile_grid.bind("<Control-Button-4>", self._on_tile_grid_zoom_linux)
        self.tile_grid.bind("<Control-Button-5>", self._on_tile_grid_zoom_linux)

        self.tile_grid_frame.pack_forget()  # start hidden

        pal_btns = ttk.Frame(self.left)
        pal_btns.pack(fill="x", pady=(6, 0))
        ttk.Button(pal_btns, text="load png tile(s)", command=self._load_tiles).pack(side="left", fill="x", expand=True)
        ttk.Button(pal_btns, text="split tilesheet...", command=self._split_tilesheet).pack(side="left", fill="x", expand=True, padx=(6, 0))
        ttk.Button(pal_btns, text="remove selected", command=self._remove_selected_tile).pack(side="left", padx=(6, 0))

        # tile reorder buttons
        reorder_row = ttk.Frame(self.left)
        reorder_row.pack(fill="x", pady=(6, 0))
        ttk.Button(reorder_row, text="move up", command=lambda: self._move_selected_tile(-1)).pack(side="left", fill="x", expand=True)
        ttk.Button(reorder_row, text="move down", command=lambda: self._move_selected_tile(1)).pack(side="left", fill="x", expand=True, padx=(6, 0))
        ttk.Button(reorder_row, text="move to...", command=self._move_selected_tile_to).pack(side="left", padx=(6, 0))

        self.tile_preview = ttk.Label(self.left, text="(no tile selected)", anchor="center")
        self.tile_preview.pack(fill="x", pady=(8, 0))

        # tools
        tools = ttk.LabelFrame(self.left, text="tools")
        tools.pack(fill="x", pady=(10, 0))

        ttk.Radiobutton(tools, text="brush (b)", value="brush", variable=self.tool_mode,
                        command=self._on_tool_changed).pack(anchor="w", padx=6, pady=(4, 0))
        ttk.Radiobutton(tools, text="rectangle (r)", value="rect", variable=self.tool_mode,
                        command=self._on_tool_changed).pack(anchor="w", padx=6)
        ttk.Radiobutton(tools, text="fill bucket (f)", value="fill", variable=self.tool_mode,
                        command=self._on_tool_changed).pack(anchor="w", padx=6)
        ttk.Radiobutton(tools, text="line (l)", value="line", variable=self.tool_mode,
                        command=self._on_tool_changed).pack(anchor="w", padx=6)

        ttk.Label(tools, text="left = paint, right = erase (brush only)").pack(anchor="w", padx=6, pady=(0, 6))
        ttk.Label(self.left, text="eyedropper: shift + left click").pack(anchor="w", pady=(10, 0))
        ttk.Label(self.left, text="undo: ctrl+z").pack(anchor="w", pady=(2, 0))

        # attributes
        self.attr_title = ttk.Label(self.left, text="attributes for selected tile")
        self.attr_title.pack(anchor="w", pady=(10, 0))

        self.attr_list = tk.Listbox(self.left, height=7, exportselection=False)
        self.attr_list.pack(fill="x")

        attr_row = ttk.Frame(self.left)
        attr_row.pack(fill="x", pady=(4, 0))
        self.attr_entry = ttk.Entry(attr_row)
        self.attr_entry.pack(side="left", fill="x", expand=True)
        ttk.Button(attr_row, text="add", command=self._add_attr).pack(side="left", padx=(6, 0))
        ttk.Button(self.left, text="remove selected attr", command=self._remove_attr).pack(fill="x", pady=(4, 0))

        # animation
        ttk.Label(self.left, text="animation for selected tile").pack(anchor="w", pady=(10, 0))

        ttk.Checkbutton(
            self.left,
            text="auto-animate tiles",
            variable=self.anim_enabled,
            command=self._on_anim_toggle
        ).pack(anchor="w", pady=(2, 6))

        anim_box = ttk.Frame(self.left)
        anim_box.pack(fill="x")

        self.anim_folder = ttk.Entry(anim_box)
        self.anim_folder.pack(side="left", fill="x", expand=True)
        ttk.Button(anim_box, text="browse", command=self._browse_anim_folder).pack(side="left", padx=(6, 0))

        anim_row2 = ttk.Frame(self.left)
        anim_row2.pack(fill="x", pady=(4, 0))
        ttk.Label(anim_row2, text="frame ms (1-255)").pack(side="left")
        self.anim_ms = ttk.Entry(anim_row2, width=6)
        self.anim_ms.pack(side="left", padx=(6, 10))
        ttk.Label(anim_row2, text="frames (1-255)").pack(side="left")
        self.anim_count = ttk.Entry(anim_row2, width=6)
        self.anim_count.pack(side="left", padx=(6, 0))

        anim_btns = ttk.Frame(self.left)
        anim_btns.pack(fill="x", pady=(4, 0))
        ttk.Button(anim_btns, text="set animation", command=self._set_animation_for_selected).pack(side="left", fill="x", expand=True)
        ttk.Button(anim_btns, text="clear", command=self._clear_animation_for_selected).pack(side="left", padx=(6, 0))

        self.anim_info = ttk.Label(self.left, text="(none)", anchor="w")
        self.anim_info.pack(fill="x", pady=(4, 0))

        # layers
        ttk.Label(self.right, text="layers (double-click toggles visibility)").pack(anchor="w")

        self.layer_list = tk.Listbox(self.right, height=14, exportselection=False)
        self.layer_list.pack(fill="x")
        self.layer_list.bind("<<ListboxSelect>>", self._on_layer_select)
        self.layer_list.bind("<Double-Button-1>", self._toggle_layer_visibility)

        lay_btns = ttk.Frame(self.right)
        lay_btns.pack(fill="x", pady=(6, 0))
        ttk.Button(lay_btns, text="add layer", command=self._add_layer).pack(side="left", fill="x", expand=True)
        ttk.Button(lay_btns, text="remove layer", command=self._remove_layer).pack(side="left", padx=(6, 0))

        lay_move = ttk.Frame(self.right)
        lay_move.pack(fill="x", pady=(6, 0))
        ttk.Button(lay_move, text="move up", command=lambda: self._move_layer(-1)).pack(side="left", fill="x", expand=True)
        ttk.Button(lay_move, text="move down", command=lambda: self._move_layer(1)).pack(side="left", fill="x", expand=True, padx=(6, 0))

        prop_frame = ttk.LabelFrame(self.right, text="prop layer")
        prop_frame.pack(fill="x", pady=(10, 0))

        self.prop_label = ttk.Label(prop_frame, text="none")
        self.prop_label.pack(anchor="w", padx=6, pady=(4, 0))

        prop_btns = ttk.Frame(prop_frame)
        prop_btns.pack(fill="x", padx=6, pady=6)
        ttk.Button(prop_btns, text="set prop layer", command=self._set_prop_layer).pack(side="left", fill="x", expand=True)
        ttk.Button(prop_btns, text="clear", command=self._clear_prop_layer).pack(side="left", padx=(6, 0))

        # canvas with scrollbars
        self.canvas = tk.Canvas(self.center, background="#1f1f1f", highlightthickness=0)
        self.hbar = ttk.Scrollbar(self.center, orient="horizontal", command=self.canvas.xview)
        self.vbar = ttk.Scrollbar(self.center, orient="vertical", command=self.canvas.yview)
        self.canvas.configure(xscrollcommand=self.hbar.set, yscrollcommand=self.vbar.set)

        self.canvas.grid(row=0, column=0, sticky="nsew")
        self.vbar.grid(row=0, column=1, sticky="ns")
        self.hbar.grid(row=1, column=0, sticky="ew")

        self.center.rowconfigure(0, weight=1)
        self.center.columnconfigure(0, weight=1)

        # status bar
        self.status = ttk.Label(self, text="", anchor="w")
        self.status.pack(fill="x", side="bottom")

    def _build_menu(self):
        menubar = tk.Menu(self)

        file_menu = tk.Menu(menubar, tearoff=False)
        file_menu.add_command(label="new", command=self._new_map)
        file_menu.add_command(label="open...", command=self._open_map)
        file_menu.add_separator()
        file_menu.add_command(label="save", command=self._save_map)
        file_menu.add_command(label="save as...", command=self._save_as_map)
        file_menu.add_command(label="save padded as...", command=self._save_padded_as)
        file_menu.add_separator()
        file_menu.add_command(label="set drawable area...", command=self._set_drawable_area_dialog)
        file_menu.add_command(label="set max canvas size (pad/crop)...", command=self._set_max_size_dialog)
        file_menu.add_separator()
        file_menu.add_command(label="export to folder...", command=self._export_folder)
        file_menu.add_separator()
        file_menu.add_command(label="set root folder...", command=self._set_root_folder)
        file_menu.add_command(label="reset root folder (script folder)", command=self._clear_root_folder)
        file_menu.add_separator()
        file_menu.add_checkbutton(
            label="flip y in file (upside-down fix)",
            variable=self.flip_y_in_file,
            command=lambda: self._update_status("flip-y toggled")
        )
        file_menu.add_separator()
        file_menu.add_command(label="quit", command=self.destroy)

        edit_menu = tk.Menu(menubar, tearoff=False)
        edit_menu.add_command(label="undo", command=self._undo, accelerator="ctrl+z")

        view_menu = tk.Menu(menubar, tearoff=False)
        view_menu.add_command(label="zoom in", command=self._zoom_in, accelerator="ctrl+=")
        view_menu.add_command(label="zoom out", command=self._zoom_out, accelerator="ctrl+-")
        view_menu.add_command(label="reset zoom", command=self._zoom_reset, accelerator="ctrl+0")
        view_menu.add_separator()
        view_menu.add_checkbutton(
            label="animate tiles",
            variable=self.anim_enabled,
            command=self._on_anim_toggle
        )

        help_menu = tk.Menu(menubar, tearoff=False)
        help_menu.add_command(label="about", command=self._show_about)

        menubar.add_cascade(label="file", menu=file_menu)
        menubar.add_cascade(label="edit", menu=edit_menu)
        menubar.add_cascade(label="view", menu=view_menu)
        menubar.add_cascade(label="help", menu=help_menu)
        self.config(menu=menubar)

    def _show_about(self):
        messagebox.showinfo(
            "about",
            "copyright non-existent, vibe-coded by tuvalutorture for onithorynque studios use\n"
            "this build adds: max canvas + centered drawable area, save padding, and load small -> save padded."
        )

    def _bind_tool_hotkeys(self):
        self.bind_all("b", lambda e: self._set_tool("brush"))
        self.bind_all("r", lambda e: self._set_tool("rect"))
        self.bind_all("f", lambda e: self._set_tool("fill"))
        self.bind_all("l", lambda e: self._set_tool("line"))

    def _set_tool(self, name: str):
        if name not in ("brush", "rect", "fill", "line"):
            return
        self.tool_mode.set(name)
        self._on_tool_changed()

    def _on_tool_changed(self):
        self._clear_rect_preview()
        self._rect_start = None
        self._rect_end = None
        self._clear_line_preview()
        self._line_start = None
        self._line_end = None
        self._cancel_undo_recording()

    def _bind_canvas(self):
        # left
        self.canvas.bind("<Button-1>", self._on_left_down)
        self.canvas.bind("<B1-Motion>", self._on_left_drag)
        self.canvas.bind("<ButtonRelease-1>", self._on_left_up)

        # right (and middle acts like right)
        self.canvas.bind("<Button-3>", self._on_right_down)
        self.canvas.bind("<B3-Motion>", self._on_right_drag)
        self.canvas.bind("<ButtonRelease-3>", self._on_right_up)

        self.canvas.bind("<Button-2>", self._on_right_down)
        self.canvas.bind("<B2-Motion>", self._on_right_drag)
        self.canvas.bind("<ButtonRelease-2>", self._on_right_up)

        # eyedropper
        self.canvas.bind("<Shift-Button-1>", self._pick_at_event)
        self.canvas.bind("<Shift-B1-Motion>", self._pick_at_event)

        # main zoom bindings
        self.canvas.bind("<Control-MouseWheel>", self._on_zoom_wheel)   # windows/mac
        self.canvas.bind("<Control-Button-4>", self._on_zoom_linux)     # linux up
        self.canvas.bind("<Control-Button-5>", self._on_zoom_linux)     # linux down

        self.bind_all("<Control-equal>", lambda e: self._zoom_in())
        self.bind_all("<Control-minus>", lambda e: self._zoom_out())
        self.bind_all("<Control-0>", lambda e: self._zoom_reset())
        self.bind_all("<Control-z>", lambda e: self._undo())

    # ---------------- undo ----------------

    def _begin_undo_recording(self):
        if self._undo_in_progress:
            return
        self._undo_current = {}

    def _cancel_undo_recording(self):
        self._undo_current = None

    def _commit_undo_recording(self, label: str = ""):
        cur = self._undo_current
        self._undo_current = None
        if not cur:
            return

        changes: list[tuple[int, int, int, int]] = []
        for (layer_i, cell_idx), (old, new) in cur.items():
            if old != new:
                changes.append((layer_i, cell_idx, old, new))

        if not changes:
            return

        self.undo_stack.append(changes)
        if len(self.undo_stack) > self._undo_max:
            self.undo_stack.pop(0)

        if label:
            self._update_status(label)

    def _undo(self):
        if not self.undo_stack:
            self._update_status("undo: nothing")
            return
        changes = self.undo_stack.pop()
        self._undo_in_progress = True
        try:
            for layer_i, cell_idx, old, _new in changes:
                cx = cell_idx % self.map_w
                cy = cell_idx // self.map_w
                self._set_cell(layer_i, cx, cy, old, update_status=False, allow_outside=True)
        finally:
            self._undo_in_progress = False
            self._cancel_undo_recording()
        self._update_status("undo")

    # ---------------- palette mode ----------------

    def _update_palette_mode(self):
        mode = self.palette_mode.get()
        if mode == "grid":
            self.tile_list.pack_forget()
            self.tile_grid_frame.pack(fill="x")
            self._refresh_tile_grid()
        else:
            self.tile_grid_frame.pack_forget()
            self.tile_list.pack(fill="x")

    def _on_tile_grid_configure(self, _evt=None):
        if self.palette_mode.get() == "grid":
            self._refresh_tile_grid()

    def _on_tile_grid_wheel(self, event):
        delta = -1 if event.delta > 0 else 1
        self.tile_grid.yview_scroll(delta, "units")

    def _on_tile_grid_wheel_linux(self, event):
        if event.num == 4:
            self.tile_grid.yview_scroll(-1, "units")
        elif event.num == 5:
            self.tile_grid.yview_scroll(1, "units")

    def _on_tile_grid_zoom_wheel(self, event):
        if event.delta > 0:
            self._pal_zoom_in()
        else:
            self._pal_zoom_out()

    def _on_tile_grid_zoom_linux(self, event):
        if event.num == 4:
            self._pal_zoom_in()
        elif event.num == 5:
            self._pal_zoom_out()

    def _pal_zoom_in(self):
        self._pal_zoom_i = min(len(self._pal_zoom_levels) - 1, self._pal_zoom_i + 1)
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()
        if self.palette_mode.get() == "grid":
            self._refresh_tile_grid()

    def _pal_zoom_out(self):
        self._pal_zoom_i = max(0, self._pal_zoom_i - 1)
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()
        if self.palette_mode.get() == "grid":
            self._refresh_tile_grid()

    def _pal_zoom_reset(self):
        self._pal_zoom_i = 3
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()
        if self.palette_mode.get() == "grid":
            self._refresh_tile_grid()

    def _scale_image_ratio(self, img: tk.PhotoImage, n: int, d: int) -> tk.PhotoImage:
        out = img
        if n > 1:
            out = out.zoom(n, n)
        if d > 1:
            out = out.subsample(d, d)
        return out

    def _get_palette_thumb(self, tile_i: int) -> tk.PhotoImage | None:
        if not (0 <= tile_i < len(self.tile_images)):
            return None

        n, d = self._pal_zoom_levels[self._pal_zoom_i]
        # prefer anim frame 0 if animated
        if tile_i in self.animations:
            frames = self.animation_frames.get(tile_i)
            if frames:
                frame0 = frames[0]
                if n == 1 and d == 1:
                    return frame0
                key = (tile_i, 0, n, d)
                if key in self._pal_anim_thumb_cache:
                    return self._pal_anim_thumb_cache[key]
                out = self._scale_image_ratio(frame0, n, d)
                self._pal_anim_thumb_cache[key] = out
                return out

        # static
        if n == 1 and d == 1:
            return self.tile_images[tile_i]
        key2 = (tile_i, n, d)
        if key2 in self._pal_thumb_cache:
            return self._pal_thumb_cache[key2]
        out2 = self._scale_image_ratio(self.tile_images[tile_i], n, d)
        self._pal_thumb_cache[key2] = out2
        return out2

    def _refresh_tile_grid(self):
        self.tile_grid.delete("all")
        self._tile_grid_thumbs = []

        if not self.tile_images:
            self.tile_grid.configure(scrollregion=(0, 0, 1, 1))
            return

        pad = 6
        label_h = 14

        n, d = self._pal_zoom_levels[self._pal_zoom_i]
        thumb_w = max(1, (self.tile_w * n) // d)
        thumb_h = max(1, (self.tile_h * n) // d)

        cell_w = max(thumb_w + pad * 2, 52)
        cell_h = thumb_h + label_h + pad * 2

        width = max(1, self.tile_grid.winfo_width())
        cols = max(1, width // cell_w)

        self._tile_grid_cols = cols
        self._tile_grid_cell_w = cell_w
        self._tile_grid_cell_h = cell_h

        for i in range(len(self.tile_images)):
            r = i // cols
            c = i % cols
            x0 = c * cell_w
            y0 = r * cell_h

            # cell background
            self.tile_grid.create_rectangle(
                x0 + 2, y0 + 2, x0 + cell_w - 2, y0 + cell_h - 2,
                outline="#2a2a2a", fill="#141414", tags=(f"t{i}", "cell")
            )

            img = self._get_palette_thumb(i)
            if img is not None:
                self._tile_grid_thumbs.append(img)  # keep alive
                self.tile_grid.create_image(x0 + pad, y0 + pad, anchor="nw", image=img, tags=(f"t{i}", "thumb"))

            tag = " (anim)" if i in self.animations else ""
            self.tile_grid.create_text(
                x0 + cell_w // 2, y0 + pad + thumb_h + (label_h // 2),
                text=f"{i}{tag}", fill="#d0d0d0", tags=(f"t{i}", "label")
            )

        rows = (len(self.tile_images) + cols - 1) // cols
        total_w = cols * cell_w
        total_h = max(1, rows * cell_h)
        self.tile_grid.configure(scrollregion=(0, 0, total_w, total_h))

        self._refresh_tile_grid_selection()

    def _refresh_tile_grid_selection(self):
        if self.palette_mode.get() != "grid":
            return

        # clear any previous selection outlines
        self.tile_grid.delete("sel")

        if not self.selected_tiles:
            return

        cols = max(1, self._tile_grid_cols)
        cell_w = self._tile_grid_cell_w
        cell_h = self._tile_grid_cell_h

        for i in sorted(self.selected_tiles):
            if not (0 <= i < len(self.tile_images)):
                continue
            r = i // cols
            c = i % cols
            x0 = c * cell_w
            y0 = r * cell_h

            primary = (i == self.selected_tile)
            outline = "#ffffff" if primary else "#888888"
            width = 2 if primary else 1

            self.tile_grid.create_rectangle(
                x0 + 2, y0 + 2, x0 + cell_w - 2, y0 + cell_h - 2,
                outline=outline, width=width, tags=("sel",)
            )

        self.tile_grid.tag_raise("sel")

    def _sync_listbox_selection(self):
        self.tile_list.selection_clear(0, "end")
        for i in sorted(self.selected_tiles):
            if 0 <= i < len(self.tile_paths):
                self.tile_list.selection_set(i)
        if self.selected_tile is not None and 0 <= self.selected_tile < len(self.tile_paths):
            self.tile_list.see(self.selected_tile)

    def _on_tile_grid_click(self, event):
        if not self.tile_images:
            return

        x = int(self.tile_grid.canvasx(event.x))
        y = int(self.tile_grid.canvasy(event.y))

        cell_w = self._tile_grid_cell_w
        cell_h = self._tile_grid_cell_h
        cols = max(1, self._tile_grid_cols)

        c = x // max(1, cell_w)
        r = y // max(1, cell_h)
        idx = r * cols + c

        if not (0 <= idx < len(self.tile_images)):
            return

        # modifiers (best-effort across platforms)
        shift = bool(event.state & 0x0001)
        ctrl = bool(event.state & 0x0004)

        if shift and self._tile_grid_last_clicked is not None:
            a = self._tile_grid_last_clicked
            b = idx
            lo, hi = (a, b) if a <= b else (b, a)
            self.selected_tiles = set(range(lo, hi + 1))
        elif ctrl:
            if idx in self.selected_tiles:
                self.selected_tiles.remove(idx)
            else:
                self.selected_tiles.add(idx)
        else:
            self.selected_tiles = {idx}

        self._tile_grid_last_clicked = idx

        # primary tile = last clicked (if any selected)
        self.selected_tile = idx if idx in self.selected_tiles else (next(iter(self.selected_tiles)) if self.selected_tiles else None)

        self._sync_listbox_selection()
        self._sync_preview_and_attrs()
        self._refresh_tile_grid_selection()
        self._update_status("picked tile(s)")

    # ---------------- main zoom ----------------

    def _clear_scaled_caches(self):
        self._scaled_tile_cache.clear()
        self._scaled_anim_cache.clear()

    def _scale_image(self, img: tk.PhotoImage) -> tk.PhotoImage:
        n, d = self.scale_n, self.scale_d
        return self._scale_image_ratio(img, n, d)

    def _apply_zoom(self, new_zoom_i: int, keep_view: bool, anchor_event=None):
        new_zoom_i = max(0, min(new_zoom_i, len(self._zoom_levels) - 1))
        if new_zoom_i == self._zoom_i and keep_view:
            return

        # capture current view anchor
        x_frac, y_frac = self.canvas.xview()[0], self.canvas.yview()[0]
        anchor = None

        if anchor_event is not None:
            cur_x = self.canvas.canvasx(anchor_event.x)
            cur_y = self.canvas.canvasy(anchor_event.y)
            base_x = cur_x * self.scale_d / self.scale_n
            base_y = cur_y * self.scale_d / self.scale_n
            anchor = (base_x, base_y, anchor_event.x, anchor_event.y)

        self._zoom_i = new_zoom_i
        self.scale_n, self.scale_d = self._zoom_levels[self._zoom_i]
        self.view_tile_w = max(1, (self.tile_w * self.scale_n) // self.scale_d)
        self.view_tile_h = max(1, (self.tile_h * self.scale_n) // self.scale_d)

        self._clear_scaled_caches()
        self._rebuild_canvas()
        self._rebuild_all_tile_items()

        if anchor is not None:
            base_x, base_y, screen_x, screen_y = anchor
            new_total_w = max(1, self.map_w * self.view_tile_w)
            new_total_h = max(1, self.map_h * self.view_tile_h)

            new_x = base_x * self.scale_n / self.scale_d
            new_y = base_y * self.scale_n / self.scale_d

            origin_x = max(0, min(new_total_w - 1, new_x - screen_x))
            origin_y = max(0, min(new_total_h - 1, new_y - screen_y))

            self.canvas.xview_moveto(origin_x / new_total_w)
            self.canvas.yview_moveto(origin_y / new_total_h)
        elif keep_view:
            self.canvas.xview_moveto(x_frac)
            self.canvas.yview_moveto(y_frac)

        self._update_status()

    def _zoom_in(self):
        self._apply_zoom(self._zoom_i + 1, keep_view=True)

    def _zoom_out(self):
        self._apply_zoom(self._zoom_i - 1, keep_view=True)

    def _zoom_reset(self):
        self._apply_zoom(3, keep_view=True)

    def _on_zoom_wheel(self, event):
        if event.delta > 0:
            self._apply_zoom(self._zoom_i + 1, keep_view=True, anchor_event=event)
        else:
            self._apply_zoom(self._zoom_i - 1, keep_view=True, anchor_event=event)

    def _on_zoom_linux(self, event):
        if event.num == 4:
            self._apply_zoom(self._zoom_i + 1, keep_view=True, anchor_event=event)
        elif event.num == 5:
            self._apply_zoom(self._zoom_i - 1, keep_view=True, anchor_event=event)

    # ---------------- map init/reset ----------------

    def _new_map(self):
        # ensure max canvas exists
        self.max_map_w = max(1, min(int(self.max_map_w), 255))
        self.max_map_h = max(1, min(int(self.max_map_h), 255))

        d = NewMapDialog(self, self.max_map_w, self.max_map_h)
        if not d.result:
            return

        tw, th, dw, dh, layers = d.result
        self.tile_w, self.tile_h = tw, th

        # max canvas size is what gets saved
        self.map_w, self.map_h = int(self.max_map_w), int(self.max_map_h)
        self.num_layers = layers

        # drawable size is what you can paint on (centered within max canvas)
        self.draw_w, self.draw_h = int(dw), int(dh)
        self._recompute_draw_origin_centered()

        self.tile_paths = []
        self.tile_images = []
        self.selected_tile = None
        self.selected_tiles = set()
        self.attributes = {}
        self.animations = {}
        self.animation_frames = {}
        self.animated_items = {}

        cell_count = self.map_w * self.map_h
        self.layers = [[EMPTY_TILE] * cell_count for _ in range(self.num_layers)]
        self.cell_items = [[None] * cell_count for _ in range(self.num_layers)]
        self.layer_visible = [True] * self.num_layers
        self.current_layer = 0
        self.prop_layer = EMPTY_TILE
        self.map_path = None

        self.undo_stack = []
        self._undo_current = None
        self._undo_in_progress = False

        self._on_tool_changed()
        self._apply_zoom(3, keep_view=False)

        self._refresh_tile_list()
        self._refresh_layer_list()
        self._rebuild_canvas()
        self._sync_preview_and_attrs()
        self._update_status("new map")

    def _draw_drawable_border(self):
        self.canvas.delete("drawable_border")
        if self.map_w <= 0 or self.map_h <= 0:
            return
        if self.draw_w <= 0 or self.draw_h <= 0:
            return

        px0 = self.draw_x0 * self.view_tile_w
        py0 = self.draw_y0 * self.view_tile_h
        px1 = (self.draw_x0 + self.draw_w) * self.view_tile_w
        py1 = (self.draw_y0 + self.draw_h) * self.view_tile_h

        self.canvas.create_rectangle(
            px0, py0, px1, py1,
            outline="#5ad7ff", width=2, dash=(6, 4),
            tags=("drawable_border",)
        )
        self.canvas.tag_raise("drawable_border")

    def _rebuild_canvas(self):
        self.canvas.delete("all")
        wpx = self.map_w * self.view_tile_w
        hpx = self.map_h * self.view_tile_h
        self.canvas.configure(scrollregion=(0, 0, wpx, hpx))
        self._draw_grid_lines()
        self._draw_drawable_border()
        cell_count = self.map_w * self.map_h
        self.cell_items = [[None] * cell_count for _ in range(self.num_layers)]
        self.animated_items = {}
        self._clear_rect_preview()
        self._clear_line_preview()

    def _draw_grid_lines(self):
        wpx = self.map_w * self.view_tile_w
        hpx = self.map_h * self.view_tile_h
        for x in range(self.map_w + 1):
            px = x * self.view_tile_w
            self.canvas.create_line(px, 0, px, hpx, fill="#333333", tags=("grid",))
        for y in range(self.map_h + 1):
            py = y * self.view_tile_h
            self.canvas.create_line(0, py, wpx, py, fill="#333333", tags=("grid",))

    # ---------------- palette ----------------

    def _refresh_tile_list(self):
        self.tile_list.delete(0, "end")
        for i, p in enumerate(self.tile_paths):
            name = os.path.basename(p.replace("/", os.sep))
            tag = " (anim)" if i in self.animations else ""
            self.tile_list.insert("end", f"{i}: {name}{tag}")

        # keep selection sane
        self.selected_tiles = {i for i in self.selected_tiles if 0 <= i < len(self.tile_paths)}
        if self.selected_tile is not None and not (0 <= self.selected_tile < len(self.tile_paths)):
            self.selected_tile = None

        if not self.selected_tiles and self.tile_paths:
            self.selected_tiles = {0}
            self.selected_tile = 0
        elif self.selected_tiles and self.selected_tile is None:
            self.selected_tile = next(iter(self.selected_tiles))

        self._sync_listbox_selection()

        if self.palette_mode.get() == "grid":
            self._refresh_tile_grid()
        else:
            self._refresh_tile_grid_selection()

    def _on_tile_select(self, _evt=None):
        sel = self.tile_list.curselection()
        self.selected_tiles = set(int(i) for i in sel)
        if not self.selected_tiles:
            self.selected_tile = None
        else:
            self.selected_tile = int(sel[-1])  # primary = last selected in list
        self._sync_preview_and_attrs()
        self._refresh_tile_grid_selection()
        self._update_status()

    def _sync_preview_and_attrs(self):
        # preview
        if self.selected_tile is None or not (0 <= self.selected_tile < len(self.tile_images)):
            self.tile_preview.configure(text="(no tile selected)", image="")
        else:
            img = self.tile_images[self.selected_tile]
            self.tile_preview.configure(image=img, text="")
            self.tile_preview.image = img

        # attributes title
        if len(self.selected_tiles) > 1:
            self.attr_title.configure(text=f"attributes for selected tiles ({len(self.selected_tiles)})")
        else:
            self.attr_title.configure(text="attributes for selected tile")

        # attributes list (shown for primary tile)
        self.attr_list.delete(0, "end")
        if self.selected_tile is None:
            self._sync_anim_fields()
            return
        for a in self.attributes.get(self.selected_tile, []):
            self.attr_list.insert("end", a)

        self._sync_anim_fields()

    def _sync_anim_fields(self):
        if self.selected_tile is None:
            self.anim_folder.delete(0, "end")
            self.anim_ms.delete(0, "end")
            self.anim_count.delete(0, "end")
            self.anim_info.configure(text="(none)")
            return

        if self.selected_tile in self.animations:
            folder, ms, cnt = self.animations[self.selected_tile]
            self.anim_folder.delete(0, "end")
            self.anim_folder.insert(0, folder)
            self.anim_ms.delete(0, "end")
            self.anim_ms.insert(0, str(ms))
            self.anim_count.delete(0, "end")
            self.anim_count.insert(0, str(cnt))
            self.anim_info.configure(text=f"using: {folder} | {ms}ms | {cnt} frames")
        else:
            self.anim_folder.delete(0, "end")
            self.anim_ms.delete(0, "end")
            self.anim_count.delete(0, "end")
            self.anim_info.configure(text="(none)")

    def _load_tiles(self):
        if self.map_w <= 0:
            messagebox.showerror("no map", "create a map first")
            return

        paths = filedialog.askopenfilenames(
            title="select png tile(s)",
            filetypes=[("png images", "*.png"), ("all files", "*.*")]
        )
        if not paths:
            return

        added = 0
        for p in paths:
            try:
                img = tk.PhotoImage(file=p)
            except Exception as e:
                messagebox.showerror("load failed", f"could not load:\n{p}\n\n{e}")
                continue

            if img.width() != self.tile_w or img.height() != self.tile_h:
                messagebox.showerror(
                    "size mismatch",
                    f"tile must be exactly {self.tile_w}x{self.tile_h}\n\n{p}\nwas {img.width()}x{img.height()}"
                )
                continue

            self.tile_images.append(img)
            self.tile_paths.append(_abs_to_rel_base(p, self.root_dir))
            added += 1

        if added:
            self._clear_scaled_caches()
            self._pal_thumb_cache.clear()
            self._pal_anim_thumb_cache.clear()
            self._refresh_tile_list()
            self._sync_preview_and_attrs()

            # refresh rendered tiles without nuking the whole canvas
            self._rebuild_all_tile_items()

            self._update_status(f"tiles loaded: +{added}")

    def _is_blank_tile(self, img: tk.PhotoImage) -> bool:
        """
        tries to detect fully transparent tiles.
        note: transparency_get support can vary by tk build; fallback is conservative.
        """
        w, h = img.width(), img.height()
        try:
            for y in range(h):
                for x in range(w):
                    if not img.transparency_get(x, y):
                        return False
            return True
        except Exception:
            try:
                first = img.get(0, 0)
                for y in range(h):
                    for x in range(w):
                        if img.get(x, y) != first:
                            return False
                return first == ""
            except Exception:
                return False

    def _split_tilesheet(self):
        if self.map_w <= 0:
            messagebox.showerror("no map", "create a map first")
            return

        sheet_path = filedialog.askopenfilename(
            title="select tilesheet png",
            filetypes=[("png images", "*.png"), ("all files", "*.*")]
        )
        if not sheet_path:
            return

        gap_x = simpledialog.askinteger("gap", "horizontal gap between tiles (px)", minvalue=0, maxvalue=9999)
        if gap_x is None:
            return
        gap_y = simpledialog.askinteger("gap", "vertical gap between tiles (px)", minvalue=0, maxvalue=9999)
        if gap_y is None:
            return

        out_dir = filedialog.askdirectory(title="output folder for sliced tiles")
        if not out_dir:
            return

        try:
            sheet = tk.PhotoImage(file=sheet_path)
        except Exception as e:
            messagebox.showerror("load failed", f"could not load tilesheet:\n{sheet_path}\n\n{e}")
            return

        sw, sh = sheet.width(), sheet.height()
        step_x = self.tile_w + gap_x
        step_y = self.tile_h + gap_y

        if step_x <= 0 or step_y <= 0:
            messagebox.showerror("invalid", "invalid tile size / gap")
            return

        base = os.path.splitext(os.path.basename(sheet_path))[0]
        added = 0
        skipped_blank = 0

        y = 0
        while y + self.tile_h <= sh:
            x = 0
            while x + self.tile_w <= sw:
                tile_img = tk.PhotoImage(width=self.tile_w, height=self.tile_h)
                try:
                    tile_img.tk.call(
                        tile_img, "copy", sheet,
                        "-from", x, y, x + self.tile_w, y + self.tile_h,
                        "-to", 0, 0
                    )
                except Exception as e:
                    messagebox.showerror("split failed", f"copy failed at {x},{y}\n\n{e}")
                    return

                if self._is_blank_tile(tile_img):
                    skipped_blank += 1
                    x += step_x
                    continue

                out_name = f"{base}_{added}.png"
                out_path = os.path.join(out_dir, out_name)

                try:
                    tile_img.write(out_path, format="png")
                except Exception as e:
                    messagebox.showerror(
                        "save failed",
                        f"could not write png:\n{out_path}\n\n{e}\n\n(note: some tk builds may not support png write)"
                    )
                    return

                self.tile_images.append(tk.PhotoImage(file=out_path))
                self.tile_paths.append(_abs_to_rel_base(out_path, self.root_dir))
                added += 1
                x += step_x
            y += step_y

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()
        self._refresh_tile_list()
        self._sync_preview_and_attrs()

        # refresh rendered tiles without nuking the whole canvas
        self._rebuild_all_tile_items()

        self._update_status(f"split tilesheet: added {added}, skipped {skipped_blank} blank")

    def _remove_selected_tile(self):
        if self.selected_tile is None:
            return
        idx = self.selected_tile
        if not (0 <= idx < len(self.tile_paths)):
            return

        # remove animation if it exists for this idx
        if idx in self.animations:
            self._remove_animation_idx(idx)

        # remove from tile lists
        del self.tile_paths[idx]
        del self.tile_images[idx]

        # shift attributes
        new_attrs: dict[int, list[str]] = {}
        for k, v in self.attributes.items():
            if k == idx:
                continue
            if 0 <= k < idx:
                new_attrs[k] = v
            elif k > idx:
                new_attrs[k - 1] = v
            else:
                new_attrs[k] = v
        self.attributes = new_attrs

        # shift animations + frames
        new_anims: dict[int, tuple[str, int, int]] = {}
        new_frames: dict[int, list[tk.PhotoImage]] = {}
        for k, v in self.animations.items():
            if k == idx:
                continue
            if 0 <= k < idx:
                new_anims[k] = v
                if k in self.animation_frames:
                    new_frames[k] = self.animation_frames[k]
            elif k > idx:
                new_anims[k - 1] = v
                if k in self.animation_frames:
                    new_frames[k - 1] = self.animation_frames[k]
            else:
                new_anims[k] = v
                if k in self.animation_frames:
                    new_frames[k] = self.animation_frames[k]
        self.animations = new_anims
        self.animation_frames = new_frames

        # update all layer indices (shift > idx down by 1, set removed ones to empty)
        for li in range(self.num_layers):
            layer = self.layers[li]
            for c in range(len(layer)):
                v = layer[c]
                if v == idx:
                    layer[c] = EMPTY_TILE
                elif v != EMPTY_TILE and 0 <= v and v > idx and v < 0x7FFFFFFF + 1:
                    layer[c] = v - 1

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self._refresh_tile_list()
        self._rebuild_all_tile_items()

        # reset selection to single
        if self.tile_paths:
            self.selected_tile = min(idx, len(self.tile_paths) - 1)
            self.selected_tiles = {self.selected_tile}
            self._sync_listbox_selection()
        else:
            self.selected_tile = None
            self.selected_tiles = set()

        self._sync_preview_and_attrs()
        self._refresh_tile_grid_selection()
        self._update_status("tile removed")

    # ---------------- tile reordering ----------------

    def _move_selected_tile(self, delta: int):
        if self.selected_tile is None:
            return
        idx = self.selected_tile
        n = len(self.tile_paths)
        if n <= 1:
            return
        new_idx = idx + delta
        if not (0 <= new_idx < n):
            return

        order = list(range(n))
        order[idx], order[new_idx] = order[new_idx], order[idx]
        self._apply_tile_reorder(order)

    def _move_selected_tile_to(self):
        if self.selected_tile is None:
            return
        n = len(self.tile_paths)
        if n <= 1:
            return

        target = simpledialog.askinteger("move tile", f"move tile {self.selected_tile} to index (0..{n-1})",
                                         minvalue=0, maxvalue=max(0, n - 1))
        if target is None:
            return

        src = self.selected_tile
        if target == src:
            return

        order = list(range(n))
        val = order.pop(src)
        order.insert(target, val)
        self._apply_tile_reorder(order)

    def _apply_tile_reorder(self, new_order: list[int]):
        n = len(self.tile_paths)
        if len(new_order) != n or sorted(new_order) != list(range(n)):
            return

        # mapping old->new
        old_to_new = [0] * n
        for new_i, old_i in enumerate(new_order):
            old_to_new[old_i] = new_i

        # reorder tiles
        self.tile_paths = [self.tile_paths[old_i] for old_i in new_order]
        self.tile_images = [self.tile_images[old_i] for old_i in new_order]

        # remap attributes
        new_attrs: dict[int, list[str]] = {}
        for k, v in self.attributes.items():
            if 0 <= k < n:
                new_attrs[old_to_new[k]] = v
            else:
                new_attrs[k] = v
        self.attributes = new_attrs

        # remap animations + frames
        new_anims: dict[int, tuple[str, int, int]] = {}
        new_frames: dict[int, list[tk.PhotoImage]] = {}
        for k, v in self.animations.items():
            if 0 <= k < n:
                nk = old_to_new[k]
                new_anims[nk] = v
                if k in self.animation_frames:
                    new_frames[nk] = self.animation_frames[k]
            else:
                new_anims[k] = v
                if k in self.animation_frames:
                    new_frames[k] = self.animation_frames[k]
        self.animations = new_anims
        self.animation_frames = new_frames

        # remap layer cell indices
        for li in range(self.num_layers):
            layer = self.layers[li]
            for i in range(len(layer)):
                v = layer[i]
                if v == EMPTY_TILE:
                    continue
                if 0 <= v < n:
                    layer[i] = old_to_new[v]

        # remap selections
        if self.selected_tiles:
            self.selected_tiles = {old_to_new[i] for i in self.selected_tiles if 0 <= i < n}
        if self.selected_tile is not None and 0 <= self.selected_tile < n:
            self.selected_tile = old_to_new[self.selected_tile]

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self._refresh_tile_list()
        self._rebuild_all_tile_items()
        self._sync_preview_and_attrs()
        self._refresh_tile_grid_selection()

        self._update_status("tiles reordered")

    # ---------------- attributes ----------------

    def _get_attr_targets(self) -> list[int]:
        # apply to all selected tiles; if none, use primary; if still none, empty
        if self.selected_tiles:
            return sorted(i for i in self.selected_tiles if 0 <= i < len(self.tile_images))
        if self.selected_tile is not None:
            return [self.selected_tile]
        return []

    def _add_attr(self):
        targets = self._get_attr_targets()
        if not targets:
            return
        raw = self.attr_entry.get()
        try:
            a = _safe_attr(raw)
        except Exception as e:
            messagebox.showerror("invalid attribute", str(e))
            return
        if not a:
            return

        changed = 0
        for t in targets:
            lst = self.attributes.setdefault(t, [])
            if a not in lst:
                lst.append(a)
                changed += 1

        self.attr_entry.delete(0, "end")
        self._sync_preview_and_attrs()
        self._update_status(f"attr added ({changed} tile(s))")

    def _remove_attr(self):
        targets = self._get_attr_targets()
        if not targets:
            return
        sel = self.attr_list.curselection()
        if not sel:
            return
        i = int(sel[0])
        # remove by value so it works across tiles
        name = self.attr_list.get(i)

        changed = 0
        for t in targets:
            lst = self.attributes.get(t, [])
            if name in lst:
                lst.remove(name)
                changed += 1
            if not lst and t in self.attributes:
                del self.attributes[t]

        self._sync_preview_and_attrs()
        self._update_status(f"attr removed ({changed} tile(s))")

    # ---------------- animation ----------------

    def _on_anim_toggle(self):
        if not self.anim_enabled.get():
            # stop loop
            if self._anim_job is not None:
                try:
                    self.after_cancel(self._anim_job)
                except Exception:
                    pass
                self._anim_job = None

            # freeze animated tiles to frame 0
            self._apply_anim_frame0_to_all_items()
            self.animated_items = {}
            self._update_status("animation paused")
        else:
            # rebuild registry and restart
            self._rebuild_anim_registry()
            self._ensure_anim_loop()
            self._update_status("animation enabled")

    def _apply_anim_frame0_to_all_items(self):
        if not self.animations:
            return
        for li in range(self.num_layers):
            for idx, v in enumerate(self.layers[li]):
                if v == EMPTY_TILE:
                    continue
                if v not in self.animations:
                    continue
                item_id = self.cell_items[li][idx] if li < len(self.cell_items) and idx < len(self.cell_items[li]) else None
                if not item_id:
                    continue
                img = self._get_scaled_anim_frame(v, 0)
                if img is None:
                    continue
                try:
                    self.canvas.itemconfigure(item_id, image=img)
                except Exception:
                    pass

    def _rebuild_anim_registry(self):
        self.animated_items = {}
        if not self.anim_enabled.get():
            return
        if not self.animations:
            return
        for li in range(self.num_layers):
            for idx, v in enumerate(self.layers[li]):
                if v == EMPTY_TILE:
                    continue
                if v not in self.animations:
                    continue
                item_id = self.cell_items[li][idx] if li < len(self.cell_items) and idx < len(self.cell_items[li]) else None
                if item_id:
                    self.animated_items.setdefault(v, set()).add(item_id)

    def _browse_anim_folder(self):
        folder = filedialog.askdirectory(title="select animation folder (contains 1.png, 2.png, ...)")
        if not folder:
            return
        rel = _abs_to_rel_base(folder, self.root_dir)
        self.anim_folder.delete(0, "end")
        self.anim_folder.insert(0, rel)

    def _load_animation_frames(self, folder_rel: str, frame_count: int, map_path_for_fallback: str | None) -> list[tk.PhotoImage]:
        folder_abs = _resolve_path(folder_rel, map_path_for_fallback, self.root_dir)
        frames: list[tk.PhotoImage] = []
        for i in range(1, frame_count + 1):
            fp = os.path.join(folder_abs, f"{i}.png")
            if not os.path.exists(fp):
                raise ValueError(f"missing frame: {fp}")
            img = tk.PhotoImage(file=fp)
            if img.width() != self.tile_w or img.height() != self.tile_h:
                raise ValueError(
                    f"frame size mismatch: {fp} expected {self.tile_w}x{self.tile_h}, got {img.width()}x{img.height()}"
                )
            frames.append(img)
        return frames

    def _set_animation_for_selected(self):
        if self.selected_tile is None:
            return
        if not (0 <= self.selected_tile < len(self.tile_images)):
            return

        folder = self.anim_folder.get().strip()
        if not folder:
            messagebox.showerror("invalid", "animation folder is required")
            return

        try:
            ms = int(self.anim_ms.get().strip())
            cnt = int(self.anim_count.get().strip())
        except Exception:
            messagebox.showerror("invalid", "frame ms and frames must be integers")
            return

        if ms < 1 or ms > 255 or cnt < 1 or cnt > 255:
            messagebox.showerror("invalid", "frame ms and frames must be 1..255")
            return

        folder_rel = _to_forward_slashes(folder)
        if os.path.isabs(folder_rel.replace("/", os.sep)):
            folder_rel = _abs_to_rel_base(folder_rel, self.root_dir)

        try:
            frames = self._load_animation_frames(folder_rel, cnt, self.map_path)
        except Exception as e:
            messagebox.showerror("invalid animation", str(e))
            return

        self.animations[self.selected_tile] = (folder_rel, ms, cnt)
        self.animation_frames[self.selected_tile] = frames

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self._refresh_tile_list()
        self._rebuild_all_tile_items()
        self._sync_preview_and_attrs()
        self._ensure_anim_loop()
        self._update_status("animation set")

    def _remove_animation_idx(self, idx: int):
        if idx in self.animations:
            del self.animations[idx]
        if idx in self.animation_frames:
            del self.animation_frames[idx]
        if idx in self.animated_items:
            del self.animated_items[idx]

    def _clear_animation_for_selected(self):
        if self.selected_tile is None:
            return
        if self.selected_tile not in self.animations:
            return
        self._remove_animation_idx(self.selected_tile)

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self._refresh_tile_list()
        self._rebuild_all_tile_items()
        self._sync_preview_and_attrs()
        self._update_status("animation cleared")

    def _ensure_anim_loop(self):
        if not self.anim_enabled.get():
            return
        if self._anim_job is None and self.animations:
            self._anim_start_time = time.monotonic()
            self._anim_job = self.after(16, self._anim_tick)

    def _get_scaled_static_tile(self, tile_i: int) -> tk.PhotoImage | None:
        if not (0 <= tile_i < len(self.tile_images)):
            return None
        n, d = self.scale_n, self.scale_d
        if n == 1 and d == 1:
            return self.tile_images[tile_i]
        key = (tile_i, n, d)
        if key in self._scaled_tile_cache:
            return self._scaled_tile_cache[key]
        img = self._scale_image(self.tile_images[tile_i])
        self._scaled_tile_cache[key] = img
        return img

    def _get_scaled_anim_frame(self, tile_i: int, frame_i: int) -> tk.PhotoImage | None:
        frames = self.animation_frames.get(tile_i)
        if not frames:
            return None
        frame_i = max(0, min(frame_i, len(frames) - 1))
        n, d = self.scale_n, self.scale_d
        if n == 1 and d == 1:
            return frames[frame_i]
        key = (tile_i, frame_i, n, d)
        if key in self._scaled_anim_cache:
            return self._scaled_anim_cache[key]
        img = self._scale_image(frames[frame_i])
        self._scaled_anim_cache[key] = img
        return img

    def _anim_tick(self):
        if not self.anim_enabled.get():
            self._anim_job = None
            return

        try:
            if not self.animations:
                self._anim_job = None
                return

            now = time.monotonic()
            elapsed_ms = (now - self._anim_start_time) * 1000.0

            any_items = False
            for idx, (_folder_rel, ms, cnt) in list(self.animations.items()):
                items = self.animated_items.get(idx)
                frames = self.animation_frames.get(idx)
                if not frames or not items:
                    continue

                any_items = True
                if ms <= 0:
                    continue
                frame_i = int(elapsed_ms // ms) % max(1, cnt)
                frame_i = min(frame_i, len(frames) - 1)

                img = self._get_scaled_anim_frame(idx, frame_i)
                if img is None:
                    continue

                dead: set[int] = set()
                for item_id in list(items):
                    try:
                        self.canvas.itemconfigure(item_id, image=img)
                    except Exception:
                        dead.add(item_id)
                if dead:
                    items.difference_update(dead)

            delay = 16 if any_items else 120
            self._anim_job = self.after(delay, self._anim_tick)
        except Exception:
            self._anim_job = self.after(120, self._anim_tick)

    # ---------------- layers ----------------

    def _refresh_layer_list(self):
        self.layer_list.delete(0, "end")
        for i in range(self.num_layers):
            vis = "x" if self.layer_visible[i] else " "
            prop = " (prop)" if self.prop_layer == i else ""
            cur = " *" if self.current_layer == i else ""
            self.layer_list.insert("end", f"[{vis}] layer {i}{prop}{cur}")
        self.layer_list.selection_clear(0, "end")
        if 0 <= self.current_layer < self.num_layers:
            self.layer_list.selection_set(self.current_layer)

        if self.prop_layer == EMPTY_TILE:
            self.prop_label.configure(text="none")
        else:
            self.prop_label.configure(text=str(self.prop_layer))

    def _on_layer_select(self, _evt=None):
        sel = self.layer_list.curselection()
        if not sel:
            return
        self.current_layer = int(sel[0])
        self._refresh_layer_list()
        self._update_status()

    def _toggle_layer_visibility(self, _evt=None):
        sel = self.layer_list.curselection()
        if not sel:
            return
        li = int(sel[0])
        self.layer_visible[li] = not self.layer_visible[li]
        tag = f"layer{li}"
        self.canvas.itemconfigure(tag, state=("normal" if self.layer_visible[li] else "hidden"))
        self._refresh_layer_list()
        self._update_status("layer visibility toggled")

    def _add_layer(self):
        if self.map_w <= 0:
            return
        cell_count = self.map_w * self.map_h
        self.layers.append([EMPTY_TILE] * cell_count)
        self.cell_items.append([None] * cell_count)
        self.layer_visible.append(True)
        self.num_layers += 1
        self._refresh_layer_list()
        self._update_status("layer added")

    def _remove_layer(self):
        if self.num_layers <= 1:
            messagebox.showerror("limit", "must have at least 1 layer")
            return
        li = self.current_layer
        self.canvas.delete(f"layer{li}")

        del self.layers[li]
        del self.cell_items[li]
        del self.layer_visible[li]
        self.num_layers -= 1

        if self.prop_layer == li:
            self.prop_layer = EMPTY_TILE
        elif self.prop_layer != EMPTY_TILE and self.prop_layer > li:
            self.prop_layer -= 1

        self.current_layer = max(0, min(self.current_layer, self.num_layers - 1))
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        self._refresh_layer_list()
        self._update_status("layer removed")

    def _move_layer(self, delta: int):
        if self.num_layers <= 1:
            return
        li = self.current_layer
        new_i = li + delta
        if not (0 <= new_i < self.num_layers):
            return

        # swap layer data
        self.layers[li], self.layers[new_i] = self.layers[new_i], self.layers[li]
        self.cell_items[li], self.cell_items[new_i] = self.cell_items[new_i], self.cell_items[li]
        self.layer_visible[li], self.layer_visible[new_i] = self.layer_visible[new_i], self.layer_visible[li]

        # prop layer tracking
        if self.prop_layer == li:
            self.prop_layer = new_i
        elif self.prop_layer == new_i:
            self.prop_layer = li

        self.current_layer = new_i

        # re-render (tags depend on indices)
        x_frac, y_frac = self.canvas.xview()[0], self.canvas.yview()[0]
        self.undo_stack = []  # layer indices changed; safest is to drop undo history
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        try:
            self.canvas.xview_moveto(x_frac)
            self.canvas.yview_moveto(y_frac)
        except Exception:
            pass
        self._refresh_layer_list()
        self._update_status("layer moved")

    def _set_prop_layer(self):
        self.prop_layer = self.current_layer
        self._refresh_layer_list()
        self._update_status("prop layer set")

    def _clear_prop_layer(self):
        self.prop_layer = EMPTY_TILE
        self._refresh_layer_list()
        self._update_status("prop layer cleared")

    # ---------------- tools / painting ----------------

    def _event_to_cell(self, event, require_drawable: bool = True) -> tuple[int, int] | None:
        if self.map_w <= 0:
            return None
        x = int(self.canvas.canvasx(event.x))
        y = int(self.canvas.canvasy(event.y))
        cx = x // max(1, self.view_tile_w)
        cy = y // max(1, self.view_tile_h)
        if cx < 0 or cy < 0 or cx >= self.map_w or cy >= self.map_h:
            return None
        if require_drawable and (not self._in_drawable(cx, cy)):
            return None
        return cx, cy

    def _pick_at_event(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        cx, cy = pos
        idx = cy * self.map_w + cx

        picked: int | None = None
        for li in range(self.num_layers - 1, -1, -1):
            if not self.layer_visible[li]:
                continue
            v = self.layers[li][idx]
            if v != EMPTY_TILE:
                picked = v
                break

        if picked is None or not (0 <= picked < len(self.tile_images)):
            self.selected_tile = None
            self.selected_tiles = set()
            self.tile_list.selection_clear(0, "end")
        else:
            self.selected_tile = picked
            self.selected_tiles = {picked}
            self._sync_listbox_selection()

        self._sync_preview_and_attrs()
        self._refresh_tile_grid_selection()
        self._update_status("picked tile")

    # left handlers
    def _on_left_down(self, event):
        mode = self.tool_mode.get()
        if mode == "brush":
            self._begin_undo_recording()
            self._paint_at_event(event)
        elif mode == "rect":
            self._begin_undo_recording()
            self._rect_begin(event)
        elif mode == "fill":
            self._begin_undo_recording()
            self._fill_at_event(event)
            self._commit_undo_recording("fill applied")
        elif mode == "line":
            self._begin_undo_recording()
            self._line_begin(event)

    def _on_left_drag(self, event):
        mode = self.tool_mode.get()
        if mode == "brush":
            self._paint_at_event(event)
        elif mode == "rect":
            self._rect_update(event)
        elif mode == "line":
            self._line_update(event)

    def _on_left_up(self, event):
        mode = self.tool_mode.get()
        if mode == "brush":
            self._commit_undo_recording()
        elif mode == "rect":
            self._rect_commit(event)
            self._commit_undo_recording()
        elif mode == "line":
            self._line_commit(event)
            self._commit_undo_recording()

    # right handlers (erase ONLY in brush mode)
    def _on_right_down(self, event):
        if self.tool_mode.get() != "brush":
            return
        self._begin_undo_recording()
        self._erase_at_event(event)

    def _on_right_drag(self, event):
        if self.tool_mode.get() != "brush":
            return
        self._erase_at_event(event)

    def _on_right_up(self, event):
        if self.tool_mode.get() != "brush":
            return
        self._commit_undo_recording()

    # brush ops
    def _paint_at_event(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        if self.selected_tile is None:
            return
        if not (0 <= self.selected_tile < len(self.tile_images)):
            return
        cx, cy = pos
        self._set_cell(self.current_layer, cx, cy, self.selected_tile)

    def _erase_at_event(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        cx, cy = pos
        self._set_cell(self.current_layer, cx, cy, EMPTY_TILE)

    # rectangle tool (paint only)
    def _rect_begin(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        if self.selected_tile is None or not (0 <= self.selected_tile < len(self.tile_images)):
            return
        self._rect_value = self.selected_tile
        self._rect_start = pos
        self._rect_end = pos
        self._draw_rect_preview()
        self._update_status("rectangle start")

    def _rect_update(self, event):
        if self._rect_start is None:
            return
        pos = self._event_to_cell(event, require_drawable=False)
        if pos is None:
            return
        self._rect_end = pos
        self._draw_rect_preview()

    def _rect_commit(self, event):
        if self._rect_start is None or self._rect_end is None:
            return

        x0, y0 = self._rect_start
        x1, y1 = self._rect_end
        minx, maxx = (x0, x1) if x0 <= x1 else (x1, x0)
        miny, maxy = (y0, y1) if y0 <= y1 else (y1, y0)

        # clamp to drawable bounds
        dx0, dy0, dx1, dy1 = self._drawable_bounds()
        minx = max(minx, dx0)
        maxx = min(maxx, dx1)
        miny = max(miny, dy0)
        maxy = min(maxy, dy1)

        val = self._rect_value

        changed = 0
        if minx <= maxx and miny <= maxy:
            for cy in range(miny, maxy + 1):
                for cx in range(minx, maxx + 1):
                    if self._set_cell(self.current_layer, cx, cy, val, update_status=False):
                        changed += 1

        self._clear_rect_preview()
        self._rect_start = None
        self._rect_end = None
        self._update_status(f"rectangle applied ({changed} cells)")

    def _clear_rect_preview(self):
        if self._rect_preview_id is not None:
            try:
                self.canvas.delete(self._rect_preview_id)
            except Exception:
                pass
        self._rect_preview_id = None

    def _draw_rect_preview(self):
        self._clear_rect_preview()
        if self._rect_start is None or self._rect_end is None:
            return

        x0, y0 = self._rect_start
        x1, y1 = self._rect_end
        minx, maxx = (x0, x1) if x0 <= x1 else (x1, x0)
        miny, maxy = (y0, y1) if y0 <= y1 else (y1, y0)

        # clamp preview to drawable bounds
        dx0, dy0, dx1, dy1 = self._drawable_bounds()
        minx = max(minx, dx0)
        maxx = min(maxx, dx1)
        miny = max(miny, dy0)
        maxy = min(maxy, dy1)

        if minx > maxx or miny > maxy:
            return

        px0 = minx * self.view_tile_w
        py0 = miny * self.view_tile_h
        px1 = (maxx + 1) * self.view_tile_w
        py1 = (maxy + 1) * self.view_tile_h

        self._rect_preview_id = self.canvas.create_rectangle(
            px0, py0, px1, py1,
            outline="#ffffff", width=2, dash=(4, 4),
            tags=("rect_preview",)
        )
        self.canvas.tag_raise("rect_preview")
        self.canvas.tag_raise("drawable_border")

    # fill bucket tool (paint only)
    def _fill_at_event(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        if self.selected_tile is None or not (0 <= self.selected_tile < len(self.tile_images)):
            return

        cx, cy = pos
        start_idx = cy * self.map_w + cx
        new_val = self.selected_tile

        layer = self.layers[self.current_layer]
        old_val = layer[start_idx]
        if old_val == new_val:
            self._update_status("fill: no-op")
            return

        # bfs flood fill (4-way), restricted to drawable bounds
        w = self.map_w
        h = self.map_h
        cell_count = w * h
        seen = [False] * cell_count
        q = deque([start_idx])
        seen[start_idx] = True

        dx0, dy0, dx1, dy1 = self._drawable_bounds()

        changed = 0
        while q:
            i = q.popleft()
            if layer[i] != old_val:
                continue

            x = i % w
            y = i // w

            if not (dx0 <= x <= dx1 and dy0 <= y <= dy1):
                continue

            if self._set_cell(self.current_layer, x, y, new_val, update_status=False):
                changed += 1

            # neighbors (stay inside drawable)
            if x > dx0:
                j = i - 1
                if not seen[j]:
                    seen[j] = True
                    q.append(j)
            if x < dx1:
                j = i + 1
                if not seen[j]:
                    seen[j] = True
                    q.append(j)
            if y > dy0:
                j = i - w
                if not seen[j]:
                    seen[j] = True
                    q.append(j)
            if y < dy1:
                j = i + w
                if not seen[j]:
                    seen[j] = True
                    q.append(j)

        self._update_status(f"fill applied ({changed} cells)")

    # line tool (paint only)
    def _clear_line_preview(self):
        if self._line_preview_id is not None:
            try:
                self.canvas.delete(self._line_preview_id)
            except Exception:
                pass
        self._line_preview_id = None

    def _draw_line_preview(self):
        self._clear_line_preview()
        if self._line_start is None or self._line_end is None:
            return
        x0, y0 = self._line_start
        x1, y1 = self._line_end

        px0 = (x0 + 0.5) * self.view_tile_w
        py0 = (y0 + 0.5) * self.view_tile_h
        px1 = (x1 + 0.5) * self.view_tile_w
        py1 = (y1 + 0.5) * self.view_tile_h

        self._line_preview_id = self.canvas.create_line(
            px0, py0, px1, py1,
            fill="#ffffff", width=2, dash=(4, 4),
            tags=("line_preview",)
        )
        self.canvas.tag_raise("line_preview")
        self.canvas.tag_raise("drawable_border")

    def _line_begin(self, event):
        pos = self._event_to_cell(event, require_drawable=True)
        if pos is None:
            return
        if self.selected_tile is None or not (0 <= self.selected_tile < len(self.tile_images)):
            return
        self._line_start = pos
        self._line_end = pos
        self._draw_line_preview()
        self._update_status("line start")

    def _line_update(self, event):
        if self._line_start is None:
            return
        pos = self._event_to_cell(event, require_drawable=False)
        if pos is None:
            return
        self._line_end = pos
        self._draw_line_preview()

    def _bresenham_cells(self, x0: int, y0: int, x1: int, y1: int):
        dx = abs(x1 - x0)
        dy = abs(y1 - y0)
        sx = 1 if x0 < x1 else -1
        sy = 1 if y0 < y1 else -1
        err = dx - dy
        x, y = x0, y0
        while True:
            yield x, y
            if x == x1 and y == y1:
                break
            e2 = 2 * err
            if e2 > -dy:
                err -= dy
                x += sx
            if e2 < dx:
                err += dx
                y += sy

    def _line_commit(self, event):
        if self._line_start is None or self._line_end is None:
            return
        if self.selected_tile is None or not (0 <= self.selected_tile < len(self.tile_images)):
            self._clear_line_preview()
            self._line_start = None
            self._line_end = None
            return

        x0, y0 = self._line_start
        x1, y1 = self._line_end
        val = self.selected_tile

        dx0, dy0, dx1, dy1 = self._drawable_bounds()

        changed = 0
        for cx, cy in self._bresenham_cells(x0, y0, x1, y1):
            if dx0 <= cx <= dx1 and dy0 <= cy <= dy1:
                if self._set_cell(self.current_layer, cx, cy, val, update_status=False):
                    changed += 1

        self._clear_line_preview()
        self._line_start = None
        self._line_end = None
        self._update_status(f"line applied ({changed} cells)")

    # ---------------- cell set / render ----------------

    def _canvas_image_for_tile(self, tile_i: int) -> tk.PhotoImage | None:
        if tile_i == EMPTY_TILE:
            return None
        if tile_i in self.animations:
            img = self._get_scaled_anim_frame(tile_i, 0)
            if img is not None:
                return img
        return self._get_scaled_static_tile(tile_i)

    def _unregister_item_from_anim(self, tile_i: int, item_id: int):
        if tile_i in self.animated_items:
            self.animated_items[tile_i].discard(item_id)

    def _register_item_to_anim(self, tile_i: int, item_id: int):
        if not self.anim_enabled.get():
            return
        if tile_i not in self.animations:
            return
        s = self.animated_items.setdefault(tile_i, set())
        s.add(item_id)
        self._ensure_anim_loop()

    def _set_cell(self, layer_i: int, cx: int, cy: int, tile_i: int, update_status: bool = True, allow_outside: bool = False) -> bool:
        """
        returns True if the cell actually changed.
        """
        if not allow_outside:
            if not self._in_drawable(cx, cy):
                return False

        idx = cy * self.map_w + cx
        old = self.layers[layer_i][idx]
        if old == tile_i:
            return False

        # if painting a real tile, ensure it has an image
        if tile_i != EMPTY_TILE:
            if not (0 <= tile_i < len(self.tile_images)):
                return False
            if self._canvas_image_for_tile(tile_i) is None:
                return False

        # record undo (merge by cell)
        if (not self._undo_in_progress) and (self._undo_current is not None):
            key = (layer_i, idx)
            if key in self._undo_current:
                old0, _new0 = self._undo_current[key]
                self._undo_current[key] = (old0, tile_i)
            else:
                self._undo_current[key] = (old, tile_i)

        self.layers[layer_i][idx] = tile_i

        item = self.cell_items[layer_i][idx]
        if item is not None:
            if old != EMPTY_TILE:
                self._unregister_item_from_anim(old, item)
            self.canvas.delete(item)
            self.cell_items[layer_i][idx] = None

        if tile_i != EMPTY_TILE:
            img = self._canvas_image_for_tile(tile_i)
            if img is None:
                return False
            px = cx * self.view_tile_w
            py = cy * self.view_tile_h
            tag = f"layer{layer_i}"
            item_id = self.canvas.create_image(px, py, anchor="nw", image=img, tags=(tag, "tile"))
            if not self.layer_visible[layer_i]:
                self.canvas.itemconfigure(item_id, state="hidden")
            self.cell_items[layer_i][idx] = item_id
            self._register_item_to_anim(tile_i, item_id)

        if update_status:
            self._update_status(f"paint {tile_i if tile_i != EMPTY_TILE else 'empty'} at {cx},{cy}")
        return True

    def _rebuild_all_tile_items(self):
        self.canvas.delete("tile")
        cell_count = self.map_w * self.map_h
        self.cell_items = [[None] * cell_count for _ in range(self.num_layers)]
        self.animated_items = {}

        for li in range(self.num_layers):
            for idx, v in enumerate(self.layers[li]):
                if v == EMPTY_TILE:
                    continue

                img = self._canvas_image_for_tile(v)
                if img is None:
                    continue

                cx = idx % self.map_w
                cy = idx // self.map_w
                px = cx * self.view_tile_w
                py = cy * self.view_tile_h
                tag = f"layer{li}"
                item_id = self.canvas.create_image(px, py, anchor="nw", image=img, tags=(tag, "tile"))
                if not self.layer_visible[li]:
                    self.canvas.itemconfigure(item_id, state="hidden")
                self.cell_items[li][idx] = item_id
                self._register_item_to_anim(v, item_id)

        self._ensure_anim_loop()
        self.canvas.tag_raise("drawable_border")

    # ---------------- file i/o ----------------

    def _open_map(self):
        path = filedialog.askopenfilename(
            title="open map",
            filetypes=[("tile map", "*.tmap *.bin *.map *.*"), ("all files", "*.*")]
        )
        if not path:
            return
        try:
            self._load_map_file(path)
        except Exception as e:
            messagebox.showerror("open failed", str(e))
            return
        self._update_status("map loaded")

    def _save_map(self):
        if self.map_path is None:
            self._save_as_map()
            return
        try:
            self._write_map_file(self.map_path)
        except Exception as e:
            messagebox.showerror("save failed", str(e))
            return
        self._update_status("saved")

    def _save_as_map(self):
        path = filedialog.asksaveasfilename(
            title="save map as",
            defaultextension=".tmap",
            filetypes=[("tile map", "*.tmap"), ("all files", "*.*")]
        )
        if not path:
            return
        try:
            self._write_map_file(path)
        except Exception as e:
            messagebox.showerror("save failed", str(e))
            return
        self.map_path = path
        self._update_status("saved")

    def _save_padded_as(self):
        if self.map_w <= 0 or self.map_h <= 0:
            return
        path = filedialog.asksaveasfilename(
            title="save padded as",
            defaultextension=".tmap",
            filetypes=[("tile map", "*.tmap"), ("all files", "*.*")]
        )
        if not path:
            return

        tw = simpledialog.askinteger("padded save", "target width in tiles (1-255)", minvalue=1, maxvalue=255, initialvalue=self.map_w)
        if tw is None:
            return
        th = simpledialog.askinteger("padded save", "target height in tiles (1-255)", minvalue=1, maxvalue=255, initialvalue=self.map_h)
        if th is None:
            return

        try:
            padded_layers = self._make_padded_layers(tw, th, center=True)
            self._write_map_file(path, override_map_w=tw, override_map_h=th, override_layers_data=padded_layers)
        except Exception as e:
            messagebox.showerror("save failed", str(e))
            return

        self._update_status(f"saved padded ({tw}x{th})")

    def _write_map_file(
            self,
            path: str,
            override_paths: list[str] | None = None,
            override_anims: dict[int, tuple[str, int, int]] | None = None,
            override_map_w: int | None = None,
            override_map_h: int | None = None,
            override_layers_data: list[list[int]] | None = None
    ):
        tile_paths = override_paths if override_paths is not None else self.tile_paths
        anims = override_anims if override_anims is not None else self.animations

        map_w = int(override_map_w) if override_map_w is not None else int(self.map_w)
        map_h = int(override_map_h) if override_map_h is not None else int(self.map_h)

        if map_w < 1 or map_w > 255 or map_h < 1 or map_h > 255:
            raise ValueError("map size must be 1..255")

        layers_data = override_layers_data if override_layers_data is not None else self.layers

        num_tiles = len(tile_paths)
        num_layers = len(layers_data)
        if num_layers <= 0:
            raise ValueError("no layers")

        prop = self.prop_layer
        if prop != EMPTY_TILE and (prop < 0 or prop >= num_layers):
            prop = EMPTY_TILE

        prop_u32 = (prop if prop != EMPTY_TILE else 0xFFFFFFFF) & 0xFFFFFFFF

        # attributes lines (text)
        attr_lines: list[bytes] = []
        for idx in sorted(self.attributes.keys()):
            attrs = self.attributes.get(idx, [])
            if not attrs:
                continue
            clean: list[str] = []
            for a in attrs:
                a2 = _safe_attr(a)
                if a2:
                    clean.append(a2)
            if not clean:
                continue
            line = (str(idx).encode("utf-8") + b"," + ",".join(clean).encode("utf-8") + b"\n")
            attr_lines.append(line)

        # animation lines (text)
        anim_lines: list[bytes] = []
        for idx in sorted(anims.keys()):
            folder_rel, ms, cnt = anims[idx]
            if ms < 1 or ms > 255 or cnt < 1 or cnt > 255:
                continue
            folder_rel = _to_forward_slashes(folder_rel.strip())
            if not folder_rel:
                continue
            line = (
                    str(idx).encode("utf-8") + b"," +
                    folder_rel.encode("utf-8") + b"," +
                    str(ms).encode("utf-8") + b"," +
                    str(cnt).encode("utf-8") + b"\n"
            )
            anim_lines.append(line)

        flip_y = bool(self.flip_y_in_file.get())

        # --- v3 layout: prop layer is in the header with the other ints ---
        with open(path, "wb") as f:
            f.write(TMAP3_MAGIC)
            f.write(struct.pack(">7I", num_tiles, num_layers, self.tile_w, self.tile_h, map_w, map_h, prop_u32))
            f.write(b"\n")

            for p in tile_paths:
                p2 = _to_forward_slashes(p)
                f.write(p2.encode("utf-8") + b"\n")

            # sections
            if attr_lines:
                f.write(MARKER_A + b"\n")
                for line in attr_lines:
                    f.write(line)
                if anim_lines:
                    f.write(MARKER_ANIM + b"\n")
                    for line in anim_lines:
                        f.write(line)
                f.write(MARKER_B + b"\n")
            else:
                if anim_lines:
                    f.write(MARKER_ANIM + b"\n")
                    for line in anim_lines:
                        f.write(line)
                f.write(MARKER_B + b"\n")

            # layers: uint32 be tile indices
            cell_count = map_w * map_h
            for li in range(num_layers):
                layer = layers_data[li]
                if len(layer) != cell_count:
                    raise ValueError(f"layer size mismatch (expected {cell_count}, got {len(layer)})")

                buf = bytearray(cell_count * 4)

                # write in file order; optionally flip Y (row order)
                for y_file in range(map_h):
                    y_int = (map_h - 1 - y_file) if flip_y else y_file
                    for x in range(map_w):
                        i_file = y_file * map_w + x
                        i_int = y_int * map_w + x
                        v = layer[i_int]
                        vv = 0xFFFFFFFF if v == EMPTY_TILE else int(v) & 0xFFFFFFFF
                        struct.pack_into(">I", buf, i_file * 4, vv)

                f.write(buf)
                f.write(b"\n")

            f.write(END_MARKER + b"\n")

    def _load_map_file(self, path: str):
        data = open(path, "rb").read()
        if len(data) < 6:
            raise ValueError("file too short")

        if data.startswith(TMAP3_MAGIC):
            parsed = self._parse_map_v3(data)
            self._apply_loaded_map(path, *parsed)
            return

        raise ValueError("unsupported map format (expected tmap3)")

    def _parse_map_v3(self, data: bytes):
        pos = len(TMAP3_MAGIC)
        if pos + 28 > len(data):
            raise ValueError("truncated v3 header")
        num_tiles, num_layers, tile_w, tile_h, map_w, map_h, prop_u32 = struct.unpack(">7I", data[pos:pos + 28])
        pos += 28
        if pos < len(data) and data[pos:pos + 1] == b"\n":
            pos += 1

        if num_tiles == 0 or num_layers == 0 or tile_w == 0 or tile_h == 0 or map_w == 0 or map_h == 0:
            raise ValueError("invalid v3 header values")

        prop_layer = EMPTY_TILE if prop_u32 == 0xFFFFFFFF else int(prop_u32)

        tile_paths: list[str] = []
        for _ in range(num_tiles):
            line, pos = _read_line(data, pos)
            tile_paths.append(line.decode("utf-8"))

        def read_next_nonempty(p: int) -> tuple[bytes, int]:
            while True:
                line, p2 = _read_line(data, p)
                p = p2
                if line == b"":
                    continue
                return line, p

        marker, pos = read_next_nonempty(pos)

        attrs: dict[int, list[str]] = {}
        anims: dict[int, tuple[str, int, int]] = {}

        if marker in (MARKER_A, MARKER_A_ALT):
            while True:
                line, pos = _read_line(data, pos)
                if line == b"":
                    continue
                if line == MARKER_B:
                    break
                if line == MARKER_ANIM:
                    while True:
                        line2, pos = _read_line(data, pos)
                        if line2 == b"":
                            continue
                        if line2 == MARKER_B:
                            break
                        idx, parts = _parse_int_prefix(line2)
                        if idx is None or len(parts) < 3:
                            continue
                        folder = parts[0]
                        try:
                            ms = int(parts[1])
                            cnt = int(parts[2])
                        except Exception:
                            continue
                        if 1 <= ms <= 255 and 1 <= cnt <= 255:
                            anims[idx] = (folder, ms, cnt)
                    break

                idx, parts = _parse_int_prefix(line)
                if idx is None or not parts:
                    continue
                attrs[idx] = parts

        elif marker == MARKER_ANIM:
            while True:
                line2, pos = _read_line(data, pos)
                if line2 == b"":
                    continue
                if line2 == MARKER_B:
                    break
                idx, parts = _parse_int_prefix(line2)
                if idx is None or len(parts) < 3:
                    continue
                folder = parts[0]
                try:
                    ms = int(parts[1])
                    cnt = int(parts[2])
                except Exception:
                    continue
                if 1 <= ms <= 255 and 1 <= cnt <= 255:
                    anims[idx] = (folder, ms, cnt)

        elif marker == MARKER_B:
            pass
        else:
            raise ValueError("missing expected section marker (markers/animated/tuvalutorture)")

        cell_count = int(map_w * map_h)
        layers: list[list[int]] = []
        need = cell_count * 4

        for _ in range(int(num_layers)):
            if pos + need > len(data):
                raise ValueError("truncated layer data (v3)")
            layer_bytes = data[pos:pos + need]
            pos += need
            if pos < len(data) and data[pos:pos + 1] == b"\n":
                pos += 1

            out: list[int] = []
            for (v,) in struct.iter_unpack(">I", layer_bytes):
                out.append(EMPTY_TILE if v == 0xFFFFFFFF else v)
            layers.append(out)

        return int(num_tiles), int(num_layers), int(tile_w), int(tile_h), int(map_w), int(map_h), tile_paths, prop_layer, attrs, anims, layers

    def _apply_loaded_map(self, path: str,
                          num_tiles, num_layers, tile_w, tile_h, map_w, map_h,
                          tile_paths, prop_layer, attrs, anims, layers):
        loaded_w = int(map_w)
        loaded_h = int(map_h)

        self.tile_w = int(tile_w)
        self.tile_h = int(tile_h)
        self.num_layers = int(num_layers)

        # apply flip-y on load if enabled (keeps editor view consistent with your engine expectation)
        if self.flip_y_in_file.get():
            layers = [_flip_layer_y(layer, loaded_w, loaded_h) for layer in layers]

        # decide max canvas size (save size): use startup max, but never smaller than loaded
        self.map_w = max(int(self.max_map_w), loaded_w)
        self.map_h = max(int(self.max_map_h), loaded_h)

        # keep these in sync for later new maps
        self.max_map_w = int(self.map_w)
        self.max_map_h = int(self.map_h)

        # drawable size defaults to the loaded map size, centered in max
        self.draw_w = loaded_w
        self.draw_h = loaded_h
        self._recompute_draw_origin_centered()

        self.tile_paths = tile_paths
        self.attributes = attrs

        # pad layers into max canvas (dead space = EMPTY_TILE)
        cell_count = self.map_w * self.map_h
        padded_layers: list[list[int]] = []
        for li in range(self.num_layers):
            buf = [EMPTY_TILE] * cell_count
            src = layers[li]
            for y in range(loaded_h):
                for x in range(loaded_w):
                    sidx = y * loaded_w + x
                    dx = self.draw_x0 + x
                    dy = self.draw_y0 + y
                    didx = dy * self.map_w + dx
                    buf[didx] = src[sidx]
            padded_layers.append(buf)
        self.layers = padded_layers

        self.layer_visible = [True] * self.num_layers
        self.current_layer = 0
        self.prop_layer = prop_layer if (prop_layer == EMPTY_TILE or prop_layer < self.num_layers) else EMPTY_TILE

        self.animations = anims
        self.animation_frames = {}
        self.animated_items = {}
        self.map_path = path

        self.undo_stack = []
        self._undo_current = None
        self._undo_in_progress = False

        # reset zoom
        self._apply_zoom(3, keep_view=False)

        # load tile images
        self.tile_images = []
        for p in self.tile_paths:
            real = _resolve_path(p, path, self.root_dir)
            try:
                img = tk.PhotoImage(file=real)
            except Exception as e:
                raise ValueError(
                    f"failed to load tile image:\n{p}\nresolved to:\n{real}\n\n"
                    f"current root folder:\n{self.root_dir}\n\n{e}"
                )
            if img.width() != self.tile_w or img.height() != self.tile_h:
                raise ValueError(
                    f"tile size mismatch for:\n{p}\nexpected {self.tile_w}x{self.tile_h}, got {img.width()}x{img.height()}"
                )
            self.tile_images.append(img)

        # load animations
        for idx, (folder_rel, ms, cnt) in list(self.animations.items()):
            if not (0 <= idx < len(self.tile_images)):
                continue
            try:
                frames = self._load_animation_frames(folder_rel, cnt, path)
            except Exception as e:
                raise ValueError(f"failed to load animation for tile {idx}:\n{folder_rel}\n\n{e}")
            self.animation_frames[idx] = frames

        self._clear_scaled_caches()
        self._pal_thumb_cache.clear()
        self._pal_anim_thumb_cache.clear()

        self.selected_tile = 0 if self.tile_paths else None
        self.selected_tiles = {0} if self.tile_paths else set()

        self._refresh_tile_list()
        self._refresh_layer_list()
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        self._sync_preview_and_attrs()
        self._ensure_anim_loop()
        self._update_status("loaded (centered + padded in editor)")

    # ---------------- max/drawable resize actions ----------------

    def _set_drawable_area_dialog(self):
        if self.map_w <= 0 or self.map_h <= 0:
            return
        dw = simpledialog.askinteger("drawable area", f"drawable width (1..{self.map_w})", minvalue=1, maxvalue=self.map_w, initialvalue=self.draw_w)
        if dw is None:
            return
        dh = simpledialog.askinteger("drawable area", f"drawable height (1..{self.map_h})", minvalue=1, maxvalue=self.map_h, initialvalue=self.draw_h)
        if dh is None:
            return

        # update + clear dead space
        self.draw_w = int(dw)
        self.draw_h = int(dh)
        self._recompute_draw_origin_centered()
        self._clear_outside_drawable()

        # rebuild
        x_frac, y_frac = self.canvas.xview()[0], self.canvas.yview()[0]
        self.undo_stack = []
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        try:
            self.canvas.xview_moveto(x_frac)
            self.canvas.yview_moveto(y_frac)
        except Exception:
            pass

        self._update_status("drawable area set (dead space cleared)")

    def _set_max_size_dialog(self):
        if self.map_w <= 0 or self.map_h <= 0:
            return
        mw = simpledialog.askinteger("max canvas", "max width in tiles (1-255)", minvalue=1, maxvalue=255, initialvalue=self.map_w)
        if mw is None:
            return
        mh = simpledialog.askinteger("max canvas", "max height in tiles (1-255)", minvalue=1, maxvalue=255, initialvalue=self.map_h)
        if mh is None:
            return

        mw = int(mw)
        mh = int(mh)

        if mw == self.map_w and mh == self.map_h:
            return

        # if cropping could drop content, confirm
        if mw < self.draw_w or mh < self.draw_h:
            ok = messagebox.askyesno(
                "crop warning",
                "new max canvas is smaller than current drawable area.\n"
                "drawable will be clamped and content may be cropped.\n\ncontinue?"
            )
            if not ok:
                return

        # build new layers by copying current drawable into new max (centered)
        old_draw_w = int(self.draw_w)
        old_draw_h = int(self.draw_h)

        # clamp drawable to new max
        self.map_w = mw
        self.map_h = mh
        self.max_map_w = mw
        self.max_map_h = mh
        self.draw_w = min(old_draw_w, mw)
        self.draw_h = min(old_draw_h, mh)
        self._recompute_draw_origin_centered()

        new_layers = self._make_padded_layers(mw, mh, center=True)
        self.layers = new_layers

        # dead space must be clear
        self._clear_outside_drawable()

        # rebuild
        x_frac, y_frac = self.canvas.xview()[0], self.canvas.yview()[0]
        self.undo_stack = []
        self._rebuild_canvas()
        self._rebuild_all_tile_items()
        try:
            self.canvas.xview_moveto(x_frac)
            self.canvas.yview_moveto(y_frac)
        except Exception:
            pass

        self._update_status("max canvas resized")

    # ---------------- export ----------------

    def _export_folder(self):
        if self.map_w <= 0:
            return
        if not self.tile_paths:
            messagebox.showerror("no tiles", "load at least one tile first")
            return

        folder = filedialog.askdirectory(title="export to folder")
        if not folder:
            return

        # store exported paths relative to export folder so the export is portable
        export_paths: list[str] = []
        for i, src_rel in enumerate(self.tile_paths):
            src_abs = _resolve_path(src_rel, self.map_path, self.root_dir)
            dst_name = f"tile_{i}.png"
            dst_path = os.path.join(folder, dst_name)
            try:
                shutil.copyfile(src_abs, dst_path)
            except Exception as e:
                messagebox.showerror("export failed", f"could not copy {src_abs} -> {dst_path}\n{e}")
                return
            export_paths.append(_abs_to_rel_base(dst_path, folder))

        export_anims: dict[int, tuple[str, int, int]] = {}
        for idx, (folder_rel, ms, cnt) in self.animations.items():
            src_folder_abs = _resolve_path(folder_rel, self.map_path, self.root_dir)
            dst_folder_abs = os.path.join(folder, f"anim_{idx}")
            os.makedirs(dst_folder_abs, exist_ok=True)

            try:
                for k in range(1, cnt + 1):
                    src_fp = os.path.join(src_folder_abs, f"{k}.png")
                    dst_fp = os.path.join(dst_folder_abs, f"{k}.png")
                    shutil.copyfile(src_fp, dst_fp)
            except Exception as e:
                messagebox.showerror("export failed", f"could not copy animation {idx} frames\n{e}")
                return

            export_anims[idx] = (_abs_to_rel_base(dst_folder_abs, folder), ms, cnt)

        map_out = os.path.join(folder, "map.tmap")
        try:
            self._write_map_file(map_out, override_paths=export_paths, override_anims=export_anims)
        except Exception as e:
            messagebox.showerror("export failed", str(e))
            return

        messagebox.showinfo("exported", f"exported to:\n{folder}\n\nmap file:\n{map_out}")

    # ---------------- status ----------------

    def _update_status(self, extra: str = ""):
        tile = "none" if self.selected_tile is None else str(self.selected_tile)
        mp = self.map_path if self.map_path else "(unsaved)"
        prop = "none" if self.prop_layer == EMPTY_TILE else str(self.prop_layer)
        zoom = f"{self.scale_n}/{self.scale_d}x" if self.scale_d != 1 else f"{self.scale_n}x"
        tool = self.tool_mode.get()
        seln = len(self.selected_tiles) if self.selected_tiles else 0
        root = self.root_dir
        anim = "on" if self.anim_enabled.get() else "off"
        flipy = "on" if self.flip_y_in_file.get() else "off"

        msg = (
            f"map: {mp} | tool: {tool} | tile: {tile} | selected tiles: {seln} | "
            f"layer: {self.current_layer} | prop layer: {prop} | zoom: {zoom} | "
            f"anim: {anim} | flipy: {flipy} | root: {root} | "
            f"max: {self.map_w}x{self.map_h} | draw: {self.draw_w}x{self.draw_h} @ {self.draw_x0},{self.draw_y0}"
        )
        if extra:
            msg += f" | {extra}"
        self.status.configure(text=msg)


if __name__ == "__main__":
    app = TileMapEditor()
    app.mainloop()
