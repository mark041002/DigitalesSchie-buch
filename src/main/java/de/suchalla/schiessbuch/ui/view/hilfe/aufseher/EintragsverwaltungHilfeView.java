package de.suchalla.schiessbuch.ui.view.hilfe.aufseher;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Eintragsverwaltung (Aufseher).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/aufseher/eintragsverwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Eintragsverwaltung")
@PermitAll
public class EintragsverwaltungHilfeView extends BaseHilfeView {

    public EintragsverwaltungHilfeView() {
        super("aufseher", "eintragsverwaltung", "var(--lumo-success-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Einträge verwalten und bestätigen", VaadinIcon.RECORDS);

        Paragraph intro = new Paragraph(
                "Als Aufseher sind Sie dafür verantwortlich, Schießeinträge zu überprüfen und zu bestätigen. "
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Einträge Ihres Vereins/Schießstands einsehen"));
        features.add(new ListItem("Unbestätigte Einträge prüfen und bearbeiten"));
        features.add(new ListItem("Einträge bestätigen oder ablehnen"));
        features.add(new ListItem("Nach verschiedenen Kriterien filtern (Datum, Schütze, Aufsheher)"));
        features.add(new ListItem("Mit den Reitern über der Status Leiste können sie ebenfalls nach Status filtern (Unsigniert,Signiert,Abgelehnt,Alle)"));
        features.add(new ListItem("Einträge zum Nachweis als PDF exportieren"));

        H3 howToTitle = new H3("So bestätigen Sie einen Eintrag:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Eintragsverwaltung' im Menü"));
        howTo.add(new ListItem("Sie sehen eine Übersicht aller unsignierter Einträge (sofern im richtigen Reiter)"));
        howTo.add(new ListItem("Prüfen Sie alle Angaben sorgfältig: Datum, Disziplin, Anzahl Schuss, Ergebnis"));
        howTo.add(new ListItem("Wenn alles korrekt ist, klicken Sie auf 'Bestätigen' oder auf 'Ablehnen', wenn der Eintrag nicht korrekt ist"));
        howTo.add(new ListItem("Der Schütze wird automatisch über die Bestätigung informiert"));

        Div warningBox = createWarningBox(
                "Wichtig: Einmal bestätigte Einträge können NICHT mehr geändert oder gelöscht werden! " +
                "Prüfen Sie alle Angaben sehr sorgfältig, bevor Sie einen Eintrag bestätigen."
        );



        section.add(intro, featuresTitle, features, howToTitle, howTo, warningBox);
        addToContent(section);
    }
}
