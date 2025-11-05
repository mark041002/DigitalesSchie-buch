package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;

/**
 * Haupt-Layout der Anwendung mit Navigation.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@PermitAll
@CssImport("./themes/schiessbuch-styles.css")
@CssImport("./themes/modern-enhancements.css")
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

        // Profil-Button (nur Desktop)
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

        // Logout-Button (nur Desktop)
        Button logout = new Button("Abmelden", VaadinIcon.SIGN_OUT.create());
        logout.addClickListener(e -> securityService.logout());
        logout.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-text-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("font-weight", "500");
        logout.addClassName("header-desktop-only");

        HorizontalLayout rightSide = new HorizontalLayout(profilButton, logout);
        rightSide.setSpacing(true);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.getStyle().set("gap", "var(--lumo-space-s)");

        HorizontalLayout header = new HorizontalLayout(toggle, logoLink, rightSide);
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
                .set("overflow-x", "hidden")
                .set("max-width", "100%")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("height", "100%");

        // Container für Navigationssektionen (wächst und nimmt verfügbaren Platz ein)
        VerticalLayout navSections = new VerticalLayout();
        navSections.setPadding(false);
        navSections.setSpacing(false);
        navSections.setWidthFull();
        navSections.getStyle()
                .set("flex", "1")
                .set("overflow-y", "auto");

        if (currentUser != null) {
            // Persönliche Funktionen
            SideNav persoenlichNav = new SideNav();
            persoenlichNav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.HOME.create()));
            persoenlichNav.addItem(new SideNavItem("Meine Einträge", MeineEintraegeView.class, VaadinIcon.BOOK.create()));
            persoenlichNav.addItem(new SideNavItem("Neuer Eintrag", NeuerEintragView.class, VaadinIcon.PLUS.create()));
            persoenlichNav.addItem(new SideNavItem("Meine Vereine", MeineVereineView.class, VaadinIcon.GROUP.create()));
            persoenlichNav.addItem(new SideNavItem("Benachrichtigungen", BenachrichtigungenView.class,
                    VaadinIcon.BELL.create()));

            Details persoenlichDetails = createModernDetailsSection("Persönlich", persoenlichNav, VaadinIcon.USER, true, true);
            navSections.add(persoenlichDetails);

            // Vereinsfunktionen
            boolean istAufseherOderChef = currentUser.getVereinsmitgliedschaften().stream()
                    .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()) ||
                                   Boolean.TRUE.equals(m.getIstVereinschef()));

            if (istAufseherOderChef) {
                SideNav vereinNav = new SideNav();

                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()))) {
                    vereinNav.addItem(new SideNavItem("Einträge signieren", EintraegeSignierenView.class,
                            VaadinIcon.EDIT.create()));
                    vereinNav.addItem(new SideNavItem("Meine Zertifikate", ZertifikateView.class,
                            VaadinIcon.DIPLOMA.create()));
                }

                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstVereinschef()))) {
                    vereinNav.addItem(new SideNavItem("Vereinsdetails", VereinDetailsView.class,
                            VaadinIcon.COG.create()));
                    vereinNav.addItem(new SideNavItem("Beitrittsanfragen", BeitrittsanfragenView.class,
                            VaadinIcon.ENVELOPE.create()));
                    vereinNav.addItem(createDebouncedSideNavItem("Eintragsverwaltung", EintraegeVerwaltungView.class,
                            VaadinIcon.RECORDS.create()));
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
                adminNav.addItem(new SideNavItem("Verbände", VerbaendeVerwaltungView.class, VaadinIcon.GLOBE.create()));
                adminNav.addItem(new SideNavItem("Vereine", VereineVerwaltungView.class, VaadinIcon.BUILDING.create()));
                adminNav.addItem(new SideNavItem("Schießstände", SchiesstaendeVerwaltungView.class, VaadinIcon.CROSSHAIRS.create()));
                adminNav.addItem(new SideNavItem("Disziplinen", DisziplinenVerwaltungView.class, VaadinIcon.RECORDS.create()));
                adminNav.addItem(new SideNavItem("Mitglieder", MitgliederVerwaltungView.class, VaadinIcon.USERS.create()));
                adminNav.addItem(new SideNavItem("Alle Zertifikate", ZertifikateView.class, VaadinIcon.DIPLOMA.create()));

                Details adminDetails = createModernDetailsSection("Administration", adminNav, VaadinIcon.COG, true, false);
                navSections.add(adminDetails);
            }
        }

        // Öffentliche Funktionen
        SideNav oeffentlichNav = new SideNav();
        oeffentlichNav.addItem(new SideNavItem("Zertifikat verifizieren", ZertifikatVerifizierungView.class, VaadinIcon.CHECK_CIRCLE.create()));

        Details oeffentlichDetails = createModernDetailsSection("Öffentlich", oeffentlichNav, VaadinIcon.GLOBE_WIRE, false, false);
        navSections.add(oeffentlichDetails);

        drawerLayout.add(navSections);

        // Mobile-only Bereich am unteren Ende der Sidebar
        if (currentUser != null) {
            VerticalLayout mobileUserActions = createMobileUserActions();
            drawerLayout.add(mobileUserActions);
        }

        addToDrawer(drawerLayout);
    }

    /**
     * Erstellt den Benutzeraktionsbereich für mobile Geräte am unteren Ende der Sidebar.
     */
    private VerticalLayout createMobileUserActions() {
        VerticalLayout mobileActions = new VerticalLayout();
        mobileActions.setPadding(false);
        mobileActions.setSpacing(false);
        mobileActions.setWidthFull();
        mobileActions.addClassName("drawer-mobile-only");
        mobileActions.getStyle()
                .set("border-top", "2px solid var(--lumo-contrast-10pct)")
                .set("padding-top", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)");

        String username = currentUser != null ? currentUser.getVollstaendigerName() : "Gast";

        // Klickbarer Benutzername (zentriert) - navigiert zum Profil
        Div userInfo = new Div();
        userInfo.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "var(--lumo-space-xs)")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease");

        // Klick-Handler für Navigation zum Profil
        userInfo.getElement().addEventListener("click", e ->
            getUI().ifPresent(ui -> ui.navigate(ProfilView.class))
        );

        // Hover-Effekt
        userInfo.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                "  this.style.background = 'var(--lumo-primary-color-20pct)';" +
                "  this.style.transform = 'scale(1.02)';" +
                "});" +
                "this.addEventListener('mouseleave', () => {" +
                "  this.style.background = 'var(--lumo-primary-color-10pct)';" +
                "  this.style.transform = 'scale(1)';" +
                "});"
        );

        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("32px");
        userIcon.getStyle().set("color", "var(--lumo-primary-color)");

        Span userNameSpan = new Span(username);
        userNameSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-align", "center")
                .set("max-width", "100%")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        Span profilHint = new Span("Zum Profil");
        profilHint.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center");

        userInfo.add(userIcon, userNameSpan, profilHint);

        // Logout-Button für Mobile
        Button mobileLogoutButton = new Button("Abmelden", VaadinIcon.SIGN_OUT.create());
        mobileLogoutButton.addClickListener(e -> securityService.logout());
        mobileLogoutButton.setWidthFull();
        mobileLogoutButton.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("color", "var(--lumo-error-text-color)");

        mobileActions.add(userInfo, mobileLogoutButton);
        return mobileActions;
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
        details.addContent(content);
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

        // Hover-Effekt
        details.getElement().executeJs(
                "this.addEventListener('mouseenter', () => {" +
                "  this.style.boxShadow = 'var(--lumo-box-shadow-s)';" +
                "  this.style.background = 'var(--lumo-contrast-10pct)';" +
                "});" +
                "this.addEventListener('mouseleave', () => {" +
                "  this.style.boxShadow = 'var(--lumo-box-shadow-xs)';" +
                "  this.style.background = 'var(--lumo-contrast-5pct)';" +
                "});"
        );

        return details;
    }

    /**
     * Erstellt ein SideNavItem mit Debouncing-Schutz gegen mehrfaches Klicken.
     */
    private SideNavItem createDebouncedSideNavItem(String label, Class<? extends com.vaadin.flow.component.Component> navigationTarget, Icon icon) {
        SideNavItem item = new SideNavItem(label, navigationTarget, icon);

        // Füge JavaScript-basiertes Debouncing hinzu
        item.getElement().executeJs(
            "let lastClick = 0;" +
            "this.addEventListener('click', function(e) {" +
            "  const now = Date.now();" +
            "  if (now - lastClick < 500) {" +
            "    e.preventDefault();" +
            "    e.stopPropagation();" +
            "    return false;" +
            "  }" +
            "  lastClick = now;" +
            "}, true);"
        );

        return item;
    }
}

