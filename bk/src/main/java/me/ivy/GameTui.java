package me.ivy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Текстовый интерфейс игры в терминале.
 */
public class GameTui {
    private static final Logger logger = LogManager.getLogger(GameTui.class);

    private final PrintStream out;
    private final BufferedReader reader;

    /**
     * Создает текстовый интерфейс игры.
     *
     * @param in поток пользовательского ввода
     * @param out поток вывода в терминал
     */
    public GameTui(InputStream in, PrintStream out) {
        this.out = out;
        this.reader = new BufferedReader(new InputStreamReader(in));
        logger.debug("GameTui initialized");
    }

    /**
     * Печатает строку с переводом строки.
     *
     * @param text выводимый текст
     */
    public void println(String text) {
        out.println(text);
    }

    /**
     * Печатает горизонтальный разделитель.
     */
    public void printSeparator() {
        out.println("-----------------");
    }

    /**
     * Запрашивает целое число с проверкой диапазона и поддержкой значения по умолчанию.
     *
     * @param prompt текст приглашения
     * @param defaultValue значение по умолчанию
     * @param minInclusive минимально допустимое значение
     * @param maxInclusive максимально допустимое значение
     * @return введенное или значение по умолчанию
     */
    public int askInt(String prompt, int defaultValue, int minInclusive, int maxInclusive) {
        while (true) {
            out.print(String.format("%s [%d]: ", prompt, defaultValue));
            out.flush();

            String line = readLineUnbounded();
            if (line == null) {
                logger.info("askInt: prompt='{}' -> EOF/null, using default {}", prompt, defaultValue);
                return defaultValue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                logger.info("askInt: prompt='{}' -> empty input, using default {}", prompt, defaultValue);
                return defaultValue;
            }

            int value;
            try {
                value = Integer.parseInt(trimmed);
            } catch (NumberFormatException e) {
                logger.info("askInt: prompt='{}' -> invalid integer input '{}'", prompt, trimmed);
                out.println("Нужно целое число.");
                continue;
            }

            if (value < minInclusive || value > maxInclusive) {
                logger.info("askInt: prompt='{}' -> out of range {} (allowed {}..{})", prompt, value, minInclusive, maxInclusive);
                out.println(String.format("Допустимый диапазон: %d..%d", minInclusive, maxInclusive));
                continue;
            }

            logger.info("askInt: prompt='{}' -> {}", prompt, value);
            return value;
        }
    }

    /**
     * Предлагает пользователю изменить параметры игры.
     *
     * @param defaults параметры по умолчанию
     * @return выбранные параметры
     */
    public GameParameters chooseGameParameters(GameParameters defaults) {
        logger.info("Choose game parameters: defaults={}", defaults);
        out.println("Настройки игры:");
        out.println(String.format("- Длина числа: %d", defaults.secretLength));
        out.println(String.format("- Попыток: %d", defaults.maxAttempts));
        out.println(String.format("- Время на попытку: %dс", defaults.timeToGuess));

        if (!askYesNo("Изменить настройки? (y/N): ", false)) {
            logger.info("Choose game parameters: keep defaults");
            return defaults;
        }

        GameParameters params = new GameParameters();
        params.secretLength = askInt("Длина числа", defaults.secretLength, 1, 10);
        params.maxAttempts = askInt("Максимум попыток", defaults.maxAttempts, 1, 999);
        params.timeToGuess = askInt("Время на попытку (сек)", defaults.timeToGuess, 1, 3600);
        logger.info("Choose game parameters: selected={}", params);
        return params;
    }

    /**
     * Задает вопрос с ответом «да/нет» и возвращает логический результат.
     *
     * @param prompt текст приглашения
     * @param defaultYes значение по умолчанию при пустом вводе или EOF
     * @return {@code true}, если ответ положительный; иначе {@code false}
     */
    public boolean askYesNo(String prompt, boolean defaultYes) {
        while (true) {
            out.print(prompt);
            out.flush();
            String line = readLineUnbounded();
            if (line == null) {
                logger.info("askYesNo: prompt='{}' -> EOF/null, using default {}", prompt, defaultYes);
                return defaultYes;
            }
            String trimmed = line.trim().toLowerCase();
            if (trimmed.isEmpty()) {
                logger.info("askYesNo: prompt='{}' -> empty input, using default {}", prompt, defaultYes);
                return defaultYes;
            }
            if (trimmed.equals("y") || trimmed.equals("yes") || trimmed.equals("д") || trimmed.equals("да")) {
                logger.info("askYesNo: prompt='{}' -> YES (input='{}')", prompt, trimmed);
                return true;
            }
            if (trimmed.equals("n") || trimmed.equals("no") || trimmed.equals("н") || trimmed.equals("нет")) {
                logger.info("askYesNo: prompt='{}' -> NO (input='{}')", prompt, trimmed);
                return false;
            }
            logger.info("askYesNo: prompt='{}' -> invalid input '{}'", prompt, trimmed);
            out.println("Ответь y/n");
        }
    }

