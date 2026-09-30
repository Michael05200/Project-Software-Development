package quentin.ai;

import quentin.Game;
import quentin.Move;

/** Un "cervello" che sceglie la mossa per il giocatore di turno. Non modifica la partita. */
public interface Strategy {

    Move choose(Game game);

    String name();
}
