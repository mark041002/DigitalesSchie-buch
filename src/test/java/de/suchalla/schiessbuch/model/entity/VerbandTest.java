package de.suchalla.schiessbuch.model.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Verband-Entity mit DTO-Mapping.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class VerbandTest {

    private Verband verband;

    @BeforeEach
    void setUp() {
        verband = Verband.builder()
                .id(1L)
                .name("Deutscher Schützenbund")
                .beschreibung("Dachverband der deutschen Schützenvereine")
                .build();
    }

    @Test
    void testVerbandBuilder() {
        assertNotNull(verband);
        assertEquals(1L, verband.getId());
        assertEquals("Deutscher Schützenbund", verband.getName());
        assertEquals("Dachverband der deutschen Schützenvereine", verband.getBeschreibung());
    }

    @Test
    void testSetterMethods() {
        verband.setName("Bund Deutscher Sportschützen");
        verband.setBeschreibung("Neue Beschreibung");

        assertEquals("Bund Deutscher Sportschützen", verband.getName());
        assertEquals("Neue Beschreibung", verband.getBeschreibung());
    }

    @Test
    void testEqualsAndHashCode() {
        Verband verband1 = Verband.builder()
                .id(1L)
                .name("DSB")
                .build();

        Verband verband2 = Verband.builder()
                .id(1L)
                .name("DSB")
                .build();

        Verband verband3 = Verband.builder()
                .id(2L)
                .name("BDS")
                .build();

        // Gleiche ID und Name -> gleich
        assertEquals(verband1, verband2);
        assertEquals(verband1.hashCode(), verband2.hashCode());

        // Verschiedene ID -> ungleich
        assertNotEquals(verband1, verband3);
    }

    @Test
    void testPrePersistSetsTimestamps() {
        Verband neuerVerband = new Verband();
        assertNull(neuerVerband.getErstelltAm());
        assertNull(neuerVerband.getAktualisiertAm());

        neuerVerband.onCreate();

        assertNotNull(neuerVerband.getErstelltAm());
        assertNotNull(neuerVerband.getAktualisiertAm());
    }

    @Test
    void testPreUpdateSetsAktualisiertAm() throws InterruptedException {
        verband.onCreate();
        var erstelltAm = verband.getErstelltAm();

        Thread.sleep(10);

        verband.onUpdate();

        assertEquals(erstelltAm, verband.getErstelltAm());
        assertNotNull(verband.getAktualisiertAm());
    }

    @Test
    void testVereineRelationship() {
        Verein verein1 = Verein.builder().id(1L).name("Verein 1").build();
        Verein verein2 = Verein.builder().id(2L).name("Verein 2").build();

        verband.getVereine().add(verein1);
        verband.getVereine().add(verein2);

        assertEquals(2, verband.getVereine().size());
    }
}

