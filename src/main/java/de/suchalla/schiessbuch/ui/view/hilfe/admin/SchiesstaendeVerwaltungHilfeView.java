package de.suchalla.schiessbuch.ui.view.hilfe.admin;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Schießständeverwaltung (Admin).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/admin/schiesstaende-verwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Schießständeverwaltung")
@PermitAll
public class SchiesstaendeVerwaltungHilfeView extends BaseHilfeView {

    public SchiesstaendeVerwaltungHilfeView() {
        super("admin", "schiesstaende-verwaltung", "var(--lumo-error-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Schießstände verwalten", VaadinIcon.CROSSHAIRS);

        Paragraph intro = new Paragraph(
                "Als Administrator können Sie alle Schießstände im System verwalten, neue Stände anlegen " +
                "und Schießstandaufseher zuweisen."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Schießstände im System einsehen"));
        features.add(new ListItem("Neue Schießstände anlegen"));
        features.add(new ListItem("Schießstanddetails bearbeiten"));
        features.add(new ListItem("Schießstandaufseher zuweisen oder ändern"));
        features.add(new ListItem("Schießstände Vereinen zuordnen"));
        features.add(new ListItem("Schießstände löschen"));

        H3 howToTitle = new H3("So legen Sie einen neuen Schießstand an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Schießstände' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Schießstand'"));
        howTo.add(new ListItem("Geben Sie den Namen und die Adresse des Schießstands ein"));
        howTo.add(new ListItem("Geben Sie an, ob der Stand Gewerblich oder Vereinsgebunden sein soll"));
        howTo.add(new ListItem("Weisen Sie dem Schießstand einen Aufseher/Inhaber oder einen Verein zu"));
        howTo.add(new ListItem("Klicken Sie auf 'Schießstand erstellen', um den Vorgang abzuschließen"));

        H3 aufseherTitle = new H3("So weisen Sie einen Schießstandaufseher im Nachhinein zu:");
        aufseherTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList aufseherSteps = new OrderedList();
        aufseherSteps.add(new ListItem("Öffnen Sie die Schießstanddetails"));
        aufseherSteps.add(new ListItem("Klicken Sie auf 'Verein' bzw 'Inhaber'"));
        aufseherSteps.add(new ListItem("Suchen Sie nach dem gewünschten Benutzer/Verein"));
        aufseherSteps.add(new ListItem("Wählen Sie den Benutzer aus"));



        section.add(intro, featuresTitle, features, howToTitle, howTo, aufseherTitle, aufseherSteps);
        addToContent(section);
    }
}
