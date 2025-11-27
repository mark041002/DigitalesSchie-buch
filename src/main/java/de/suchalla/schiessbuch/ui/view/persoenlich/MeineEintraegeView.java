package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.service.VereinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.model.entity.Verband;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.TreeSet;

/**
 * View für die Anzeige der eigenen Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "meine-eintraege", layout = MainLayout.class)
@PageTitle("Meine Einträge | Digitales Schießbuch")
@PermitAll
public class MeineEintraegeView extends VerticalLayout {

    private final SchiessnachweisService schiessnachweisService;
    private final PdfExportService pdfExportService;
    private final VereinService vereinService;
        private final VerbandService verbandService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private Div emptyStateMessage;
    private final Benutzer currentUser;
    private Tab alleTab;
    private Tab unsigniertAbgelehntTab;
    private Tab signiertTab;
    private Tab aktuellerTab;
    private ComboBox<String> vereinFilter;
        private ComboBox<String> disziplinFilter;
        private ComboBox<String> verbandFilter;
        private com.vaadin.flow.component.grid.Grid.Column<SchiessnachweisEintrag> actionsColumn;

        public MeineEintraegeView(SecurityService securityService,
                                                          SchiessnachweisService schiessnachweisService,
                                                          PdfExportService pdfExportService,
                                                          VereinService vereinService,
                                                          VerbandService verbandService) {
        this.schiessnachweisService = schiessnachweisService;
        this.pdfExportService = pdfExportService;
        this.vereinService = vereinService;
                this.verbandService = verbandService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.setWidthFull();

        // Header-Bereich
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("gradient-header");
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-m)");

        // Text-Container
        Div textContainer = new Div();
        H3 title = new H3("Meine Schießnachweis-Einträge");
        title.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-contrast-color)")
                .set("font-size", "clamp(1.2rem, 4vw, 1.5rem)"); // Responsive Schriftgröße
        textContainer.add(title);

        // Button für neuen Eintrag
        Button neuerEintragButton = new Button("Neuer Eintrag", new Icon(VaadinIcon.PLUS_CIRCLE));
        neuerEintragButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neuerEintragButton.addClassName("neuer-eintrag-btn");
        neuerEintragButton.getStyle()
                .set("flex-shrink", "0")
                .set("white-space", "nowrap");
        neuerEintragButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate(NeuerEintragView.class))
        );

        header.add(textContainer, neuerEintragButton);
        contentWrapper.add(header);

        // Info-Box separat unter dem Header
        Div infoBox = ViewComponentHelper.createInfoBox("Filtern Sie Ihre Einträge nach Datum oder exportieren Sie diese als PDF-Datei.");
        contentWrapper.add(infoBox);

        // Tabs für Status-Filter
        alleTab = new Tab("Alle");
        unsigniertAbgelehntTab = new Tab("Unsigniert/Abgelehnt");
        signiertTab = new Tab("Signiert");
        aktuellerTab = alleTab;
        Tabs tabs = new Tabs(alleTab, unsigniertAbgelehntTab, signiertTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            aktuellerTab = event.getSelectedTab();
            updateGrid();
        });
        contentWrapper.add(tabs);


        // Datum-Filter standardmäßig leer
        vonDatum.setWidth("200px");

        bisDatum.setWidth("200px");

        // Verband-Filter (anfangs leer -> zeigt alle)
        verbandFilter = new ComboBox<>("Verband");
        verbandFilter.setWidth("250px");
        verbandFilter.setPlaceholder("");
        verbandFilter.setAllowCustomValue(false);
        verbandFilter.addValueChangeListener(e -> updateGrid());

        // Disziplin-Filter (anfangs leer -> zeigt alle)
        disziplinFilter = new ComboBox<>("Disziplin");
        disziplinFilter.setWidth("250px");
        disziplinFilter.setPlaceholder("");
        disziplinFilter.setAllowCustomValue(false);
        disziplinFilter.addValueChangeListener(e -> updateGrid());

        // Vereinsfilter (anfangs leer -> zeigt alle)
        vereinFilter = new ComboBox<>("Verein");
        vereinFilter.setWidth("250px");
        vereinFilter.setPlaceholder(""); // leer anzeigen, damit alle selektiert werden
        vereinFilter.setAllowCustomValue(false);
        vereinFilter.addValueChangeListener(e -> updateGrid());

        // Filter-Button
        Button filterButton = new Button("Filtern", new Icon(VaadinIcon.FILTER));
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addClickListener(e -> updateGrid());

        // PDF-Download als Anchor
        Anchor pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren", new Icon(VaadinIcon.DOWNLOAD));
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        // Alles in einem HorizontalLayout nebeneinander
        HorizontalLayout filterLayout = new HorizontalLayout(vonDatum, bisDatum, verbandFilter, disziplinFilter, vereinFilter, filterButton, pdfDownload);
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setSpacing(false);
        filterLayout.setPadding(false);
        filterLayout.setWidthFull();
        filterLayout.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-m)")
                .set("box-sizing", "border-box");

        // Filter-Container
        contentWrapper.add(ViewComponentHelper.createFilterBox(filterLayout));


        // Grid-Container mit weißem Hintergrund
        Div gridContainer = ViewComponentHelper.createGridContainer();

        // Grid mit modernem Styling - jetzt mit DTOs (flache Struktur)
        grid.addClassName("rounded-grid");
        grid.setSizeFull();
        grid.addColumn(dto -> dto.getDatum() == null ? "" : dateFormatter.format(dto.getDatum()))
                .setHeader("Datum")
                .setSortable(true);

        grid.addColumn(eintrag -> eintrag.getDisziplin() != null ? eintrag.getDisziplin().getProgramm() : "-")
                .setHeader("Disziplin")
                .setSortable(true);

        // Vereinsspalte anzeigen (direkt aus DTO)
        grid.addColumn(eintrag -> eintrag.getSchiesstand() != null && eintrag.getSchiesstand().getVerein() != null ? eintrag.getSchiesstand().getVerein().getName() : null != null ? eintrag.getSchiesstand() != null && eintrag.getSchiesstand().getVerein() != null ? eintrag.getSchiesstand().getVerein().getName() : null : "-")
                .setHeader("Verein")
                .setSortable(true);

        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber")
                .setSortable(true);
                
        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart")
                .setSortable(true);
                
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true);

        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status");

        grid.addColumn(eintrag -> eintrag.getAufseher() != null ? eintrag.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher")
                .setSortable(true);

        actionsColumn = grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen");

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.getColumns().forEach(c -> c.setFlexGrow(1));

        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Empty State Message erstellen
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage(
                "Keine Einträge im ausgewählten Zeitraum gefunden. Erstellen Sie einen neuen Eintrag über den Button oben.",
                VaadinIcon.BOOK
        );
        emptyStateMessage.setVisible(false);

        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);
        add(contentWrapper);

        // Vereinsfilter und andere Optionen laden (über Service)
        updateFilterOptions();
    }


    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param eintrag Der Eintrag (DTO)
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
                Button deleteButton = new Button("Löschen", new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.TRASH));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

                // Zeige Löschen-Button nur wenn Eintrag NICHT signiert ist
                if (!(eintrag.getStatus() != null && eintrag.getStatus() == de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)) {
                        deleteButton.addClickListener(e -> deleteEintrag(eintrag.getId()));
                        layout.add(deleteButton);
                }

        return layout;
    }

    /**
     * Löscht einen Eintrag anhand der ID.
     *
     * @param eintragId Die ID des zu löschenden Eintrags
     */
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
     * Aktualisiert das Grid mit Einträgen (jetzt mit DTOs).
     */
    private void updateGrid() {
        if (currentUser != null) {
            LocalDate von = vonDatum.getValue();
            LocalDate bis = bisDatum.getValue();
            List<SchiessnachweisEintrag> eintraege;
            if (von != null && bis != null) {
                eintraege = schiessnachweisService.findeEintraegeImZeitraum(currentUser, von, bis);
            } else {
                eintraege = schiessnachweisService.findeEintraegeFuerSchuetze(currentUser);
            }

            // Tab-Filter anwenden
            if (aktuellerTab == signiertTab) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)
                        .toList();
            } else if (aktuellerTab == unsigniertAbgelehntTab) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == de.suchalla.schiessbuch.model.enums.EintragStatus.UNSIGNIERT
                                || e.getStatus() == de.suchalla.schiessbuch.model.enums.EintragStatus.ABGELEHNT
                                || e.getStatus() == de.suchalla.schiessbuch.model.enums.EintragStatus.OFFEN)
                        .toList();
            }
            // Bei 'Alle' keine Statusfilterung

                        // Disziplinfilter anwenden (direkt über DTO-Felder)
                        String disziplinFilterValue = disziplinFilter.getValue();
                        if (disziplinFilterValue != null && !disziplinFilterValue.isEmpty()) {
                                eintraege = eintraege.stream()
                                                .filter(e -> e.getDisziplin() != null &&
                                                             e.getDisziplin().getProgramm() != null &&
                                                             e.getDisziplin().getProgramm().equals(disziplinFilterValue))
                                                .toList();
                        }

                        // Vereinsfilter anwenden
                        String vereinFilterValue = vereinFilter.getValue();
                        if (vereinFilterValue != null && !vereinFilterValue.isEmpty()) {
                                eintraege = eintraege.stream()
                                                .filter(e -> e.getSchiesstand() != null &&
                                                             e.getSchiesstand().getVerein() != null &&
                                                             e.getSchiesstand().getVerein().getName().equals(vereinFilterValue))
                                                .toList();
                        }

                        // Verband-Filter anwenden: finde Vereine des ausgewählten Verbands und filtere nach vereinId
                        String verbandFilterValue = verbandFilter.getValue();
                        if (verbandFilterValue != null && !verbandFilterValue.isEmpty()) {
                                Verband selected = verbandService.findeAlleVerbaendeEntities().stream()
                                                .filter(v -> v.getName() != null && v.getName().equals(verbandFilterValue))
                                                .findFirst().orElse(null);
                                if (selected != null) {
                                        Set<Long> vereinIds = verbandService.findeVereineVonVerband(selected).stream()
                                                        .map(v -> v.getId())
                                                        .filter(Objects::nonNull)
                                                        .collect(Collectors.toSet());

                                        eintraege = eintraege.stream()
                                                        .filter(e -> e.getSchiesstand() != null &&
                                                                     e.getSchiesstand().getVerein() != null &&
                                                                     vereinIds.contains(e.getSchiesstand().getVerein().getId()))
                                                        .toList();
                                } else {
                                        eintraege = List.of();
                                }
                        }

            grid.setItems(eintraege);

                        // Aktionen-Spalte im Signiert-Tab ausblenden
                        if (actionsColumn != null) {
                                actionsColumn.setVisible(!(aktuellerTab == signiertTab));
                        }

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = eintraege.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    /**
     * Aktualisiert die Filteroptionen für den Vereins-Filter basierend auf den vorhandenen Vereinen.
     */
    private void updateFilterOptions() {
        // Lade Vereinsnamen sicher über den Service, um LazyInitializationExceptions zu vermeiden
        Set<String> vereinNames = vereinService.findAllVereinsnamen().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        // Setze Items; nicht vorauswählen (leer bleibt, zeigt alle)
        vereinFilter.setItems(vereinNames);
        // Disziplinnamen aus vorhandenen Einträgen laden (zeigt alle, wenn leer)
        Set<String> disziplinNames = new TreeSet<>();
        if (currentUser != null) {
            disziplinNames = schiessnachweisService.findeEintraegeFuerSchuetze(currentUser).stream()
                    .map(eintrag -> eintrag.getDisziplin() != null ? eintrag.getDisziplin().getProgramm() : "-")
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        disziplinFilter.setItems(disziplinNames);
        // Verbandnamen ebenfalls laden (anfangs leer -> zeigt alle)
        Set<String> verbandNames = verbandService.findeAlleVerbaendeEntities().stream()
                .map(Verband::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        verbandFilter.setItems(verbandNames);
    }

    /**
     * Erstellt eine StreamResource für den PDF-Export (jetzt mit DTOs).
     *
     * @return StreamResource
     */
    private StreamResource createPdfResource() {
        return new StreamResource("schiessnachweise.pdf", () -> {
            try {
                LocalDate von = vonDatum.getValue();
                LocalDate bis = bisDatum.getValue();
                LocalDate vonEff = von != null ? von : LocalDate.now().minusMonths(3);
                LocalDate bisEff = bis != null ? bis : LocalDate.now();

                // Service gibt jetzt DTOs zurück
                List<SchiessnachweisEintrag> eintraege = schiessnachweisService
                        .findeSignierteEintraegeImZeitraum(currentUser, vonEff, bisEff);

                // BenutzerMapper wird nicht benötigt, da currentUser noch Entity ist (für Security)
                // Aber wir erstellen ein DTO für den PDF-Export
                de.suchalla.schiessbuch.model.dto.BenutzerDTO schuetzeDTO = de.suchalla.schiessbuch.model.dto.BenutzerDTO.builder()
                        .id(currentUser.getId())
                        .vorname(currentUser.getVorname())
                        .nachname(currentUser.getNachname())
                        .email(currentUser.getEmail())
                        .build();

                byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(schuetzeDTO, eintraege, vonEff, bisEff);
                return new ByteArrayInputStream(pdfBytes);
            } catch (Exception e) {
                Notification.show("Fehler beim PDF-Export: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return new ByteArrayInputStream(new byte[0]);
            }
        });
    }

    /**
     * Erstellt ein farbiges Status-Badge (jetzt mit DTO).
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
            case OFFEN, UNSIGNIERT -> {
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
