package quentin;

import java.util.List;

/**
 * Un territorio: regione di punti vuoti in cui ogni punto ha almeno due pietre adiacenti.
 * {@code blackStones} e {@code whiteStones} contano le pietre DISTINTE adiacenti al territorio.
 */
public record Territory(List<Point> points, int blackStones, int whiteStones) {

    /**
     * Colore con cui il territorio viene riempito: vince la maggioranza;
     * in caso di parità si usa il colore dell'avversario di chi ha appena mosso.
     */
    public Stone fillColor(Stone mover) {
        if (blackStones > whiteStones) {
            return Stone.BLACK;
        }
        if (whiteStones > blackStones) {
            return Stone.WHITE;
        }
        return mover.opponent();
    }
}
