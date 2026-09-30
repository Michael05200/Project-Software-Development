package quentin.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import quentin.Game;
import quentin.Move;
import quentin.MoveResult;
import quentin.Point;
import quentin.Stone;
import quentin.ai.Strategy;

/**
 * Il contenuto della finestra: barra di stato, scacchiera, controlli, e il collegamento
 * tra interfaccia e logica di gioco. Non conosce il JFrame: i dialoghi passano da {@link Dialogs}.
 *
 * <p>Tutto gira sull'Event Dispatch Thread; il calcolo dell'AI, che può durare qualche
 * secondo, gira in un SwingWorker per non bloccare la finestra.
 */
@SuppressWarnings("serial")
public final class QuentinPanel extends JPanel {

    private static final Integer[] BOARD_SIZES = {5, 7, 9, 11, 13, 15, 19};
    private static final int DEFAULT_BOARD_SIZE = 13;

    static final String RULES_TEXT = """
            Quentin è un gioco di connessione per due giocatori, senza pareggi.

            OBIETTIVO
            Il Nero deve collegare il bordo alto con quello basso, il Bianco il bordo sinistro con quello destro, con una catena di pietre ortogonalmente adiacenti (le diagonali non collegano).

            IL TURNO
            1. Il giocatore di turno piazza una pietra del proprio colore su un incrocio vuoto. Il Nero muove per primo.
            2. Poi ogni territorio viene riempito con pietre del colore che ha la maggioranza di pietre adiacenti. In caso di parità, con pietre del colore dell'avversario di chi ha appena mosso.
            3. A fine turno, due pietre dello stesso colore in diagonale devono avere almeno un vicino ortogonale in comune dello stesso colore. Altrimenti la mossa è illegale e va scelta un'altra.

            DEFINIZIONI
            Regione: gruppo di punti vuoti ortogonalmente adiacenti. Territorio: regione in cui ogni punto ha almeno due pietre adiacenti.

            PASSARE
            Si passa solo se non c'è nessuna mossa legale.

            PIE RULE
            Al suo primo turno il Bianco può, invece di muovere, cambiare lato: prende il posto del Nero (con la pietra già giocata) e l'avversario gioca col Bianco.

            COMANDI
            Clicca su un incrocio per giocare. In alto scegli la dimensione e chi controlla ciascun lato (persona o AI), poi premi "Nuova partita".
            """;

