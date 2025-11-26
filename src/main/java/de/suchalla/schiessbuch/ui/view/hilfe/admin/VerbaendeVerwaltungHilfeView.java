package de.suchalla.schiessbuch.ui.view.hilfe.admin;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Verbänderverwaltung (Admin).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/admin/verbaende-verwaltung", layout = MainLayout.class)
@PageTitle("Hilfe: Verbänderverwaltung")
@PermitAll
public class VerbaendeVerwaltungHilfeView extends BaseHilfeView {

    public VerbaendeVerwaltungHilfeView() {
        super("admin", "verbaende-verwaltung", "var(--lumo-error-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Verbände verwalten", VaadinIcon.GLOBE);

        Paragraph intro = new Paragraph(
                "Als Administrator können Sie alle Verbände im System verwalten und neue Verbände anlegen "
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Verbände im System einsehen"));
        features.add(new ListItem("Neue Verbände anlegen"));
        features.add(new ListItem("Verbandsdetails einsehen"));
        features.add(new ListItem("Dort können sie neue Disziplinen für den Verband anlegen oder bestehende löschen"));
        features.add(new ListItem("Verbände löschen"));

        H3 howToTitle = new H3("So legen Sie neue Disziplinen für einen Verband an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Verbände' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Details' bei dem gewünschten Verband"));
        howTo.add(new ListItem("Nutzen sie die Manuelle Eingabe oder den CSV-Import um neue Disziplinen hinzuzufügen"));
        howTo.add(new ListItem("Füllen sie die erforderlichen Felder aus bzw. formatieren sie die CSV-Datei korrekt"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));

       

        section.add(intro, featuresTitle, features, howToTitle, howTo);
        addToContent(section);
    }
}
