package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benachrichtigung;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenachrichtigungsService;
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

    public BenachrichtigungenView(SecurityService securityService,
                                   BenachrichtigungsService benachrichtigungsService) {
        this.securityService = securityService;
        this.benachrichtigungsService = benachrichtigungsService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Benachrichtigungen"));

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

        add(grid);
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
        }
    }
}

