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
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragListDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.service.VereinService;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
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

    private final Grid<SchiessnachweisEintragListDTO> grid = new Grid<>(SchiessnachweisEintragListDTO.class, false);
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private Div emptyStateMessage;
    private final Benutzer currentUser;
    private Tab alleTab;
    private Tab unsigniertAbgelehntTab;
    private Tab signiertTab;
    private Tab aktuellerTab;
    private ComboBox<String> vereinFilter;

    public MeineEintraegeView(SecurityService securityService,
                              SchiessnachweisService schiessnachweisService,
                              PdfExportService pdfExportService,
                              VereinService vereinService) {
        this.schiessnachweisService = schiessnachweisService;
        this.pdfExportService = pdfExportService;
        this.vereinService = vereinService;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

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
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("gradient-header");
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        // Text-Container
        Div textContainer = new Div();
        H3 title = new H3("Meine Schießnachweis-Einträge");
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "var(--lumo-primary-contrast-color)"); // Überschrift weiß
        textContainer.add(title);

        // Button für neuen Eintrag
        Button neuerEintragButton = new Button("Neuer Eintrag", new Icon(VaadinIcon.PLUS_CIRCLE));
        neuerEintragButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        neuerEintragButton.addClassName("neuer-eintrag-btn");
        neuerEintragButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate(NeuerEintragView.class))
        );

        header.add(textContainer, neuerEintragButton);
        contentWrapper.add(header);

        // Info-Box separat unter dem Header
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Filtern Sie Ihre Einträge nach Datum oder exportieren Sie diese als PDF-Datei."
        );
        beschreibung.getStyle().set("margin", "0");
        infoBox.add(infoIcon, beschreibung);
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

        // Filter-Bereich
        Div filterBox = new Div();
        filterBox.addClassName("filter-box");
        filterBox.setWidthFull();
        filterBox.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Datum-Filter standardmäßig leer
        vonDatum.setWidth("200px");

        bisDatum.setWidth("200px");

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
        HorizontalLayout filterLayout = new HorizontalLayout(vonDatum, bisDatum, vereinFilter, filterButton, pdfDownload);
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setSpacing(true);
        filterLayout.setWidthFull();
        filterLayout.getStyle().set("flex-wrap", "wrap").set("margin-top", "var(--lumo-space-m)");

        filterBox.add(filterLayout);
        contentWrapper.add(filterBox);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid mit modernem Styling - jetzt mit DTOs (flache Struktur)
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        grid.addColumn(SchiessnachweisEintragListDTO::getDatum)
                .setHeader("Datum")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintragListDTO::getDisziplinName)
                .setHeader("Disziplin")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Vereinsspalte anzeigen (direkt aus DTO)
        grid.addColumn(eintrag -> eintrag.getVereinName() != null ? eintrag.getVereinName() : "-")
                .setHeader("Verein")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintragListDTO::getKaliber)
                .setHeader("Kaliber")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintragListDTO::getWaffenart)
                .setHeader("Waffenart")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintragListDTO::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(SchiessnachweisEintragListDTO::getErgebnis)
                .setHeader("Ergebnis")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintragListDTO::getAufseherVollstaendigerName)
                .setHeader("Aufseher")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setAutoWidth(true);

        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Keine Einträge im ausgewählten Zeitraum gefunden. Erstellen Sie einen neuen Eintrag über den Button oben.");
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

        // Vereinsfilter und andere Optionen laden (über Service)
        updateFilterOptions();
    }


    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param eintrag Der Eintrag (DTO)
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintragListDTO eintrag) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        Button deleteButton = new Button("Löschen", new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteButton.addClickListener(e -> deleteEintrag(eintrag.getId()));
        layout.add(deleteButton);

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
            List<SchiessnachweisEintragListDTO> eintraege;
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

            // Vereinsfilter anwenden (jetzt direkt über DTO-Felder)
            String vereinFilterValue = vereinFilter.getValue();
            if (vereinFilterValue != null && !vereinFilterValue.isEmpty()) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getVereinName() != null && e.getVereinName().equals(vereinFilterValue))
                        .toList();
            }

            grid.setItems(eintraege);

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = eintraege.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    /**
     * Aktualisiert die Filteroptionen für Status und Schießstand basierend auf den vorhandenen Einträgen.
     *
     * @param eintraege Die Liste der Einträge
     */
    private void updateFilterOptions() {
        // Lade Vereinsnamen sicher über den Service, um LazyInitializationExceptions zu vermeiden
        Set<String> vereinNames = vereinService.findAllVereinsnamen().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        // Setze Items; nicht vorauswählen (leer bleibt, zeigt alle)
        vereinFilter.setItems(vereinNames);
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
                List<SchiessnachweisEintragListDTO> eintraege = schiessnachweisService
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
    private Span createStatusBadge(SchiessnachweisEintragListDTO eintrag) {
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

    /**
     * Gibt den deutschen Text für einen Status zurück.
     *
     * @param status Der Status
     * @return Deutscher Statustext
     */
    private String getStatusText(de.suchalla.schiessbuch.model.enums.EintragStatus status) {
        return switch (status) {
            case OFFEN -> "Offen";
            case UNSIGNIERT -> "Unsigniert";
            case SIGNIERT -> "Signiert";
            case ABGELEHNT -> "Abgelehnt";
        };
    }
}
