package quentin.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import quentin.Board;
import quentin.Stone;

/**
 * Prova il collegamento tra interfaccia e gioco senza aprire finestre: il pannello gira
 * sull'Event Dispatch Thread e i dialoghi sono sostituiti da un finto {@link Dialogs}.
 */
class QuentinPanelTest {

    private static final class FakeDialogs implements Dialogs {
        volatile boolean swapAnswer;
        final AtomicInteger swapAsked = new AtomicInteger();
        volatile String gameOverMessage;
        volatile String rulesText;

        @Override
        public boolean askSwap() {
            swapAsked.incrementAndGet();
            return swapAnswer;
        }

        @Override
        public void showGameOver(String message) {
            gameOverMessage = message;
        }

        @Override
        public void showRules(String text) {
            rulesText = text;
        }
    }

    // ---------------------------------------------------------------- utilità

    private static void onEdt(Runnable action) {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T query(java.util.function.Supplier<T> supplier) {
        Object[] box = new Object[1];
        onEdt(() -> {
            box[0] = supplier.get();
        });
        @SuppressWarnings("unchecked")
        T value = (T) box[0];
        return value;
    }

    private static int intOnEdt(java.util.function.IntSupplier supplier) {
        int[] box = new int[1];
        onEdt(() -> {
            box[0] = supplier.getAsInt();
        });
        return box[0];
    }

    /** Svuota la coda dell'EDT: esegue tutto ciò che era già stato messo in coda (invokeLater). */
    private static void flush() {
        onEdt(() -> { });
    }

    private static QuentinPanel newPanel(FakeDialogs dialogs, int size, PlayerType first, PlayerType second) {
        return query(() -> {
            QuentinPanel panel = new QuentinPanel(dialogs, 0);
            panel.boardPanel().setSize(700, 700);
            panel.configure(size, first, second);
            return panel;
        });
    }

    private static void click(QuentinPanel panel, int row, int col) {
        onEdt(() -> {
            BoardPanel board = panel.boardPanel();
            int[] px = board.pixelOf(row, col);
            board.processMouseEvent(new MouseEvent(board, MouseEvent.MOUSE_RELEASED, 0L, 0,
                    px[0], px[1], 1, false, MouseEvent.BUTTON1));
        });
    }

    private static boolean waitFor(BooleanSupplier condition, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (query(condition::getAsBoolean)) {
                return true;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return query(condition::getAsBoolean);
    }

    private static int stones(Board board) {
        int count = 0;
        for (int r = 0; r < board.size(); r++) {
            for (int c = 0; c < board.size(); c++) {
                if (!board.isEmpty(r, c)) {
                    count++;
                }
            }
        }
        return count;
    }

    // ---------------------------------------------------------------- test

    @Test
    void laPartitaParteConIlTurnoDelNeroEUnaBoardVuota() {
        QuentinPanel panel = newPanel(new FakeDialogs(), 5, PlayerType.HUMAN, PlayerType.HUMAN);
        assertEquals(5, intOnEdt(() -> panel.game().size()));
        assertEquals("Turno del Nero – Giocatore 1 (Umano)", query(panel::statusText));
        assertFalse(query(panel::passEnabled));
        onEdt(panel::dispose);
    }

    @Test
    void iClicDeiDueUmaniPiazzanoLePietreAlternandoIColori() {
        FakeDialogs dialogs = new FakeDialogs(); // risponde "No" alla pie rule
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.HUMAN, PlayerType.HUMAN);

        click(panel, 0, 0);
        flush(); // qui compare (finto) il dialogo della pie rule
        click(panel, 4, 4);

        assertEquals(Stone.BLACK, query(() -> panel.game().stoneAt(0, 0)));
        assertEquals(Stone.WHITE, query(() -> panel.game().stoneAt(4, 4)));
        assertEquals(Stone.BLACK, query(() -> panel.game().current()));
        assertEquals("Turno del Nero – Giocatore 1 (Umano)", query(panel::statusText));
        onEdt(panel::dispose);
    }

    @Test
    void sePiazziSuUnPuntoOccupatoVieneMostratoUnMessaggioENonCambiaIlTurno() {
        QuentinPanel panel = newPanel(new FakeDialogs(), 5, PlayerType.HUMAN, PlayerType.HUMAN);
        click(panel, 2, 2);
        flush();
        click(panel, 2, 2);
        assertEquals("Quel punto è già occupato.", query(panel::messageText));
        assertEquals(Stone.WHITE, query(() -> panel.game().current()));
        onEdt(panel::dispose);
    }

    @Test
    void laPieRuleChiedeAlBiancoUmanoEAccettandoScambiaILati() {
        FakeDialogs dialogs = new FakeDialogs();
        dialogs.swapAnswer = true;
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.HUMAN, PlayerType.HUMAN);

        click(panel, 2, 2);
        flush();

        assertEquals(1, dialogs.swapAsked.get());
        assertTrue(query(() -> panel.game().isSwapped()));
        assertEquals(Stone.WHITE, query(() -> panel.game().current()));
        assertEquals("Turno del Bianco – Giocatore 1 (Umano)", query(panel::statusText));
        onEdt(panel::dispose);
    }

