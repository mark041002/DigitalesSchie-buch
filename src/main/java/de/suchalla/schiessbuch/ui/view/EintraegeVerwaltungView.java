package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View für Vereinschefs zur Verwaltung aller Einträge ihres Vereins.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "eintraege-verwaltung", layout = MainLayout.class)
@PageTitle("Einträgsverwaltung | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class EintraegeVerwaltungView extends VerticalLayout {

    private final SchiessnachweisService schiessnachweisService;
    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);

    private final TextField personFilter = new TextField("Person");
    private final DatePicker datumVonFilter = new DatePicker("Von");
    private final DatePicker datumBisFilter = new DatePicker("Bis");
    private final ComboBox<EintragStatus> statusFilter = new ComboBox<>("Status");

    private Tabs filterTabs;
    private Tab offeneTab;
    private Tab genehmigteTab;
    private Tab abgelehnteTab;

    private Verein aktuellerVerein;

    public EintraegeVerwaltungView(SecurityService securityService,
                                    SchiessnachweisService schiessnachweisService,
                                    VereinsmitgliedschaftService mitgliedschaftService) {
        this.schiessnachweisService = schiessnachweisService;
        this.mitgliedschaftService = mitgliedschaftService;

        Benutzer currentUser = securityService.getAuthenticatedUser().orElse(null);

        // Hole den Verein des aktuellen Benutzers (ersten Verein, bei dem er Vereinschef ist)
        if (currentUser != null) {
            this.aktuellerVerein = currentUser.getVereinsmitgliedschaften().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                    .findFirst()
                    .map(m -> m.getVerein())
                    .orElse(null);
        }

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Einträgsverwaltung"));

        // Tabs für verschiedene Ansichten
        offeneTab = new Tab("Offen/Zur Genehmigung");
        genehmigteTab = new Tab("Genehmigte");
        abgelehnteTab = new Tab("Abgelehnte");
        Tab alleTab = new Tab("Alle");

        filterTabs = new Tabs(offeneTab, genehmigteTab, abgelehnteTab, alleTab);
        filterTabs.addSelectedChangeListener(event -> updateGrid());

        add(filterTabs);

        // Filter
        personFilter.setPlaceholder("Name filtern...");
        personFilter.setValueChangeMode(ValueChangeMode.LAZY);
        personFilter.addValueChangeListener(e -> updateGrid());

        datumVonFilter.setPlaceholder("Startdatum");
        datumVonFilter.addValueChangeListener(e -> updateGrid());

        datumBisFilter.setPlaceholder("Enddatum");
        datumBisFilter.addValueChangeListener(e -> updateGrid());

        statusFilter.setItems(EintragStatus.values());
        statusFilter.setItemLabelGenerator(EintragStatus::name);
        statusFilter.setPlaceholder("Status wählen...");
        statusFilter.addValueChangeListener(e -> updateGrid());

        HorizontalLayout filterLayout = new HorizontalLayout(personFilter, datumVonFilter, datumBisFilter, statusFilter);
        filterLayout.setDefaultVerticalComponentAlignment(Alignment.END);
        add(filterLayout);

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
        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName())
                .setHeader("Schießstand");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse");
        grid.addColumn(SchiessnachweisEintrag::getStatus)
                .setHeader("Status")
                .setSortable(true);
        grid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                eintrag.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher");
        grid.addColumn(SchiessnachweisEintrag::getSigniertAm)
                .setHeader("Signiert am");
        grid.addColumn(SchiessnachweisEintrag::getAblehnungsgrund)
                .setHeader("Ablehnungsgrund");

        grid.setHeight("600px");
        add(grid);
    }

    /**
     * Aktualisiert das Grid basierend auf den Filtern.
     */
    private void updateGrid() {
        if (aktuellerVerein == null) {
            grid.setItems(List.of());
            return;
        }

        try {
            // Hole alle aktiven Mitgliedschaften des Vereins
            List<Benutzer> vereinsmitglieder = mitgliedschaftService
                    .findeAktiveMitgliedschaften(aktuellerVerein).stream()
                    .map(Vereinsmitgliedschaft::getBenutzer)
                    .collect(Collectors.toList());

            // Sammle alle Einträge aller Vereinsmitglieder
            List<SchiessnachweisEintrag> eintraege = vereinsmitglieder.stream()
                    .flatMap(mitglied -> schiessnachweisService.findeEintraegeVonSchuetze(mitglied).stream())
                    .collect(Collectors.toList());

            // Tab-Filter anwenden
            Tab selectedTab = filterTabs.getSelectedTab();
            if (selectedTab == offeneTab) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == EintragStatus.OFFEN ||
                                   e.getStatus() == EintragStatus.UNSIGNIERT)
                        .collect(Collectors.toList());
            } else if (selectedTab == genehmigteTab) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == EintragStatus.SIGNIERT)
                        .collect(Collectors.toList());
            } else if (selectedTab == abgelehnteTab) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == EintragStatus.ABGELEHNT)
                        .collect(Collectors.toList());
            }
            // Bei alleTab werden alle Einträge angezeigt

            // Personen-Filter
            if (personFilter.getValue() != null && !personFilter.getValue().isEmpty()) {
                String filterText = personFilter.getValue().toLowerCase();
                eintraege = eintraege.stream()
                        .filter(e -> e.getSchuetze().getVollstaendigerName().toLowerCase().contains(filterText))
                        .collect(Collectors.toList());
            }

            // Datums-Filter
            if (datumVonFilter.getValue() != null) {
                LocalDate von = datumVonFilter.getValue();
                eintraege = eintraege.stream()
                        .filter(e -> !e.getDatum().isBefore(von))
                        .collect(Collectors.toList());
            }

            if (datumBisFilter.getValue() != null) {
                LocalDate bis = datumBisFilter.getValue();
                eintraege = eintraege.stream()
                        .filter(e -> !e.getDatum().isAfter(bis))
                        .collect(Collectors.toList());
            }

            // Status-Filter
            if (statusFilter.getValue() != null) {
                eintraege = eintraege.stream()
                        .filter(e -> e.getStatus() == statusFilter.getValue())
                        .collect(Collectors.toList());
            }

            grid.setItems(eintraege);

        } catch (Exception e) {
            log.error("Fehler beim Laden der Einträge", e);
            grid.setItems(List.of());
        }
    }
}
