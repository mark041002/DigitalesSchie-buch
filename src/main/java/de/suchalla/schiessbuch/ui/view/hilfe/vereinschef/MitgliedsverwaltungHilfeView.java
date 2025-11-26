package de.suchalla.schiessbuch.ui.view.hilfe.vereinschef;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Mitgliedsverwaltung (Vereinschef).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/vereinschef/mitgliedsverwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Mitgliedsverwaltung")
@PermitAll
public class MitgliedsverwaltungHilfeView extends BaseHilfeView {

    public MitgliedsverwaltungHilfeView() {
        super("vereinschef", "mitgliedsverwaltung", "var(--lumo-warning-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Mitglieder verwalten", VaadinIcon.USERS);

        Paragraph intro = new Paragraph(
                "Verwalten Sie die Mitglieder Ihres Vereins, ernennen Sie Aufseher und fügen Sie neue Mitglieder hinzu."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Vereinsmitglieder in einer Übersicht sehen"));
        features.add(new ListItem("Durch den Filter und die Reiter über der Filter Leiste können sie nach Mitgliedsstatus (Aktiv,Inaktiv,Alle) und Namen filtern"));
        features.add(new ListItem("Neue Mitglieder dem Verein beitreten lassen"));
        features.add(new ListItem("Aufseher-Rechte an Mitglieder vergeben oder entziehen"));
        features.add(new ListItem("Mitglieder aus dem Verein entfernen"));
        features.add(new ListItem("Kontaktdaten der Mitglieder einsehen"));

        H3 howToTitle = new H3("So fügen Sie ein neues Mitglied hinzu:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Mitgliedsverwaltung' im Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Zur Genehmigung'"));
        howTo.add(new ListItem("Nehmen sie die Beitrittsanfrage an oder lehnen sie diese ab."));
        
        H3 aufseherTitle = new H3("So ernennen Sie einen Aufseher:");
        aufseherTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList aufseherSteps = new OrderedList();
        aufseherSteps.add(new ListItem("Gehen Sie zur Mitgliedsliste"));
        aufseherSteps.add(new ListItem("Suchen Sie das gewünschte Mitglied"));
        aufseherSteps.add(new ListItem("Klicken sie auf den Knopf 'Zu Aufseher ernennen'"));
        
        H3 removeTitle = new H3("So entfernen Sie ein Mitglied:");
        removeTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList removeSteps = new OrderedList();
        removeSteps.add(new ListItem("Gehen Sie zur Mitgliedsliste"));
        removeSteps.add(new ListItem("Suchen Sie das zu entfernende Mitglied"));
        removeSteps.add(new ListItem("Klicken Sie auf 'Entfernen'"));

        
        Div tippBox = createTippBox(
                "Tipp: Sie selbst sind ebenfalls als Aufseher tätig."
        );

        section.add(intro, featuresTitle, features, howToTitle, howTo, aufseherTitle, aufseherSteps,
                   removeTitle, removeSteps, tippBox);
        addToContent(section);
    }
}
