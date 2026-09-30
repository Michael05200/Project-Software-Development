package quentin.gui;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;
import quentin.Game;
import quentin.Point;
import quentin.Stone;
import quentin.WinChecker;

/**
 * Componente Swing custom che disegna la scacchiera: estende JComponent, ridefinisce
 * paintComponent (con una Graphics2D creata e poi rilasciata) e gestisce il mouse
 * abilitando gli eventi e ridefinendo processMouseEvent, come nelle slide.
 */
@SuppressWarnings("serial")
public class BoardPanel extends JComponent {

    private static final Color WALL = new Color(0x1B2A26);
    private static final Color BOARD_FACE = new Color(0x2F5D50);
    private static final Color GRID = new Color(0x9CBDB0);
    private static final Color BAR_OUTLINE = new Color(0xC9DDD3);
    private static final Color BLACK_BASE = new Color(0x14181B);
    private static final Color BLACK_SHEEN = new Color(0x56616A);
    private static final Color WHITE_BASE = new Color(0xD5D0C1);
    private static final Color WHITE_SHEEN = new Color(0xFFFFFF);
    private static final Color MARKER = new Color(0xE8B04A);

    private Game game;
    private Set<Point> winningChain = Collections.emptySet();
    private Consumer<Point> pointListener = p -> { };
    private boolean interactive;
    private Point hover;

    public BoardPanel(Game game) {
        this.game = game;
        setBackground(WALL);
        setOpaque(true);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public void setGame(Game game) {
        this.game = game;
        this.hover = null;
        refresh();
    }

    /** Da chiamare dopo ogni modifica della partita: ricalcola la catena vincente e ridisegna. */
    public void refresh() {
        winningChain = game.isOver()
                ? WinChecker.winningChain(game.board(), game.winner())
                : Collections.emptySet();
        repaint();
    }

    /** Se true la scacchiera mostra l'anteprima della pietra sotto il mouse. */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
        if (!interactive) {
            hover = null;
        }
        repaint();
    }

