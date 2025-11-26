package de.suchalla.schiessbuch.ui.view.hilfe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;

/**
 * Basis-Klasse für alle Hilfe-Views mit standardisierter Navigation.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public abstract class BaseHilfeView extends VerticalLayout {

    protected final String role;
    protected final String currentPage;
    private final VerticalLayout contentWrapper;

    public BaseHilfeView(String role, String currentPage, String titleColor) {
        this.role = role;
        this.currentPage = currentPage;

        setSpacing(false);
        setPadding(false);
        addClassName("content-wrapper");

        // Create navigation at the top
        createTopNavigation();

        // Create header
        Div header = ViewComponentHelper.createGradientHeader(
                "Hilfe: " + HilfeNavigation.getPageTitle(currentPage),
                getRoleDisplayName() + " / " + HilfeNavigation.getPageTitle(currentPage)
        );
        super.add(header);

        // Content wrapper
        contentWrapper = ViewComponentHelper.createContentWrapper();
        contentWrapper.getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("gap", "var(--lumo-space-l)");
        super.add(contentWrapper);

        createContent();
    }

    private void createTopNavigation() {
        HorizontalLayout navigationBar = new HorizontalLayout();
        navigationBar.setWidthFull();
        navigationBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navigationBar.setAlignItems(FlexComponent.Alignment.CENTER);
        navigationBar.getStyle()
                .set("padding", "var(--lumo-space-m) var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("gap", "var(--lumo-space-m)");

        String previousPage = HilfeNavigation.getPreviousPage(role, currentPage);
        String nextPage = HilfeNavigation.getNextPage(role, currentPage);
        // Build three areas: left arrow (icons only), centered overview, right arrow (icons only)
        HorizontalLayout leftArrow = new HorizontalLayout();
        leftArrow.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArrow.getStyle().set("gap", "var(--lumo-space-s)");

        // Center area: Übersicht (zentral)
        HorizontalLayout centerArea = new HorizontalLayout();
        centerArea.setWidthFull();
        centerArea.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        centerArea.setAlignItems(FlexComponent.Alignment.CENTER);

        // Right area: next icon
        HorizontalLayout rightArrow = new HorizontalLayout();
        rightArrow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Übersicht (zentrales Element)
        Button backButton = new Button("Übersicht", VaadinIcon.HOME.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backButton.getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("hilfe")));
        centerArea.add(backButton);

        // Previous (links) - Icon + Seitentitel
        if (previousPage != null) {
            Button prevButton = new Button(HilfeNavigation.getPageTitle(previousPage), VaadinIcon.ARROW_LEFT.create());
            prevButton.getElement().setProperty("title", HilfeNavigation.getPageTitle(previousPage));
            prevButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            prevButton.getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("min-width", "88px");
            // Icon should appear before the text (default)
            prevButton.setIconAfterText(false);
            prevButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(HilfeNavigation.getPageRoute(role, previousPage))));
            leftArrow.add(prevButton);
        }

        // Next (rechts) - Seitentitel + Icon
        if (nextPage != null) {
            Button nextButton = new Button(HilfeNavigation.getPageTitle(nextPage), VaadinIcon.ARROW_RIGHT.create());
            nextButton.getElement().setProperty("title", HilfeNavigation.getPageTitle(nextPage));
            nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            nextButton.getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("min-width", "88px");
            // Icon should appear after the text
            nextButton.setIconAfterText(true);
            nextButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(HilfeNavigation.getPageRoute(role, nextPage))));
            rightArrow.add(nextButton);
        }

        navigationBar.add(leftArrow, centerArea, rightArrow);
        super.add(navigationBar);
    }

    private String getRoleDisplayName() {
        return switch (role) {
            case "schuetze" -> "Schütze";
            case "aufseher" -> "Aufseher";
            case "schiesstand-aufseher" -> "Schießstandaufseher";
            case "vereinschef" -> "Vereinschef";
            case "admin" -> "Administrator";
            default -> "Hilfe";
        };
    }

    /**
     * Erstellt den Inhalt der Hilfe-Seite. Muss von Subklassen implementiert werden.
     * Inhalt wird automatisch zum contentWrapper hinzugefügt.
     */
    protected abstract void createContent();

    /**
     * Fügt eine Komponente zum Content-Wrapper hinzu.
     */
    protected void addToContent(com.vaadin.flow.component.Component... components) {
        contentWrapper.add(components);
    }

    /**
     * Hilfsmethode zum Erstellen einer weißen Content-Card.
     */
    protected Div createSection(String title, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
            .set("box-shadow", "var(--lumo-box-shadow-s)");
        // Center the section within its parent: keep it responsive with a max width
        card.getStyle()
            .set("margin", "0 auto")
            .set("max-width", "900px");

        // Header with icon and title
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("gap", "var(--lumo-space-s)");

        com.vaadin.flow.component.icon.Icon iconComponent = icon.create();
        iconComponent.setSize("28px");
        iconComponent.getStyle().set("color", "var(--lumo-primary-color)");

        H2 titleElement = new H2(title);
        titleElement.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-size", "var(--lumo-font-size-xl)");

        header.add(iconComponent, titleElement);
        card.add(header);

        return card;
    }

    /**
     * Hilfsmethode zum Erstellen einer Tipp-Box (einheitlich in Primary-Farbe).
     */
    protected Div createTippBox(String text) {
        return ViewComponentHelper.createTippBox(text, "var(--lumo-primary-color)");
    }

    /**
     * Hilfsmethode zum Erstellen einer Warn-Box.
     */
    protected Div createWarningBox(String text) {
        return ViewComponentHelper.createErrorBox(text);
    }

    /**
     * Hilfsmethode zum Erstellen eines Bild-Platzhalters.
     */
    protected Div createImagePlaceholder(String imageName, String description) {
        return ViewComponentHelper.createImagePlaceholder(imageName, description);
    }
}
