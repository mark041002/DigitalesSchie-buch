package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.StreamResource;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View fÃ¼r Aufseher und Vereinschefs zur Verwaltung von Mitgliedschaften.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "mitgliedsverwaltung", layout = MainLayout.class)
@PageTitle("Mitgliedsverwaltung | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "AUFSEHER", "SCHIESSSTAND_AUFSEHER", "ADMIN"})
@Slf4j
public class MitgliedschaftenVerwaltenView extends VerticalLayout implements BeforeEnterObserver {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final PdfExportService pdfExportService;
    private final Benutzer currentUser;
    private final VereinRepository vereinRepository;

    private final Grid<Vereinsmitgliedschaft> mitgliederGrid = new Grid<>(Vereinsmitgliedschaft.class, false);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Tabs as class fields so we can select them from beforeEnter
    private Tab genehmigtTab;
    private Tab beantragtTab;
    private Tab abgelehntTab;
    private Tabs tabs;

    private final TextField suchfeld = new TextField();
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private final Button filterButton = new Button("Filtern");
    private HorizontalLayout filterLayout;
    private Div emptyStateMessage;
    private Anchor pdfDownload;

    private Verein aktuellerVerein;
    private MitgliedschaftsStatus aktuellerStatus = MitgliedschaftsStatus.AKTIV;
    private Tab aktuellerTab;
    private Tab alleTab;
    private Grid.Column<Vereinsmitgliedschaft> statusColumn;
    private Grid.Column<Vereinsmitgliedschaft> rolleColumn;

    public MitgliedschaftenVerwaltenView(SecurityService securityService,
                                         VereinsmitgliedschaftService mitgliedschaftService,
                                         PdfExportService pdfExportService,
                                         VereinRepository vereinRepository) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.pdfExportService = pdfExportService;
        this.vereinRepository = vereinRepository;
        this.currentUser = securityService.getAuthenticatedUser();

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        ladeVerein();
        createContent();
    }

    /**
     * Lädt den Verein des aktuellen Benutzers.
     */
    private void ladeVerein() {
        if (currentUser != null) {
            mitgliedschaftService.findeMitgliedschaften(currentUser).stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()) || Boolean.TRUE.equals(m.getIstAufseher()))
                    .findFirst()
                    .ifPresent(m -> {
                        Long vereinId = m.getVerein().getId();
                        aktuellerVerein = vereinRepository.findById(vereinId).orElse(null);
                    });
        }
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        if (aktuellerVerein == null) {
            // Fallback-Anzeige wenn kein Verein verfügbar
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.addClassName("view-container");
            errorLayout.add(new H2("Sie sind kein Aufseher oder Vereinschef in einem Verein"));
            add(errorLayout);
            return;
        }

        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        // Header-Bereich: Titel + Vereinsname rechts vom Titel
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("gradient-header");
        header.addClassName("header-inline-subtitle");
        header.setWidthFull();
        H2 title = new H2("Mitgliedsverwaltung");
        title.getStyle().set("margin", "0");
        // Vereinsname als rechter Inhalt
        Span vereinName = new Span(aktuellerVerein.getName());
        vereinName.addClassName("subtitle");

        header.add(title);
        header.expand(title);
        header.add(vereinName);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = ViewComponentHelper.createInfoBox(
                "Verwalten Sie die Mitgliedschaften Ihres Vereins. Genehmigen oder lehnen Sie Beitrittsanfragen ab und verwalten Sie aktive Mitglieder."
        );
        contentWrapper.add(infoBox);

        // Tabs-Container mit weißem Hintergrund
        Div tabsContainer = new Div();
        tabsContainer.addClassName("tabs-container");
        tabsContainer.setWidthFull();

        // Tabs fÃ¼r Status-Filter
        genehmigtTab = new Tab("Aktive Mitglieder");
        beantragtTab = new Tab("Zur Genehmigung");
        abgelehntTab = new Tab("Abgelehnte");
        alleTab = new Tab("Alle");

        // Setze initialen Tab
        aktuellerTab = genehmigtTab;

        tabs = new Tabs(genehmigtTab, beantragtTab, abgelehntTab, alleTab);
        tabs.setWidthFull();

        // CSS fÃ¼r grÃ¶ÃŸeren Indikator-Balken
        tabs.getElement().getStyle()
                .set("--lumo-size-xs", "4px")
                .set("--_lumo-tab-marker-width", "100%");

        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            aktuellerTab = selectedTab;

            if (selectedTab == beantragtTab) {
                aktuellerStatus = MitgliedschaftsStatus.BEANTRAGT;
            } else if (selectedTab == genehmigtTab) {
                aktuellerStatus = MitgliedschaftsStatus.AKTIV;
            } else if (selectedTab == abgelehntTab) {
                aktuellerStatus = MitgliedschaftsStatus.ABGELEHNT;
            } else {
                aktuellerStatus = null; // Alle
            }

            // Rebuild filter layout basierend auf Tab
            updateFilterLayout(selectedTab == alleTab);

            // Zeige/Verstecke Status-Spalte
            if (statusColumn != null) {
                statusColumn.setVisible(selectedTab == alleTab);
            }
            // Zeige/Verstecke Rollenspalte (bei 'Zur Genehmigung' und 'Abgelehnte' ausblenden)
            if (rolleColumn != null) {
                rolleColumn.setVisible(!(selectedTab == beantragtTab || selectedTab == abgelehntTab));
            }

            updateGrid();
        });

        tabsContainer.add(tabs);
        contentWrapper.add(tabsContainer);

        // Filter-Container
        Div filterContainer = new Div();
        filterContainer.addClassName("filter-box");
        filterContainer.setWidthFull();
        filterContainer.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");
        filterContainer.add(createFilterLayout());
        contentWrapper.add(filterContainer);

        // Empty State Message
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage("Noch keine Mitgliedschaften vorhanden.", VaadinIcon.USERS);
        emptyStateMessage.setVisible(false);

        // Grid-Container
        Div gridContainer = ViewComponentHelper.createGridContainer();
        gridContainer.add(emptyStateMessage, createGridLayout());
        contentWrapper.add(gridContainer);

        add(contentWrapper);
        updateGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParams = event.getLocation().getQueryParameters();
        if (queryParams.getParameters().containsKey("tab")) {
            String tab = queryParams.getParameters().get("tab").get(0);
            if (tabs != null && beantragtTab != null) {
                if ("beantragt".equalsIgnoreCase(tab) || "zur_genehmigung".equalsIgnoreCase(tab) || "zurGenehmigung".equalsIgnoreCase(tab)) {
                    tabs.setSelectedTab(beantragtTab);
                    aktuellerStatus = MitgliedschaftsStatus.BEANTRAGT;
                    updateFilterLayout(false);
                    if (statusColumn != null) statusColumn.setVisible(false);
                    if (rolleColumn != null) rolleColumn.setVisible(true);
                    updateGrid();
                } else if ("alle".equalsIgnoreCase(tab)) {
                    tabs.setSelectedTab(alleTab);
                    aktuellerStatus = null;
                    updateFilterLayout(true);
                    if (statusColumn != null) statusColumn.setVisible(true);
                    updateGrid();
                }
            }
        }
    }

    /**
     * Erstellt das Filter-Layout.
     */
    private HorizontalLayout createFilterLayout() {
        // Initialisiere Filter-Komponenten
        suchfeld.setPlaceholder("Nach Namen suchen...");
        suchfeld.setWidth("300px");
        suchfeld.setPrefixComponent(VaadinIcon.SEARCH.create());
        suchfeld.addValueChangeListener(e -> updateGrid());

        vonDatum.setValue(LocalDate.now().minusYears(1));
        vonDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        vonDatum.setWidth("200px");
        vonDatum.getStyle().set("padding-top", "0");

        bisDatum.setValue(LocalDate.now());
        bisDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        bisDatum.setWidth("200px");
        bisDatum.getStyle().set("padding-top", "0");

        filterButton.addClickListener(e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.setIcon(new Icon(VaadinIcon.FILTER));

        // PDF-Download
        pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren", new Icon(VaadinIcon.DOWNLOAD));
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        // Erstelle initiales Layout (ohne Datums-Filter)
        filterLayout = new HorizontalLayout(suchfeld, pdfDownload);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);
        filterLayout.setPadding(false);
        filterLayout.getStyle().set("flex-wrap", "wrap");

        return filterLayout;
    }

    /**
     * Aktualisiert das Filter-Layout je nach ausgewähltem Tab.
     */
    private void updateFilterLayout(boolean showDateFilters) {
        if (filterLayout != null) {
            filterLayout.removeAll();

            if (showDateFilters) {
                // Mit Datums-Filtern fÃ¼r "Alle" Tab
                filterLayout.add(suchfeld, vonDatum, bisDatum, filterButton, pdfDownload);
            } else {
                // Ohne Datums-Filter fÃ¼r andere Tabs
                filterLayout.add(suchfeld, pdfDownload);
            }
        }
    }

    /**
     * Erstellt das Grid-Layout.
     */
    private VerticalLayout createGridLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        // Grid konfigurieren
        mitgliederGrid.addClassName("rounded-grid");
        mitgliederGrid.setColumnReorderingAllowed(true);

        mitgliederGrid.addColumn(dto -> (dto.getBenutzer().getVorname() + " " + dto.getBenutzer().getNachname()).trim())
                .setHeader("Name")
                .setSortable(true)
                .setAutoWidth(true);
        mitgliederGrid.addColumn(m -> m.getBenutzer() != null ? m.getBenutzer().getEmail() : "-")
                .setHeader("E-Mail")
                .setAutoWidth(true);
        mitgliederGrid.addColumn(dto -> {
                if (dto == null) return "";
                // Bei abgelehnten Einträgen, falls ein Austrittsdatum vorhanden ist, dieses anzeigen
                if (dto.getStatus() == MitgliedschaftsStatus.ABGELEHNT && dto.getAustrittDatum() != null) {
                    return dateFormatter.format(dto.getAustrittDatum());
                }
                return dto.getBeitrittDatum() == null ? "" : dateFormatter.format(dto.getBeitrittDatum());
            })
            .setHeader("Datum")
            .setSortable(true)
            .setAutoWidth(true);
        rolleColumn = mitgliederGrid.addColumn(this::getRolleText)
            .setHeader("Rolle")
            .setAutoWidth(true);

        // Status-Spalte with reference
        statusColumn = mitgliederGrid.addColumn(this::getStatusText)
                .setHeader("Status")
                .setAutoWidth(true);

        // Status-Spalte initial verstecken (da wir mit "Aktive Mitglieder" starten)
        statusColumn.setVisible(false);

        mitgliederGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("350px")
                .setFlexGrow(0);

        layout.add(mitgliederGrid);
        return layout;
    }

    /**
     * Erstellt Aktions-Buttons je nach Status.
     */
    private HorizontalLayout createActionButtons(Vereinsmitgliedschaft dto) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");
        layout.setWidth("100%");

        // Für beantragte Mitgliedschaften: Genehmigen/Ablehnen
            if (dto.getStatus() == MitgliedschaftsStatus.BEANTRAGT) {
            Button genehmigenButton = new Button("Genehmigen", e -> genehmigen(dto.getId()));
            // Einheitliches Design: weißer Text auf blauem Hintergrund (Primary)
            genehmigenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(dto.getId()));
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            layout.add(genehmigenButton, ablehnenButton);
        }

        // Für aktive Mitgliedschaften: Entfernen (nur Vereinschef)
        // Aufseher-Verwaltung nur wenn NICHT im "Alle"-Tab
        if (dto.getStatus() == MitgliedschaftsStatus.AKTIV) {
            boolean istVereinschef = currentUser.getVereinsmitgliedschaften().stream()
                    .anyMatch(m -> m.getVerein().getId().equals(aktuellerVerein.getId()) &&
                            Boolean.TRUE.equals(m.getIstVereinschef()));

            if (istVereinschef && !Boolean.TRUE.equals(dto.getIstVereinschef())) {
                // Aufseher-Buttons nur anzeigen, wenn NICHT im "Alle"-Tab
                if (aktuellerTab != alleTab) {
                    Button aufseherButton;
                    if (Boolean.TRUE.equals(dto.getIstAufseher())) {
                        aufseherButton = new Button("Aufseher entziehen",
                                e -> setzeAufseherStatus(dto.getId(), false));
                        aufseherButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                    } else {
                        aufseherButton = new Button("Zu Aufseher ernennen",
                                e -> setzeAufseherStatus(dto.getId(), true));
                        aufseherButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    }
                    layout.add(aufseherButton);
                }

                Button entfernenButton = new Button("Entfernen", e -> mitgliedEntfernen(dto.getId()));
                entfernenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                layout.add(entfernenButton);
            }
        }

        // Für abgelehnte Mitgliedschaften: Lösch-Button
        if (dto.getStatus() == MitgliedschaftsStatus.ABGELEHNT) {
            Button loeschenButton = new Button("Löschen", e -> loescheMitgliedschaft(dto.getId()));
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.getElement().setAttribute("title", "Eintrag endgültig löschen");
            layout.add(loeschenButton);
        }

        return layout;
    }

    /**
     * Zeigt einen Dialog zur Ablehnung mit Begründung.
     */
    private void zeigeAblehnungsDialog(Long anfrageId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Beitrittsanfrage ablehnen");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextArea grundField = new TextArea("Begründung (optional)");
        grundField.setWidthFull();
        grundField.setHeight("150px");

        Button ablehnenButton = new Button("Ablehnen", e -> {
            String grund = grundField.getValue();
            ablehnen(anfrageId, grund);
            dialog.close();
        });
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(ablehnenButton, abbrechenButton);
        dialogLayout.add(grundField, buttons);

        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Genehmigt eine Beitrittsanfrage.
     */
    private void genehmigen(Long anfrageId) {
        try {
            mitgliedschaftService.genehmigeAnfrage(anfrageId);
            Notification.show("Beitrittsanfrage genehmigt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Beitrittsanfrage genehmigt (ID: {})", anfrageId);
        } catch (Exception e) {
            log.error("Fehler beim Genehmigen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Lehnt eine Beitrittsanfrage ab.
     */
    private void ablehnen(Long anfrageId, String grund) {
        try {
            if (grund != null && !grund.trim().isEmpty()) {
                mitgliedschaftService.lehneAnfrageAbMitGrund(anfrageId, grund);
            } else {
                mitgliedschaftService.lehneAnfrageAb(anfrageId);
            }
            Notification.show("Beitrittsanfrage abgelehnt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Beitrittsanfrage abgelehnt (ID: {})", anfrageId);
        } catch (Exception e) {
            log.error("Fehler beim Ablehnen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Setzt den Aufseher-Status eines Mitglieds.
     */
    private void setzeAufseherStatus(Long mitgliedschaftId, boolean istAufseher) {
        try {
            mitgliedschaftService.setzeAufseherStatus(mitgliedschaftId, istAufseher);
            String nachricht = istAufseher ? "Mitglied zu Aufseher ernannt" : "Aufseher-Status entzogen";
            Notification.show(nachricht).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Aufseher-Status geändert (ID: {})", mitgliedschaftId);
        } catch (Exception e) {
            log.error("Fehler beim Ändern des Aufseher-Status", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Entfernt ein Mitglied aus dem Verein.
     */
    private void mitgliedEntfernen(Long mitgliedschaftId) {
        try {
            mitgliedschaftService.mitgliedEntfernen(mitgliedschaftId);
            Notification.show("Mitglied aus Verein entfernt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Mitglied entfernt (ID: {})", mitgliedschaftId);
        } catch (Exception e) {
            log.error("Fehler beim Entfernen des Mitglieds", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Löscht eine Mitgliedschaft endgültig (nur zulässig für ABGELEHNT/VERLASSEN/BEENDET).
     */
    private void loescheMitgliedschaft(Long mitgliedschaftId) {
        try {
            mitgliedschaftService.loescheMitgliedschaft(mitgliedschaftId);
            Notification.show("Mitgliedschaft gelöscht")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Mitgliedschaft gelöscht (ID: {})", mitgliedschaftId);
        } catch (Exception e) {
            log.error("Fehler beim Löschen der Mitgliedschaft", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Grid basierend auf dem ausgewählten Status.
     */
    private void updateGrid() {
        if (aktuellerVerein != null) {
            List<Vereinsmitgliedschaft> mitglieder;

            if (aktuellerStatus == null) {
                // Alle Mitgliedschaften
                mitglieder = mitgliedschaftService.findeAlleMitgliedschaften(aktuellerVerein);
            } else {
                // Nach Status filtern
                mitglieder = mitgliedschaftService.findeMitgliedschaftenNachStatus(aktuellerVerein, aktuellerStatus);
            }

            // Filter nach Suchfeld
            String suchbegriff = suchfeld.getValue();
            if (suchbegriff != null && !suchbegriff.trim().isEmpty()) {
                mitglieder = mitglieder.stream()
                        .filter(m -> (m.getBenutzer().getVorname() + " " + m.getBenutzer().getNachname()).toLowerCase()
                                .contains(suchbegriff.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filter nach Datum NUR wenn "Alle" Tab aktiv ist
            if (aktuellerTab == alleTab) {
                LocalDate von = vonDatum.getValue();
                LocalDate bis = bisDatum.getValue();
                if (von != null && bis != null) {
                    mitglieder = mitglieder.stream()
                            .filter(m -> !m.getBeitrittDatum().isBefore(von) &&
                                    !m.getBeitrittDatum().isAfter(bis))
                            .collect(Collectors.toList());
                }
            }

            log.info("Finale Anzahl im Grid: {}", mitglieder.size());
            mitgliederGrid.setItems(mitglieder);

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = mitglieder.isEmpty();
            mitgliederGrid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    /**
     * Erstellt eine StreamResource für den PDF-Export.
     */
    private StreamResource createPdfResource() {
        return new StreamResource("mitgliedschaften_" + LocalDate.now() + ".pdf", () -> {
            try {
                List<Vereinsmitgliedschaft> mitglieder;

                if (aktuellerStatus == null) {
                    mitglieder = mitgliedschaftService.findeAlleMitgliedschaften(aktuellerVerein);
                } else {
                    mitglieder = mitgliedschaftService.findeMitgliedschaftenNachStatus(aktuellerVerein, aktuellerStatus);
                }

                // Filter anwenden
                String suchbegriff = suchfeld.getValue();
                if (suchbegriff != null && !suchbegriff.trim().isEmpty()) {
                    mitglieder = mitglieder.stream()
                            .filter(m -> (m.getBenutzer().getVorname() + " " + m.getBenutzer().getNachname()).toLowerCase()
                                    .contains(suchbegriff.toLowerCase()))
                            .collect(Collectors.toList());
                }

                LocalDate von = vonDatum.getValue();
                LocalDate bis = bisDatum.getValue();

                byte[] pdfBytes = pdfExportService.exportiereVereinsmitgliedschaften(
                        aktuellerVerein, mitglieder, von, bis);
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
     * Gibt den Rollentext für ein Mitglied zurück.
     */
    private String getRolleText(Vereinsmitgliedschaft dto) {
        if (Boolean.TRUE.equals(dto.getIstVereinschef())) {
            return "Vereinschef";
        } else if (Boolean.TRUE.equals(dto.getIstAufseher())) {
            return "Aufseher";
        }
        return "Mitglied";
    }

    /**
     * Gibt den Statustext zurück.
     */
    private String getStatusText(Vereinsmitgliedschaft dto) {
        return switch (dto.getStatus()) {
            case AKTIV -> "Aktiv";
            case BEANTRAGT -> "Zur Genehmigung";
            case ABGELEHNT -> "Abgelehnt";
            case BEENDET -> "Beendet";
            default -> "Unbekannt";
        };
    }

}
