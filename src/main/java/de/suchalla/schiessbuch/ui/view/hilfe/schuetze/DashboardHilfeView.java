package de.suchalla.schiessbuch.ui.view.hilfe.schuetze;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Dashboard (Schütze).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze/dashboard", layout = MainLayout.class)
@PageTitle("Hilfe: Dashboard")
@PermitAll
public class DashboardHilfeView extends BaseHilfeView {

    public DashboardHilfeView() {
        super("schuetze", "dashboard", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Dashboard-Übersicht", VaadinIcon.HOME);

        Paragraph intro = new Paragraph(
                "Das Dashboard ist Ihre Startseite und bietet Ihnen einen schnellen Überblick/Zugriff auf wichtige Funktionen."
        );

        H3 featuresTitle = new H3("Was Sie hier finden:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Übersicht unsignierter/offener Schießeinträge"));
        features.add(new ListItem("Schnellzugriff auf häufig genutzte Funktionen"));
        features.add(new ListItem("Informationen zu Ihrer Rolle"));
        features.add(new ListItem("Für Aufseher und Vereinschefs zusätzliche Übersichten über Offene Einträge bzw Beitrittsanfragen"));

        H3 howToTitle = new H3("So nutzen Sie das Dashboard:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Nach dem Login gelangen Sie automatisch zum Dashboard"));
        howTo.add(new ListItem("Sie können auch jederzeit über das Menü links zu 'Dashboard' navigieren"));
        howTo.add(new ListItem("Nutzen Sie die Statistiken, um direkt auf die entsprechenden Bereiche zuzugreifen"));
        howTo.add(new ListItem("Nutzen sie die Schnellzugriff Funktionen, um direkt auf die wichtigsten Seiten/Aktinen weitergeleitet zu werden"));
        howTo.add(new ListItem("Über die Seitliche Leiste können sie auf andere Views navigieren"));


        section.add(intro, featuresTitle, features, howToTitle, howTo);
        addToContent(section);
    }
}
