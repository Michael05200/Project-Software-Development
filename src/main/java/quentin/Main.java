package quentin;

import javax.swing.SwingUtilities;
import quentin.gui.QuentinApp;

/** Punto di ingresso: tutto il codice grafico parte sull'Event Dispatch Thread. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuentinApp::start);
    }
}
