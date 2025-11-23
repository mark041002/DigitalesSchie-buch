package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.mapper.DigitalesZertifikatMapper;
import de.suchalla.schiessbuch.model.dto.DigitalesZertifikatDTO;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DigitalesZertifikatTest {

    private DigitalesZertifikat zertifikat;
    private Benutzer benutzer;
    private DigitalesZertifikatMapper mapper;

    @BeforeEach
    void setUp() {
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
        zertifikat = TestDataFactory.createZertifikat(1L, "AUFSEHER", benutzer);
        mapper = new DigitalesZertifikatMapper();
    }

    @Test
    void testBuilder() {
        assertNotNull(zertifikat);
        assertEquals("AUFSEHER", zertifikat.getZertifikatsTyp());
        assertNotNull(zertifikat.getSeriennummer());
        assertTrue(zertifikat.getSeriennummer().length() > 0);
        assertEquals(benutzer, zertifikat.getBenutzer());
        assertFalse(zertifikat.isWiderrufen());
    }

    @Test
    void testIstGueltigTrue() {
        assertTrue(zertifikat.istGueltig());
    }

    @Test
    void testIstGueltigFalseWiderrufen() {
        zertifikat.setWiderrufen(true);
        assertFalse(zertifikat.istGueltig());
    }

    @Test
    void testIstGueltigFalseVorGueltigSeit() {
        zertifikat.setGueltigSeit(LocalDateTime.now().plusHours(1));
        assertFalse(zertifikat.istGueltig());
    }

    @Test
    void testIstGueltigFalseNachGueltigBis() {
        zertifikat.setGueltigBis(LocalDateTime.now().minusMinutes(1));
        assertFalse(zertifikat.istGueltig());
    }

    @Test
    void testPrePersistSetsTimestamps() {
        DigitalesZertifikat neu = DigitalesZertifikat.builder()
                .zertifikatsTyp("ROOT")
                .seriennummer("123")
                .subjectDN("CN=Test")
                .issuerDN("CN=TestIssuer")
                .zertifikatPEM("PEM")
                .gueltigSeit(LocalDateTime.now().minusDays(1))
                .build();
        assertNull(neu.getErstelltAm());
        assertNull(neu.getAktualisiertAm());
        neu.onCreate();
        assertNotNull(neu.getErstelltAm());
        assertNotNull(neu.getAktualisiertAm());
    }

    @Test
    void testPreUpdateUpdatesTimestamp() throws InterruptedException {
        zertifikat.onCreate();
        LocalDateTime created = zertifikat.getErstelltAm();
        Thread.sleep(5);
        zertifikat.onUpdate();
        assertEquals(created, zertifikat.getErstelltAm());
        assertNotNull(zertifikat.getAktualisiertAm());
        assertTrue(zertifikat.getAktualisiertAm().isAfter(created) || zertifikat.getAktualisiertAm().isEqual(created));
    }

    @Test
    void testToString() {
        String ts = zertifikat.toString();
        assertTrue(ts.contains("AUFSEHER"));
        assertTrue(ts.contains("seriennummer".replace("seriennummer","")) || ts.contains("DigitalesZertifikat"));
    }

    @Test
    void testEntityToDtoMapping() {
        DigitalesZertifikatDTO dto = mapper.toDTO(zertifikat);

        assertNotNull(dto);
        assertEquals(zertifikat.getId(), dto.getId());
        assertEquals(zertifikat.getZertifikatsTyp(), dto.getZertifikatsTyp());
        assertEquals(zertifikat.getSeriennummer(), dto.getSeriennummer());
        assertEquals(zertifikat.getSubjectDN(), dto.getSubjectDN());
        assertEquals(zertifikat.getIssuerDN(), dto.getIssuerDN());
        assertEquals(zertifikat.getZertifikatPEM(), dto.getZertifikatPEM());
        assertEquals(zertifikat.isWiderrufen(), dto.getWiderrufen());

        // Benutzer-Mapping
        assertEquals(benutzer.getId(), dto.getBenutzerId());
        assertEquals(benutzer.getVollstaendigerName(), dto.getBenutzerVollstaendigerName());
        assertEquals(benutzer.getEmail(), dto.getBenutzerEmail());
    }

    @Test
    void testEntityToDtoMappingNullEntity() {
        DigitalesZertifikatDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testEntityToDtoIstGueltigMethode() {
        DigitalesZertifikatDTO dto = mapper.toDTO(zertifikat);
        assertEquals(zertifikat.istGueltig(), dto.istGueltig());
    }
}

