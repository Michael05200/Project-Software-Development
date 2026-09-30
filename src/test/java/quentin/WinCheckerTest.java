package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WinCheckerTest {

    @Test
    void ilNeroVinceCollegandoBordoAltoEBordoBasso() {
        Board board = Board.parse(
                "B..",
                "B..",
                "B..");
        assertTrue(WinChecker.hasWon(board, Stone.BLACK));
        assertFalse(WinChecker.hasWon(board, Stone.WHITE));
    }

    @Test
    void ilBiancoVinceCollegandoBordoSinistroEBordoDestro() {
        Board board = Board.parse(
                "...",
                "WWW",
                "...");
        assertTrue(WinChecker.hasWon(board, Stone.WHITE));
        assertFalse(WinChecker.hasWon(board, Stone.BLACK));
    }

    @Test
    void unaCatenaCheToccaUnSoloBordoNonVince() {
        Board board = Board.parse(
                "B..",
                "B..",
                "...");
        assertFalse(WinChecker.hasWon(board, Stone.BLACK));
    }

    @Test
    void lePietreInDiagonaleNonFormanoUnaCatena() {
        Board board = Board.parse(
                "B..",
                ".B.",
                "..B");
        assertFalse(WinChecker.hasWon(board, Stone.BLACK));
    }

    @Test
    void duePezziSeparatiNonFormanoUnaCatena() {
        Board board = Board.parse(
                "B..",
                "...",
                "B..");
        assertFalse(WinChecker.hasWon(board, Stone.BLACK));
    }

    @Test
    void laCatenaVincenteEUnPercorsoConAncheRamificazioni() {
        Board board = Board.parse(
                ".B..",
                ".BB.",
                "..B.",
                "..B.");
        assertEquals(5, WinChecker.winningChain(board, Stone.BLACK).size());
    }

    @Test
    void senzaVincitoreWinnerRestituisceNull() {
        assertNull(WinChecker.winner(new Board(4), Stone.BLACK));
    }

    @Test
    void winnerIndicaIlColoreVincente() {
        Board board = Board.parse(
                "...",
                "WWW",
                "...");
        assertEquals(Stone.WHITE, WinChecker.winner(board, Stone.WHITE));
        assertEquals(Stone.WHITE, WinChecker.winner(board, Stone.BLACK));
    }
}
