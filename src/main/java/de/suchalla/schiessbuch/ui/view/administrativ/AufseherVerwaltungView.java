package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        infoIcon.getStyle().set("margin-right", "var(--lumo-space-s)");
        Paragraph beschreibung = new Paragraph(
                "Verwalten Sie die Aufseher Ihres Vereins. Aufseher können Schießnachweis-Einträge signieren und verwalten."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0")
                .set("display", "inline");
        HorizontalLayout infoContent = new HorizontalLayout(infoIcon, beschreibung);
        infoContent.setAlignItems(FlexComponent.Alignment.START);
        infoContent.setSpacing(false);
        infoBox.add(infoContent);
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
