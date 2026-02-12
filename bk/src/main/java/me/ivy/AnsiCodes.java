package me.ivy;

/**
 * Набор ANSI-последовательностей для управления курсором и очистки строки в терминале.
 */
public class AnsiCodes {
    public static final String ANSI_SAVE_CURSOR = "\u001B[s";
    public static final String ANSI_RESTORE_CURSOR = "\u001B[u";
    public static final String ANSI_CLEAR_LINE = "\u001B[2K";
    public static final String ANSI_CURSOR_UP_1 = "\u001B[1A";
    public static final String ANSI_CURSOR_DOWN_1 = "\u001B[1B";
}
