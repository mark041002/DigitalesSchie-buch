package de.suchalla.schiessbuch.ui.view.oeffentlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

/**
 * Öffentliche Ansicht für Sportverbände - zeigt alle Verbände und deren Disziplinen.
 * Jeder (auch nicht authentifizierte Benutzer) kann Verbände und Disziplinen ansehen.
 */
@Route(value = "verbaende", layout = MainLayout.class)
@PageTitle("Schützenverbände | Digitales Schießbuch")
@AnonymousAllowed
public class VerbaendeView extends VerticalLayout {

    private final VerbandService verbandService;
    private final DisziplinService disziplinService;

    private final Grid<Verband> grid = new Grid<>(Verband.class, false);

    public VerbaendeView(VerbandService verbandService,
                         DisziplinService disziplinService,
                         SecurityService securityService) {
        this.verbandService = verbandService;
        this.disziplinService = disziplinService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    private void createContent() {
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        Div header = ViewComponentHelper.createGradientHeader("Schützenverbände");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox(
                "Hier sehen Sie alle verfügbaren Schützenverbände und deren Disziplinen."
        );
        contentWrapper.add(infoBox);

        grid.setColumnReorderingAllowed(true);
        grid.addClassName("rounded-grid");

        grid.addColumn(Verband::getName).setHeader("Verbandsname").setSortable(true).setFlexGrow(1);
        grid.addColumn(Verband::getBeschreibung).setHeader("Beschreibung").setFlexGrow(2);

        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Aktionen")
            .setWidth("260px")
            .setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        contentWrapper.add(grid);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(Verband verband) {
        Button disziplinenButton = new Button("Disziplinen", e -> zeigeDisziplinen(verband));
        disziplinenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout layout = new HorizontalLayout(disziplinenButton);
        return layout;
    }

    private void zeigeDisziplinen(Verband verband) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Disziplinen von " + verband.getName());
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        List<Disziplin> disziplinen = disziplinService.findeDisziplinenVonVerbandEntities(verband.getId());

        if (disziplinen.isEmpty()) {
            layout.add(new Paragraph("Dieser Verband bietet noch keine Disziplinen an."));
        } else {
            H3 title = new H3("Verfügbare Disziplinen:");
            layout.add(title);

            Grid<Disziplin> disziplinGrid = new Grid<>(Disziplin.class, false);
            disziplinGrid.addColumn(Disziplin::getKennziffer).setHeader("Kennziffer");
            disziplinGrid.addColumn(d -> d.getProgramm() != null ? d.getProgramm() : "").setHeader("Programm");
            disziplinGrid.setItems(disziplinen);
            disziplinGrid.setAllRowsVisible(true);

            disziplinGrid.getColumns().forEach(c -> c.setAutoWidth(true));
            layout.add(disziplinGrid);
        }

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(layout);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    private void updateGrid() {
        List<Verband> verbaende = verbandService.findeAlleVerbaendeMitVereinenEntities();
        grid.setItems(verbaende);
        grid.recalculateColumnWidths();
        grid.getDataProvider().refreshAll();
    }
}
