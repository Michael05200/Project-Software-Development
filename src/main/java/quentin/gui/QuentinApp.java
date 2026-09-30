package quentin.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/** Crea e mostra la finestra principale (un solo JFrame, con menu). */
public final class QuentinApp {

    private static final int AI_DELAY_MS = 350;

    private QuentinApp() {
    }

    /** Da chiamare sull'Event Dispatch Thread. */
    public static void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            // si usa il look-and-feel predefinito
        }

        JFrame frame = new JFrame("Quentin");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        QuentinPanel panel = new QuentinPanel(new SwingDialogs(frame), AI_DELAY_MS);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                panel.dispose();
            }
        });
        frame.setContentPane(panel);
        frame.setJMenuBar(buildMenuBar(frame, panel));

        frame.pack();
        frame.setMinimumSize(new Dimension(600, 700));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JMenuBar buildMenuBar(JFrame frame, QuentinPanel panel) {
        JMenu gameMenu = new JMenu("Partita");
        JMenuItem newItem = new JMenuItem("Nuova partita");
        newItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newItem.addActionListener(e -> panel.newGame());
        JMenuItem quitItem = new JMenuItem("Esci");
        quitItem.addActionListener(e -> frame.dispose());
        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        JMenu helpMenu = new JMenu("Aiuto");
        JMenuItem rulesItem = new JMenuItem("Regole del gioco");
        rulesItem.addActionListener(e -> panel.showRules());
        helpMenu.add(rulesItem);

        JMenuBar bar = new JMenuBar();
        bar.add(gameMenu);
        bar.add(helpMenu);
        return bar;
    }
}
