package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;

/**
 * Platzhalter-View für Aufseherverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "vereins/aufseher", layout = MainLayout.class)
@PageTitle("Aufseher | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "ADMIN"})
public class AufseherVerwaltungView extends VerticalLayout {

    public AufseherVerwaltungView() {
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
    }

    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = ViewComponentHelper.createContentWrapper();

        // Header-Bereich mit Untertitel
        Div header = ViewComponentHelper.createGradientHeader(
                "Aufseherverwaltung",
                "Hier können Vereins-Admins Aufseher ernennen oder entziehen"
        );
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = ViewComponentHelper.createInfoBox(
                "Hier können Sie Aufseher für Ihren Schießstand verwalten."
        );
        contentWrapper.add(infoBox);

        // Info-Container
        Div infoContainer = new Div();
        infoContainer.addClassName("form-container");
        infoContainer.setWidthFull();
        infoContainer.add("Diese Funktion ist in Entwicklung.");

        contentWrapper.add(infoContainer);
        add(contentWrapper);
    }
}
