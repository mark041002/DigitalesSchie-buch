package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benachrichtigung;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenachrichtigungsService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View für Benachrichtigungen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "benachrichtigungen", layout = MainLayout.class)
@PageTitle("Benachrichtigungen | Digitales Schießbuch")
@PermitAll
public class BenachrichtigungenView extends VerticalLayout {

    private final SecurityService securityService;
    private final BenachrichtigungsService benachrichtigungsService;

    private final Grid<Benachrichtigung> grid = new Grid<>(Benachrichtigung.class, false);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Div emptyStateMessage;

    public BenachrichtigungenView(SecurityService securityService,
                                   BenachrichtigungsService benachrichtigungsService) {
        this.securityService = securityService;
        this.benachrichtigungsService = benachrichtigungsService;

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

        H2 title = new H2("Benachrichtigungen");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("margin-bottom", "var(--lumo-space-l)");
        com.vaadin.flow.component.icon.Icon infoIcon = com.vaadin.flow.component.icon.VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Bleiben Sie über wichtige Mitteilungen und Updates informiert."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");
        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        grid.addColumn(benachrichtigung -> benachrichtigung.getGelesen() ? "" : "●")
                .setHeader("").setWidth("50px");
        grid.addColumn(Benachrichtigung::getTitel).setHeader("Titel").setAutoWidth(true);
        grid.addColumn(Benachrichtigung::getNachricht).setHeader("Nachricht");
        grid.addColumn(benachrichtigung -> benachrichtigung.getErstelltAm().format(FORMATTER))
                .setHeader("Datum").setSortable(true);

        grid.addSelectionListener(selection -> {
            selection.getFirstSelectedItem().ifPresent(benachrichtigung -> {
                if (!benachrichtigung.getGelesen()) {
                    benachrichtigungsService.markiereAlsGelesen(benachrichtigung.getId());
                    updateGrid();
                }
            });
        });

        grid.addClassName("rounded-grid");
        grid.getStyle().set("min-height", "400px");

        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Keine Benachrichtigungen vorhanden. Sie sind auf dem neuesten Stand!");
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

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        Benutzer currentUser = securityService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            List<Benachrichtigung> benachrichtigungen =
                    benachrichtigungsService.findeBenachrichtigungen(currentUser);
            grid.setItems(benachrichtigungen);

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = benachrichtigungen.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }
}
