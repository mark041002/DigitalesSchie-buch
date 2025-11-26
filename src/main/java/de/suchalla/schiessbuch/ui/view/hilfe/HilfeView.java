package de.suchalla.schiessbuch.ui.view.hilfe;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

/**
 * Haupt-Hilfe-Seite mit Auswahl der verschiedenen Rollen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe", layout = MainLayout.class)
@PageTitle("Hilfe")
@PermitAll
public class HilfeView extends VerticalLayout implements HasUrlParameter<String>, BeforeEnterObserver {

    private String fromRoute;

    public HilfeView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        setWidthFull();
        getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto");

        createContent();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        // This method is required but we'll use query parameters instead
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Get the "from" query parameter
        this.fromRoute = event.getLocation().getQueryParameters()
                .getParameters()
                .getOrDefault("from", java.util.Collections.singletonList(""))
                .get(0);
    }

    private void createContent() {
        // Header
        H1 title = new H1("Hilfe & Anleitung");
        title.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph intro = new Paragraph(
                "Wählen Sie Ihre Rolle aus, um die passende Anleitung zu erhalten:"
        );
        intro.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-xl)")
                .set("text-align", "center")
                .set("font-size", "var(--lumo-font-size-l)");

        // Role buttons in a grid layout
        HorizontalLayout roleButtonsRow1 = new HorizontalLayout();
        roleButtonsRow1.setWidthFull();
        roleButtonsRow1.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        roleButtonsRow1.setSpacing(true);
        roleButtonsRow1.getStyle()
                .set("gap", "var(--lumo-space-l)")
                .set("flex-wrap", "wrap");

        // Schütze Button
        Div schuetzeCard = createRoleCard(
                "Schütze",
                "Anleitung für Schützen",
                "Erfahren Sie, wie Sie Ihre Schießeinträge verwalten, neue Einträge erstellen und Ihre Vereine einsehen können.",
                VaadinIcon.USER,
                "var(--lumo-primary-color)",
                () -> navigateToRoleHelp("schuetze")
        );

        // Aufseher Button
        Div aufseherCard = createRoleCard(
                "Aufseher / Schießstandaufseher",
                "Anleitung für Aufseher",
                "Erfahren Sie, wie Sie Einträge verwalten, Zertifikate ausstellen und Schießstände überwachen können.",
                VaadinIcon.EYE,
                "var(--lumo-success-color)",
                () -> navigateToRoleHelp("aufseher")
        );

        // Vereinschef Button
        Div vereinschefCard = createRoleCard(
                "Vereinschef",
                "Anleitung für Vereinschefs",
                "Erfahren Sie, wie Sie Ihren Verein verwalten, Mitglieder hinzufügen und Zertifikate verwalten können.",
                VaadinIcon.BRIEFCASE,
                "var(--lumo-warning-color)",
                () -> navigateToRoleHelp("vereinschef")
        );

        // Admin Button
        Div adminCard = createRoleCard(
                "Administrator",
                "Anleitung für Administratoren",
                "Erfahren Sie, wie Sie das gesamte System verwalten, Verbände und Vereine anlegen und alle Funktionen nutzen können.",
                VaadinIcon.COG,
                "var(--lumo-error-color)",
                () -> navigateToRoleHelp("admin")
        );

        roleButtonsRow1.add(schuetzeCard, aufseherCard);

        HorizontalLayout roleButtonsRow2 = new HorizontalLayout();
        roleButtonsRow2.setWidthFull();
        roleButtonsRow2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        roleButtonsRow2.setSpacing(true);
        roleButtonsRow2.getStyle()
                .set("gap", "var(--lumo-space-l)")
                .set("flex-wrap", "wrap");

        roleButtonsRow2.add(vereinschefCard, adminCard);

        add(title, intro, roleButtonsRow1, roleButtonsRow2);
    }

    private Div createRoleCard(String title, String subtitle, String description,
                               VaadinIcon icon, String color, Runnable onClick) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("transition", "all 0.3s ease")
                .set("cursor", "pointer")
                .set("width", "280px")
                .set("min-height", "220px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("text-align", "center");

        card.addClickListener(e -> onClick.run());

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-5px)")
                    .set("box-shadow", "var(--lumo-box-shadow-m)")
                    .set("border-color", color);
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "var(--lumo-box-shadow-s)")
                    .set("border-color", "var(--lumo-contrast-10pct)");
        });

        // Icon
        com.vaadin.flow.component.icon.Icon iconComponent = icon.create();
        iconComponent.setSize("48px");
        iconComponent.getStyle()
                .set("color", color)
                .set("margin-bottom", "var(--lumo-space-m)");

        // Title
        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "700")
                .set("color", "var(--lumo-header-text-color)")
                .set("margin-bottom", "var(--lumo-space-xs)");

        // Subtitle
        Div subtitleDiv = new Div();
        subtitleDiv.setText(subtitle);
        subtitleDiv.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("color", color)
                .set("margin-bottom", "var(--lumo-space-s)");

        // Description
        Paragraph desc = new Paragraph(description);
        desc.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0")
                .set("line-height", "1.5");

        card.add(iconComponent, titleDiv, subtitleDiv, desc);
        return card;
    }

    private void navigateToRoleHelp(String role) {
        // Navigate to the first help page for the selected role
        final String targetUrl = switch (role) {
            case "schuetze" -> "hilfe/schuetze/dashboard";
            case "aufseher" -> "hilfe/aufseher/eintragsverwaltung";
            case "schiesstand-aufseher" -> "hilfe/aufseher/eintragsverwaltung";
            case "vereinschef" -> "hilfe/vereinschef/vereinsdetails";
            case "admin" -> "hilfe/admin/verbaende-verwaltung";
            default -> "hilfe";
        };
        getUI().ifPresent(ui -> ui.navigate(targetUrl));
    }
}
