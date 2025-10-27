package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * View für die Verwaltung und Anzeige von PKI-Zertifikaten.
 * - Aufseher: Nur eigenes Zertifikat
 * - Vereinschefs: Vereinszertifikat + eigenes Zertifikat
 * - Admins: Alle Zertifikate
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
    private final BenutzerService benutzerService;
    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<DigitalesZertifikat> grid;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public ZertifikateView(
            DigitalesZertifikatRepository zertifikatRepository,
            SecurityService securityService,
            BenutzerService benutzerService,
            VereinsmitgliedschaftService mitgliedschaftService) {

        this.zertifikatRepository = zertifikatRepository;
        this.securityService = securityService;
        this.benutzerService = benutzerService;
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

        // Grid für Zertifikate
        grid = new Grid<>(DigitalesZertifikat.class, false);
        grid.setSizeFull();
        configureGrid();

        add(grid);

        // Daten laden
        loadZertifikate();
    }

    private void configureGrid() {
        grid.addColumn(zert -> {
            switch (zert.getZertifikatsTyp()) {
                case "ROOT": return "Root CA";
                case "VEREIN": return "Verein";
                case "AUFSEHER": return "Aufseher";
                default: return zert.getZertifikatsTyp();
            }
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

        grid.addColumn(zert -> zert.getGueltigBis().format(dateFormatter))
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

        Button downloadButton = new Button("PEM", VaadinIcon.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        downloadButton.addClickListener(e -> downloadPEM(zertifikat));

        return new HorizontalLayout(detailsButton, downloadButton);
    }

    private void loadZertifikate() {
        Benutzer currentUser = securityService.getAuthenticatedUser()
                .orElse(null);
        if (currentUser == null) {
            return;
        }

        List<DigitalesZertifikat> zertifikate = new ArrayList<>();

        if (currentUser.getRolle() == BenutzerRolle.ADMIN) {
            // Admins sehen alle Zertifikate - mit EAGER loading
            zertifikate = zertifikatRepository.findAllWithDetails();
        } else {
            // Eigenes Zertifikat laden (Aufseher) - mit EAGER loading
            zertifikatRepository.findByBenutzerWithDetails(currentUser).ifPresent(zertifikate::add);

            // Wenn Vereinschef, auch Vereinszertifikate laden - mit EAGER loading
            List<Verein> vereine = mitgliedschaftService.getVereineWhereUserIsChef(currentUser);
            for (Verein verein : vereine) {
                zertifikate.addAll(zertifikatRepository.findByVereinWithDetails(verein));
            }
        }

        grid.setItems(zertifikate);
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
        detailsLayout.add(createDetailField("Gültig bis", zertifikat.getGueltigBis().format(dateFormatter)));
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

    private void downloadPEM(DigitalesZertifikat zertifikat) {
        try {
            // PEM-Datei zum Download anbieten
            String filename = String.format("zertifikat_%s_%s.pem",
                zertifikat.getZertifikatsTyp().toLowerCase(),
                zertifikat.getSeriennummer());

            // Hinweis: Für echten Download müsste hier StreamResource verwendet werden
            // Vorerst kopieren wir den Inhalt in die Zwischenablage

            Notification notification = Notification.show(
                "Zertifikat-PEM wurde angezeigt. Zum Download bitte aus dem Details-Dialog kopieren.",
                5000,
                Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

            showDetails(zertifikat);

        } catch (Exception e) {
            log.error("Fehler beim Download des Zertifikats", e);
            Notification.show("Fehler beim Download: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
