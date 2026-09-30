package quentin.ai;

import java.util.List;
import java.util.Random;
import quentin.Game;
import quentin.Move;
import quentin.Point;

/** Gioca una mossa legale a caso (utile come avversario base e per i test). */
public final class RandomStrategy implements Strategy {

    private final Random random;

    public RandomStrategy() {
        this(new Random());
    }

    public RandomStrategy(Random random) {
        this.random = random;
    }

    @Override
    public Move choose(Game game) {
        if (game.canSwap() && random.nextBoolean()) {
            return Move.swap();
        }
        List<Point> moves = game.legalMoves();
        if (moves.isEmpty()) {
            return Move.pass();
        }
        Point p = moves.get(random.nextInt(moves.size()));
        return Move.place(p.row(), p.col());
    }

    @Override
    public String name() {
        return "AI casuale";
    }
}
