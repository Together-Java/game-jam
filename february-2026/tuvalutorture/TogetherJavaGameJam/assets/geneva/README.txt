ascii glyph tiles

- font: DejaVuSansMono.ttf (rendered at 12px ~= 9pt @ 96 dpi)
- tile: 16x16px (12pt @ 96 dpi), transparent background, white glyphs
- baseline-aligned using max above/below across the full printable ascii set
- exported: 95 png files

naming matches your loader:
- lower_<a-z>.png
- upper_<A-Z>.png
- digit_<0-9>.png
- symbol_<name>.png  (names from your SymbolNames enum)

note: only symbols present in your SymbolNames enum are included (plus letters/digits).
