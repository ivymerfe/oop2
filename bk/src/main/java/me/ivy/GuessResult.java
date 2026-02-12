package me.ivy;

/**
 * Результат проверки догадки: количество быков и коров.
 */
public class GuessResult {
    int bulls;
    int cows;

    /**
     * Создает результат проверки догадки.
     *
     * @param bulls количество быков
     * @param cows количество коров
     */
    public GuessResult(int bulls, int cows) {
        this.bulls = bulls;
        this.cows = cows;
    }

    /**
    * Возвращает количество быков.
    *
    * @return количество быков
    */
    public int getBulls() {
        return bulls;
    }

    /**
     * Возвращает количество коров.
     *
     * @return количество коров
     */
    public int getCows() {
        return cows;
    }
}
