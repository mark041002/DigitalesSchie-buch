package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;

import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * View für Benutzerverwaltung - zeigt alle Benutzer im System.
 */
@Route(value = "admin/benutzer", layout = MainLayout.class)
@PageTitle("Benutzerverwaltung | Digitales Schießbuch")
@RolesAllowed({"ADMIN"})
@Slf4j
public class MitgliederVerwaltungView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final Grid<Benutzer> grid = new Grid<>(Benutzer.class, false);
    private Div emptyStateMessage;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public MitgliederVerwaltungView(BenutzerService benutzerService) {
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
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.setWidthFull();

        // Header-Bereich
        Div header = ViewComponentHelper.createGradientHeader("Benutzerverwaltung");
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = ViewComponentHelper.createInfoBox("Übersicht aller Benutzer im System.");
        contentWrapper.add(infoBox);

        // Button: Admin ernennen (oben)
        Button adminErnennenButton = new Button("Admin ernennen", VaadinIcon.USER_STAR.create());
        adminErnennenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        adminErnennenButton.addClickListener(e -> oeffneAdminErnennungDialog());
        HorizontalLayout buttonLayout = new HorizontalLayout(adminErnennenButton);
        buttonLayout.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        contentWrapper.add(buttonLayout);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = ViewComponentHelper.createGridContainer();

        // Empty State Message
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Keine Benutzer vorhanden.", VaadinIcon.USERS);
        emptyStateMessage.setVisible(false);

        // Grid Einrichtung
        setupGrid();

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);
        add(contentWrapper);
    }

    private void setupGrid() {

        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);
        grid.setSizeFull();

        grid.addColumn(Benutzer::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0)
                .setResizable(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(Benutzer::getVollstaendigerName)
                .setHeader("Name")
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(Benutzer::getEmail)
                .setHeader("E-Mail")
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(b -> b.getRolle() != null ? b.getRolle().name() : "-")
                .setHeader("Rolle")
                .setFlexGrow(0)
                .setResizable(true);

        grid.addColumn(b -> b.getErstelltAm() != null ? dateFormatter.format(b.getErstelltAm().toLocalDate()) : "-")
            .setHeader("Erstellt")
            .setFlexGrow(0)
            .setResizable(true);

        // Aktionen-Spalte
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
                .setFlexGrow(0)
                .setResizable(false);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );
    }

    private HorizontalLayout createActionButtons(Benutzer benutzer) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "nowrap");

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        loeschenButton.addClickListener(e -> bestaetigeLoesch(benutzer));

        layout.add(loeschenButton);
        return layout;
    }

    private void oeffneAdminErnennungDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Admin ernennen");

        VerticalLayout layout = new VerticalLayout();
        
        ComboBox<Benutzer> benutzerComboBox = new ComboBox<>("Benutzer auswählen");
        benutzerComboBox.setItems(benutzerService.findAlleBenutzerEntities());
        benutzerComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        benutzerComboBox.setWidthFull();
        benutzerComboBox.setPlaceholder("Benutzer wählen...");
        
        layout.add(benutzerComboBox);

        Button ernennButton = new Button("Zum Admin ernennen", e -> {
            Benutzer selectedBenutzer = benutzerComboBox.getValue();
            if (selectedBenutzer != null) {
                bestaetigeAdmin(selectedBenutzer);
                dialog.close();
            } else {
                Notification.show("Bitte wählen Sie einen Benutzer aus")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        ernennButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(abbrechenButton, ernennButton);
        dialog.open();
    }

    private void bestaetigeLoesch(Benutzer benutzer) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Benutzer löschen?");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Paragraph("Möchten Sie den Benutzer " + benutzer.getVollstaendigerName() + " wirklich löschen?"));

        Button loeschenButton = new Button("Ja, löschen", e -> {
            try {
                // Use deletion by id to avoid passing a detached entity into the service
                if (benutzer.getId() != null) {
                    benutzerService.loescheBenutzerById(benutzer.getId());
                } else {
                    benutzerService.loescheBenutzer(benutzer);
                }
                Notification.show("Benutzer gelöscht")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                updateGrid();
            } catch (Exception ex) {
                log.error("Fehler beim Löschen", ex);
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> confirmDialog.close());

        confirmDialog.add(layout);
        confirmDialog.getFooter().add(abbrechenButton, loeschenButton);
        confirmDialog.open();
    }

    // Bestätigungs-Dialog und Aktion: Benutzer zur Rolle ADMIN machen
    private void bestaetigeAdmin(Benutzer benutzer) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Benutzer zum Admin machen?");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Paragraph("Möchten Sie den Benutzer " + benutzer.getVollstaendigerName() + " zur Rolle 'Administrator' befördern?"));

        Button confirmButton = new Button("Als Admin machen", e -> {
            try {
                benutzer.setRolle(BenutzerRolle.ADMIN);
                benutzerService.aktualisiereBenutzer(benutzer);
                Notification.show("Benutzer wurde zum Administrator gemacht")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                updateGrid();
            } catch (Exception ex) {
                log.error("Fehler beim Setzen der Rolle", ex);
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button cancelButton = new Button("Abbrechen", e -> confirmDialog.close());

        confirmDialog.add(layout);
        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    private void updateGrid() {
        List<Benutzer> alleBenutzzer = benutzerService.findAlleBenutzerEntities();
        grid.setItems(alleBenutzzer);

        boolean isEmpty = alleBenutzzer.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}
