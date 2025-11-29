package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchiesstandTest {

    private Schiesstand schiesstand;
    private Verein verein;

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
    }

    @Test
    void testBuilder() {
        assertEquals(5L, schiesstand.getId());
        assertEquals("Stand A", schiesstand.getName());
        assertEquals(SchiesstandTyp.VEREINSGEBUNDEN, schiesstand.getTyp());
        assertEquals(verein, schiesstand.getVerein());
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
}

