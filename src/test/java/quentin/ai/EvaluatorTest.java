package quentin.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import quentin.Board;
import quentin.Stone;

class EvaluatorTest {

    @Test
    void suUnaBoardVuotaServonoNPuntiPerAttraversareUnaBoardNPerN() {
        Board board = new Board(5);
        assertEquals(5, Evaluator.distance(board, Stone.BLACK));
        assertEquals(5, Evaluator.distance(board, Stone.WHITE));
    }

    @Test
    void lePietreGiaPiazzateAbbassanoLaDistanza() {
        Board board = Board.parse(
                "..B..",
                "..B..",
                "..B..",
                ".....",
                ".....");
        assertEquals(2, Evaluator.distance(board, Stone.BLACK));
        assertEquals(5, Evaluator.distance(board, Stone.WHITE));
    }

    @Test
    void unaCatenaCompletaHaDistanzaZero() {
        Board board = Board.parse(
                "...",
                "WWW",
                "...");
        assertEquals(0, Evaluator.distance(board, Stone.WHITE));
    }

    @Test
    void unMuroAvversarioCompletoBloccaIlPercorso() {
        Board board = Board.parse(
                ".....",
                ".....",
                "WWWWW",
                ".....",
                ".....");
        assertEquals(Evaluator.INFINITE, Evaluator.distance(board, Stone.BLACK));
    }

    @Test
    void ilPunteggioPremiaChiEPiuVicinoAlCollegamento() {
        Board board = Board.parse(
                "..B..",
                "..B..",
                "..B..",
                ".....",
                ".....");
        assertTrue(Evaluator.score(board, Stone.BLACK, Stone.BLACK) > 0);
        assertTrue(Evaluator.score(board, Stone.WHITE, Stone.BLACK) < 0);
    }

    @Test
    void ilPunteggioDiUnaPosizioneVintaEIlMassimo() {
        Board board = Board.parse(
                "B..",
                "B..",
                "B..");
        assertEquals(Evaluator.WIN, Evaluator.score(board, Stone.BLACK, Stone.BLACK));
        assertEquals(-Evaluator.WIN, Evaluator.score(board, Stone.WHITE, Stone.BLACK));
    }
}
