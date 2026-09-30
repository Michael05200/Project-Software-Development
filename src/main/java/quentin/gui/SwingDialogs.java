package quentin.gui;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Implementazione con JOptionPane. */
final class SwingDialogs implements Dialogs {

    private final Component parent;

    SwingDialogs(Component parent) {
        this.parent = parent;
    }

    @Override
    public boolean askSwap() {
        int answer = JOptionPane.showConfirmDialog(parent,
                "Il Nero ha aperto. Vuoi cambiare lato (pie rule)?\n"
                        + "Se accetti prendi il posto del Nero, con la sua pietra,\n"
                        + "e l'avversario gioca col Bianco.",
                "Pie rule", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    @Override
    public void showGameOver(String message) {
        JOptionPane.showMessageDialog(parent, message, "Partita finita", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showRules(String text) {
        JTextArea area = new JTextArea(text, 18, 48);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JOptionPane.showMessageDialog(parent, new JScrollPane(area),
                "Regole di Quentin", JOptionPane.INFORMATION_MESSAGE);
    }
}
