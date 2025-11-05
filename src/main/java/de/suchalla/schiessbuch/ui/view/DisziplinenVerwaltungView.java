package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * View für Disziplinverwaltung (nur für Admins).
 * Mit Schützenbund-Auswahl und CSV-Import.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/disziplinen", layout = MainLayout.class)
@PageTitle("Disziplinen | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class DisziplinenVerwaltungView extends VerticalLayout implements BeforeEnterObserver {

    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Disziplin> grid = new Grid<>(Disziplin.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextArea beschreibungField = new TextArea("Beschreibung");

    private VerticalLayout formularBereich;
    private Verband ausgewaehlterVerband;

    public DisziplinenVerwaltungView(DisziplinService disziplinService, VerbandService verbandService) {
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;

        setSpacing(true);
        setPadding(true);

        createContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Überprüfe, ob ein Verband-Parameter übergeben wurde
        Optional<String> verbandIdParam = event.getLocation().getQueryParameters()
                .getParameters().getOrDefault("verbandId", List.of()).stream().findFirst();

        if (verbandIdParam.isPresent()) {
            try {
                Long verbandId = Long.parseLong(verbandIdParam.get());
                Optional<Verband> verbandOptional = verbandService.findeVerband(verbandId);
                if (verbandOptional.isPresent()) {
                    ausgewaehlterVerband = verbandOptional.get();
                    updateFormularBereich();
                } else {
                    Notification.show("Verband nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (NumberFormatException e) {
                Notification.show("Ungültige Verband-ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            Notification.show("Bitte wählen Sie einen Verband aus der Verbände-Übersicht aus")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    private void createContent() {
        if (ausgewaehlterVerband != null) {
            add(new H2("Disziplinverwaltung - " + ausgewaehlterVerband.getName()));
        } else {
            add(new H2("Disziplinverwaltung"));
        }

        // Formular-Bereich
        formularBereich = new VerticalLayout();
        formularBereich.setSpacing(true);
        formularBereich.setPadding(false);
        add(formularBereich);
    }

    private void updateFormularBereich() {
        formularBereich.removeAll();

        if (ausgewaehlterVerband == null) {
            formularBereich.setVisible(false);
            return;
        }

        formularBereich.setVisible(true);

        // Überschrift mit ausgewähltem Verband
        formularBereich.add(new H3("Disziplinen für: " + ausgewaehlterVerband.getName()));

        // Manuelle Eingabe
        nameField.setRequired(true);
        nameField.clear();
        beschreibungField.setMaxLength(1000);
        beschreibungField.clear();

        FormLayout formLayout = new FormLayout(nameField, beschreibungField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formularBereich.add(formLayout, speichernButton);

        // CSV-Import
        formularBereich.add(new H3("CSV-Import"));

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv", "text/csv");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        upload.setDropLabel(new com.vaadin.flow.component.html.Span("CSV-Datei hier ablegen"));
        upload.setUploadButton(new Button("CSV hochladen"));

        upload.addSucceededListener(event -> {
            try {
                importiereDisziplinenAusCsv(buffer.getInputStream());
            } catch (Exception ex) {
                Notification.show("Fehler beim Import: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        formularBereich.add(upload);

        // Hinweis für CSV-Format
        com.vaadin.flow.component.html.Div hinweis = new com.vaadin.flow.component.html.Div();
        hinweis.setText("CSV-Format: Name;Beschreibung (eine Disziplin pro Zeile, erste Zeile wird als Header übersprungen)");
        hinweis.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)");
        formularBereich.add(hinweis);

        // Grid
        grid.removeAllColumns();
        grid.addColumn(Disziplin::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Disziplin::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Disziplin::getBeschreibung).setHeader("Beschreibung").setAutoWidth(true);

        // Aktionen-Spalte mit Löschen-Button
        grid.addComponentColumn(disziplin -> {
            Button loeschenButton = new Button("Löschen");
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.addClickListener(e -> zeigeLoeschDialog(disziplin));
            return loeschenButton;
        }).setHeader("Aktionen").setWidth("120px").setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );

        formularBereich.add(grid);
        updateGrid();
    }

    private void speichereDisziplin() {
        if (ausgewaehlterVerband == null) {
            Notification.show("Bitte wählen Sie zuerst einen Schützenbund aus")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (nameField.isEmpty()) {
            Notification.show("Name ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Disziplin disziplin = Disziplin.builder()
                    .name(nameField.getValue())
                    .verband(ausgewaehlterVerband)
                    .beschreibung(beschreibungField.getValue())
                    .build();

            disziplinService.erstelleDisziplin(disziplin);
            Notification.show("Disziplin erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            beschreibungField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void importiereDisziplinenAusCsv(InputStream inputStream) {
        if (ausgewaehlterVerband == null) {
            Notification.show("Bitte wählen Sie zuerst einen Schützenbund aus")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<Disziplin> importierteDisziplinen = new ArrayList<>();
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Erste Zeile als Header überspringen
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Leere Zeilen überspringen
                if (line.trim().isEmpty()) {
                    continue;
                }

                // CSV-Zeile parsen (Format: Name;Beschreibung)
                String[] parts = line.split(";", -1);
                if (parts.length >= 1) {
                    String name = parts[0].trim();
                    String beschreibung = parts.length > 1 ? parts[1].trim() : "";

                    if (!name.isEmpty()) {
                        Disziplin disziplin = Disziplin.builder()
                                .name(name)
                                .verband(ausgewaehlterVerband)
                                .beschreibung(beschreibung)
                                .build();

                        Disziplin gespeichert = disziplinService.erstelleDisziplin(disziplin);
                        importierteDisziplinen.add(gespeichert);
                    }
                }
            }

            Notification.show(importierteDisziplinen.size() + " Disziplin(en) erfolgreich importiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler beim CSV-Import: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(Disziplin disziplin) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Disziplin löschen");
        dialog.setText("Sind Sie sicher, dass Sie die Disziplin \"" + disziplin.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> loescheDisziplin(disziplin));
        dialog.open();
    }

    private void loescheDisziplin(Disziplin disziplin) {
        try {
            disziplinService.loescheDisziplin(disziplin.getId());
            Notification.show("Disziplin erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        if (ausgewaehlterVerband != null) {
            grid.setItems(disziplinService.findeDisziplinenVonVerband(ausgewaehlterVerband));
        } else {
            grid.setItems(new ArrayList<>());
        }
    }
}
