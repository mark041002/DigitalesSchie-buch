package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.ui.view.administrativ.*;
import de.suchalla.schiessbuch.ui.view.oeffentlich.*;
import de.suchalla.schiessbuch.ui.view.organisatorisch.*;
import de.suchalla.schiessbuch.ui.view.persoenlich.*;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Span;

/**
 * Haupt-Layout der Anwendung mit Navigation.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@PermitAll
@CssImport("./themes/variables.css")
@CssImport("./themes/layout.css")
@CssImport("./themes/cards.css")
@CssImport("./themes/notifications.css")
@CssImport("./themes/dialogs.css")
@CssImport("./themes/buttons.css")
@CssImport("./themes/animations.css")
@CssImport("./themes/responsive.css")
@CssImport("./themes/sidebar-fix.css")
@JsModule("./js/MainLayout-interactions.js")
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private final Benutzer currentUser;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        createHeader();
        createDrawer();
    }

    /**
     * Erstellt den Header mit modernem Design.
     */
    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menü");

        // Logo mit Icon
        Icon logoIcon = VaadinIcon.BOOK.create();
        logoIcon.setSize("24px");
        logoIcon.getStyle()
                .set("margin-right", "var(--lumo-space-s)")
                .set("color", "var(--lumo-primary-color)");

        Span logoText = new Span("Digitales Schießbuch");
        logoText.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("color", "var(--lumo-header-text-color)");
        logoText.addClassName("header-logo-text");

        HorizontalLayout logoContent = new HorizontalLayout(logoIcon, logoText);
        logoContent.setSpacing(false);
        logoContent.setAlignItems(FlexComponent.Alignment.CENTER);
        logoContent.getStyle().set("gap", "var(--lumo-space-s)");

        RouterLink logoLink = new RouterLink("", DashboardView.class);
        logoLink.add(logoContent);
        logoLink.getElement().getStyle()
                .set("text-decoration", "none")
                .set("color", "inherit")
                .set("display", "flex")
                .set("align-items", "center");

        String username = currentUser != null ? currentUser.getVollstaendigerName() : "Gast";

        // Profil-Button (nur Desktop, rechts)
        Button profilButton = new Button(username, VaadinIcon.USER.create());
        profilButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(ProfilView.class))
        );
        profilButton.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("font-weight", "500");
        profilButton.addClassName("header-desktop-only");

        // Logout-Button (nur Desktop, rechts)
        Button logoutButton = new Button("Abmelden", VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(e -> securityService.logout());
        logoutButton.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-text-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("font-weight", "500");
        logoutButton.addClassName("header-desktop-only");

        HorizontalLayout rightButtons = new HorizontalLayout(profilButton, logoutButton);
        rightButtons.setSpacing(true);
        rightButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        rightButtons.getStyle().set("gap", "var(--lumo-space-s)");

        HorizontalLayout header = new HorizontalLayout(toggle, logoLink, rightButtons);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logoLink);
        header.setWidthFull();
        header.getStyle()
                .set("padding", "var(--lumo-space-m) var(--lumo-space-l)")
                .set("background", "linear-gradient(90deg, var(--lumo-base-color) 0%, var(--lumo-contrast-5pct) 100%)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("box-sizing", "border-box")
                .set("overflow-x", "hidden")
                .set("max-width", "100%");

        addToNavbar(header);
    }

    /**
     * Erstellt die Navigation im Drawer.
     */
    private void createDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.setWidthFull();
        drawerLayout.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-base-color)")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("height", "100%")
                .set("width", "100%")
                .set("max-width", "280px")
                .set("overflow", "hidden")
                .set("gap", "0");
        drawerLayout.addClassName("responsive-drawer");

        // Container für Navigationssektionen (wächst und nimmt verfügbaren Platz ein)
        VerticalLayout navSections = new VerticalLayout();
        navSections.setPadding(false);
        navSections.setSpacing(false);
        navSections.setWidthFull();
        navSections.getStyle()
                .set("flex", "1 1 auto")
                .set("overflow-y", "auto")
                .set("overflow-x", "hidden")
                .set("min-height", "0");

        if (currentUser != null) {
            // Persönliche Funktionen
            SideNav persoenlichNav = new SideNav();
            persoenlichNav.setWidthFull();
            persoenlichNav.getStyle()
                    .set("white-space", "normal")
                    .set("word-wrap", "break-word");
            persoenlichNav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.HOME.create()));
            persoenlichNav.addItem(new SideNavItem("Meine Einträge", MeineEintraegeView.class, VaadinIcon.BOOK.create()));
            persoenlichNav.addItem(new SideNavItem("Neuer Eintrag", NeuerEintragView.class, VaadinIcon.PLUS.create()));
            persoenlichNav.addItem(new SideNavItem("Meine Vereine", MeineVereineView.class, VaadinIcon.GROUP.create()));

            Details persoenlichDetails = createModernDetailsSection("Persönlich", persoenlichNav, VaadinIcon.USER, true, true);
            navSections.add(persoenlichDetails);

            // Vereinsfunktionen (nur für AUFSEHER und VEREINS_CHEF, nicht für SCHIESSSTAND_AUFSEHER)
            boolean istAufseherOderChef = (currentUser.getRolle() != de.suchalla.schiessbuch.model.enums.BenutzerRolle.SCHIESSSTAND_AUFSEHER) &&
                    currentUser.getVereinsmitgliedschaften().stream()
                            .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()) ||
                                    Boolean.TRUE.equals(m.getIstVereinschef()));

            if (istAufseherOderChef) {
                SideNav vereinNav = new SideNav();
                vereinNav.setWidthFull();
                vereinNav.getStyle()
                        .set("white-space", "normal")
                        .set("word-wrap", "break-word");

                // Eintragsverwaltung für Aufseher, Vereinschefs und Admins
                vereinNav.addItem(createDebouncedSideNavItem("Eintragsverwaltung", EintraegeVerwaltungView.class,
                        VaadinIcon.RECORDS.create()));

                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()))) {
                    vereinNav.addItem(new SideNavItem("Meine Zertifikate", ZertifikateView.class,
                            VaadinIcon.DIPLOMA.create()));
                }

                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstVereinschef()))) {
                    vereinNav.addItem(new SideNavItem("Vereinsdetails", VereinDetailsView.class,
                            VaadinIcon.COG.create()));
                    vereinNav.addItem(new SideNavItem("Mitgliedsverwaltung", MitgliedschaftenVerwaltenView.class,
                            VaadinIcon.USERS.create()));
                    vereinNav.addItem(new SideNavItem("Vereins-Zertifikate", ZertifikateView.class,
                            VaadinIcon.DIPLOMA.create()));
                }

                Details vereinDetails = createModernDetailsSection("Organisatorisches", vereinNav, VaadinIcon.BRIEFCASE, true, false);
                navSections.add(vereinDetails);
            }

            // Admin-Funktionen
            if (currentUser.getRolle() == de.suchalla.schiessbuch.model.enums.BenutzerRolle.ADMIN) {
                SideNav adminNav = new SideNav();
                adminNav.setWidthFull();
                adminNav.getStyle()
                        .set("white-space", "normal")
                        .set("word-wrap", "break-word");
                adminNav.addItem(new SideNavItem("Verbände", VerbaendeVerwaltungView.class, VaadinIcon.GLOBE.create()));
                adminNav.addItem(new SideNavItem("Vereine", VereineVerwaltungView.class, VaadinIcon.BUILDING.create()));
                adminNav.addItem(new SideNavItem("Schießstände", SchiesstaendeVerwaltungView.class, VaadinIcon.CROSSHAIRS.create()));
                adminNav.addItem(new SideNavItem("Mitglieder", MitgliederVerwaltungView.class, VaadinIcon.USERS.create()));
                adminNav.addItem(new SideNavItem("Alle Zertifikate", ZertifikateView.class, VaadinIcon.DIPLOMA.create()));

                Details adminDetails = createModernDetailsSection("Administration", adminNav, VaadinIcon.COG, true, false);
                navSections.add(adminDetails);
            }

            // Schießstandaufseher-Funktionen
            if (currentUser.getRolle() == de.suchalla.schiessbuch.model.enums.BenutzerRolle.SCHIESSSTAND_AUFSEHER) {
                SideNav schiesstandNav = new SideNav();
                schiesstandNav.setWidthFull();
                schiesstandNav.getStyle()
                        .set("white-space", "normal")
                        .set("word-wrap", "break-word");

                // Eintragsverwaltung für Schießstandaufseher
                schiesstandNav.addItem(createDebouncedSideNavItem("Eintragsverwaltung", EintraegeVerwaltungView.class,
                        VaadinIcon.RECORDS.create()));

                // Schießstanddetails
                schiesstandNav.addItem(new SideNavItem("Schießstanddetails", SchiesstandDetailsView.class, VaadinIcon.COG.create()));

                // Zertifikate
                schiesstandNav.addItem(new SideNavItem("Meine Zertifikate", ZertifikateView.class,
                        VaadinIcon.DIPLOMA.create()));

                Details schiesstandDetails = createModernDetailsSection("Organisatorisches", schiesstandNav, VaadinIcon.BRIEFCASE, true, false);
                navSections.add(schiesstandDetails);
            }
        }

        // Öffentliche Funktionen
        SideNav oeffentlichNav = new SideNav();
        oeffentlichNav.setWidthFull();
        oeffentlichNav.getStyle()
                .set("white-space", "normal")
                .set("word-wrap", "break-word");
        oeffentlichNav.addItem(new SideNavItem("Zertifikat verifizieren", ZertifikatVerifizierungView.class, VaadinIcon.CHECK_CIRCLE.create()));

        Details oeffentlichDetails = createModernDetailsSection("Öffentlich", oeffentlichNav, VaadinIcon.GLOBE_WIRE, false, false);
        navSections.add(oeffentlichDetails);

        // User-Aktionen in Navigation (nur Mobile) - wird als normale Sektion angezeigt
        if (currentUser != null) {
            VerticalLayout mobileUserSection = createMobileUserSection();
            navSections.add(mobileUserSection);
        }

        drawerLayout.add(navSections);

        addToDrawer(drawerLayout);
    }

    /**
     * Erstellt die User-Sektion für Mobile (wird in Navigation integriert).
     */
    private VerticalLayout createMobileUserSection() {
        VerticalLayout mobileSection = new VerticalLayout();
        mobileSection.setPadding(false);
        mobileSection.setSpacing(false);
        mobileSection.setWidthFull();
        mobileSection.addClassName("mobile-user-section");
        mobileSection.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding-top", "var(--lumo-space-m)")
                .set("border-top", "2px solid var(--lumo-contrast-10pct)");

        String username = currentUser != null ? currentUser.getVollstaendigerName() : "Gast";

        // Profil-Button für Mobile
        Button profilButton = new Button(username, VaadinIcon.USER.create());
        profilButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(ProfilView.class))
        );
        profilButton.setWidthFull();
        profilButton.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("justify-content", "flex-start")
                .set("margin-bottom", "var(--lumo-space-xs)");

        // Logout-Button für Mobile
        Button logoutButton = new Button("Abmelden", VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(e -> securityService.logout());
        logoutButton.setWidthFull();
        logoutButton.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-text-color)")
                .set("justify-content", "flex-start");

        mobileSection.add(profilButton, logoutButton);
        return mobileSection;
    }

    /**
     * Erstellt eine moderne Details-Section mit Icon und Styling.
     */
    private Details createModernDetailsSection(String title, SideNav content, VaadinIcon icon, boolean opened, boolean isFirst) {
        Icon sectionIcon = icon.create();
        sectionIcon.setSize("20px");
        sectionIcon.getStyle()
                .set("margin-right", "var(--lumo-space-s)")
                .set("color", "var(--lumo-primary-color)");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px")
                .set("color", "var(--lumo-header-text-color)");

        HorizontalLayout titleLayout = new HorizontalLayout(sectionIcon, titleSpan);
        titleLayout.setSpacing(false);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.getStyle().set("gap", "var(--lumo-space-s)");

        Details details = new Details();
        details.setSummary(titleLayout);
        details.add(content);
        details.setOpened(opened);
        details.setWidthFull();

        details.getStyle()
                .set("margin-top", isFirst ? "0" : "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-xs)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-s)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("border", "none")
                .set("transition", "all 0.2s ease");

        // Hover-Effekt mit ausgelagerter JavaScript-Funktion
        details.getElement().executeJs("window.addSidebarHoverEffects(this)");

        return details;
    }

    /**
     * Erstellt ein SideNavItem mit Debouncing-Schutz gegen mehrfaches Klicken.
     */
    private SideNavItem createDebouncedSideNavItem(String label, Class<? extends com.vaadin.flow.component.Component> navigationTarget, Icon icon) {
        SideNavItem item = new SideNavItem(label, navigationTarget, icon);

        // Füge JavaScript-basiertes Debouncing mit ausgelagerter Funktion hinzu
        item.getElement().executeJs("window.addNavigationDebounce(this, 500)");

        return item;
    }
}
