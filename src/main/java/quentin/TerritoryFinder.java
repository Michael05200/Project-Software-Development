package quentin;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Trova regioni e territori di una board. */
public final class TerritoryFinder {

    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, -1, 1};

    private TerritoryFinder() {
    }

    /** Tutte le regioni: gruppi di punti vuoti ortogonalmente adiacenti. */
    public static List<Set<Point>> regions(Board board) {
        int n = board.size();
        boolean[][] seen = new boolean[n][n];
        List<Set<Point>> result = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (seen[r][c] || !board.isEmpty(r, c)) {
                    continue;
                }
                Set<Point> region = new LinkedHashSet<>();
                Deque<Point> queue = new ArrayDeque<>();
                queue.add(new Point(r, c));
                seen[r][c] = true;
                while (!queue.isEmpty()) {
                    Point p = queue.poll();
                    region.add(p);
                    for (Point q : board.orthogonalNeighbors(p.row(), p.col())) {
                        if (board.isEmpty(q.row(), q.col()) && !seen[q.row()][q.col()]) {
                            seen[q.row()][q.col()] = true;
                            queue.add(q);
                        }
                    }
                }
                result.add(region);
            }
        }
        return result;
    }

    /**
     * I territori della board: le regioni in cui OGNI punto ha almeno due pietre adiacenti.
     * Versione ottimizzata (array di interi): viene chiamata migliaia di volte dall'AI.
     */
    public static List<Territory> territories(Board board) {
        int n = board.size();
        int total = n * n;
        boolean[] visited = new boolean[total];
        int[] stack = new int[total];
        int[] region = new int[total];
        int[] countedIn = new int[total]; // per ogni pietra: id dell'ultima regione che l'ha contata
        List<Territory> result = new ArrayList<>();
        int regionId = 0;

        for (int start = 0; start < total; start++) {
            if (visited[start] || board.get(start / n, start % n) != null) {
                continue;
            }
            regionId++;
            int sp = 0;
            int regionSize = 0;
            stack[sp++] = start;
            visited[start] = true;
            boolean territory = true;
            int black = 0;
            int white = 0;

            while (sp > 0) {
                int cell = stack[--sp];
                region[regionSize++] = cell;
                int r = cell / n;
                int c = cell % n;
                int adjacentStones = 0;
                for (int k = 0; k < 4; k++) {
                    int nr = r + DR[k];
                    int nc = c + DC[k];
                    if (nr < 0 || nr >= n || nc < 0 || nc >= n) {
                        continue;
                    }
                    Stone s = board.get(nr, nc);
                    int idx = nr * n + nc;
                    if (s == null) {
                        if (!visited[idx]) {
                            visited[idx] = true;
                            stack[sp++] = idx;
                        }
                    } else {
                        adjacentStones++;
                        if (countedIn[idx] != regionId) {
                            countedIn[idx] = regionId;
                            if (s == Stone.BLACK) {
                                black++;
                            } else {
                                white++;
                            }
                        }
                    }
                }
                if (adjacentStones < 2) {
                    territory = false;
                }
            }

            if (territory) {
                List<Point> points = new ArrayList<>(regionSize);
                for (int i = 0; i < regionSize; i++) {
                    points.add(new Point(region[i] / n, region[i] % n));
                }
                result.add(new Territory(points, black, white));
            }
        }
        return result;
    }
}
