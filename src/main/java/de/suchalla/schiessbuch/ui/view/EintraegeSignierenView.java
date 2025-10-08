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
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import jakarta.annotation.security.RolesAllowed;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * View zum Signieren von Schießnachweis-Einträgen (für Aufseher).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "eintraege-signieren", layout = MainLayout.class)
@PageTitle("Einträge signieren | Digitales Schießbuch")
@RolesAllowed({"AUFSEHER", "ADMIN"})
public class EintraegeSignierenView extends VerticalLayout {

    private final SecurityService securityService;
    private final SchiessnachweisService schiessnachweisService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);

    private Benutzer currentUser;

    public EintraegeSignierenView(SecurityService securityService,
                                   SchiessnachweisService schiessnachweisService) {
        this.securityService = securityService;
        this.schiessnachweisService = schiessnachweisService;

        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(true);
        setPadding(true);

        createContent();
        updateGrid();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Unsignierte Einträge"));

        // Grid
        grid.addColumn(eintrag -> eintrag.getSchuetze().getVollstaendigerName()).setHeader("Schütze");
        grid.addColumn(SchiessnachweisEintrag::getDatum).setHeader("Datum").setSortable(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName()).setHeader("Disziplin");
        grid.addColumn(SchiessnachweisEintrag::getKaliber).setHeader("Kaliber");
        grid.addColumn(SchiessnachweisEintrag::getWaffenart).setHeader("Waffenart");
        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName()).setHeader("Schießstand");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse).setHeader("Schüsse");
        grid.addColumn(SchiessnachweisEintrag::getBemerkung).setHeader("Bemerkung");

        grid.addComponentColumn(this::createActionButtons).setHeader("Aktionen");

        add(grid);
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param eintrag Der Eintrag
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        Button signierenButton = new Button("Signieren", e -> signiereEintrag(eintrag));
        signierenButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        Button ablehnenButton = new Button("Ablehnen", e -> zeigeAblehnungsDialog(eintrag));
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        return new HorizontalLayout(signierenButton, ablehnenButton);
    }

    /**
     * Signiert einen Eintrag.
     *
     * @param eintrag Der zu signierende Eintrag
     */
    private void signiereEintrag(SchiessnachweisEintrag eintrag) {
        try {
            String signatur = generiereSignatur(eintrag);
            schiessnachweisService.signiereEintrag(eintrag.getId(), currentUser, signatur);

            Notification.show("Eintrag erfolgreich signiert")
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
     * Generiert eine digitale Signatur für einen Eintrag.
     *
     * @param eintrag Der Eintrag
     * @return Digitale Signatur als Hex-String
     */
    private String generiereSignatur(SchiessnachweisEintrag eintrag) {
        try {
            String daten = eintrag.getId() + "|" +
                    eintrag.getSchuetze().getId() + "|" +
                    eintrag.getDatum() + "|" +
                    currentUser.getId() + "|" +
                    LocalDateTime.now();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(daten.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            return "SIGNATUR_FEHLER_" + System.currentTimeMillis();
        }
    }

    /**
     * Aktualisiert das Grid mit unsignierten Einträgen.
     */
    private void updateGrid() {
        // Hier müsste man die Schießstände des Aufsehers laden
        // Vereinfacht: Alle unsignierten Einträge
        List<SchiessnachweisEintrag> eintraege = new ArrayList<>();
        // TODO: Filtern nach Schießständen, für die der Aufseher zuständig ist
        grid.setItems(eintraege);
    }
}
