package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.entity.Verein;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Verein-Entity mit DTO-Mapping.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class VereinTest {

    private Verein verein;

    @BeforeEach
    void setUp() {
        verein = Verein.builder()
                .id(1L)
                .name("Schützenverein Teststadt")
                .adresse("Schützenstraße 1, 12345 Teststadt")
                .beschreibung("Traditionsreicher Schützenverein seit 1900")
                .build();
        mapper = new VereinMapper();
    }

    @Test
    void testVereinBuilder() {
        assertNotNull(verein);
        assertEquals(1L, verein.getId());
        assertEquals("Schützenverein Teststadt", verein.getName());
        assertEquals("Schützenstraße 1, 12345 Teststadt", verein.getAdresse());
        assertEquals("Traditionsreicher Schützenverein seit 1900", verein.getBeschreibung());
    }

    @Test
    void testSetterMethods() {
        verein.setName("Neuer Verein");
        verein.setAdresse("Neue Straße 1");

        assertEquals("Neuer Verein", verein.getName());
        assertEquals("Neue Straße 1", verein.getAdresse());
    }

    @Test
    void testEqualsAndHashCode() {
        Verein verein1 = Verein.builder()
                .id(1L)
                .name("Verein 1")
                .build();

        Verein verein2 = Verein.builder()
                .id(1L)
                .name("Verein 2")
                .build();

        Verein verein3 = Verein.builder()
                .id(2L)
                .name("Verein 1")
                .build();

        assertEquals(verein1, verein2);
        assertEquals(verein1.hashCode(), verein2.hashCode());
        assertNotEquals(verein1, verein3);
    }

    @Test
    void testToString() {
        String toString = verein.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Schützenverein Teststadt"));
        assertTrue(toString.contains("SV-12345"));
    }

    @Test
    void testPrePersistSetsTimestamps() {
        Verein neuerVerein = new Verein();
        assertNull(neuerVerein.getErstelltAm());
        assertNull(neuerVerein.getAktualisiertAm());

        neuerVerein.onCreate();

        assertNotNull(neuerVerein.getErstelltAm());
        assertNotNull(neuerVerein.getAktualisiertAm());
    }

    @Test
    void testPreUpdateSetsAktualisiertAm() throws InterruptedException {
        verein.onCreate();
        var erstelltAm = verein.getErstelltAm();

        Thread.sleep(10);

        verein.onUpdate();
        assertEquals(erstelltAm, verein.getErstelltAm());
        assertNotNull(verein.getAktualisiertAm());
    }

    @Test
    void testEntityToDtoMapping() {
        Verein dto = mapper.toDTO(verein);

        assertNotNull(dto);
        assertEquals(verein.getId(), dto.getId());
        assertEquals(verein.getName(), dto.getName());
        assertEquals(verein.getAdresse(), dto.getAdresse());
        assertEquals(verein.getBeschreibung(), dto.getBeschreibung());
    }

    @Test
    void testEntityToDtoMappingNull() {
        Verein dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testEntityToDtoWithVerbaende() {
        Verband verband = Verband.builder().id(1L).name("DSB").build();
        verein.getVerbaende().add(verband);

        Verein dto = mapper.toDTO(verein);

        assertNotNull(dto);
        assertEquals(1, dto.getVerbandIds().size());
        assertEquals(1L, dto.getVerbandIds().get(0));
        assertEquals("DSB", dto.getVerbandNamen().get(0));
    }

    @Test
    void testEntityToDtoMitgliederAnzahl() {
        // MitgliederAnzahl sollte 0 sein wenn keine Mitgliedschaften
        Verein dto = mapper.toDTO(verein);
        assertEquals(0, dto.getMitgliederAnzahl());
    }
}

