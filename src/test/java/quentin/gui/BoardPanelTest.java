package quentin.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import quentin.Game;
import quentin.Point;

class BoardPanelTest {

    private static BoardPanel panel(Game game) {
        BoardPanel panel = new BoardPanel(game);
        panel.setSize(700, 700);
        return panel;
    }

    private static MouseEvent released(BoardPanel panel, int x, int y) {
        return new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, 0L, 0, x, y, 1, false, MouseEvent.BUTTON1);
    }

    @Test
    void ogniIncrocioSiConvertePixelEDiNuovoInIncrocio() {
        BoardPanel panel = panel(new Game(13));
        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 13; c++) {
                int[] px = panel.pixelOf(r, c);
                assertEquals(new Point(r, c), panel.pointAt(px[0], px[1]));
            }
        }
    }

    @Test
    void unClicVicinoAdUnIncrocioLoSeleziona() {
        BoardPanel panel = panel(new Game(9));
        int[] px = panel.pixelOf(4, 5);
        assertEquals(new Point(4, 5), panel.pointAt(px[0] + 8, px[1] - 8));
    }

    @Test
    void unClicFuoriDallaScacchieraNonSelezionaNulla() {
        BoardPanel panel = panel(new Game(9));
        assertNull(panel.pointAt(2, 2));
        assertNull(panel.pointAt(699, 350));
    }

    @Test
    void ilClicSinistroSuUnIncrocioAvvisaIlListenerSoloSeInterattiva() {
        BoardPanel panel = panel(new Game(9));
        List<Point> clicked = new ArrayList<>();
        panel.setPointListener(clicked::add);
        int[] px = panel.pixelOf(3, 3);

        panel.setInteractive(false);
        panel.processMouseEvent(released(panel, px[0], px[1]));
        assertTrue(clicked.isEmpty());

        panel.setInteractive(true);
        panel.processMouseEvent(released(panel, px[0], px[1]));
        assertEquals(List.of(new Point(3, 3)), clicked);
    }

    @Test
    void ilClicDestroNonGiocaNulla() {
        BoardPanel panel = panel(new Game(9));
        List<Point> clicked = new ArrayList<>();
        panel.setPointListener(clicked::add);
        panel.setInteractive(true);
        int[] px = panel.pixelOf(3, 3);
        panel.processMouseEvent(new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, 0L, 0,
                px[0], px[1], 1, true, MouseEvent.BUTTON3));
        assertTrue(clicked.isEmpty());
    }

    @Test
    void ilDisegnoSuUnImmagineNonLanciaEccezioniEColoraLePietre() {
        Game game = new Game(5);
        game.play(2, 2);   // nero
        game.play(0, 0);   // bianco
        BoardPanel panel = panel(game);
        BufferedImage image = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);
        g2.dispose();

        int[] black = panel.pixelOf(2, 2);
        int[] white = panel.pixelOf(0, 0);
        Color blackPixel = new Color(image.getRGB(black[0], black[1]));
        Color whitePixel = new Color(image.getRGB(white[0], white[1]));
        assertTrue(blackPixel.getRed() < 90, "la pietra nera deve essere scura: " + blackPixel);
        assertTrue(whitePixel.getRed() > 170, "la pietra bianca deve essere chiara: " + whitePixel);
    }
}
