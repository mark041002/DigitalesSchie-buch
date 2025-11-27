package de.suchalla.schiessbuch.ui.view.hilfe.schuetze;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Meine Einträge (Schütze).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze/meine-eintraege", layout = MainLayout.class)
@PageTitle("Hilfe: Meine Einträge")
@PermitAll
public class MeineEintraegeHilfeView extends BaseHilfeView {

    public MeineEintraegeHilfeView() {
        super("schuetze", "meine-eintraege", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Meine Einträge verwalten", VaadinIcon.BOOK);

        Paragraph intro = new Paragraph(
                "Hier können Sie alle Ihre Schießeinträge einsehen, filtern und exportieren. " +
                "Sie haben einen vollständigen Überblick über Ihre gesamte Schießhistorie."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Ihre Schießeinträge in einer Tabelle anzeigen"));
        features.add(new ListItem("Nach Datum, Verein, Schießstand oder Disziplin filtern über die Filter-Leiste"));
        features.add(new ListItem("Einträge in der Tabelle durch das Klicken der Kopfzeile sortieren (z.B. nach Datum, Treffer, Status)"));
        features.add(new ListItem("Status der Einträge einsehen (bestätigt, unbestätigt, abgelehnt)"));
        features.add(new ListItem("Einträge als PDF oder Excel exportieren"));
        features.add(new ListItem("Unbestätigte Einträge löschen"));

        H3 howToTitle = new H3("So verwalten Sie Ihre Einträge:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Meine Einträge' im Menü"));
        howTo.add(new ListItem("Die Tabelle zeigt automatisch alle Ihre Einträge"));
        howTo.add(new ListItem("Nutzen Sie die Filter-Leiste oben, um bestimmte Einträge zu finden"));
        howTo.add(new ListItem("Über der Filter-Leiste sind auch Reiter um nach bestimmten Statussen zu filtern (Alle,Unsigniert/Abgelehnt,Signiert"));
        howTo.add(new ListItem("Klicken Sie auf eine Spaltenüberschrift, um nach dieser Spalte zu sortieren"));
        howTo.add(new ListItem("Verwenden Sie die Export-Buttons, um Ihre Daten herunterzuladen"));

        H3 statusTitle = new H3("Status-Bedeutungen:");
        statusTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList statusList = new UnorderedList();
        statusList.add(new ListItem("Unbestätigt (gelb): Eintrag wartet auf Bestätigung durch einen Aufseher"));
        statusList.add(new ListItem("Bestätigt (grün): Eintrag wurde von einem Aufseher geprüft und bestätigt"));
        statusList.add(new ListItem("Abgelehnt (rot): Eintrag wurde abgelehnt, sehen Sie sich den Ablehnungsgrund an"));

        Div warningBox = createWarningBox(
                "Wichtig: Einmal bestätigte Einträge können nicht mehr bearbeitet oder gelöscht werden. " +
                "Prüfen Sie Ihre Angaben sorgfältig, bevor Sie einen Eintrag einreichen."
        );

        Div tippBox = createTippBox(
                "Tipp: Nutzen Sie die Export-Funktion, um Ihre Schießeinträge zu auszudrucken. "
        );


        section.add(intro, featuresTitle, features, howToTitle, howTo, statusTitle, statusList,
                   warningBox, tippBox);
        addToContent(section);
    }
}
