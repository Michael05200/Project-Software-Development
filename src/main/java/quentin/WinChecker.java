package quentin;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Controlla la vittoria: il Nero deve collegare il bordo alto e quello basso,
 * il Bianco il bordo sinistro e quello destro, con una catena di pietre
 * ortogonalmente adiacenti (le diagonali NON collegano).
 */
public final class WinChecker {

    private WinChecker() {
    }

    public static boolean hasWon(Board board, Stone stone) {
        return !winningChain(board, stone).isEmpty();
    }

    /** La catena che collega i due bordi del colore dato, oppure un insieme vuoto. */
    public static Set<Point> winningChain(Board board, Stone stone) {
        int n = board.size();
        boolean[][] seen = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            int startRow = stone == Stone.BLACK ? 0 : i;
            int startCol = stone == Stone.BLACK ? i : 0;
            if (board.get(startRow, startCol) != stone || seen[startRow][startCol]) {
                continue;
            }
            Set<Point> component = new LinkedHashSet<>();
            Deque<Point> queue = new ArrayDeque<>();
            queue.add(new Point(startRow, startCol));
            seen[startRow][startCol] = true;
            boolean reachesEnd = false;
            while (!queue.isEmpty()) {
                Point p = queue.poll();
                component.add(p);
                if (stone == Stone.BLACK ? p.row() == n - 1 : p.col() == n - 1) {
                    reachesEnd = true;
                }
                for (Point q : board.orthogonalNeighbors(p.row(), p.col())) {
                    if (board.get(q.row(), q.col()) == stone && !seen[q.row()][q.col()]) {
                        seen[q.row()][q.col()] = true;
                        queue.add(q);
                    }
                }
            }
            if (reachesEnd) {
                return component;
            }
        }
        return Collections.emptySet();
    }

    /**
     * Il vincitore della posizione, oppure null. Si controlla prima chi ha appena mosso
     * (il riempimento dei territori potrebbe teoricamente completare anche la catena avversaria).
     */
    public static Stone winner(Board board, Stone mover) {
        if (hasWon(board, mover)) {
            return mover;
        }
        if (hasWon(board, mover.opponent())) {
            return mover.opponent();
        }
        return null;
    }
}
