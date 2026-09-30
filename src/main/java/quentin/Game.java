package quentin;

import java.util.List;
import java.util.Optional;

/**
 * Stato di una partita di Quentin: turno, pie rule, vincitore.
 * Nessuna dipendenza dalla grafica: si può testare e far giocare a un'AI.
 *
 * <p>Pie rule: al PRIMO turno del Bianco, invece di muovere, il Bianco può scambiare i lati.
 * La board non cambia e il turno resta al colore Bianco, ma ora a muovere col Bianco
 * è l'altro giocatore (quello che aveva aperto col Nero).
 */
public final class Game {

    private Board board;
    private Stone current = Stone.BLACK;
    private boolean pieAvailable = true;
    private boolean swapped;
    private Stone winner;
    private Point lastMove;

    public Game(int size) {
        this.board = new Board(size);
    }

    private Game(Game other) {
        this.board = other.board.copy();
        this.current = other.current;
        this.pieAvailable = other.pieAvailable;
        this.swapped = other.swapped;
        this.winner = other.winner;
        this.lastMove = other.lastMove;
    }

    /** Crea una partita da una posizione data (per test e debug). La pie rule non è disponibile. */
    public static Game fromPosition(Board board, Stone toMove) {
        Game game = new Game(board.size());
        game.board = board.copy();
        game.current = toMove;
        game.pieAvailable = false;
        game.winner = WinChecker.winner(game.board, toMove.opponent());
        return game;
    }

    public Game copy() {
        return new Game(this);
    }

    public int size() {
        return board.size();
    }

    /** Copia della board (modificarla non altera la partita). */
    public Board board() {
        return board.copy();
    }

    public Stone stoneAt(int row, int col) {
        return board.get(row, col);
    }

    public Stone current() {
        return current;
    }

    public boolean isOver() {
        return winner != null;
    }

    public Stone winner() {
        return winner;
    }

    /** L'ultima pietra piazzata, oppure null se l'ultimo turno è stato un passo. */
    public Point lastMove() {
        return lastMove;
    }

    public boolean isSwapped() {
        return swapped;
    }

    /** Quale dei due giocatori (0 = quello che ha aperto, 1 = l'altro) controlla il colore dato. */
    public int playerIndex(Stone color) {
        return (color == Stone.BLACK) != swapped ? 0 : 1;
    }

    public MoveResult play(int row, int col) {
        if (isOver()) {
            return MoveResult.GAME_OVER;
        }
        if (!board.inBounds(row, col)) {
            return MoveResult.OUT_OF_BOUNDS;
        }
        if (!board.isEmpty(row, col)) {
            return MoveResult.OCCUPIED;
        }
        Optional<Board> next = Rules.play(board, row, col, current);
        if (next.isEmpty()) {
            return MoveResult.ILLEGAL;
        }
        board = next.get();
        lastMove = new Point(row, col);
        endTurn();
        return MoveResult.OK;
    }

    public boolean hasLegalMove() {
        return !isOver() && Rules.hasLegalMove(board, current);
    }

    public List<Point> legalMoves() {
        return isOver() ? List.of() : Rules.legalMoves(board, current);
    }

    /** Si può passare SOLO se non c'è nessuna mossa legale. */
    public boolean pass() {
        if (isOver() || hasLegalMove()) {
            return false;
        }
        lastMove = null;
        endTurn();
        return true;
    }

    public boolean canSwap() {
        return !isOver() && pieAvailable && current == Stone.WHITE;
    }

    public boolean swapSides() {
        if (!canSwap()) {
            return false;
        }
        swapped = !swapped;
        pieAvailable = false;
        return true;
    }

    public MoveResult apply(Move move) {
        if (isOver()) {
            return MoveResult.GAME_OVER;
        }
        return switch (move.type()) {
            case PLACE -> play(move.point().row(), move.point().col());
            case PASS -> pass() ? MoveResult.OK : MoveResult.ILLEGAL;
            case SWAP -> swapSides() ? MoveResult.OK : MoveResult.ILLEGAL;
        };
    }

    private void endTurn() {
        Stone mover = current;
        winner = WinChecker.winner(board, mover);
        if (mover == Stone.WHITE) {
            pieAvailable = false; // il primo turno del Bianco è finito
        }
        if (winner == null) {
            current = mover.opponent();
        }
    }
}
