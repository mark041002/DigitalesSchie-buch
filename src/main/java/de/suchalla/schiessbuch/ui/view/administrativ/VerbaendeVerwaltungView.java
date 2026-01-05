package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Verbandsverwaltung (nur für Admins).
 */
@Route(value = "admin/verbaende", layout = MainLayout.class)
@PageTitle("Verbände | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class VerbaendeVerwaltungView extends VerticalLayout {

    private final VerbandService verbandService;
    private final Grid<Verband> grid = new Grid<>(Verband.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextArea beschreibungField = new TextArea("Beschreibung");

    public VerbaendeVerwaltungView(VerbandService verbandService) {
        this.verbandService = verbandService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    private void createContent() {
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        Div header = ViewComponentHelper.createGradientHeader("Verbandsverwaltung");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox(
                "Erstellen und verwalten Sie Verbände im System. Jeder Verband kann mehrere Vereine enthalten."
        );
        contentWrapper.add(infoBox);

        Div formContainer = ViewComponentHelper.createFormContainer();

        H3 erstellenTitle = new H3("Neuen Verband erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        nameField.setRequired(true);
        beschreibungField.setMaxLength(1000);

        FormLayout formLayout = ViewComponentHelper.createResponsiveFormLayout();
        formLayout.add(nameField, beschreibungField);

        Button speichernButton = new Button("Verband erstellen", e -> speichereVerband());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        Div gridContainer = ViewComponentHelper.createGridContainer();

        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Noch keine Verbände vorhanden.", VaadinIcon.INSTITUTION);
        emptyStateMessage.setVisible(false);

        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);
        grid.addColumn(Verband::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(Verband::getName)
                .setHeader("Name")
                .setFlexGrow(1);

        grid.addColumn(Verband::getBeschreibung)
                .setHeader("Beschreibung")
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("200px")
                .setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(Verband verband) {
        Button detailsButton = new Button("Details", VaadinIcon.SEARCH.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> {
            // Navigiere zur DisziplinenVerwaltung mit Verband-ID
            UI.getCurrent().navigate(DisziplinenVerwaltungView.class,
                    new com.vaadin.flow.router.QueryParameters(
                            java.util.Map.of("verbandId", java.util.List.of(verband.getId().toString()))
                    )
            );
        });

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(verband));

        HorizontalLayout actions = new HorizontalLayout(detailsButton, loeschenButton);
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.getStyle().set("gap", "8px");
        return actions;
    }

    private void speichereVerband() {
        if (nameField.isEmpty()) {
            Notification.show("Name ist erforderlich").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Verband verband = Verband.builder()
                    .name(nameField.getValue())
                    .beschreibung(beschreibungField.getValue())
                    .build();

            verbandService.erstelleVerband(verband);
            Notification.show("Verband erfolgreich erstellt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            beschreibungField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(Verband verband) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Verband löschen");
        dialog.setText("Sind Sie sicher, dass Sie den Verband \"" + verband.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setCancelText("Abbrechen");
        dialog.addConfirmListener(e -> loescheVerband(verband));
        dialog.open();
    }

    private void loescheVerband(Verband verband) {
        try {
            verbandService.loescheVerband(verband.getId());
            Notification.show("Verband erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<Verband> verbaende = verbandService.findeAlleVerbaendeEntities();
        grid.setItems(verbaende);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = verbaende.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}

