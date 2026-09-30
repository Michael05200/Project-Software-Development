package quentin;

/** Un'azione possibile in un turno: piazzare una pietra, passare o (pie rule) cambiare lato. */
public record Move(Type type, Point point) {

    public enum Type {
        PLACE, PASS, SWAP
    }

    public static Move place(int row, int col) {
        return new Move(Type.PLACE, new Point(row, col));
    }

    public static Move pass() {
        return new Move(Type.PASS, null);
    }

    public static Move swap() {
        return new Move(Type.SWAP, null);
    }
}
