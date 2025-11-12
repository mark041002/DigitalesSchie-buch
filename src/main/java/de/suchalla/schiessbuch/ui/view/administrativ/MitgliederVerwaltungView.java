package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import de.suchalla.schiessbuch.service.VereinService;
import de.suchalla.schiessbuch.service.AktiveBenutzerService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * View für Mitgliederverwaltung eines Vereins (Admin-Ansicht).
 *
 * @author Markus Suchalla
 * @version 1.0.4
 */
@Route(value = "vereins/mitglieder", layout = MainLayout.class)
@PageTitle("Mitglieder | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "AUFSEHER", "SCHIESSSTAND_AUFSEHER", "ADMIN"})
@Slf4j
public class MitgliederVerwaltungView extends VerticalLayout implements HasUrlParameter<String> {

    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VereinService vereinService;
    private final AktiveBenutzerService aktiveBenutzerService;
    private final Grid<Vereinsmitgliedschaft> grid = new Grid<>(Vereinsmitgliedschaft.class, false);
    private Div emptyStateMessage;
    private Verein aktuellerVerein;

    public MitgliederVerwaltungView(VereinsmitgliedschaftService mitgliedschaftService, VereinService vereinService, AktiveBenutzerService aktiveBenutzerService) {
        this.mitgliedschaftService = mitgliedschaftService;
        this.vereinService = vereinService;
        this.aktiveBenutzerService = aktiveBenutzerService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        if (params.containsKey("vereinId")) {
            try {
                Long vereinId = Long.parseLong(params.get("vereinId").get(0));
                aktuellerVerein = vereinService.findById(vereinId);
            } catch (NumberFormatException e) {
                createErrorContent("Ungültige Vereins-ID");
                return;
            }
        }

        if (aktuellerVerein == null) {
            createErrorContent("Verein nicht gefunden.");
            return;
        }

        createContent();
        updateGrid();
    }

    private void createErrorContent(String errorMessage) {
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        Div errorBox = new Div();
        errorBox.addClassName("error-box");
        errorBox.setWidthFull();

        H2 errorTitle = new H2("Fehler");
        Paragraph errorText = new Paragraph(errorMessage);

        errorBox.add(errorTitle, errorText);
        contentWrapper.add(errorBox);
        add(contentWrapper);
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

        H2 title = new H2("Mitgliederverwaltung - " + aktuellerVerein.getName());
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(
                "Verwalten Sie die Mitglieder des Vereins. Genehmigen Sie Beitrittsanfragen, ernennen Sie Aufseher oder entfernen Sie Mitglieder."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Übersicht eingeloggte Mitglieder
        VerticalLayout eingeloggteMitgliederLayout = new VerticalLayout();
        eingeloggteMitgliederLayout.setSpacing(false);
        eingeloggteMitgliederLayout.setPadding(false);
        eingeloggteMitgliederLayout.addClassName("eingeloggte-mitglieder-wrapper");

        H2 eingeloggteTitle = new H2("Aktuell eingeloggte Mitglieder");
        eingeloggteTitle.getStyle().set("margin", "0");
        eingeloggteMitgliederLayout.add(eingeloggteTitle);

        Grid<Benutzer> eingeloggteGrid = new Grid<>(Benutzer.class, false);
        eingeloggteGrid.setWidthFull();
        eingeloggteGrid.addColumn(Benutzer::getId).setHeader("ID").setWidth("80px").setAutoWidth(true);
        eingeloggteGrid.addColumn(b -> b.getVorname() + " " + b.getNachname()).setHeader("Name").setAutoWidth(true);
        eingeloggteGrid.addColumn(Benutzer::getEmail).setHeader("E-Mail").setAutoWidth(true);
        eingeloggteGrid.addColumn(b -> b.getRolle() != null ? b.getRolle().name() : "-").setHeader("Rolle").setAutoWidth(true);
        eingeloggteGrid.setItems(aktiveBenutzerService.getEingeloggteBenutzer());

        eingeloggteMitgliederLayout.add(eingeloggteGrid);
        contentWrapper.add(eingeloggteMitgliederLayout);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("min-height", "0")
                .set("overflow-x", "auto")
                .set("overflow-y", "auto");

        // Empty State Message
        emptyStateMessage = new Div();
        emptyStateMessage.addClassName("empty-state");
        emptyStateMessage.setWidthFull();
        emptyStateMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)")
                .set("color", "var(--lumo-secondary-text-color)");

        Icon emptyIcon = VaadinIcon.USERS.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Noch keine Mitglieder vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid Setup
        setupGrid();

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private void setupGrid() {
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getId())
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getVollstaendigerName())
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(mitgliedschaft -> mitgliedschaft.getBenutzer().getEmail())
                .setHeader("E-Mail")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(this::getRolleText)
                .setHeader("Rolle")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true);

        grid.addColumn(mitgliedschaft ->
                        mitgliedschaft.getBeitrittDatum() != null ?
                                mitgliedschaft.getBeitrittDatum().toString() : "-")
                .setHeader("Beitrittsdatum")
                .setWidth("140px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true);

        grid.addColumn(this::getStatusText)
                .setHeader("Status")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true);

        // Aktionen-Spalte
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("280px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(false);

        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );
    }

    private HorizontalLayout createActionButtons(Vereinsmitgliedschaft mitgliedschaft) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "nowrap");

        if (mitgliedschaft.getStatus() == MitgliedschaftStatus.BEANTRAGT) {
            Button genehmigerButton = new Button("Genehmigen", VaadinIcon.CHECK.create());
            genehmigerButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            genehmigerButton.addClickListener(e -> genehmigen(mitgliedschaft));

            Button ablehnenButton = new Button("Ablehnen", VaadinIcon.CLOSE.create());
            ablehnenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            ablehnenButton.addClickListener(e -> zeigeAblehnungsDialog(mitgliedschaft));

            layout.add(genehmigerButton, ablehnenButton);
        } else if (mitgliedschaft.getStatus() == MitgliedschaftStatus.AKTIV) {
            Button aufseherButton = new Button(
                    Boolean.TRUE.equals(mitgliedschaft.getIstAufseher()) ? "Aufseher entfernen" : "Zu Aufseher ernennen");
            aufseherButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            aufseherButton.addClickListener(e ->
                    setzeAufseherStatus(mitgliedschaft, !Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())));

            Button entfernenButton = new Button("Entfernen", VaadinIcon.TRASH.create());
            entfernenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
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

        dialogLayout.add(grundField);
        dialog.add(dialogLayout);
        dialog.getFooter().add(abbrechenButton, ablehnenButton);
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
            grid.getDataProvider().refreshAll();

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = mitglieder.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
        }
    }

    private String getRolleText(Vereinsmitgliedschaft mitgliedschaft) {
        if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef())) {
            return "Vereinschef ernennen";
        } else if (Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
            return "Aufseher ernennen";
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

