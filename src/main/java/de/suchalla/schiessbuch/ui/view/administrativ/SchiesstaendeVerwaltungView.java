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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
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
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Schießstandverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.2
 */
@Route(value = "admin/schiesstaende", layout = MainLayout.class)
@PageTitle("Schießstände | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class SchiesstaendeVerwaltungView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Schiesstand> grid = new Grid<>(Schiesstand.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final ComboBox<SchiesstandTyp> typComboBox = new ComboBox<>("Typ");
    private final ComboBox<Verein> vereinComboBox = new ComboBox<>("Verein");
    private final ComboBox<Benutzer> inhaberComboBox = new ComboBox<>("Inhaber");
    public SchiesstaendeVerwaltungView(BenutzerService benutzerService, DisziplinService disziplinService, VerbandService verbandService) {
        this.benutzerService = benutzerService;
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
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

        H2 title = new H2("Schießstandverwaltung");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(
                "Erstellen und verwalten Sie Schießstände im System. Schießstände können gewerblich oder vereinsgebunden sein."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

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

        FormLayout formLayout = new FormLayout(nameField, typComboBox, vereinComboBox, inhaberComboBox, adresseField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Schießstand erstellen", e -> speichereSchiesstand());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("min-height", "0")
                .set("overflow-x", "auto")
                .set("overflow-y", "auto");

        // Empty State Message
        emptyStateMessage = new Div();
        emptyStateMessage.addClassName("empty-state");
        emptyStateMessage.setWidthFull();
        emptyStateMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)")
                .set("color", "var(--lumo-secondary-text-color)");

        Icon emptyIcon = VaadinIcon.BUILDING.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Noch keine Schießstände vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Schiesstand::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(Schiesstand::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(schiesstand -> getTypText(schiesstand.getTyp()))
                .setHeader("Typ")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(schiesstand -> schiesstand.getVerein() != null ?
                        schiesstand.getVerein().getName() : "-")
                .setHeader("Verein")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(schiesstand -> {
                    if (schiesstand.getAufseher() != null) {
                        return schiesstand.getAufseher().getVollstaendigerName();
                    }
                    return "-";
                })
                .setHeader("Vereinschef / Aufseher")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);


        // Aktionen-Spalte mit Details, Zuweisen und Löschen Buttons
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("300px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(false);


        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(Schiesstand schiesstand) {
        Button detailsButton = new Button("Details", VaadinIcon.SEARCH.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> zeigeBearbeitungsDialog(schiesstand));

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(schiesstand));

        HorizontalLayout actions = new HorizontalLayout(detailsButton, loeschenButton);
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

            disziplinService.erstelleSchiesstand(schiesstand);
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
        dialog.setConfirmText("Löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> loescheSchiesstand(schiesstand));
        dialog.open();
    }

    private void loescheSchiesstand(Schiesstand schiesstand) {
        try {
            disziplinService.loescheSchiesstand(schiesstand.getId());
            Notification.show("Schießstand erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeBearbeitungsDialog(Schiesstand schiesstand) {
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

        // Inhaber-ComboBox (nur bei gewerblich)
        ComboBox<Benutzer> inhaberComboBoxEdit = new ComboBox<>("Inhaber");
        inhaberComboBoxEdit.setItems(benutzerService.findAlleBenutzerEntities());
        inhaberComboBoxEdit.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        inhaberComboBoxEdit.setValue(schiesstand.getAufseher());
        inhaberComboBoxEdit.setWidthFull();
        inhaberComboBoxEdit.setVisible(schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH);
        inhaberComboBoxEdit.setRequired(schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH);
        inhaberComboBoxEdit.setPlaceholder("Inhaber auswählen...");

        layout.add(nameFieldEdit, vereinComboBoxEdit, inhaberComboBoxEdit, adresseFieldEdit);

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
                schiesstand.setVerein(vereinComboBoxEdit.getValue());

                // Bei gewerblichen Schießständen den Inhaber aktualisieren
                if (schiesstand.getTyp() == SchiesstandTyp.GEWERBLICH) {
                    schiesstand.setAufseher(inhaberComboBoxEdit.getValue());
                }

                disziplinService.aktualisiereSchiesstand(schiesstand);
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

    private void updateGrid() {
        List<Schiesstand> schiesstaende = disziplinService.findeAlleSchiesstaendeEntities();
        grid.setItems(schiesstaende);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = schiesstaende.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }


    private String getTypText(SchiesstandTyp typ) {
        return switch (typ) {
            case VEREINSGEBUNDEN -> "Vereinsgebunden";
            case GEWERBLICH -> "Gewerblich";
            case SONSTIGES -> "Sonstiges";
        };
    }
}
