package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import de.suchalla.schiessbuch.model.dto.VereinDTO;
import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * View für Vereinsverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/vereine", layout = MainLayout.class)
@PageTitle("Vereine | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class VereineVerwaltungView extends VerticalLayout {

    private final VerbandService verbandService;
    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final BenutzerService benutzerService;
    private final Grid<VereinDTO> grid = new Grid<>(VereinDTO.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final TextField vereinsNummerField = new TextField("Vereinsnummer");
    private final MultiSelectComboBox<Verband> verbaendeComboBox = new MultiSelectComboBox<>("Verbände");
    private final ComboBox<Benutzer> vereinschefComboBox = new ComboBox<>("Vereinschef");

    public VereineVerwaltungView(VerbandService verbandService, VereinsmitgliedschaftService mitgliedschaftService, BenutzerService benutzerService) {
        this.verbandService = verbandService;
        this.mitgliedschaftService = mitgliedschaftService;
        this.benutzerService = benutzerService;

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

        com.vaadin.flow.component.html.H2 title = new com.vaadin.flow.component.html.H2("Vereinsverwaltung");
        title.getStyle().set("margin", "0");


        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling und 100% Breite
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Erstellen und verwalten Sie Vereine im System. Jeder Verein muss einem Verband zugeordnet sein."
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

        H3 erstellenTitle = new H3("Neuen Verein erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        // Formular
        nameField.setRequired(true);
        vereinsNummerField.setRequired(true);
        verbaendeComboBox.setRequired(true);
        verbaendeComboBox.setItems(verbandService.findeAlleVerbaendeEntities());
        verbaendeComboBox.setItemLabelGenerator(Verband::getName);
        verbaendeComboBox.setPlaceholder("Verbände auswählen...");

        vereinschefComboBox.setItems(benutzerService.findAlleBenutzerEntities());
        vereinschefComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        vereinschefComboBox.setClearButtonVisible(true);

        FormLayout formLayout = new FormLayout(nameField, adresseField, vereinsNummerField, verbaendeComboBox, vereinschefComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Verein erstellen", e -> speichereVerein());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto") // Grid-Container wächst mit View
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("min-height", "0") // Für flexibles Scrollen
                .set("overflow-x", "auto") // horizontales Scrollen explizit aktivieren
                .set("overflow-y", "auto"); // vertikales Scrollen

        // Grid
        grid.setHeight("100%"); // Grid nimmt volle Höhe des Containers
        grid.setWidthFull();
        grid.getStyle()
                .remove("min-width") // min-width entfernen, damit Grid flexibel bleibt
                .set("min-height", "400px") // Mindesthöhe für Lesbarkeit
                .remove("overflow"); // Grid selbst nicht scrollbar
        grid.addClassName("rounded-grid");
        grid.addColumn(VereinDTO::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(VereinDTO::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(VereinDTO::getVereinsNummer)
                .setHeader("Vereinsnummer")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(VereinDTO::getAdresse)
                .setHeader("Adresse")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(VereinDTO::getMitgliederAnzahl)
                .setHeader("Mitglieder")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(dto -> dto.getVerbandNamen() != null && !dto.getVerbandNamen().isEmpty()
                ? String.join(", ", dto.getVerbandNamen())
                : "")
                .setHeader("Verbände")
                .setAutoWidth(true)
                .setFlexGrow(1);
        grid.addComponentColumn(dto -> {
                    HorizontalLayout layout = new HorizontalLayout();
                    layout.setSpacing(true);
                    layout.getStyle().set("flex-wrap", "nowrap");

                    Button mitgliederBtn = new Button("Mitglieder", VaadinIcon.USERS.create());
                    mitgliederBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                    mitgliederBtn.addClickListener(e -> zeigeMitgliederDialog(dto.getId()));

                    Button detailsBtn = new Button("Details", VaadinIcon.SEARCH.create());
                    detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                    detailsBtn.addClickListener(e -> zeigeVereinDetailsDialog(dto.getId()));

                    Button loeschenBtn = new Button("Löschen", VaadinIcon.TRASH.create());
                    loeschenBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                    loeschenBtn.addClickListener(e -> zeigeLoeschDialog(dto.getId()));

                    layout.add(mitgliederBtn, detailsBtn, loeschenBtn);
                    return layout;
                }).setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setClassNameGenerator(item -> "actions-cell-padding");


        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Noch keine Vereine vorhanden. Erstellen Sie einen neuen Verein über das Formular oben.");
        emptyStateMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border", "2px dashed var(--lumo-contrast-20pct)")
                .set("margin", "var(--lumo-space-m)");
        emptyStateMessage.setVisible(false);

        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private void speichereVerein() {
        String name = nameField.getValue();
        String adresse = adresseField.getValue();
        String vereinsNummer = vereinsNummerField.getValue();
        Set<Verband> verbaende = verbaendeComboBox.getValue();
        Benutzer vereinschef = vereinschefComboBox.getValue();

        if (name == null || name.trim().isEmpty()) {
            Notification.show("Name ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (vereinsNummer == null || vereinsNummer.trim().isEmpty()) {
            Notification.show("Vereinsnummer ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (verbaende == null || verbaende.isEmpty()) {
            Notification.show("Mindestens ein Verband ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Verein verein = new Verein();
        verein.setName(name);
        verein.setAdresse(adresse);
        verein.setVereinsNummer(vereinsNummer);
        verein.setVerbaende(verbaende);

        try {
            Verein gespeicherterVerein = verbandService.erstelleVerein(verein);

            // Vereinschef als Mitglied hinzufügen und zum Chef ernennen
            if (vereinschef != null) {
                Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftService.beantragenMitgliedschaft(vereinschef, gespeicherterVerein.getId());
                // Direkt genehmigen und zum Vereinschef machen
                mitgliedschaftService.genehmigeAnfrage(mitgliedschaft.getId());
                mitgliedschaft.setIstVereinschef(true);
                mitgliedschaftService.genehmigeAnfrage(mitgliedschaft.getId()); // nochmal speichern mit Chef-Flag
            }

            Notification.show("Verein erfolgreich erstellt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            adresseField.clear();
            vereinsNummerField.clear();
            verbaendeComboBox.clear();
            vereinschefComboBox.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<VereinDTO> vereine = verbandService.findeAlleVereine();
        grid.setItems(vereine);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = vereine.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
    private void zeigeLoeschDialog(Long vereinId) {
        verbandService.findeVerein(vereinId).ifPresent(verein -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Verein löschen");
            dialog.setText("Sind Sie sicher, dass Sie den Verein \"" + verein.getName() + "\" löschen möchten?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Löschen");
            dialog.setRejectText("Abbrechen");
            dialog.addConfirmListener(e -> loescheVerein(vereinId));
            dialog.open();
        });
    }
    private void loescheVerein(Long vereinId) {
        try {
            verbandService.loescheVerein(vereinId);
            Notification.show("Verein erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeMitgliederDialog(Long vereinId) {
        verbandService.findeVerein(vereinId).ifPresentOrElse(verein -> zeigeMitgliederDialogInternal(verein),
                () -> Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR));
    }

    private void zeigeMitgliederDialogInternal(Verein verein) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Mitglieder von " + verein.getName());
        dialog.setWidth("750px");
        dialog.setMaxWidth("95vw");

        List<VereinsmigliedschaftDTO> mitglieder = mitgliedschaftService.findeAlleMitgliedschaften(verein);

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setWidthFull();

        if (mitglieder.isEmpty()) {
            com.vaadin.flow.component.html.Paragraph emptyText = new com.vaadin.flow.component.html.Paragraph(
                    "Diesem Verein sind noch keine Mitglieder zugeordnet."
            );
            emptyText.getStyle()
                    .set("text-align", "center")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("padding", "var(--lumo-space-l)");
            layout.add(emptyText);
        } else {
            for (VereinsmigliedschaftDTO m : mitglieder) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
                row.getStyle()
                        .set("padding", "var(--lumo-space-s)")
                        .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                String name = m.getBenutzerVorname() + " " + m.getBenutzerNachname();
                String rolle = Boolean.TRUE.equals(m.getIstVereinschef()) ? "Vereinschef" :
                        (Boolean.TRUE.equals(m.getIstAufseher()) ? "Aufseher" : "Mitglied");

                com.vaadin.flow.component.html.Span nameSpan = new com.vaadin.flow.component.html.Span(name + " (" + rolle + ")");
                nameSpan.getStyle()
                        .set("flex", "1")
                        .set("font-weight", "500")
                        .set("padding-right", "var(--lumo-space-m)");

                HorizontalLayout buttonLayout = new HorizontalLayout();
                buttonLayout.setSpacing(true);
                buttonLayout.getStyle().set("gap", "var(--lumo-space-xs)");

                Button entfernenBtn = new Button("Entfernen", ev -> {
                    mitgliedschaftService.mitgliedEntfernen(m.getId());
                    Notification.show("Mitglied entfernt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                    updateGrid();
                });
                entfernenBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

                buttonLayout.add(entfernenBtn);
                row.add(nameSpan, buttonLayout);
                layout.add(row);
            }
        }

        Button closeBtn = new Button("Schließen", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(layout);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private void zeigeVereinDetailsDialog(Long vereinId) {
        verbandService.findeVerein(vereinId).ifPresentOrElse(verein -> zeigeVereinDetailsDialogInternal(verein),
                () -> Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR));
    }

    private void zeigeVereinDetailsDialogInternal(Verein verein) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Vereinsdetails bearbeiten");
        dialog.setWidth("600px");
        dialog.setMaxWidth("95vw");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSpacing(true);
        formLayout.setPadding(false);
        formLayout.setWidthFull();

        TextField nameField = new TextField("Name");
        nameField.setValue(verein.getName() != null ? verein.getName() : "");
        nameField.setWidthFull();

        TextField nummerField = new TextField("Vereinsnummer");
        nummerField.setValue(verein.getVereinsNummer() != null ? verein.getVereinsNummer() : "");
        nummerField.setWidthFull();

        TextArea adresseField = new TextArea("Adresse");
        adresseField.setValue(verein.getAdresse() != null ? verein.getAdresse() : "");
        adresseField.setWidthFull();
        adresseField.setHeight("100px");

        MultiSelectComboBox<Verband> verbaendeField = new MultiSelectComboBox<>("Verbände");
        verbaendeField.setItems(verbandService.findeAlleVerbaendeEntities());
        verbaendeField.setItemLabelGenerator(Verband::getName);
        verbaendeField.setValue(verein.getVerbaende() != null ? verein.getVerbaende() : new HashSet<>());
        verbaendeField.setWidthFull();
        verbaendeField.setRequired(true);

        // Vereinschef-ComboBox
        ComboBox<Benutzer> vereinschefComboBox = new ComboBox<>("Vereinschef");
        List<Vereinsmitgliedschaft> aktiveMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
        List<Benutzer> mitgliederBenutzer = aktiveMitglieder.stream()
                .map(Vereinsmitgliedschaft::getBenutzer)
                .collect(java.util.stream.Collectors.toList());
        vereinschefComboBox.setItems(mitgliederBenutzer);
        vereinschefComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        vereinschefComboBox.setWidthFull();
        vereinschefComboBox.setPlaceholder("Vereinschef auswählen...");
        vereinschefComboBox.setClearButtonVisible(true);

        // Aktuellen Vereinschef vorauswählen
        aktiveMitglieder.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                .findFirst()
                .ifPresent(aktuellerChef -> vereinschefComboBox.setValue(aktuellerChef.getBenutzer()));

        formLayout.add(nameField, nummerField, adresseField, verbaendeField, vereinschefComboBox);

        Button speichernBtn = new Button("Speichern", e -> {
            if (verbaendeField.getValue() == null || verbaendeField.getValue().isEmpty()) {
                Notification.show("Mindestens ein Verband ist erforderlich")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            verein.setName(nameField.getValue());
            verein.setVereinsNummer(nummerField.getValue());
            verein.setAdresse(adresseField.getValue());
            verein.setVerbaende(verbaendeField.getValue());
            verbandService.erstelleVerein(verein);

            // Vereinschef setzen, falls ausgewählt
            Benutzer neuerVereinschef = vereinschefComboBox.getValue();
            if (neuerVereinschef != null) {
                // Finde die Mitgliedschaft des ausgewählten Benutzers
                List<Vereinsmitgliedschaft> alleMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
                alleMitglieder.stream()
                        .filter(m -> m.getBenutzer().getId().equals(neuerVereinschef.getId()))
                        .findFirst()
                        .ifPresent(neueMitgliedschaft ->
                                mitgliedschaftService.setzeVereinschef(neueMitgliedschaft, alleMitglieder));
            } else {
                // Falls kein Vereinschef ausgewählt, alle Vereinschef-Flags entfernen
                List<Vereinsmitgliedschaft> alleMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
                alleMitglieder.stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                        .forEach(m -> {
                            m.setIstVereinschef(false);
                            mitgliedschaftService.genehmigeAnfrage(m.getId()); // speichern
                        });
            }

            Notification.show("Verein aktualisiert").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
            updateGrid();
        });
        speichernBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button abbrechenBtn = new Button("Abbrechen", e -> dialog.close());
        abbrechenBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(formLayout);
        dialog.getFooter().add(abbrechenBtn, speichernBtn);
        dialog.open();
    }
}

