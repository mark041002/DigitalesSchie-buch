package de.suchalla.schiessbuch.ui.view.oeffentlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import de.suchalla.schiessbuch.ui.view.MainLayout;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final DatePicker pruefDatumField;
    private final VerticalLayout ergebnisLayout;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ZertifikatVerifizierungView(ZertifikatVerifizierungsService verifizierungsService) {
        this.verifizierungsService = verifizierungsService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        getElement().getThemeList().add("zertifikat-verifizierung-view");

        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Zertifikat verifizieren");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");

        // Icon hinzufügen
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.addClassName("info-icon");
        infoBox.add(infoIcon);

        Paragraph beschreibung = new Paragraph(
                "Geben Sie die Seriennummer eines Zertifikats und optional ein Datum ein, um zu prüfen, " +
                        "ob das Zertifikat zu diesem Zeitpunkt gültig war oder ist. Diese Funktion steht allen " +
                        "Personen zur Verfügung, um nachzuvollziehen, wer einen Schießnachweis bestätigt hat."
        );
        beschreibung.addClassName("info-description");
        infoBox.add(beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        // Eingabefeld für Seriennummer
        seriennummerField = new TextField("Zertifikat-Seriennummer");
        seriennummerField.setPlaceholder("z.B. 1a2b3c4d5e6f7890...");
        seriennummerField.setWidthFull();
        seriennummerField.setHelperText("Die vollständige Seriennummer des Zertifikats");

        // Datumspicker für Prüfzeitpunkt
        pruefDatumField = new DatePicker("Prüfzeitpunkt (optional)");
        pruefDatumField.setPlaceholder("TT.MM.JJJJ");
        pruefDatumField.setWidthFull();
        pruefDatumField.setValue(LocalDate.now());
        pruefDatumField.setPrefixComponent(VaadinIcon.CALENDAR.create());
        pruefDatumField.setHelperText("Datum, zu dem die Gültigkeit geprüft werden soll (Standard: heute)");

        // Verifizierungs-Button
        Button verifizierenButton = new Button("Verifizieren", VaadinIcon.SEARCH.create());
        verifizierenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        verifizierenButton.addClickListener(e -> verifizieren());
        verifizierenButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Enter-Taste im Textfeld
        seriennummerField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> verifizieren());

        formContainer.add(seriennummerField, pruefDatumField, verifizierenButton);
        contentWrapper.add(formContainer);

        // Ergebnis-Layout
        ergebnisLayout = new VerticalLayout();
        ergebnisLayout.setPadding(false);
        ergebnisLayout.setVisible(false);

        contentWrapper.add(ergebnisLayout);
        add(contentWrapper);
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

        // Prüfdatum ermitteln (Standard: heute)
        LocalDate pruefDatum = pruefDatumField.getValue();
        if (pruefDatum == null) {
            pruefDatum = LocalDate.now();
        }
        LocalDateTime pruefZeitpunkt = pruefDatum.atStartOfDay();

        try {
            DigitalesZertifikat zertifikat = verifizierungsService.verifiziere(seriennummer.trim());

            if (zertifikat != null) {
                // Prüfe Gültigkeit zum angegebenen Zeitpunkt
                boolean warGueltig = istGueltigZuZeitpunkt(zertifikat, pruefZeitpunkt);
                zeigeErgebnis(zertifikat, true, warGueltig, pruefZeitpunkt);
            } else {
                zeigeErgebnis(null, false, false, pruefZeitpunkt);
            }
        } catch (Exception e) {
            log.error("Fehler bei der Zertifikatsverifizierung", e);
            Notification.show("Fehler bei der Verifizierung: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Prüft, ob das Zertifikat zu einem bestimmten Zeitpunkt gültig war.
     */
    private boolean istGueltigZuZeitpunkt(DigitalesZertifikat zertifikat, LocalDateTime zeitpunkt) {
        // Prüfe ob Zeitpunkt im Gültigkeitszeitraum liegt
        boolean imZeitraum = !zeitpunkt.isBefore(zertifikat.getGueltigSeit()) &&
                             !zeitpunkt.isAfter(zertifikat.getGueltigBis());

        // Prüfe ob nicht widerrufen oder erst nach dem Prüfzeitpunkt widerrufen wurde
        boolean nichtWiderrufen = !Boolean.TRUE.equals(zertifikat.getWiderrufen());
        if (!nichtWiderrufen && zertifikat.getWiderrufenAm() != null) {
            // War zum Prüfzeitpunkt noch nicht widerrufen
            nichtWiderrufen = zeitpunkt.isBefore(zertifikat.getWiderrufenAm());
        }

        return imZeitraum && nichtWiderrufen;
    }

    /**
     * Zeigt das Verifizierungsergebnis an.
     */
    private void zeigeErgebnis(DigitalesZertifikat zertifikat, boolean gefunden,
                               boolean gueltigZumZeitpunkt, LocalDateTime pruefZeitpunkt) {
        ergebnisLayout.removeAll();

        if (!gefunden || zertifikat == null) {
            // Zertifikat nicht gefunden
            Div fehlerBox = new Div();
            fehlerBox.setWidthFull();
            fehlerBox.getStyle()
                    .set("border", "2px solid var(--lumo-error-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("background-color", "var(--lumo-error-color-10pct)")
                    .set("padding", "var(--lumo-space-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)");

            H3 fehlerTitel = new H3("❌ Zertifikat nicht gefunden");
            fehlerTitel.getStyle()
                    .set("color", "var(--lumo-error-text-color)")
                    .set("margin-top", "0");

            Paragraph fehlerText = new Paragraph(
                    "Die eingegebene Seriennummer konnte nicht gefunden werden. " +
                            "Bitte überprüfen Sie die Eingabe oder kontaktieren Sie den Herausgeber des Zertifikats."
            );

            fehlerBox.add(fehlerTitel, fehlerText);
            ergebnisLayout.add(fehlerBox);
        } else {
            // Zertifikat gefunden - Details anzeigen
            Div erfolgsBox = new Div();
            erfolgsBox.setWidthFull();

            // Status-abhängige Gestaltung
            boolean istAktivGueltig = zertifikat.istGueltig();
            String borderColor = gueltigZumZeitpunkt ? "var(--lumo-success-color)" : "var(--lumo-error-color)";
            String bgColor = gueltigZumZeitpunkt ? "var(--lumo-success-color-10pct)" : "var(--lumo-error-color-10pct)";

            erfolgsBox.getStyle()
                    .set("border", "2px solid " + borderColor)
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("background-color", bgColor)
                    .set("padding", "var(--lumo-space-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)");

            String statusText;

            if (gueltigZumZeitpunkt) {
                statusText = "Zertifikat war/ist zum Prüfzeitpunkt gültig";
            } else if (!istAktivGueltig) {
                statusText = "Zertifikat wurde widerrufen oder ist abgelaufen";
            } else {
                statusText = "Zertifikat war zum Prüfzeitpunkt noch nicht gültig";
            }

            H3 statusTitel = new H3(statusText);
            statusTitel.getStyle()
                    .set("margin-top", "0")
                    .set("color", gueltigZumZeitpunkt ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");

            // Prüfzeitpunkt anzeigen
            Div pruefzeitDiv = new Div();
            pruefzeitDiv.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("margin-bottom", "var(--lumo-space-m)");

            Span pruefzeitLabel = new Span("Geprüft zum Zeitpunkt: ");
            pruefzeitLabel.getStyle().set("font-weight", "600");

            Span pruefzeitWert = new Span(pruefZeitpunkt.format(dateFormatter) + " Uhr");
            pruefzeitDiv.add(pruefzeitLabel, pruefzeitWert);

            // Zertifikatsdetails
            Div details = new Div();
            details.getStyle()
                    .set("display", "grid")
                    .set("gap", "var(--lumo-space-m)")
                    .set("margin-top", "var(--lumo-space-m)");

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

            details.add(createDetailRow("Gültig von:", zertifikat.getGueltigSeit().format(dateFormatter) + " Uhr"));
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

            erfolgsBox.add(statusTitel, pruefzeitDiv, details);

            // Hinweis für Behörden
            if ("AUFSEHER".equals(zertifikat.getZertifikatsTyp())) {
                Div behoerdenHinweis = new Div();
                behoerdenHinweis.getStyle()
                        .set("margin-top", "var(--lumo-space-m)")
                        .set("padding", "var(--lumo-space-m)")
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("border-left", "4px solid var(--lumo-primary-color)")
                        .set("border-radius", "var(--lumo-border-radius-s)");

                Paragraph hinweisText = new Paragraph(
                        "ℹ️ Hinweis für Behörden: Dieses Zertifikat wurde verwendet, um Schießnachweise " +
                                "digital zu signieren. Die oben genannte Person hat als Aufseher die Einträge bestätigt."
                );
                hinweisText.getStyle()
                        .set("margin", "0")
                        .set("font-style", "italic");
                behoerdenHinweis.add(hinweisText);
                erfolgsBox.add(behoerdenHinweis);
            }

            ergebnisLayout.add(erfolgsBox);
        }

        ergebnisLayout.setVisible(true);
    }

    /**
     * Erstellt eine Detailzeile mit Label und Wert.
     */
    private Div createDetailRow(String label, String value) {
        Div row = new Div();
        row.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-s)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-xs)");

        Span valueSpan = new Span(value != null ? value : "Nicht verfügbar");
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("word-wrap", "break-word")
                .set("overflow-wrap", "break-word");

        row.add(labelSpan, valueSpan);
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
