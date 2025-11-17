package de.suchalla.schiessbuch.service;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragListDTO;
import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.dto.VereinDTO;
import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Service für PDF-Export von Schießnachweisen mit PKI-Signaturinformationen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Exportiert Schießnachweise als PDF mit PKI-Signaturinformationen.
     * Verwendet DTOs für sichere Datenübergabe.
     *
     * @param schuetze Der Schütze (DTO)
     * @param eintraege Liste der Einträge (DTOs)
     * @param von Start-Datum
     * @param bis End-Datum
     * @return PDF als Byte-Array
     * @throws IOException bei Fehlern
     */
    public byte[] exportiereSchiessnachweise(BenutzerDTO schuetze, List<SchiessnachweisEintragListDTO> eintraege,
                                              LocalDate von, LocalDate bis) throws IOException {
        log.info("=== PDF-EXPORT GESTARTET ===");
        log.info("Schütze: {}", schuetze.getVollstaendigerName());
        log.info("Zeitraum: {} bis {}", von, bis);
        log.info("Anzahl Einträge: {}", eintraege.size());

        // PKI-Zertifikat-Informationen loggen (DTOs haben zertifikatSeriennummer)
        for (SchiessnachweisEintragListDTO eintrag : eintraege) {
            log.info("Eintrag {}: Aufseher={}, Signiert am={}",
                eintrag.getId(),
                eintrag.getAufseherVollstaendigerName(),
                eintrag.getSigniertAm());
        }
        log.info("============================");

        // Sortiere Einträge nach Datum aufsteigend (nulls last)
        eintraege.sort(Comparator.comparing(SchiessnachweisEintragListDTO::getDatum, Comparator.nullsLast(Comparator.naturalOrder())));

        // Ermittelter Anzeige-Zeitraum basierend auf den tatsächlichen Einträgen (falls vorhanden)
        LocalDate displayVon = null;
        LocalDate displayBis = null;
        if (!eintraege.isEmpty()) {
            for (SchiessnachweisEintragListDTO e : eintraege) {
                if (e == null || e.getDatum() == null) continue;
                LocalDate d = e.getDatum();
                if (displayVon == null || d.isBefore(displayVon)) displayVon = d;
                if (displayBis == null || d.isAfter(displayBis)) displayBis = d;
            }
        }
        // Fallback auf übergebene Parameter, falls keine Einträge vorhanden oder keine Daten in Einträgen
        if (displayVon == null) displayVon = von;
        if (displayBis == null) displayBis = bis;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float pageWidth = page.getMediaBox().getWidth();
            float yPosition = page.getMediaBox().getHeight() - margin;

            // Kopfzeile
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Digitales Schießbuch - PKI-gesichert");
                contentStream.endText();

                yPosition -= 30;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Schütze: " + schuetze.getVollstaendigerName());
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                String zeitraumText = "-";
                if (displayVon != null || displayBis != null) {
                    String vonText = displayVon != null ? displayVon.format(DATE_FORMATTER) : "-";
                    String bisText = displayBis != null ? displayBis.format(DATE_FORMATTER) : "-";
                    zeitraumText = vonText + " - " + bisText;
                }
                contentStream.showText("Zeitraum: " + zeitraumText);
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Exportiert am: " + LocalDate.now().format(DATE_FORMATTER));
                contentStream.endText();

                yPosition -= 30;
            }

            // Zentrierte Tabelle erstellen
            float tableWidth = pageWidth - (2 * margin); // nutze symmetrische Ränder
            float leftMargin = (pageWidth - tableWidth) / 2f;

            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, tableWidth, leftMargin, document, page, true, true);

            // Header (Zertifikat-SN entfernt, andere Spalten verbreitert)
            Row<PDPage> headerRow = table.createRow(20);
            Cell<PDPage> cell1 = headerRow.createCell(10, "Datum");
            cell1.setFont(PDType1Font.HELVETICA_BOLD);
            cell1.setFontSize(9);

            Cell<PDPage> cell2 = headerRow.createCell(18, "Disziplin");
            cell2.setFont(PDType1Font.HELVETICA_BOLD);
            cell2.setFontSize(10);

            Cell<PDPage> cell3 = headerRow.createCell(10, "Kaliber");
            cell3.setFont(PDType1Font.HELVETICA_BOLD);
            cell3.setFontSize(9);

            Cell<PDPage> cell4 = headerRow.createCell(12, "Schießstand");
            cell4.setFont(PDType1Font.HELVETICA_BOLD);
            cell4.setFontSize(10);

            // Neue Spalten für Schütze-Export: Schüsse und Ergebnis
            Cell<PDPage> cellSchuesse = headerRow.createCell(8, "Schüsse");
            cellSchuesse.setFont(PDType1Font.HELVETICA_BOLD);
            cellSchuesse.setFontSize(10);

            Cell<PDPage> cellErgebnis = headerRow.createCell(15, "Ergebnis");
            cellErgebnis.setFont(PDType1Font.HELVETICA_BOLD);
            cellErgebnis.setFontSize(10);

            Cell<PDPage> cell5 = headerRow.createCell(15, "Aufseher");
            cell5.setFont(PDType1Font.HELVETICA_BOLD);
            cell5.setFontSize(10);

            Cell<PDPage> cell7 = headerRow.createCell(10, "Signiert am");
            cell7.setFont(PDType1Font.HELVETICA_BOLD);
            cell7.setFontSize(10);

            // Map, um pro Aufseher die Seriennummern zu sammeln (für spätere Erweiterung)
            Map<String, Set<String>> aufseherToSns = new LinkedHashMap<>();

            // Datenzeilen (jetzt mit DTOs - direkte Feldaufrufe)
            for (SchiessnachweisEintragListDTO eintrag : eintraege) {
                Row<PDPage> row = table.createRow(15);

                row.createCell(10, eintrag.getDatum() != null ? eintrag.getDatum().format(DATE_FORMATTER) : "-").setFontSize(8);
                row.createCell(18, eintrag.getDisziplinName() != null ? eintrag.getDisziplinName() : "-").setFontSize(9);
                row.createCell(10, eintrag.getKaliber() != null ? eintrag.getKaliber() : "-").setFontSize(8);
                row.createCell(12, eintrag.getSchiesstandName() != null ? eintrag.getSchiesstandName() : "-").setFontSize(9);

                // Anzahl Schüsse und Ergebnis anzeigen
                row.createCell(8, eintrag.getAnzahlSchuesse() != null ? eintrag.getAnzahlSchuesse().toString() : "-").setFontSize(9);
                row.createCell(15, eintrag.getErgebnis() != null ? eintrag.getErgebnis() : "-").setFontSize(9);

                String aufseherName = eintrag.getAufseherVollstaendigerName();
                row.createCell(15, aufseherName).setFontSize(9);

                // Sammle Aufseher-Namen für PKI-Hinweis
                if (aufseherName != null && !aufseherName.equals("-")) {
                    aufseherToSns.computeIfAbsent(aufseherName, k -> new LinkedHashSet<>())
                            .add("PKI-signiert");
                }

                String signiertAm = eintrag.getSigniertAm() != null ?
                        eintrag.getSigniertAm().format(DATETIME_FORMATTER) : "-";
                row.createCell(10, signiertAm).setFontSize(8);
            }

            table.draw();

            // PKI-Zertifikatsdetails nach der Tabelle: jetzt pro Aufseher einmal die SN(s) ausgeben
            PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, lastPage,
                    PDPageContentStream.AppendMode.APPEND, true)) {

                float footerY = margin + 150;

                // PKI-Zertifikatsdetails
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, footerY);
                contentStream.showText("PKI-Zertifikatsdetails für die Aufseher:");
                contentStream.endText();

                footerY -= 15;
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                if (!aufseherToSns.isEmpty()) {
                    for (Map.Entry<String, Set<String>> entry : aufseherToSns.entrySet()) {
                        String name = entry.getKey();
                        String sns = String.join(", ", entry.getValue());
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, footerY);
                        contentStream.showText("- " + name + " -> SN: " + sns);
                        contentStream.endText();
                        footerY -= 12;
                    }
                } else {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, footerY);
                    contentStream.showText("WARNUNG: Keine PKI-Zertifikate gefunden!");
                    contentStream.endText();
                    footerY -= 12;
                }

                footerY -= 10;
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, footerY);
                contentStream.showText("Anzahl Einträge: " + eintraege.size());
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("PDF für {} mit {} Einträgen und PKI-Signaturinformationen erstellt (DTOs verwendet)", schuetze.getEmail(), eintraege.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Export für die Eintragsverwaltung (Schießstand-Ansicht).
     * Zeigt zusätzlich die Spalte Schütze an und verwendet in der Kopfzeile den Schießstandnamen.
     * Verwendet DTOs für sichere Datenübergabe.
     */
    public byte[] exportiereEintragsverwaltungSchiesstand(SchiesstandDTO schiesstand, List<SchiessnachweisEintragListDTO> eintraege,
                                                         LocalDate von, LocalDate bis) throws IOException {
        log.info("=== PDF-EXPORT EINTRAGSVERWALTUNG (Schießstand) GESTARTET ===");
        log.info("Schießstand: {}", schiesstand != null ? schiesstand.getName() : "-" );
        log.info("Zeitraum: {} bis {}", von, bis);
        log.info("Anzahl Einträge: {}", eintraege.size());

        // Sortiere nach Datum
        eintraege.sort(Comparator.comparing(SchiessnachweisEintragListDTO::getDatum, Comparator.nullsLast(Comparator.naturalOrder())));

        // Ermittlung Zeitraum (wie in der anderen Methode)
        LocalDate displayVon = null;
        LocalDate displayBis = null;
        if (!eintraege.isEmpty()) {
            for (SchiessnachweisEintragListDTO e : eintraege) {
                if (e == null || e.getDatum() == null) continue;
                LocalDate d = e.getDatum();
                if (displayVon == null || d.isBefore(displayVon)) displayVon = d;
                if (displayBis == null || d.isAfter(displayBis)) displayBis = d;
            }
        }
        if (displayVon == null) displayVon = von;
        if (displayBis == null) displayBis = bis;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float pageWidth = page.getMediaBox().getWidth();
            float yPosition = page.getMediaBox().getHeight() - margin;

            // Kopfzeile mit Schießstand
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Digitales Schießbuch - ");
                contentStream.endText();

                yPosition -= 30;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Schießstand: " + (schiesstand != null ? schiesstand.getName() : "-"));
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                String vonText = displayVon != null ? displayVon.format(DATE_FORMATTER) : "-";
                String bisText = displayBis != null ? displayBis.format(DATE_FORMATTER) : "-";
                contentStream.showText("Zeitraum: " + vonText + " - " + bisText);
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Exportiert am: " + LocalDate.now().format(DATE_FORMATTER));
                contentStream.endText();

                yPosition -= 30;
            }

            // Tabelle: zusätzliche Spalte Schütze
            float tableWidth = pageWidth - (2 * margin);
            float leftMargin = (pageWidth - tableWidth) / 2f;
            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, tableWidth, leftMargin, document, page, true, true);

            Row<PDPage> headerRow = table.createRow(20);
            Cell<PDPage> h1 = headerRow.createCell(10, "Datum");
            h1.setFont(PDType1Font.HELVETICA_BOLD);
            h1.setFontSize(9);

            Cell<PDPage> h2 = headerRow.createCell(18, "Disziplin");
            h2.setFont(PDType1Font.HELVETICA_BOLD);
            h2.setFontSize(10);

            Cell<PDPage> h3 = headerRow.createCell(10, "Kaliber");
            h3.setFont(PDType1Font.HELVETICA_BOLD);
            h3.setFontSize(9);

            Cell<PDPage> h4 = headerRow.createCell(15, "Schütze");
            h4.setFont(PDType1Font.HELVETICA_BOLD);
            h4.setFontSize(10);

            Cell<PDPage> h5 = headerRow.createCell(8, "Schüsse");
            h5.setFont(PDType1Font.HELVETICA_BOLD);
            h5.setFontSize(10);

            Cell<PDPage> h6 = headerRow.createCell(15, "Ergebnis");
            h6.setFont(PDType1Font.HELVETICA_BOLD);
            h6.setFontSize(10);

            Cell<PDPage> h7 = headerRow.createCell(14, "Aufseher");
            h7.setFont(PDType1Font.HELVETICA_BOLD);
            h7.setFontSize(10);

            Cell<PDPage> h8 = headerRow.createCell(10, "Signiert am");
            h8.setFont(PDType1Font.HELVETICA_BOLD);
            h8.setFontSize(10);

            Map<String, Set<String>> aufseherToSns = new LinkedHashMap<>();

            for (SchiessnachweisEintragListDTO eintrag : eintraege) {
                Row<PDPage> row = table.createRow(15);
                row.createCell(10, eintrag.getDatum() != null ? eintrag.getDatum().format(DATE_FORMATTER) : "-").setFontSize(8);
                row.createCell(15, eintrag.getSchuetzeVollstaendigerName() != null ? eintrag.getSchuetzeVollstaendigerName() : "-").setFontSize(9);
                row.createCell(18, eintrag.getDisziplinName() != null ? eintrag.getDisziplinName() : "-").setFontSize(9);
                row.createCell(10, eintrag.getKaliber() != null ? eintrag.getKaliber() : "-").setFontSize(8);
                row.createCell(8, eintrag.getAnzahlSchuesse() != null ? eintrag.getAnzahlSchuesse().toString() : "-").setFontSize(9);
                row.createCell(15, eintrag.getErgebnis() != null ? eintrag.getErgebnis() : "-").setFontSize(9);
                String aufName = eintrag.getAufseherVollstaendigerName();
                row.createCell(14, aufName).setFontSize(9);
                if (aufName != null && !aufName.equals("-")) {
                    aufseherToSns.computeIfAbsent(aufName, k -> new LinkedHashSet<>()).add("PKI-signiert");
                }
                row.createCell(10, eintrag.getSigniertAm() != null ? eintrag.getSigniertAm().format(DATETIME_FORMATTER) : "-").setFontSize(8);
            }

            table.draw();

            PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, lastPage,
                    PDPageContentStream.AppendMode.APPEND, true)) {
                float footerY = margin + 150;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, footerY);
                contentStream.showText("PKI-Zertifikatsdetails für die Aufseher:");
                contentStream.endText();
                footerY -= 15;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                if (!aufseherToSns.isEmpty()) {
                    for (Map.Entry<String, Set<String>> entry : aufseherToSns.entrySet()) {
                        String name = entry.getKey();
                        String sns = String.join(", ", entry.getValue());
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, footerY);
                        contentStream.showText("- " + name + " -> SN: " + sns);
                        contentStream.endText();
                        footerY -= 12;
                    }
                } else {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, footerY);
                    contentStream.showText("WARNUNG: Keine PKI-Zertifikate gefunden!");
                    contentStream.endText();
                    footerY -= 12;
                }
                footerY -= 10;
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, footerY);
                contentStream.showText("Anzahl Einträge: " + eintraege.size());
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("Eintragsverwaltungs-PDF für Schießstand {} erstellt ({} Einträge)", schiesstand != null ? schiesstand.getName() : "-", eintraege.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Exportiert Vereinsmitgliedschaften als PDF.
     * Verwendet DTOs für sichere Datenübergabe.
     *
     * @param verein Der Verein (DTO)
     * @param mitgliedschaften Liste der Mitgliedschaften (DTOs)
     * @param von Start-Datum (optional)
     * @param bis End-Datum (optional)
     * @return PDF als Byte-Array
     * @throws IOException bei Fehlern
     */
    public byte[] exportiereVereinsmitgliedschaften(VereinDTO verein, List<VereinsmigliedschaftDTO> mitgliedschaften,
                                                     LocalDate von, LocalDate bis) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;

            // Kopfzeile
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Vereinsmitgliedschaften");
                contentStream.endText();

                yPosition -= 30;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Verein: " + verein.getName());
                contentStream.endText();

                yPosition -= 20;
                if (von != null && bis != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Zeitraum: " + von.format(DATE_FORMATTER) + " - " + bis.format(DATE_FORMATTER));
                    contentStream.endText();
                    yPosition -= 20;
                }

                yPosition -= 10;
            }

            // Tabelle erstellen
            float pageWidth2 = page.getMediaBox().getWidth();
            float tableWidth2 = pageWidth2 - (2 * margin);
            float leftMargin2 = (pageWidth2 - tableWidth2) / 2f;

            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, tableWidth2, leftMargin2, document, page, true, true);

            // Header
            Row<PDPage> headerRow = table.createRow(20);
            Cell<PDPage> cell1 = headerRow.createCell(35, "Name");
            cell1.setFont(PDType1Font.HELVETICA_BOLD);
            cell1.setFontSize(10);

            Cell<PDPage> cell2 = headerRow.createCell(20, "Beitritt");
            cell2.setFont(PDType1Font.HELVETICA_BOLD);
            cell2.setFontSize(10);

            Cell<PDPage> cell3 = headerRow.createCell(20, "Status");
            cell3.setFont(PDType1Font.HELVETICA_BOLD);
            cell3.setFontSize(10);

            Cell<PDPage> cell4 = headerRow.createCell(25, "Rolle");
            cell4.setFont(PDType1Font.HELVETICA_BOLD);
            cell4.setFontSize(10);

            // Datenzeilen (jetzt mit DTOs - direkte Feldaufrufe)
            for (VereinsmigliedschaftDTO mitgliedschaft : mitgliedschaften) {
                Row<PDPage> row = table.createRow(15);
                row.createCell(35, mitgliedschaft.getBenutzerVollstaendigerName()).setFontSize(9);
                row.createCell(20, mitgliedschaft.getBeitrittDatum().format(DATE_FORMATTER)).setFontSize(9);
                row.createCell(20, mitgliedschaft.getStatus().name()).setFontSize(9);

                String rolle;
                if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
                    rolle = "Vereinschef";
                } else if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
                    rolle = "Aufseher";
                } else {
                    rolle = "Mitglied";
                }
                row.createCell(25, rolle).setFontSize(9);
            }

            table.draw();

            // Fußzeile
            PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, lastPage,
                    PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, margin - 20);
                contentStream.showText("Erstellt am: " + LocalDate.now().format(DATE_FORMATTER));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, margin - 30);
                contentStream.showText("Anzahl Mitgliedschaften: " + mitgliedschaften.size());
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("Mitgliedschafts-PDF erstellt für Verein {}", verein.getName());
            return outputStream.toByteArray();
        }
    }
}
