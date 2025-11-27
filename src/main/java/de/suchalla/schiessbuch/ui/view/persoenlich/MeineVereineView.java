package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View für Vereinsmitgliedschaften.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "meine-vereine", layout = MainLayout.class)
@PageTitle("Meine Vereine | Digitales Schießbuch")
@PermitAll
public class MeineVereineView extends VerticalLayout {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VerbandService verbandService;

    // Formatter für Beitrittsdatum
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);

    private final Benutzer currentUser;
    private Div emptyStateMessage;

    public MeineVereineView(SecurityService securityService,
                            VereinsmitgliedschaftService mitgliedschaftService,
                            VerbandService verbandService) {
        this.mitgliedschaftService = mitgliedschaftService;
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
        contentWrapper.setSizeFull();

        // Header-Bereich wie bei "Meine Einträge"
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("gradient-header");
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.setAlignItems(Alignment.START);

        H2 title = new H2("Meine Vereine");
        title.getStyle().set("margin", "0");

        // Button für Vereinsbeitritt wie bei "Meine Einträge"
        Button beitretenButton = new Button("Einem Verein beitreten", new Icon(VaadinIcon.PLUS_CIRCLE));
        beitretenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        beitretenButton.addClassName("neuer-eintrag-btn");
        beitretenButton.getStyle().set("margin-left", "auto");
        beitretenButton.addClickListener(e -> zeigeVereinsbeitrittsDialog());

        header.add(title, beitretenButton);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("margin-bottom", "var(--lumo-space-l)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Sehen Sie alle Ihre Vereinsmitgliedschaften und verwalten Sie diese."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");
        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Grid-Container mit weißem Hintergrund (standardisiert)
        Div gridContainer = ViewComponentHelper.createGridContainer();

        // Grid mit modernem Styling
        grid.addClassName("rounded-grid");
        grid.setSizeFull();
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getVerein().getName())
                .setHeader("Verein")
                .setFlexGrow(1);

        grid.addColumn(mitgliedschaft -> {
            if (mitgliedschaft == null) return "";
            if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) return "Vereinschef";
            else if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) return "Aufseher";
            else return "Schütze";
        })
                .setHeader("Rolle")
                .setTextAlign(ColumnTextAlign.START);

        // Beitrittsdatum formatiert als dd.MM.yyyy
        grid.addColumn(mitgliedschaft -> {
            if (mitgliedschaft == null) return "Unbekannt";
            LocalDate d = mitgliedschaft.getBeitrittDatum();
            return d == null ? "Unbekannt" : dateFormatter.format(d);
        })
                .setHeader("Beitrittsdatum")
                .setTextAlign(ColumnTextAlign.START);

        // Status als farbige Pill (ComponentColumn)
        grid.addComponentColumn(mitgliedschaft -> createStatusPill(mitgliedschaft))
                .setHeader("Status")
                .setTextAlign(ColumnTextAlign.START);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen");

        grid.getColumns().forEach(c -> c.setAutoWidth(true));

        grid.addThemeVariants(
                com.vaadin.flow.component.grid.GridVariant.LUMO_ROW_STRIPES,
                com.vaadin.flow.component.grid.GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        // Empty State Message erstellen
        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage(
                "Sie sind noch keinem Verein beigetreten. Verwenden Sie den Button oben, um einem Verein beizutreten.",
                VaadinIcon.GROUP
        );
        emptyStateMessage.setVisible(false);

        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);
        add(contentWrapper);
    }

    /**
     * Erzeugt eine farbige Status-Pill für das Grid (grün/gelb/rot).
     */
    private Span createStatusPill(Vereinsmitgliedschaft m) {
        Span pill = new Span();
        pill.getStyle().set("display", "inline-block");
        pill.getStyle().set("padding", "0.15rem 0.6rem");
        pill.getStyle().set("border-radius", "12px");
        pill.getStyle().set("font-size", "0.85rem");

        if (m == null || m.getStatus() == null) {
            pill.setText("Unbekannt");
            pill.getStyle().set("background-color", "#9e9e9e");
            pill.getStyle().set("color", "white");
            return pill;
        }

        switch (m.getStatus()) {
            case AKTIV -> {
                pill.setText("Aktiv");
                pill.getStyle().set("background-color", "#4caf50");
                pill.getStyle().set("color", "white");
            }
            case BEANTRAGT -> {
                pill.setText("Beantragt");
                pill.getStyle().set("background-color", "#ffb300");
                pill.getStyle().set("color", "black");
            }
            case ABGELEHNT -> {
                pill.setText("Abgelehnt");
                pill.getStyle().set("background-color", "#d32f2f");
                pill.getStyle().set("color", "white");
            }
            case BEENDET -> {
                pill.setText("Beendet");
                pill.getStyle().set("background-color", "#d32f2f");
                pill.getStyle().set("color", "white");
            }
            case VERLASSEN -> {
                pill.setText("Verlassen");
                pill.getStyle().set("background-color", "#d32f2f");
                pill.getStyle().set("color", "white");
            }
            default -> {
                pill.setText(m.getStatus().name());
                pill.getStyle().set("background-color", "#9e9e9e");
                pill.getStyle().set("color", "white");
            }
        }

        return pill;
    }


    /**
     * Zeigt den Dialog für den Vereinsbeitritt mit Verbandsauswahl.
     */
    private void zeigeVereinsbeitrittsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Einem Verein beitreten");
        dialog.setWidth("500px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);
        layout.setWidthFull();

        // Info-Box über die gesamte Breite und mit Standard-Info-Box-Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        // Keine explizite Farbe setzen, Standard-Info-Box-Styling übernimmt das Blau

        Span infoText = new Span("Suchen Sie nach Namen oder Adresse (Teiltext möglich). Wählen Sie einen Verein aus der Liste aus — vollständiger Name und Adresse werden angezeigt. 'Beitreten' sendet eine Beitrittsanfrage.");
        infoText.getStyle().set("margin-left", "var(--lumo-space-s)");
        infoText.getStyle().set("color", "var(--lumo-primary-text-color)"); // Blauschrift

        HorizontalLayout infoLayout = new HorizontalLayout(infoIcon, infoText);
        infoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        infoLayout.setSpacing(false);
        infoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // linksbündig
        infoBox.add(infoLayout);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        // Einfache Suche: ein Feld für Name / Nummer / Adresse und eine Ergebnisliste (keine Tabelle)
        TextField suchFeld = new TextField("Verein suchen (Name, Adresse)");
        suchFeld.setWidthFull();
        suchFeld.setPlaceholder("Teil des Namens oder der Adresse eingeben");
        suchFeld.setClearButtonVisible(true);
        suchFeld.setValueChangeMode(ValueChangeMode.EAGER);

        VerticalLayout ergebnisContainer = new VerticalLayout();
        ergebnisContainer.setWidthFull();
        ergebnisContainer.setSpacing(false);
        ergebnisContainer.setPadding(false);

        final List<Verein> alleVereine = loadAlleVereine();

        // Hilfsmethode zum (rek)aufbauen der Ergebnisliste
        java.util.function.Consumer<List<Verein>> refreshResults = matches -> {
            ergebnisContainer.removeAll();
            if (matches == null || matches.isEmpty()) {
                Span none = new Span("Keine Vereine gefunden");
                none.getStyle().set("color", "var(--lumo-disabled-text-color)");
                ergebnisContainer.add(none);
                return;
            }

            for (Verein v : matches) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setAlignItems(FlexComponent.Alignment.CENTER);

                VerticalLayout info = new VerticalLayout();
                info.setPadding(false);
                info.setSpacing(false);
                info.setWidthFull();

                Span name = new Span(v.getName());
                name.getStyle().set("font-weight", "600");
                com.vaadin.flow.component.html.Paragraph addr = new com.vaadin.flow.component.html.Paragraph(v.getAdresse() == null ? "" : v.getAdresse());
                addr.getStyle().set("margin", "0");

                info.add(name, addr);

                Button join = new Button("Beitreten", e -> {
                    try {
                        mitgliedschaftService.vereinBeitreten(currentUser, v);
                        Notification.show("Beitrittsanfrage wurde gesendet").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGrid();
                        dialog.close();
                    } catch (Exception ex) {
                        Notification.show("Fehler: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                join.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                row.add(info, join);
                row.expand(info);
                ergebnisContainer.add(row);
            }
        };

        // Anfangs alle Vereine anzeigen
        refreshResults.accept(alleVereine);

        // Filter beim Tippen
        suchFeld.addValueChangeListener(e -> {
            String q = e.getValue() == null ? "" : e.getValue().trim().toLowerCase();
            if (q.isEmpty()) {
                refreshResults.accept(alleVereine);
                return;
            }
            List<Verein> matches = alleVereine.stream().filter(v -> {
                String name = v.getName() == null ? "" : v.getName().toLowerCase();
                String adr = v.getAdresse() == null ? "" : v.getAdresse().toLowerCase();
                return name.contains(q) || adr.contains(q);
            }).toList();
            refreshResults.accept(matches);
        });

        layout.add(infoBox, suchFeld, ergebnisContainer);
        dialog.add(layout);
        dialog.getFooter().add(abbrechenButton);
        dialog.open();
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     * @return Button
     */
    private Button createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        if (mitgliedschaft.getAktiv()) {
            Button verlassenButton = new Button("Verlassen", new Icon(VaadinIcon.SIGN_OUT));
            verlassenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            verlassenButton.addClickListener(e -> vereinVerlassen(mitgliedschaft));
            return verlassenButton;
        } else if (mitgliedschaft.getStatus() == MitgliedschaftsStatus.VERLASSEN
                || mitgliedschaft.getStatus() == MitgliedschaftsStatus.ABGELEHNT
                || mitgliedschaft.getStatus() == MitgliedschaftsStatus.BEENDET
                || mitgliedschaft.getStatus() == MitgliedschaftsStatus.BEANTRAGT) {
            Button loeschenButton = new Button("Löschen", new Icon(VaadinIcon.TRASH));
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.getElement().setAttribute("title", "Eintrag endgültig löschen");
            loeschenButton.addClickListener(e -> mitgliedschaftLoeschen(mitgliedschaft));
            return loeschenButton;
        }
        return new Button();
    }

    

    /**
     * Verlässt einen Verein.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     */
    private void vereinVerlassen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.vereinVerlassen(mitgliedschaft.getId());
            Notification.show("Verein verlassen").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Löscht eine Mitgliedschaft endgültig.
     *
     * @param mitgliedschaft Die Mitgliedschaft
     */
    private void mitgliedschaftLoeschen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.loescheMitgliedschaft(mitgliedschaft.getId());
            Notification.show("Mitgliedschaft gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Grid.
     */
    private void updateGrid() {
        if (currentUser != null) {
                List<Vereinsmitgliedschaft> mitgliedschaften =
                    mitgliedschaftService.findeAlleMitgliedschaftenVonBenutzerEntities(currentUser);
            grid.setItems(mitgliedschaften);

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = mitgliedschaften.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    private List<Verein> loadAlleVereine() {
        try {
            return verbandService.findeAlleVereineEntities();
        } catch (Exception ex) {
            Notification.show("Fehler beim Laden der Vereine: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return List.of();
        }
    }
}
