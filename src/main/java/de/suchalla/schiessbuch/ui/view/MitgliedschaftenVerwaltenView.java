package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
@Route(value = "mitgliedschaften-verwalten", layout = MainLayout.class)
@PageTitle("Mitgliedschaften verwalten | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "AUFSEHER", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class MitgliedschaftenVerwaltenView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final PdfExportService pdfExportService;
    private final Benutzer currentUser;

    private final Grid<Vereinsmitgliedschaft> anfrageGrid = new Grid<>(Vereinsmitgliedschaft.class, false);
    private final Grid<Vereinsmitgliedschaft> mitgliederGrid = new Grid<>(Vereinsmitgliedschaft.class, false);

    private final TextField suchfeld = new TextField();
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");

    private Verein aktuellerVerein;
    private VerticalLayout anfrageLayout;
    private VerticalLayout mitgliederLayout;

    public MitgliedschaftenVerwaltenView(SecurityService securityService,
                                         VereinsmitgliedschaftService mitgliedschaftService,
                                         PdfExportService pdfExportService) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.pdfExportService = pdfExportService;
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
                    .ifPresent(mitgliedschaft -> aktuellerVerein = mitgliedschaft.getVerein());
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

        add(new H2("Mitgliedschaften verwalten - " + aktuellerVerein.getName()));

        // Tabs für Anfragen und Mitglieder
        Tab anfrageTab = new Tab("Offene Anfragen");
        Tab mitgliederTab = new Tab("Aktive Mitglieder");

        Tabs tabs = new Tabs(anfrageTab, mitgliederTab);

        anfrageLayout = createAnfrageLayout();
        mitgliederLayout = createMitgliederLayout();

        // Nur Anfragen-Layout initial sichtbar
        mitgliederLayout.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            anfrageLayout.setVisible(event.getSelectedTab() == anfrageTab);
            mitgliederLayout.setVisible(event.getSelectedTab() == mitgliederTab);
        });

        add(tabs, anfrageLayout, mitgliederLayout);

        updateAnfrageGrid();
        updateMitgliederGrid();
    }

    /**
     * Erstellt das Layout für Beitrittsanfragen.
     */
    private VerticalLayout createAnfrageLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Offene Beitrittsanfragen"));

        // Grid für Beitrittsanfragen
        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setSortable(true);
        anfrageGrid.addColumn(anfrage -> anfrage.getBenutzer().getEmail())
                .setHeader("E-Mail");
        anfrageGrid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Antragsdatum")
                .setSortable(true);
        anfrageGrid.addComponentColumn(this::createAnfrageActionButtons)
                .setHeader("Aktionen");

        layout.add(anfrageGrid);
        return layout;
    }

    /**
     * Erstellt das Layout für aktive Mitglieder.
     */
    private VerticalLayout createMitgliederLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3("Aktive Mitglieder"));

        // Filter
        suchfeld.setPlaceholder("Nach Namen suchen...");
        suchfeld.setWidth("300px");
        suchfeld.addValueChangeListener(e -> updateMitgliederGrid());

        vonDatum.setValue(LocalDate.now().minusYears(1));
        bisDatum.setValue(LocalDate.now());

        Button filterButton = new Button("Filtern", e -> updateMitgliederGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // PDF-Download
        Anchor pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren");
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        FormLayout filterLayout = new FormLayout(suchfeld, vonDatum, bisDatum);
        HorizontalLayout buttonLayout = new HorizontalLayout(filterButton, pdfDownload);

        layout.add(filterLayout, buttonLayout);

        // Grid für aktive Mitglieder
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
        mitgliederGrid.addComponentColumn(this::createMitgliederActionButtons)
                .setHeader("Aktionen");

        layout.add(mitgliederGrid);
        return layout;
    }

    /**
     * Erstellt Aktions-Buttons für Anfragen.
     */
    private HorizontalLayout createAnfrageActionButtons(Vereinsmitgliedschaft anfrage) {
        Button genehmigenButton = new Button("Genehmigen", e -> genehmigen(anfrage));
        genehmigenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(anfrage));
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        return new HorizontalLayout(genehmigenButton, ablehnenButton);
    }

    /**
     * Erstellt Aktions-Buttons für Mitglieder.
     */
    private HorizontalLayout createMitgliederActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        HorizontalLayout layout = new HorizontalLayout();

        // Nur Vereinschef kann Aufseher ernennen und Mitglieder entfernen
        if (Boolean.TRUE.equals(currentUser.getVereinsmitgliedschaften().stream()
                .anyMatch(m -> m.getVerein().equals(aktuellerVerein) && m.getIstVereinschef()))) {

            if (!Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
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
            updateAnfrageGrid();
            updateMitgliederGrid();
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
            updateAnfrageGrid();
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
            updateMitgliederGrid();
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
            updateMitgliederGrid();
            log.info("Mitglied entfernt: {}", mitgliedschaft.getBenutzer().getEmail());
        } catch (Exception e) {
            log.error("Fehler beim Entfernen des Mitglieds", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Anfrage-Grid.
     */
    private void updateAnfrageGrid() {
        if (aktuellerVerein != null) {
            List<Vereinsmitgliedschaft> anfragen = mitgliedschaftService.findeBeitrittsanfragen(aktuellerVerein);
            anfrageGrid.setItems(anfragen);
        }
    }

    /**
     * Aktualisiert das Mitglieder-Grid.
     */
    private void updateMitgliederGrid() {
        if (aktuellerVerein != null) {
            List<Vereinsmitgliedschaft> mitglieder = mitgliedschaftService.findeAktiveMitgliedschaften(aktuellerVerein);

            // Filter nach Suchfeld
            String suchbegriff = suchfeld.getValue();
            if (suchbegriff != null && !suchbegriff.trim().isEmpty()) {
                mitglieder = mitglieder.stream()
                        .filter(m -> m.getBenutzer().getVollstaendigerName().toLowerCase()
                                .contains(suchbegriff.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filter nach Datum
            LocalDate von = vonDatum.getValue();
            LocalDate bis = bisDatum.getValue();
            if (von != null && bis != null) {
                mitglieder = mitglieder.stream()
                        .filter(m -> !m.getBeitrittDatum().isBefore(von) &&
                                     !m.getBeitrittDatum().isAfter(bis))
                        .collect(Collectors.toList());
            }

            mitgliederGrid.setItems(mitglieder);
        }
    }

    /**
     * Erstellt eine StreamResource für den PDF-Export.
     */
    private StreamResource createPdfResource() {
        return new StreamResource("mitgliedschaften_" + LocalDate.now() + ".pdf", () -> {
            try {
                List<Vereinsmitgliedschaft> mitglieder = mitgliedschaftService
                        .findeAktiveMitgliedschaften(aktuellerVerein);

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
}
