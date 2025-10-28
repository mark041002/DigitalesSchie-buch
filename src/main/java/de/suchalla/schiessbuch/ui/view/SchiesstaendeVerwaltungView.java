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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Schießstandverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/schiesstaende", layout = MainLayout.class)
@PageTitle("Schießstände | Digitales Schießbuch")
@RolesAllowed({"ADMIN", "SOFTWARE_ADMIN"})
public class SchiesstaendeVerwaltungView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Schiesstand> grid = new Grid<>(Schiesstand.class, false);

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final ComboBox<SchiesstandTyp> typComboBox = new ComboBox<>("Typ");
    private final ComboBox<Verein> vereinComboBox = new ComboBox<>("Verein");
    private final ComboBox<Benutzer> inhaberComboBox = new ComboBox<>("Inhaber / Vereinschef");

    public SchiesstaendeVerwaltungView(BenutzerService benutzerService, DisziplinService disziplinService, VerbandService verbandService) {
        this.benutzerService = benutzerService;
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    private void createContent() {
        add(new H2("Schießstandverwaltung"));

        nameField.setRequired(true);

        // Nur die beiden Typen erlauben
        typComboBox.setItems(SchiesstandTyp.GEWERBLICH, SchiesstandTyp.VEREINSGEBUNDEN);
        typComboBox.setItemLabelGenerator(this::getTypText);
        typComboBox.setRequired(true);
        typComboBox.addValueChangeListener(e -> {
            if (e.getValue() == SchiesstandTyp.VEREINSGEBUNDEN) {
                vereinComboBox.setVisible(true);
                vereinComboBox.setRequired(true);
                inhaberComboBox.setVisible(true);
                inhaberComboBox.setRequired(true);
                // Vereinschefs laden
                List<Benutzer> vereinschefs = benutzerService.findByRolle(BenutzerRolle.VEREINS_CHEF);
                inhaberComboBox.setItems(vereinschefs);
            } else if (e.getValue() == SchiesstandTyp.GEWERBLICH) {
                vereinComboBox.setVisible(false);
                vereinComboBox.setRequired(false);
                inhaberComboBox.setVisible(true);
                inhaberComboBox.setRequired(true);
                inhaberComboBox.clear();
                // Alle Benutzer als mögliche gewerbliche Inhaber
                List<Benutzer> inhaber = benutzerService.findAlleBenutzer();
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

        vereinComboBox.setItems(verbandService.findeAlleVereine());
        vereinComboBox.setItemLabelGenerator(Verein::getName);
        vereinComboBox.setVisible(false);

        FormLayout formLayout = new FormLayout(nameField, typComboBox, vereinComboBox, inhaberComboBox, adresseField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Schießstand erstellen", e -> speichereSchiesstand());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(formLayout, speichernButton);

        // Grid
        grid.addColumn(Schiesstand::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Schiesstand::getName).setHeader("Name");
        grid.addColumn(schiesstand -> getTypText(schiesstand.getTyp())).setHeader("Typ");
        grid.addColumn(schiesstand -> schiesstand.getVerein() != null ?
                schiesstand.getVerein().getName() : "-").setHeader("Verein");
        grid.addColumn(Schiesstand::getAdresse).setHeader("Adresse");

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        add(grid);
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

        try {
            Schiesstand schiesstand = Schiesstand.builder()
                    .name(nameField.getValue())
                    .typ(typComboBox.getValue())
                    .verein(vereinComboBox.getValue())
                    .adresse(adresseField.getValue())
                    .build();

            disziplinService.erstelleSchiesstand(schiesstand);
            Notification.show("Schießstand erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            typComboBox.clear();
            vereinComboBox.clear();
            adresseField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        grid.setItems(disziplinService.findeAlleSchiesstaende());
    }

    private String getTypText(SchiesstandTyp typ) {
        return switch (typ) {
            case LUFTGEWEHR_10M -> "10m Luftgewehr/Luftpistole";
            case PISTOLE_25M -> "25m Pistole";
            case GEWEHR_50M -> "50m Gewehr";
            case GEWEHR_100M -> "100m Gewehr";
            case TRAP_SKEET -> "Trap/Skeet";
            case VEREINSGEBUNDEN -> "Vereinsgebunden";
            case GEWERBLICH -> "Gewerblich";
            case SONSTIGES -> "Sonstiges";
        };
    }
}
