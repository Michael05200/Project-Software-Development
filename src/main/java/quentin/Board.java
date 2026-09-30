package quentin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scacchiera quadrata di N x N incroci; le pietre stanno sugli incroci.
 * Bordo alto e basso sono del Nero, bordo sinistro e destro del Bianco.
 * Un punto vuoto è rappresentato da {@code null}.
 */
public final class Board {

    private final int size;
    private final Stone[][] cells;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("La dimensione deve essere positiva: " + size);
        }
        this.size = size;
        this.cells = new Stone[size][size];
    }

    /**
     * Costruisce una board da un diagramma testuale: 'B' nero, 'W' bianco, '.' vuoto.
     * Gli spazi vengono ignorati. Molto comoda nei test.
     */
    public static Board parse(String... rows) {
        int n = rows.length;
        if (n == 0) {
            throw new IllegalArgumentException("Il diagramma è vuoto");
        }
        Board board = new Board(n);
        for (int r = 0; r < n; r++) {
            String row = rows[r].replace(" ", "");
            if (row.length() != n) {
                throw new IllegalArgumentException(
                        "La riga " + r + " deve avere " + n + " colonne: '" + rows[r] + "'");
            }
            for (int c = 0; c < n; c++) {
                switch (row.charAt(c)) {
                    case 'B' -> board.cells[r][c] = Stone.BLACK;
                    case 'W' -> board.cells[r][c] = Stone.WHITE;
                    case '.' -> board.cells[r][c] = null;
                    default -> throw new IllegalArgumentException(
                            "Carattere non valido '" + row.charAt(c) + "' nella riga " + r);
                }
            }
        }
        return board;
    }

    public Board copy() {
        Board copy = new Board(size);
        for (int r = 0; r < size; r++) {
            copy.cells[r] = cells[r].clone();
        }
        return copy;
    }

    public int size() {
        return size;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /** Restituisce la pietra nel punto, oppure null se il punto è vuoto. */
    public Stone get(int row, int col) {
        checkBounds(row, col);
        return cells[row][col];
    }

    public boolean isEmpty(int row, int col) {
        return get(row, col) == null;
    }

    public void set(int row, int col, Stone stone) {
        checkBounds(row, col);
        cells[row][col] = stone;
    }

    /** Vicini in alto, in basso, a sinistra e a destra (solo quelli dentro la board). */
    public List<Point> orthogonalNeighbors(int row, int col) {
        List<Point> result = new ArrayList<>(4);
        addIfInBounds(result, row - 1, col);
        addIfInBounds(result, row + 1, col);
        addIfInBounds(result, row, col - 1);
        addIfInBounds(result, row, col + 1);
        return result;
    }

    /** I quattro vicini in diagonale (solo quelli dentro la board). */
    public List<Point> diagonalNeighbors(int row, int col) {
        List<Point> result = new ArrayList<>(4);
        addIfInBounds(result, row - 1, col - 1);
        addIfInBounds(result, row - 1, col + 1);
        addIfInBounds(result, row + 1, col - 1);
        addIfInBounds(result, row + 1, col + 1);
        return result;
    }

    private void addIfInBounds(List<Point> list, int row, int col) {
        if (inBounds(row, col)) {
            list.add(new Point(row, col));
        }
    }

    private void checkBounds(int row, int col) {
        if (!inBounds(row, col)) {
            throw new IndexOutOfBoundsException(
                    "Punto fuori dalla board: (" + row + ", " + col + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Board other
                && size == other.size
                && Arrays.deepEquals(cells, other.cells);
    }

    @Override
    public int hashCode() {
        return 31 * size + Arrays.deepHashCode(cells);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Stone s = cells[r][c];
                sb.append(s == null ? '.' : s == Stone.BLACK ? 'B' : 'W');
            }
            if (r < size - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
