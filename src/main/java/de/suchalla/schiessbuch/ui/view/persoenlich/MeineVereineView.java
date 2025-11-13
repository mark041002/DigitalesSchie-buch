package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

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

    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);

    private final Benutzer currentUser;
    private Div emptyStateMessage;

    public MeineVereineView(SecurityService securityService,
                            VereinsmitgliedschaftService mitgliedschaftService,
                            VerbandService verbandService) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.verbandService = verbandService;

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

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid mit modernem Styling
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getVerein().getName())
                .setHeader("Verein")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getVerein().getVerbaende() != null && !mitgliedschaft.getVerein().getVerbaende().isEmpty()
                ? mitgliedschaft.getVerein().getVerbaende().stream()
                    .map(Verband::getName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("")
                : "")
                .setHeader("Verbände")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createRolleBadge)
                .setHeader("Rolle")
                .setAutoWidth(true);

        grid.addColumn(Vereinsmitgliedschaft::getBeitrittDatum)
                .setHeader("Beitrittsdatum")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
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
        emptyStateMessage.setText("Sie sind noch keinem Verein beigetreten. Verwenden Sie den Button oben, um einem Verein beizutreten.");
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
    }

    /**
     * Erstellt ein Badge für die Rolle.
     */
    private Span createRolleBadge(Vereinsmitgliedschaft mitgliedschaft) {
        Span badge = new Span();
        String text;

        if (mitgliedschaft.getIstVereinschef()) {
            text = "Vereinschef";
        } else if (mitgliedschaft.getIstAufseher()) {
            text = "Aufseher";
        } else {
            text = "Schütze";
        }

        badge.setText(text);
        badge.addClassName("badge");
        badge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center");

        return badge;
    }

    /**
     * Erstellt ein Badge für den Status.
     */
    private Span createStatusBadge(Vereinsmitgliedschaft mitgliedschaft) {
        Span badge = new Span();
        String text;
        String cssClass;

        switch (mitgliedschaft.getStatus()) {
            case AKTIV -> {
                text = "Aktiv";
                cssClass = "badge-success";
            }
            case BEANTRAGT -> {
                text = "Beantragt";
                cssClass = "badge-warning";
            }
            case ABGELEHNT -> {
                text = "Abgelehnt";
                cssClass = "badge-error";
            }
            case BEENDET -> {
                text = "Beendet";
                cssClass = "badge-error";
            }
            case VERLASSEN -> {
                text = "Verlassen";
                cssClass = "badge-error";
            }
            default -> {
                text = "Unbekannt";
                cssClass = "badge";
            }
        }

        badge.setText(text);
        badge.addClassName("badge");
        badge.addClassName(cssClass);
        badge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center");

        return badge;
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

        Span infoText = new Span("Geben Sie die Vereinsnummer ein, um eine Beitrittsanfrage zu senden.");
        infoText.getStyle().set("margin-left", "var(--lumo-space-s)");
        infoText.getStyle().set("color", "var(--lumo-primary-text-color)"); // Blauschrift

        HorizontalLayout infoLayout = new HorizontalLayout(infoIcon, infoText);
        infoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        infoLayout.setSpacing(false);
        infoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START); // linksbündig
        infoBox.add(infoLayout);

        TextField vereinsNummerField = new TextField("Vereinsnummer");
        vereinsNummerField.setWidthFull();
        vereinsNummerField.setPlaceholder("z.B. DSB-12345");
        vereinsNummerField.setClearButtonVisible(true);

        Button beitretenButton = new Button("Beitreten", new Icon(VaadinIcon.SIGN_IN), e -> {
             if (vereinsNummerField.getValue() == null || vereinsNummerField.getValue().isEmpty()) {
                 Notification.show("Bitte geben Sie eine Vereinsnummer ein")
                         .addThemeVariants(NotificationVariant.LUMO_ERROR);
                 return;
             }

             vereinBeitreten(vereinsNummerField.getValue());
             dialog.close();
         });
         beitretenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

         Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

         layout.add(infoBox, vereinsNummerField);
         dialog.add(layout);
         dialog.getFooter().add(abbrechenButton, beitretenButton);
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
        } else if (mitgliedschaft.getStatus() == de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus.VERLASSEN
                || mitgliedschaft.getStatus() == de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus.ABGELEHNT
                || mitgliedschaft.getStatus() == de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus.BEENDET
                || mitgliedschaft.getStatus() == de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus.BEANTRAGT) {
            Button loeschenButton = new Button("Löschen", new Icon(VaadinIcon.TRASH));
            loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            loeschenButton.getElement().setAttribute("title", "Eintrag endgültig löschen");
            loeschenButton.addClickListener(e -> mitgliedschaftLoeschen(mitgliedschaft));
            return loeschenButton;
        }
        return new Button();
    }

    /**
     * Tritt einem Verein bei.
     */
    private void vereinBeitreten(String vereinsNummer) {
        try {
            if (vereinsNummer == null || vereinsNummer.isBlank()) {
                Notification.show("Ungültige Vereinsnummer").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Verein verein = verbandService.findeVereinByVereinsNummer(vereinsNummer)
                    .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

            // Verein gefunden, Verband wird automatisch aus dem Verein bestimmt
            mitgliedschaftService.vereinBeitreten(currentUser, verein);

            Notification.show("Beitrittsanfrage wurde gesendet")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
                    mitgliedschaftService.findeMitgliedschaftenVonBenutzer(currentUser);
            grid.setItems(mitgliedschaften);

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = mitgliedschaften.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }
}
