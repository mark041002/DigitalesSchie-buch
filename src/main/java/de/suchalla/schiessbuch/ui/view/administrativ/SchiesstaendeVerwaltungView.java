package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.SchiesstandService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Schießstandverwaltung (nur für Admins).
 */
@Route(value = "admin/schiesstaende", layout = MainLayout.class)
@PageTitle("Schießstände | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class SchiesstaendeVerwaltungView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final SchiesstandService schiesstandService;
    private final VerbandService verbandService;
    private final SchiessnachweisService schiessnachweisService;
    private final Grid<Schiesstand> grid = new Grid<>(Schiesstand.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final ComboBox<SchiesstandTyp> typComboBox = new ComboBox<>("Typ");
    private final ComboBox<Verein> vereinComboBox = new ComboBox<>("Verein");
    private final ComboBox<Benutzer> inhaberComboBox = new ComboBox<>("Inhaber");
    public SchiesstaendeVerwaltungView(BenutzerService benutzerService,
                                      SchiesstandService schiesstandService,
                                      VerbandService verbandService,
                                      SchiessnachweisService schiessnachweisService) {
        this.benutzerService = benutzerService;
        this.schiesstandService = schiesstandService;
        this.verbandService = verbandService;
        this.schiessnachweisService = schiessnachweisService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        // Header-Bereich
        Div header = ViewComponentHelper.createGradientHeader("Schießstandverwaltung");
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = ViewComponentHelper.createInfoBox(
                "Erstellen und verwalten Sie Schießstände im System. Schießstände können gewerblich oder vereinsgebunden sein."
        );
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = ViewComponentHelper.createFormContainer();

        H3 erstellenTitle = new H3("Neuen Schießstand erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        // Formular
        nameField.setRequired(true);

        // Nur die beiden Typen erlauben
        typComboBox.setItems(SchiesstandTyp.GEWERBLICH, SchiesstandTyp.VEREINSGEBUNDEN);
        typComboBox.setItemLabelGenerator(this::getTypText);
        typComboBox.setRequired(true);
        typComboBox.addValueChangeListener(e -> {
            if (e.getValue() == SchiesstandTyp.VEREINSGEBUNDEN) {
                vereinComboBox.setVisible(true);
                vereinComboBox.setRequired(true);
                inhaberComboBox.setVisible(false);
                inhaberComboBox.setRequired(false);
                inhaberComboBox.clear();
            } else if (e.getValue() == SchiesstandTyp.GEWERBLICH) {
                vereinComboBox.setVisible(false);
                vereinComboBox.setRequired(false);
                inhaberComboBox.setVisible(true);
                inhaberComboBox.setRequired(true);
                // Alle Benutzer als mögliche gewerbliche Inhaber
                List<Benutzer> inhaber = benutzerService.findAlleBenutzerEntities();
                inhaberComboBox.setItems(inhaber);
            } else {
                vereinComboBox.setVisible(false);
                inhaberComboBox.setVisible(false);
            }
        });

        inhaberComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        inhaberComboBox.setVisible(false);
        inhaberComboBox.setRequired(false);
        inhaberComboBox.setAllowCustomValue(false);
        inhaberComboBox.setClearButtonVisible(true);
        inhaberComboBox.setPlaceholder("Namen suchen...");
        inhaberComboBox.addCustomValueSetListener(event ->
                Notification.show("Bitte wählen Sie einen Benutzer aus der Liste.", 3000, Notification.Position.MIDDLE)
        );
        vereinComboBox.setItems(verbandService.findeAlleVereineEntities());
        vereinComboBox.setItemLabelGenerator(Verein::getName);
        vereinComboBox.setVisible(false);

        FormLayout formLayout = ViewComponentHelper.createResponsiveFormLayout();
        formLayout.add(nameField, typComboBox, vereinComboBox, inhaberComboBox, adresseField);

        Button speichernButton = new Button("Schießstand erstellen", e -> speichereSchiesstand());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = ViewComponentHelper.createGridContainer();

        // Empty State Message
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Noch keine Schießstände vorhanden.", VaadinIcon.BUILDING);
        emptyStateMessage.setVisible(false);

        // Grid
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Schiesstand::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0)
                .setResizable(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(Schiesstand::getName)
                .setHeader("Name")
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(schiesstand -> getTypText(schiesstand.getTyp()))
                .setHeader("Typ")
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(schiesstand -> schiesstand.getVerein() != null ?
                        schiesstand.getVerein().getName() : "-")
                .setHeader("Verein")
                .setFlexGrow(1)
                .setResizable(true);

                

        grid.addColumn(schiesstand -> {
                    if (schiesstand.getAufseher() != null) {
                        return schiesstand.getAufseher().getVollstaendigerName();
                    }
                    return "-";
                })
                .setHeader("Vereinschef / Aufseher")
                .setFlexGrow(1)
                .setResizable(true);


        // Aktionen-Spalte mit Details, Zuweisen und Löschen Buttons
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setFlexGrow(0)
                .setResizable(false);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private String getTypText(SchiesstandTyp typ) {
        if (typ == null) return "-";
        return typ == SchiesstandTyp.GEWERBLICH ? "Gewerblich" : "Vereinsgebunden";
    }

    private HorizontalLayout createActionButtons(Schiesstand schiesstand) {
        Button detailsButton = new Button("Details", VaadinIcon.SEARCH.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> zeigeDetailsDialog(schiesstand));

        Button eintraegeButton = new Button("Einträge", VaadinIcon.RECORDS.create());
        eintraegeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        eintraegeButton.addClickListener(e -> zeigeEintraegeDialog(schiesstand));

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(schiesstand));

        HorizontalLayout actions = new HorizontalLayout();
        actions.add(detailsButton, eintraegeButton, loeschenButton);
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.getStyle().set("gap", "8px");
        return actions;
    }

    private void speichereSchiesstand() {
        if (nameField.isEmpty() || typComboBox.isEmpty()) {
            Notification.show("Name und Typ sind erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (typComboBox.getValue() == SchiesstandTyp.VEREINSGEBUNDEN && vereinComboBox.isEmpty()) {
            Notification.show("Verein ist für vereinsgebundene Schießstände erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (typComboBox.getValue() == SchiesstandTyp.GEWERBLICH && inhaberComboBox.isEmpty()) {
            Notification.show("Inhaber ist für gewerbliche Schießstände erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Benutzer aufseher = null;
            if (typComboBox.getValue() == SchiesstandTyp.GEWERBLICH) {
                aufseher = inhaberComboBox.getValue();
            }

            Schiesstand schiesstand = Schiesstand.builder()
                    .name(nameField.getValue())
                    .typ(typComboBox.getValue())
                    .verein(vereinComboBox.getValue())
                    .adresse(adresseField.getValue())
                    .aufseher(aufseher)
                    .build();

            schiesstandService.erstelleSchiesstand(schiesstand);
            Notification.show("Schießstand erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            typComboBox.clear();
            vereinComboBox.clear();
            inhaberComboBox.clear();
            adresseField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(Schiesstand schiesstand) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Schießstand löschen");
        dialog.setText("Sind Sie sicher, dass Sie den Schießstand \"" + schiesstand.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");
        dialog.setConfirmText("Löschen");
        dialog.addConfirmListener(e -> loescheSchiesstand(schiesstand));
        dialog.open();
    }

    private void loescheSchiesstand(Schiesstand schiesstand) {
        try {
            schiesstandService.loescheSchiesstand(schiesstand.getId());
            Notification.show("Schießstand erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeDetailsDialog(Schiesstand schiesstand) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Schießstand bearbeiten: " + schiesstand.getName());
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        // Name-Feld
        TextField nameFieldEdit = new TextField("Name");
        nameFieldEdit.setValue(schiesstand.getName() != null ? schiesstand.getName() : "");
        nameFieldEdit.setWidthFull();
        nameFieldEdit.setRequired(true);

        // Adresse-Feld
        TextField adresseFieldEdit = new TextField("Adresse");
        adresseFieldEdit.setValue(schiesstand.getAdresse() != null ? schiesstand.getAdresse() : "");
        adresseFieldEdit.setWidthFull();

        // Verein-ComboBox (nur bei vereinsgebunden)
        ComboBox<Verein> vereinComboBoxEdit = new ComboBox<>("Verein");
        vereinComboBoxEdit.setItems(verbandService.findeAlleVereineEntities());
        vereinComboBoxEdit.setItemLabelGenerator(Verein::getName);
        vereinComboBoxEdit.setValue(schiesstand.getVerein());
        vereinComboBoxEdit.setWidthFull();
        vereinComboBoxEdit.setVisible(schiesstand.getTyp() == SchiesstandTyp.VEREINSGEBUNDEN);

        // Inhaber/Aufseher-ComboBox (nur bei gewerblich)
        ComboBox<Benutzer> inhaberComboBoxEdit = new ComboBox<>("Inhaber/Aufseher");
        inhaberComboBoxEdit.setItems(benutzerService.findAlleBenutzerEntities());
        inhaberComboBoxEdit.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        inhaberComboBoxEdit.setValue(schiesstand.getAufseher());
        inhaberComboBoxEdit.setWidthFull();
        inhaberComboBoxEdit.setVisible(schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH);
        inhaberComboBoxEdit.setRequired(schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH);
        inhaberComboBoxEdit.setPlaceholder("Inhaber/Aufseher auswählen...");

        layout.add(nameFieldEdit, adresseFieldEdit, vereinComboBoxEdit, inhaberComboBoxEdit);

        Button speichernButton = new Button("Speichern", e -> {
            if (nameFieldEdit.isEmpty()) {
                Notification.show("Name ist erforderlich")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH && inhaberComboBoxEdit.isEmpty()) {
                Notification.show("Inhaber ist für gewerbliche Schießstände erforderlich")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                schiesstand.setName(nameFieldEdit.getValue());
                schiesstand.setAdresse(adresseFieldEdit.getValue());

                // Bei vereinsgebunden: Verein aktualisieren
                if (schiesstand.getTyp() == SchiesstandTyp.VEREINSGEBUNDEN) {
                    schiesstand.setVerein(vereinComboBoxEdit.getValue());
                }

                // Bei gewerblichen Schießständen den Inhaber/Aufseher aktualisieren
                if (schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH) {
                    schiesstand.setAufseher(inhaberComboBoxEdit.getValue());
                }

                schiesstandService.aktualisiereSchiesstand(schiesstand);
                Notification.show("Schießstand erfolgreich aktualisiert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(abbrechenButton, speichernButton);
        dialog.open();
    }

    /**
     * Zeigt einen Dialog mit allen Einträgen eines gewerblichen Schießstands.
     */
    private void zeigeEintraegeDialog(Schiesstand schiesstand) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Einträge von " + schiesstand.getName());
        dialog.setWidth("900px");
        dialog.setMaxWidth("95vw");
        dialog.setHeight("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setSizeFull();

        // Info-Text
        Paragraph infoText = new Paragraph(
                "Hier sehen Sie alle Schießnachweis-Einträge, die an diesem gewerblichen Schießstand erstellt wurden."
        );
        infoText.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Grid für Einträge
        Grid<de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag> eintraegeGrid =
                new Grid<>(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag.class, false);
        eintraegeGrid.setHeight("100%");
        eintraegeGrid.addClassName("rounded-grid");

        eintraegeGrid.addColumn(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag::getDatum)
                .setHeader("Datum")
                .setSortable(true)
                .setAutoWidth(true);

        eintraegeGrid.addColumn(e -> e.getSchuetze() != null ? e.getSchuetze().getVollstaendigerName() : "-")
                .setHeader("Schütze")
                .setAutoWidth(true);

        eintraegeGrid.addColumn(e -> e.getDisziplin() != null ? e.getDisziplin().getProgramm() : "-")
                .setHeader("Disziplin")
                .setAutoWidth(true);

        eintraegeGrid.addColumn(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber")
                .setAutoWidth(true);

        eintraegeGrid.addColumn(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        eintraegeGrid.addComponentColumn(this::createEintragStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        // Lade Einträge
        try {
            // Lade alle Einträge und filtere nach Schießstand-ID
            List<de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag> eintraege =
                    schiessnachweisService.findeEintraegeAnSchiesstand(schiesstand);

            if (eintraege.isEmpty()) {
                Paragraph emptyText = new Paragraph("Noch keine Einträge an diesem Schießstand vorhanden.");
                emptyText.getStyle()
                        .set("text-align", "center")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("padding", "var(--lumo-space-xl)");
                layout.add(infoText, emptyText);
            } else {
                eintraegeGrid.setItems(eintraege);
                layout.add(infoText, eintraegeGrid);
            }
        } catch (Exception e) {
            Notification.show("Fehler beim Laden der Einträge: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(layout);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    /**
     * Erstellt ein Status-Badge für Einträge.
     */
    private com.vaadin.flow.component.html.Span createEintragStatusBadge(
            de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag) {
        com.vaadin.flow.component.html.Span badge = new com.vaadin.flow.component.html.Span();
        badge.getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-weight", "500")
                .set("font-size", "12px")
                .set("display", "inline-block");

        switch (eintrag.getStatus()) {
            case OFFEN, UNSIGNIERT -> {
                badge.setText("Unsigniert");
                badge.getStyle()
                        .set("background-color", "#ffeb3b")
                        .set("color", "#333333");
            }
            case SIGNIERT -> {
                badge.setText("Signiert");
                badge.getStyle()
                        .set("background-color", "#4caf50")
                        .set("color", "white");
            }
            case ABGELEHNT -> {
                badge.setText("Abgelehnt");
                badge.getStyle()
                        .set("background-color", "#f44336")
                        .set("color", "white");
            }
        }

        return badge;
    }

    private void updateGrid() {
        List<Schiesstand> schiesstaende = schiesstandService.findeAlleSchiesstaendeEntities();
        grid.setItems(schiesstaende);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = schiesstaende.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}
