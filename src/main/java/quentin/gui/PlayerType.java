package quentin.gui;

import java.util.Random;
import quentin.ai.MinimaxStrategy;
import quentin.ai.RandomStrategy;
import quentin.ai.Strategy;

/** Chi controlla un lato: una persona oppure una delle AI disponibili. */
public enum PlayerType {
    HUMAN("Umano"),
    RANDOM("AI casuale"),
    EASY("AI facile"),
    MEDIUM("AI media"),
    HARD("AI forte");

    private final String label;

    PlayerType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** La strategia da usare, oppure null se il giocatore è umano. */
    public Strategy createStrategy() {
        return switch (this) {
            case HUMAN -> null;
            case RANDOM -> new RandomStrategy();
            case EASY -> new MinimaxStrategy(label, 1, 1, new Random());
            case MEDIUM -> new MinimaxStrategy(label, 2, 10, new Random());
            case HARD -> new MinimaxStrategy(label, 4, 6, new Random());
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
