package de.suchalla.schiessbuch.ui.view.hilfe.admin;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Mitgliederverwaltung (Admin).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/admin/mitglieder-verwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Mitgliederverwaltung")
@PermitAll
public class MitgliederVerwaltungHilfeView extends BaseHilfeView {

    public MitgliederVerwaltungHilfeView() {
        super("admin", "mitglieder-verwaltung", "var(--lumo-error-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Benutzer verwalten", VaadinIcon.USERS);

        Paragraph intro = new Paragraph(
                "Als Administrator können Sie alle Benutzer im System einsehen und löschen."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Benutzer im System mit Daten einsehen"));
        features.add(new ListItem("Einen Benutzer als Admin ernennen"));

        Div warningBox = createWarningBox(
                "WICHTIG: Die Admin-Rolle hat vollständigen Zugriff auf das gesamte System, " +
                "einschließlich aller Daten und Einstellungen. Vergeben Sie diese Rolle nur an " +
                "absolut vertrauenswürdige Personen!"
        );

        Div imagePlaceholder = createImagePlaceholder(
                "mitglieder-verwaltung-admin.png",
                "Mitgliederverwaltung mit Benutzerübersicht und Rollenzuweisung"
        );

        section.add(intro, featuresTitle, features, warningBox,  imagePlaceholder);
        addToContent(section);
    }
}
