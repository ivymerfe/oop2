package me.ivy;

/**
 * Контракт игры «Быки и коровы».
 */
public interface IBullsCowsGame {
    /**
     * Генерирует новое секретное число заданной длины.
     *
     * @param length длина секрета в цифрах
     */
    void generateSecretNumber(int length);

    /**
     * Возвращает текущее секретное число.
     *
     * @return секретное число
     */
    String getSecretNumber();

    /**
     * Проверяет пользовательскую догадку и возвращает результат сравнения.
     *
     * @param guess догадка игрока
     * @return количество быков и коров
     */
    GuessResult tryGuess(String guess);
}
