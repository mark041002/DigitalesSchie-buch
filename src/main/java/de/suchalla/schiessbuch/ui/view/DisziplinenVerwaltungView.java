package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

/**
 * View für Disziplinverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/disziplinen", layout = MainLayout.class)
@PageTitle("Disziplinen | Digitales Schießbuch")
@RolesAllowed({"ADMIN", "SOFTWARE_ADMIN"})
public class DisziplinenVerwaltungView extends VerticalLayout {

    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Disziplin> grid = new Grid<>(Disziplin.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextArea beschreibungField = new TextArea("Beschreibung");
    private final ComboBox<Verband> verbandComboBox = new ComboBox<>("Verband");

    public DisziplinenVerwaltungView(DisziplinService disziplinService, VerbandService verbandService) {
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    private void createContent() {
        add(new H2("Disziplinverwaltung"));

        // Formular
        nameField.setRequired(true);

        verbandComboBox.setItems(verbandService.findeAlleVerbaende());
        verbandComboBox.setItemLabelGenerator(Verband::getName);
        verbandComboBox.setRequired(true);

        beschreibungField.setMaxLength(1000);

        FormLayout formLayout = new FormLayout(nameField, verbandComboBox, beschreibungField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(formLayout, speichernButton);

        // Grid
        grid.addColumn(Disziplin::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(Disziplin::getName).setHeader("Name");
        grid.addColumn(disziplin -> disziplin.getVerband().getName()).setHeader("Verband");
        grid.addColumn(Disziplin::getBeschreibung).setHeader("Beschreibung");

        add(grid);
    }

    private void speichereDisziplin() {
        if (nameField.isEmpty() || verbandComboBox.isEmpty()) {
            Notification.show("Name und Verband sind erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Disziplin disziplin = Disziplin.builder()
                    .name(nameField.getValue())
                    .verband(verbandComboBox.getValue())
                    .beschreibung(beschreibungField.getValue())
                    .build();

            disziplinService.erstelleDisziplin(disziplin);
            Notification.show("Disziplin erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            verbandComboBox.clear();
            beschreibungField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        grid.setItems(disziplinService.findeAlleDisziplinen());
    }
}

