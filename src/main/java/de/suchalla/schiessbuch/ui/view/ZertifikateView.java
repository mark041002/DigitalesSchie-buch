package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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

    private Grid<DigitalesZertifikat> grid;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private List<DigitalesZertifikat> allZertifikate = new ArrayList<>();
    private TextField searchField;
    private ComboBox<String> typFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> vereinFilter;

    public ZertifikateView(
            DigitalesZertifikatRepository zertifikatRepository,
            SecurityService securityService,
            VereinsmitgliedschaftService mitgliedschaftService) {

        this.zertifikatRepository = zertifikatRepository;
        this.securityService = securityService;
        this.mitgliedschaftService = mitgliedschaftService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        loadZertifikate();
    }

    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("PKI-Zertifikate");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box
        Div infoBox = new Div();
        infoBox.addClassName("info-box");

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph description = new Paragraph(
                "Hier können Sie Ihre digitalen Zertifikate einsehen, die für die Signierung von Schießnachweisen verwendet werden."
        );

        infoBox.add(infoIcon, description);
        contentWrapper.add(infoBox);

        // Filter-Container mit grauem Hintergrund
        Div filterContainer = new Div();
        filterContainer.setWidthFull();
        filterContainer.getStyle().set("background", "var(--lumo-contrast-5pct)");
        filterContainer.getStyle().set("padding", "var(--lumo-space-m)");
        filterContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        filterContainer.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        filterContainer.getStyle().set("box-sizing", "border-box");
        filterContainer.add(createFilterBar());
        contentWrapper.add(filterContainer);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid für Zertifikate
        grid = new Grid<>(DigitalesZertifikat.class, false);
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        configureGrid();

        gridContainer.add(grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createFilterBar() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);
        filterLayout.addClassName("filter-bar");
        filterLayout.setAlignItems(Alignment.END);
        // Ensure the filter layout is responsive
        filterLayout.getStyle().set("flex-wrap", "wrap");
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        // Suchfeld
        searchField = new TextField();
        searchField.setPlaceholder("Suchen nach Seriennummer, Inhaber...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());
        searchField.setWidth("300px");
        searchField.getStyle().set("flex-shrink", "0");

        // Typ-Filter
        typFilter = new ComboBox<>("Typ");
        typFilter.setItems("Alle", "Root CA", "Verein", "Aufseher");
        typFilter.setValue("Alle");
        typFilter.addValueChangeListener(e -> applyFilters());
        typFilter.setWidth("150px");
        typFilter.getStyle().set("flex-shrink", "0");

        // Status-Filter
        statusFilter = new ComboBox<>("Status");
        statusFilter.setItems("Alle", "Gültig", "Widerrufen");
        statusFilter.setValue("Alle");
        statusFilter.addValueChangeListener(e -> applyFilters());
        statusFilter.setWidth("150px");
        statusFilter.getStyle().set("flex-shrink", "0");

        // Vereins-Filter
        vereinFilter = new ComboBox<>("Verein");
        vereinFilter.setWidth("200px");
        vereinFilter.addValueChangeListener(e -> applyFilters());
        vereinFilter.getStyle().set("flex-shrink", "0");

        // Reset-Button
        Button resetButton = new Button("Filter zurücksetzen", VaadinIcon.REFRESH.create());
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> resetFilters());
        resetButton.getStyle().set("flex-shrink", "0");

        filterLayout.add(searchField, typFilter, statusFilter, vereinFilter, resetButton);

        return filterLayout;
    }

    private void applyFilters() {
        List<DigitalesZertifikat> filtered = allZertifikate.stream()
                .filter(this::matchesSearchFilter)
                .filter(this::matchesTypFilter)
                .filter(this::matchesStatusFilter)
                .filter(this::matchesVereinFilter)
                .toList();

        grid.setItems(filtered);
    }

    private boolean matchesSearchFilter(DigitalesZertifikat zert) {
        String searchTerm = searchField.getValue();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }

        searchTerm = searchTerm.toLowerCase();
        String seriennummer = zert.getSeriennummer().toLowerCase();
        String inhaber = "";
        if (zert.getBenutzer() != null) {
            inhaber = zert.getBenutzer().getVollstaendigerName().toLowerCase();
        } else if (zert.getVerein() != null) {
            inhaber = zert.getVerein().getName().toLowerCase();
        }

        return seriennummer.contains(searchTerm) || inhaber.contains(searchTerm);
    }

    private boolean matchesTypFilter(DigitalesZertifikat zert) {
        String selectedTyp = typFilter.getValue();
        if (selectedTyp == null || "Alle".equals(selectedTyp)) {
            return true;
        }

        return switch (selectedTyp) {
            case "Root CA" -> "ROOT".equals(zert.getZertifikatsTyp());
            case "Verein" -> "VEREIN".equals(zert.getZertifikatsTyp());
            case "Aufseher" -> "AUFSEHER".equals(zert.getZertifikatsTyp());
            default -> true;
        };
    }

    private boolean matchesStatusFilter(DigitalesZertifikat zert) {
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus == null || "Alle".equals(selectedStatus)) {
            return true;
        }

        return switch (selectedStatus) {
            case "Gültig" -> zert.istGueltig() && !zert.getWiderrufen();
            case "Widerrufen" -> zert.getWiderrufen();
            default -> true;
        };
    }

    private boolean matchesVereinFilter(DigitalesZertifikat zert) {
        String selectedVerein = vereinFilter.getValue();
        if (selectedVerein == null || "Alle".equals(selectedVerein)) {
            return true;
        }

        if (zert.getVerein() != null) {
            return zert.getVerein().getName().equals(selectedVerein);
        }

        return false;
    }

    private void resetFilters() {
        searchField.clear();
        typFilter.setValue("Alle");
        statusFilter.setValue("Alle");
        vereinFilter.setValue("Alle");
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

        // Neue Spalte: Verein
        grid.addColumn(zert -> {
            if (zert.getVerein() != null) {
                return zert.getVerein().getName();
            } else {
                return "-";
            }
        }).setHeader("Verein").setSortable(true).setAutoWidth(true);

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
        if (zertifikat.getWiderrufen()) {
            badge.setText("Widerrufen");
            badge.getStyle().set("background-color", "var(--lumo-error-color)");
            badge.getStyle().set("color", "white");
            badge.getStyle().set("padding", "var(--lumo-space-xs) var(--lumo-space-s)");
            badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            badge.getStyle().set("font-weight", "600");
            badge.getStyle().set("font-size", "var(--lumo-font-size-s)");
            badge.getStyle().set("display", "inline-block");
        } else if (zertifikat.istGueltig()) {
            badge.setText("Gültig");
            badge.getStyle().set("background-color", "var(--lumo-success-color)");
            badge.getStyle().set("color", "white");
            badge.getStyle().set("padding", "var(--lumo-space-xs) var(--lumo-space-s)");
            badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            badge.getStyle().set("font-weight", "600");
            badge.getStyle().set("font-size", "var(--lumo-font-size-s)");
            badge.getStyle().set("display", "inline-block");
        } else {
            badge.setText("Abgelaufen");
            badge.getStyle().set("background-color", "var(--lumo-contrast-50pct)");
            badge.getStyle().set("color", "white");
            badge.getStyle().set("padding", "var(--lumo-space-xs) var(--lumo-space-s)");
            badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
            badge.getStyle().set("font-weight", "600");
            badge.getStyle().set("font-size", "var(--lumo-font-size-s)");
            badge.getStyle().set("display", "inline-block");
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

            // Entferne Duplikate basierend auf Zertifikat-ID
            allZertifikate = allZertifikate.stream()
                    .distinct()
                    .collect(Collectors.toList());
        }

        log.info("Gesamt geladene Zertifikate (nach Duplikat-Entfernung): {}", allZertifikate.size());

        // Vereins-Filter initialisieren
        updateVereinFilter();

        applyFilters();
    }

    private void updateVereinFilter() {
        List<String> vereinsNamen = allZertifikate.stream()
                .filter(zert -> zert.getVerein() != null)
                .map(zert -> zert.getVerein().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> items = new ArrayList<>();
        items.add("Alle");
        items.addAll(vereinsNamen);

        vereinFilter.setItems(items);
        vereinFilter.setValue("Alle");
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
