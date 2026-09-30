package quentin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Le regole di Quentin applicate a una posizione (senza stato di partita). */
public final class Rules {

    private Rules() {
    }

    /**
     * Gioca una pietra e applica le regole del turno:
     * 1) piazza la pietra; 2) riempie TUTTI i territori (calcolati insieme, una sola volta);
     * 3) verifica la regola delle diagonali. Se la mossa non è possibile o è illegale
     * restituisce un Optional vuoto; la board originale non viene mai modificata.
     */
    public static Optional<Board> play(Board board, int row, int col, Stone mover) {
        if (!board.inBounds(row, col) || !board.isEmpty(row, col)) {
            return Optional.empty();
        }
        Board next = board.copy();
        next.set(row, col, mover);
        fillTerritories(next, mover);
        if (!diagonalRuleHolds(next)) {
            return Optional.empty();
        }
        return Optional.of(next);
    }

    /** Riempie ogni territorio col colore di maggioranza (parità: colore dell'avversario di chi muove). */
    public static void fillTerritories(Board board, Stone mover) {
        List<Territory> territories = TerritoryFinder.territories(board);
        for (Territory territory : territories) {
            Stone fill = territory.fillColor(mover);
            for (Point p : territory.points()) {
                board.set(p.row(), p.col(), fill);
            }
        }
    }

    /**
     * Regola delle diagonali: due pietre dello stesso colore in diagonale devono avere
     * almeno un vicino ortogonale in comune dello stesso colore.
     */
    public static boolean diagonalRuleHolds(Board board) {
        int n = board.size();
        for (int r = 0; r < n - 1; r++) {
            for (int c = 0; c < n - 1; c++) {
                Stone topLeft = board.get(r, c);
                Stone topRight = board.get(r, c + 1);
                Stone bottomLeft = board.get(r + 1, c);
                Stone bottomRight = board.get(r + 1, c + 1);
                // diagonale \ : i vicini in comune sono topRight e bottomLeft
                if (topLeft != null && topLeft == bottomRight
                        && topRight != topLeft && bottomLeft != topLeft) {
                    return false;
                }
                // diagonale / : i vicini in comune sono topLeft e bottomRight
                if (topRight != null && topRight == bottomLeft
                        && topLeft != topRight && bottomRight != topRight) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<Point> legalMoves(Board board, Stone mover) {
        List<Point> moves = new ArrayList<>();
        int n = board.size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.isEmpty(r, c) && play(board, r, c, mover).isPresent()) {
                    moves.add(new Point(r, c));
                }
            }
        }
        return moves;
    }

    public static boolean hasLegalMove(Board board, Stone mover) {
        int n = board.size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.isEmpty(r, c) && play(board, r, c, mover).isPresent()) {
                    return true;
                }
            }
        }
        return false;
    }
}
