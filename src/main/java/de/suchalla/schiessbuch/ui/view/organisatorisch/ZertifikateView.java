package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.service.EmailService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View für Zertifikatsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Route(value = "zertifikate", layout = MainLayout.class)
@PageTitle("Zertifikate | Digitales Schießbuch")
@RolesAllowed({"ADMIN", "AUFSEHER", "SCHIESSSTAND_AUFSEHER", "VEREINS_CHEF"})
public class ZertifikateView extends VerticalLayout {
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final BenutzerRepository benutzerRepository;
    private final BenutzerService benutzerService;
    private final EmailService emailService;
    private Tab gueltigTab;
    private Tab widerrufenTab;
    private Tab aktuellerTab;
    private final Grid<DigitalesZertifikat> grid = new Grid<>(DigitalesZertifikat.class, false);
    private Div emptyStateMessage;

    public ZertifikateView(DigitalesZertifikatRepository zertifikatRepository,
                          BenutzerRepository benutzerRepository,
                          BenutzerService benutzerService,
                          EmailService emailService) {
        this.zertifikatRepository = zertifikatRepository;
        this.benutzerRepository = benutzerRepository;
        this.benutzerService = benutzerService;
        this.emailService = emailService;
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        createContent();
        updateGrid();
    }

    private void createContent() {
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.setWidthFull();

        // Header-Bereich
        Div header = ViewComponentHelper.createGradientHeader("Zertifikatsverwaltung");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox(
                "Verwalten Sie Zertifikate für Benutzer und Vereine."
        );
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
        Div gridContainer = ViewComponentHelper.createGridContainer();

        emptyStateMessage = ViewComponentHelper.createEmptyStateMessage(
                "Noch keine Zertifikate vorhanden.", VaadinIcon.DIPLOMA
        );
        emptyStateMessage.setVisible(false);

        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);
        grid.setWidthFull();
        grid.getStyle()
                .set("flex", "1 1 auto")
                .set("min-height", "0");

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
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator((z1, z2) -> {
                    String typ1 = z1.getZertifikatsTyp() != null ? z1.getZertifikatsTyp() : "";
                    String typ2 = z2.getZertifikatsTyp() != null ? z2.getZertifikatsTyp() : "";
                    return typ1.compareToIgnoreCase(typ2);
                });

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
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator((z1, z2) -> {
                    String name1 = "";
                    if (z1.getVerein() != null) {
                        name1 = z1.getVerein().getName();
                    } else if (z1.getSchiesstand() != null) {
                        name1 = z1.getSchiesstand().getName();
                    }

                    String name2 = "";
                    if (z2.getVerein() != null) {
                        name2 = z2.getVerein().getName();
                    } else if (z2.getSchiesstand() != null) {
                        name2 = z2.getSchiesstand().getName();
                    }

                    return name1.compareToIgnoreCase(name2);
                });

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
                .setWidth("120px")
                .setFlexGrow(0);

        grid.addColumn(zertifikat -> zertifikat.getWiderrufenAm() != null ? formatDatum(zertifikat.getWiderrufenAm()) : "-")
                .setHeader("Widerrufen am")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("280px")
                .setFlexGrow(0);


