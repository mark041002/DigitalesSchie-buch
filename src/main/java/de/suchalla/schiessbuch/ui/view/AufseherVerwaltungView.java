package de.suchalla.schiessbuch.ui.view;import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
    }

    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        // Text-Container
        Div textContainer = new Div();

        H2 title = new H2("Aufseherverwaltung");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("Hier können Vereins-Admins Aufseher ernennen oder entziehen");
        subtitle.addClassName("subtitle");

        textContainer.add(title, subtitle);
        header.add(textContainer);
        contentWrapper.add(header);

        // Info-Container
        Div infoContainer = new Div();
        infoContainer.addClassName("form-container");
        infoContainer.setWidthFull();
        infoContainer.add("Diese Funktion ist in Entwicklung.");

        contentWrapper.add(infoContainer);
        add(contentWrapper);
    }
}

