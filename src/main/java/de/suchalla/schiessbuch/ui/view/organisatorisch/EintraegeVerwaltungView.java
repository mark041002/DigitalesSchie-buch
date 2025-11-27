package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.router.PreserveOnRefresh;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.SignaturService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View für Aufseher zur Verwaltung von Schießnachweis-Einträgen mit PKI-Signierung und PDF-Export.
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Route(value = "eintraege-verwaltung", layout = MainLayout.class)
@PageTitle("Eintragsverwaltung | Digitales Schießbuch")
@RolesAllowed({"AUFSEHER", "SCHIESSSTAND_AUFSEHER", "VEREINS_CHEF", "ADMIN"})
@PreserveOnRefresh
@Slf4j
public class EintraegeVerwaltungView extends VerticalLayout implements BeforeEnterObserver {

    private final SchiessnachweisService schiessnachweisService;
    private final SchiesstandRepository schiesstandRepository;
    private final PdfExportService pdfExportService;
    private final SignaturService signaturService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);
    private final DateTimeFormatter dateFormatter;
    private final ComboBox<String> schuetzenComboBox = new ComboBox<>("Schütze");
    private final ComboBox<String> aufseherComboBox = new ComboBox<>("Aufseher");
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private final Button filterButton = new Button("Filtern");
    private Div filterContainer;
    private Div emptyStateMessage;
    private Anchor pdfDownload;

    private final Benutzer currentUser;
    private Schiesstand aktuellerSchiesstand;
    private EintragStatus aktuellerStatus = EintragStatus.UNSIGNIERT; // Standard: Unsigniert
    private Tab aktuellerTab;
    private Tab alleTab;
    private com.vaadin.flow.component.grid.Grid.Column<SchiessnachweisEintrag> actionsColumn;

    private List<SchiessnachweisEintrag> aktuelleFiltierteEintraege = List.of();
    private Long uebergebeneSchiesstandId; // Über URL übergebene Schießstand-ID
    private boolean contentCreated = false; // Flag um mehrfaches Erstellen zu verhindern

    public EintraegeVerwaltungView(SecurityService securityService,
                                   SchiessnachweisService schiessnachweisService,
                                   SchiesstandRepository schiesstandRepository,
                                   PdfExportService pdfExportService,
                                   SignaturService signaturService) {
        this.schiessnachweisService = schiessnachweisService;
        this.schiesstandRepository = schiesstandRepository;
        this.pdfExportService = pdfExportService;
        this.signaturService = signaturService;
        this.currentUser = securityService.getAuthenticatedUser();

        // Formatter für Datumsausgabe
        this.dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verhindere mehrfaches Erstellen des Contents
        if (contentCreated) {
            log.debug("Content bereits erstellt, Navigation wird übersprungen");
            return;
        }

        // Prüfe Query-Parameter
        QueryParameters queryParams = event.getLocation().getQueryParameters();
        if (queryParams.getParameters().containsKey("schiesstandId")) {
            try {
                uebergebeneSchiesstandId = Long.parseLong(
                    queryParams.getParameters().get("schiesstandId").get(0)
                );
            } catch (NumberFormatException e) {
                log.warn("Ungültige schiesstandId: {}", e.getMessage());
            }
        }

        ladeSchiesstand();
        createContent();
        contentCreated = true;
    }

    /**
     * Lädt den Schießstand des aktuellen Benutzers oder den über URL-Parameter angegebenen.
     */
    private void ladeSchiesstand() {
        // Wenn eine schiesstandId über URL übergeben wurde, verwende diese
        if (uebergebeneSchiesstandId != null) {
            aktuellerSchiesstand = schiesstandRepository.findById(uebergebeneSchiesstandId)
                    .orElse(null);
            if (aktuellerSchiesstand != null) {
                log.info("Schießstand über URL geladen: {}", aktuellerSchiesstand.getName());
                return;
            }
        }

        // Sonst: Standard-Logik - lade Schießstand des aktuellen Benutzers
        if (currentUser != null) {
            log.info("Lade Schießstand für Benutzer: {} {} (ID: {})", currentUser.getVorname(), currentUser.getNachname(), currentUser.getId());
            log.info("Anzahl Vereinsmitgliedschaften: {}", currentUser.getVereinsmitgliedschaften().size());
            
            // Lade ALLE Schießstände mit eager loading
            // (Nicht nur die der Vereine, in denen der Benutzer Mitglied ist, 
            // da ein Standaufseher einem Schießstand zugeordnet sein kann, ohne Vereinsmitglied zu sein)
            List<Schiesstand> alleSchiesstaende = schiesstandRepository.findAllWithVerein();
            log.info("Anzahl aller Schießstände im System: {}", alleSchiesstaende.size());

            // Filtere: Nur Schießstände, bei denen der Benutzer als Standaufseher eingetragen ist
            // ODER Aufseher/Vereinschef im Verein ist
            aktuellerSchiesstand = alleSchiesstaende.stream()
                    .filter(schiesstand -> {

                        // Prüfe ob Benutzer als Aufseher direkt im Schießstand eingetragen ist
                        boolean istStandaufseher = schiesstand.getAufseher() != null &&
                                                   schiesstand.getAufseher().getId().equals(currentUser.getId());

                        // Prüfe ob Benutzer Aufseher ODER Vereinschef im Verein des Schießstands ist
                        boolean istVereinsAufseherOderChef = false;
                        if (!currentUser.getVereinsmitgliedschaften().isEmpty() && schiesstand.getVerein() != null) {
                            istVereinsAufseherOderChef = currentUser.getVereinsmitgliedschaften().stream()
                                    .anyMatch(m -> m.getVerein().getId().equals(schiesstand.getVerein().getId()) &&
                                                 (Boolean.TRUE.equals(m.getIstAufseher()) ||
                                                  Boolean.TRUE.equals(m.getIstVereinschef())));
                        }

                        boolean berechtigt = istStandaufseher || istVereinsAufseherOderChef;

                        log.debug("Schießstand: {}, Standaufseher: {}, VereinsAufseherOderChef: {}, Berechtigt: {}",
                                 schiesstand.getName(), istStandaufseher, istVereinsAufseherOderChef, berechtigt);

                        return berechtigt;
                    })
                    .findFirst()
                    .orElse(null);
            
            if (aktuellerSchiesstand != null) {
                log.info("Schießstand geladen: {} (ID: {})", aktuellerSchiesstand.getName(), aktuellerSchiesstand.getId());
            } else {
                log.warn("Kein berechtigter Schießstand gefunden für Benutzer: {} {}", currentUser.getVorname(), currentUser.getNachname());
            }
        }
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        if (aktuellerSchiesstand == null) {
            // Fallback-Anzeige wenn kein Schießstand verfügbar
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.addClassName("view-container");
            errorLayout.add(new H2("Kein Schießstand verfügbar"));
            errorLayout.add("Sie müssen Aufseher oder Vereinschef in einem Verein mit Schießstand sein.");
            add(errorLayout);
            return;
        }

        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.setWidthFull();

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Eintragsverwaltung");
        title.getStyle().set("margin", "0");

        Span schiesstandName = new Span(aktuellerSchiesstand.getName());
        schiesstandName.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "white")
                .set("font-weight", "500")
                .set("margin-left", "auto");

        HorizontalLayout headerContent = new HorizontalLayout(title, schiesstandName);
        headerContent.setWidthFull();
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContent.setSpacing(true);
        header.add(headerContent);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = ViewComponentHelper.createInfoBox(
                "Verwalten Sie Schießnachweis-Einträge: Signieren oder lehnen Sie Einträge ab. " +
                "Nutzen Sie die Filter um gezielt nach Schützen, Aufsehern oder Status zu suchen."
        );
        contentWrapper.add(infoBox);

        // Tabs-Container mit weißem Hintergrund
        Div tabsContainer = new Div();
        tabsContainer.addClassName("tabs-container");
        tabsContainer.setWidthFull();

        // Tabs für Status-Filter - "Unsigniert" ist standardmäßig ausgewählt
        Tab unsigniertTab = new Tab("Unsigniert");
        Tab signiertTab = new Tab("Signiert");
        Tab abgelehntTab = new Tab("Abgelehnt");
        alleTab = new Tab("Alle Einträge");

        // Setze initialen Tab
        aktuellerTab = unsigniertTab;

        Tabs tabs = new Tabs(unsigniertTab, signiertTab, abgelehntTab, alleTab);
        tabs.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            aktuellerTab = selectedTab;

            if (selectedTab == unsigniertTab) {
                aktuellerStatus = EintragStatus.UNSIGNIERT;
            } else if (selectedTab == signiertTab) {
                aktuellerStatus = EintragStatus.SIGNIERT;
            } else if (selectedTab == abgelehntTab) {
                aktuellerStatus = EintragStatus.ABGELEHNT;
            } else {
                aktuellerStatus = null; // Alle
            }

            // Filter aktualisieren (ComboBoxen neu befüllen und Status-Filter anzeigen)
            aktualisiereFilterOptionen();
            updateGrid();
        });

        tabsContainer.add(tabs);
        contentWrapper.add(tabsContainer);

        // Filter-Container
        contentWrapper.add(ViewComponentHelper.createFilterBox(createFilterLayout()));


        // Empty State Message
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Noch keine Einträge vorhanden.", VaadinIcon.RECORDS);
        emptyStateMessage.setVisible(false);

        Div gridContainer = ViewComponentHelper.createGridContainer();

        // Grid direkt konfigurieren
        configureGrid();

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);

        add(contentWrapper);
        updateGrid();
    }

    /**
     * Erstellt das Filter-Layout.
     */
    private VerticalLayout createFilterLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        schuetzenComboBox.setPlaceholder("Alle Schützen");
        schuetzenComboBox.setWidth("250px");
        schuetzenComboBox.setClearButtonVisible(true);
        schuetzenComboBox.addValueChangeListener(e -> updateGrid());

        aufseherComboBox.setPlaceholder("Alle Aufseher");
        aufseherComboBox.setWidth("250px");
        aufseherComboBox.setClearButtonVisible(true);
        aufseherComboBox.addValueChangeListener(e -> updateGrid());

        // Datum-Filter standardmäßig leer (Benutzer setzt bei Bedarf)
        vonDatum.setWidth("200px");

        bisDatum.setWidth("200px");

        filterButton.addClickListener(e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.setIcon(new Icon(VaadinIcon.FILTER));

        pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren", new Icon(VaadinIcon.DOWNLOAD));
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        // Alle Filter nebeneinander, responsive mit flex-wrap
        HorizontalLayout filterRow = new HorizontalLayout(
            schuetzenComboBox, aufseherComboBox, vonDatum, bisDatum, filterButton, pdfDownload
        );
        filterRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filterRow.setSpacing(false);
        filterRow.setPadding(false);
        filterRow.setWidthFull();
        filterRow.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-m)")
                .set("box-sizing", "border-box");

        layout.add(filterRow);
        aktualisiereFilterOptionen();
        return layout;
    }

    /**
     * Aktualisiert die Filter-Optionen basierend auf den aktuellen Tabellendaten.
     */
    private void aktualisiereFilterOptionen() {
        if (aktuellerSchiesstand == null) {
            return;
        }

        // Lade alle Einträge des Schießstands als DTOs
        List<SchiessnachweisEintrag> alleEintraege = schiessnachweisService.findeEintraegeAnSchiesstand(aktuellerSchiesstand);

        // Filtern nach aktuellem Status (wenn nicht "Alle")
        if (aktuellerStatus != null) {
            alleEintraege = alleEintraege.stream()
                    .filter(e -> e.getStatus() == aktuellerStatus)
                    .toList();
        }

        // Extrahiere eindeutige Schützennamen
        List<String> schuetzenNamen = alleEintraege.stream()
                .map(e -> e.getSchuetze() != null ? e.getSchuetze().getVollstaendigerName() : "-")
                .distinct()
                .sorted()
                .toList();
        schuetzenComboBox.setItems(schuetzenNamen);

        // Extrahiere eindeutige Aufsehernamen (nur signierte/abgelehnte Einträge)
        List<String> aufseherNamen = alleEintraege.stream()
                .filter(e -> e.getAufseher() != null)
                .map(e -> e.getAufseher().getVollstaendigerName())
                .distinct()
                .sorted()
                .toList();
        aufseherComboBox.setItems(aufseherNamen);
    }

    /**
     * Konfiguriert das Grid.
     */
    private void configureGrid() {
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);
        grid.setSizeFull();

        grid.addColumn(e -> e.getSchuetze() != null ? e.getSchuetze().getVollstaendigerName() : "-")
                .setHeader("Schütze")
                .setSortable(true);
        grid.addColumn(dto -> dto.getDatum() == null ? "" : dateFormatter.format(dto.getDatum()))
            .setHeader("Datum")
            .setSortable(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin() != null ? eintrag.getDisziplin().getProgramm() : "-")
                .setHeader("Disziplin");
        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber");
        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setTextAlign(ColumnTextAlign.END);
        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setTextAlign(ColumnTextAlign.END);
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status");
        grid.addColumn(e -> e.getAufseher() != null ? e.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher");

        actionsColumn = grid.addComponentColumn(this::createActionButtons)
            .setHeader("Aktionen")
            .setFlexGrow(0);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );
    }

    /**
     * Erstellt Aktions-Buttons je nach Status.
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag dto) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");

        if (dto.getStatus() == EintragStatus.UNSIGNIERT) {
            Button signierenButton = new Button("Signieren", e -> signiereEintrag(dto.getId()));
            signierenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(dto.getId()));
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            layout.add(signierenButton, ablehnenButton);
        }

        // Löschen-Button: nicht für signierte Einträge erlauben
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        // Nur anzeigen, wenn Eintrag nicht signiert ist
        if (dto.getStatus() != EintragStatus.SIGNIERT) {
            loeschenButton.addClickListener(e -> zeigeLoeschDialog(dto.getId()));
            layout.add(loeschenButton);
        }

        return layout;
    }

    private void zeigeLoeschDialog(Long eintragId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eintrag löschen");
        dialog.add(new Paragraph("Möchten Sie diesen Eintrag wirklich löschen?"));
        Button loeschenButton = new Button("Löschen", event -> {
            deleteEintrag(eintragId);
            dialog.close();
        });
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button abbrechenButton = new Button("Abbrechen", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(loeschenButton, abbrechenButton);
        dialog.add(buttons);
        dialog.open();
    }

    private void deleteEintrag(Long eintragId) {
        try {
            schiessnachweisService.loescheEintrag(eintragId);
            Notification.show("Eintrag erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler beim Löschen: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Signiert einen Eintrag mit PKI-Zertifikat.
     * Service-Layer lädt die Entity intern und führt die Signierung durch.
     */
    private void signiereEintrag(Long eintragId) {
        try {
            log.info("Starte PKI-Signierung für Eintrag {} in EintraegeVerwaltungView", eintragId);

            // SignaturService wird die Entity intern laden und signieren
            // Wir übergeben nur die IDs
            signaturService.signEintragMitId(eintragId, currentUser, aktuellerSchiesstand.getVerein());

            Notification.show("Eintrag erfolgreich mit PKI-Zertifikat signiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            updateGrid();
            log.info("PKI-Signierung erfolgreich abgeschlossen für Eintrag {}", eintragId);

        } catch (Exception e) {
            log.error("Fehler beim PKI-Signieren von Eintrag {}", eintragId, e);
            Notification.show("Fehler beim Signieren: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Zeigt einen Dialog zum Ablehnen eines Eintrags.
     */
    private void zeigeAblehnungsDialog(Long eintragId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eintrag ablehnen");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextArea grundField = new TextArea("Ablehnungsgrund");
        grundField.setWidthFull();
        grundField.setHeight("150px");
        grundField.setRequired(true);

        Button ablehnenButton = new Button("Ablehnen", e -> {
            if (grundField.isEmpty()) {
                Notification.show("Bitte geben Sie einen Grund an")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                schiessnachweisService.lehneEintragAb(eintragId, currentUser, grundField.getValue());
                Notification.show("Eintrag abgelehnt")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateGrid();
                log.info("Eintrag abgelehnt: {}", eintragId);
            } catch (Exception ex) {
                log.error("Fehler beim Ablehnen", ex);
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(ablehnenButton, abbrechenButton);
        dialogLayout.add(grundField, buttons);

        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Aktualisiert das Grid basierend auf dem ausgewählten Status.
     */
    private void updateGrid() {
        if (aktuellerSchiesstand != null) {
            List<SchiessnachweisEintrag> eintraege;

            if (aktuellerStatus == null) {
                // Alle Einträge
                eintraege = schiessnachweisService.findeEintraegeAnSchiesstand(aktuellerSchiesstand);
            } else {
                // Nach Status filtern
                eintraege = schiessnachweisService.findeEintraegeAnSchiesstand(aktuellerSchiesstand).stream()
                        .filter(e -> e.getStatus() == aktuellerStatus)
                        .collect(Collectors.toList());
            }

            // Filter nach Schütze (ComboBox)
            String selektierterSchuetze = schuetzenComboBox.getValue();
            if (selektierterSchuetze != null && !selektierterSchuetze.trim().isEmpty()) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getSchuetze() != null &&
                                     e.getSchuetze().getVollstaendigerName().equals(selektierterSchuetze))
                        .collect(Collectors.toList());
            }

            // Filter nach Aufseher (ComboBox)
            String selektierterAufseher = aufseherComboBox.getValue();
            if (selektierterAufseher != null && !selektierterAufseher.trim().isEmpty()) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getAufseher() != null &&
                                     e.getAufseher().getVollstaendigerName().equals(selektierterAufseher))
                        .collect(Collectors.toList());
            }

            // Filter nach Datum nur anwenden, wenn beide Felder gesetzt sind und Tab 'Alle' aktiv
            if (aktuellerTab == alleTab) {
                LocalDate von = vonDatum.getValue();
                LocalDate bis = bisDatum.getValue();
                if (von != null && bis != null) {
                    eintraege = eintraege.stream()
                            .filter(e -> !e.getDatum().isBefore(von) && !e.getDatum().isAfter(bis))
                            .collect(Collectors.toList());
                }
            }

            // Sortierung: Neueste zuerst
            eintraege = eintraege.stream()
                    .sorted(Comparator.comparing(SchiessnachweisEintrag::getDatum).reversed())
                    .collect(Collectors.toList());

            aktuelleFiltierteEintraege = eintraege;
            grid.setItems(eintraege);

            // Aktionen-Spalte im Signiert-Tab ausblenden
            if (actionsColumn != null) {
                actionsColumn.setVisible(aktuellerStatus != EintragStatus.SIGNIERT);
            }

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = eintraege.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    /**
     * Erstellt eine StreamResource für den PDF-Export.
     */
    private StreamResource createPdfResource() {
        return new StreamResource("eintraege_" + LocalDate.now() + ".pdf", () -> {
            try {
                if (aktuelleFiltierteEintraege.isEmpty()) {
                    return new ByteArrayInputStream(new byte[0]);
                }

                LocalDate von = vonDatum.getValue() != null ? vonDatum.getValue() : LocalDate.now().minusMonths(3);
                LocalDate bis = bisDatum.getValue() != null ? bisDatum.getValue() : LocalDate.now();

                // Verwende den Schießstand-spezifischen Export für die Eintragsverwaltung
                byte[] pdfBytes = pdfExportService.exportiereEintragsverwaltungSchiesstand(
                        aktuellerSchiesstand,
                        aktuelleFiltierteEintraege,
                        von,
                        bis
                );

                log.info("PDF exportiert: {} Einträge mit PKI-Zertifikaten", aktuelleFiltierteEintraege.size());
                return new ByteArrayInputStream(pdfBytes);
            } catch (Exception e) {
                log.error("Fehler beim Erstellen der PDF", e);
                Notification.show("Fehler beim Erstellen der PDF: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return new ByteArrayInputStream(new byte[0]);
            }
        });
    }

    /**
     * Erstellt ein farbiges Status-Badge.
     */
    private Span createStatusBadge(SchiessnachweisEintrag dto) {
        Span badge = new Span();
        badge.getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-weight", "500")
                .set("font-size", "12px")
                .set("display", "inline-block");

        switch (dto.getStatus()) {
            case UNSIGNIERT, OFFEN -> {
                badge.setText("Unsigniert");
                badge.getStyle()
                        .set("background-color", "#ffeb3b")
                        .set("color", "#333333");
            }
            case SIGNIERT -> {
                badge.setText("Signiert");
                badge.getStyle()
                        .set("background-color", "#4caf50")
                        .set("color", "white");
            }
            case ABGELEHNT -> {
                badge.setText("Abgelehnt");
                badge.getStyle()
                        .set("background-color", "#f44336")
                        .set("color", "white");
            }
        }

        return badge;
    }
}