    public void setPointListener(Consumer<Point> listener) {
        this.pointListener = listener;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, 640);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(320, 320);
    }

    // ---------------------------------------------------------------- geometria

    private record Geometry(double boardX, double boardY, double side,
                            double left, double top, double step, double margin) {
        double x(int col) {
            return left + col * step;
        }

        double y(int row) {
            return top + row * step;
        }
    }

    private Geometry geometry() {
        double side = Math.min(getWidth(), getHeight());
        double margin = side * 0.085;
        int n = game.size();
        double step = (side - 2 * margin) / Math.max(1, n - 1);
        double boardX = (getWidth() - side) / 2.0;
        double boardY = (getHeight() - side) / 2.0;
        return new Geometry(boardX, boardY, side, boardX + margin, boardY + margin, step, margin);
    }

    /** Converte le coordinate del mouse nell'incrocio più vicino, oppure null se è fuori dalla scacchiera. */
    Point pointAt(int x, int y) {
        Geometry geo = geometry();
        int n = game.size();
        int col = (int) Math.round((x - geo.left()) / geo.step());
        int row = (int) Math.round((y - geo.top()) / geo.step());
        if (row < 0 || row >= n || col < 0 || col >= n) {
            return null;
        }
        return new Point(row, col);
    }

    /** Coordinate in pixel (x, y) di un incrocio: l'inverso di {@link #pointAt}. Usato nei test. */
    int[] pixelOf(int row, int col) {
        Geometry geo = geometry();
        return new int[] {(int) Math.round(geo.x(col)), (int) Math.round(geo.y(row))};
    }

    // ---------------------------------------------------------------- disegno

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            int n = game.size();
            Geometry geo = geometry();

            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(BOARD_FACE);
            g2.fill(new RoundRectangle2D.Double(geo.boardX(), geo.boardY(), geo.side(), geo.side(), 20, 20));

            drawGrid(g2, geo, n);
            drawEdgeBars(g2, geo, n);

            double radius = geo.step() * 0.44;
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    Stone stone = game.stoneAt(r, c);
                    if (stone != null) {
                        drawStone(g2, geo.x(c), geo.y(r), radius, stone);
                    }
                }
            }
            drawLastMove(g2, geo, radius);
            drawWinningChain(g2, geo, radius);
            drawHover(g2, geo, radius);
        } finally {
            g2.dispose();
        }
    }

    private void drawGrid(Graphics2D g2, Geometry geo, int n) {
        g2.setColor(GRID);
        g2.setStroke(new BasicStroke(1.2f));
        double last = (n - 1) * geo.step();
        for (int i = 0; i < n; i++) {
            g2.draw(new Line2D.Double(geo.left(), geo.top() + i * geo.step(), geo.left() + last, geo.top() + i * geo.step()));
            g2.draw(new Line2D.Double(geo.left() + i * geo.step(), geo.top(), geo.left() + i * geo.step(), geo.top() + last));
        }
    }

    /** Barre dei bordi: nere sopra e sotto (Nero), bianche a sinistra e a destra (Bianco). */
    private void drawEdgeBars(Graphics2D g2, Geometry geo, int n) {
        double thickness = geo.margin() * 0.2;
        double gap = geo.margin() * 0.55;
        double grid = (n - 1) * geo.step();
        double ext = Math.min(geo.step() * 0.5, gap * 0.9);

        fillBar(g2, geo.left() - ext, geo.top() - gap - thickness / 2, grid + 2 * ext, thickness, BLACK_BASE);
        fillBar(g2, geo.left() - ext, geo.top() + grid + gap - thickness / 2, grid + 2 * ext, thickness, BLACK_BASE);
        fillBar(g2, geo.left() - gap - thickness / 2, geo.top() - ext, thickness, grid + 2 * ext, WHITE_BASE);
        fillBar(g2, geo.left() + grid + gap - thickness / 2, geo.top() - ext, thickness, grid + 2 * ext, WHITE_BASE);
    }

    private void fillBar(Graphics2D g2, double x, double y, double w, double h, Color color) {
        RoundRectangle2D bar = new RoundRectangle2D.Double(x, y, w, h, Math.min(w, h), Math.min(w, h));
        g2.setColor(color);
        g2.fill(bar);
        g2.setColor(BAR_OUTLINE);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(bar);
    }

    private void drawStone(Graphics2D g2, double cx, double cy, double radius, Stone stone) {
        Color base = stone == Stone.BLACK ? BLACK_BASE : WHITE_BASE;
        Color sheen = stone == Stone.BLACK ? BLACK_SHEEN : WHITE_SHEEN;

        g2.setColor(new Color(0, 0, 0, 70)); // ombra leggera
        g2.fill(new Ellipse2D.Double(cx - radius + radius * 0.1, cy - radius + radius * 0.14, 2 * radius, 2 * radius));

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(cx - radius * 0.3, cy - radius * 0.3), (float) (radius * 1.5),
                new float[] {0f, 1f}, new Color[] {sheen, base}));
        g2.fill(new Ellipse2D.Double(cx - radius, cy - radius, 2 * radius, 2 * radius));

        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new Ellipse2D.Double(cx - radius, cy - radius, 2 * radius, 2 * radius));
    }

    private void drawLastMove(Graphics2D g2, Geometry geo, double radius) {
        Point last = game.lastMove();
        if (last == null) {
            return;
        }
        double r = radius * 0.42;
        g2.setColor(MARKER);
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(new Ellipse2D.Double(geo.x(last.col()) - r, geo.y(last.row()) - r, 2 * r, 2 * r));
    }

    private void drawWinningChain(Graphics2D g2, Geometry geo, double radius) {
        g2.setColor(MARKER);
        g2.setStroke(new BasicStroke(3.2f));
        double r = radius + 3.5;
        for (Point p : winningChain) {
            g2.draw(new Ellipse2D.Double(geo.x(p.col()) - r, geo.y(p.row()) - r, 2 * r, 2 * r));
        }
    }

    private void drawHover(Graphics2D g2, Geometry geo, double radius) {
        if (!interactive || hover == null || game.isOver()
                || game.stoneAt(hover.row(), hover.col()) != null) {
            return;
        }
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        drawStone(g2, geo.x(hover.col()), geo.y(hover.row()), radius, game.current());
        g2.setComposite(old);
    }

    // ---------------------------------------------------------------- mouse

    @Override
    protected void processMouseEvent(MouseEvent e) {
        switch (e.getID()) {
            case MouseEvent.MOUSE_RELEASED -> {
                if (interactive && e.getButton() == MouseEvent.BUTTON1) {
                    Point p = pointAt(e.getX(), e.getY());
                    if (p != null) {
                        pointListener.accept(p);
                    }
                }
            }
            case MouseEvent.MOUSE_EXITED -> {
                if (hover != null) {
                    hover = null;
                    repaint();
                }
            }
            default -> { }
        }
        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_MOVED && interactive) {
            Point p = pointAt(e.getX(), e.getY());
            if (p == null ? hover != null : !p.equals(hover)) {
                hover = p;
                repaint();
            }
        }
        super.processMouseMotionEvent(e);
    }
}
