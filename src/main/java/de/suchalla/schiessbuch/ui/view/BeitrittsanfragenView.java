package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
@RolesAllowed({"VEREINS_CHEF", "ADMIN"})
@Slf4j
public class BeitrittsanfragenView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<Vereinsmitgliedschaft> anfrageGrid = new Grid<>(Vereinsmitgliedschaft.class, false);
    private Div emptyStateMessage;

    public BeitrittsanfragenView(VereinsmitgliedschaftService mitgliedschaftService) {
        this.mitgliedschaftService = mitgliedschaftService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
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

        // Text-Container
        Div textContainer = new Div();

        H2 title = new H2("Beitrittsanfragen");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("Verwalten Sie offene Mitgliedschaftsanträge");
        subtitle.addClassName("subtitle");

        textContainer.add(title, subtitle);
        header.add(textContainer);
        contentWrapper.add(header);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid für Beitrittsanfragen
        anfrageGrid.setHeight("600px");
        anfrageGrid.addClassName("rounded-grid");

        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setSortable(true)
                .setAutoWidth(true);
        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getEmail())
                .setHeader("E-Mail")
                .setAutoWidth(true);
        anfrageGrid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Antragsdatum")
                .setSortable(true)
                .setAutoWidth(true);
        anfrageGrid.addComponentColumn(this::createAnfrageActionButtons)
                .setHeader("Aktionen")
                .setWidth("250px")
                .setFlexGrow(0);

        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Keine offenen Beitrittsanfragen vorhanden. Alle Anfragen wurden bearbeitet.");
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

        gridContainer.add(anfrageGrid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
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

        HorizontalLayout layout = new HorizontalLayout(genehmigenButton, ablehnenButton);
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
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

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = anfragen.isEmpty();
            anfrageGrid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        });
    }
}
