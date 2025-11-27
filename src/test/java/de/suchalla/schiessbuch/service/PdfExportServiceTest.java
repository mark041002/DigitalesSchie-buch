package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
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
    private Verein testVerein;
    private Schiesstand testSchiesstand;
    private List<SchiessnachweisEintrag> testEintraege;
    private List<Vereinsmitgliedschaft> testMitgliedschaften;

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

        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();

        testSchiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                .adresse("Teststraße 1")
                .verein(testVerein)
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
        List<SchiessnachweisEintrag> leereEintraege = new ArrayList<>();
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
        List<Vereinsmitgliedschaft> leereMitgliedschaften = new ArrayList<>();

        byte[] pdfBytes = pdfExportService.exportiereVereinsmitgliedschaften(
                testVerein, leereMitgliedschaften, null, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testPdfGroesseMitMehrerenEintraegen() throws IOException {
        // Erstelle viele Einträge
        List<SchiessnachweisEintrag> vieleEintraege = new ArrayList<>();
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
        List<SchiessnachweisEintrag> gemischteEintraege = new ArrayList<>();
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
        SchiessnachweisEintrag eintragMitZertifikat = createTestEintrag(
                1L, LocalDate.now(), "Luftgewehr 10m", "4.5mm", 40, "380 Ringe",
                EintragStatus.SIGNIERT);

        // Create Aufseher
        Benutzer aufseher = Benutzer.builder()
                .id(2L)
                .vorname("Hans")
                .nachname("Schmidt")
                .email("hans@example.com")
                .passwort("test")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        eintragMitZertifikat.setAufseher(aufseher);
        eintragMitZertifikat.setSigniertAm(LocalDateTime.now());

        List<SchiessnachweisEintrag> eintraegeMitZertifikat = List.of(eintragMitZertifikat);

        byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                testSchuetze, eintraegeMitZertifikat,
                LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    // Hilfsmethoden

    private SchiessnachweisEintrag createTestEintrag(Long id, LocalDate datum,
            String disziplinName, String kaliber, Integer schuesse, String ergebnis,
            EintragStatus status) {
        // Create Benutzer (Schütze)
        Benutzer schuetze = Benutzer.builder()
                .id(1L)
                .vorname("Max")
                .nachname("Mustermann")
                .email("max@example.com")
                .passwort("test")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        // Create Disziplin
        Disziplin disziplin = Disziplin.builder()
                .id(1L)
                .kennziffer("LG-10m")
                .programm(disziplinName)
                .build();

        // Create Verein
        Verein verein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();

        // Create Schiesstand
        Schiesstand schiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                .adresse("Teststraße 1")
                .verein(verein)
                .build();

        // Create Eintrag
        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .id(id)
                .datum(datum)
                .disziplin(disziplin)
                .kaliber(kaliber)
                .anzahlSchuesse(schuesse)
                .ergebnis(ergebnis)
                .status(status)
                .schuetze(schuetze)
                .schiesstand(schiesstand)
                .build();

        return eintrag;
    }

    private Vereinsmitgliedschaft createTestMitgliedschaft(Long id, String vorname,
            String nachname, LocalDate beitritt, MitgliedschaftsStatus status,
            Boolean istVereinschef, Boolean istAufseher) {
        // Create Benutzer
        Benutzer benutzer = Benutzer.builder()
                .id(id)
                .vorname(vorname)
                .nachname(nachname)
                .email(vorname.toLowerCase() + "@example.com")
                .passwort("test")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        // Create Verein
        Verein verein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();

        // Create Vereinsmitgliedschaft
        Vereinsmitgliedschaft mitgliedschaft = Vereinsmitgliedschaft.builder()
                .id(id)
                .benutzer(benutzer)
                .verein(verein)
                .beitrittDatum(beitritt)
                .status(status)
                .istVereinschef(istVereinschef)
                .istAufseher(istAufseher)
                .build();

        return mitgliedschaft;
    }
}
