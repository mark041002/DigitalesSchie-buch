package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * View für Sportverbände - zeigt alle Verbände und deren Disziplinen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "verbaende", layout = MainLayout.class)
@PageTitle("Schützenverbände | Digitales Schießbuch")
@PermitAll
public class VerbaendeView extends VerticalLayout {

    private final VerbandService verbandService;
    private final DisziplinService disziplinService;
    private final SecurityService securityService;

    private final Grid<Verband> grid = new Grid<>(Verband.class, false);

    public VerbaendeView(VerbandService verbandService,
                         DisziplinService disziplinService,
                         SecurityService securityService) {
        this.verbandService = verbandService;
        this.disziplinService = disziplinService;
        this.securityService = securityService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Schützenverbände"));
        add(new Paragraph("Hier sehen Sie alle verfügbaren Schützenverbände. Sie können einem Verband beitreten oder austreten."));

        // Grid konfigurieren
        grid.addColumn(Verband::getName).setHeader("Verbandsname").setSortable(true);
        grid.addColumn(Verband::getBeschreibung).setHeader("Beschreibung");
        grid.addColumn(verband -> verband.getVereine().size())
                .setHeader("Anzahl Vereine")
                .setClassNameGenerator(item -> "align-right");
        // Status-Spalte: zeigt mit Icon an, ob der aktuelle Benutzer beigetreten ist
        grid.addComponentColumn(verband -> {
            Benutzer currentUser = securityService.getAuthenticatedUser().orElse(null);
            if (currentUser == null) {
                return new Span("");
            }
            boolean istMitglied = verbandService.istMitgliedImVerband(currentUser, verband.getId());
            Icon icon = istMitglied ? VaadinIcon.CHECK.create() : VaadinIcon.CLOSE.create();
            icon.getStyle().set("color", istMitglied ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");
            icon.getElement().setProperty("title", istMitglied ? "Beigetreten" : "Nicht beigetreten");
            return icon;
        }).setHeader("Beigetreten");
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Aktionen")
            .setWidth("260px")
            .setFlexGrow(0)
            .setClassNameGenerator(item -> "actions-cell-padding");

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; } .actions-cell-padding { padding-right: 24px !important; }';" +
                "document.head.appendChild(style);"
        );

        add(grid);
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param verband Der Verband
     * @return Button-Layout
     */
    private HorizontalLayout createActionButtons(Verband verband) {
        Button disziplinenButton = new Button("Disziplinen", e -> zeigeDisziplinen(verband));
        disziplinenButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        HorizontalLayout layout = new HorizontalLayout(disziplinenButton);

        Benutzer currentUser = securityService.getAuthenticatedUser().orElse(null);

        if (currentUser != null) {
            boolean istMitglied = verbandService.istMitgliedImVerband(currentUser, verband.getId());
            if (istMitglied) {
                Button austrittButton = new Button("Austreten", e -> {
                    verbandService.austretenAusVerband(currentUser, verband.getId());
                    Notification.show("Sie haben den Verband verlassen.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Grid neu laden, damit Status- und Aktionsspalte aktualisiert werden
                    updateGrid();
                });
                austrittButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                austrittButton.addClassName("table-delete-btn");
                layout.add(austrittButton);
            } else {
                Button beitretenButton = new Button("Beitreten", e -> {
                    verbandService.beitretenZuVerband(currentUser, verband.getId());
                    Notification.show("Sie sind dem Verband beigetreten.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    // Grid neu laden, damit Status- und Aktionsspalte aktualisiert werden
                    updateGrid();
                });
                beitretenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                layout.add(beitretenButton);
            }
        }
        return layout;
    }

    /**
     * Zeigt die Disziplinen eines Verbands in einem Dialog.
     *
     * @param verband Der Verband
     */
    private void zeigeDisziplinen(Verband verband) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Disziplinen von " + verband.getName());
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        List<Disziplin> disziplinen = disziplinService.findeDisziplinenVonVerband(verband);

        if (disziplinen.isEmpty()) {
            layout.add(new Paragraph("Dieser Verband bietet noch keine Disziplinen an."));
        } else {
            H3 title = new H3("Verfügbare Disziplinen:");
            layout.add(title);

            Grid<Disziplin> disziplinGrid = new Grid<>(Disziplin.class, false);
            disziplinGrid.addColumn(Disziplin::getName).setHeader("Name");
            disziplinGrid.addColumn(Disziplin::getBeschreibung).setHeader("Beschreibung");
            disziplinGrid.setItems(disziplinen);
            disziplinGrid.setAllRowsVisible(true);

            layout.add(disziplinGrid);
        }

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(layout);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        List<Verband> verbaende = verbandService.findeAlleVerbaendeMitVereinen(); // EAGER laden!
        grid.setItems(verbaende);
        // Wichtig: Grid komplett neu rendern, damit Status und Buttons aktualisiert werden
        grid.recalculateColumnWidths();
        grid.getDataProvider().refreshAll();
    }
}
