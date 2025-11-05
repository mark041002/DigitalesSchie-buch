package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View für die Verwaltung und Anzeige von PKI-Zertifikaten.
 * - Aufseher: Nur eigenes Zertifikat
 * - Vereinschefs: Vereinszertifikate + eigenes Zertifikat (OHNE Root CA)
 * - Admins: Alle Zertifikate (inkl. Root CA)
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "zertifikate", layout = MainLayout.class)
@PageTitle("Zertifikate | Digitales Schießbuch")
@PermitAll
@Slf4j
public class ZertifikateView extends VerticalLayout {

    private final DigitalesZertifikatRepository zertifikatRepository;
    private final SecurityService securityService;
    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<DigitalesZertifikat> grid;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private List<DigitalesZertifikat> allZertifikate = new ArrayList<>();
    private TextField searchField;
    private ComboBox<String> typFilter;
    private ComboBox<String> statusFilter;

    public ZertifikateView(
            DigitalesZertifikatRepository zertifikatRepository,
            SecurityService securityService,
            VereinsmitgliedschaftService mitgliedschaftService) {

        this.zertifikatRepository = zertifikatRepository;
        this.securityService = securityService;
        this.mitgliedschaftService = mitgliedschaftService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H2 title = new H2("PKI-Zertifikate");
        Paragraph description = new Paragraph(
                "Hier können Sie Ihre digitalen Zertifikate einsehen, die für die Signierung von Schießnachweisen verwendet werden."
        );

        add(title, description);

        // Filter erstellen
        add(createFilterBar());

        // Grid für Zertifikate
        grid = new Grid<>(DigitalesZertifikat.class, false);
        grid.setSizeFull();
        configureGrid();

        add(grid);

        // Daten laden
        loadZertifikate();
    }

    private HorizontalLayout createFilterBar() {
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setSpacing(true);

        // Suchfeld
        searchField = new TextField();
        searchField.setPlaceholder("Suchen nach Seriennummer, Inhaber...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());
        searchField.setWidth("300px");

        // Typ-Filter
        typFilter = new ComboBox<>("Typ");
        typFilter.setItems("Alle", "Root CA", "Verein", "Aufseher");
        typFilter.setValue("Alle");
        typFilter.addValueChangeListener(e -> applyFilters());
        typFilter.setWidth("150px");

        // Status-Filter
        statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Alle", "Gültig", "Widerrufen");
        statusFilter.setValue("Alle");
        statusFilter.addValueChangeListener(e -> applyFilters());
        statusFilter.setWidth("150px");

        // Reset-Button
        Button resetButton = new Button("Filter zurücksetzen", VaadinIcon.REFRESH.create());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> resetFilters());

        filterBar.add(searchField, typFilter, statusFilter, resetButton);
        filterBar.setAlignItems(Alignment.END);

        return filterBar;
    }

    private void applyFilters() {
        List<DigitalesZertifikat> filtered = allZertifikate.stream()
                .filter(this::matchesSearchFilter)
                .filter(this::matchesTypFilter)
                .filter(this::matchesStatusFilter)
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }

    private boolean matchesSearchFilter(DigitalesZertifikat zert) {
        if (searchField.isEmpty()) {
            return true;
        }

        String searchTerm = searchField.getValue().toLowerCase();

        // Durchsuche Seriennummer
        if (zert.getSeriennummer().toLowerCase().contains(searchTerm)) {
            return true;
        }

        // Durchsuche Inhaber
        if (zert.getBenutzer() != null &&
            zert.getBenutzer().getVollstaendigerName().toLowerCase().contains(searchTerm)) {
            return true;
        }

        if (zert.getVerein() != null &&
            zert.getVerein().getName().toLowerCase().contains(searchTerm)) {
            return true;
        }

        // Durchsuche Subject DN
        if (zert.getSubjectDN().toLowerCase().contains(searchTerm)) {
            return true;
        }

        return false;
    }

    private boolean matchesTypFilter(DigitalesZertifikat zert) {
        if (typFilter.isEmpty() || "Alle".equals(typFilter.getValue())) {
            return true;
        }

        String selectedTyp = typFilter.getValue();
        String zertTyp = zert.getZertifikatsTyp();

        switch (selectedTyp) {
            case "Root CA":
                return "ROOT".equals(zertTyp);
            case "Verein":
                return "VEREIN".equals(zertTyp);
            case "Aufseher":
                return "AUFSEHER".equals(zertTyp);
            default:
                return true;
        }
    }

    private boolean matchesStatusFilter(DigitalesZertifikat zert) {
        if (statusFilter.isEmpty() || "Alle".equals(statusFilter.getValue())) {
            return true;
        }

        String selectedStatus = statusFilter.getValue();

        switch (selectedStatus) {
            case "Gültig":
                return zert.istGueltig() && !zert.getWiderrufen();
            case "Widerrufen":
                return zert.getWiderrufen();
            default:
                return true;
        }
    }

    private void resetFilters() {
        searchField.clear();
        typFilter.setValue("Alle");
        statusFilter.setValue("Alle");
        applyFilters();
    }

