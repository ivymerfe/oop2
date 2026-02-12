package me.ivy;

/**
 * Контракт ведущего игры, управляющего игровым циклом.
 */
public interface IGameHost {
    /**
     * Запускает один сеанс игры с указанными параметрами.
     *
     * @param params параметры игрового сеанса
     */
    void hostGame(GameParameters params);
}
