package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für VereinsmigliedschaftMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class VereinsmigliedschaftMapperTest {

    private VereinsmigliedschaftMapper mapper;
    private Vereinsmitgliedschaft testMitgliedschaft;
    private Benutzer testBenutzer;
    private Verein testVerein;

    @BeforeEach
    void setUp() {
        mapper = new VereinsmigliedschaftMapper();
        
        testBenutzer = Benutzer.builder()
                .id(1L)
                .vorname("Max")
                .nachname("Mustermann")
                .email("max@example.com")
                .build();
        
        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .vereinsNummer("TV-123")
                .build();
        
        testMitgliedschaft = Vereinsmitgliedschaft.builder()
                .id(1L)
                .benutzer(testBenutzer)
                .verein(testVerein)
                .beitrittDatum(LocalDate.now().minusYears(2))
                .austrittDatum(null)
                .status(MitgliedschaftsStatus.AKTIV)
                .istVereinschef(false)
                .istAufseher(true)
                .build();
    }

    @Test
    void testToDTO_MapsAllFields() {
        VereinsmigliedschaftDTO dto = mapper.toDTO(testMitgliedschaft);

        assertNotNull(dto);
        assertEquals(testMitgliedschaft.getId(), dto.getId());
        assertEquals(testBenutzer.getId(), dto.getBenutzerId());
        assertEquals(testBenutzer.getVorname(), dto.getBenutzerVorname());
        assertEquals(testBenutzer.getNachname(), dto.getBenutzerNachname());
        assertEquals("Max Mustermann", dto.getBenutzerVollstaendigerName());
        assertEquals(testVerein.getId(), dto.getVereinId());
        assertEquals(testVerein.getName(), dto.getVereinName());
        assertEquals(testMitgliedschaft.getBeitrittDatum(), dto.getBeitrittDatum());
        assertEquals(testMitgliedschaft.getAustrittDatum(), dto.getAustrittDatum());
        assertEquals(testMitgliedschaft.getStatus(), dto.getStatus());
        assertEquals(false, dto.getIstVereinschef());
        assertEquals(true, dto.getIstAufseher());
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTO_WithAustrittsDatum() {
        testMitgliedschaft.setAustrittDatum(LocalDate.now());
        testMitgliedschaft.setStatus(MitgliedschaftsStatus.BEENDET);

        VereinsmigliedschaftDTO dto = mapper.toDTO(testMitgliedschaft);

        assertNotNull(dto);
        assertNotNull(dto.getAustrittDatum());
        assertEquals(MitgliedschaftsStatus.BEENDET, dto.getStatus());
    }

    @Test
    void testToDTOList() {
        Vereinsmitgliedschaft mitgliedschaft2 = Vereinsmitgliedschaft.builder()
                .id(2L)
                .benutzer(testBenutzer)
                .verein(testVerein)
                .beitrittDatum(LocalDate.now().minusYears(1))
                .status(MitgliedschaftsStatus.AKTIV)
                .istVereinschef(true)
                .istAufseher(false)
                .build();

        List<VereinsmigliedschaftDTO> dtos = mapper.toDTOList(
                List.of(testMitgliedschaft, mitgliedschaft2));

        assertEquals(2, dtos.size());
        assertTrue(dtos.get(0).getIstAufseher());
        assertTrue(dtos.get(1).getIstVereinschef());
    }

    @Test
    void testMapping_DifferentStatus() {
        for (MitgliedschaftsStatus status : MitgliedschaftsStatus.values()) {
            testMitgliedschaft.setStatus(status);
            VereinsmigliedschaftDTO dto = mapper.toDTO(testMitgliedschaft);
            assertEquals(status, dto.getStatus());
        }
    }
}
