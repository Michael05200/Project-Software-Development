package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void laPartitaIniziaConIlNero() {
        Game game = new Game(5);
        assertEquals(Stone.BLACK, game.current());
        assertFalse(game.isOver());
        assertNull(game.winner());
    }

    @Test
    void iTurniAlternano() {
        Game game = new Game(5);
        assertEquals(MoveResult.OK, game.play(2, 2));
        assertEquals(Stone.WHITE, game.current());
        assertEquals(MoveResult.OK, game.play(0, 0));
        assertEquals(Stone.BLACK, game.current());
    }

    @Test
    void laMossaPiazzaLaPietraDelGiocatoreDiTurno() {
        Game game = new Game(5);
        game.play(2, 3);
        assertEquals(Stone.BLACK, game.stoneAt(2, 3));
        assertEquals(new Point(2, 3), game.lastMove());
    }

    @Test
    void nonSiGiocaSuUnPuntoOccupatoOFuoriDallaBoard() {
        Game game = new Game(5);
        game.play(2, 2);
        assertEquals(MoveResult.OCCUPIED, game.play(2, 2));
        assertEquals(MoveResult.OUT_OF_BOUNDS, game.play(5, 0));
        assertEquals(Stone.WHITE, game.current()); // i tentativi falliti non consumano il turno
    }

    @Test
    void unaMossaIllegaleNonCambiaNienteENonConsumaIlTurno() {
        Board board = Board.parse(
                "....",
                "..W.",
                "....",
                "....");
        Game game = Game.fromPosition(board, Stone.WHITE);
        assertEquals(MoveResult.ILLEGAL, game.play(2, 1));
        assertEquals(Stone.WHITE, game.current());
        assertTrue(game.board().isEmpty(2, 1));
    }

    @Test
    void laBoardRestituitaEUnaCopia() {
        Game game = new Game(3);
        game.board().set(1, 1, Stone.BLACK);
        assertTrue(game.board().isEmpty(1, 1));
    }

    @Test
    void nonSiPuoPassareSeCiSonoMosseLegali() {
        Game game = new Game(5);
        assertFalse(game.pass());
        assertEquals(Stone.BLACK, game.current());
    }

    @Test
    void siDevePassareQuandoNonCiSonoMosseELoSiPuoFare() {
        Board full = Board.parse(
                "BWB",
                "WBW",
                "BWB");
        Game game = Game.fromPosition(full, Stone.BLACK);
        assertFalse(game.hasLegalMove());
        assertTrue(game.pass());
        assertEquals(Stone.WHITE, game.current());
        assertNull(game.lastMove());
    }

    @Test
    void ilNeroVinceQuandoCompletaLaCatenaEIlRiempimentoSiApplica() {
        Board before = Board.parse(
                ".B.",
                "B.W",
                "...");
        Game game = Game.fromPosition(before, Stone.BLACK);
        assertEquals(MoveResult.OK, game.play(2, 1));
        assertEquals(Board.parse(
                "BBW",
                "BBW",
                "BBW"), game.board());
        assertTrue(game.isOver());
        assertEquals(Stone.BLACK, game.winner());
    }

    @Test
    void aPartitaFinitaNonSiPuoPiuGiocare() {
        Board before = Board.parse(
                "..B..",
                "..B..",
                "..B..",
                "..B..",
                ".....");
        Game game = Game.fromPosition(before, Stone.BLACK);
        assertEquals(MoveResult.OK, game.play(4, 2));
        assertEquals(Stone.BLACK, game.winner());
        assertEquals(MoveResult.GAME_OVER, game.play(0, 0));
        assertFalse(game.pass());
        assertTrue(game.legalMoves().isEmpty());
    }

    @Test
    void ilBiancoVinceConUnaCatenaSinistraDestra() {
        Board before = Board.parse(
                ".....",
                ".....",
                "WWWW.",
                ".....",
                ".....");
        Game game = Game.fromPosition(before, Stone.WHITE);
        assertEquals(MoveResult.OK, game.play(2, 4));
        assertEquals(Stone.WHITE, game.winner());
    }

    @Test
    void laPieRuleNonEDisponibileAllInizioNeAlTurnoDelNero() {
        Game game = new Game(5);
        assertFalse(game.canSwap());
        assertFalse(game.swapSides());
    }

    @Test
    void ilBiancoPuoScambiareILatiAlSuoPrimoTurno() {
        Game game = new Game(5);
        game.play(2, 2);
        assertTrue(game.canSwap());
        assertEquals(0, game.playerIndex(Stone.BLACK));
        assertEquals(1, game.playerIndex(Stone.WHITE));

        assertTrue(game.swapSides());

        assertTrue(game.isSwapped());
        assertEquals(Stone.WHITE, game.current()); // il turno resta al Bianco...
        assertEquals(1, game.playerIndex(Stone.BLACK)); // ...ma i giocatori si sono scambiati
        assertEquals(0, game.playerIndex(Stone.WHITE));
        assertEquals(Stone.BLACK, game.stoneAt(2, 2)); // la board non cambia
        assertFalse(game.canSwap()); // si può scambiare una volta sola
    }

    @Test
    void dopoLaPrimaMossaDelBiancoLaPieRuleNonEPiuDisponibile() {
        Game game = new Game(5);
        game.play(2, 2);
        game.play(0, 0);
        game.play(4, 4);
        assertFalse(game.canSwap());
        game.play(0, 4);
        assertFalse(game.canSwap());
    }

    @Test
    void applyEseguePiazzamentoPassoESwap() {
        Game game = new Game(5);
        assertEquals(MoveResult.OK, game.apply(Move.place(2, 2)));
        assertEquals(MoveResult.OK, game.apply(Move.swap()));
        assertEquals(MoveResult.ILLEGAL, game.apply(Move.pass())); // ci sono mosse legali
    }

    @Test
    void laCopiaDellaPartitaEIndipendente() {
        Game game = new Game(5);
        Game copy = game.copy();
        copy.play(1, 1);
        assertTrue(game.board().isEmpty(1, 1));
        assertEquals(Stone.BLACK, game.current());
        assertEquals(Stone.WHITE, copy.current());
    }
}
