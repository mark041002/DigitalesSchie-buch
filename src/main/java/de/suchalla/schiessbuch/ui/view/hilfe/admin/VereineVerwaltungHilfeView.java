package de.suchalla.schiessbuch.ui.view.hilfe.admin;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Vereinsverwaltung (Admin).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/admin/vereine-verwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Vereinsverwaltung")
@PermitAll
public class VereineVerwaltungHilfeView extends BaseHilfeView {

    public VereineVerwaltungHilfeView() {
        super("admin", "vereine-verwaltung", "var(--lumo-error-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Vereine verwalten", VaadinIcon.BUILDING);

        Paragraph intro = new Paragraph(
                "Als Administrator können Sie alle Vereine im System verwalten, neue Vereine anlegen " +
                "und Vereinschefs zuweisen."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Vereine im System einsehen"));
        features.add(new ListItem("Neue Vereine anlegen"));
        features.add(new ListItem("Vereinsdetails bearbeiten (Name, Adresse, Verbandszugehörigkeit, Vereinschef)"));
        features.add(new ListItem("Vereinsmitglieder einsehen"));
        features.add(new ListItem("Den Verein Löschen"));

        H3 howToTitle = new H3("So legen Sie einen neuen Verein an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Vereine' im Admin-Menü"));
        howTo.add(new ListItem("Füllen sie die erforderlichen Felder im Formular aus"));
        howTo.add(new ListItem("Optional: Weisen Sie einen Vereinschef zu"));
        howTo.add(new ListItem("Optional: Ordnen Sie den Verein einem Verband zu"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));

        H3 vereinschefTitle = new H3("So weisen Sie einen Vereinschef zu:");
        vereinschefTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList vereinschefSteps = new OrderedList();
        vereinschefSteps.add(new ListItem("Suchen Sie den gewünschten Verein in der Vereinsliste"));
        vereinschefSteps.add(new ListItem("Klicken Sie auf 'Details'"));
        vereinschefSteps.add(new ListItem("Klicken Sie auf 'Vereinschef'"));
        vereinschefSteps.add(new ListItem("Suchen Sie nach dem gewünschten Benutzer per Dropdown Liste oder geben sie einfach den Namen ein"));
        vereinschefSteps.add(new ListItem("Wählen Sie den Benutzer aus"));
        vereinschefSteps.add(new ListItem("Klicken Sie auf 'Speichern'"));


        section.add(intro, featuresTitle, features, howToTitle, howTo, vereinschefTitle, vereinschefSteps);
        addToContent(section);
    }
}
