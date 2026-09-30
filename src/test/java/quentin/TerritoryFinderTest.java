package quentin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TerritoryFinderTest {

    @Test
    void unaBoardVuotaHaUnaSolaRegioneENessunTerritorio() {
        Board board = new Board(4);
        assertEquals(1, TerritoryFinder.regions(board).size());
        assertTrue(TerritoryFinder.territories(board).isEmpty());
    }

    @Test
    void leRegioniSonoGruppiDiPuntiVuotiOrtogonalmenteAdiacenti() {
        Board board = Board.parse(
                "..B",
                ".B.",
                "B..");
        List<Set<Point>> regions = TerritoryFinder.regions(board);
        assertEquals(2, regions.size());
        assertEquals(3, regions.get(0).size());
        assertEquals(3, regions.get(1).size());
    }

    @Test
    void unaRegioneConPuntiAdiacentiAMenoDiDuePietreNonEUnTerritorio() {
        Board board = Board.parse(
                "..B",
                ".B.",
                "B..");
        assertTrue(TerritoryFinder.territories(board).isEmpty());
    }

    @Test
    void unPuntoCircondatoDaQuattroPietreEUnTerritorio() {
        Board board = Board.parse(
                "BWB",
                "W.W",
                "BWB");
        List<Territory> territories = TerritoryFinder.territories(board);
        assertEquals(1, territories.size());
        Territory t = territories.get(0);
        assertEquals(List.of(new Point(1, 1)), t.points());
        assertEquals(0, t.blackStones());
        assertEquals(4, t.whiteStones());
    }

    @Test
    void unAngoloConDuePietreAdiacentiEUnTerritorio() {
        Board board = Board.parse(
                ".B.",
                "B.B",
                ".B.");
        // ogni angolo ha esattamente due pietre nere adiacenti, e il centro ne ha quattro
        assertEquals(5, TerritoryFinder.territories(board).size());
    }

    @Test
    void unAngoloConUnaSolaPietraAdiacenteNonEUnTerritorio() {
        Board board = Board.parse(
                ".B.",
                "...",
                "...");
        assertTrue(TerritoryFinder.territories(board).isEmpty());
    }

    @Test
    void ogniPietraAdiacenteAlTerritorioVieneContataUnaSolaVolta() {
        // la pietra bianca in (2,1) tocca due punti della regione ma conta 1
        Board board = Board.parse(
                "BBBB",
                "B..B",
                "WW.B",
                "WWBB");
        List<Territory> territories = TerritoryFinder.territories(board);
        assertEquals(1, territories.size());
        Territory t = territories.get(0);
        assertEquals(3, t.points().size());
        assertEquals(6, t.blackStones());
        assertEquals(1, t.whiteStones());
        assertEquals(Stone.BLACK, t.fillColor(Stone.WHITE));
    }

    @Test
    void inCasoDiParitaSiUsaIlColoreDelloAvversarioDiChiHaMosso() {
        Board board = Board.parse(
                "BBW",
                "W.W",
                "WBB");
        // il punto centrale ha 2 pietre nere e 2 bianche adiacenti
        Territory t = TerritoryFinder.territories(board).get(0);
        assertEquals(2, t.blackStones());
        assertEquals(2, t.whiteStones());
        assertEquals(Stone.WHITE, t.fillColor(Stone.BLACK));
        assertEquals(Stone.BLACK, t.fillColor(Stone.WHITE));
    }
}
