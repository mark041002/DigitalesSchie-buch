package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * Dashboard-View als Startseite.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Digitales Schießbuch")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final SchiessnachweisService schiessnachweisService;
    private final VereinsmitgliedschaftService vereinsmitgliedschaftService;
    private final DisziplinService disziplinService;

    public DashboardView(SecurityService securityService,
                         SchiessnachweisService schiessnachweisService,
                         VereinsmitgliedschaftService vereinsmitgliedschaftService,
                         DisziplinService disziplinService) {
        this.securityService = securityService;
        this.schiessnachweisService = schiessnachweisService;
        this.vereinsmitgliedschaftService = vereinsmitgliedschaftService;
        this.disziplinService = disziplinService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        setHeightFull();
        addClassName("view-container");
        getStyle()
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden");

        createContent();
    }

    /**
     * Erstellt den Dashboard-Inhalt.
     */
    private void createContent() {
        Benutzer currentUser = securityService.getAuthenticatedUser();

        if (currentUser == null) {
            createGuestContent();
            return;
        }

        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        Div header = ViewComponentHelper.createGradientHeader("Willkommen zurück, " + currentUser.getVorname() + "!");
        header.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        header.setWidthFull();
        contentWrapper.add(header);

        Div statsGrid = createStatsGrid(currentUser);
        statsGrid.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        statsGrid.setWidthFull();
        contentWrapper.add(statsGrid);

        Div quickActions = createQuickActions(currentUser);
        quickActions.getStyle().set("margin-top", "var(--lumo-space-m)");
        quickActions.setWidthFull();
        contentWrapper.add(quickActions);

        add(contentWrapper);
    }

    private void createGuestContent() {
        H2 title = new H2("Willkommen im Digitalen Schießbuch");
        title.getStyle()
                .set("margin-top", "var(--lumo-space-l)")
                .set("text-align", "center");

        Span subtitle = new Span("Bitte melden Sie sich an, um fortzufahren.");
        subtitle.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }


    private Div createStatsGrid(Benutzer user) {
        Div grid = new Div();
        grid.addClassName("stats-grid");

        try {
            long unsignierteEintraege = schiessnachweisService.zaehleUnsignierteEintraege(user);

            long offeneEintraege = 0;
            long beitrittsanfragen = 0;
            if (user.getRolle() == BenutzerRolle.AUFSEHER || user.getRolle() == BenutzerRolle.SCHIESSSTAND_AUFSEHER || user.getRolle() == BenutzerRolle.VEREINS_CHEF || user.getRolle() == BenutzerRolle.ADMIN) {
                Verein verein = user.getVereinsmitgliedschaften().stream()
                    .filter(m -> (Boolean.TRUE.equals(m.getIstAufseher()) || Boolean.TRUE.equals(m.getIstVereinschef())) && m.getVerein() != null)
                    .map(Vereinsmitgliedschaft::getVerein)
                    .findFirst()
                    .orElse(null);
                if (verein != null) {
                    List<Schiesstand> schiesstaende = disziplinService.findeSchiesstaendeVonVerein(verein);
                    offeneEintraege = schiesstaende.stream()
                        .mapToLong(schiesstand -> schiessnachweisService.findeUnsignierteEintraege(schiesstand).size())
                        .sum();
                    beitrittsanfragen = vereinsmitgliedschaftService.findeBeitrittsanfragen(verein).size();
                }
            }

            if (user.getRolle() == BenutzerRolle.AUFSEHER || user.getRolle() == BenutzerRolle.SCHIESSSTAND_AUFSEHER) {
                grid.add(
                        createStatCard("Meine unsignierten Einträge", String.valueOf(unsignierteEintraege),
                                VaadinIcon.EDIT, "var(--lumo-warning-color)", "meine-eintraege"),
                        createStatCard("Einträge zum Signieren", String.valueOf(offeneEintraege),
                                VaadinIcon.CLIPBOARD_CHECK, "var(--lumo-error-color)", "eintraege-verwaltung?tab=unsigniert"),
                        createStatCard("Rolle", getRollenText(user.getRolle()),
                                VaadinIcon.USER_STAR, "var(--lumo-success-color)", null)
                );
            } else if (user.getRolle() == BenutzerRolle.VEREINS_CHEF) {
                grid.add(
                        createStatCard("Meine unsignierten Einträge", String.valueOf(unsignierteEintraege),
                                VaadinIcon.EDIT, "var(--lumo-warning-color)", "meine-eintraege"),
                        createStatCard("Einträge zum Signieren", String.valueOf(offeneEintraege),
                                VaadinIcon.CLIPBOARD_CHECK, "var(--lumo-error-color)", "eintraege-verwaltung?tab=unsigniert"),
                        createStatCard("Beitrittsanfragen", String.valueOf(beitrittsanfragen),
                                VaadinIcon.USERS, "var(--lumo-primary-color)", "mitgliedsverwaltung"),
                        createStatCard("Rolle", getRollenText(user.getRolle()),
                                VaadinIcon.USER_STAR, "var(--lumo-success-color)", null)
                );
            } else {
                grid.add(
                        createStatCard("Unsignierte Einträge", String.valueOf(unsignierteEintraege),
                                VaadinIcon.EDIT, "var(--lumo-warning-color)", "meine-eintraege"),
                        createStatCard("Rolle", getRollenText(user.getRolle()),
                                VaadinIcon.USER_STAR, "var(--lumo-success-color)", null)
                );
            }
        } catch (Exception e) {
            grid.add(
                createStatCard("Rolle", getRollenText(user.getRolle()),
                        VaadinIcon.USER_STAR, "var(--lumo-success-color)", null)
            );
        }

        return grid;
    }

    private Div createStatCard(String label, String value, VaadinIcon icon, String color, String route) {
        Div card = new Div();
        card.addClassName("stat-card");
        card.getStyle()
                .set("border-left", "4px solid " + color)
                .set("cursor", route != null ? "pointer" : "default")
                .set("background", "#ffffff")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        if (route != null) {
            card.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(route))
            );
        }

        Div header = new Div();
        header.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center")
                .set("margin-bottom", "var(--lumo-space-m)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        Icon iconComponent = icon.create();
        iconComponent.setSize("24px");
        iconComponent.getStyle().set("color", color);

        header.add(labelSpan, iconComponent);

        Span valueSpan = new Span(value);
        String fontSize = value.length() > 12 ? "var(--lumo-font-size-xxl)" : "var(--lumo-font-size-xxxl)";
        valueSpan.getStyle()
                .set("font-size", fontSize)
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)")
                .set("word-wrap", "break-word")
                .set("overflow-wrap", "break-word")
                .set("max-width", "100%")
                .set("display", "block")
                .set("line-height", "1.2");

        card.add(header, valueSpan);
        return card;
    }

    private Div createQuickActions(Benutzer user) {
        Div outerContainer = new Div();
        outerContainer.setWidthFull();
        outerContainer.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-contrast-5pct) 0%, var(--lumo-primary-color-10pct) 100%)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("box-sizing", "border-box")
                .set("max-width", "100%")
                .set("overflow", "hidden");

        H2 title = new H2("Schnellzugriff");
        title.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-align", "center");

        Div actionsGrid = new Div();
        actionsGrid.addClassName("quick-actions");

        actionsGrid.add(
                createActionButton("Neuer Eintrag", VaadinIcon.PLUS, "neuer-eintrag"),
                createActionButton("Meine Einträge", VaadinIcon.BOOK, "meine-eintraege"),
                createActionButton("Zertifikat verifizieren", VaadinIcon.DIPLOMA, "zertifikat-verifizierung"),
                createActionButton("Profil", VaadinIcon.USER, "profil")
        );

        if (user.getRolle() == BenutzerRolle.VEREINS_CHEF) {
                        actionsGrid.add(createActionButton("Alle Mitglieder", VaadinIcon.USERS, "mitgliedsverwaltung"));
                        actionsGrid.add(createActionButton("Beitrittsanfragen", VaadinIcon.USER_CHECK, "mitgliedsverwaltung?tab=beantragt"));
                        actionsGrid.add(createActionButton("Eintragsverwaltung", VaadinIcon.RECORDS, "eintraege-verwaltung"));
        }

        if (user.getRolle() == BenutzerRolle.AUFSEHER || user.getRolle() == BenutzerRolle.SCHIESSSTAND_AUFSEHER) {
            actionsGrid.add(createActionButton("Eintragsverwaltung", VaadinIcon.RECORDS, "eintraege-verwaltung"));
        }

        Div infoBox = new Div();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        infoIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("margin-right", "var(--lumo-space-s)")
                .set("flex-shrink", "0");

        Span infoText = new Span("Nutzen Sie diese Aktionen für häufig verwendete Funktionen");
        infoText.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "500");

        Div infoContent = new Div();
        infoContent.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("flex-wrap", "wrap");
        infoContent.add(infoIcon, infoText);

        infoBox.add(infoContent);

        outerContainer.add(title, actionsGrid, infoBox);
        return outerContainer;
    }

    private Div createActionButton(String label, VaadinIcon icon, String route) {
        Div button = new Div();
        button.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("text-align", "center")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s")
                .set("border", "2px solid transparent")
                .set("min-width", "140px")
                .set("max-width", "320px")
                .set("flex", "1 1 140px") // grow, shrink, basis -> nebeneinander, wrap bei wenig Platz
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center");

        button.addClickListener(e ->
            getUI().ifPresent(ui -> ui.navigate(route))
        );
        button.addClassName("action-button-hover");

        Icon iconComponent = icon.create();
        iconComponent.setSize("32px");
        iconComponent.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("margin-bottom", "var(--lumo-space-xs)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-weight", "500")
                .set("color", "var(--lumo-primary-text-color)");

        Div iconContainer = new Div(iconComponent);
        iconContainer.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        button.add(iconContainer, labelSpan);
        return button;
    }

    /**
     * Gibt den deutschen Text für eine Benutzerrolle zurück.
     *
     * @param rolle Die Benutzerrolle
     * @return Deutscher Rollentext
     */
    private String getRollenText(BenutzerRolle rolle) {
        return switch (rolle) {
            case ADMIN -> "Administrator";
            case VEREINS_CHEF -> "Vereinschef";
            case AUFSEHER -> "Aufseher";
            case SCHIESSSTAND_AUFSEHER -> "Schießstandaufseher";
            case SCHUETZE -> "Schütze";
        };
    }
}
