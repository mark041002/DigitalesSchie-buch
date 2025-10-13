package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

/**
 * View für Vereinsverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/vereine", layout = MainLayout.class)
@PageTitle("Vereine | Digitales Schießbuch")
@RolesAllowed({"ADMIN", "SOFTWARE_ADMIN"})
public class VereineVerwaltungView extends VerticalLayout {

    private final VerbandService verbandService;
    private final Grid<Verein> grid = new Grid<>(Verein.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final TextField vereinsNummerField = new TextField("Vereinsnummer");

    public VereineVerwaltungView(VerbandService verbandService) {
        this.verbandService = verbandService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    private void createContent() {
        add(new H2("Vereinsverwaltung"));

        // Formular
        nameField.setRequired(true);

        FormLayout formLayout = new FormLayout(nameField, adresseField, vereinsNummerField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Verein erstellen", e -> speichereVerein());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(formLayout, speichernButton);

        // Grid
        grid.addColumn(Verein::getId).setHeader("ID").setWidth("80px");
        grid.addColumn(Verein::getName).setHeader("Name");
        grid.addColumn(Verein::getVereinsNummer).setHeader("Vereinsnummer");
        grid.addColumn(Verein::getAdresse).setHeader("Adresse");
        grid.addColumn(verein -> verein.getMitgliedschaften().size()).setHeader("Mitglieder");

        add(grid);
    }

    private void speichereVerein() {
        String name = nameField.getValue();
        String adresse = adresseField.getValue();
        String vereinsNummer = vereinsNummerField.getValue();
        if (name == null || name.trim().isEmpty()) {
            Notification.show("Name ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        Verein verein = new Verein();
        verein.setName(name);
        verein.setAdresse(adresse);
        verein.setVereinsNummer(vereinsNummer);

        try {
            verbandService.erstelleVerein(verein);
            Notification.show("Verein erfolgreich erstellt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            adresseField.clear();
            vereinsNummerField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        grid.setItems(verbandService.findeAlleVereine());
    }
}
