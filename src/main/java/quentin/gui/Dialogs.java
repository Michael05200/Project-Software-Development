package quentin.gui;

/**
 * I dialoghi che l'applicazione deve mostrare. Sono dietro un'interfaccia così la logica
 * dell'interfaccia si può provare nei test senza aprire vere finestre.
 */
public interface Dialogs {

    /** Pie rule: chiede a un giocatore umano se vuole cambiare lato. */
    boolean askSwap();

    void showGameOver(String message);

    void showRules(String text);
}
