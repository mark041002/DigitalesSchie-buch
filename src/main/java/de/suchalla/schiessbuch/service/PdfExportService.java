package de.suchalla.schiessbuch.service;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
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

/**
 * Service für PDF-Export von Schießnachweisen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Exportiert Schießnachweise als PDF.
     *
     * @param schuetze Der Schütze
     * @param eintraege Liste der Einträge
     * @param von Start-Datum
     * @param bis End-Datum
     * @return PDF als Byte-Array
     * @throws IOException bei Fehlern
     */
    public byte[] exportiereSchiessnachweise(Benutzer schuetze, List<SchiessnachweisEintrag> eintraege,
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
                contentStream.showText("Digitales Schießbuch");
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
                contentStream.showText("Zeitraum: " + von.format(DATE_FORMATTER) + " - " + bis.format(DATE_FORMATTER));
                contentStream.endText();

                yPosition -= 30;
            }

            // Tabelle erstellen
            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, page.getMediaBox().getWidth() - margin, margin, document, page, true, true);

            // Header
            Row<PDPage> headerRow = table.createRow(20);
            Cell<PDPage> cell1 = headerRow.createCell(15, "Datum");
            cell1.setFont(PDType1Font.HELVETICA_BOLD);
            cell1.setFontSize(10);

            Cell<PDPage> cell2 = headerRow.createCell(20, "Disziplin");
            cell2.setFont(PDType1Font.HELVETICA_BOLD);
            cell2.setFontSize(10);

            Cell<PDPage> cell3 = headerRow.createCell(15, "Kaliber");
            cell3.setFont(PDType1Font.HELVETICA_BOLD);
            cell3.setFontSize(10);

            Cell<PDPage> cell4 = headerRow.createCell(15, "Waffenart");
            cell4.setFont(PDType1Font.HELVETICA_BOLD);
            cell4.setFontSize(10);

            Cell<PDPage> cell5 = headerRow.createCell(20, "Schießstand");
            cell5.setFont(PDType1Font.HELVETICA_BOLD);
            cell5.setFontSize(10);

            Cell<PDPage> cell6 = headerRow.createCell(18, "Aufseher");
            cell6.setFont(PDType1Font.HELVETICA_BOLD);
            cell6.setFontSize(10);

            // Datenzeilen
            for (SchiessnachweisEintrag eintrag : eintraege) {
                Row<PDPage> row = table.createRow(15);

                row.createCell(12, eintrag.getDatum().format(DATE_FORMATTER)).setFontSize(9);
                row.createCell(20, eintrag.getDisziplin().getName()).setFontSize(9);
                row.createCell(15, eintrag.getKaliber()).setFontSize(9);
                row.createCell(15, eintrag.getWaffenart()).setFontSize(9);
                row.createCell(20, eintrag.getSchiesstand().getName()).setFontSize(9);

                String aufseherName = eintrag.getAufseher() != null ?
                        eintrag.getAufseher().getVollstaendigerName() : "-";
                row.createCell(18, aufseherName).setFontSize(9);
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
                contentStream.showText("Anzahl Einträge: " + eintraege.size());
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("PDF für {} mit {} Einträgen erstellt", schuetze.getEmail(), eintraege.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Exportiert Schießstand-Nachweise als PDF.
     *
     * @param schiesstandName Name des Schießstands
     * @param schuetzeName Name des Schützen
     * @param eintraege Liste der Einträge
     * @param von Start-Datum
     * @param bis End-Datum
     * @return PDF als Byte-Array
     * @throws IOException bei Fehlern
     */
    public byte[] exportiereSchiesstandNachweise(String schiesstandName, String schuetzeName,
                                                  List<SchiessnachweisEintrag> eintraege,
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
                contentStream.showText("Schießstand-Nachweis");
                contentStream.endText();

                yPosition -= 30;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Schießstand: " + schiesstandName);
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Schütze: " + schuetzeName);
                contentStream.endText();

                yPosition -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Zeitraum: " + von.format(DATE_FORMATTER) + " - " + bis.format(DATE_FORMATTER));
                contentStream.endText();

                yPosition -= 30;
            }

            // Tabelle
            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, page.getMediaBox().getWidth() - margin, margin, document, page, true, true);

            // Header
            Row<PDPage> headerRow = table.createRow(20);
            Cell<PDPage> cell1 = headerRow.createCell(15, "Datum");
            cell1.setFont(PDType1Font.HELVETICA_BOLD);
            cell1.setFontSize(10);

            Cell<PDPage> cell2 = headerRow.createCell(25, "Disziplin");
            cell2.setFont(PDType1Font.HELVETICA_BOLD);
            cell2.setFontSize(10);

            Cell<PDPage> cell3 = headerRow.createCell(15, "Kaliber");
            cell3.setFont(PDType1Font.HELVETICA_BOLD);
            cell3.setFontSize(10);

            Cell<PDPage> cell4 = headerRow.createCell(15, "Waffenart");
            cell4.setFont(PDType1Font.HELVETICA_BOLD);
            cell4.setFontSize(10);

            Cell<PDPage> cell5 = headerRow.createCell(10, "Schüsse");
            cell5.setFont(PDType1Font.HELVETICA_BOLD);
            cell5.setFontSize(10);

            Cell<PDPage> cell6 = headerRow.createCell(20, "Status");
            cell6.setFont(PDType1Font.HELVETICA_BOLD);
            cell6.setFontSize(10);

            // Datenzeilen
            for (SchiessnachweisEintrag eintrag : eintraege) {
                Row<PDPage> row = table.createRow(15);
                row.createCell(15, eintrag.getDatum().format(DATE_FORMATTER)).setFontSize(9);
                row.createCell(25, eintrag.getDisziplin().getName()).setFontSize(9);
                row.createCell(15, eintrag.getKaliber()).setFontSize(9);
                row.createCell(15, eintrag.getWaffenart()).setFontSize(9);
                row.createCell(10, eintrag.getAnzahlSchuesse() != null ?
                        eintrag.getAnzahlSchuesse().toString() : "-").setFontSize(9);
                row.createCell(20, getStatusText(eintrag.getStatus())).setFontSize(9);
            }

            table.draw();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("Schießstand-PDF erstellt mit {} Einträgen", eintraege.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Exportiert Vereinsmitgliedschaften als PDF.
     *
     * @param verein Der Verein
     * @param mitgliedschaften Liste der Mitgliedschaften
     * @param von Start-Datum (optional)
     * @param bis End-Datum (optional)
     * @return PDF als Byte-Array
     * @throws IOException bei Fehlern
     */
    public byte[] exportiereVereinsmitgliedschaften(Verein verein, List<Vereinsmitgliedschaft> mitgliedschaften,
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
            BaseTable table = new BaseTable(yPosition, yPosition - margin,
                    margin, page.getMediaBox().getWidth() - margin, margin, document, page, true, true);

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

            // Datenzeilen
            for (Vereinsmitgliedschaft mitgliedschaft : mitgliedschaften) {
                Row<PDPage> row = table.createRow(15);
                row.createCell(35, mitgliedschaft.getBenutzer().getVollstaendigerName()).setFontSize(9);
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

    private String getStatusText(EintragStatus status) {
        return switch (status) {
            case SIGNIERT -> "Signiert";
            case OFFEN -> "Offen";
            case UNSIGNIERT -> "Unsigniert";
            case ABGELEHNT -> "Abgelehnt";
        };
    }
}
