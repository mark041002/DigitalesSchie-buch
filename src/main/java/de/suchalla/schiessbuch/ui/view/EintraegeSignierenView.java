package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.SignaturService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * View zum Signieren von SchieÃŸnachweis-EintrÃ¤gen (fÃ¼r Aufseher) mit PKI-Zertifikaten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "eintraege-signieren", layout = MainLayout.class)
@PageTitle("EintrÃ¤ge signieren | Digitales SchieÃŸbuch")
@RolesAllowed({"AUFSEHER", "ADMIN"})
@Slf4j
public class EintraegeSignierenView extends VerticalLayout {

    private final SecurityService securityService;
    private final SchiessnachweisService schiessnachweisService;
    private final SignaturService signaturService;
    private final VereinsmitgliedschaftService mitgliedschaftService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);

    private Benutzer currentUser;
    private Div emptyStateMessage;

    public EintraegeSignierenView(SecurityService securityService,
                                  SchiessnachweisService schiessnachweisService,
                                  SignaturService signaturService,
                                  VereinsmitgliedschaftService mitgliedschaftService) {
        this.securityService = securityService;
        this.schiessnachweisService = schiessnachweisService;
        this.signaturService = signaturService;
        this.mitgliedschaftService = mitgliedschaftService;

        this.currentUser = securityService.getAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("Benutzer nicht authentifiziert"));

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
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        // Text-Container
        Div textContainer = new Div();

        H2 title = new H2("Einträge signieren");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("PKI-Signierung unsignierter Schießnachweis-Einträge");
        subtitle.addClassName("subtitle");

        textContainer.add(title, subtitle);
        header.add(textContainer);
        contentWrapper.add(header);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid mit modernem Styling
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");

        grid.addColumn(eintrag -> eintrag.getSchuetze().getVollstaendigerName())
                .setHeader("Schütze")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getDatum)
                .setHeader("Datum")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName())
                .setHeader("Disziplin")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart")
                .setAutoWidth(true);
        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName())
                .setHeader("Schießstand")
                .setAutoWidth(true);
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(SchiessnachweisEintrag::getBemerkung)
                .setHeader("Bemerkung")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("250px")
                .setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        // Empty State Message erstellen
        emptyStateMessage = new Div();
        emptyStateMessage.setText("Keine unsignierten Einträge vorhanden. Alle Einträge wurden bereits signiert oder es gibt aktuell keine neuen Einträge.");
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
     * Erstellt Aktions-Buttons fÃ¼r Grid-Zeilen.
     *
     * @param eintrag Der Eintrag
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        Button signierenButton = new Button("PKI-Signieren", e -> signiereEintrag(eintrag));
        signierenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(eintrag));
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout layout = new HorizontalLayout(signierenButton, ablehnenButton);
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
    }

    /**
     * Signiert einen Eintrag mit PKI-Zertifikat.
     *
     * @param eintrag Der zu signierende Eintrag
     */
    private void signiereEintrag(SchiessnachweisEintrag eintrag) {
        try {
            // Eintrag mit allen Relationen inkl. Verein neu aus DB laden
            // Verwendet JOIN FETCH um LazyInitializationException zu vermeiden
            SchiessnachweisEintrag vollstaendigerEintrag = schiessnachweisService.findeEintragMitVerein(eintrag.getId())
                    .orElseThrow(() -> new RuntimeException("Eintrag nicht gefunden"));

            // Verein des SchieÃŸstands ermitteln - jetzt ohne LazyInitializationException
            Verein verein = vollstaendigerEintrag.getSchiesstand().getVerein();

            // Mit PKI-Zertifikat signieren
            signaturService.signEintrag(vollstaendigerEintrag, currentUser, verein);

            Notification.show("Eintrag erfolgreich mit PKI-Zertifikat signiert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            updateGrid();

        } catch (Exception e) {
            Notification.show("Fehler beim Signieren: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Zeigt einen Dialog zum Ablehnen eines Eintrags.
     *
     * @param eintrag Der abzulehnende Eintrag
     */
    private void zeigeAblehnungsDialog(SchiessnachweisEintrag eintrag) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eintrag ablehnen");

        TextArea grundField = new TextArea("Ablehnungsgrund");
        grundField.setWidthFull();
        grundField.setRequired(true);

        Button ablehnenButton = new Button("Ablehnen", e -> {
            if (grundField.isEmpty()) {
                Notification.show("Bitte geben Sie einen Grund an")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                schiessnachweisService.lehneEintragAb(eintrag.getId(), currentUser, grundField.getValue());
                Notification.show("Eintrag abgelehnt").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateGrid();
            } catch (Exception ex) {
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        dialog.add(grundField);
        dialog.getFooter().add(abbrechenButton, ablehnenButton);

        dialog.open();
    }

    /**
     * Aktualisiert das Grid mit unsignierten EintrÃ¤gen.
     */
    private void updateGrid() {
        // Lade alle Vereine, in denen der Benutzer Aufseher ist
        List<Verein> vereineAlsAufseher = mitgliedschaftService.findeMitgliedschaften(currentUser).stream()
                .filter(m -> m.getStatus() == de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus.AKTIV
                        && Boolean.TRUE.equals(m.getIstAufseher()))
                .map(de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft::getVerein)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        // Lade unsignierte EintrÃ¤ge fÃ¼r diese Vereine
        List<SchiessnachweisEintrag> eintraege = schiessnachweisService.getUnsignierteEintraegeForVereine(vereineAlsAufseher);

        grid.setItems(eintraege);

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = eintraege.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);

        log.info("Grid aktualisiert: {} unsignierte EintrÃ¤ge gefunden fÃ¼r {} Vereine",
                eintraege.size(), vereineAlsAufseher.size());
    }
}