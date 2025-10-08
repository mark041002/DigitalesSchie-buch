package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * View für Vereinschefs zur Verwaltung von Beitrittsanfragen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "beitrittsanfragen", layout = MainLayout.class)
@PageTitle("Beitrittsanfragen | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class BeitrittsanfragenView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<Vereinsmitgliedschaft> anfrageGrid = new Grid<>(Vereinsmitgliedschaft.class, false);

    public BeitrittsanfragenView(VereinsmitgliedschaftService mitgliedschaftService) {
        this.mitgliedschaftService = mitgliedschaftService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Beitrittsanfragen verwalten"));

        // Grid für Beitrittsanfragen
        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setSortable(true);
        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getEmail())
                .setHeader("E-Mail");
        anfrageGrid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Antragsdatum")
                .setSortable(true);
        anfrageGrid.addComponentColumn(this::createAnfrageActionButtons)
                .setHeader("Aktionen");

        add(anfrageGrid);
    }

    /**
     * Erstellt Aktions-Buttons für Anfragen.
     *
     * @param anfrage Die Anfrage
     * @return Layout mit Buttons
     */
    private HorizontalLayout createAnfrageActionButtons(Vereinsmitgliedschaft anfrage) {
        Button genehmigenButton = new Button("Genehmigen", e -> genehmigen(anfrage));
        genehmigenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        Button ablehnenButton = new Button("Ablehnen", e -> ablehnen(anfrage));
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        return new HorizontalLayout(genehmigenButton, ablehnenButton);
    }

    /**
     * Genehmigt eine Beitrittsanfrage.
     *
     * @param anfrage Die Anfrage
     */
    private void genehmigen(Vereinsmitgliedschaft anfrage) {
        try {
            mitgliedschaftService.genehmigeAnfrage(anfrage.getId());
            Notification.show("Beitrittsanfrage genehmigt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Genehmigen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Lehnt eine Beitrittsanfrage ab.
     *
     * @param anfrage Die Anfrage
     */
    private void ablehnen(Vereinsmitgliedschaft anfrage) {
        try {
            mitgliedschaftService.lehneAnfrageAb(anfrage.getId());
            Notification.show("Beitrittsanfrage abgelehnt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Ablehnen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        // Für den Demo-Verein (ID 1) - in einer echten Anwendung würde man
        // die Vereine des Vereinschefs aus der Datenbank laden
        mitgliedschaftService.findeVerein(1L).ifPresent(verein -> {
            List<Vereinsmitgliedschaft> anfragen = mitgliedschaftService.findeBeitrittsanfragen(verein);
            anfrageGrid.setItems(anfragen);
        });
    }
}
