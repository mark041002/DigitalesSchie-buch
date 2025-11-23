package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.mapper.SchiesstandMapper;
import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchiesstandTest {

    private Schiesstand schiesstand;
    private Verein verein;
    private SchiesstandMapper mapper;

    @BeforeEach
    void setUp() {
        verein = TestDataFactory.createVerein(1L, "Verein A");
        schiesstand = Schiesstand.builder()
                .id(5L)
                .name("Stand A")
                .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                .verein(verein)
                .adresse("Adresse")
                .beschreibung("Beschreibung")
                .build();
        mapper = new SchiesstandMapper();
    }

    @Test
    void testBuilder() {
        assertEquals(5L, schiesstand.getId());
        assertEquals("Stand A", schiesstand.getName());
        assertEquals(SchiesstandTyp.VEREINSGEBUNDEN, schiesstand.getTyp());
        assertEquals(verein, schiesstand.getVerein());
        assertNotNull(schiesstand.getEintraege());
        assertTrue(schiesstand.getEintraege().isEmpty());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        Schiesstand neu = Schiesstand.builder()
                .name("Neu")
                .typ(SchiesstandTyp.GEWERBLICH)
                .build();
        assertNull(neu.getErstelltAm());
        assertNull(neu.getAktualisiertAm());
        neu.onCreate();
        assertNotNull(neu.getErstelltAm());
        assertNotNull(neu.getAktualisiertAm());
    }

    @Test
    void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        schiesstand.onCreate();
        var created = schiesstand.getErstelltAm();
        Thread.sleep(5);
        schiesstand.onUpdate();
        assertEquals(created, schiesstand.getErstelltAm());
        assertTrue(schiesstand.getAktualisiertAm().isAfter(created) || schiesstand.getAktualisiertAm().isEqual(created));
    }

    @Test
    void testEqualsHashCode() {
        Schiesstand s1 = Schiesstand.builder().id(1L).name("A").typ(SchiesstandTyp.VEREINSGEBUNDEN).build();
        Schiesstand s2 = Schiesstand.builder().id(1L).name("B").typ(SchiesstandTyp.GEWERBLICH).build();
        Schiesstand s3 = Schiesstand.builder().id(2L).name("A").typ(SchiesstandTyp.VEREINSGEBUNDEN).build();
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    @Test
    void testEntityToDtoMapping() {
        SchiesstandDTO dto = mapper.toDTO(schiesstand);

        assertNotNull(dto);
        assertEquals(schiesstand.getId(), dto.getId());
        assertEquals(schiesstand.getName(), dto.getName());
        assertEquals(schiesstand.getTyp(), dto.getTyp());
        assertEquals(schiesstand.getAdresse(), dto.getAdresse());
        assertEquals(schiesstand.getBeschreibung(), dto.getBeschreibung());
        assertEquals(verein.getId(), dto.getVereinId());
        assertEquals(verein.getName(), dto.getVereinName());
    }

    @Test
    void testEntityToDtoMappingNull() {
        SchiesstandDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testEntityToDtoWithAufseher() {
        Benutzer aufseher = TestDataFactory.createBenutzer(2L, "aufseher@example.com");
        schiesstand.setAufseher(aufseher);

        SchiesstandDTO dto = mapper.toDTO(schiesstand);

        assertNotNull(dto);
        assertEquals(aufseher.getId(), dto.getAufseherId());
        assertEquals("Max Mustermann", dto.getAufseherVollstaendigerName());
    }
}

