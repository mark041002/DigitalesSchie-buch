package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.security.SecurityService;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.details.Details;

/**
 * Haupt-Layout der Anwendung mit Navigation.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@PermitAll
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
     * Erstellt den Header mit Titel und Logout-Button.
     */
    private void createHeader() {
        H1 logo = new H1("Digitales Schießbuch");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.MEDIUM);
        logo.getStyle()
                .set("font-size", "clamp(1.2rem, 4vw, 1.75rem)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis");

        String username = currentUser != null ? currentUser.getVollstaendigerName() : "Gast";
        Button logout = new Button("Abmelden (" + username + ")", e -> securityService.logout());
        logout.setIcon(VaadinIcon.SIGN_OUT.create());
        logout.getStyle()
                .set("font-size", "clamp(0.8rem, 2vw, 1rem)")
                .set("white-space", "nowrap");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);
        header.getStyle()
                .set("flex-wrap", "nowrap")
                .set("gap", "var(--lumo-space-s)");

        addToNavbar(header);
    }

    /**
     * Erstellt die Navigation im Drawer basierend auf der Benutzerrolle.
     */
    private void createDrawer() {
        VerticalLayout drawerLayout = new VerticalLayout();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);

        // Dashboard für alle
        SideNav mainNav = new SideNav();
        mainNav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        drawerLayout.add(mainNav);

        if (currentUser != null) {
            // Block für persönliche Funktionen (zuklappbar)
            SideNav persoenlichNav = new SideNav();
            persoenlichNav.addItem(new SideNavItem("Meine Einträge", MeineEintraegeView.class, VaadinIcon.BOOK.create()));
            persoenlichNav.addItem(new SideNavItem("Neuer Eintrag", NeuerEintragView.class, VaadinIcon.PLUS.create()));
            persoenlichNav.addItem(new SideNavItem("Meine Vereine", MeineVereineView.class, VaadinIcon.GROUP.create()));
            persoenlichNav.addItem(new SideNavItem("Profil", ProfilView.class, VaadinIcon.USER.create()));
            persoenlichNav.addItem(new SideNavItem("Benachrichtigungen", BenachrichtigungenView.class,
                    VaadinIcon.BELL.create()));
            persoenlichNav.getStyle().set("padding-left", "var(--lumo-space-m)");

            Details persoenlichDetails = new Details("Persönlich", persoenlichNav);
            persoenlichDetails.setOpened(true);
            persoenlichDetails.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

            drawerLayout.add(persoenlichDetails);

            // Block für Vereinsfunktionen (Aufseher/Vereinschef)
            boolean istAufseherOderChef = currentUser.getVereinsmitgliedschaften().stream()
                    .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()) ||
                                   Boolean.TRUE.equals(m.getIstVereinschef()));

            if (istAufseherOderChef) {
                SideNav vereinNav = new SideNav();

                // Aufseher-Funktionen
                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()))) {
                    vereinNav.addItem(new SideNavItem("Einträge signieren", EintraegeSignierenView.class,
                            VaadinIcon.EDIT.create()));
                }

                // Vereinschef-Funktionen
                if (currentUser.getVereinsmitgliedschaften().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getIstVereinschef()))) {
                    vereinNav.addItem(new SideNavItem("Vereinsdetails", VereinDetailsView.class,
                            VaadinIcon.COG.create()));
                    vereinNav.addItem(new SideNavItem("Beitrittsanfragen", BeitrittsanfragenView.class,
                            VaadinIcon.ENVELOPE.create()));
                    vereinNav.addItem(new SideNavItem("Einträgsverwaltung", EintraegeVerwaltungView.class,
                            VaadinIcon.RECORDS.create()));
                    vereinNav.addItem(new SideNavItem("Mitgliedschaften", MitgliedschaftenVerwaltenView.class,
                            VaadinIcon.USERS.create()));
                }

                vereinNav.getStyle().set("padding-left", "var(--lumo-space-m)");

                Details vereinDetails = new Details("Organisatorisches", vereinNav);
                vereinDetails.setOpened(true);
                vereinDetails.getStyle()
                        .set("margin-top", "var(--lumo-space-s)")
                        .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                drawerLayout.add(vereinDetails);
            }

            // Admin-Funktionen
            if (currentUser.istAdmin()) {
                SideNav adminNav = new SideNav();
                adminNav.addItem(new SideNavItem("Verbände", VerbaendeVerwaltungView.class, VaadinIcon.GLOBE.create()));
                adminNav.addItem(new SideNavItem("Vereine", VereineVerwaltungView.class, VaadinIcon.BUILDING.create()));
                adminNav.addItem(new SideNavItem("Schießstände", SchiesstaendeVerwaltungView.class,
                        VaadinIcon.CROSSHAIRS.create()));
                adminNav.addItem(new SideNavItem("Disziplinen", DisziplinenVerwaltungView.class,
                        VaadinIcon.MEDAL.create()));
                adminNav.addItem(new SideNavItem("Mitglieder", MitgliederVerwaltungView.class,
                        VaadinIcon.USERS.create()));
                adminNav.getStyle().set("padding-left", "var(--lumo-space-m)");

                Details adminDetails = new Details("Administration", adminNav);
                adminDetails.setOpened(true);
                adminDetails.getStyle()
                        .set("margin-top", "var(--lumo-space-s)")
                        .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                drawerLayout.add(adminDetails);
            }
        }

        addToDrawer(drawerLayout);
    }
}
