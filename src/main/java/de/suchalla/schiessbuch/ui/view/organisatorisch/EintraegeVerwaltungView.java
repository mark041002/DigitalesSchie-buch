package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.SignaturService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
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
    private final ComboBox<String> schuetzenComboBox = new ComboBox<>("Schütze");
    private final ComboBox<String> aufseherComboBox = new ComboBox<>("Aufseher");
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private final Button filterButton = new Button("Filtern");
    private Div filterContainer;
    private Anchor pdfDownload;

    private final Benutzer currentUser;
    private Schiesstand aktuellerSchiesstand;
    private EintragStatus aktuellerStatus = EintragStatus.UNSIGNIERT; // Standard: Unsigniert
    private Tab aktuellerTab;
    private Tab alleTab;

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
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

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
        contentCreated = true; // Markiere als erstellt
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
            // Findet den ersten Schießstand, bei dem der Benutzer Aufseher ODER Vereinschef ist
            currentUser.getVereinsmitgliedschaften().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstAufseher()) || Boolean.TRUE.equals(m.getIstVereinschef()))
                    .findFirst()
                    .ifPresent(mitgliedschaft -> {
                        // Nimm den ersten Schießstand des Vereins
                        aktuellerSchiesstand = schiesstandRepository.findByVerein(mitgliedschaft.getVerein())
                                .stream()
                                .findFirst()
                                .orElse(null);
                    });
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

        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Einträgsverwaltung");
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
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        infoIcon.getStyle().set("margin-right", "var(--lumo-space-s)");
        Paragraph beschreibung = new Paragraph(
                "Verwalten Sie Schießnachweis-Einträge: Signieren oder lehnen Sie Einträge ab. " +
                "Nutzen Sie die Filter um gezielt nach Schützen, Aufsehern oder Status zu suchen."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0")
                .set("display", "inline");
        HorizontalLayout infoContent = new HorizontalLayout(infoIcon, beschreibung);
        infoContent.setAlignItems(FlexComponent.Alignment.START);
        infoContent.setSpacing(false);
        infoBox.add(infoContent);
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

        // CSS für größeren Indikator-Balken (volle Breite des Tabs) - einheitliche Höhe
        tabs.getElement().getStyle()
                .set("--lumo-size-xs", "4px")
                .set("--_lumo-tab-marker-width", "100%");

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
        filterContainer = new Div();
        filterContainer.addClassName("filter-box");
        filterContainer.setWidthFull();
        filterContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");
        filterContainer.add(createFilterLayout());
        contentWrapper.add(filterContainer);

        // Grid-Container
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.add(createGridLayout());
        contentWrapper.add(gridContainer);

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

        vonDatum.setValue(LocalDate.now().minusMonths(3));
        vonDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        vonDatum.setWidth("200px");

        bisDatum.setValue(LocalDate.now());
        bisDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        bisDatum.setWidth("200px");

        filterButton.addClickListener(e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.setIcon(new Icon(VaadinIcon.FILTER));

        pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren", new Icon(VaadinIcon.DOWNLOAD));
        pdfButton.addClassName("pdf-export-btn");
        pdfDownload.add(pdfButton);

        // Alle Filter nebeneinander, responsive mit flex-wrap
        HorizontalLayout filterRow = new HorizontalLayout(
            schuetzenComboBox, aufseherComboBox, vonDatum, bisDatum, filterButton, pdfDownload
        );
        filterRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filterRow.setSpacing(true);
        filterRow.setWidthFull();
        filterRow.getStyle().set("flex-wrap", "wrap");

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

        // Lade alle Einträge des Schießstands
        List<SchiessnachweisEintrag> alleEintraege = schiessnachweisService.findeEintraegeAnSchiesstand(aktuellerSchiesstand);

        // Filtern nach aktuellem Status (wenn nicht "Alle")
        if (aktuellerStatus != null) {
            alleEintraege = alleEintraege.stream()
                    .filter(e -> e.getStatus() == aktuellerStatus)
                    .toList();
        }

        // Extrahiere eindeutige Schützennamen
        List<String> schuetzenNamen = alleEintraege.stream()
                .map(e -> e.getSchuetze().getVollstaendigerName())
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
     * Erstellt das Grid-Layout.
     */
    private VerticalLayout createGridLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        // Grid konfigurieren
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");

        grid.addColumn(eintrag -> eintrag.getSchuetze().getVollstaendigerName())
                .setHeader("Schütze")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getDatum)
                .setHeader("Datum")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName())
                .setHeader("Disziplin")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);
        grid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                        eintrag.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher")
                .setAutoWidth(true);
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("250px")
                .setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        layout.add(grid);
        return layout;
    }

    /**
     * Erstellt Aktions-Buttons je nach Status.
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");

        if (eintrag.getStatus() == EintragStatus.UNSIGNIERT) {
            Button signierenButton = new Button("Signieren", e -> signiereEintrag(eintrag));
            signierenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(eintrag));
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            layout.add(signierenButton, ablehnenButton);
        }

        // Löschen-Button für alle Einträge
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(eintrag));
        layout.add(loeschenButton);

        return layout;
    }

    private void zeigeLoeschDialog(SchiessnachweisEintrag eintrag) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eintrag löschen");
        dialog.add(new Paragraph("Möchten Sie diesen Eintrag wirklich löschen?"));
        Button loeschenButton = new Button("Löschen", event -> {
            deleteEintrag(eintrag);
            dialog.close();
        });
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button abbrechenButton = new Button("Abbrechen", event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout(loeschenButton, abbrechenButton);
        dialog.add(buttons);
        dialog.open();
    }

    private void deleteEintrag(SchiessnachweisEintrag eintrag) {
        try {
            schiessnachweisService.loescheEintrag(eintrag.getId());
            Notification.show("Eintrag erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler beim Löschen: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Signiert einen Eintrag mit PKI-Zertifikat.
     */
    private void signiereEintrag(SchiessnachweisEintrag eintrag) {
        try {
            log.info("Starte PKI-Signierung für Eintrag {} in EintraegeVerwaltungView", eintrag.getId());

            // Eintrag mit allen Relationen inkl. Verein neu aus DB laden
            // Verwendet JOIN FETCH um LazyInitializationException zu vermeiden
            SchiessnachweisEintrag vollstaendigerEintrag = schiessnachweisService.findeEintragMitVerein(eintrag.getId())
                    .orElseThrow(() -> new RuntimeException("Eintrag nicht gefunden"));

            // Verein des Schießstands ermitteln - jetzt ohne LazyInitializationException
            Verein verein = vollstaendigerEintrag.getSchiesstand().getVerein();

            log.info("Verwende Verein: {} (ID: {})", verein.getName(), verein.getId());
            log.info("Aufseher: {} (ID: {})", currentUser.getVollstaendigerName(), currentUser.getId());

            // Mit PKI-Zertifikat signieren über SignaturService
            signaturService.signEintrag(vollstaendigerEintrag, currentUser, verein);

            Notification.show("Eintrag erfolgreich mit PKI-Zertifikat signiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            updateGrid();
            log.info("PKI-Signierung erfolgreich abgeschlossen für Eintrag {}", eintrag.getId());

        } catch (Exception e) {
            log.error("Fehler beim PKI-Signieren von Eintrag {}", eintrag.getId(), e);
            Notification.show("Fehler beim Signieren: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Zeigt einen Dialog zum Ablehnen eines Eintrags.
     */
    private void zeigeAblehnungsDialog(SchiessnachweisEintrag eintrag) {
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
                schiessnachweisService.lehneEintragAb(eintrag.getId(), currentUser, grundField.getValue());
                Notification.show("Eintrag abgelehnt")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateGrid();
                log.info("Eintrag abgelehnt: {}", eintrag.getId());
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
                        .filter(e -> e.getSchuetze().getVollstaendigerName().equals(selektierterSchuetze))
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

            // Filter nach Datum NUR wenn "Alle" Tab aktiv ist
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

                byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(
                        currentUser,
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
    private Span createStatusBadge(SchiessnachweisEintrag eintrag) {
        Span badge = new Span();
        badge.getStyle()
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-weight", "500")
                .set("font-size", "12px")
                .set("display", "inline-block");

        switch (eintrag.getStatus()) {
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

