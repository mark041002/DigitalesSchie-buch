package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View für Aufseher und Vereinschefs zur Verwaltung von Mitgliedschaften.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "mitgliedsverwaltung", layout = MainLayout.class)
@PageTitle("Mitgliedsverwaltung | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "AUFSEHER", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class MitgliedschaftenVerwaltenView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final PdfExportService pdfExportService;
    private final Benutzer currentUser;
    private final VereinRepository vereinRepository;

    private final Grid<Vereinsmitgliedschaft> mitgliederGrid = new Grid<>(Vereinsmitgliedschaft.class, false);

    private final TextField suchfeld = new TextField();
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");
    private final Button filterButton = new Button("Filtern");
    private HorizontalLayout filterLayout;
    private Anchor pdfDownload;

    private Verein aktuellerVerein;
    private MitgliedschaftStatus aktuellerStatus = MitgliedschaftStatus.BEANTRAGT;
    private Tab aktuellerTab;
    private Tab alleTab;
    private Grid.Column<Vereinsmitgliedschaft> statusColumn;

    public MitgliedschaftenVerwaltenView(SecurityService securityService,
                                         VereinsmitgliedschaftService mitgliedschaftService,
                                         PdfExportService pdfExportService,
                                         VereinRepository vereinRepository) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.pdfExportService = pdfExportService;
        this.vereinRepository = vereinRepository;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(true);
        setPadding(true);

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
                    .ifPresent(mitgliedschaft -> {
                        Long vereinId = mitgliedschaft.getVerein().getId();
                        aktuellerVerein = vereinRepository.findById(vereinId).orElse(null);
                    });
        }
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        if (aktuellerVerein == null) {
            add(new H2("Sie sind kein Aufseher oder Vereinschef in einem Verein"));
            return;
        }

        add(new H2("Mitgliedsverwaltung - " + aktuellerVerein.getName()));

        // Tabs für Status-Filter
        Tab genehmigtTab = new Tab("Aktive Mitglieder");
        Tab beantragtTab = new Tab("Zur Genehmigung");
        Tab abgelehntTab = new Tab("Abgelehnte");
        alleTab = new Tab("Alle");

        // Setze initialen Tab
        aktuellerTab = genehmigtTab;

        Tabs tabs = new Tabs(genehmigtTab, beantragtTab, abgelehntTab, alleTab);
        tabs.setWidthFull();

        // CSS für größeren Indikator-Balken
        tabs.getElement().getStyle()
                .set("--lumo-size-xs", "4px");

        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            aktuellerTab = selectedTab;

            if (selectedTab == beantragtTab) {
                aktuellerStatus = MitgliedschaftStatus.BEANTRAGT;
            } else if (selectedTab == genehmigtTab) {
                aktuellerStatus = MitgliedschaftStatus.AKTIV;
            } else if (selectedTab == abgelehntTab) {
                aktuellerStatus = MitgliedschaftStatus.ABGELEHNT;
            } else {
                aktuellerStatus = null; // Alle
            }

            // Rebuild filter layout basierend auf Tab
            updateFilterLayout(selectedTab == alleTab);

            // Zeige/Verstecke Status-Spalte
            if (statusColumn != null) {
                statusColumn.setVisible(selectedTab == alleTab);
            }

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

        vonDatum.setValue(LocalDate.now().minusYears(1));
        bisDatum.setValue(LocalDate.now());
        filterButton.addClickListener(e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // PDF-Download
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
        mitgliederGrid.addColumn(m -> m.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setSortable(true);
        mitgliederGrid.addColumn(m -> m.getBenutzer().getEmail())
                .setHeader("E-Mail");
        mitgliederGrid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Beitrittsdatum")
                .setSortable(true);
        mitgliederGrid.addColumn(this::getRolleText)
                .setHeader("Rolle");

        // Status-Spalte mit Referenz speichern
        statusColumn = mitgliederGrid.addColumn(this::getStatusText)
                .setHeader("Status");

        // Status-Spalte initial verstecken (da wir mit "Aktive Mitglieder" starten)
        statusColumn.setVisible(false);

        mitgliederGrid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen");

        layout.add(mitgliederGrid);
        return layout;
    }

    /**
     * Erstellt Aktions-Buttons je nach Status.
     */
    private HorizontalLayout createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        HorizontalLayout layout = new HorizontalLayout();

        // Für beantragte Mitgliedschaften: Genehmigen/Ablehnen
        if (mitgliedschaft.getStatus() == MitgliedschaftStatus.BEANTRAGT) {
            Button genehmigenButton = new Button("Genehmigen", e -> genehmigen(mitgliedschaft));
            genehmigenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

            Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(mitgliedschaft));
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

            layout.add(genehmigenButton, ablehnenButton);
        }

        // Für aktive Mitgliedschaften: Aufseher-Status ändern, Entfernen (nur Vereinschef)
        if (mitgliedschaft.getStatus() == MitgliedschaftStatus.AKTIV) {
            boolean istVereinschef = currentUser.getVereinsmitgliedschaften().stream()
                    .anyMatch(m -> m.getVerein().getId().equals(aktuellerVerein.getId()) &&
                            Boolean.TRUE.equals(m.getIstVereinschef()));

            if (istVereinschef && !Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
                Button aufseherButton;
                if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
                    aufseherButton = new Button("Aufseher entziehen",
                            e -> setzeAufseherStatus(mitgliedschaft, false));
                    aufseherButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                } else {
                    aufseherButton = new Button("Zu Aufseher ernennen",
                            e -> setzeAufseherStatus(mitgliedschaft, true));
                    aufseherButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                }
                layout.add(aufseherButton);

                Button entfernenButton = new Button("Entfernen", e -> mitgliedEntfernen(mitgliedschaft));
                entfernenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                layout.add(entfernenButton);
            }
        }

        return layout;
    }

    /**
     * Zeigt einen Dialog zur Ablehnung mit Begründung.
     */
    private void zeigeAblehnungsDialog(Vereinsmitgliedschaft anfrage) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Beitrittsanfrage ablehnen");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextArea grundField = new TextArea("Begründung (optional)");
        grundField.setWidthFull();
        grundField.setHeight("150px");

        Button ablehnenButton = new Button("Ablehnen", e -> {
            String grund = grundField.getValue();
            ablehnen(anfrage, grund);
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
    private void genehmigen(Vereinsmitgliedschaft anfrage) {
        try {
            mitgliedschaftService.genehmigeAnfrage(anfrage.getId());
            Notification.show("Beitrittsanfrage genehmigt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Beitrittsanfrage genehmigt für Benutzer: {}", anfrage.getBenutzer().getEmail());
        } catch (Exception e) {
            log.error("Fehler beim Genehmigen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Lehnt eine Beitrittsanfrage ab.
     */
    private void ablehnen(Vereinsmitgliedschaft anfrage, String grund) {
        try {
            if (grund != null && !grund.trim().isEmpty()) {
                mitgliedschaftService.lehneAnfrageAbMitGrund(anfrage.getId(), grund);
            } else {
                mitgliedschaftService.lehneAnfrageAb(anfrage.getId());
            }
            Notification.show("Beitrittsanfrage abgelehnt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Beitrittsanfrage abgelehnt für Benutzer: {}", anfrage.getBenutzer().getEmail());
        } catch (Exception e) {
            log.error("Fehler beim Ablehnen der Anfrage", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Setzt den Aufseher-Status eines Mitglieds.
     */
    private void setzeAufseherStatus(Vereinsmitgliedschaft mitgliedschaft, boolean istAufseher) {
        try {
            mitgliedschaftService.setzeAufseherStatus(mitgliedschaft.getId(), istAufseher);
            String nachricht = istAufseher ? "Mitglied zu Aufseher ernannt" : "Aufseher-Status entzogen";
            Notification.show(nachricht).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Aufseher-Status geändert für Benutzer: {}", mitgliedschaft.getBenutzer().getEmail());
        } catch (Exception e) {
            log.error("Fehler beim Ändern des Aufseher-Status", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Entfernt ein Mitglied aus dem Verein.
     */
    private void mitgliedEntfernen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.mitgliedEntfernen(mitgliedschaft.getId());
            Notification.show("Mitglied aus Verein entfernt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
            log.info("Mitglied entfernt: {}", mitgliedschaft.getBenutzer().getEmail());
        } catch (Exception e) {
            log.error("Fehler beim Entfernen des Mitglieds", e);
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
                        .filter(m -> m.getBenutzer().getVollstaendigerName().toLowerCase()
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
                            .filter(m -> m.getBenutzer().getVollstaendigerName().toLowerCase()
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
    private String getRolleText(Vereinsmitgliedschaft mitgliedschaft) {
        if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
            return "Vereinschef";
        } else if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
            return "Aufseher";
        }
        return "Mitglied";
    }

    /**
     * Gibt den Statustext zurück.
     */
    private String getStatusText(Vereinsmitgliedschaft mitgliedschaft) {
        return switch (mitgliedschaft.getStatus()) {
            case AKTIV -> "Aktiv";
            case BEANTRAGT -> "Zur Genehmigung";
            case ABGELEHNT -> "Abgelehnt";
            case BEENDET -> "Beendet";
            default -> "Unbekannt";
        };
    }

}
