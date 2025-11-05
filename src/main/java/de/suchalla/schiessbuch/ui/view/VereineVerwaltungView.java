package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.component.UI;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View für Vereinsverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "admin/vereine", layout = MainLayout.class)
@PageTitle("Vereine | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class VereineVerwaltungView extends VerticalLayout {

    private final VerbandService verbandService;
    private final Grid<Verein> grid = new Grid<>(Verein.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextField adresseField = new TextField("Adresse");
    private final TextField vereinsNummerField = new TextField("Vereinsnummer");
    private final ComboBox<Verband> verbandComboBox = new ComboBox<>("Verband");

    public VereineVerwaltungView(VerbandService verbandService) {
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

        com.vaadin.flow.component.html.H2 title = new com.vaadin.flow.component.html.H2("Vereinsverwaltung");
        title.getStyle().set("margin", "0");

        com.vaadin.flow.component.html.Span subtitle = new com.vaadin.flow.component.html.Span("Verwaltung aller Vereine und deren Mitglieder");
        subtitle.addClassName("subtitle");

        header.add(title, subtitle);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Erstellen und verwalten Sie Vereine im System. Jeder Verein muss einem Verband zugeordnet sein."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        // Formular
        nameField.setRequired(true);

        verbandComboBox.setRequired(true);
        verbandComboBox.setItems(verbandService.findeAlleVerbaende());
        verbandComboBox.setItemLabelGenerator(Verband::getName);
        verbandComboBox.setPlaceholder("Verband auswählen...");

        FormLayout formLayout = new FormLayout(nameField, adresseField, vereinsNummerField, verbandComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Verein erstellen", e -> speichereVerein());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        grid.addColumn(Verein::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(Verein::getName).setHeader("Name");
        grid.addColumn(Verein::getVereinsNummer).setHeader("Vereinsnummer");
        grid.addColumn(Verein::getAdresse).setHeader("Adresse");
        grid.addColumn(verein -> verein.getMitgliedschaften().size())
                .setHeader("Mitglieder")
                .setWidth("120px")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(verein -> {
            // Vereinschef finden
            return verein.getMitgliedschaften().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                    .findFirst()
                    .map(m -> m.getBenutzer().getVollstaendigerName())
                    .orElse("-");
        }).setHeader("Vereinschef");

        // Aktionen-Spalte mit Mitglieder und Löschen Buttons
        grid.addComponentColumn(verein -> {
            Button mitgliederButton = new Button("Mitglieder");
            mitgliederButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            mitgliederButton.addClickListener(e -> navigiereZuMitgliederverwaltung(verein));

            Button loeschenButton = new Button("Löschen");
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.addClickListener(e -> zeigeLoeschDialog(verein));

            HorizontalLayout actions = new HorizontalLayout(mitgliederButton, loeschenButton);
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

        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Noch keine Vereine vorhanden. Erstellen Sie einen neuen Verein über das Formular oben.");
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

    private void speichereVerein() {
        String name = nameField.getValue();
        String adresse = adresseField.getValue();
        String vereinsNummer = vereinsNummerField.getValue();
        Verband verband = verbandComboBox.getValue();

        if (name == null || name.trim().isEmpty()) {
            Notification.show("Name ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (verband == null) {
            Notification.show("Verband ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Verein verein = new Verein();
        verein.setName(name);
        verein.setAdresse(adresse);
        verein.setVereinsNummer(vereinsNummer);
        verein.setVerband(verband);

        try {
            verbandService.erstelleVerein(verein);
            Notification.show("Verein erfolgreich erstellt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            adresseField.clear();
            vereinsNummerField.clear();
            verbandComboBox.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<Verein> vereine = verbandService.findeAlleVereine();
        grid.setItems(vereine);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = vereine.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }

    private void navigiereZuVereinDetails(Verein verein) {
        Map<String, List<String>> parametersMap = new HashMap<>();
        parametersMap.put("vereinId", List.of(String.valueOf(verein.getId())));
        QueryParameters queryParameters = new QueryParameters(parametersMap);

        UI.getCurrent().navigate(VereinDetailsView.class, queryParameters);
    }

    private void navigiereZuMitgliederverwaltung(Verein verein) {
        Map<String, List<String>> parametersMap = new HashMap<>();
        parametersMap.put("vereinId", List.of(String.valueOf(verein.getId())));
        QueryParameters queryParameters = new QueryParameters(parametersMap);

        UI.getCurrent().navigate(MitgliederVerwaltungView.class, queryParameters);
    }

    private void zeigeLoeschDialog(Verein verein) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Verein löschen");
        dialog.setText("Sind Sie sicher, dass Sie den Verein \"" + verein.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> loescheVerein(verein));
        dialog.open();
    }

    private void loescheVerein(Verein verein) {
        try {
            verbandService.loescheVerein(verein.getId());
            Notification.show("Verein erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
