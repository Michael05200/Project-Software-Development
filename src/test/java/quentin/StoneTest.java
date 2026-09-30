package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StoneTest {

    @Test
    void ilNeroHaPerAvversarioIlBianco() {
        assertEquals(Stone.WHITE, Stone.BLACK.opponent());
    }

    @Test
    void ilBiancoHaPerAvversarioIlNero() {
        assertEquals(Stone.BLACK, Stone.WHITE.opponent());
    }
}
