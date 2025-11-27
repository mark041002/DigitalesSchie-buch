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
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
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
    private final TextField kennzifferField = new TextField("Kennziffer");
    private final TextArea programmField = new TextArea("Programm / Beschreibung");
    private final TextField waffeKlasseField = new TextField("Waffe/Klasse (z.B. über 9mm)");
    private Verband aktuellerVerband;

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
                Long verbandId = Long.parseLong(idStr);
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
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        // Header-Bereich mit Zurück-Button
        Div headerContainer = new Div();
        headerContainer.addClassName("gradient-header");
        headerContainer.setWidthFull();
        headerContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        H2 title = new H2("Disziplinverwaltung");
        title.getStyle().set("margin", "0");

        Button zurueckButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        zurueckButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        zurueckButton.getStyle()
                .set("background", "transparent")
                .set("color", "white")
                .set("border", "none")
                .set("margin-right", "var(--lumo-space-m)");
        zurueckButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("admin/verbaende")));

        headerContainer.add(title, zurueckButton);
        contentWrapper.add(headerContainer);

        // Info-Box
        Div infoBox = ViewComponentHelper.createInfoBox(
                "Erstellen und verwalten Sie Schießdisziplinen für den Verband: " +
                (aktuellerVerband != null ? aktuellerVerband.getName() : "-")
        );
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = ViewComponentHelper.createFormContainer();

        H3 erstellenTitle = new H3("Neue Disziplin erstellen");
        erstellenTitle.getStyle().set("margin-top", "0").set("margin-bottom", "var(--lumo-space-m)");

        FormLayout formLayout = ViewComponentHelper.createResponsiveFormLayout();
        formLayout.add(kennzifferField, programmField, waffeKlasseField);

        Button speichernButton = new Button("Disziplin erstellen", e -> speichereDisziplin());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(erstellenTitle, formLayout, speichernButton);
        contentWrapper.add(formContainer);
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
            "Format: Die CSV-Datei sollte bis zu drei Spalten enthalten: Kennziffer,Programm,WaffeKlasse. " +
            "Die erste Zeile kann eine Überschrift sein (wird ignoriert). " +
            "Beispiel: \"1114/1001,25m Schießen,über 9mm\""
        );
        csvInfo.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-m)");

        Upload upload = getUpload();

        uploadContainer.add(csvTitle, csvInfo, upload);
        contentWrapper.add(uploadContainer);

        // Grid-Container mit weißem Hintergrund (standardisiert)
        Div gridContainer = ViewComponentHelper.createGridContainer();

        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Keine Disziplinen vorhanden.", VaadinIcon.TROPHY);
        emptyStateMessage.setVisible(false);

        grid.setHeight("100%");
        grid.setWidthFull();
        grid.setColumnReorderingAllowed(true);
        grid.addClassName("rounded-grid");
        grid.addColumn(Disziplin::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(Disziplin::getKennziffer)
            .setHeader("Kennziffer")
            .setFlexGrow(1);
        grid.addColumn(d -> d.getProgramm() != null ? d.getProgramm() : "")
            .setHeader("Programm")
            .setFlexGrow(1);
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
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

    private Upload getUpload() {
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
                    String kennziffer = parts[0].trim();
                    String programm = parts.length > 1 ? parts[1].trim() : "";
                    String waffeKlasse = parts.length > 2 ? parts[2].trim() : null;
                    if (!kennziffer.isEmpty()) {
                        try {
                            Disziplin disziplin = Disziplin.builder()
                                    .kennziffer(kennziffer)
                                    .programm(programm)
                                    .waffeKlasse(waffeKlasse)
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
        return upload;
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
        if (kennzifferField.isEmpty() || aktuellerVerband == null) {
            Notification.show("Kennziffer ist erforderlich und ein Verband muss ausgewählt sein.")
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            Disziplin disziplin = Disziplin.builder()
                .kennziffer(kennzifferField.getValue())
                .programm(programmField.getValue())
                .waffeKlasse(waffeKlasseField.getValue())
                .verband(aktuellerVerband)
                .build();
            disziplinService.erstelleDisziplin(disziplin);
            Notification.show("Disziplin erfolgreich erstellt")
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            kennzifferField.clear();
            programmField.clear();
            waffeKlasseField.clear();
            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(Disziplin disziplin) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Disziplin löschen");
        dialog.setText("Sind Sie sicher, dass Sie die Disziplin \"" + disziplin.getKennziffer() + "\" löschen möchten?");
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
        List<Disziplin> disziplinen = aktuellerVerband != null
            ? disziplinService.findeDisziplinenVonVerbandEntities(aktuellerVerband.getId())
            : List.of();
        grid.setItems(disziplinen);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = disziplinen.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}
