package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

/**
 * View für Vereinschefs zur Verwaltung aktiver Mitglieder.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "aktive-mitglieder", layout = MainLayout.class)
@PageTitle("Aktive Mitglieder | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class AktiveMitgliederView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final SchiessnachweisService schiessnachweisService;

    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);

    public AktiveMitgliederView(VereinsmitgliedschaftService mitgliedschaftService,
                                SchiessnachweisService schiessnachweisService) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.schiessnachweisService = schiessnachweisService;

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Aktive Vereinsmitglieder"));

        // Grid konfigurieren
        grid.addColumn(m -> m.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setSortable(true);
        grid.addColumn(m -> m.getBenutzer().getEmail())
                .setHeader("E-Mail")
                .setSortable(true);
        grid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Beitrittsdatum")
                .setSortable(true);
        grid.addColumn(m -> m.getIstAufseher() ? "Ja" : "Nein")
                .setHeader("Aufseher")
                .setSortable(true);
        grid.addColumn(m -> m.getVerein().getName())
                .setHeader("Verein");
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen");

        add(grid);
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     * @return Button-Layout
     */
    private HorizontalLayout createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        Button eintraegeButton = new Button("Einträge anzeigen", e -> zeigeEintraege(mitgliedschaft.getBenutzer()));
        eintraegeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button aufseherButton;

        if (mitgliedschaft.getIstAufseher()) {
            aufseherButton = new Button("Aufseher entfernen", e -> aufseherStatusAendern(mitgliedschaft, false));
            aufseherButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        } else {
            aufseherButton = new Button("Zu Aufseher ernennen", e -> aufseherStatusAendern(mitgliedschaft, true));
            aufseherButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        }

        Button kickButton = new Button("Aus Verein entfernen", e -> mitgliedKicken(mitgliedschaft));
        kickButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        return new HorizontalLayout(eintraegeButton, aufseherButton, kickButton);
    }

    /**
     * Zeigt die Schießnachweis-Einträge eines Mitglieds in einem Dialog.
     *
     * @param benutzer Der Benutzer
     */
    private void zeigeEintraege(Benutzer benutzer) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Einträge von " + benutzer.getVollstaendigerName());
        dialog.setWidth("1000px");
        dialog.setMaxHeight("80vh");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);

        // Filter
        DatePicker vonDatum = new DatePicker("Von");
        vonDatum.setValue(LocalDate.now().minusMonths(3));

        DatePicker bisDatum = new DatePicker("Bis");
        bisDatum.setValue(LocalDate.now());

        // Grid für Einträge
        Grid<SchiessnachweisEintrag> eintraegeGrid = new Grid<>(SchiessnachweisEintrag.class, false);
        eintraegeGrid.addColumn(SchiessnachweisEintrag::getDatum).setHeader("Datum").setSortable(true);
        eintraegeGrid.addColumn(eintrag -> eintrag.getDisziplin().getName()).setHeader("Disziplin");
        eintraegeGrid.addColumn(SchiessnachweisEintrag::getKaliber).setHeader("Kaliber");
        eintraegeGrid.addColumn(SchiessnachweisEintrag::getWaffenart).setHeader("Waffenart");
        eintraegeGrid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setClassNameGenerator(item -> "align-right");
        eintraegeGrid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setClassNameGenerator(item -> "align-right");
        eintraegeGrid.addColumn(eintrag -> eintrag.getSchiesstand().getName()).setHeader("Schießstand");
        eintraegeGrid.addColumn(eintrag -> getStatusText(eintrag.getStatus())).setHeader("Status");
        eintraegeGrid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                eintrag.getAufseher().getVollstaendigerName() : "-").setHeader("Aufseher");

        eintraegeGrid.setAllRowsVisible(false);
        eintraegeGrid.setHeight("400px");

        // CSS für rechtsbündige Ausrichtung
        eintraegeGrid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        Button filterButton = new Button("Filtern", e -> {
            LocalDate von = vonDatum.getValue();
            LocalDate bis = bisDatum.getValue();
            List<SchiessnachweisEintrag> eintraege = schiessnachweisService
                    .findeEintraegeImZeitraum(benutzer, von, bis);
            eintraegeGrid.setItems(eintraege);
        });
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout filterLayout = new FormLayout(vonDatum, bisDatum);

        layout.add(new H3("Zeitraum filtern"), filterLayout, filterButton, eintraegeGrid);

        // Initial laden
        List<SchiessnachweisEintrag> eintraege = schiessnachweisService
                .findeEintraegeImZeitraum(benutzer, vonDatum.getValue(), bisDatum.getValue());
        eintraegeGrid.setItems(eintraege);

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(layout);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    /**
     * Gibt den deutschen Text für einen Status zurück.
     *
     * @param status Der Status
     * @return Deutscher Statustext
     */
    private String getStatusText(EintragStatus status) {
        return switch (status) {
            case OFFEN -> "Offen";
            case UNSIGNIERT -> "Unsigniert";
            case SIGNIERT -> "Signiert";
            case ABGELEHNT -> "Abgelehnt";
        };
    }

    /**
     * Ändert den Aufseher-Status eines Mitglieds.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     * @param istAufseher Der neue Aufseher-Status
     */
    private void aufseherStatusAendern(Vereinsmitgliedschaft mitgliedschaft, boolean istAufseher) {
        try {
            mitgliedschaftService.setzeAufseherStatus(mitgliedschaft.getId(), istAufseher);

            String message = istAufseher
                ? mitgliedschaft.getBenutzer().getVollstaendigerName() + " wurde zum Aufseher ernannt"
                : "Aufseher-Status von " + mitgliedschaft.getBenutzer().getVollstaendigerName() + " wurde entfernt";

            Notification.show(message)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Ändern des Aufseher-Status", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Entfernt ein Mitglied aus dem Verein.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     */
    private void mitgliedKicken(Vereinsmitgliedschaft mitgliedschaft) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Mitglied entfernen");
        dialog.setText("Möchten Sie " + mitgliedschaft.getBenutzer().getVollstaendigerName() +
                       " wirklich aus dem Verein entfernen?");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Entfernen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            try {
                mitgliedschaftService.mitgliedEntfernen(mitgliedschaft.getId());
                Notification.show("Mitglied wurde aus dem Verein entfernt")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGrid();
            } catch (Exception e) {
                log.error("Fehler beim Entfernen des Mitglieds", e);
                Notification.show("Fehler: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        // Für den Demo-Verein (ID 1) - in einer echten Anwendung würde man
        // die Vereine des Vereinschefs aus der Datenbank laden
        mitgliedschaftService.findeVerein(1L).ifPresent(verein -> {
            List<Vereinsmitgliedschaft> mitglieder = mitgliedschaftService.findeAktiveMitgliedschaften(verein);
            grid.setItems(mitglieder);
        });
    }
}
