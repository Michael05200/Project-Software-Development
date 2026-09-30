package quentin;

/** Esito di un tentativo di mossa. */
public enum MoveResult {
    OK,
    OUT_OF_BOUNDS,
    OCCUPIED,
    ILLEGAL,
    GAME_OVER
}
