package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
public class DisziplinenVerwaltungView extends VerticalLayout implements BeforeEnterObserver {
    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Grid<Disziplin> grid = new Grid<>(Disziplin.class, false);
    private Div emptyStateMessage;
    private final TextField nameField = new TextField("Name");
    private final TextArea beschreibungField = new TextArea("Beschreibung");
    private Verband aktuellerVerband;
    private Long verbandId;

    public DisziplinenVerwaltungView(DisziplinService disziplinService, VerbandService verbandService) {
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        // createContent wird nach BeforeEnter aufgerufen, wenn Verband geladen ist
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String idStr = null;
        if (event.getLocation().getQueryParameters().getParameters().containsKey("verbandId")) {
            idStr = event.getLocation().getQueryParameters().getParameters().get("verbandId").get(0);
        }
        if (idStr != null) {
            try {
                verbandId = Long.parseLong(idStr);
                aktuellerVerband = verbandService.findeVerband(verbandId);
            } catch (Exception e) {
                Notification.show("Ungültige Verband-ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
        if (aktuellerVerband == null) {
            Notification.show("Kein Verband gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
            setEnabled(false);
            return;
        }
        removeAll();
        createContent();
        updateGrid();
    }

    private void createContent() {
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();
        header.getStyle().set("display", "flex").set("justify-content", "space-between").set("align-items", "center");

        H2 title = new H2("Disziplinverwaltung");
        title.getStyle().set("margin", "0");
        header.add(title);

        Button zurueckButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        zurueckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        zurueckButton.getStyle()
            .set("background", "transparent")
            .set("color", "white")
            .set("border", "none")
            .set("margin-right", "var(--lumo-space-m)");
        zurueckButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/verbaende")));
        header.add(zurueckButton);
        contentWrapper.add(header);

        // Info-Box
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        Paragraph beschreibungueberschrift = new Paragraph(
                "Erstellen und verwalten Sie Schießdisziplinen für den Verband: " + (aktuellerVerband != null ? aktuellerVerband.getName() : "-")
        );
        beschreibungueberschrift.getStyle().set("color", "var(--lumo-primary-text-color)").set("margin", "0");
        infoBox.add(infoIcon, beschreibungueberschrift);
        contentWrapper.add(infoBox);

        // Formular-Container für manuelle Disziplin-Erstellung
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        H3 erstellenTitle = new H3("Neue Disziplin erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        nameField.setRequired(true);
        beschreibungField.setMaxLength(1000);
        FormLayout formLayout = new FormLayout(nameField, beschreibungField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);

        // CSV-Upload Container
        Div uploadContainer = new Div();
        uploadContainer.addClassName("form-container");
        uploadContainer.setWidthFull();
        uploadContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        H3 csvTitle = new H3("Disziplinen per CSV importieren");
        csvTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        Paragraph csvInfo = new Paragraph(
            "Format: Die CSV-Datei sollte zwei Spalten enthalten: Name,Beschreibung. " +
            "Die erste Zeile kann eine Überschrift sein (wird ignoriert). " +
            "Beispiel: \"Luftgewehr 10m,Schießen mit Luftgewehr auf 10 Meter Distanz\""
        );
        csvInfo.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        upload.setDropLabel(new Paragraph("CSV-Datei hier ablegen oder auswählen"));
        upload.addSucceededListener(event -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(buffer.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.toLowerCase().startsWith("name")) continue; // Überschrift oder leer
                    String[] parts = line.split(",");
                    if (parts.length < 1) continue;
                    String name = parts[0].trim();
                    String beschreibung = parts.length > 1 ? parts[1].trim() : "";
                    if (!name.isEmpty()) {
                        try {
                            Disziplin disziplin = Disziplin.builder()
                                    .name(name)
                                    .beschreibung(beschreibung)
                                    .verband(aktuellerVerband)
                                    .build();
                            disziplinService.erstelleDisziplin(disziplin);
                            count++;
                        } catch (Exception ex) {
                            Notification.show("Fehler beim Import: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }
                }
                Notification.show(count + " Disziplin(en) importiert.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
            } catch (Exception ex) {
                Notification.show("Fehler beim Lesen der Datei: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        uploadContainer.add(csvTitle, csvInfo, upload);
        contentWrapper.add(uploadContainer);

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
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle().set("min-height", "400px");
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
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0);
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
        if (nameField.isEmpty() || aktuellerVerband == null) {
            Notification.show("Name ist erforderlich und ein Verband muss ausgewählt sein.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            Disziplin disziplin = Disziplin.builder()
                    .name(nameField.getValue())
                    .beschreibung(beschreibungField.getValue())
                    .verband(aktuellerVerband)
                    .build();
            disziplinService.erstelleDisziplin(disziplin);
            Notification.show("Disziplin erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            beschreibungField.clear();
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
