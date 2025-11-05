package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.component.UI;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    private void createContent() {
        add(new H2("Verbandsverwaltung"));

        // Formular
        nameField.setRequired(true);
        beschreibungField.setMaxLength(1000);

        FormLayout formLayout = new FormLayout(nameField, beschreibungField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        Button speichernButton = new Button("Verband erstellen", e -> speichereVerband());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(formLayout, speichernButton);

        // Grid
        grid.addColumn(Verband::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Verband::getName).setHeader("Name");
        grid.addColumn(Verband::getBeschreibung).setHeader("Beschreibung");
        grid.addColumn(verband -> verband.getVereine().size())
                .setHeader("Anzahl Vereine")
                .setWidth("150px")
                .setClassNameGenerator(item -> "align-right");

        // Aktionen-Spalte mit Details und Löschen Buttons
        grid.addComponentColumn(verband -> {
            Button detailsButton = new Button("Details");
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            detailsButton.addClickListener(e -> navigiereToDisziplinen(verband));

            Button loeschenButton = new Button("Löschen");
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.addClickListener(e -> zeigeLoeschDialog(verband));

            HorizontalLayout actions = new HorizontalLayout(detailsButton, loeschenButton);
            actions.setSpacing(true);
            actions.setPadding(false);
            return actions;
        }).setHeader("Aktionen").setWidth("200px").setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        add(grid);
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

    private void navigiereToDisziplinen(Verband verband) {
        Map<String, List<String>> parametersMap = new HashMap<>();
        parametersMap.put("verbandId", List.of(String.valueOf(verband.getId())));
        QueryParameters queryParameters = new QueryParameters(parametersMap);

        UI.getCurrent().navigate(DisziplinenVerwaltungView.class, queryParameters);
    }

    private void updateGrid() {
        grid.setItems(verbandService.findeAlleVerbaende());
        grid.getDataProvider().refreshAll();
    }
}
