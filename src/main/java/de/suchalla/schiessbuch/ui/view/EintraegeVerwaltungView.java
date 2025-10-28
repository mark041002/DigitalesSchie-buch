package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
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
@PageTitle("Einträgsverwaltung | Digitales Schießbuch")
@RolesAllowed({"AUFSEHER", "VEREINS_CHEF", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class EintraegeVerwaltungView extends VerticalLayout {

    private final SchiessnachweisService schiessnachweisService;
    private final SchiesstandRepository schiesstandRepository;
    private final PdfExportService pdfExportService;
    private final SignaturService signaturService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);
    private final TextField suchfeld = new TextField();
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private final Button filterButton = new Button("Filtern");
    private HorizontalLayout filterLayout;
    private Anchor pdfDownload;

    private final Benutzer currentUser;
    private Schiesstand aktuellerSchiesstand;
    private EintragStatus aktuellerStatus = EintragStatus.UNSIGNIERT; // Standard: Unsigniert
    private Tab aktuellerTab;
    private Tab alleTab;

    private List<SchiessnachweisEintrag> aktuelleFiltierteEintraege = List.of();

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

        setSpacing(true);
        setPadding(true);

        ladeSchiesstand();
        createContent();
    }

    /**
     * Lädt den Schießstand des aktuellen Benutzers.
     */
    private void ladeSchiesstand() {
        if (currentUser != null) {
            // Findet den ersten Schießstand, bei dem der Benutzer Aufseher ist
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
            add(new H2("Kein Schießstand verfügbar"));
            add("Sie müssen Aufseher oder Vereinschef in einem Verein mit Schießstand sein.");
            return;
        }

        add(new H2("Einträgsverwaltung - " + aktuellerSchiesstand.getName()));

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

            // Rebuild filter layout basierend auf Tab
            updateFilterLayout(selectedTab == alleTab);
            updateGrid();
        });

        add(tabs);
        add(createFilterLayout());
        add(createGridLayout());

        updateGrid();
    }

    /**
     * Erstellt das Filter-Layout.
     */
    private HorizontalLayout createFilterLayout() {
        // Initialisiere Filter-Komponenten
        suchfeld.setPlaceholder("Nach Namen suchen...");
        suchfeld.setWidth("300px");
        suchfeld.addValueChangeListener(e -> updateGrid());

        vonDatum.setValue(LocalDate.now().minusMonths(3));
        bisDatum.setValue(LocalDate.now());
        filterButton.addClickListener(e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // PDF-Download mit SUCCESS Theme (grüner Button)
        pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren");
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        // Erstelle initiales Layout (ohne Datums-Filter)
        filterLayout = new HorizontalLayout(suchfeld, pdfDownload);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);
        filterLayout.setPadding(false);

        return filterLayout;
    }

    /**
     * Aktualisiert das Filter-Layout je nach ausgewähltem Tab.
     */
    private void updateFilterLayout(boolean showDateFilters) {
        if (filterLayout != null) {
            filterLayout.removeAll();

            if (showDateFilters) {
                // Mit Datums-Filtern für "Alle" Tab
                filterLayout.add(suchfeld, vonDatum, bisDatum, filterButton, pdfDownload);
            } else {
                // Ohne Datums-Filter für andere Tabs
                filterLayout.add(suchfeld, pdfDownload);
            }
        }
    }

    /**
     * Erstellt das Grid-Layout.
     */
    private VerticalLayout createGridLayout() {
        VerticalLayout layout = new VerticalLayout();

        // Grid konfigurieren
        grid.addColumn(eintrag -> eintrag.getSchuetze().getVollstaendigerName())
                .setHeader("Schütze")
                .setSortable(true);
        grid.addColumn(SchiessnachweisEintrag::getDatum)
                .setHeader("Datum")
                .setSortable(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName())
                .setHeader("Disziplin");
        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber");
        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(this::getStatusText)
                .setHeader("Status");
        grid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                        eintrag.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher");
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen");

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

        if (eintrag.getStatus() == EintragStatus.UNSIGNIERT) {
            Button signierenButton = new Button("Signieren", e -> signiereEintrag(eintrag));
            signierenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

            Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(eintrag));
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            layout.add(signierenButton, ablehnenButton);
        }

        return layout;
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

            // Filter nach Suchfeld
            String suchbegriff = suchfeld.getValue();
            if (suchbegriff != null && !suchbegriff.trim().isEmpty()) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getSchuetze().getVollstaendigerName().toLowerCase()
                                .contains(suchbegriff.toLowerCase()))
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
     * Gibt den deutschen Text für einen Status zurück.
     */
    private String getStatusText(SchiessnachweisEintrag eintrag) {
        return switch (eintrag.getStatus()) {
            case OFFEN, UNSIGNIERT -> "Unsigniert";
            case SIGNIERT -> "Signiert";
            case ABGELEHNT -> "Abgelehnt";
        };
    }
}

