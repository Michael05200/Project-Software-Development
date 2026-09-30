package quentin.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;
import quentin.Board;
import quentin.Game;
import quentin.Move;
import quentin.MoveResult;
import quentin.Stone;

class StrategyTest {

    @Test
    void laStrategiaCasualeGiocaUnaMossaLegale() {
        Game game = new Game(5);
        Move move = new RandomStrategy(new Random(42)).choose(game);
        assertEquals(Move.Type.PLACE, move.type());
        assertEquals(MoveResult.OK, game.apply(move));
    }

    @Test
    void laStrategiaCasualePassaSeNonHaMosse() {
        Board full = Board.parse(
                "BWB",
                "WBW",
                "BWB");
        Game game = Game.fromPosition(full, Stone.BLACK);
        assertEquals(Move.pass(), new RandomStrategy(new Random(1)).choose(game));
    }

    @Test
    void minimaxCompletaLaCatenaSeCiPuoVincereSubito() {
        Board board = Board.parse(
                "..B..",
                "..B..",
                "..B..",
                "..B..",
                ".....");
        Game game = Game.fromPosition(board, Stone.BLACK);
        Move move = new MinimaxStrategy(2, 8, new Random(1)).choose(game);
        assertEquals(Move.place(4, 2), move);
    }

    @Test
    void minimaxBloccaLaVittoriaImmediataDellAvversario() {
        Board board = Board.parse(
                ".....",
                ".....",
                "WWWW.",
                ".....",
                ".....");
        Game game = Game.fromPosition(board, Stone.BLACK);
        Move move = new MinimaxStrategy(2, 8, new Random(1)).choose(game);
        assertEquals(Move.place(2, 4), move);
    }

    @Test
    void ancheLaVersioneGolosaBloccaLaVittoriaAvversaria() {
        Board board = Board.parse(
                ".....",
                ".....",
                "WWWW.",
                ".....",
                ".....");
        Game game = Game.fromPosition(board, Stone.BLACK);
        Move move = new MinimaxStrategy(1, 1, new Random(1)).choose(game);
        assertEquals(Move.place(2, 4), move);
    }

    @Test
    void minimaxPassaSeNonHaMosse() {
        Board full = Board.parse(
                "BWB",
                "WBW",
                "BWB");
        Game game = Game.fromPosition(full, Stone.WHITE);
        assertEquals(Move.pass(), new MinimaxStrategy(2, 4, new Random(1)).choose(game));
    }

    @Test
    void minimaxAprePartitaConUnaMossaLegaleNonCentrale() {
        Game game = new Game(13);
        Move move = new MinimaxStrategy(2, 6, new Random(3)).choose(game);
        assertEquals(Move.Type.PLACE, move.type());
        int distanceFromCenter = Math.max(Math.abs(move.point().row() - 6), Math.abs(move.point().col() - 6));
        assertTrue(distanceFromCenter > 13 / 4);
        assertEquals(MoveResult.OK, game.apply(move));
    }

    @Test
    void ilBiancoScambiaSeIlNeroHaApertoAlCentro() {
        Game game = new Game(13);
        game.play(6, 6);
        assertEquals(Move.swap(), new MinimaxStrategy(1, 1, new Random(1)).choose(game));
    }

    @Test
    void ilBiancoNonScambiaSeIlNeroHaApertoLontanoDalCentro() {
        Game game = new Game(13);
        game.play(1, 1);
        Move move = new MinimaxStrategy(1, 1, new Random(1)).choose(game);
        assertEquals(Move.Type.PLACE, move.type());
    }

    @Test
    void ilMinimaxProfondoGiocaSempreMosseLegaliInUnaPartitaCompleta() {
        Game game = new Game(7);
        Strategy black = new MinimaxStrategy(2, 6, new Random(5));
        Strategy white = new MinimaxStrategy(1, 1, new Random(6));
        int turns = 0;
        while (!game.isOver() && turns++ < 500) {
            Strategy s = game.playerIndex(game.current()) == 0 ? black : white;
            assertEquals(MoveResult.OK, game.apply(s.choose(game)));
        }
        assertTrue(game.isOver());
    }

    @Test
    void leGuidePartiteCasualiFinisconoSempreConUnVincitore() {
        for (int size : new int[] {3, 4, 5, 7}) {
            for (long seed = 0; seed < 25; seed++) {
                Game game = new Game(size);
                Strategy random = new RandomStrategy(new Random(seed));
                int turns = 0;
                while (!game.isOver() && turns++ < 2000) {
                    assertEquals(MoveResult.OK, game.apply(random.choose(game)),
                            "size=" + size + " seed=" + seed);
                }
                assertTrue(game.isOver(), "la partita non è finita: size=" + size + " seed=" + seed);
            }
        }
    }
}
