package de.suchalla.schiessbuch.ui.view.hilfe.schuetze;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Meine Vereine (Schütze).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze/meine-vereine", layout = MainLayout.class)
@PageTitle("Hilfe: Meine Vereine")
@PermitAll
public class MeineVereineHilfeView extends BaseHilfeView {

    public MeineVereineHilfeView() {
        super("schuetze", "meine-vereine", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Meine Vereinsmitgliedschaften", VaadinIcon.GROUP);

        Paragraph intro = new Paragraph(
                "Hier sehen Sie alle Vereine, in denen Sie Mitglied sind, und können Details zu diesen Vereinen einsehen."
        );

        H3 featuresTitle = new H3("Was Sie hier finden:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Liste aller Ihrer Vereinsmitgliedschaften"));
        features.add(new ListItem("Vereinsdetails (Name, Adresse, Kontaktdaten)"));
        features.add(new ListItem("Ihre Rolle im Verein (Mitglied, Aufseher, Vereinschef)"));
        features.add(new ListItem("Beitrittsdatum und Status Ihrer Mitgliedschaft"));

        H3 howToTitle = new H3("So nutzen Sie die Vereinsübersicht:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Meine Vereine' im Menü"));
        howTo.add(new ListItem("Sehen Sie alle Ihre Vereinsmitgliedschaften auf einen Blick"));
        howTo.add(new ListItem("Klicken sie auf 'Einen Verein beitreten' oben rechts, um einem neuen Verein beizutreten"));

        H3 rolesTitle = new H3("Ihre Rollen im Verein:");
        rolesTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList roles = new UnorderedList();
        roles.add(new ListItem("Schütze: Standard-Rolle, Sie können Einträge für diesen Verein erstellen"));
        roles.add(new ListItem("Aufseher: Sie können Einträge anderer Mitglieder bestätigen (wird vom Vereinschef vergeben)"));
        roles.add(new ListItem("Vereinschef: Sie verwalten den Verein und seine Mitglieder (wird vom Admin vergeben)"));

        Div tippBox = createTippBox(
            "Hinweis: Um einem neuen Verein beizutreten, muss ihr Vereinschef sie aktzeptieren. Nutzen Sie die Suchfunktion 'Einem Verein beitreten', um Ihren Verein per Namen oder Adresse zu finden."
        );


        section.add(intro, featuresTitle, features, howToTitle, howTo, rolesTitle, roles, tippBox);
        addToContent(section);
    }
}
