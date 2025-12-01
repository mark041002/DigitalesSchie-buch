package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.service.VereinService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Organisatorische View für Sportverbände - sichtbar für Vereinschefs und Admins.
 */
@Route(value = "organisatorisch/verbaende", layout = MainLayout.class)
@PageTitle("Verbände | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "ADMIN"})
public class VerbaendeView extends VerticalLayout {

    private final VerbandService verbandService;
    private final DisziplinService disziplinService;
    private final SecurityService securityService;
    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VereinService vereinService;

    private final Grid<Verband> grid = new Grid<>(Verband.class, false);
    private Div emptyStateMessage;

    public VerbaendeView(VerbandService verbandService,
                         DisziplinService disziplinService,
                         SecurityService securityService,
                         VereinsmitgliedschaftService mitgliedschaftService,
                         VereinService vereinService) {
        this.verbandService = verbandService;
        this.disziplinService = disziplinService;
        this.securityService = securityService;
        this.mitgliedschaftService = mitgliedschaftService;
        this.vereinService = vereinService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    private void createContent() {
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.setWidthFull();

        Div header = ViewComponentHelper.createGradientHeader("Schützenverbände");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox(
                "Hier sehen Sie alle verfügbaren Schützenverbände. Sie können einem Verband beitreten oder austreten."
        );
        contentWrapper.add(infoBox);

        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage(
                "Noch keine Verbände vorhanden.", VaadinIcon.RECORDS
        );
        emptyStateMessage.setVisible(false);

        Div gridContainer = ViewComponentHelper.createGridContainer();

        grid.setColumnReorderingAllowed(true);
        grid.addClassName("rounded-grid");
        grid.setWidthFull();
        grid.getStyle()
                .set("flex", "1 1 auto")
                .set("min-height", "0");

        grid.addColumn(Verband::getName).setHeader("Verbandsname").setSortable(true).setFlexGrow(1);
        grid.addColumn(Verband::getBeschreibung).setHeader("Beschreibung").setFlexGrow(2);
        grid.addColumn(verband -> verband.getVereine() == null ? 0 : verband.getVereine().size())
                .setHeader("Anzahl Vereine")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END)
                .setWidth("140px");

        grid.addComponentColumn(verband -> {
            Benutzer currentUser = securityService.getAuthenticatedUser();
            if (currentUser == null) {
                return new Span("");
            }
            boolean istMitglied = verbandService.istMitgliedImVerband(currentUser, verband.getId());
            Icon icon = istMitglied ? VaadinIcon.CHECK.create() : VaadinIcon.CLOSE.create();
            icon.getStyle().set("color", istMitglied ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");
            icon.getElement().setProperty("title", istMitglied ? "Beigetreten" : "Nicht beigetreten");
            return icon;
          }).setHeader("Beigetreten")
            .setTextAlign(ColumnTextAlign.CENTER)
            .setFlexGrow(0)
            .setWidth("120px");

        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Aktionen")
            .setWidth("260px")
            .setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(Verband verband) {
        Button disziplinenButton = new Button("Disziplinen", e -> zeigeDisziplinen(verband));
        disziplinenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout layout = new HorizontalLayout(disziplinenButton);

        Benutzer currentUser = securityService.getAuthenticatedUser();

        if (currentUser != null) {
            // Ermittle Verein, bei dem der Benutzer Vereinschef ist (falls vorhanden)
            var chefMitgliedschaft = mitgliedschaftService.findeMitgliedschaften(currentUser).stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                    .findFirst();

            if (chefMitgliedschaft.isPresent()) {
                Long vereinId = chefMitgliedschaft.get().getVerein().getId();
                de.suchalla.schiessbuch.model.entity.Verein verein = verbandService.findeVerein(vereinId);
                if (verein != null) {
                    boolean vereinHatVerband = verein.getVerbaende() != null && verein.getVerbaende().stream()
                            .anyMatch(v -> v.getId() != null && v.getId().equals(verband.getId()));

                    if (vereinHatVerband) {
                        Button entferneVonVerein = new Button("Aus Verein entfernen", e -> {
                            try {
                                verein.getVerbaende().removeIf(v -> v.getId() != null && v.getId().equals(verband.getId()));
                                vereinService.aktualisiereVerein(verein);
                                Notification.show("Verband aus Ihrem Verein entfernt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                updateGrid();
                            } catch (Exception ex) {
                                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        });
                        entferneVonVerein.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                        layout.add(entferneVonVerein);
                    } else {
                        Button fuegeZuVereinHinzu = new Button("Für Verein hinzufügen", e -> {
                            try {
                                if (verein.getVerbaende() == null) {
                                    verein.setVerbaende(new java.util.HashSet<>());
                                }
                                verein.getVerbaende().add(verband);
                                vereinService.aktualisiereVerein(verein);
                                Notification.show("Verband zum Verein hinzugefügt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                updateGrid();
                            } catch (Exception ex) {
                                Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        });
                        fuegeZuVereinHinzu.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                        layout.add(fuegeZuVereinHinzu);
                    }
                }
            }
        }
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

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = verbaende.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}
