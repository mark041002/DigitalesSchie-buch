package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.DigitalesZertifikatDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für DigitalesZertifikatMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class DigitalesZertifikatMapperTest {

    private DigitalesZertifikatMapper mapper;
    private DigitalesZertifikat testZertifikat;
    private Benutzer testBenutzer;
    private Verein testVerein;

    @BeforeEach
    void setUp() {
        mapper = new DigitalesZertifikatMapper();
        
        testBenutzer = Benutzer.builder()
                .id(1L)
                .vorname("Hans")
                .nachname("Schmidt")
                .email("hans@example.com")
                .build();
        
        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();
        
        testZertifikat = DigitalesZertifikat.builder()
                .id(1L)
                .zertifikatsTyp("AUFSEHER")
                .seriennummer("ZERT123456")
                .subjectDN("CN=Hans Schmidt, O=Digitales Schiessbuch")
                .issuerDN("CN=Testverein, O=Digitales Schiessbuch")
                .zertifikatPEM("-----BEGIN CERTIFICATE-----\nCERT_DATA\n-----END CERTIFICATE-----")
                .gueltigSeit(LocalDateTime.now().minusYears(1))
                .gueltigBis(LocalDateTime.now().plusYears(9))
                .widerrufen(false)
                .benutzer(testBenutzer)
                .verein(testVerein)
                .build();
    }

    @Test
    void testToDTO_MapsAllFields() {
        DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);

        assertNotNull(dto);
        assertEquals(testZertifikat.getId(), dto.getId());
        assertEquals(testZertifikat.getZertifikatsTyp(), dto.getZertifikatsTyp());
        assertEquals(testZertifikat.getSeriennummer(), dto.getSeriennummer());
        assertEquals(testZertifikat.getSubjectDN(), dto.getSubjectDN());
        assertEquals(testZertifikat.getIssuerDN(), dto.getIssuerDN());
        assertEquals(testZertifikat.getGueltigSeit(), dto.getGueltigSeit());
        assertEquals(testZertifikat.getGueltigBis(), dto.getGueltigBis());
        assertEquals(testZertifikat.isWiderrufen(), dto.getWiderrufen());
        assertEquals(testBenutzer.getId(), dto.getBenutzerId());
        assertEquals("Hans Schmidt", dto.getBenutzerVollstaendigerName());
        assertEquals(testVerein.getId(), dto.getVereinId());
        assertEquals("Testverein", dto.getVereinName());
    }

    @Test
    void testToDTO_DoesNotIncludePrivateKey() {
        DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);

        assertNotNull(dto);
        // Prüfe dass kein "privateKeyPEM" Feld im DTO existiert
        assertFalse(java.util.Arrays.stream(dto.getClass().getDeclaredFields())
                .anyMatch(f -> f.getName().equals("privateKeyPEM")));
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTO_WithNullBenutzer() {
        testZertifikat.setBenutzer(null);
        DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);

        assertNotNull(dto);
        assertNull(dto.getBenutzerId());
        assertNull(dto.getBenutzerVollstaendigerName());
    }

    @Test
    void testToDTO_WithNullVerein() {
        testZertifikat.setVerein(null);
        DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);

        assertNotNull(dto);
        assertNull(dto.getVereinId());
        assertNull(dto.getVereinName());
    }

    @Test
    void testToDTO_WithWiderrufenZertifikat() {
        testZertifikat.setWiderrufen(true);
        DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);

        assertNotNull(dto);
        assertTrue(dto.getWiderrufen());
        assertFalse(dto.istGueltig()); // Widerrufene Zertifikate sind ungültig
    }

    @Test
    void testToDTOList() {
        DigitalesZertifikat zertifikat2 = DigitalesZertifikat.builder()
                .id(2L)
                .zertifikatsTyp("VEREIN")
                .seriennummer("VEREIN123")
                .subjectDN("CN=Testverein")
                .issuerDN("CN=Root CA")
                .gueltigSeit(LocalDateTime.now())
                .widerrufen(false)
                .verein(testVerein)
                .build();

        List<DigitalesZertifikatDTO> dtos = mapper.toDTOList(
                List.of(testZertifikat, zertifikat2));

        assertEquals(2, dtos.size());
        assertEquals("AUFSEHER", dtos.get(0).getZertifikatsTyp());
        assertEquals("VEREIN", dtos.get(1).getZertifikatsTyp());
    }

    @Test
    void testMapping_DifferentZertifikatstypen() {
        String[] typen = {"ROOT", "VEREIN", "AUFSEHER", "SCHIESSTANDAUFSEHER"};

        for (String typ : typen) {
            testZertifikat.setZertifikatsTyp(typ);
            DigitalesZertifikatDTO dto = mapper.toDTO(testZertifikat);
            assertEquals(typ, dto.getZertifikatsTyp());
        }
    }
}
