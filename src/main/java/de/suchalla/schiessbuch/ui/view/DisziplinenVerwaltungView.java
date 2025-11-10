package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.component.UI;
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

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

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
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        Button backButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("color", "white");
        backButton.addClickListener(e -> UI.getCurrent().navigate(VerbaendeVerwaltungView.class));

        H2 title = new H2("Disziplinverwaltung");
        title.getStyle().set("margin", "0");
        title.getStyle().set("margin-left", "var(--lumo-space-m)");

        headerContent.add(backButton, title);
        header.add(headerContent);
        contentWrapper.add(header);

        // Info-Box
        Div infoBox = new Div();
        infoBox.addClassName("info-box");

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph description = new Paragraph(
                "Verwalten Sie hier die Disziplinen für die ausgewählten Verbände. Sie können Disziplinen manuell hinzufügen oder per CSV-Import importieren."
        );

        infoBox.add(infoIcon, description);
        contentWrapper.add(infoBox);

        // Formular-Bereich
        formularBereich = new VerticalLayout();
        formularBereich.setSpacing(false);
        formularBereich.setPadding(false);
        contentWrapper.add(formularBereich);

        add(contentWrapper);
    }

    private void updateFormularBereich() {
        formularBereich.removeAll();
        formularBereich.setSpacing(false);
        formularBereich.setPadding(false);

        if (ausgewaehlterVerband == null) {
            formularBereich.setVisible(false);
            return;
        }

        formularBereich.setVisible(true);

        // ===== MANUELLE EINGABE CONTAINER =====
        Div manuelleEingabeContainer = new Div();
        manuelleEingabeContainer.getStyle().set("background", "white");
        manuelleEingabeContainer.getStyle().set("padding", "var(--lumo-space-m)");
        manuelleEingabeContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        manuelleEingabeContainer.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        manuelleEingabeContainer.getStyle().set("box-sizing", "border-box");
        manuelleEingabeContainer.setWidthFull();

        H3 manuellerTitel = new H3("Disziplin manuell hinzufügen");
        manuellerTitel.getStyle().set("margin-top", "0");

        // Manuelle Eingabe
        nameField.setRequired(true);
        nameField.clear();
        nameField.setWidthFull();
        beschreibungField.setMaxLength(1000);
        beschreibungField.clear();
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("120px");

        FormLayout formLayout = new FormLayout(nameField, beschreibungField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setColspan(beschreibungField, 1);

        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.setIcon(VaadinIcon.PLUS.create());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        manuelleEingabeContainer.add(manuellerTitel, formLayout, speichernButton);
        formularBereich.add(manuelleEingabeContainer);

        // ===== CSV-IMPORT CONTAINER =====
        Div csvImportContainer = new Div();
        csvImportContainer.getStyle().set("background", "white");
        csvImportContainer.getStyle().set("padding", "var(--lumo-space-m)");
        csvImportContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        csvImportContainer.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        csvImportContainer.getStyle().set("box-sizing", "border-box");
        csvImportContainer.setWidthFull();

        H3 csvTitel = new H3("Disziplinen per CSV importieren");
        csvTitel.getStyle().set("margin-top", "0");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv", "text/csv");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        upload.setDropLabel(new com.vaadin.flow.component.html.Span("CSV-Datei hier ablegen"));
        upload.setUploadButton(new Button("CSV hochladen", VaadinIcon.UPLOAD.create()));

        upload.addSucceededListener(event -> {
            try {
                importiereDisziplinenAusCsv(buffer.getInputStream());
            } catch (Exception ex) {
                Notification.show("Fehler beim Import: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Hinweis für CSV-Format
        com.vaadin.flow.component.html.Div hinweis = new com.vaadin.flow.component.html.Div();
        hinweis.setText("CSV-Format: Name;Beschreibung (eine Disziplin pro Zeile, erste Zeile wird als Header übersprungen)");
        hinweis.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("margin-top", "var(--lumo-space-s)");

        csvImportContainer.add(csvTitel, upload, hinweis);
        formularBereich.add(csvImportContainer);

        // ===== GRID CONTAINER =====
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid
        grid.removeAllColumns();
        grid.addClassName("rounded-grid");

        grid.addColumn(Disziplin::getId)
                .setHeader("ID")
                .setWidth("60px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Disziplin::getName).setHeader("Name").setAutoWidth(true);
        grid.addColumn(Disziplin::getBeschreibung).setHeader("Beschreibung").setAutoWidth(true);

        // Aktionen-Spalte mit Löschen-Button
        grid.addComponentColumn(disziplin -> {
            Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.addClickListener(e -> zeigeLoeschDialog(disziplin));
            return loeschenButton;
        }).setHeader("Aktionen").setWidth("150px").setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        gridContainer.add(grid);
        formularBereich.add(gridContainer);
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
