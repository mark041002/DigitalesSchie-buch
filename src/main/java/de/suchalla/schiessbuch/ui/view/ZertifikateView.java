package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.service.BenutzerService;
import jakarta.annotation.security.RolesAllowed;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View für Zertifikatsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Route(value = "admin/zertifikate", layout = MainLayout.class)
@PageTitle("Zertifikate | Digitales Schießbuch")
@RolesAllowed("ADMIN")
public class ZertifikateView extends VerticalLayout {
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final BenutzerService benutzerService;
    private final Grid<DigitalesZertifikat> grid = new Grid<>(DigitalesZertifikat.class, false);
    private Div emptyStateMessage;
    private final ComboBox<Benutzer> benutzerComboBox = new ComboBox<>("Benutzer");
    private final ComboBox<String> typComboBox = new ComboBox<>("Zertifikatstyp");
    private final TextField seriennummerField = new TextField("Seriennummer");
    private final DatePicker gueltigAbPicker = new DatePicker("Gültig ab");
    private final DatePicker gueltigBisPicker = new DatePicker("Gültig bis");

    public ZertifikateView(DigitalesZertifikatRepository zertifikatRepository, BenutzerService benutzerService) {
        this.zertifikatRepository = zertifikatRepository;
        this.benutzerService = benutzerService;
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        typComboBox.setItems("ROOT", "VEREIN", "AUFSEHER");
        typComboBox.setRequired(true);
        typComboBox.setPlaceholder("Typ auswählen...");
        createContent();
        updateGrid();
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

        H2 title = new H2("Zertifikatsverwaltung");
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
                "Erstellen und verwalten Sie Zertifikate für Benutzer. Zertifikate können verschiedene Typen haben und ein Ablaufdatum besitzen."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Formular-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        // Formular
        benutzerComboBox.setRequired(true);
        benutzerComboBox.setItems(benutzerService.findAlleBenutzer());
        benutzerComboBox.setItemLabelGenerator(Benutzer::getVollstaendigerName);
        benutzerComboBox.setPlaceholder("Benutzer auswählen...");

        typComboBox.setRequired(true);
        typComboBox.setItems("ROOT", "VEREIN", "AUFSEHER");
        typComboBox.setPlaceholder("Typ auswählen...");

        seriennummerField.setRequired(true);
        gueltigAbPicker.setRequired(true);

        FormLayout formLayout = new FormLayout(
                benutzerComboBox, typComboBox, seriennummerField,
                gueltigAbPicker, gueltigBisPicker
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichernButton = new Button("Zertifikat erstellen", e -> speichereZertifikat());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        formContainer.add(formLayout, speichernButton);
        contentWrapper.add(formContainer);

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

        Icon emptyIcon = VaadinIcon.DIPLOMA.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Noch keine Zertifikate vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(DigitalesZertifikat::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(zertifikat -> zertifikat.getBenutzer() != null ? zertifikat.getBenutzer().getVollstaendigerName() : "-")
                .setHeader("Benutzer")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(DigitalesZertifikat::getZertifikatsTyp)
                .setHeader("Typ")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(DigitalesZertifikat::getSeriennummer)
                .setHeader("Seriennummer")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(zertifikat -> formatDatum(zertifikat.getGueltigAb()))
                .setHeader("Gültig ab")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> formatDatum(zertifikat.getGueltigBis()))
                .setHeader("Gültig bis")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> zertifikat.getWiderrufen() ? "Widerrufen" : "Gültig")
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> zertifikat.getWiderrufenAm() != null ? formatDatum(zertifikat.getWiderrufenAm()) : "-")
                .setHeader("Widerrufen am")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> zertifikat.getWiderrufsGrund() != null ? zertifikat.getWiderrufsGrund() : "-")
                .setHeader("Widerrufsgrund")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // CSS für rechtsbündige Ausrichtung
        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(DigitalesZertifikat zertifikat) {
        Button detailsButton = new Button("Details", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> zeigeDetailsDialog(zertifikat));

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        loeschenButton.addClickListener(e -> zeigeLoeschDialog(zertifikat));

        HorizontalLayout actions = new HorizontalLayout(detailsButton, loeschenButton);
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.getStyle().set("gap", "8px");
        return actions;
    }

    private void speichereZertifikat() {
        if (benutzerComboBox.isEmpty() || typComboBox.isEmpty() || seriennummerField.isEmpty() || gueltigAbPicker.isEmpty()) {
            Notification.show("Bitte füllen Sie alle Pflichtfelder aus")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (zertifikatRepository.findBySeriennummer(seriennummerField.getValue()).isPresent()) {
            Notification.show("Seriennummer ist bereits vergeben!")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            DigitalesZertifikat zertifikat = DigitalesZertifikat.builder()
                    .benutzer(benutzerComboBox.getValue())
                    .zertifikatsTyp(typComboBox.getValue())
                    .seriennummer(seriennummerField.getValue())
                    .gueltigAb(gueltigAbPicker.getValue().atStartOfDay())
                    .gueltigBis(gueltigBisPicker.isEmpty() ? null : gueltigBisPicker.getValue().atStartOfDay())
                    .widerrufen(false)
                    .build();
            zertifikatRepository.save(zertifikat);
            Notification.show("Zertifikat erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            benutzerComboBox.clear();
            typComboBox.clear();
            seriennummerField.clear();
            gueltigAbPicker.clear();
            gueltigBisPicker.clear();
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void zeigeLoeschDialog(DigitalesZertifikat zertifikat) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Zertifikat löschen");
        dialog.setText("Sind Sie sicher, dass Sie dieses Zertifikat löschen möchten?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> loescheZertifikat(zertifikat));
        dialog.open();
    }

    private void zeigeDetailsDialog(DigitalesZertifikat zertifikat) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Zertifikat-Details");
        dialog.setWidth("600px");
        dialog.setMaxWidth("95vw");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setWidthFull();
        layout.getStyle().set("gap", "var(--lumo-space-s)");

        // Detail-Zeilen
        layout.add(createDetailRow("ID:", String.valueOf(zertifikat.getId())));
        layout.add(createDetailRow("Benutzer:", zertifikat.getBenutzer() != null ? zertifikat.getBenutzer().getVollstaendigerName() : "-"));
        layout.add(createDetailRow("E-Mail:", zertifikat.getBenutzer() != null ? zertifikat.getBenutzer().getEmail() : "-"));
        layout.add(createDetailRow("Typ:", zertifikat.getZertifikatsTyp()));
        layout.add(createDetailRow("Seriennummer:", zertifikat.getSeriennummer()));
        layout.add(createDetailRow("Gültig ab:", formatDatum(zertifikat.getGueltigAb())));
        layout.add(createDetailRow("Gültig bis:", formatDatum(zertifikat.getGueltigBis())));
        layout.add(createDetailRow("Widerrufen am:", formatDatum(zertifikat.getWiderrufenAm())));
        layout.add(createDetailRow("Widerrufsgrund:", zertifikat.getWiderrufsGrund() != null ? zertifikat.getWiderrufsGrund() : "-"));

        Button schliessenButton = new Button("Schließen", e -> dialog.close());
        schliessenButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(layout);
        dialog.getFooter().add(schliessenButton);
        dialog.open();
    }

    private com.vaadin.flow.component.html.Div createDetailRow(String label, String value) {
        com.vaadin.flow.component.html.Div row = new com.vaadin.flow.component.html.Div();
        row.addClassName("detail-row");
        row.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "180px 1fr")
                .set("gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s) 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        com.vaadin.flow.component.html.Span labelSpan = new com.vaadin.flow.component.html.Span(label);
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-secondary-text-color)");

        com.vaadin.flow.component.html.Span valueSpan = new com.vaadin.flow.component.html.Span(value != null ? value : "-");
        valueSpan.getStyle()
                .set("color", "var(--lumo-body-text-color)")
                .set("word-break", "break-word");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void loescheZertifikat(DigitalesZertifikat zertifikat) {
        try {
            zertifikatRepository.deleteById(zertifikat.getId());
            Notification.show("Zertifikat erfolgreich gelöscht")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateGrid() {
        List<DigitalesZertifikat> zertifikate = zertifikatRepository.findAllWithDetails();
        grid.setItems(zertifikate);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = zertifikate.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }

    private String formatDatum(java.time.LocalDateTime datum) {
        if (datum == null) return "-";
        return datum.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}