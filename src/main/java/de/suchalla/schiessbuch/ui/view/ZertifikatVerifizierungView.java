package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.service.ZertifikatVerifizierungsService;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

/**
 * Öffentlich zugängliche View zur Verifizierung von Zertifikatsnummern.
 * Ermöglicht es Personen und Behörden nachzuvollziehen, ob ein Zertifikat
 * echt ist und wer der Aufseher war.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "zertifikat-verifizierung", layout = MainLayout.class)
@PageTitle("Zertifikat verifizieren | Digitales Schießbuch")
@AnonymousAllowed
@Slf4j
public class ZertifikatVerifizierungView extends VerticalLayout {

    private final ZertifikatVerifizierungsService verifizierungsService;
    private final TextField seriennummerField;
    private final VerticalLayout ergebnisLayout;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ZertifikatVerifizierungView(ZertifikatVerifizierungsService verifizierungsService) {
        this.verifizierungsService = verifizierungsService;

        setSizeFull();
        setPadding(true);

        // Titel und Beschreibung
        H2 title = new H2("Zertifikat verifizieren");
        Paragraph beschreibung = new Paragraph(
                "Geben Sie die Seriennummer eines Zertifikats ein, um dessen Echtheit zu überprüfen. Diese Funktion steht allen Personen zur Verfügung, um nachzuvollziehen, wer einen Schießnachweis bestätigt hat."
        );
        beschreibung.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Eingabefeld für Seriennummer
        seriennummerField = new TextField("Zertifikat-Seriennummer");
        seriennummerField.setPlaceholder("z.B. 1a2b3c4d5e6f7890...");
        seriennummerField.setWidthFull();
        seriennummerField.setMaxWidth("600px");
        seriennummerField.setPrefixComponent(VaadinIcon.BARCODE.create());

        // Verifizierungs-Button
        Button verifizierenButton = new Button("Verifizieren", VaadinIcon.SEARCH.create());
        verifizierenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        verifizierenButton.addClickListener(e -> verifizieren());

        // Enter-Taste im Textfeld
        seriennummerField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> verifizieren());

        // Ergebnis-Layout
        ergebnisLayout = new VerticalLayout();
        ergebnisLayout.setPadding(false);
        ergebnisLayout.setVisible(false);

        add(title, beschreibung, seriennummerField, verifizierenButton, ergebnisLayout);
    }

    /**
     * Führt die Verifizierung durch.
     */
    private void verifizieren() {
        String seriennummer = seriennummerField.getValue();

        if (seriennummer == null || seriennummer.trim().isEmpty()) {
            Notification.show("Bitte geben Sie eine Seriennummer ein", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            DigitalesZertifikat zertifikat = verifizierungsService.verifiziere(seriennummer.trim());

            if (zertifikat != null) {
                zeigeErgebnis(zertifikat, true);
            } else {
                zeigeErgebnis(null, false);
            }
        } catch (Exception e) {
            log.error("Fehler bei der Zertifikatsverifizierung", e);
            Notification.show("Fehler bei der Verifizierung: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Zeigt das Verifizierungsergebnis an.
     */
    private void zeigeErgebnis(DigitalesZertifikat zertifikat, boolean gueltig) {
        ergebnisLayout.removeAll();

        if (!gueltig || zertifikat == null) {
            // Zertifikat nicht gefunden
            VerticalLayout fehlerBox = new VerticalLayout();
            fehlerBox.setMaxWidth("800px");
            fehlerBox.getStyle()
                    .set("border", "2px solid var(--lumo-error-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("background-color", "var(--lumo-error-color-10pct)")
                    .set("padding", "var(--lumo-space-m)");

            H3 fehlerTitel = new H3("❌ Zertifikat nicht gefunden");
            fehlerTitel.getStyle().set("color", "var(--lumo-error-text-color)");

            Paragraph fehlerText = new Paragraph(
                    "Die eingegebene Seriennummer konnte nicht gefunden werden. " +
                            "Bitte überprüfen Sie die Eingabe oder kontaktieren Sie den Herausgeber des Zertifikats."
            );

            fehlerBox.add(fehlerTitel, fehlerText);
            ergebnisLayout.add(fehlerBox);
        } else {
            // Zertifikat gefunden - Details anzeigen
            VerticalLayout erfolgsBox = new VerticalLayout();
            erfolgsBox.setMaxWidth("800px");

            // Status-abhängige Gestaltung
            boolean istAktivGueltig = zertifikat.istGueltig();
            String borderColor = istAktivGueltig ? "var(--lumo-success-color)" : "var(--lumo-error-color)";
            String bgColor = istAktivGueltig ? "var(--lumo-success-color-10pct)" : "var(--lumo-error-color-10pct)";

            erfolgsBox.getStyle()
                    .set("border", "2px solid " + borderColor)
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("background-color", bgColor)
                    .set("padding", "var(--lumo-space-m)");

            String statusIcon = istAktivGueltig ? "✅" : "⚠️";
            String statusText = istAktivGueltig ? "Gültiges Zertifikat" : "Zertifikat ungültig/widerrufen";

            H3 statusTitel = new H3(statusIcon + " " + statusText);

            // Zertifikatsdetails
            VerticalLayout details = new VerticalLayout();
            details.setPadding(false);
            details.setSpacing(false);

            details.add(createDetailRow("Typ:", getZertifikatsTypBeschreibung(zertifikat.getZertifikatsTyp())));
            details.add(createDetailRow("Seriennummer:", zertifikat.getSeriennummer()));

            // Aufseher-Informationen (falls vorhanden)
            if ("AUFSEHER".equals(zertifikat.getZertifikatsTyp()) && zertifikat.getBenutzer() != null) {
                details.add(createDetailRow("Aufseher:", zertifikat.getBenutzer().getVollstaendigerName()));
                details.add(createDetailRow("E-Mail:", zertifikat.getBenutzer().getEmail()));
            }

            // Vereinsinformationen (falls vorhanden)
            if (zertifikat.getVerein() != null) {
                details.add(createDetailRow("Verein:", zertifikat.getVerein().getName()));
                if (zertifikat.getVerein().getAdresse() != null && !zertifikat.getVerein().getAdresse().isEmpty()) {
                    details.add(createDetailRow("Vereinsadresse:", zertifikat.getVerein().getAdresse()));
                }
            }

            details.add(createDetailRow("Gültig von:", zertifikat.getGueltigAb().format(dateFormatter) + " Uhr"));
            details.add(createDetailRow("Gültig bis:", zertifikat.getGueltigBis().format(dateFormatter) + " Uhr"));

            // Widerruf-Informationen
            if (Boolean.TRUE.equals(zertifikat.getWiderrufen())) {
                details.add(createDetailRow("Widerrufen am:",
                        zertifikat.getWiderrufenAm() != null
                                ? zertifikat.getWiderrufenAm().format(dateFormatter) + " Uhr"
                                : "Unbekannt"));
                if (zertifikat.getWiderrufsGrund() != null && !zertifikat.getWiderrufsGrund().isEmpty()) {
                    details.add(createDetailRow("Widerrufsgrund:", zertifikat.getWiderrufsGrund()));
                }
            }

            // Subject DN
            details.add(createDetailRow("Subject DN:", zertifikat.getSubjectDN()));
            details.add(createDetailRow("Issuer DN:", zertifikat.getIssuerDN()));

            erfolgsBox.add(statusTitel, details);

            // Hinweis für Behörden
            if ("AUFSEHER".equals(zertifikat.getZertifikatsTyp())) {
                Paragraph behoerdenHinweis = new Paragraph(
                        "ℹ️ Hinweis für Behörden: Dieses Zertifikat wurde verwendet, um Schießnachweise " +
                                "digital zu signieren. Die oben genannte Person hat als Aufseher die Einträge bestätigt."
                );
                behoerdenHinweis.getStyle()
                        .set("margin-top", "var(--lumo-space-m)")
                        .set("padding", "var(--lumo-space-s)")
                        .set("background-color", "var(--lumo-contrast-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-s)")
                        .set("font-style", "italic");
                erfolgsBox.add(behoerdenHinweis);
            }

            ergebnisLayout.add(erfolgsBox);
        }

        ergebnisLayout.setVisible(true);
    }

    /**
     * Erstellt eine Detailzeile mit Label und Wert.
     */
    private VerticalLayout createDetailRow(String label, String value) {
        VerticalLayout row = new VerticalLayout();
        row.setPadding(false);
        row.setSpacing(false);
        row.getStyle()
                .set("margin-bottom", "var(--lumo-space-xs)");

        Paragraph labelPara = new Paragraph(label);
        labelPara.getStyle()
                .set("font-weight", "bold")
                .set("margin", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Paragraph valuePara = new Paragraph(value != null ? value : "Nicht verfügbar");
        valuePara.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-m)");

        row.add(labelPara, valuePara);
        return row;
    }

    /**
     * Gibt eine lesbare Beschreibung des Zertifikatstyps zurück.
     */
    private String getZertifikatsTypBeschreibung(String typ) {
        return switch (typ) {
            case "ROOT" -> "Root CA (Stammzertifikat)";
            case "VEREIN" -> "Vereinszertifikat";
            case "AUFSEHER" -> "Aufseher-Zertifikat";
            default -> typ;
        };
    }
}
