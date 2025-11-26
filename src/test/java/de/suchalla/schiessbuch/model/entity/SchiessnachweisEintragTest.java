package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.mapper.SchiessnachweisEintragMapper;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die SchiessnachweisEintrag-Entity mit DTO-Mapping.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class SchiessnachweisEintragTest {

    private SchiessnachweisEintrag eintrag;
    private Benutzer schuetze;
    private Disziplin disziplin;
    private Schiesstand schiesstand;
    private Verein verein;
    private SchiessnachweisEintragMapper mapper;

    @BeforeEach
    void setUp() {
        verein = Verein.builder()
                .id(1L)
                .name("Verein A")
                .build();
        schuetze = Benutzer.builder()
                .id(1L)
                .email("schuetze@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .build();

        disziplin = Disziplin.builder()
            .id(1L)
            .kennziffer("LG-10m")
            .programm("Luftgewehr 10m")
            .build();

        schiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .verein(verein)
                .build();

        eintrag = SchiessnachweisEintrag.builder()
                .id(1L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .kaliber("4.5mm")
                .waffenart("Luftgewehr")
                .bemerkung("Gutes Training")
                .istSigniert(false)
                .status(EintragStatus.OFFEN)
                .build();

        mapper = new SchiessnachweisEintragMapper();
    }

    @Test
    void testSchiessnachweisEintragBuilder() {
        assertNotNull(eintrag);
        assertEquals(1L, eintrag.getId());
        assertEquals(schuetze, eintrag.getSchuetze());
        assertEquals(disziplin, eintrag.getDisziplin());
        assertEquals(schiesstand, eintrag.getSchiesstand());
        assertEquals(40, eintrag.getAnzahlSchuesse());
        assertEquals("380 Ringe", eintrag.getErgebnis());
        assertEquals("4.5mm", eintrag.getKaliber());
        assertEquals("Luftgewehr", eintrag.getWaffenart());
        assertEquals("Gutes Training", eintrag.getBemerkung());
        assertFalse(eintrag.getIstSigniert());
        assertEquals(EintragStatus.OFFEN, eintrag.getStatus());
    }

    @Test
    void testDefaultStatus() {
        SchiessnachweisEintrag neuerEintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .build();

        assertEquals(EintragStatus.OFFEN, neuerEintrag.getStatus());
    }

    @Test
    void testDefaultIstSigniert() {
        SchiessnachweisEintrag neuerEintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .build();

        assertFalse(neuerEintrag.getIstSigniert());
    }

    @Test
    void testSignierung() {
        Benutzer aufseher = Benutzer.builder()
                .id(2L)
                .email("aufseher@example.com")
                .vorname("Hans")
                .nachname("Schmidt")
                .rolle(BenutzerRolle.AUFSEHER)
                .passwort("password")
                .build();

        LocalDateTime signiertAm = LocalDateTime.now();

        eintrag.setAufseher(aufseher);
        eintrag.setSigniertAm(signiertAm);
        eintrag.setIstSigniert(true);
        eintrag.setStatus(EintragStatus.SIGNIERT);
        eintrag.setDigitaleSignatur("signature123");

        assertEquals(aufseher, eintrag.getAufseher());
        assertEquals(signiertAm, eintrag.getSigniertAm());
        assertTrue(eintrag.getIstSigniert());
        assertEquals(EintragStatus.SIGNIERT, eintrag.getStatus());
        assertEquals("signature123", eintrag.getDigitaleSignatur());
    }

    @Test
    void testAblehnung() {
        String ablehnungsgrund = "Fehlende Dokumentation";

        eintrag.setStatus(EintragStatus.ABGELEHNT);
        eintrag.setAblehnungsgrund(ablehnungsgrund);

        assertEquals(EintragStatus.ABGELEHNT, eintrag.getStatus());
        assertEquals(ablehnungsgrund, eintrag.getAblehnungsgrund());
    }

    @Test
    void testEqualsAndHashCode() {
        SchiessnachweisEintrag eintrag1 = SchiessnachweisEintrag.builder()
                .id(1L)
                .datum(LocalDate.now())
                .build();

        SchiessnachweisEintrag eintrag2 = SchiessnachweisEintrag.builder()
                .id(1L)
                .datum(LocalDate.now().plusDays(1))
                .build();

        SchiessnachweisEintrag eintrag3 = SchiessnachweisEintrag.builder()
                .id(2L)
                .datum(LocalDate.now())
                .build();

        assertEquals(eintrag1, eintrag2);
        assertEquals(eintrag1.hashCode(), eintrag2.hashCode());
        assertNotEquals(eintrag1, eintrag3);
    }

    @Test
    void testPrePersistSetsTimestamps() {
        SchiessnachweisEintrag neuerEintrag = new SchiessnachweisEintrag();
        assertNull(neuerEintrag.getErstelltAm());
        assertNull(neuerEintrag.getAktualisiertAm());

        neuerEintrag.onCreate();

        assertNotNull(neuerEintrag.getErstelltAm());
        assertNotNull(neuerEintrag.getAktualisiertAm());
    }

    @Test
    void testEntityToListDtoMapping() {
        SchiessnachweisEintrag dto = mapper.toDTO(eintrag);

        assertNotNull(dto);
        assertEquals(eintrag.getId(), dto.getId());
        assertEquals(eintrag.getDatum(), dto.getDatum());
        assertEquals(eintrag.getAnzahlSchuesse(), dto.getAnzahlSchuesse());
        assertEquals(eintrag.getErgebnis(), dto.getErgebnis());
        assertEquals(eintrag.getStatus(), dto.getStatus());

        // Schütze-Mapping
        assertEquals(schuetze.getId(), dto.getSchuetzeId());
        assertEquals("Max", dto.getSchuetzeVorname());
        assertEquals("Mustermann", dto.getSchuetzeNachname());

        // Disziplin-Mapping (Kennziffer shown in lists)
        assertEquals(disziplin.getId(), dto.getDisziplinId());
        assertEquals(disziplin.getKennziffer(), dto.getDisziplinName());

        // Schießstand-Mapping
        assertEquals(schiesstand.getId(), dto.getSchiesstandId());
        assertEquals("Stand 1", dto.getSchiesstandName());

        // Verein-Mapping (über Schießstand)
        assertEquals(verein.getId(), dto.getVereinId());
        assertEquals("Verein A", dto.getVereinName());
    }

    @Test
    void testEntityToDetailDtoMapping() {
        SchiessnachweisEintrag dto = mapper.toDetailDTO(eintrag);

        assertNotNull(dto);
        assertEquals(eintrag.getId(), dto.getId());
        assertEquals(eintrag.getBemerkung(), dto.getBemerkung());
        assertEquals(eintrag.getDigitaleSignatur(), dto.getDigitaleSignatur());
        assertEquals(schuetze.getEmail(), dto.getSchuetzeEmail());
    }

    @Test
    void testEntityToDtoMappingNull() {
        SchiessnachweisEintrag listDto = mapper.toDTO(null);
        assertNull(listDto);

        SchiessnachweisEintrag detailDto = mapper.toDetailDTO(null);
        assertNull(detailDto);
    }

    @Test
    void testEntityToDtoHelperMethods() {
        SchiessnachweisEintrag dto = mapper.toDTO(eintrag);
        assertEquals("Max Mustermann", dto.getSchuetzeVollstaendigerName());
    }
}