    @Test
    void laPieRuleNonVieneChiestaUnaSecondaVolta() {
        FakeDialogs dialogs = new FakeDialogs();
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.HUMAN, PlayerType.HUMAN);
        click(panel, 2, 2);
        flush();
        click(panel, 0, 0);
        flush();
        click(panel, 4, 4);
        flush();
        assertEquals(1, dialogs.swapAsked.get());
        onEdt(panel::dispose);
    }

    @Test
    void ilBottonePassaEAbilitatoSoloSeNonCiSonoMosseLegali() {
        QuentinPanel panel = newPanel(new FakeDialogs(), 5, PlayerType.HUMAN, PlayerType.HUMAN);
        assertFalse(query(panel::passEnabled));
        onEdt(panel::pressPass); // non ha effetto: ci sono mosse
        assertEquals(Stone.BLACK, query(() -> panel.game().current()));
        onEdt(panel::dispose);
    }

    @Test
    void controUnaAiLaRispostaArrivaDaSolaEIlTurnoTornaAllUmano() {
        FakeDialogs dialogs = new FakeDialogs();
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.HUMAN, PlayerType.EASY);

        click(panel, 0, 0); // apertura lontana dal centro: l'AI non scambia

        assertTrue(waitFor(() -> stones(panel.game().board()) >= 2 && !panel.isAiThinking(), 10_000),
                "l'AI non ha risposto");
        assertEquals(2, intOnEdt(() -> stones(panel.game().board())));
        assertEquals(Stone.BLACK, query(() -> panel.game().current()));
        onEdt(panel::dispose);
    }

    @Test
    void unaPartitaTraDueAiFinisceEMostraIlDialogoDiFinePartita() {
        FakeDialogs dialogs = new FakeDialogs();
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.MEDIUM, PlayerType.EASY);

        assertTrue(waitFor(() -> panel.game().isOver(), 30_000), "la partita non è finita");
        flush(); // il dialogo di fine partita viene mostrato in differita
        assertTrue(waitFor(() -> dialogs.gameOverMessage != null, 5_000));
        Stone winner = query(() -> panel.game().winner());
        assertTrue(dialogs.gameOverMessage.startsWith(winner == Stone.BLACK ? "Nero" : "Bianco"));
        assertTrue(query(panel::statusText).contains("ha vinto"));
        assertFalse(query(panel::passEnabled));
        onEdt(panel::dispose);
    }

    @Test
    void unaNuovaPartitaAzzeraLaScacchiera() {
        QuentinPanel panel = newPanel(new FakeDialogs(), 5, PlayerType.HUMAN, PlayerType.HUMAN);
        click(panel, 2, 2);
        flush();
        onEdt(() -> panel.configure(7, PlayerType.HUMAN, PlayerType.HUMAN));
        assertEquals(7, intOnEdt(() -> panel.game().size()));
        assertEquals(0, intOnEdt(() -> stones(panel.game().board())));
        assertEquals(Stone.BLACK, query(() -> panel.game().current()));
        onEdt(panel::dispose);
    }

    @Test
    void ilPannelloMostraLeRegoleTramiteIDialoghi() {
        FakeDialogs dialogs = new FakeDialogs();
        QuentinPanel panel = newPanel(dialogs, 5, PlayerType.HUMAN, PlayerType.HUMAN);
        onEdt(panel::showRules);
        assertTrue(dialogs.rulesText.contains("PIE RULE"));
        onEdt(panel::dispose);
    }
}
