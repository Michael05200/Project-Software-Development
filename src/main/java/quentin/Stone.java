package quentin;

/** Colore di una pietra. Nero collega alto e basso, Bianco collega sinistra e destra. */
public enum Stone {
    BLACK, WHITE;

    public Stone opponent() {
        return this == BLACK ? WHITE : BLACK;
    }
}
