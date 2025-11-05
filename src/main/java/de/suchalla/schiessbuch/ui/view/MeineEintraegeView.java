package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
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
    private Div emptyStateMessage;

    private final Benutzer currentUser;

    public MeineEintraegeView(SecurityService securityService,
                              SchiessnachweisService schiessnachweisService,
                              PdfExportService pdfExportService) {
        this.schiessnachweisService = schiessnachweisService;
        this.pdfExportService = pdfExportService;
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

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        // Text-Container
        Div textContainer = new Div();

        H2 title = new H2("Meine Schießnachweis-Einträge");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("Übersicht Ihrer dokumentierten Schießaktivitäten");
        subtitle.addClassName("subtitle");

        textContainer.add(title, subtitle);

        // Button für neuen Eintrag
        Button neuerEintragButton = new Button("Neuer Eintrag", new Icon(VaadinIcon.PLUS_CIRCLE));
        neuerEintragButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        neuerEintragButton.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate(NeuerEintragView.class))
        );

        header.add(textContainer, neuerEintragButton);
        contentWrapper.add(header);

        // Filter-Bereich mit modernem Styling - alles nebeneinander
        Div filterBox = new Div();
        filterBox.addClassName("filter-box");

        vonDatum.setValue(LocalDate.now().minusMonths(3));
        vonDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        vonDatum.setWidth("200px");

        bisDatum.setValue(LocalDate.now());
        bisDatum.setPrefixComponent(VaadinIcon.CALENDAR.create());
        bisDatum.setWidth("200px");

        Button filterButton = new Button("Filtern", new Icon(VaadinIcon.FILTER));
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addClickListener(e -> updateGrid());

        // PDF-Download als Anchor
        Anchor pdfDownload = new Anchor(createPdfResource(), "");
        pdfDownload.getElement().setAttribute("download", true);
        Button pdfButton = new Button("PDF exportieren", new Icon(VaadinIcon.DOWNLOAD));
        pdfButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        pdfDownload.add(pdfButton);

        // Alles in einem HorizontalLayout nebeneinander
        HorizontalLayout filterLayout = new HorizontalLayout(vonDatum, bisDatum, filterButton, pdfDownload);
        filterLayout.setAlignItems(FlexComponent.Alignment.END);
        filterLayout.setSpacing(true);
        filterLayout.setWidthFull();
        filterLayout.getStyle().set("flex-wrap", "wrap");

        filterBox.add(filterLayout);
        contentWrapper.add(filterBox);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();

        // Grid mit modernem Styling
        grid.setHeight("600px");
        grid.addClassName("rounded-grid");
        grid.addColumn(SchiessnachweisEintrag::getDatum)
                .setHeader("Datum")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(eintrag -> eintrag.getDisziplin().getName())
                .setHeader("Disziplin")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(SchiessnachweisEintrag::getKaliber)
                .setHeader("Kaliber")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintrag::getWaffenart)
                .setHeader("Waffenart")
                .setAutoWidth(true);

        grid.addColumn(SchiessnachweisEintrag::getAnzahlSchuesse)
                .setHeader("Schüsse")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(SchiessnachweisEintrag::getErgebnis)
                .setHeader("Ergebnis")
                .setAutoWidth(true)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(eintrag -> eintrag.getSchiesstand().getName())
                .setHeader("Schießstand")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(eintrag -> eintrag.getAufseher() != null ?
                eintrag.getAufseher().getVollstaendigerName() : "-")
                .setHeader("Aufseher")
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
        emptyStateMessage.setText("Keine Einträge im ausgewählten Zeitraum gefunden. Erstellen Sie einen neuen Eintrag über den Button oben.");
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
     * Erstellt ein Badge für den Status.
     */
    private Span createStatusBadge(SchiessnachweisEintrag eintrag) {
        Span badge = new Span();
        Icon icon;
        String text = getStatusText(eintrag.getStatus());
        String theme;

        switch (eintrag.getStatus()) {
            case OFFEN, UNSIGNIERT -> {
                icon = VaadinIcon.EDIT.create();
                theme = "badge contrast";
            }
            case SIGNIERT -> {
                icon = VaadinIcon.CHECK_CIRCLE.create();
                theme = "badge success";
            }
            case ABGELEHNT -> {
                icon = VaadinIcon.CLOSE_CIRCLE.create();
                theme = "badge error";
            }
            default -> {
                icon = VaadinIcon.QUESTION_CIRCLE.create();
                theme = "badge";
            }
        }

        icon.getStyle().set("padding", "0");
        icon.setSize("14px");

        badge.add(icon, new Span(" " + text));
        badge.getElement().getThemeList().addAll(java.util.Arrays.asList(theme.split(" ")));
        badge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "4px");

        return badge;
    }

    /**
     * Erstellt Aktions-Buttons für Grid-Zeilen.
     *
     * @param eintrag Der Eintrag
     * @return Layout mit Buttons
     */
    private HorizontalLayout createActionButtons(SchiessnachweisEintrag eintrag) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        if (eintrag.kannGeloeschtWerden()) {
            Button deleteButton = new Button("Löschen", new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(e -> deleteEintrag(eintrag));
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

            // Zeige/Verstecke Empty State Message
            boolean isEmpty = eintraege.isEmpty();
            grid.setVisible(!isEmpty);
            emptyStateMessage.setVisible(isEmpty);
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
