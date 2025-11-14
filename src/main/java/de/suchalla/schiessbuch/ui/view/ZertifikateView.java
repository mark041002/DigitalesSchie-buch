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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
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
    private Tab gueltigTab;
    private Tab widerrufenTab;
    private Tab aktuellerTab;
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
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.setSizeFull();
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
                "Verwalten Sie Zertifikate für Benutzer und Vereine."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Tabs für Status-Filter
        gueltigTab = new Tab("Gültig");
        widerrufenTab = new Tab("Widerrufen");
        aktuellerTab = gueltigTab;
        Tabs tabs = new Tabs(gueltigTab, widerrufenTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            aktuellerTab = event.getSelectedTab();
            updateGrid();
        });
        contentWrapper.add(tabs);

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

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("120px")
                .setAutoWidth(true)
                .setFlexGrow(0);


        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(DigitalesZertifikat zertifikat) {
        Button detailsButton = new Button("Details", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> zeigeDetailsDialog(zertifikat));

        if (aktuellerTab == gueltigTab) {
            Button widerrufenButton = new Button("Widerrufen", VaadinIcon.BAN.create());
            widerrufenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            widerrufenButton.addClickListener(e -> zeigeLoeschDialog(zertifikat));
            return new HorizontalLayout(detailsButton, widerrufenButton);
        } else {
            Button endgueltigLoeschenButton = new Button("Endgültig löschen", VaadinIcon.TRASH.create());
            endgueltigLoeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            endgueltigLoeschenButton.addClickListener(e -> zeigeEndgueltigLoeschenDialog(zertifikat));
            return new HorizontalLayout(detailsButton, endgueltigLoeschenButton);
        }
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
        // Dialog zum Widerrufen mit optionalem Grund
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Zertifikat widerrufen");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        Paragraph info = new Paragraph("Möchten Sie dieses Zertifikat wirklich widerrufen? (Root- und Vereinszertifikate können nicht widerrufen werden)");
        info.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");

        TextArea grundField = new TextArea("Widerrufsgrund (optional)");
        grundField.setWidthFull();
        grundField.setHeight("120px");

        layout.add(info, grundField);

        Button widerrufenButton = new Button("Widerrufen", event -> {
            loescheZertifikat(zertifikat, grundField.getValue());
            dialog.close();
        });
        widerrufenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", event -> dialog.close());

        dialog.add(layout);
        dialog.getFooter().add(abbrechenButton, widerrufenButton);
        dialog.open();
    }

    private void zeigeEndgueltigLoeschenDialog(DigitalesZertifikat zertifikat) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Zertifikat endgültig löschen");
        dialog.setText("Sind Sie sicher, dass Sie dieses Zertifikat unwiderruflich löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Endgültig löschen");
        dialog.setRejectText("Abbrechen");
        dialog.addConfirmListener(e -> endgueltigLoescheZertifikat(zertifikat));
        dialog.open();
    }

    private void loescheZertifikat(DigitalesZertifikat zertifikat, String grund) {
        try {
            String typ = zertifikat.getZertifikatsTyp() != null ? zertifikat.getZertifikatsTyp().toUpperCase() : "";
            // Verhindere Widerruf von ROOT und VEREIN Zertifikaten
            if ("ROOT".equals(typ) || "VEREIN".equals(typ)) {
                Notification.show("Dieses Zertifikat kann nicht widerrufen werden.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Markiere Zertifikat als widerrufen
            zertifikat.setWiderrufen(true);
            zertifikat.setWiderrufenAm(LocalDateTime.now());
            zertifikat.setWiderrufsGrund(grund != null && !grund.isBlank() ? grund : "Vom Administrator widerrufen");

            // Wenn das Zertifikat einem Benutzer gehört, entziehe Rollen/Flags falls nötig
            Benutzer b = zertifikat.getBenutzer();
            if (b != null) {
                boolean changed = false;

                // Bestimme betroffenen Verein (falls vorhanden)
                Verein certVerein = zertifikat.getVerein();
                if (certVerein == null && zertifikat.getSchiesstand() != null) {
                    certVerein = zertifikat.getSchiesstand().getVerein();
                }

                // Entziehe Vereinsrollen in den Mitgliedschaften zwar nur für den betroffenen Verein
                if (b.getVereinsmitgliedschaften() != null) {
                    for (var vm : b.getVereinsmitgliedschaften()) {
                        if (certVerein == null || vm.getVerein() == null || vm.getVerein().getId().equals(certVerein.getId())) {
                            if (Boolean.TRUE.equals(vm.getIstVereinschef())) {
                                vm.setIstVereinschef(false);
                                changed = true;
                            }
                            if (Boolean.TRUE.equals(vm.getIstAufseher())) {
                                vm.setIstAufseher(false);
                                changed = true;
                            }
                        }
                    }
                }

                // Entziehe die globale Rolle, falls Zertifikat Typen auf spezielle Rollen hindeuten
                if ("AUFSEHER".equals(typ) || "SCHIESSTANDAUFSEHER".equals(typ) || "SCHIESSSTAND_AUFSEHER".equals(typ) || "VEREINS_CHEF".equals(typ)) {
                    b.setRolle(de.suchalla.schiessbuch.model.enums.BenutzerRolle.SCHUETZE);
                    changed = true;
                }

                if (changed) {
                    benutzerRepository.save(b);
                }
            }

            // Speichere das Zertifikat als widerrufen
            zertifikatRepository.save(zertifikat);
            Notification.show("Zertifikat erfolgreich widerrufen")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            updateGrid();
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void endgueltigLoescheZertifikat(DigitalesZertifikat zertifikat) {
        try {
            zertifikatRepository.deleteById(zertifikat.getId());
            Notification.show("Zertifikat endgültig gelöscht")
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

        Benutzer aktuellerBenutzer = benutzerRepository.findByEmailWithMitgliedschaften(username).orElse(null);
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
        List<DigitalesZertifikat> alleZertifikate = zertifikatRepository.findAllWithDetailsAndMitgliedschaften();

        if (aktuellerTab == gueltigTab) {
            // Zeige alle gültigen Zertifikate
            for (DigitalesZertifikat z : alleZertifikate) {
                if (!Boolean.TRUE.equals(z.getWiderrufen())) {
                    zertifikate.add(z);
                }
            }
        } else {
            // Zeige alle widerrufenen Zertifikate
            for (DigitalesZertifikat z : alleZertifikate) {
                if (Boolean.TRUE.equals(z.getWiderrufen())) {
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

    private void zeigeDetailsDialog(DigitalesZertifikat zertifikat) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Zertifikatsdetails");
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.add(new Paragraph("Seriennummer: " + (zertifikat.getSeriennummer() != null ? zertifikat.getSeriennummer() : "-")));
        layout.add(new Paragraph("Typ: " + (zertifikat.getZertifikatsTyp() != null ? zertifikat.getZertifikatsTyp() : "-")));
        layout.add(new Paragraph("Status: " + (zertifikat.getWiderrufen() ? "Widerrufen" : "Gültig")));
        layout.add(new Paragraph("Gültig seit: " + formatDatum(zertifikat.getGueltigSeit())));
        layout.add(new Paragraph("Widerrufen am: " + (zertifikat.getWiderrufenAm() != null ? formatDatum(zertifikat.getWiderrufenAm()) : "-")));
        layout.add(new Paragraph("Widerrufsgrund: " + (zertifikat.getWiderrufsGrund() != null ? zertifikat.getWiderrufsGrund() : "-")));
        if (zertifikat.getBenutzer() != null) {
            layout.add(new Paragraph("Benutzer: " + zertifikat.getBenutzer().getVollstaendigerName()));
        }
        if (zertifikat.getVerein() != null) {
            layout.add(new Paragraph("Verein: " + zertifikat.getVerein().getName()));
        }
        if (zertifikat.getSchiesstand() != null) {
            layout.add(new Paragraph("Schießstand: " + zertifikat.getSchiesstand().getName()));
        }
        Button closeButton = new Button("Schließen", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(layout);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }
}
