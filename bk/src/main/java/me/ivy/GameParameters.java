package me.ivy;

/**
 * Параметры игрового сеанса.
 */
public class GameParameters {
    public int secretLength = 4;
    public int maxAttempts = 10;
    public int timeToGuess = 30; // seconds

    @Override
    public String toString() {
        return "GameParameters{" +
                "secretLength=" + secretLength +
                ", maxAttempts=" + maxAttempts +
                ", timeToGuess=" + timeToGuess +
                '}';
    }
}
