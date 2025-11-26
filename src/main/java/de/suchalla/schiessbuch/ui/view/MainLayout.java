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
import de.suchalla.schiessbuch.ui.view.administrativ.*;
import de.suchalla.schiessbuch.ui.view.oeffentlich.ZertifikatVerifizierungView;
import de.suchalla.schiessbuch.ui.view.organisatorisch.*;
import de.suchalla.schiessbuch.ui.view.persoenlich.*;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Span;
// removed duplicate explicit import

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
@CssImport("./themes/sidebar.css")
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
        private final Benutzer currentUser;
        // Buttons for updating username dynamically
        private Button profilButtonDesktop;
        private Button profilButtonMobile;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        this.currentUser = securityService.getAuthenticatedUser();

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

        // Hilfe-Button (nur Desktop)
        Button hilfeButton = new Button("Hilfe", VaadinIcon.QUESTION_CIRCLE.create());
        hilfeButton.addClickListener(e -> {
            // Get current route to pass as context
            getUI().ifPresent(ui -> {
                String currentRoute = ui.getInternals().getActiveViewLocation().getPath();
                ui.navigate("hilfe?from=" + currentRoute);
            });
        });
        hilfeButton.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("font-weight", "500");
        hilfeButton.addClassName("header-desktop-only");

        // Profil-Button (nur Desktop, rechts)
        profilButtonDesktop = new Button(username, VaadinIcon.USER.create());
        profilButtonDesktop.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(ProfilView.class))
        );
        profilButtonDesktop.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("font-weight", "500");
        profilButtonDesktop.addClassName("header-desktop-only");

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

        HorizontalLayout rightButtons = new HorizontalLayout(hilfeButton, profilButtonDesktop, logoutButton);
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
        drawerLayout.addClassName("responsive-drawer");

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
            drawerLayout.add(persoenlichDetails);

            // Vereinsfunktionen (für AUFSEHER, VEREINS_CHEF und ADMIN mit entsprechenden Mitgliedschaften)
            // WICHTIG: currentUser muss aus SecurityService geladen werden, nicht aus Session
            // damit Änderungen an Mitgliedschaften sofort sichtbar werden
            Benutzer aktuellerBenutzer = securityService.getAuthenticatedUser();
            boolean istAufseherOderChef = aktuellerBenutzer != null && 
                    aktuellerBenutzer.getVereinsmitgliedschaften() != null &&
                    aktuellerBenutzer.getVereinsmitgliedschaften().stream()
                            .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()) ||
                                    Boolean.TRUE.equals(m.getIstVereinschef()));

            boolean istSchiesstandAufseher = aktuellerBenutzer != null && 
                    aktuellerBenutzer.getRolle() == de.suchalla.schiessbuch.model.enums.BenutzerRolle.SCHIESSSTAND_AUFSEHER;
            
            // Zeige Organisatorisches für:
            // - Aufseher/Vereinschef (nicht Schießstandaufseher) ODER
            // - Admin mit Aufseher/Vereinschef-Mitgliedschaften
            boolean zeigeOrganisatorisches = istAufseherOderChef && !istSchiesstandAufseher;

            if (zeigeOrganisatorisches) {
                SideNav vereinNav = new SideNav();
                vereinNav.setWidthFull();
                vereinNav.getStyle()
                        .set("white-space", "normal")
                        .set("word-wrap", "break-word");

                // Eintragsverwaltung für Aufseher, Vereinschefs und Admins
                vereinNav.addItem(createDebouncedSideNavItem(
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
                    vereinNav.addItem(new SideNavItem("Verbände", de.suchalla.schiessbuch.ui.view.organisatorisch.VerbaendeView.class,
                            VaadinIcon.GLOBE.create()));
                }

                Details vereinDetails = createModernDetailsSection("Organisatorisches", vereinNav, VaadinIcon.BRIEFCASE, true, false);
                drawerLayout.add(vereinDetails);
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
                drawerLayout.add(adminDetails);
            }

            // Schießstandaufseher-Funktionen
            if (currentUser.getRolle() == de.suchalla.schiessbuch.model.enums.BenutzerRolle.SCHIESSSTAND_AUFSEHER) {
                SideNav schiesstandNav = new SideNav();
                schiesstandNav.setWidthFull();
                schiesstandNav.getStyle()
                        .set("white-space", "normal")
                        .set("word-wrap", "break-word");

                // Eintragsverwaltung für Schießstandaufseher
                schiesstandNav.addItem(createDebouncedSideNavItem(
                        VaadinIcon.RECORDS.create()));

                // Schießstanddetails
                schiesstandNav.addItem(new SideNavItem("Schießstanddetails", SchiesstandDetailsView.class, VaadinIcon.COG.create()));

                // Zertifikate
                schiesstandNav.addItem(new SideNavItem("Meine Zertifikate", ZertifikateView.class,
                        VaadinIcon.DIPLOMA.create()));

                Details schiesstandDetails = createModernDetailsSection("Organisatorisches", schiesstandNav, VaadinIcon.BRIEFCASE, true, false);
                drawerLayout.add(schiesstandDetails);
            }
        }

        // Öffentliche Funktionen
        SideNav oeffentlichNav = new SideNav();
        oeffentlichNav.setWidthFull();
        oeffentlichNav.getStyle()
                .set("white-space", "normal")
                .set("word-wrap", "break-word");
        oeffentlichNav.addItem(new SideNavItem("Zertifikat verifizieren", ZertifikatVerifizierungView.class, VaadinIcon.CHECK_CIRCLE.create()));
        oeffentlichNav.addItem(new SideNavItem("Verbände", de.suchalla.schiessbuch.ui.view.oeffentlich.VerbaendeView.class, VaadinIcon.GLOBE.create()));

        Details oeffentlichDetails = createModernDetailsSection("Öffentlich", oeffentlichNav, VaadinIcon.GLOBE_WIRE, false, false);
        drawerLayout.add(oeffentlichDetails);

        // User-Aktionen in Navigation (nur Mobile) - wird als normale Sektion angezeigt
        if (currentUser != null) {
            VerticalLayout mobileUserSection = createMobileUserSection();
            drawerLayout.add(mobileUserSection);
        }

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

        // Hilfe-Button für Mobile
        Button hilfeButtonMobile = new Button("Hilfe", VaadinIcon.QUESTION_CIRCLE.create());
        hilfeButtonMobile.addClickListener(e -> {
            getUI().ifPresent(ui -> {
                String currentRoute = ui.getInternals().getActiveViewLocation().getPath();
                ui.navigate("hilfe?from=" + currentRoute);
            });
        });
        hilfeButtonMobile.setWidthFull();
        hilfeButtonMobile.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("justify-content", "flex-start")
                .set("margin-bottom", "var(--lumo-space-xs)");

        // Profil-Button für Mobile
        profilButtonMobile = new Button(username, VaadinIcon.USER.create());
        profilButtonMobile.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate(ProfilView.class))
        );
        profilButtonMobile.setWidthFull();
        profilButtonMobile.getStyle()
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

                                                                mobileSection.add(hilfeButtonMobile, profilButtonMobile, logoutButton);
        return mobileSection;
    }

        /**
         * Aktualisiert den angezeigten Benutzernamen in Header/Drawer (Desktop + Mobile).
         * @param neuerName Neuer anzuzeigender Name
         */
        public void updateUsername(String neuerName) {
                if (profilButtonDesktop != null) {
                        profilButtonDesktop.setText(neuerName);
                }
                if (profilButtonMobile != null) {
                        profilButtonMobile.setText(neuerName);
                }
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

        // Hover-Effekt: entfernt, JS-Datei gelöscht

        return details;
    }

    /**
     * Erstellt ein SideNavItem mit Debouncing-Schutz gegen mehrfaches Klicken.
     */
    private SideNavItem createDebouncedSideNavItem(Icon icon) {
        SideNavItem item = new SideNavItem("Eintragsverwaltung", EintraegeVerwaltungView.class, icon);

        // Debouncing: entfernt, JS-Datei gelöscht

        return item;
    }
}
