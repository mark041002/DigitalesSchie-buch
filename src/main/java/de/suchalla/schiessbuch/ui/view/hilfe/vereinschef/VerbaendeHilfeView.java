package de.suchalla.schiessbuch.ui.view.hilfe.vereinschef;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Verbände (Vereinschef).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/vereinschef/verbaende", layout = MainLayout.class)
@PageTitle("Hilfe: Verbände")
@PermitAll
public class VerbaendeHilfeView extends BaseHilfeView {

    public VerbaendeHilfeView() {
        super("vereinschef", "verbaende", "var(--lumo-warning-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Verbandszugehörigkeit verwalten", VaadinIcon.GLOBE);

        Paragraph intro = new Paragraph(
                "Verwalten Sie die Verbandszugehörigkeit Ihres Vereins."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle verfügbaren Verbände einsehen"));
        features.add(new ListItem("Disziplinen der Verbände anzeigen lassen"));
        features.add(new ListItem("Verbänd per Knopfdruck beitreten/Verlassen"));

        Div tippBox = createTippBox(
                "Tipp: Die Mitgliedschaft in einem Verband ermöglicht allen Schützen Ihres Vereins, " +
                "an den Disziplinen dieses Verbands teilzunehmen beziehungsweise diese beim Schießnachweiseintrag auszuwählen."
        );

        Div imagePlaceholder = createImagePlaceholder(
                "verbaende-vereinschef.png",
                "Verbände-Übersicht mit Beitrittsantrag-Option"
        );

        section.add(intro, featuresTitle, features, tippBox, imagePlaceholder);
        addToContent(section);
    }
}
