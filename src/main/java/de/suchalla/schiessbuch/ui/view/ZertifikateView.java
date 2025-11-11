package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * View für Zertifikatsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Route(value = "admin/zertifikate", layout = MainLayout.class)
@PageTitle("Zertifikate | Digitales Schießbuch")
@RolesAllowed({"ADMIN", "AUFSEHER", "SCHIESSSTAND_AUFSEHER", "VEREINS_CHEF"})
public class ZertifikateView extends VerticalLayout {
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final BenutzerRepository benutzerRepository;
    private final Grid<DigitalesZertifikat> grid = new Grid<>(DigitalesZertifikat.class, false);
    private Div emptyStateMessage;

    public ZertifikateView(DigitalesZertifikatRepository zertifikatRepository,
                          BenutzerRepository benutzerRepository) {
        this.zertifikatRepository = zertifikatRepository;
        this.benutzerRepository = benutzerRepository;
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        createContent();
        updateGrid();
    }

    private void createContent() {
        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Zertifikatsverwaltung");
        title.getStyle().set("margin", "0");

        header.add(title);
        add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(
                "Verwalten Sie Zertifikate für Benutzer und Vereine."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        add(infoBox);

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
        grid.setWidthFull();
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);
        // Höhe wird dynamisch in updateGrid() gesetzt

        // Entfernte Spalten: ID, Widerrufen am, Widerrufsgrund
        // grid.addColumn(DigitalesZertifikat::getId)
        //         .setHeader("ID")
        //         .setWidth("80px")
        //         .setAutoWidth(true)
        //         .setFlexGrow(0)
        //         .setClassNameGenerator(item -> "align-right");
        grid.addColumn(zertifikat -> {
            if (zertifikat.getBenutzer() != null) {
                return zertifikat.getBenutzer().getVollstaendigerName();
            } else if (zertifikat.getVerein() != null) {
                return zertifikat.getVerein().getName();
            } else if (zertifikat.getSchiesstand() != null) {
                return zertifikat.getSchiesstand().getName();
            }
            return "-";
        })
                .setHeader("Benutzer/Verein/Schießstand")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(DigitalesZertifikat::getZertifikatsTyp)
                .setHeader("Typ")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(zertifikat -> {
            if (zertifikat.getVerein() != null) {
                return zertifikat.getVerein().getName();
            } else if (zertifikat.getSchiesstand() != null) {
                return zertifikat.getSchiesstand().getName();
            }
            return "-";
        })
                .setHeader("Verein/Schießstand")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(DigitalesZertifikat::getSeriennummer)
                .setHeader("Seriennummer")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(zertifikat -> formatDatum(zertifikat.getGueltigSeit()))
                .setHeader("Gültig seit")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> zertifikat.getWiderrufenAm() != null ? formatDatum(zertifikat.getWiderrufenAm()) : "-");
        // Entfernte Spalten:
        // grid.addColumn(zertifikat -> zertifikat.getWiderrufenAm() != null ? formatDatum(zertifikat.getWiderrufenAm()) : "-")
        //         .setHeader("Widerrufen am")
        //         .setAutoWidth(true)
        //         .setFlexGrow(0);
        // grid.addColumn(zertifikat -> zertifikat.getWiderrufsGrund() != null ? zertifikat.getWiderrufsGrund() : "-")
        //         .setHeader("Widerrufsgrund")
        //         .setAutoWidth(true)
        //         .setFlexGrow(1);
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
        add(gridContainer);
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

