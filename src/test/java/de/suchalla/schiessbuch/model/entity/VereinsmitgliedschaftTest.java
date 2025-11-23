package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.mapper.VereinsmigliedschaftMapper;
import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class VereinsmitgliedschaftTest {

    private Vereinsmitgliedschaft mitgliedschaft;
    private Benutzer benutzer;
    private Verein verein;
    private VereinsmigliedschaftMapper mapper;

    @BeforeEach
    void setUp() {
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
        verein = TestDataFactory.createVerein(1L, "Verein A");
        mitgliedschaft = Vereinsmitgliedschaft.builder()
                .id(10L)
                .benutzer(benutzer)
                .verein(verein)
                .status(MitgliedschaftsStatus.BEANTRAGT)
                .aktiv(true)
                .build();
        mapper = new VereinsmigliedschaftMapper();
    }

    @Test
    void testBuilder() {
        assertEquals(10L, mitgliedschaft.getId());
        assertEquals(MitgliedschaftsStatus.BEANTRAGT, mitgliedschaft.getStatus());
        assertTrue(mitgliedschaft.getAktiv());
        assertFalse(mitgliedschaft.getIstAufseher());
        assertFalse(mitgliedschaft.getIstVereinschef());
    }

    @Test
    void testDefaults() {
        Vereinsmitgliedschaft neu = Vereinsmitgliedschaft.builder()
                .benutzer(benutzer)
                .verein(verein)
                .build();
        assertEquals(MitgliedschaftsStatus.BEANTRAGT, neu.getStatus());
        assertTrue(neu.getAktiv());
        assertFalse(neu.getIstAufseher());
        assertFalse(neu.getIstVereinschef());
    }

    @Test
    void testPrePersistSetsBeitrittDatumIfNull() {
        Vereinsmitgliedschaft neu = Vereinsmitgliedschaft.builder()
                .benutzer(benutzer)
                .verein(verein)
                .build();
        assertNull(neu.getBeitrittDatum());
        neu.onCreate();
        assertNotNull(neu.getBeitrittDatum());
    }

    @Test
    void testPrePersistKeepsExistingBeitrittDatum() {
        LocalDate custom = LocalDate.now().minusDays(5);
        Vereinsmitgliedschaft neu = Vereinsmitgliedschaft.builder()
                .benutzer(benutzer)
                .verein(verein)
                .beitrittDatum(custom)
                .build();
        neu.onCreate();
        assertEquals(custom, neu.getBeitrittDatum());
    }

    @Test
    void testSetStatus() {
        mitgliedschaft.setStatus(MitgliedschaftsStatus.AKTIV);
        assertEquals(MitgliedschaftsStatus.AKTIV, mitgliedschaft.getStatus());
    }

    @Test
    void testSetAufseherFlag() {
        assertFalse(mitgliedschaft.getIstAufseher());
        mitgliedschaft.setIstAufseher(true);
        assertTrue(mitgliedschaft.getIstAufseher());
    }

    @Test
    void testSetVereinschefFlag() {
        assertFalse(mitgliedschaft.getIstVereinschef());
        mitgliedschaft.setIstVereinschef(true);
        assertTrue(mitgliedschaft.getIstVereinschef());
    }

    @Test
    void testEqualsHashCodeOnId() {
        Vereinsmitgliedschaft m1 = Vereinsmitgliedschaft.builder().id(1L).benutzer(benutzer).verein(verein).build();
        Vereinsmitgliedschaft m2 = Vereinsmitgliedschaft.builder().id(1L).benutzer(benutzer).verein(verein).build();
        Vereinsmitgliedschaft m3 = Vereinsmitgliedschaft.builder().id(2L).benutzer(benutzer).verein(verein).build();
        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void testEntityToDtoMapping() {
        VereinsmigliedschaftDTO dto = mapper.toDTO(mitgliedschaft);

        assertNotNull(dto);
        assertEquals(mitgliedschaft.getId(), dto.getId());
        assertEquals(mitgliedschaft.getStatus(), dto.getStatus());
        assertEquals(mitgliedschaft.getAktiv(), dto.getAktiv());
        assertEquals(mitgliedschaft.getIstAufseher(), dto.getIstAufseher());
        assertEquals(mitgliedschaft.getIstVereinschef(), dto.getIstVereinschef());

        // Benutzer-Mapping
        assertEquals(benutzer.getId(), dto.getBenutzerId());
        assertEquals(benutzer.getVorname(), dto.getBenutzerVorname());
        assertEquals(benutzer.getNachname(), dto.getBenutzerNachname());

        // Verein-Mapping
        assertEquals(verein.getId(), dto.getVereinId());
        assertEquals(verein.getName(), dto.getVereinName());
    }

    @Test
    void testEntityToDtoMappingNull() {
        VereinsmigliedschaftDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testEntityToDtoHelperMethod() {
        VereinsmigliedschaftDTO dto = mapper.toDTO(mitgliedschaft);
        assertEquals("Max Mustermann", dto.getBenutzerVollstaendigerName());
    }
}

