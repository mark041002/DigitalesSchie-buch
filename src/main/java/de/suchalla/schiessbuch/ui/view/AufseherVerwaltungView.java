package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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
        add(new H2("Aufseherverwaltung"));
        add("Hier können Vereins-Admins Aufseher ernennen oder entziehen.");
    }
}