    private Span createStatusBadge(DigitalesZertifikat zertifikat) {
        Span badge = new Span();

        if (zertifikat.getWiderrufen()) {
            badge.setText("Widerrufen");
            badge.getElement().getThemeList().add("badge error");
            badge.getStyle()
                    .set("background-color", "#fee")
                    .set("color", "#c00")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-weight", "500")
                    .set("font-size", "0.875rem");
        } else {
            badge.setText("Gültig");
            badge.getElement().getThemeList().add("badge success");
            badge.getStyle()
                    .set("background-color", "#efe")
                    .set("color", "#0a0")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-weight", "500")
                    .set("font-size", "0.875rem");
        }

        return badge;
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

        if (zertifikat.getBenutzer() != null) {
            layout.add(createDetailRow("Benutzer:", zertifikat.getBenutzer().getVollstaendigerName()));
            layout.add(createDetailRow("E-Mail:", zertifikat.getBenutzer().getEmail()));
        }

        if (zertifikat.getVerein() != null) {
            layout.add(createDetailRow("Verein:", zertifikat.getVerein().getName()));
        }

        if (zertifikat.getSchiesstand() != null) {
            layout.add(createDetailRow("Schießstand:", zertifikat.getSchiesstand().getName()));
        }

        layout.add(createDetailRow("Typ:", zertifikat.getZertifikatsTyp()));
        layout.add(createDetailRow("Seriennummer:", zertifikat.getSeriennummer()));
        layout.add(createDetailRow("Gültig seit:", formatDatum(zertifikat.getGueltigSeit())));
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        if (username == null) {
            grid.setItems(List.of());
            grid.setVisible(false);
            emptyStateMessage.setVisible(true);
            return;
        }

        Benutzer aktuellerBenutzer = benutzerRepository.findByEmail(username).orElse(null);
        if (aktuellerBenutzer == null) {
            grid.setItems(List.of());
            grid.setVisible(false);
            emptyStateMessage.setVisible(true);
            return;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isVereinschef = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_VEREINS_CHEF".equals(a.getAuthority()));
        boolean isAufseher = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_AUFSEHER".equals(a.getAuthority()));
        boolean isSchiesstandAufseher = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SCHIESSSTAND_AUFSEHER".equals(a.getAuthority()));

        List<DigitalesZertifikat> zertifikate = new ArrayList<>();

        if (isAdmin) {
            zertifikate = zertifikatRepository.findAllWithDetails();
        } else if (isVereinschef) {
            List<Long> vereinsIds = aktuellerBenutzer.getVereinsmitgliedschaften().stream()
                    .filter(vm -> Boolean.TRUE.equals(vm.getIstVereinschef()))
                    .map(vm -> vm.getVerein().getId())
                    .toList();
            if (!vereinsIds.isEmpty()) {
                List<DigitalesZertifikat> alleZertifikate = zertifikatRepository.findAllWithDetails();
                for (DigitalesZertifikat z : alleZertifikate) {
                    if (z.getVerein() != null && vereinsIds.contains(z.getVerein().getId())
                        && "VEREIN".equals(z.getZertifikatsTyp())) {
                        zertifikate.add(z);
                    } else if (z.getBenutzer() != null && "AUFSEHER".equals(z.getZertifikatsTyp())) {
                        boolean istImVerein = z.getBenutzer().getVereinsmitgliedschaften().stream()
                                .anyMatch(vm -> vereinsIds.contains(vm.getVerein().getId()));
                        if (istImVerein) {
                            zertifikate.add(z);
                        }
                    }
                }
            }
        } else if (isAufseher) {
            List<DigitalesZertifikat> alleZertifikate = zertifikatRepository.findAllWithDetails();
            for (DigitalesZertifikat z : alleZertifikate) {
                if (z.getBenutzer() != null
                    && z.getBenutzer().getId().equals(aktuellerBenutzer.getId())
                    && "AUFSEHER".equals(z.getZertifikatsTyp())) {
                    zertifikate.add(z);
                }
            }
        } else if (isSchiesstandAufseher) {
            List<DigitalesZertifikat> alleZertifikate = zertifikatRepository.findAllWithDetails();
            for (DigitalesZertifikat z : alleZertifikate) {
                if (z.getBenutzer() != null
                    && z.getBenutzer().getId().equals(aktuellerBenutzer.getId())
                    && "SCHIESSTANDAUFSEHER".equals(z.getZertifikatsTyp())) {
                    zertifikate.add(z);
                }
            }
        }

        grid.setItems(zertifikate);
        grid.getDataProvider().refreshAll();

        boolean isEmpty = zertifikate.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);

        if (!isEmpty) {
            int anzahlEintraege = zertifikate.size();
            int zeilenHoehe = 53;
            int headerHoehe = 56;
            int minHoehe = 200;
            int maxHoehe = 800;
            int berechneteHoehe = headerHoehe + (anzahlEintraege * zeilenHoehe);
            int tatsaechlicheHoehe = Math.max(minHoehe, Math.min(maxHoehe, berechneteHoehe));

            grid.setHeight(tatsaechlicheHoehe + "px");
            grid.getStyle().remove("min-height");
        }
    }

    private String formatDatum(java.time.LocalDateTime datum) {
        if (datum == null) return "-";
        return datum.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
