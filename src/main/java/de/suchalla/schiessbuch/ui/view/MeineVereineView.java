package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * View für Vereinsmitgliedschaften.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "meine-vereine", layout = MainLayout.class)
@PageTitle("Meine Vereine | Digitales Schießbuch")
@PermitAll
public class MeineVereineView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VerbandService verbandService;

    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);

    private final Benutzer currentUser;

    public MeineVereineView(SecurityService securityService,
                            VereinsmitgliedschaftService mitgliedschaftService,
                            VerbandService verbandService) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.verbandService = verbandService;

        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Meine Vereine"));

        // Button für Vereinsbeitritt
        Button beitretenButton = new Button("Einem Verein beitreten", e -> zeigeVereinsbeitrittsDialog());
        beitretenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(beitretenButton);

        // Grid
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getVerein().getName()).setHeader("Verein");
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getVerein().getVerband().getName()).setHeader("Verband");
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getIstAufseher() ? "Ja" : "Nein")
                .setHeader("Aufseher");
        grid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum).setHeader("Beitrittsdatum");
        grid.addColumn(mitgliedschaft -> switch (mitgliedschaft.getStatus()) {
            case AKTIV -> "Aktiv";
            case BEANTRAGT -> "Beantragt";
            case ABGELEHNT -> "Abgelehnt";
            case BEENDET -> "Beendet";
        }).setHeader("Status");

        grid.addComponentColumn(this::createActionButtons).setHeader("Aktionen");

        add(grid);
    }

    /**
     * Zeigt den Dialog für den Vereinsbeitritt mit Verbandsauswahl.
     */
    private void zeigeVereinsbeitrittsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Einem Verein beitreten");
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        H3 title = new H3("Geben Sie die Vereins-ID ein:");

        TextField vereinIdField = new TextField("Vereins-ID");
        vereinIdField.setWidthFull();
        vereinIdField.setPlaceholder("ID des Vereins eingeben");

        Button beitretenButton = new Button("Beitreten", e -> {
             if (vereinIdField.getValue() == null || vereinIdField.getValue().isEmpty()) {
                 Notification.show("Bitte geben Sie eine Vereins-ID ein")
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
             }

             vereinBeitreten(vereinIdField.getValue());
             dialog.close();
         });
         beitretenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

         Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

         layout.add(title, vereinIdField);
         dialog.add(layout);
         dialog.getFooter().add(abbrechenButton, beitretenButton);
         dialog.open();
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     * @return Button
     */
    private Button createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        if (mitgliedschaft.getAktiv()) {
            Button verlassenButton = new Button("Verlassen", e -> vereinVerlassen(mitgliedschaft));
            verlassenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return verlassenButton;
        }
        return new Button();
    }

    /**
     * Tritt einem Verein bei.
     */
    private void vereinBeitreten(String vereinIdStr) {
        try {
            Long vereinId = Long.parseLong(vereinIdStr);
            Verein verein = verbandService.findeVerein(vereinId)
                    .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

            // Verein gefunden, Verband wird automatisch aus dem Verein bestimmt
            mitgliedschaftService.vereinBeitreten(currentUser, verein);

            Notification.show("Beitrittsanfrage wurde gesendet")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            updateGrid();

        } catch (NumberFormatException e) {
            Notification.show("Ungültige Vereins-ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Verlässt einen Verein.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     */
    private void vereinVerlassen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.vereinVerlassen(mitgliedschaft.getId());
            Notification.show("Verein verlassen").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        if (currentUser != null) {
            List<Vereinsmitgliedschaft> mitgliedschaften =
                    mitgliedschaftService.findeMitgliedschaftenVonBenutzer(currentUser);
            grid.setItems(mitgliedschaften);
        }
    }
}
