package quentin.ai;

import java.util.ArrayDeque;
import java.util.Arrays;
import quentin.Board;
import quentin.Stone;

/**
 * Valutazione euristica di una posizione.
 *
 * <p>La "distanza" di un colore è il numero minimo di punti vuoti che deve ancora riempire
 * per completare la propria catena: percorso più breve tra i due bordi, dove passare
 * su una pietra propria costa 0, su un punto vuoto costa 1 e le pietre avversarie bloccano.
 * Si calcola con una BFS a pesi 0/1. Distanza 0 = catena completa = vittoria.
 */
public final class Evaluator {

    /** Distanza assegnata quando il colore non ha più nessun percorso possibile. */
    public static final int INFINITE = 1000;
    /** Punteggio di una posizione vinta. */
    public static final int WIN = 100_000;

    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, -1, 1};

    private Evaluator() {
    }

    public static int distance(Board board, Stone stone) {
        int n = board.size();
        int[] dist = new int[n * n];
        Arrays.fill(dist, INFINITE);
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        // sorgenti: tutti i punti del bordo di partenza
        for (int i = 0; i < n; i++) {
            int r = stone == Stone.BLACK ? 0 : i;
            int c = stone == Stone.BLACK ? i : 0;
            Stone s = board.get(r, c);
            if (s == stone.opponent()) {
                continue;
            }
            int cost = s == stone ? 0 : 1;
            int idx = r * n + c;
            if (cost < dist[idx]) {
                dist[idx] = cost;
                if (cost == 0) {
                    deque.addFirst(idx);
                } else {
                    deque.addLast(idx);
                }
            }
        }

        while (!deque.isEmpty()) {
            int cell = deque.pollFirst();
            int r = cell / n;
            int c = cell % n;
            for (int k = 0; k < 4; k++) {
                int nr = r + DR[k];
                int nc = c + DC[k];
                if (nr < 0 || nr >= n || nc < 0 || nc >= n) {
                    continue;
                }
                Stone s = board.get(nr, nc);
                if (s == stone.opponent()) {
                    continue;
                }
                int cost = s == stone ? 0 : 1;
                int idx = nr * n + nc;
                int candidate = dist[cell] + cost;
                if (candidate < dist[idx]) {
                    dist[idx] = candidate;
                    if (cost == 0) {
                        deque.addFirst(idx);
                    } else {
                        deque.addLast(idx);
                    }
                }
            }
        }

        // pozzi: tutti i punti del bordo opposto
        int best = INFINITE;
        for (int i = 0; i < n; i++) {
            int r = stone == Stone.BLACK ? n - 1 : i;
            int c = stone == Stone.BLACK ? i : n - 1;
            best = Math.min(best, dist[r * n + c]);
        }
        return best;
    }

    /**
     * Punteggio della posizione dal punto di vista di {@code me}: più è alto, meglio sta {@code me}.
     * {@code mover} è chi ha appena mosso (serve solo se entrambe le catene risultassero complete).
     */
    public static int score(Board board, Stone me, Stone mover) {
        int mine = distance(board, me);
        int theirs = distance(board, me.opponent());
        if (mine == 0 && theirs == 0) {
            return mover == me ? WIN : -WIN;
        }
        if (mine == 0) {
            return WIN;
        }
        if (theirs == 0) {
            return -WIN;
        }
        return theirs - mine;
    }
}
