package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenachrichtigungsService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import jakarta.annotation.security.PermitAll;

/**
 * Dashboard-View als Startseite.
 *
 * Responsive: Schnellzugriff-Buttons nebeneinander (wrap bei kleinem Viewport).
 *
 * @author Markus Suchalla
 * @version 1.0.3 (responsive Schnellzugriff inline)
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Digitales Schießbuch")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final SecurityService securityService;
    private final SchiessnachweisService schiessnachweisService;
    private final BenachrichtigungsService benachrichtigungsService;

    public DashboardView(SecurityService securityService,
                         SchiessnachweisService schiessnachweisService,
                         BenachrichtigungsService benachrichtigungsService) {
        this.securityService = securityService;
        this.schiessnachweisService = schiessnachweisService;
        this.benachrichtigungsService = benachrichtigungsService;

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        setMaxWidth("1400px");
        getStyle().set("margin", "0 auto")
                .set("padding", "var(--lumo-space-m)");

        createContent();
    }

    /**
     * Erstellt den Dashboard-Inhalt.
     */
    private void createContent() {
        Benutzer currentUser = securityService.getAuthenticatedUser().orElse(null);

        if (currentUser == null) {
            createGuestContent();
            return;
        }

        // Willkommens-Header
        Div header = createWelcomeHeader(currentUser);
        header.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        header.setWidthFull();
        add(header);

        // Statistik-Cards (volle Breite, responsive durch CSS grid)
        Div statsGrid = createStatsGrid(currentUser);
        statsGrid.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        statsGrid.setWidthFull();
        add(statsGrid);

        // Schnellzugriff: unter den Statistik-Karten, Buttons nebeneinander (wrap)
        Div quickActions = createQuickActions(currentUser);
        quickActions.getStyle().set("margin-top", "var(--lumo-space-m)");
        quickActions.setWidthFull();
        add(quickActions);
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

    private Div createWelcomeHeader(Benutzer user) {
        Div header = new Div();
        header.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("color", "var(--lumo-primary-contrast-color)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 greeting = new H2("Willkommen zurück, " + user.getVorname() + "!");
        greeting.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-contrast-color)");

        Span roleInfo = new Span(getRollenText(user.getRolle()));
        roleInfo.getStyle()
                .set("display", "block")
                .set("margin-top", "var(--lumo-space-xs)")
                .set("opacity", "0.9")
                .set("font-size", "var(--lumo-font-size-m)");

        header.add(greeting, roleInfo);
        return header;
    }

    private Div createStatsGrid(Benutzer user) {
        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("gap", "var(--lumo-space-m)")
                .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))");

        try {
            long unsignierteEintraege = schiessnachweisService.zaehleUnsignierteEintraege(user);
            long ungeleseneNachrichten = benachrichtigungsService.zaehleUngelesene(user);

            grid.add(
                    createStatCard("Unsignierte Einträge", String.valueOf(unsignierteEintraege),
                            VaadinIcon.EDIT, "var(--lumo-warning-color)"),
                    createStatCard("Ungelesene Nachrichten", String.valueOf(ungeleseneNachrichten),
                            VaadinIcon.BELL, "var(--lumo-primary-color)"),
                    createStatCard("Rolle", getRollenText(user.getRolle()),
                            VaadinIcon.USER_STAR, "var(--lumo-success-color)")
            );
        } catch (Exception e) {
            // Fallback wenn Services nicht verfügbar
            grid.add(
                // Status-Karte entfernt, keine Fallback-Karte mehr
            );
        }

        return grid;
    }

    private Div createStatCard(String label, String value, VaadinIcon icon, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("border-left", "4px solid " + color)
                .set("transition", "transform 0.2s, box-shadow 0.2s")
                .set("cursor", "default")
                .set("min-width", "0");

        // Hover-Effekt
        card.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.transform = 'translateY(-4px)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-m)';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.transform = 'translateY(0)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-s)';" +
                        "});"
        );

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
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)");

        card.add(header, valueSpan);
        return card;
    }

    private Div createQuickActions(Benutzer user) {
        Div container = new Div();
        container.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        H3 title = new H3("Schnellzugriff");
        title.getStyle().set("margin-top", "0");

        // Actions als flexible Zeile: nebeneinander auf breiten Bildschirmen, wrap auf schmalen
        Div actionsGrid = new Div();
        actionsGrid.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("flex-wrap", "wrap")
                .set("gap", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)")
                .set("align-items", "stretch");

        // Buttons: flexible Karten, nebeneinander; min-width sorgt für Umbruch auf Mobile
        actionsGrid.add(
                createActionButton("Neuer Eintrag", VaadinIcon.PLUS, "meine-eintraege"),
                createActionButton("Meine Einträge", VaadinIcon.BOOK, "meine-eintraege"),
                createActionButton("Benachrichtigungen", VaadinIcon.BELL, "benachrichtigungen"),
                createActionButton("Profil", VaadinIcon.USER, "profil")
        );

        container.add(title, actionsGrid);
        return container;
    }

    private Div createActionButton(String label, VaadinIcon icon, String route) {
        Div button = new Div();
        // Flex-Karte: steht nebeneinander, ist zentriert und responsive
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

        button.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                        "  this.style.background = 'var(--lumo-primary-color-10pct)';" +
                        "  this.style.borderColor = 'var(--lumo-primary-color)';" +
                        "  this.style.transform = 'translateY(-2px)';" +
                        "});" +
                        "this.addEventListener('mouseleave', () => {" +
                        "  this.style.background = 'var(--lumo-contrast-5pct)';" +
                        "  this.style.borderColor = 'transparent';" +
                        "  this.style.transform = 'translateY(0)';" +
                        "});" +
                        "this.addEventListener('click', () => {" +
                        "  window.location.href = '" + route + "';" +
                        "});"
        );

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
            case SCHUETZE -> "Schütze";
            default -> rolle.getBezeichnung();
        };
    }
}