    /**
     * Подтверждает намерение пользователя завершить игру.
     *
     * @return {@code true}, если пользователь подтвердил выход
     */
    public boolean confirmExit() {
        logger.info("confirmExit requested");
        return askYesNo("Ты хочешь уйти с позором? (y/N): ", false);
    }
    
    /**
     * Считывает строку с обратным отсчетом в секундах.
     *
     * @param prompt текст приглашения
     * @param statusPrefix префикс статусной строки
     * @param timeoutSeconds время ожидания в секундах
     * @return введенная строка или {@code null} при истечении времени
     */
    public String readLineWithCountdown(String prompt, String statusPrefix, int timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            out.print(prompt);
            out.flush();
            logger.debug("readLineWithCountdown: no timeout, prompt='{}'", prompt);
            return readLineUnbounded();
        }

        long deadlineMs = System.currentTimeMillis() + timeoutSeconds * 1000L;
        logger.debug("readLineWithCountdown: timeoutSeconds={}, prompt='{}'", timeoutSeconds, prompt);
        return readLineWithDeadline(prompt, statusPrefix, deadlineMs);
    }

    /**
     * Считывает строку до указанного абсолютного дедлайна.
     *
     * @param prompt текст приглашения
     * @param statusPrefix префикс статусной строки
     * @param deadlineMs абсолютное время дедлайна в миллисекундах
     * @return введенная строка или {@code null} при тайм-ауте/прерывании
     */
    public String readLineWithDeadline(String prompt, String statusPrefix, long deadlineMs) {
        out.println();
        out.println(statusPrefix);
        out.print(prompt);
        out.flush();

        logger.debug("readLineWithDeadline: prompt='{}', deadlineMs={}", prompt, deadlineMs);

        long nextTickMs = 0;

        while (true) {
            long nowMs = System.currentTimeMillis();
            long remainingMs = deadlineMs - nowMs;
            int remainingSeconds = (int) Math.max(0L, (remainingMs + 999L) / 1000L);

            if (remainingMs <= 0) {
                redrawStatusLine(statusPrefix + " 0с");
                out.println();
                out.println("Время вышло.");
                logger.info("readLineWithDeadline: timeout, prompt='{}'", prompt);
                return null;
            }

            if (nowMs >= nextTickMs) {
                redrawStatusLine(statusPrefix + " " + remainingSeconds + "с");
                nextTickMs = nowMs + 250L;
            }

            try {
                if (reader.ready()) {
                    String line = reader.readLine();
                    logger.info("readLineWithDeadline: input received (len={})", line == null ? -1 : line.length());
                    return line;
                }
            } catch (IOException e) {
                logger.error("readLineWithDeadline: IO error", e);
                throw new RuntimeException("Failed to read input", e);
            }

            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.info("readLineWithDeadline: interrupted");
                return null;
            }
        }
    }

    /**
     * Перерисовывает строку статуса над строкой ввода.
     *
     * @param statusText текст статусной строки
     */
    private void redrawStatusLine(String statusText) {
        out.print(AnsiCodes.ANSI_SAVE_CURSOR);
        out.print(AnsiCodes.ANSI_CURSOR_UP_1);
        out.print("\r");
        out.print(AnsiCodes.ANSI_CLEAR_LINE);
        out.print(statusText);
        out.print(AnsiCodes.ANSI_RESTORE_CURSOR);
        out.flush();
    }

    /**
     * Считывает строку без ограничения по времени.
     *
     * @return считанная строка или {@code null} при EOF
     */
    private String readLineUnbounded() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input", e);
        }
    }
}
