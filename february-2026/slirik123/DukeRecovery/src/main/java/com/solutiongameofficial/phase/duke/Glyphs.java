package com.solutiongameofficial.phase.duke;

import java.util.HashMap;
import java.util.Map;

public final class Glyphs {

    private static final boolean[][] BOX = new boolean[][] {
            row("#####"),
            row("#...#"),
            row("#...#"),
            row("#...#"),
            row("#...#"),
            row("#...#"),
            row("#####")
    };

    private static final Map<Character, boolean[][]> GLYPHS = new HashMap<>();

    static {
        GLYPHS.put('#', boxWithCenter());
        GLYPHS.put('@', ring());
        GLYPHS.put('%', percent());
        GLYPHS.put('&', amp());
        GLYPHS.put('*', star());
        GLYPHS.put('+', plus());
        GLYPHS.put('=', equals());
        GLYPHS.put('~', wave());
        GLYPHS.put('!', bang());
        GLYPHS.put('?', question());
        GLYPHS.put('/', slash());
        GLYPHS.put('\\', backslash());
        GLYPHS.put('^', caret());
        GLYPHS.put(':', colon());
        GLYPHS.put(';', semicolon());
        GLYPHS.put('░', dotNoise());
        GLYPHS.put('▒', midNoise());
        GLYPHS.put('▓', heavyNoise());
    }

    public static boolean[][] get(char c) {
        boolean[][] g = GLYPHS.get(c);
        return g != null ? g : BOX;
    }

    private static boolean[] row(String s) {
        boolean[] r = new boolean[s.length()];
        for (int i = 0; i < s.length(); i++) {
            r[i] = s.charAt(i) == '#';
        }
        return r;
    }

    private static boolean[][] boxWithCenter() {
        return new boolean[][] {
                row("#####"),
                row("##..#"),
                row("#.#.#"),
                row("#..##"),
                row("#.#.#"),
                row("#..##"),
                row("#####")
        };
    }

    private static boolean[][] ring() {
        return new boolean[][] {
                row(".###."),
                row("##.##"),
                row("#...#"),
                row("#.#.#"),
                row("#...#"),
                row("##.##"),
                row(".###.")
        };
    }

    private static boolean[][] percent() {
        return new boolean[][] {
                row("##..#"),
                row("##..#"),
                row("...#."),
                row("..#.."),
                row(".#..."),
                row("#..##"),
                row("#..##")
        };
    }

    private static boolean[][] amp() {
        return new boolean[][] {
                row(".###."),
                row("#...#"),
                row(".###."),
                row("#.#.."),
                row("#..#."),
                row("#...#"),
                row(".###.")
        };
    }

    private static boolean[][] star() {
        return new boolean[][] {
                row("..#.."),
                row("#.#.#"),
                row(".###."),
                row("#####"),
                row(".###."),
                row("#.#.#"),
                row("..#..")
        };
    }

    private static boolean[][] plus() {
        return new boolean[][] {
                row("..#.."),
                row("..#.."),
                row("#####"),
                row("..#.."),
                row("..#.."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] equals() {
        return new boolean[][] {
                row("....."),
                row("#####"),
                row("....."),
                row("#####"),
                row("....."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] wave() {
        return new boolean[][] {
                row("....."),
                row("....."),
                row(".#.#."),
                row("#.#.#"),
                row(".#.#."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] bang() {
        return new boolean[][] {
                row("..#.."),
                row("..#.."),
                row("..#.."),
                row("..#.."),
                row("..#.."),
                row("....."),
                row("..#..")
        };
    }

    private static boolean[][] question() {
        return new boolean[][] {
                row(".###."),
                row("#...#"),
                row("...#."),
                row("..#.."),
                row("..#.."),
                row("....."),
                row("..#..")
        };
    }

    private static boolean[][] slash() {
        return new boolean[][] {
                row("....#"),
                row("...#."),
                row("..#.."),
                row(".#..."),
                row("#...."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] backslash() {
        return new boolean[][] {
                row("#...."),
                row(".#..."),
                row("..#.."),
                row("...#."),
                row("....#"),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] caret() {
        return new boolean[][] {
                row("..#.."),
                row(".#.#."),
                row("#...#"),
                row("....."),
                row("....."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] colon() {
        return new boolean[][] {
                row("....."),
                row("..#.."),
                row("....."),
                row("....."),
                row("..#.."),
                row("....."),
                row(".....")
        };
    }

    private static boolean[][] semicolon() {
        return new boolean[][] {
                row("....."),
                row("..#.."),
                row("....."),
                row("....."),
                row("..#.."),
                row(".#..."),
                row(".....")
        };
    }

    private static boolean[][] dotNoise() {
        return new boolean[][] {
                row("..#.."),
                row("....."),
                row("#...#"),
                row("....."),
                row("..#.."),
                row("....."),
                row("#....")
        };
    }

    private static boolean[][] midNoise() {
        return new boolean[][] {
                row("#.#.#"),
                row("..#.."),
                row("#...#"),
                row(".#.#."),
                row("#...#"),
                row("..#.."),
                row("#.#.#")
        };
    }

    private static boolean[][] heavyNoise() {
        return new boolean[][] {
                row("#####"),
                row("##.##"),
                row("#####"),
                row("#.###"),
                row("#####"),
                row("##.##"),
                row("#####")
        };
    }
}