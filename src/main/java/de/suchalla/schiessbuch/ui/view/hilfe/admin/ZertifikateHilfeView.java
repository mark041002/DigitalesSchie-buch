package de.suchalla.schiessbuch.ui.view.hilfe.admin;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Zertifikate (Admin) - delegiert Inhalt vom Aufseher-Help
 */
@Route(value = "hilfe/admin/zertifikate", layout = MainLayout.class)
@PageTitle("Hilfe: Zertifikate")
@PermitAll
public class ZertifikateHilfeView extends BaseHilfeView {

    public ZertifikateHilfeView() {
        super("admin", "zertifikate", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Zertifikate verwalten", VaadinIcon.DIPLOMA);

        Paragraph intro = new Paragraph(
                "Als Aufseher können Sie hier die Zertifikate der Aufseher im Verein einsehen " +
                "Dabei hat jeder Aufseher und Verein ein eigenes Zertifikat."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Zertifikate inklusive Details einsehen und in dem Reiter nach Gültigen/Widerrufen Zertifikaten filtern"));
        features.add(new ListItem("Als Vereinschef können sie hier auch Zertifikate widerrufen, was allerdings durch das ernennen/entfernen von Aufsehern automatisch passiert"));
        features.add(new ListItem("Als Admin können sie hier jedes Zertifikat des Systems einsehen"));

        section.add(intro, featuresTitle, features);
        addToContent(section);
    }
}
