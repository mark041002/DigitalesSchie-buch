package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
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
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Verbandsverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.1
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
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Verbandsverwaltung");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(
                "Erstellen und verwalten Sie Verbände im System. Jeder Verband kann mehrere Vereine enthalten."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        H3 erstellenTitle = new H3("Neuen Verband erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        // Formular
        nameField.setRequired(true);
        beschreibungField.setMaxLength(1000);

        FormLayout formLayout = new FormLayout(nameField, beschreibungField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Verband erstellen", e -> speichereVerband());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("min-height", "0")
                .set("overflow-x", "auto")
                .set("overflow-y", "auto");

        // Empty State Message
        emptyStateMessage = new Div();
        emptyStateMessage.addClassName("empty-state");
        emptyStateMessage.setWidthFull();
        emptyStateMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)")
                .set("color", "var(--lumo-secondary-text-color)");

        Icon emptyIcon = VaadinIcon.INSTITUTION.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Noch keine Verbände vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");

        grid.addColumn(Verband::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(Verband::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Verband::getBeschreibung)
                .setHeader("Beschreibung")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Aktionen-Spalte mit Details und Löschen Buttons
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("200px")
                .setAutoWidth(true)
                .setFlexGrow(0);


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
        dialog.setRejectText("Abbrechen");
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
        List<Verband> verbaende = verbandService.findeAlleVerbaende();
        grid.setItems(verbaende);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = verbaende.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}

