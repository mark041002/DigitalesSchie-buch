package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Platzhalter-View für Mitgliederverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "vereins/mitglieder", layout = MainLayout.class)
@PageTitle("Mitglieder | Digitales Schießbuch")
@RolesAllowed({"VEREINS_ADMIN"})
public class MitgliederVerwaltungView extends VerticalLayout {

    public MitgliederVerwaltungView() {
        add(new H2("Mitgliederverwaltung"));
        add("Hier können Vereins-Admins ihre Mitglieder verwalten.");
    }
}

