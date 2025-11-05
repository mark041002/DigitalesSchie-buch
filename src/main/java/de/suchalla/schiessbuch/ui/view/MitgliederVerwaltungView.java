package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * View für Mitgliederverwaltung eines Vereins (Admin-Ansicht).
 *
 * @author Markus Suchalla
 * @version 1.0.3
 */
@Route(value = "vereins/mitglieder", layout = MainLayout.class)
@PageTitle("Mitglieder | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "AUFSEHER", "ADMIN"})
@Slf4j
public class MitgliederVerwaltungView extends VerticalLayout implements HasUrlParameter<String> {

    private final VerbandService verbandService;
    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VereinRepository vereinRepository;
    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);
    private Long vereinId;
    private Verein aktuellerVerein;

    public MitgliederVerwaltungView(VerbandService verbandService,
                                    VereinsmitgliedschaftService mitgliedschaftService,
                                    VereinRepository vereinRepository) {
        this.verbandService = verbandService;
        this.mitgliedschaftService = mitgliedschaftService;
        this.vereinRepository = vereinRepository;
        setSpacing(true);
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        if (params.containsKey("vereinId")) {
            try {
                vereinId = Long.parseLong(params.get("vereinId").get(0));
                aktuellerVerein = vereinRepository.findById(vereinId).orElse(null);
            } catch (NumberFormatException e) {
                add(new H2("Fehler"));
                add(new Paragraph("Ungültige Vereins-ID"));
                return;
            }
        }

        if (aktuellerVerein == null) {
            add(new H2("Fehler"));
            add(new Paragraph("Verein nicht gefunden."));
            return;
        }

        add(new H2("Mitgliederverwaltung - " + aktuellerVerein.getName()));
        setupGrid();
        add(grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getId())
                .setHeader("ID")
                .setWidth("80px")
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getVollstaendigerName())
                .setHeader("Name");

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getEmail())
                .setHeader("E-Mail");

        grid.addColumn(this::getRolleText)
                .setHeader("Rolle");

        grid.addColumn(mitgliedschaft ->
                        mitgliedschaft.getBeitrittDatum() != null ?
                                mitgliedschaft.getBeitrittDatum().toString() : "-")
                .setHeader("Beitrittsdatum")
                .setWidth("140px");

        grid.addColumn(this::getStatusText)
                .setHeader("Status")
                .setWidth("120px");

        // Aktionen-Spalte
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("280px")
                .setFlexGrow(0);

        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );
    }

    private HorizontalLayout createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        if (mitgliedschaft.getStatus() == MitgliedschaftStatus.BEANTRAGT) {
            Button genehmigerButton = new Button("Genehmigen");
            genehmigerButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            genehmigerButton.addClickListener(e -> genehmigen(mitgliedschaft));

            Button ablehnenButton = new Button("Ablehnen");
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            ablehnenButton.addClickListener(e -> zeigeAblehnungsDialog(mitgliedschaft));

            layout.add(genehmigerButton, ablehnenButton);
        } else if (mitgliedschaft.getStatus() == MitgliedschaftStatus.AKTIV) {
            Button aufseherButton = new Button(
                    Boolean.TRUE.equals(mitgliedschaft.getIstAufseher()) ? "Aufseher entfernen" : "Zu Aufseher ernennen");
            aufseherButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            aufseherButton.addClickListener(e ->
                    setzeAufseherStatus(mitgliedschaft, !Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())));

            Button entfernenButton = new Button("Entfernen");
            entfernenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            entfernenButton.addClickListener(e -> mitgliedEntfernen(mitgliedschaft));

            layout.add(aufseherButton, entfernenButton);
        }

        return layout;
    }

    private void genehmigen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.genehmigeAnfrage(mitgliedschaft.getId());
            Notification.show("Beitrittsanfrage genehmigt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Genehmigen", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeAblehnungsDialog(Vereinsmitgliedschaft mitgliedschaft) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Beitrittsanfrage ablehnen");

        VerticalLayout dialogLayout = new VerticalLayout();
        TextArea grundField = new TextArea("Ablehnungsgrund");
        grundField.setWidthFull();
        grundField.setHeight("150px");

        Button ablehnenButton = new Button("Ablehnen", e -> {
            String grund = grundField.getValue();
            try {
                if (grund != null && !grund.trim().isEmpty()) {
                    mitgliedschaftService.lehneAnfrageAbMitGrund(mitgliedschaft.getId(), grund);
                } else {
                    mitgliedschaftService.lehneAnfrageAb(mitgliedschaft.getId());
                }
                Notification.show("Beitrittsanfrage abgelehnt")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                updateGrid();
            } catch (Exception ex) {
                log.error("Fehler beim Ablehnen", ex);
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        ablehnenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(ablehnenButton, abbrechenButton);
        dialogLayout.add(grundField, buttons);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void setzeAufseherStatus(Vereinsmitgliedschaft mitgliedschaft, boolean istAufseher) {
        try {
            mitgliedschaftService.setzeAufseherStatus(mitgliedschaft.getId(), istAufseher);
            String nachricht = istAufseher ? "Mitglied zu Aufseher ernannt" : "Aufseher-Status entzogen";
            Notification.show(nachricht).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Ändern des Aufseher-Status", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void mitgliedEntfernen(Vereinsmitgliedschaft mitgliedschaft) {
        try {
            mitgliedschaftService.mitgliedEntfernen(mitgliedschaft.getId());
            Notification.show("Mitglied aus Verein entfernt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            log.error("Fehler beim Entfernen", e);
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        if (aktuellerVerein != null) {
            List<Vereinsmitgliedschaft> mitglieder = mitgliedschaftService.findeAlleMitgliedschaften(aktuellerVerein);
            grid.setItems(mitglieder);
        }
    }

    private String getRolleText(Vereinsmitgliedschaft mitgliedschaft) {
        if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
            return "Vereinschef";
        } else if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
            return "Aufseher";
        }
        return "Mitglied";
    }

    private String getStatusText(Vereinsmitgliedschaft mitgliedschaft) {
        return switch (mitgliedschaft.getStatus()) {
            case AKTIV -> "Aktiv";
            case BEANTRAGT -> "Beantragt";
            case ABGELEHNT -> "Abgelehnt";
            case BEENDET -> "Beendet";
            default -> "Unbekannt";
        };
    }
}