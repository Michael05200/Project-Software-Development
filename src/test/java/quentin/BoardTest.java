package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void laBoardHaLaDimensioneRichiesta() {
        assertEquals(5, new Board(5).size());
    }

    @Test
    void unaBoardNuovaEVuota() {
        Board board = new Board(4);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                assertTrue(board.isEmpty(r, c));
                assertNull(board.get(r, c));
            }
        }
    }

    @Test
    void laDimensioneDeveEssereMaggioreDiZero() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0));
        assertThrows(IllegalArgumentException.class, () -> new Board(-3));
    }

    @Test
    void getRestituisceLaPietraPiazzataConSet() {
        Board board = new Board(5);
        board.set(1, 3, Stone.BLACK);
        assertEquals(Stone.BLACK, board.get(1, 3));
        assertFalse(board.isEmpty(1, 3));
        assertTrue(board.isEmpty(3, 1));
    }

    @Test
    void inBoundsDistingueIPuntiDentroDaiPuntiFuori() {
        Board board = new Board(5);
        assertTrue(board.inBounds(0, 0));
        assertTrue(board.inBounds(4, 4));
        assertFalse(board.inBounds(-1, 0));
        assertFalse(board.inBounds(0, 5));
        assertFalse(board.inBounds(5, 0));
    }

    @Test
    void accessoFuoriDaiLimitiLanciaEccezione() {
        Board board = new Board(5);
        assertThrows(IndexOutOfBoundsException.class, () -> board.get(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.get(0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> board.set(5, 0, Stone.WHITE));
    }

    @Test
    void parseLeggeIlDiagramma() {
        Board board = Board.parse(
                "B.W",
                "...",
                "W.B");
        assertEquals(Stone.BLACK, board.get(0, 0));
        assertEquals(Stone.WHITE, board.get(0, 2));
        assertEquals(Stone.WHITE, board.get(2, 0));
        assertEquals(Stone.BLACK, board.get(2, 2));
        assertTrue(board.isEmpty(1, 1));
    }

    @Test
    void parseRifiutaDiagrammiNonQuadratiOConCaratteriStrani() {
        assertThrows(IllegalArgumentException.class, () -> Board.parse("B.", "..."));
        assertThrows(IllegalArgumentException.class, () -> Board.parse("BX", ".."));
    }

    @Test
    void toStringEParseSonoInversi() {
        Board board = Board.parse("B.W", ".B.", "W..");
        assertEquals(board, Board.parse(board.toString().split("\n")));
    }

    @Test
    void laCopiaEIndipendenteDallOriginale() {
        Board original = Board.parse("B.", "..");
        Board copy = original.copy();
        copy.set(1, 1, Stone.WHITE);
        assertTrue(original.isEmpty(1, 1));
        assertEquals(Stone.WHITE, copy.get(1, 1));
        assertFalse(original.equals(copy));
    }

    @Test
    void ilVicinatoOrtogonaleDiUnAngoloHaDuePunti() {
        List<Point> neighbors = new Board(4).orthogonalNeighbors(0, 0);
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(new Point(0, 1)));
        assertTrue(neighbors.contains(new Point(1, 0)));
    }

    @Test
    void ilVicinatoOrtogonaleDiUnPuntoCentraleHaQuattroPunti() {
        assertEquals(4, new Board(5).orthogonalNeighbors(2, 2).size());
    }

    @Test
    void ilVicinatoDiagonaleDiUnAngoloHaUnPuntoEQuelloCentraleQuattro() {
        Board board = new Board(5);
        assertEquals(List.of(new Point(1, 1)), board.diagonalNeighbors(0, 0));
        assertEquals(4, board.diagonalNeighbors(2, 2).size());
    }
}