    private final Dialogs dialogs;
    private final BoardPanel boardPanel;
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.LEFT);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.LEFT);
    private final JComboBox<Integer> sizeCombo = new JComboBox<>(BOARD_SIZES);
    private final JComboBox<PlayerType> firstPlayerCombo = new JComboBox<>(PlayerType.values());
    private final JComboBox<PlayerType> secondPlayerCombo = new JComboBox<>(PlayerType.values());
    private final JButton newGameButton = new JButton("Nuova partita");
    private final JButton passButton = new JButton("Passa");
    private final Timer aiTimer;

    private final PlayerType[] playerTypes = {PlayerType.HUMAN, PlayerType.HUMAN};
    private final Strategy[] strategies = new Strategy[2];
    private Game game;
    private int generation;          // cambia a ogni nuova partita: serve a scartare risposte "vecchie" dell'AI
    private boolean aiThinking;
    private SwingWorker<Move, Void> worker;

    /**
     * @param dialogs   come mostrare i dialoghi
     * @param aiDelayMs pausa prima di ogni mossa dell'AI (si vede meglio l'AI contro AI)
     */
    public QuentinPanel(Dialogs dialogs, int aiDelayMs) {
        super(new BorderLayout());
        this.dialogs = dialogs;
        this.game = new Game(DEFAULT_BOARD_SIZE);
        this.boardPanel = new BoardPanel(game);
        this.aiTimer = new Timer(aiDelayMs, e -> startAiMove());
        this.aiTimer.setRepeats(false);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        boardPanel.setPointListener(this::onBoardClicked);
        newGameButton.addActionListener(e -> newGame());
        passButton.addActionListener(e -> onPass());
        sizeCombo.setSelectedItem(DEFAULT_BOARD_SIZE);
        newGame();
    }

    // ------------------------------------------------------------ costruzione dell'interfaccia

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(statusLabel, cell(0, 0, 6, 1.0, new Insets(0, 0, 2, 0)));
        panel.add(messageLabel, cell(0, 1, 6, 1.0, new Insets(0, 0, 8, 0)));

        panel.add(new JLabel("Dimensione:"), cell(0, 2, 1, 0.0, new Insets(0, 0, 0, 4)));
        panel.add(sizeCombo, cell(1, 2, 1, 0.0, new Insets(0, 0, 0, 12)));
        panel.add(new JLabel("Giocatore 1 (Nero):"), cell(2, 2, 1, 0.0, new Insets(0, 0, 0, 4)));
        panel.add(firstPlayerCombo, cell(3, 2, 1, 0.0, new Insets(0, 0, 0, 12)));
        panel.add(new JLabel("Giocatore 2 (Bianco):"), cell(4, 2, 1, 0.0, new Insets(0, 0, 0, 4)));
        panel.add(secondPlayerCombo, cell(5, 2, 1, 1.0, new Insets(0, 0, 0, 0)));
        return panel;
    }

    private static GridBagConstraints cell(int x, int y, int width, double weightX, Insets insets) {
        return new GridBagConstraints(x, y, width, 1, weightX, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panel.add(newGameButton);
        panel.add(passButton);
        return panel;
    }

    // ------------------------------------------------------------ azioni pubbliche

    /** Inizia una nuova partita con dimensione e giocatori scelti nei menu a tendina. */
    public void newGame() {
        cancelPending();
        generation++;
        game = new Game((Integer) sizeCombo.getSelectedItem());
        playerTypes[0] = (PlayerType) firstPlayerCombo.getSelectedItem();
        playerTypes[1] = (PlayerType) secondPlayerCombo.getSelectedItem();
        for (int i = 0; i < 2; i++) {
            strategies[i] = playerTypes[i].createStrategy();
        }
        boardPanel.setGame(game);
        setMessage("");
        nextTurn();
    }

    public void showRules() {
        dialogs.showRules(RULES_TEXT);
    }

    /** Ferma timer e calcolo dell'AI in corso (da chiamare quando la finestra viene chiusa). */
    public void dispose() {
        cancelPending();
    }

    private void cancelPending() {
        aiTimer.stop();
        if (worker != null) {
            worker.cancel(true);
            worker = null;
        }
        aiThinking = false;
    }

    // ------------------------------------------------------------ flusso della partita

    /** Aggiorna interfaccia e stato dopo ogni azione, poi passa la mano a persona o AI. */
    private void nextTurn() {
        boardPanel.refresh();
        updateStatus();
        if (game.isOver()) {
            finishGame();
            return;
        }
        int seat = game.playerIndex(game.current());
        boolean human = strategies[seat] == null;
        boardPanel.setInteractive(human);
        boolean mustPass = human && !game.hasLegalMove();
        passButton.setEnabled(mustPass);
        if (human) {
            if (mustPass) {
                setMessage("Non hai mosse legali: devi passare.");
            }
            if (game.canSwap()) {
                offerSwap();
            }
        } else {
            aiTimer.restart();
        }
    }

    private void updateStatus() {
        if (game.isOver()) {
            statusLabel.setText(colorName(game.winner()) + " ha vinto! ("
                    + seatName(game.playerIndex(game.winner())) + ")");
            return;
        }
        String text = "Turno del " + colorName(game.current())
                + " – " + seatName(game.playerIndex(game.current()));
        statusLabel.setText(aiThinking ? text + " – sta pensando…" : text);
    }

    private void finishGame() {
        passButton.setEnabled(false);
        boardPanel.setInteractive(false);
        String text = colorName(game.winner()) + " ha vinto la partita!\n"
                + seatName(game.playerIndex(game.winner()));
        int expected = generation;
        // in differita: prima la scacchiera mostra la posizione finale e la catena vincente
        SwingUtilities.invokeLater(() -> {
            if (expected == generation) {
                dialogs.showGameOver(text);
            }
        });
    }

    private void onBoardClicked(Point p) {
        if (game.isOver() || aiThinking || strategies[game.playerIndex(game.current())] != null) {
            return;
        }
        MoveResult result = game.play(p.row(), p.col());
        switch (result) {
            case OK -> {
                setMessage("");
                nextTurn();
            }
            case OCCUPIED -> setMessage("Quel punto è già occupato.");
            case ILLEGAL -> setMessage("Mossa illegale: due pietre uguali in diagonale devono avere un vicino comune dello stesso colore.");
            case OUT_OF_BOUNDS, GAME_OVER -> { }
        }
    }

    private void onPass() {
        if (game.isOver() || aiThinking) {
            return;
        }
        if (game.pass()) {
            setMessage(seatName(game.playerIndex(game.current().opponent())) + " ha passato.");
            nextTurn();
        } else {
            setMessage("Puoi passare solo se non hai nessuna mossa legale.");
        }
    }

    /** Pie rule per un giocatore umano: dialogo Sì/No al primo turno del Bianco. */
    private void offerSwap() {
        int expected = generation;
        SwingUtilities.invokeLater(() -> {
            if (expected != generation || game.isOver() || !game.canSwap()) {
                return;
            }
            boolean swap = dialogs.askSwap();
            if (swap && expected == generation && game.swapSides()) {
                setMessage("Lati scambiati: ora " + seatName(game.playerIndex(Stone.BLACK)) + " gioca col Nero.");
                nextTurn();
            }
        });
    }

    // ------------------------------------------------------------ turno dell'AI

    private void startAiMove() {
        if (game.isOver()) {
            return;
        }
        int seat = game.playerIndex(game.current());
        Strategy strategy = strategies[seat];
        if (strategy == null) {
            return;
        }
        aiThinking = true;
        updateStatus();

        int expected = generation;
        Game snapshot = game.copy(); // il calcolo lavora su una copia: mai sulla partita condivisa
        worker = new SwingWorker<>() {
            @Override
            protected Move doInBackground() {
                return strategy.choose(snapshot);
            }

            @Override
            protected void done() {
                if (isCancelled() || expected != generation) {
                    return;
                }
                aiThinking = false;
                worker = null;
                Move move = null;
                try {
                    move = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    setMessage("Errore dell'AI: " + ex.getCause());
                }
                String description = move == null ? "" : describe(move, seat);
                applyOrFallback(move);
                if (!description.isEmpty()) {
                    setMessage(description);
                }
                nextTurn();
            }
        };
        worker.execute();
    }

    /** Applica la mossa dell'AI; se per qualsiasi motivo non fosse valida, gioca la prima mossa legale. */
    private void applyOrFallback(Move move) {
        if (move != null && game.apply(move) == MoveResult.OK) {
            return;
        }
        List<Point> legal = game.legalMoves();
        if (legal.isEmpty()) {
            game.pass();
        } else {
            game.play(legal.get(0).row(), legal.get(0).col());
        }
    }

    private String describe(Move move, int seat) {
        return switch (move.type()) {
            case PLACE -> seatName(seat) + " ha giocato in riga " + (move.point().row() + 1)
                    + ", colonna " + (move.point().col() + 1) + ".";
            case PASS -> seatName(seat) + " ha passato.";
            case SWAP -> seatName(seat) + " ha cambiato lato (pie rule).";
        };
    }

    // ------------------------------------------------------------ testi

    private void setMessage(String text) {
        messageLabel.setText(text.isEmpty() ? " " : text);
    }

    private String seatName(int seat) {
        return "Giocatore " + (seat + 1) + " (" + playerTypes[seat].label() + ")";
    }

    private static String colorName(Stone stone) {
        return stone == Stone.BLACK ? "Nero" : "Bianco";
    }

    // ------------------------------------------------------------ appoggi per i test (stesso package)

    Game game() {
        return game;
    }

    BoardPanel boardPanel() {
        return boardPanel;
    }

    String statusText() {
        return statusLabel.getText();
    }

    String messageText() {
        return messageLabel.getText();
    }

    boolean passEnabled() {
        return passButton.isEnabled();
    }

    void pressPass() {
        passButton.doClick();
    }

    boolean isAiThinking() {
        return aiThinking;
    }

    /** Imposta dimensione e giocatori e inizia subito una nuova partita. */
    void configure(int size, PlayerType first, PlayerType second) {
        sizeCombo.setSelectedItem(size);
        firstPlayerCombo.setSelectedItem(first);
        secondPlayerCombo.setSelectedItem(second);
        newGame();
    }
}
