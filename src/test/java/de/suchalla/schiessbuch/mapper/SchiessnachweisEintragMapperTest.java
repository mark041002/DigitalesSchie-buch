package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragDetailDTO;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragListDTO;
import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für SchiessnachweisEintragMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class SchiessnachweisEintragMapperTest {

    private SchiessnachweisEintragMapper mapper;
    private SchiessnachweisEintrag testEintrag;
    private Benutzer schuetze;
    private Benutzer aufseher;
    private Disziplin disziplin;
    private Schiesstand schiesstand;
    private Verein verein;

    @BeforeEach
    void setUp() {
        mapper = new SchiessnachweisEintragMapper();

        verein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .vereinsNummer("TV-123")
                .build();

        schuetze = Benutzer.builder()
                .id(1L)
                .email("schuetze@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        aufseher = Benutzer.builder()
                .id(2L)
                .email("aufseher@example.com")
                .vorname("Hans")
                .nachname("Schmidt")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        disziplin = Disziplin.builder()
            .id(1L)
            .kennziffer("LG-10m")
            .programm("Standard-Disziplin")
            .build();

        schiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .adresse("Teststraße 1")
                .verein(verein)
                .build();

        testEintrag = SchiessnachweisEintrag.builder()
                .id(1L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .kaliber("4.5mm")
                .waffenart("Luftgewehr")
                .bemerkung("Test-Bemerkung")
                .status(EintragStatus.SIGNIERT)
                .aufseher(aufseher)
                .signiertAm(LocalDateTime.now())
                .digitaleSignatur("SIGNATURE123")
                .build();
    }

    @Test
    void testToDTO_MapsListDTOCorrectly() {
        SchiessnachweisEintragListDTO dto = mapper.toDTO(testEintrag);

        assertNotNull(dto);
        assertEquals(testEintrag.getId(), dto.getId());
        assertEquals(testEintrag.getDatum(), dto.getDatum());
        assertEquals(testEintrag.getAnzahlSchuesse(), dto.getAnzahlSchuesse());
        assertEquals(testEintrag.getErgebnis(), dto.getErgebnis());
        assertEquals(testEintrag.getKaliber(), dto.getKaliber());
        assertEquals(testEintrag.getStatus(), dto.getStatus());
        
        // Schütze-Felder
        assertEquals(schuetze.getId(), dto.getSchuetzeId());
        assertEquals(schuetze.getVorname(), dto.getSchuetzeVorname());
        assertEquals(schuetze.getNachname(), dto.getSchuetzeNachname());
        assertEquals("Max Mustermann", dto.getSchuetzeVollstaendigerName());
        
        // Disziplin-Felder (Kennziffer wird als Label verwendet)
        assertEquals(disziplin.getId(), dto.getDisziplinId());
        assertEquals(disziplin.getKennziffer(), dto.getDisziplinName());
        assertEquals(disziplin.getProgramm(), dto.getDisziplinProgramm());
        
        // Schießstand-Felder
        assertEquals(schiesstand.getId(), dto.getSchiesstandId());
        assertEquals(schiesstand.getName(), dto.getSchiesstandName());
        
        // Verein-Felder (über Schießstand)
        assertEquals(verein.getId(), dto.getVereinId());
        assertEquals(verein.getName(), dto.getVereinName());
        
        // Aufseher-Felder
        assertEquals("Hans Schmidt", dto.getAufseherVollstaendigerName());
        assertEquals(testEintrag.getSigniertAm(), dto.getSigniertAm());
    }

    @Test
    void testToDTO_HandlesNull() {
        SchiessnachweisEintragListDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testToDTO_HandlesNullAufseher() {
        testEintrag.setAufseher(null);
        testEintrag.setSigniertAm(null);
        testEintrag.setStatus(EintragStatus.OFFEN);

        SchiessnachweisEintragListDTO dto = mapper.toDTO(testEintrag);

        assertNotNull(dto);
        assertEquals("-", dto.getAufseherVollstaendigerName());
        assertNull(dto.getSigniertAm());
    }

    @Test
    void testToDetailDTO_MapsAllFieldsCorrectly() {
        SchiessnachweisEintragDetailDTO detailDto = mapper.toDetailDTO(testEintrag);

        assertNotNull(detailDto);
        assertEquals(testEintrag.getId(), detailDto.getId());
        assertEquals(testEintrag.getDatum(), detailDto.getDatum());
        assertEquals(testEintrag.getAnzahlSchuesse(), detailDto.getAnzahlSchuesse());
        assertEquals(testEintrag.getErgebnis(), detailDto.getErgebnis());
        assertEquals(testEintrag.getKaliber(), detailDto.getKaliber());
        assertEquals(testEintrag.getWaffenart(), detailDto.getWaffenart());
        assertEquals(testEintrag.getBemerkung(), detailDto.getBemerkung());
        assertEquals(testEintrag.getStatus(), detailDto.getStatus());
        assertEquals(testEintrag.getDigitaleSignatur(), detailDto.getDigitaleSignatur());
        
        // Erweiterte Felder
        assertEquals(schuetze.getEmail(), detailDto.getSchuetzeEmail());
        assertEquals(aufseher.getEmail(), detailDto.getAufseherEmail());
    }

    @Test
    void testToDetailDTO_HandlesNull() {
        SchiessnachweisEintragDetailDTO detailDto = mapper.toDetailDTO(null);
        assertNull(detailDto);
    }

    @Test
    void testToDetailDTO_WithNullAufseher() {
        testEintrag.setAufseher(null);
        testEintrag.setDigitaleSignatur(null);

        SchiessnachweisEintragDetailDTO detailDto = mapper.toDetailDTO(testEintrag);

        assertNotNull(detailDto);
        assertNull(detailDto.getAufseherEmail());
        assertNull(detailDto.getDigitaleSignatur());
    }

    @Test
    void testToDTOList_MapsMultipleEntities() {
        SchiessnachweisEintrag eintrag2 = SchiessnachweisEintrag.builder()
                .id(2L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now().minusDays(1))
                .anzahlSchuesse(30)
                .ergebnis("290 Ringe")
                .status(EintragStatus.OFFEN)
                .build();

        List<SchiessnachweisEintragListDTO> dtos = mapper.toDTOList(
                List.of(testEintrag, eintrag2));

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals(40, dtos.get(0).getAnzahlSchuesse());
        assertEquals(30, dtos.get(1).getAnzahlSchuesse());
    }

    @Test
    void testMapping_WithDifferentStatus() {
        EintragStatus[] statuses = {EintragStatus.OFFEN, EintragStatus.SIGNIERT, 
                                     EintragStatus.ABGELEHNT};

        for (EintragStatus status : statuses) {
            testEintrag.setStatus(status);
            SchiessnachweisEintragListDTO dto = mapper.toDTO(testEintrag);
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testMapping_WithNullOptionalFields() {
        SchiessnachweisEintrag minimalEintrag = SchiessnachweisEintrag.builder()
                .id(3L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .status(EintragStatus.OFFEN)
                // Keine optionalen Felder gesetzt
                .build();

        SchiessnachweisEintragListDTO dto = mapper.toDTO(minimalEintrag);

        assertNotNull(dto);
        assertNull(dto.getAnzahlSchuesse());
        assertNull(dto.getErgebnis());
        assertNull(dto.getKaliber());
        assertEquals("-", dto.getAufseherVollstaendigerName());
    }

    @Test
    void testSchuetzeVollstaendigerName_Constructed() {
        SchiessnachweisEintragListDTO dto = mapper.toDTO(testEintrag);
        
        assertEquals(schuetze.getVollstaendigerName(), dto.getSchuetzeVollstaendigerName());
        assertEquals(schuetze.getVorname() + " " + schuetze.getNachname(), 
                     dto.getSchuetzeVollstaendigerName());
    }
}
