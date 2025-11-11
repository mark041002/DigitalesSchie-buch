package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

/**
 * View für Disziplinverwaltung (nur für Admins).
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Route(value = "admin/disziplinen", layout = MainLayout.class)
@PageTitle("Disziplinen | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class DisziplinenVerwaltungView extends VerticalLayout {

    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Disziplin> grid = new Grid<>(Disziplin.class, false);
    private Div emptyStateMessage;

    private final TextField nameField = new TextField("Name");
    private final TextArea beschreibungField = new TextArea("Beschreibung");
    private final ComboBox<Verband> verbandComboBox = new ComboBox<>("Verband");

    public DisziplinenVerwaltungView(DisziplinService disziplinService, VerbandService verbandService) {
        this.disziplinService = disziplinService;
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

        H2 title = new H2("Disziplinverwaltung");
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
                "Erstellen und verwalten Sie Schießdisziplinen im System. Jede Disziplin muss einem Verband zugeordnet sein."
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

        // Formular
        nameField.setRequired(true);
        beschreibungField.setMaxLength(1000);

        verbandComboBox.setRequired(true);
        verbandComboBox.setItems(verbandService.findeAlleVerbaende());
        verbandComboBox.setItemLabelGenerator(Verband::getName);
        verbandComboBox.setPlaceholder("Verband auswählen...");

        FormLayout formLayout = new FormLayout(nameField, beschreibungField, verbandComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(formLayout, speichernButton);
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

        Icon emptyIcon = VaadinIcon.TROPHY.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Noch keine Disziplinen vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");

        grid.addColumn(Disziplin::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(Disziplin::getName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(Disziplin::getBeschreibung)
                .setHeader("Beschreibung")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(disziplin -> disziplin.getVerband() != null ? disziplin.getVerband().getName() : "-")
                .setHeader("Verband")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(Disziplin disziplin) {
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(disziplin));

        HorizontalLayout actions = new HorizontalLayout(loeschenButton);
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.getStyle().set("gap", "8px");
        return actions;
    }

    private void speichereDisziplin() {
        if (nameField.isEmpty() || verbandComboBox.isEmpty()) {
            Notification.show("Name und Verband sind erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Disziplin disziplin = Disziplin.builder()
                    .name(nameField.getValue())
                    .beschreibung(beschreibungField.getValue())
                    .verband(verbandComboBox.getValue())
                    .build();

            disziplinService.erstelleDisziplin(disziplin);
            Notification.show("Disziplin erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameField.clear();
            beschreibungField.clear();
            verbandComboBox.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(Disziplin disziplin) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Disziplin löschen");
        dialog.setText("Sind Sie sicher, dass Sie die Disziplin \"" + disziplin.getName() + "\" löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> loescheDisziplin(disziplin));
        dialog.open();
    }

    private void loescheDisziplin(Disziplin disziplin) {
        try {
            disziplinService.loescheDisziplin(disziplin.getId());
            Notification.show("Disziplin erfolgreich gelöscht")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<Disziplin> disziplinen = disziplinService.findeAlleDisziplinen();
        grid.setItems(disziplinen);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = disziplinen.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}