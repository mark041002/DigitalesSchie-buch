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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final Grid<Verein> grid = new Grid<>(Verein.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
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
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        Div header = ViewComponentHelper.createGradientHeader("Vereinsverwaltung");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox(
                "Erstellen und verwalten Sie Vereine im System. Jeder Verein muss mindestens einem Verband zugeordnet sein."
        );
        contentWrapper.add(infoBox);

        Div formContainer = ViewComponentHelper.createFormContainer();

        H3 erstellenTitle = new H3("Neuen Verein erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        nameField.setRequired(true);
        verbaendeComboBox.setRequired(true);
        verbaendeComboBox.setItems(verbandService.findeAlleVerbaendeEntities());
        verbaendeComboBox.setItemLabelGenerator(Verband::getName);
        verbaendeComboBox.setPlaceholder("Verbände auswählen...");

        vereinschefComboBox.setItems(benutzerService.findAlleBenutzerEntities());
        vereinschefComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        vereinschefComboBox.setClearButtonVisible(true);

        FormLayout formLayout = new FormLayout(nameField, adresseField, verbaendeComboBox, vereinschefComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Verein erstellen", e -> speichereVerein());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        Div gridContainer = ViewComponentHelper.createGridContainer();
        
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Verein::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Verein::getName)
                .setHeader("Name")
                .setFlexGrow(1);
        grid.addColumn(verein -> verein.getMitgliedschaften() != null ? verein.getMitgliedschaften().size() : 0)
                .setHeader("Mitglieder")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(verein -> verein.getVerbaende() != null && !verein.getVerbaende().isEmpty()
                ? verein.getVerbaende().stream().map(Verband::getName).collect(Collectors.joining(", "))
                : "")
                .setHeader("Verbände")
                .setFlexGrow(1);
        grid.addColumn(verein -> {
                    if (verein.getMitgliedschaften() == null) return "-";
                    return verein.getMitgliedschaften().stream()
                            .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                            .map(m -> m.getBenutzer().getVollstaendigerName())
                            .findFirst()
                            .orElse("-");
                })
                .setHeader("Vereinschef")
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
                .setFlexGrow(0);
        
        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Empty State Message erstellen
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage(
                "Noch keine Vereine vorhanden. Erstellen Sie einen neuen Verein über das Formular oben.",
                VaadinIcon.BUILDING
        );
        emptyStateMessage.setVisible(false);

        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private void speichereVerein() {
        String name = nameField.getValue();
        String adresse = adresseField.getValue();
        Set<Verband> verbaende = verbaendeComboBox.getValue();
        Benutzer vereinschef = vereinschefComboBox.getValue();

        if (name == null || name.trim().isEmpty()) {
            Notification.show("Name ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Verein verein = new Verein();
        verein.setName(name);
        verein.setAdresse(adresse);
        verein.setVerbaende(verbaende);

        try {
            Verein gespeicherterVerein = verbandService.erstelleVerein(verein);

            // Vereinschef als Mitglied hinzufügen und zum Chef ernennen
            if (vereinschef != null) {
                // Beantrage Mitgliedschaft
                Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftService.beantragenMitgliedschaft(vereinschef, gespeicherterVerein.getId());
                
                // Genehmige die Mitgliedschaft
                mitgliedschaftService.genehmigeAnfrage(mitgliedschaft.getId());
                
                // Lade alle aktiven Mitgliedschaften neu
                List<Vereinsmitgliedschaft> alleMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(gespeicherterVerein);
                
                // Finde die gerade erstellte Mitgliedschaft und setze als Vereinschef
                alleMitglieder.stream()
                        .filter(m -> m.getBenutzer().getId().equals(vereinschef.getId()))
                        .findFirst()
                        .ifPresent(m -> mitgliedschaftService.setzeVereinschef(m, alleMitglieder));
            }

            Notification.show("Verein erfolgreich erstellt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            adresseField.clear();
            verbaendeComboBox.clear();
            vereinschefComboBox.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<Verein> vereine = verbandService.findeAlleVereine();
        grid.setItems(vereine);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = vereine.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
    private void zeigeLoeschDialog(Long vereinId) {
        Verein verein = verbandService.findeVerein(vereinId);
        if (verein == null) {
            Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Verein löschen");
        dialog.setText("Sind Sie sicher, dass Sie den Verein \"" + verein.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setCancelText("Abbrechen");
        dialog.addConfirmListener(e -> loescheVerein(vereinId));
        dialog.open();
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
        Verein verein = verbandService.findeVerein(vereinId);
        if (verein == null) {
            Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        zeigeMitgliederDialogInternal(verein);
    }

    private void zeigeMitgliederDialogInternal(Verein verein) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Mitglieder von " + verein.getName());
        dialog.setWidth("750px");
        dialog.setMaxWidth("95vw");

        List<Vereinsmitgliedschaft> mitglieder = mitgliedschaftService.findeAlleMitgliedschaften(verein);

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
            for (Vereinsmitgliedschaft m : mitglieder) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
                row.getStyle()
                        .set("padding", "var(--lumo-space-s)")
                        .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                String name = m.getBenutzer().getVorname() + " " + m.getBenutzer().getNachname();
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

                Button entfernenBtn = new Button("Entfernen", e -> {
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
        Verein verein = verbandService.findeVerein(vereinId);
        if (verein == null) {
            Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        zeigeVereinDetailsDialogInternal(verein);
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

        // Vereinschef-ComboBox - alle Benutzer im System zur Auswahl
        ComboBox<Benutzer> vereinschefComboBox = new ComboBox<>("Vereinschef");
        vereinschefComboBox.setItems(benutzerService.findAlleBenutzerEntities());
        vereinschefComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        vereinschefComboBox.setWidthFull();
        vereinschefComboBox.setPlaceholder("Vereinschef auswählen...");
        vereinschefComboBox.setClearButtonVisible(true);

        // Aktuellen Vereinschef vorauswählen
        List<Vereinsmitgliedschaft> aktiveMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
        aktiveMitglieder.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                .findFirst()
                .ifPresent(aktuellerChef -> vereinschefComboBox.setValue(aktuellerChef.getBenutzer()));

        formLayout.add(nameField, adresseField, verbaendeField, vereinschefComboBox);

        Button speichernBtn = new Button("Speichern", e -> {
            if (verbaendeField.getValue() == null || verbaendeField.getValue().isEmpty()) {
                Notification.show("Mindestens ein Verband ist erforderlich")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            verein.setName(nameField.getValue());
            verein.setAdresse(adresseField.getValue());
            verein.setVerbaende(verbaendeField.getValue());
            verbandService.erstelleVerein(verein);

            // Vereinschef setzen, falls ausgewählt
            Benutzer neuerVereinschef = vereinschefComboBox.getValue();
            List<Vereinsmitgliedschaft> alleMitglieder = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
            
            if (neuerVereinschef != null) {
                // Prüfe, ob der Benutzer bereits Mitglied ist
                java.util.Optional<Vereinsmitgliedschaft> bestehendesMitglied = alleMitglieder.stream()
                        .filter(m -> m.getBenutzer().getId().equals(neuerVereinschef.getId()))
                        .findFirst();
                
                if (bestehendesMitglied.isPresent()) {
                    // Benutzer ist bereits Mitglied, setze als Vereinschef
                    mitgliedschaftService.setzeVereinschef(bestehendesMitglied.get(), alleMitglieder);
                } else {
                    // Benutzer ist noch kein Mitglied, füge hinzu und setze als Vereinschef
                    Vereinsmitgliedschaft neueMitgliedschaft = mitgliedschaftService.beantragenMitgliedschaft(neuerVereinschef, verein.getId());
                    mitgliedschaftService.genehmigeAnfrage(neueMitgliedschaft.getId());
                    
                    // Lade Mitgliedschaft neu und setze als Vereinschef
                    List<Vereinsmitgliedschaft> aktualisierte = mitgliedschaftService.findeAktiveMitgliedschaftenEntities(verein);
                    aktualisierte.stream()
                            .filter(m -> m.getBenutzer().getId().equals(neuerVereinschef.getId()))
                            .findFirst()
                            .ifPresent(m -> mitgliedschaftService.setzeVereinschef(m, aktualisierte));
                }
            } else {
                // Falls kein Vereinschef ausgewählt, alle Vereinschef-Flags entfernen
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

