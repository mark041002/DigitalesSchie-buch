package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.dto.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für PdfExportService.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class PdfExportServiceTest {

    private PdfExportService pdfExportService;
    private BenutzerDTO testSchuetze;
    private VereinDTO testVerein;
    private SchiesstandDTO testSchiesstand;
    private List<SchiessnachweisEintragListDTO> testEintraege;
    private List<VereinsmigliedschaftDTO> testMitgliedschaften;

    @BeforeEach
    void setUp() {
        pdfExportService = new PdfExportService();

        testSchuetze = BenutzerDTO.builder()
                .id(1L)
                .vorname("Max")
                .nachname("Mustermann")
                .email("max.mustermann@example.com")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        testVerein = VereinDTO.builder()
                .id(1L)
                .name("Testverein")
                .vereinsNummer("TV-123")
                .build();

        testSchiesstand = SchiesstandDTO.builder()
                .id(1L)
                .name("Stand 1")
                .adresse("Teststraße 1")
                .vereinId(1L)
                .vereinName("Testverein")
                .build();

        testEintraege = new ArrayList<>();
        testEintraege.add(createTestEintrag(1L, LocalDate.now(), "Luftgewehr 10m", 
                "4.5mm", 40, "380 Ringe", EintragStatus.SIGNIERT));
        testEintraege.add(createTestEintrag(2L, LocalDate.now().minusDays(1), 
                "Luftpistole 10m", "4.5mm", 30, "290 Ringe", EintragStatus.SIGNIERT));

        testMitgliedschaften = new ArrayList<>();
        testMitgliedschaften.add(createTestMitgliedschaft(1L, "Max", "Mustermann", 
                LocalDate.now().minusYears(1), MitgliedschaftsStatus.AKTIV, true, false));
        testMitgliedschaften.add(createTestMitgliedschaft(2L, "Hans", "Schmidt", 
                LocalDate.now().minusYears(2), MitgliedschaftsStatus.AKTIV, false, true));
    }

    @Test
    void testExportiereSchiessnachweiseErsteltPdf() throws IOException {
        LocalDate von = LocalDate.now().minusDays(7);
        LocalDate bis = LocalDate.now();

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, testEintraege, von, bis);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF sollte Daten enthalten");
        
        // PDF-Header prüfen
        String pdfHeader = new String(pdfBytes, 0, Math.min(8, pdfBytes.length));
        assertTrue(pdfHeader.startsWith("%PDF"), "Sollte ein gültiges PDF sein");
    }

    @Test
    void testExportiereSchiessnachweiseMitLeerenEintraegen() throws IOException {
        List<SchiessnachweisEintragListDTO> leereEintraege = new ArrayList<>();
        LocalDate von = LocalDate.now().minusDays(7);
        LocalDate bis = LocalDate.now();

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, leereEintraege, von, bis);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF sollte auch mit leeren Einträgen erstellt werden");
    }

    @Test
    void testExportiereSchiessnachweiseMitNullDaten() {
        assertThrows(Exception.class, () -> {
            pdfExportService.exportiereSchiessnachweise(null, testEintraege, 
                    LocalDate.now(), LocalDate.now());
        });
    }

    @Test
    void testExportiereEintragsverwaltungSchiesstand() throws IOException {
        LocalDate von = LocalDate.now().minusDays(7);
        LocalDate bis = LocalDate.now();

        byte[] pdfBytes = pdfExportService.exportiereEintragsverwaltungSchiesstand(
                testSchiesstand, testEintraege, von, bis);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF sollte Daten enthalten");
        
        // PDF-Header prüfen
        String pdfHeader = new String(pdfBytes, 0, Math.min(8, pdfBytes.length));
        assertTrue(pdfHeader.startsWith("%PDF"), "Sollte ein gültiges PDF sein");
    }

    @Test
    void testExportiereEintragsverwaltungMitNullSchiesstand() throws IOException {
        // Null Schießstand ist erlaubt - zeigt "-" an
        byte[] pdfBytes = pdfExportService.exportiereEintragsverwaltungSchiesstand(
                null, testEintraege, LocalDate.now(), LocalDate.now());
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testExportiereVereinsmitgliedschaften() throws IOException {
        LocalDate von = LocalDate.now().minusYears(1);
        LocalDate bis = LocalDate.now();

        byte[] pdfBytes = pdfExportService.exportiereVereinsmitgliedschaften(
                testVerein, testMitgliedschaften, von, bis);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF sollte Daten enthalten");
        
        // PDF-Header prüfen
        String pdfHeader = new String(pdfBytes, 0, Math.min(8, pdfBytes.length));
        assertTrue(pdfHeader.startsWith("%PDF"), "Sollte ein gültiges PDF sein");
    }

    @Test
    void testExportiereVereinsmitgliedschaftenOhneZeitraum() throws IOException {
        byte[] pdfBytes = pdfExportService.exportiereVereinsmitgliedschaften(
                testVerein, testMitgliedschaften, null, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testExportiereVereinsmitgliedschaftenMitLeerenMitgliedschaften() throws IOException {
        List<VereinsmigliedschaftDTO> leereMitgliedschaften = new ArrayList<>();

        byte[] pdfBytes = pdfExportService.exportiereVereinsmitgliedschaften(
                testVerein, leereMitgliedschaften, null, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testPdfGroesseMitMehrerenEintraegen() throws IOException {
        // Erstelle viele Einträge
        List<SchiessnachweisEintragListDTO> vieleEintraege = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            vieleEintraege.add(createTestEintrag((long) i, 
                    LocalDate.now().minusDays(i), 
                    "Disziplin " + i, 
                    "4.5mm", 
                    40, 
                    (380 + i) + " Ringe", 
                    EintragStatus.SIGNIERT));
        }

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, vieleEintraege, 
                LocalDate.now().minusDays(50), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 10000, "PDF mit vielen Einträgen sollte größer sein");
    }

    @Test
    void testExportiereSchiessnachweiseMitVerschiedenenStatus() throws IOException {
        List<SchiessnachweisEintragListDTO> gemischteEintraege = new ArrayList<>();
        gemischteEintraege.add(createTestEintrag(1L, LocalDate.now(), 
                "Luftgewehr 10m", "4.5mm", 40, "380 Ringe", EintragStatus.OFFEN));
        gemischteEintraege.add(createTestEintrag(2L, LocalDate.now().minusDays(1), 
                "Luftpistole 10m", "4.5mm", 30, "290 Ringe", EintragStatus.SIGNIERT));
        gemischteEintraege.add(createTestEintrag(3L, LocalDate.now().minusDays(2), 
                "KK Gewehr 50m", "5.6mm", 25, "240 Ringe", EintragStatus.ABGELEHNT));

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, gemischteEintraege, 
                LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testPdfMitPkiZertifikatsinformationen() throws IOException {
        // Einträge mit PKI-Signaturinformationen
        SchiessnachweisEintragListDTO eintragMitZertifikat = createTestEintrag(
                1L, LocalDate.now(), "Luftgewehr 10m", "4.5mm", 40, "380 Ringe", 
                EintragStatus.SIGNIERT);
        eintragMitZertifikat.setAufseherVorname("Hans");
        eintragMitZertifikat.setAufseherNachname("Schmidt");
        eintragMitZertifikat.setSigniertAm(LocalDateTime.now());
        
        List<SchiessnachweisEintragListDTO> eintraegeMitZertifikat = List.of(eintragMitZertifikat);

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, eintraegeMitZertifikat, 
                LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    // Hilfsmethoden

    private SchiessnachweisEintragListDTO createTestEintrag(Long id, LocalDate datum, 
            String disziplin, String kaliber, Integer schuesse, String ergebnis, 
            EintragStatus status) {
        SchiessnachweisEintragListDTO dto = new SchiessnachweisEintragListDTO();
        dto.setId(id);
        dto.setDatum(datum);
        dto.setDisziplinId(1L);
        dto.setDisziplinName(disziplin);
        dto.setKaliber(kaliber);
        dto.setAnzahlSchuesse(schuesse);
        dto.setErgebnis(ergebnis);
        dto.setStatus(status);
        dto.setSchuetzeId(1L);
        dto.setSchuetzeVorname("Max");
        dto.setSchuetzeNachname("Mustermann");
        dto.setSchiesstandId(1L);
        dto.setSchiesstandName("Stand 1");
        dto.setVereinId(1L);
        dto.setVereinName("Testverein");
        dto.setAufseherVorname(null);
        dto.setAufseherNachname(null);
        return dto;
    }

    private VereinsmigliedschaftDTO createTestMitgliedschaft(Long id, String vorname, 
            String nachname, LocalDate beitritt, MitgliedschaftsStatus status, 
            Boolean istVereinschef, Boolean istAufseher) {
        VereinsmigliedschaftDTO dto = new VereinsmigliedschaftDTO();
        dto.setId(id);
        dto.setBenutzerId(id);
        dto.setBenutzerVorname(vorname);
        dto.setBenutzerNachname(nachname);
        dto.setVereinId(1L);
        dto.setVereinName("Testverein");
        dto.setBeitrittDatum(beitritt);
        dto.setStatus(status);
        dto.setIstVereinschef(istVereinschef);
        dto.setIstAufseher(istAufseher);
        return dto;
    }
}
