package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
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

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Unsignierte EintrÃ¤ge - PKI-Signierung"));

        // Grid
        grid.addColumn(eintrag -> eintrag.getSchuetze().getVollstaendigerName()).setHeader("SchÃ¼tze");
        grid.addColumn(SchiessnachweisEintrag::getDatum).setHeader("Datum").setSortable(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName()).setHeader("Disziplin");
        grid.addColumn(SchiessnachweisEintrag::getKaliber).setHeader("Kaliber");
        grid.addColumn(SchiessnachweisEintrag::getWaffenart).setHeader("Waffenart");
        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName()).setHeader("SchieÃŸstand");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse).setHeader("SchÃ¼sse");
        grid.addColumn(SchiessnachweisEintrag::getBemerkung).setHeader("Bemerkung");

        grid.addComponentColumn(this::createActionButtons).setHeader("Aktionen");

        add(grid);
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

        return new HorizontalLayout(signierenButton, ablehnenButton);
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

        log.info("Grid aktualisiert: {} unsignierte EintrÃ¤ge gefunden fÃ¼r {} Vereine",
                eintraege.size(), vereineAlsAufseher.size());
    }
}