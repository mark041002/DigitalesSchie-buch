package de.suchalla.schiessbuch.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Disziplin-Entity mit DTO-Mapping.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class DisziplinTest {

    private Disziplin disziplin;

    @BeforeEach
    void setUp() {
        disziplin = Disziplin.builder()
            .id(1L)
            .kennziffer("KK-50m")
            .programm("Kleinkaliber 50m - Präzisionsprogramm")
            .build();
    }

    @Test
    void testDisziplinBuilder() {
        assertNotNull(disziplin);
        assertEquals(1L, disziplin.getId());
        assertEquals("KK-50m", disziplin.getKennziffer());
        assertEquals("Kleinkaliber 50m - Präzisionsprogramm", disziplin.getProgramm());
    }

    @Test
    void testSetterMethods() {
        disziplin.setKennziffer("LG-10m");
        disziplin.setProgramm("Sportschießen mit Luftgewehr");

        assertEquals("LG-10m", disziplin.getKennziffer());
        assertEquals("Sportschießen mit Luftgewehr", disziplin.getProgramm());
    }

    @Test
    void testEqualsAndHashCode() {
        Disziplin disziplin1 = Disziplin.builder()
            .id(1L)
            .kennziffer("D-1")
            .build();

        Disziplin disziplin2 = Disziplin.builder()
            .id(1L)
            .kennziffer("D-1")
            .build();

        Disziplin disziplin3 = Disziplin.builder()
            .id(2L)
            .kennziffer("D-1")
            .build();

        assertEquals(disziplin1, disziplin2);
        assertEquals(disziplin1.hashCode(), disziplin2.hashCode());
        assertNotEquals(disziplin1, disziplin3);
    }

    @Test
    void testPrePersistSetsTimestamps() {
        Disziplin neueDisziplin = new Disziplin();
        assertNull(neueDisziplin.getErstelltAm());
        assertNull(neueDisziplin.getAktualisiertAm());

        neueDisziplin.onCreate();

        assertNotNull(neueDisziplin.getErstelltAm());
        assertNotNull(neueDisziplin.getAktualisiertAm());
    }

    @Test
    void testPreUpdateSetsAktualisiertAm() throws InterruptedException {
        disziplin.onCreate();
        var erstelltAm = disziplin.getErstelltAm();

        Thread.sleep(10);

        disziplin.onUpdate();

        assertEquals(erstelltAm, disziplin.getErstelltAm());
        assertNotNull(disziplin.getAktualisiertAm());
    }

    @Test
    void testVerbandRelationship() {
        Verband verband = Verband.builder()
                .id(1L)
                .name("DSB")
                .build();

        disziplin.setVerband(verband);

        assertNotNull(disziplin.getVerband());
        assertEquals("DSB", disziplin.getVerband().getName());
    }
}