    private void configureGrid() {
        grid.addColumn(zert -> switch (zert.getZertifikatsTyp()) {
            case "ROOT" -> "Root CA";
            case "VEREIN" -> "Verein";
            case "AUFSEHER" -> "Aufseher";
            default -> zert.getZertifikatsTyp();
        }).setHeader("Typ").setSortable(true).setAutoWidth(true);

        grid.addColumn(DigitalesZertifikat::getSeriennummer)
                .setHeader("Seriennummer")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(zert -> {
            if (zert.getBenutzer() != null) {
                return zert.getBenutzer().getVollstaendigerName();
            } else if (zert.getVerein() != null) {
                return zert.getVerein().getName();
            } else {
                return "Digitales Schießbuch";
            }
        }).setHeader("Inhaber").setSortable(true).setAutoWidth(true);

        grid.addColumn(zert -> zert.getGueltigAb().format(dateFormatter))
                .setHeader("Gültig ab")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(zert -> zert.getGueltigBis() != null ? zert.getGueltigBis().format(dateFormatter) : "Unbegrenzt")
                .setHeader("Gültig bis")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActionsColumn)
                .setHeader("Aktionen")
                .setAutoWidth(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
    }

    private Span createStatusBadge(DigitalesZertifikat zertifikat) {
        Span badge = new Span();
        if (zertifikat.istGueltig()) {
            badge.setText("Gültig");
            badge.getElement().getThemeList().add("badge success");
        } else if (zertifikat.getWiderrufen()) {
            badge.setText("Widerrufen");
            badge.getElement().getThemeList().add("badge error");
        } else {
            badge.setText("Abgelaufen");
            badge.getElement().getThemeList().add("badge");
        }
        return badge;
    }

    private HorizontalLayout createActionsColumn(DigitalesZertifikat zertifikat) {
        Button detailsButton = new Button("Details", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        detailsButton.addClickListener(e -> showDetails(zertifikat));

        return new HorizontalLayout(detailsButton);
    }

    private void loadZertifikate() {
        Benutzer currentUser = securityService.getAuthenticatedUser()
                .orElse(null);
        if (currentUser == null) {
            log.warn("Kein authentifizierter Benutzer gefunden");
            return;
        }

        allZertifikate.clear();

        if (currentUser.istAdmin()) {
            // Admins sehen ALLE Zertifikate inkl. Root CA
            log.info("Lade alle Zertifikate für Admin: {}", currentUser.getEmail());
            allZertifikate = zertifikatRepository.findAllWithDetails();
            log.info("Gefundene Zertifikate für Admin: {}", allZertifikate.size());
        } else {
            // Eigenes Zertifikat laden (für alle Benutzer)
            zertifikatRepository.findByBenutzerWithDetails(currentUser).ifPresent(zert -> {
                log.info("Eigenes Zertifikat gefunden: {}", zert.getSeriennummer());
                allZertifikate.add(zert);
            });

            // Wenn Vereinschef: Vereinszertifikate laden (OHNE Root CA!)
            if (currentUser.getRolle() == BenutzerRolle.VEREINS_CHEF) {
                List<Verein> vereine = mitgliedschaftService.getVereineWhereUserIsChef(currentUser);
                log.info("Benutzer {} ist Chef von {} Vereinen", currentUser.getEmail(), vereine.size());

                for (Verein verein : vereine) {
                    List<DigitalesZertifikat> vereinsZertifikate = zertifikatRepository
                            .findByVereinWithDetails(verein)
                            .stream()
                            // WICHTIG: Root CA ausfiltern!
                            .filter(z -> !"ROOT".equals(z.getZertifikatsTyp()))
                            .toList();

                    log.info("Gefundene Vereinszertifikate für {}: {}", verein.getName(), vereinsZertifikate.size());
                    allZertifikate.addAll(vereinsZertifikate);
                }
            }
        }

        log.info("Gesamt geladene Zertifikate: {}", allZertifikate.size());
        applyFilters();
    }

    private void showDetails(DigitalesZertifikat zertifikat) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setSpacing(true);
        detailsLayout.setPadding(true);

        detailsLayout.add(new H2("Zertifikat-Details"));

        // Grundinformationen
        detailsLayout.add(createDetailField("Typ", zertifikat.getZertifikatsTyp()));
        detailsLayout.add(createDetailField("Seriennummer", zertifikat.getSeriennummer()));
        detailsLayout.add(createDetailField("Subject DN", zertifikat.getSubjectDN()));
        detailsLayout.add(createDetailField("Issuer DN", zertifikat.getIssuerDN()));
        detailsLayout.add(createDetailField("Gültig von", zertifikat.getGueltigAb().format(dateFormatter)));
        detailsLayout.add(createDetailField("Gültig bis", zertifikat.getGueltigBis() != null ? zertifikat.getGueltigBis().format(dateFormatter) : "Unbegrenzt"));
        detailsLayout.add(createDetailField("Status", zertifikat.istGueltig() ? "Gültig" : "Ungültig"));

        if (zertifikat.getWiderrufen()) {
            detailsLayout.add(createDetailField("Widerrufen am",
                zertifikat.getWiderrufenAm() != null ? zertifikat.getWiderrufenAm().format(dateFormatter) : ""));
            detailsLayout.add(createDetailField("Widerrufsgrund",
                zertifikat.getWiderrufsGrund() != null ? zertifikat.getWiderrufsGrund() : ""));
        }

        // Zertifikat im PEM-Format anzeigen
        TextArea pemArea = new TextArea("Zertifikat (PEM-Format)");
        pemArea.setValue(zertifikat.getZertifikatPEM());
        pemArea.setReadOnly(true);
        pemArea.setWidthFull();
        pemArea.setHeight("300px");
        detailsLayout.add(pemArea);

        // Dialog erstellen
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");
        dialog.add(detailsLayout);

        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeButton);

        dialog.open();
    }

    private HorizontalLayout createDetailField(String label, String value) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.setWidth("200px");

        Span valueSpan = new Span(value != null ? value : "");

        layout.add(labelSpan, valueSpan);
        return layout;
    }
}