        gridContainer.add(grid, emptyStateMessage);
        contentWrapper.add(gridContainer);
        contentWrapper.expand(gridContainer);
        add(contentWrapper);
    }

    private HorizontalLayout createActionButtons(DigitalesZertifikat zertifikat) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);
        actions.setPadding(false);
        actions.setWidthFull();
        actions.getStyle()
                .set("gap", "8px")
                .set("flex-wrap", "wrap");

        Button detailsButton = new Button("Details", VaadinIcon.EYE.create());
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> zeigeDetailsDialog(zertifikat));
        actions.add(detailsButton);

        if (aktuellerTab == gueltigTab) {
            // Widerrufen-Button nur bei Personen-Zertifikaten anzeigen
            // und nur wenn der aktuelle Benutzer berechtigt ist (Admin oder Vereinschef des betroffenen Vereins)
            if (zertifikat.getBenutzer() != null && userCanRevoke(zertifikat)) {
                Button widerrufenButton = new Button("Widerrufen", VaadinIcon.BAN.create());
                widerrufenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                widerrufenButton.addClickListener(e -> zeigeLoeschDialog(zertifikat));
                actions.add(widerrufenButton);
            }
        } else {
            Button endgueltigLoeschenButton = new Button("Endgültig löschen", VaadinIcon.TRASH.create());
            endgueltigLoeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            endgueltigLoeschenButton.addClickListener(e -> zeigeEndgueltigLoeschenDialog(zertifikat));
            actions.add(endgueltigLoeschenButton);
        }

        return actions;
    }

    private Span createStatusBadge(DigitalesZertifikat zertifikat) {
        Span badge = new Span();

        if (zertifikat.isWiderrufen()) {
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

        Button widerrufenButton = new Button("Widerrufen", e -> {
            loescheZertifikat(zertifikat, grundField.getValue());
            dialog.close();
        });
        widerrufenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> dialog.close());

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
        dialog.setCancelText("Abbrechen");
        dialog.addConfirmListener(e -> endgueltigLoescheZertifikat(zertifikat));
        dialog.open();
    }

    private void loescheZertifikat(DigitalesZertifikat zertifikat, String grund) {
        // Zusätzliche serverseitige Prüfung: nur Admins und Vereinschefs dürfen widerrufen
        if (!userCanRevoke(zertifikat)) {
            Notification.show("Sie haben keine Berechtigung, dieses Zertifikat zu widerrufen.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            String typ = zertifikat.getZertifikatsTyp() != null ? zertifikat.getZertifikatsTyp().toUpperCase() : "";
            // Verhindere Widerruf von ROOT und VEREIN Zertifikaten
            if ("ROOT".equals(typ) || "VEREIN".equals(typ)) {
                Notification.show("Dieses Zertifikat kann nicht widerrufen werden.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

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

            // Sende E-Mail-Benachrichtigung an den Benutzer
            emailService.notifyCertificateRevoked(zertifikat);

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

        Benutzer aktuellerBenutzer = benutzerService.findeBenutzerByEmailWithMitgliedschaften(username);
        if (aktuellerBenutzer == null) {
            grid.setItems(List.of());
            grid.setVisible(false);
            emptyStateMessage.setVisible(true);
            return;
        }

        // Admin sieht alle Zertifikate
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<DigitalesZertifikat> zertifikate;
        if (aktuellerTab == gueltigTab) {
            zertifikate = zertifikatRepository.findAllGueltigeWithDetailsAndMitgliedschaften();
        } else {
            zertifikate = zertifikatRepository.findAllWiderrufeneWithDetailsAndMitgliedschaften();
        }

        // Für Nicht-Admins: Filtere Zertifikate nach Zugehörigkeit
        if (!isAdmin) {
            zertifikate = zertifikate.stream()
                    .filter(z -> istZertifikatZugaenglich(z, aktuellerBenutzer))
                    .toList();
        }

        grid.setItems(zertifikate);
        grid.getDataProvider().refreshAll();

        boolean isEmpty = zertifikate.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }

    private boolean istZertifikatZugaenglich(DigitalesZertifikat zertifikat, Benutzer benutzer) {
        // Eigene Zertifikate
        if (zertifikat.getBenutzer() != null && zertifikat.getBenutzer().getId().equals(benutzer.getId())) {
            return true;
        }

        // Zertifikate von Vereinen/Schießständen, in denen der Benutzer Aufseher oder Vereinschef ist
        if (benutzer.getVereinsmitgliedschaften() != null) {
            for (var mitgliedschaft : benutzer.getVereinsmitgliedschaften()) {
                if (Boolean.TRUE.equals(mitgliedschaft.getIstVereinschef()) || Boolean.TRUE.equals(mitgliedschaft.getIstAufseher())) {
                    Verein vereinDesMitglieds = mitgliedschaft.getVerein();
                    
                    // Prüfe ob Zertifikat zu diesem Verein gehört
                    if (zertifikat.getVerein() != null && zertifikat.getVerein().getId().equals(vereinDesMitglieds.getId())) {
                        return true;
                    }
                    
                    // Prüfe ob Zertifikat zu einem Schießstand dieses Vereins gehört
                    if (zertifikat.getSchiesstand() != null && zertifikat.getSchiesstand().getVerein() != null 
                            && zertifikat.getSchiesstand().getVerein().getId().equals(vereinDesMitglieds.getId())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Prüft, ob der aktuell eingeloggte Benutzer berechtigt ist, das gegebene Zertifikat zu widerrufen.
     * Erlaubt sind: ROLE_ADMIN oder Vereinschef des betroffenen Vereins (falls vorhanden).
     */
    private boolean userCanRevoke(DigitalesZertifikat zertifikat) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Admins dürfen immer
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        String username = auth.getName();
        if (username == null) return false;

        Benutzer current = benutzerService.findeBenutzerByEmailWithMitgliedschaften(username);
        if (current == null) return false;

        // Bestimme betroffenen Verein des Zertifikats
        Verein certVerein = zertifikat.getVerein();
        if (certVerein == null && zertifikat.getSchiesstand() != null) {
            certVerein = zertifikat.getSchiesstand().getVerein();
        }
        if (certVerein == null) return false;

        if (current.getVereinsmitgliedschaften() != null) {
            for (var vm : current.getVereinsmitgliedschaften()) {
                if (vm.getVerein() != null && vm.getVerein().getId().equals(certVerein.getId())
                        && Boolean.TRUE.equals(vm.getIstVereinschef())) {
                    return true;
                }
            }
        }

        return false;
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
        layout.add(new Paragraph("Status: " + (zertifikat.isWiderrufen() ? "Widerrufen" : "Gültig")));
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
