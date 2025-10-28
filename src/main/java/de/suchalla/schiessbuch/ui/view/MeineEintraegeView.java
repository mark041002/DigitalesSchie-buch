package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.PdfExportService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * View für die Anzeige der eigenen Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "meine-eintraege", layout = MainLayout.class)
@PageTitle("Meine Einträge | Digitales Schießbuch")
@PermitAll
public class MeineEintraegeView extends VerticalLayout {

    private final SchiessnachweisService schiessnachweisService;
    private final PdfExportService pdfExportService;

    private final Grid<SchiessnachweisEintrag> grid = new Grid<>(SchiessnachweisEintrag.class, false);
    private final DatePicker vonDatum = new DatePicker("Von");
    private final DatePicker bisDatum = new DatePicker("Bis");

    private final Benutzer currentUser;

    public MeineEintraegeView(SecurityService securityService,
                              SchiessnachweisService schiessnachweisService,
                              PdfExportService pdfExportService) {
        this.schiessnachweisService = schiessnachweisService;
        this.pdfExportService = pdfExportService;
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
        add(new H2("Meine Schießnachweis-Einträge"));

        // Filter
        vonDatum.setValue(LocalDate.now().minusMonths(3));
        bisDatum.setValue(LocalDate.now());

        Button filterButton = new Button("Filtern", e -> updateGrid());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // PDF-Download als Anchor
        Anchor pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren");
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        FormLayout filterLayout = new FormLayout(vonDatum, bisDatum);
        HorizontalLayout buttonLayout = new HorizontalLayout(filterButton, pdfDownload);

        add(filterLayout, buttonLayout);

        // Grid
        grid.addColumn(SchiessnachweisEintrag::getDatum).setHeader("Datum").setSortable(true);
        grid.addColumn(eintrag -> eintrag.getDisziplin().getName()).setHeader("Disziplin");
        grid.addColumn(SchiessnachweisEintrag::getKaliber).setHeader("Kaliber");
        grid.addColumn(SchiessnachweisEintrag::getWaffenart).setHeader("Waffenart");
        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setClassNameGenerator(item -> "align-right");
        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName()).setHeader("Schießstand");
        grid.addColumn(eintrag -> getStatusText(eintrag.getStatus())).setHeader("Status");
        grid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                eintrag.getAufseher().getVollstaendigerName() : "-").setHeader("Aufseher");

        grid.addComponentColumn(this::createActionButtons).setHeader("Aktionen");

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                "style.textContent = '.align-right { text-align: right; }';" +
                "document.head.appendChild(style);"
        );

        add(grid);
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param eintrag Der Eintrag
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        HorizontalLayout layout = new HorizontalLayout();

        if (eintrag.kannGeloeschtWerden()) {
            Button deleteButton = new Button("Löschen", e -> deleteEintrag(eintrag));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            layout.add(deleteButton);
        }

        return layout;
    }

    /**
     * Löscht einen Eintrag.
     *
     * @param eintrag Der zu löschende Eintrag
     */
    private void deleteEintrag(SchiessnachweisEintrag eintrag) {
        try {
            schiessnachweisService.loescheEintrag(eintrag.getId());
            Notification.show("Eintrag erfolgreich gelöscht").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler beim Löschen: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aktualisiert das Grid mit Einträgen.
     */
    private void updateGrid() {
        if (currentUser != null) {
            LocalDate von = vonDatum.getValue();
            LocalDate bis = bisDatum.getValue();

            List<SchiessnachweisEintrag> eintraege = schiessnachweisService
                    .findeEintraegeImZeitraum(currentUser, von, bis);
            grid.setItems(eintraege);
        }
    }

    /**
     * Erstellt eine StreamResource für den PDF-Export.
     *
     * @return StreamResource
     */
    private StreamResource createPdfResource() {
        return new StreamResource("schiessnachweise.pdf", () -> {
            try {
                LocalDate von = vonDatum.getValue();
                LocalDate bis = bisDatum.getValue();

                List<SchiessnachweisEintrag> eintraege = schiessnachweisService
                        .findeSignierteEintraegeImZeitraum(currentUser, von, bis);

                byte[] pdfBytes = pdfExportService.exportiereSchiessnachweise(currentUser, eintraege, von, bis);
                return new ByteArrayInputStream(pdfBytes);
            } catch (Exception e) {
                Notification.show("Fehler beim PDF-Export: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return new ByteArrayInputStream(new byte[0]);
            }
        });
    }

    /**
     * Gibt den deutschen Text für einen Status zurück.
     *
     * @param status Der Status
     * @return Deutscher Statustext
     */
    private String getStatusText(de.suchalla.schiessbuch.model.enums.EintragStatus status) {
        return switch (status) {
            case OFFEN -> "Offen";
            case UNSIGNIERT -> "Unsigniert";
            case SIGNIERT -> "Signiert";
            case ABGELEHNT -> "Abgelehnt";
        };
    }
}
