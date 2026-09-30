package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class RulesTest {

    @Test
    void unaMossaSuUnPuntoOccupatoNonEPossibile() {
        Board board = Board.parse("B.", "..");
        assertTrue(Rules.play(board, 0, 0, Stone.WHITE).isEmpty());
    }

    @Test
    void unaMossaFuoriDallaBoardNonEPossibile() {
        assertTrue(Rules.play(new Board(3), 3, 0, Stone.BLACK).isEmpty());
    }

    @Test
    void unaMossaSemplicePiazzaSoloLaPietra() {
        Optional<Board> next = Rules.play(new Board(5), 2, 2, Stone.BLACK);
        assertEquals(Board.parse(
                ".....",
                ".....",
                "..B..",
                ".....",
                "....."), next.orElseThrow());
    }

    @Test
    void laBoardOriginaleNonVieneModificata() {
        Board board = new Board(3);
        Rules.play(board, 1, 1, Stone.BLACK);
        assertTrue(board.isEmpty(1, 1));
    }

    @Test
    void iTerritoriVengonoRiempitiPerMaggioranzaEInParitaConIlColoreDelloAvversario() {
        Board before = Board.parse(
                ".B.",
                "W.B",
                "...");
        // dopo Nero (2,1): centro 3N-1B -> N; (0,2) e (2,2) 2N -> N; (0,0) e (2,0) pari -> Bianco
        Board after = Rules.play(before, 2, 1, Stone.BLACK).orElseThrow();
        assertEquals(Board.parse(
                "WBB",
                "WBB",
                "WBB"), after);
    }

    @Test
    void esempioDelRiempimentoInParitaCondiviso() {
        Board before = Board.parse(
                ".B.",
                "B.W",
                "...");
        Board after = Rules.play(before, 2, 1, Stone.BLACK).orElseThrow();
        assertEquals(Board.parse(
                "BBW",
                "BBW",
                "BBW"), after);
    }

    @Test
    void duePietreInDiagonaleSenzaVicinoComuneRendonoLaMossaIllegale() {
        Board board = Board.parse(
                "....",
                "..W.",
                "....",
                "....");
        // (2,1) è in diagonale con (1,2) ma né (1,1) né (2,2) sono bianchi
        assertTrue(Rules.play(board, 2, 1, Stone.WHITE).isEmpty());
    }

    @Test
    void duePietreInDiagonaleConUnVicinoComuneSonoLegali() {
        Board board = Board.parse(
                "....",
                "..W.",
                "..W.",
                "....");
        assertTrue(Rules.play(board, 2, 1, Stone.WHITE).isPresent());
    }

    @Test
    void duePietreInOrizzontaleSonoSempreLegali() {
        Board board = Board.parse(
                "....",
                "..W.",
                "....",
                "....");
        assertTrue(Rules.play(board, 1, 1, Stone.WHITE).isPresent());
    }

    @Test
    void unaMossaChePerColpaDelRiempimentoCreaUnaDiagonaleSenzaVicinoEIllegale() {
        // Bianco in (1,2): il punto (1,1) ha 2 pietre nere e 2 bianche -> pari -> riempito di NERO;
        // dopo il riempimento (1,2) e (2,1) sono bianche in diagonale senza vicino comune.
        Board board = Board.parse(
                "BB...",
                "B....",
                ".W...",
                ".....",
                ".....");
        assertTrue(Rules.play(board, 1, 2, Stone.WHITE).isEmpty());
    }

    @Test
    void unaMossaAdiacenteInVerticaleAUnaPietraDelloStessoColoreELegale() {
        Board board = Board.parse(
                "BB...",
                "B....",
                "..W..",
                ".....",
                ".....");
        Board after = Rules.play(board, 1, 2, Stone.WHITE).orElseThrow();
        assertEquals(Stone.WHITE, after.get(1, 2));
        assertEquals(Stone.WHITE, after.get(2, 2));
        assertTrue(after.isEmpty(1, 1)); // (1,1) non è territorio: la sua regione è tutta la board
    }

    @Test
    void suUnaBoardVuotaOgniPuntoEUnaMossaLegale() {
        assertEquals(9, Rules.legalMoves(new Board(3), Stone.BLACK).size());
    }

    @Test
    void suUnaBoardPienaNonCiSonoMosse() {
        Board full = Board.parse(
                "BWB",
                "WBW",
                "BWB");
        assertFalse(Rules.hasLegalMove(full, Stone.BLACK));
        assertTrue(Rules.legalMoves(full, Stone.WHITE).isEmpty());
    }
}
