package de.suchalla.schiessbuch.ui.component;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Hilfsklasse für die Erstellung häufig verwendeter UI-Komponenten.
 * Reduziert Code-Duplikation in View-Klassen durch wiederverwendbare Komponenten-Factory-Methoden.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public final class ViewComponentHelper {

    private ViewComponentHelper() {
        // Utility-Klasse - keine Instanziierung
    }

    /**
     * Erstellt einen standardisierten Content-Wrapper für Views.
     * - spacing/padding: false
     * - CSS-Klasse: content-wrapper
     *
     * @return vorkonfigurierter VerticalLayout-Wrapper
     */
    public static VerticalLayout createContentWrapper() {
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");
        return contentWrapper;
    }

    /**
     * Erstellt einen Gradient-Header mit Titel und optionalem Untertitel.
     *
     * @param title Der Haupttitel
     * @param subtitle Der optionale Untertitel (kann null sein)
     * @return Gradient-Header-Komponente
     */
    public static Div createGradientHeader(String title, String subtitle) {
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 titleElement = new H2(title);
        titleElement.getStyle().set("margin", "0");

        header.add(titleElement);

        if (subtitle != null && !subtitle.isEmpty()) {
            Paragraph subtitleElement = new Paragraph(subtitle);
            subtitleElement.addClassName("subtitle");
            header.add(subtitleElement);
        }

        return header;
    }

    /**
     * Erstellt einen Gradient-Header nur mit Titel.
     *
     * @param title Der Haupttitel
     * @return Gradient-Header-Komponente
     */
    public static Div createGradientHeader(String title) {
        return createGradientHeader(title, null);
    }

    /**
     * Erstellt eine Info-Box mit Icon und Text.
     *
     * @param text Der Informationstext
     * @return Info-Box-Komponente
     */
    public static Div createInfoBox(String text) {
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(text);
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        return infoBox;
    }

    /**
     * Erstellt eine Warnung-Box mit Icon und Text.
     *
     * @param text Der Warnungstext
     * @return Warning-Box-Komponente
     */
    public static Div createWarningBox(String text) {
        Div warningBox = new Div();
        warningBox.addClassName("warning-box");
        warningBox.setWidthFull();

        Icon warningIcon = VaadinIcon.WARNING.create();
        warningIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(text);
        beschreibung.getStyle()
                .set("color", "var(--lumo-warning-text-color)")
                .set("margin", "0");

        warningBox.add(warningIcon, beschreibung);
        return warningBox;
    }

    /**
     * Erstellt einen Formular-Container.
     *
     * @return Formular-Container-Komponente
     */
    public static Div createFormContainer() {
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        return formContainer;
    }

    /**
     * Erstellt einen Grid-Container mit responsive Styling.
     *
     * @return Grid-Container-Komponente
     */
    public static Div createGridContainer() {
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("overflow-x", "auto")
                .set("overflow-y", "auto");
        gridContainer.setHeightFull();
        return gridContainer;
    }

    /**
     * Erstellt eine Empty-State-Nachricht mit Standard-Icon (Info).
     *
     * @param message Die anzuzeigende Nachricht
     * @return Empty-State-Komponente
     */
    public static Div createEmptyStateMessage(String message) {
        return createEmptyStateMessage(message, VaadinIcon.INFO_CIRCLE_O);
    }

    /**
     * Erstellt eine Empty-State-Nachricht mit anpassbarem Icon.
     *
     * @param message Die anzuzeigende Nachricht
     * @param iconType Das VaadinIcon für den Empty-State
     * @return Empty-State-Komponente
     */
    public static Div createEmptyStateMessage(String message, VaadinIcon iconType) {
        Div emptyState = new Div();
        emptyState.addClassName("empty-state");
        emptyState.setWidthFull();
        emptyState.setHeightFull();
        emptyState.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "center")
            .set("align-items", "center")
            .set("text-align", "center")
            .set("padding", "var(--lumo-space-xl)")
            // Allow the empty state to expand inside flex containers (fills available grid area)
            .set("flex", "1 1 auto")
            .set("min-height", "300px")
            .set("box-sizing", "border-box")
            .set("color", "var(--lumo-secondary-text-color)");


        Icon icon = iconType.create();
        icon.setSize("64px");
        icon.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-contrast-30pct)");

        Paragraph text = new Paragraph(message);
        text.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("max-width", "600px");

        emptyState.add(icon, text);
        return emptyState;
    }

    /**
     * Erstellt ein responsives FormLayout mit Standard-Breakpoints.
     * - Mobil (0px): 1 Spalte
     * - Desktop (500px+): 2 Spalten
     *
     * @return Responsive FormLayout
     */
    public static FormLayout createResponsiveFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        return formLayout;
    }

    /**
     * Erstellt ein responsives FormLayout mit 3 Spalten auf großen Bildschirmen.
     * - Mobil (0px): 1 Spalte
     * - Tablet (500px+): 2 Spalten
     * - Desktop (900px+): 3 Spalten
     *
     * @return Responsive FormLayout mit 3 Spalten
     */
    public static FormLayout createResponsiveFormLayout3Columns() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );
        return formLayout;
    }

    /**
     * Erstellt einen Abschnitts-Header (H3) für Formulare.
     *
     * @param title Der Titel des Abschnitts
     * @return H3-Element mit Styling
     */
    public static H3 createSectionHeader(String title) {
        H3 header = new H3(title);
        header.getStyle()
                .set("margin-top", "var(--lumo-space-l)")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-primary-text-color)");
        return header;
    }

}
