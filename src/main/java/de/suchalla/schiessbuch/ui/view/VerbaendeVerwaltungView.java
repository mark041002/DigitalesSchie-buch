package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import jakarta.annotation.security.RolesAllowed;

/**
 * View für Verbandsverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/verbaende", layout = MainLayout.class)
@PageTitle("Verbände | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class VerbaendeVerwaltungView extends VerticalLayout {

    private final VerbandService verbandService;
    private final Grid<Verband> grid = new Grid<>(Verband.class, false);

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

        H2 title = new H2("Verbandsverwaltung");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();

        Paragraph beschreibung = new Paragraph(
                "Erstellen und verwalten Sie Verbände im System. Jeder Verband kann mehrere Vereine enthalten."
        );

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");

        // Überschrift für Formular
        H2 formTitle = new H2("Neuen Verband erstellen");
        formTitle.getStyle().set("margin-top", "0");

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

        formContainer.add(formTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");

        // Grid - responsiv konfiguriert
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Verband::getId)
                .setHeader("ID")
                .setWidth("50px")
                .setResizable(true)
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Verband::getName)
                .setHeader("Name")
                .setResizable(true)
                .setFlexGrow(1);
        grid.addColumn(Verband::getBeschreibung)
                .setHeader("Beschreibung")
                .setResizable(true)
                .setFlexGrow(1);

        // Aktionen-Spalte mit Details und Löschen Buttons
        grid.addComponentColumn(verband -> {
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
            actions.setWidthFull();
            return actions;
        })
                .setHeader("Aktionen")
                .setWidth("200px")
                .setFlexGrow(0)
                .setResizable(false);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );

        gridContainer.add(grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
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
        grid.setItems(verbandService.findeAlleVerbaende());
        grid.getDataProvider().refreshAll();
    }
}
