package quentin.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import quentin.Board;
import quentin.Game;
import quentin.Move;
import quentin.Point;
import quentin.Rules;
import quentin.Stone;

/**
 * AI classica: minimax con potatura alpha-beta a profondità limitata e "beam":
 * a ogni nodo si espandono solo le migliori {@code beam} mosse secondo l'euristica
 * (distanza minima dal completare la catena, vedi {@link Evaluator}).
 * Le vittorie immediate non vengono mai potate.
 */
public final class MinimaxStrategy implements Strategy {

    private record Child(Point move, Board board, int score) {
    }

    private final String name;
    private final int depth;
    private final int beam;
    private final Random random;

    public MinimaxStrategy(String name, int depth, int beam, Random random) {
        if (depth < 1 || beam < 1) {
            throw new IllegalArgumentException("depth e beam devono essere almeno 1");
        }
        this.name = name;
        this.depth = depth;
        this.beam = beam;
        this.random = random;
    }

    public MinimaxStrategy(int depth, int beam, Random random) {
        this("AI minimax (profondità " + depth + ")", depth, beam, random);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Move choose(Game game) {
        Board board = game.board();
        if (game.canSwap() && shouldSwap(board)) {
            return Move.swap();
        }
        Stone me = game.current();
        if (isEmpty(board)) {
            return opening(board);
        }

        List<Child> children = expand(board, me, me);
        if (children.isEmpty()) {
            return Move.pass();
        }
        children.sort((a, b) -> Integer.compare(b.score(), a.score()));

        // 1) vittoria immediata
        for (Child child : children) {
            if (child.score() == Evaluator.WIN) {
                return place(child.move());
            }
        }

        // 2) ricerca: si valutano le migliori mosse e si sceglie a caso tra quelle a pari valore
        int rootLimit = depth == 1 ? children.size() : Math.min(children.size(), beam + 2);
        int bestValue = Integer.MIN_VALUE;
        List<Child> best = new ArrayList<>();
        for (int i = 0; i < rootLimit; i++) {
            Child child = children.get(i);
            int value = depth == 1
                    ? child.score()
                    : search(child.board(), me.opponent(), depth - 1,
                            Integer.MIN_VALUE, Integer.MAX_VALUE, me);
            if (value > bestValue) {
                bestValue = value;
                best.clear();
                best.add(child);
            } else if (value == bestValue) {
                best.add(child);
            }
        }
        return place(best.get(random.nextInt(best.size())).move());
    }

    private int search(Board board, Stone toMove, int remaining, int alpha, int beta, Stone me) {
        boolean maximizing = toMove == me;
        List<Child> children = expand(board, toMove, me);

        if (children.isEmpty()) { // chi deve muovere non ha mosse: passa
            if (remaining <= 1) {
                return Evaluator.score(board, me, toMove);
            }
            return search(board, toMove.opponent(), remaining - 1, alpha, beta, me);
        }

        int winningScore = maximizing ? Evaluator.WIN : -Evaluator.WIN;
        for (Child child : children) {
            if (child.score() == winningScore) { // chi muove vince subito: meglio se prima
                return maximizing ? winningScore + remaining : winningScore - remaining;
            }
        }

        if (maximizing) {
            children.sort((a, b) -> Integer.compare(b.score(), a.score()));
        } else {
            children.sort((a, b) -> Integer.compare(a.score(), b.score()));
        }
        if (remaining == 1) {
            return children.get(0).score();
        }

        int limit = Math.min(beam, children.size());
        int value = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < limit; i++) {
            int v = search(children.get(i).board(), toMove.opponent(), remaining - 1, alpha, beta, me);
            if (maximizing) {
                value = Math.max(value, v);
                alpha = Math.max(alpha, value);
            } else {
                value = Math.min(value, v);
                beta = Math.min(beta, value);
            }
            if (alpha >= beta) {
                break;
            }
        }
        return value;
    }

    /** Tutte le mosse legali di {@code toMove} con la posizione risultante e il suo punteggio (per {@code me}). */
    private List<Child> expand(Board board, Stone toMove, Stone me) {
        List<Child> children = new ArrayList<>();
        int n = board.size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (Thread.currentThread().isInterrupted()) {
                    return children; // partita annullata dalla GUI: il risultato verrà scartato
                }
                if (!board.isEmpty(r, c)) {
                    continue;
                }
                Optional<Board> next = Rules.play(board, r, c, toMove);
                if (next.isPresent()) {
                    children.add(new Child(new Point(r, c), next.get(),
                            Evaluator.score(next.get(), me, toMove)));
                }
            }
        }
        return children;
    }

    /** Pie rule: il Bianco scambia i lati se il Nero ha aperto vicino al centro (mossa troppo forte). */
    private boolean shouldSwap(Board board) {
        int n = board.size();
        int mid = n / 2;
        int radius = n / 4;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.get(r, c) == Stone.BLACK
                        && Math.max(Math.abs(r - mid), Math.abs(c - mid)) <= radius) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Prima mossa del Nero: un punto non centrale (e non sul bordo), così la pie rule ha senso. */
    private Move opening(Board board) {
        int n = board.size();
        int mid = n / 2;
        List<Point> candidates = new ArrayList<>();
        List<Point> all = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Point p = new Point(r, c);
                all.add(p);
                boolean inner = r > 0 && r < n - 1 && c > 0 && c < n - 1;
                if (inner && Math.max(Math.abs(r - mid), Math.abs(c - mid)) > n / 4) {
                    candidates.add(p);
                }
            }
        }
        List<Point> pool = candidates.isEmpty() ? all : candidates;
        return place(pool.get(random.nextInt(pool.size())));
    }

    private static boolean isEmpty(Board board) {
        int n = board.size();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (!board.isEmpty(r, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Move place(Point p) {
        return Move.place(p.row(), p.col());
    }
}
