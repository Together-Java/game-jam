package com.tuvalutorture.gamejam;

import java.util.HashMap;
import java.util.Map;

public enum SymbolNames {
    SPACE("space", ' '),
    EXCLAMATION("exclamation", '!'),
    DOUBLE_QUOTE("double_quote", '"'),
    HASH("hash", '#'),
    DOLLAR("dollar", '$'),
    PERCENT("percent", '%'),
    AMPERSAND("ampersand", '&'),
    APOSTROPHE("apostrophe", '\''),
    LEFT_PARENTHESIS("left_paren", '('),
    RIGHT_PARENTHESIS("right_paren", ')'),
    ASTERISK("asterisk", '*'),
    PLUS("plus", '+'),
    COMMA("comma", ','),
    HYPHEN("hyphen", '-'),
    PERIOD("period", '.'),
    SLASH("slash", '/'),
    COLON("colon", ':'),
    SEMICOLON("semicolon", ';'),
    LESS_THAN("less_than", '<'),
    EQUALS("equals", '='),
    GREATER_THAN("greater_than", '>'),
    QUESTION("question", '?'),
    AT("at", '@'),
    LEFT_BRACKET("left_bracket", '['),
    BACKSLASH("backslash", '\\'),
    RIGHT_BRACKET("right_bracket", ']'),
    CARET("caret", '^'),
    UNDERSCORE("underscore", '_'),
    BACKTICK("backtick", '`'),
    LEFT_BRACE("left_brace", '{'),
    PIPE("pipe", '|'),
    RIGHT_BRACE("right_brace", '}'),
    TILDE("tilde", '~');

    private final String name;
    private final char map;

    private static final Map<Character, SymbolNames> CHARMAP = new HashMap<>();

    static {
        for (SymbolNames symbols : values()) {
            CHARMAP.put(symbols.map, symbols);
        }
    }

    SymbolNames(String name, char map) {
        this.name = name;
        this.map = map;
    }

    private String getName() {
        return name;
    }

    public static String getSymbolName(char symbol) {
        SymbolNames s = CHARMAP.get(symbol);
        return s != null ? s.getName() : null;
    }
}
