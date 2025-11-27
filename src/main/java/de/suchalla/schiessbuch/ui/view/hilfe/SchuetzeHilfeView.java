package de.suchalla.schiessbuch.ui.view.hilfe;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Schützen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze", layout = MainLayout.class)
@PageTitle("Hilfe für Schützen")
@PermitAll
public class SchuetzeHilfeView extends VerticalLayout implements BeforeEnterObserver {

    private String fromRoute;

    public SchuetzeHilfeView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.START);
        setWidthFull();
        getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto");

        createContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.fromRoute = event.getLocation().getQueryParameters()
                .getParameters()
                .getOrDefault("from", java.util.Collections.singletonList(""))
                .get(0);

        // Scroll to the relevant section after page load
        if (fromRoute != null && !fromRoute.isEmpty()) {
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => { const element = document.getElementById($0); if(element) element.scrollIntoView({behavior: 'smooth', block: 'start'}); }, 100)",
                    getSectionIdFromRoute(fromRoute)
            );
        }
    }

    private void createContent() {
        // Back button
        Button backButton = new Button("← Zurück zur Hilfe-Übersicht", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("hilfe")));
        backButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        // Header
        H1 title = new H1("Hilfe für Schützen");
        title.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin-bottom", "var(--lumo-space-s)");

        Paragraph intro = new Paragraph(
                "Hier finden Sie eine Übersicht über alle Funktionen, die Ihnen als Schütze zur Verfügung stehen."
        );
        intro.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-xl)")
                .set("font-size", "var(--lumo-font-size-l)");

        add(backButton, title, intro);

        // Erstelle Hilfe-Abschnitte
        add(createDashboardSection());
        add(createMeineEintraegeSection());
        add(createNeuerEintragSection());
        add(createMeineVereineSection());
        add(createProfilSection());
    }

    private Div createDashboardSection() {
        Div section = createSection("dashboard", "Dashboard", VaadinIcon.HOME);

        Paragraph description = new Paragraph(
                "Das Dashboard ist Ihre Startseite und bietet Ihnen einen schnellen Überblick über Ihre Aktivitäten."
        );

        H3 featuresTitle = new H3("Was Sie hier finden:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Übersicht Ihrer letzten Schießeinträge"));
        features.add(new ListItem("Statistiken zu Ihren Schießaktivitäten"));
        features.add(new ListItem("Schnellzugriff auf häufig genutzte Funktionen"));
        features.add(new ListItem("Benachrichtigungen und Hinweise"));

        H3 howToTitle = new H3("So nutzen Sie das Dashboard:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie über das Menü links zu 'Dashboard'"));
        howTo.add(new ListItem("Sehen Sie sich Ihre neuesten Einträge an"));
        howTo.add(new ListItem("Nutzen Sie die Schnellzugriff-Buttons für häufige Aktionen"));

        // Placeholder for screenshot
        Div imagePlaceholder = createImagePlaceholder("dashboard-overview.png", "Dashboard Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, imagePlaceholder);
        return section;
    }

    private Div createMeineEintraegeSection() {
        Div section = createSection("meine-eintraege", "Meine Einträge", VaadinIcon.BOOK);

        Paragraph description = new Paragraph(
                "Hier können Sie alle Ihre Schießeinträge einsehen, filtern und exportieren."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Ihre Schießeinträge in einer übersichtlichen Tabelle anzeigen"));
        features.add(new ListItem("Nach Datum, Verein oder Disziplin filtern"));
        features.add(new ListItem("Einträge als PDF oder Excel exportieren"));
        features.add(new ListItem("Details zu einzelnen Einträgen anzeigen"));
        features.add(new ListItem("Einträge bearbeiten oder löschen (wenn noch nicht bestätigt)"));

        H3 howToTitle = new H3("So verwalten Sie Ihre Einträge:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Meine Einträge' im Menü"));
        howTo.add(new ListItem("Nutzen Sie die Filter-Optionen oben, um bestimmte Einträge zu finden"));
        howTo.add(new ListItem("Klicken Sie auf einen Eintrag, um Details anzuzeigen"));
        howTo.add(new ListItem("Verwenden Sie die Export-Buttons, um Ihre Daten zu exportieren"));
        howTo.add(new ListItem("Bei unbestätigten Einträgen können Sie diese noch bearbeiten oder löschen"));

        // Placeholder for screenshot
        Div imagePlaceholder = createImagePlaceholder("meine-eintraege.png", "Meine Einträge Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, imagePlaceholder);
        return section;
    }

    private Div createNeuerEintragSection() {
        Div section = createSection("neuer-eintrag", "Neuer Eintrag", VaadinIcon.PLUS);

        Paragraph description = new Paragraph(
                "Erstellen Sie hier neue Schießeinträge und dokumentieren Sie Ihre Schießaktivitäten."
        );

        H3 featuresTitle = new H3("Was Sie eintragen können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Datum des Schießens"));
        features.add(new ListItem("Verein, bei dem Sie geschossen haben"));
        features.add(new ListItem("Schießstand"));
        features.add(new ListItem("Disziplin (z.B. Luftgewehr, Kleinkalibergewehr)"));
        features.add(new ListItem("Anzahl der Schuss"));
        features.add(new ListItem("Ergebnis/Treffer"));
        features.add(new ListItem("Optionale Notizen"));

        H3 howToTitle = new H3("So erstellen Sie einen neuen Eintrag:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Eintrag' im Menü"));
        howTo.add(new ListItem("Wählen Sie das Datum des Schießens aus"));
        howTo.add(new ListItem("Wählen Sie den Verein und Schießstand aus"));
        howTo.add(new ListItem("Geben Sie die Disziplin an"));
        howTo.add(new ListItem("Tragen Sie Anzahl der Schuss und Ihr Ergebnis ein"));
        howTo.add(new ListItem("Fügen Sie optional Notizen hinzu"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern', um den Eintrag zu erstellen"));
        howTo.add(new ListItem("Der Eintrag wird nun an den zuständigen Aufseher zur Bestätigung gesendet"));

        Div tippBox = createTippBox(
                "Tipp: Ihr Eintrag muss von einem Aufseher bestätigt werden, bevor er offiziell wird. " +
                "Sie können den Eintrag solange noch bearbeiten, bis er bestätigt wurde."
        );

        // Placeholder for screenshot
        Div imagePlaceholder = createImagePlaceholder("neuer-eintrag-formular.png", "Formular für neuen Eintrag");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createMeineVereineSection() {
        Div section = createSection("meine-vereine", "Meine Vereine", VaadinIcon.GROUP);

        Paragraph description = new Paragraph(
                "Hier sehen Sie alle Vereine, in denen Sie Mitglied sind, und können Details zu diesen Vereinen einsehen."
        );

        H3 featuresTitle = new H3("Was Sie hier finden:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Liste aller Ihrer Vereinsmitgliedschaften"));
        features.add(new ListItem("Vereinsdetails (Name, Adresse, Kontakt)"));
        features.add(new ListItem("Ihre Rolle im Verein (Mitglied, Aufseher, Vereinschef)"));
        features.add(new ListItem("Zugeordnete Schießstände des Vereins"));
        features.add(new ListItem("Mitgliedschaftsstatus und Beitrittsdatum"));

        H3 howToTitle = new H3("So nutzen Sie die Vereinsübersicht:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Meine Vereine' im Menü"));
        howTo.add(new ListItem("Sehen Sie alle Ihre Vereinsmitgliedschaften auf einen Blick"));
        howTo.add(new ListItem("Klicken Sie auf einen Verein, um weitere Details anzuzeigen"));
        howTo.add(new ListItem("Kontaktieren Sie bei Fragen den Vereinschef über die angegebenen Kontaktdaten"));

        Div tippBox = createTippBox(
                "Hinweis: Um einem neuen Verein beizutreten, wenden Sie sich bitte an den Vereinschef " +
                "des gewünschten Vereins oder an einen Administrator."
        );

        // Placeholder for screenshot
        Div imagePlaceholder = createImagePlaceholder("meine-vereine.png", "Meine Vereine Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createProfilSection() {
        Div section = createSection("profil", "Profil", VaadinIcon.USER);

        Paragraph description = new Paragraph(
                "In Ihrem Profil können Sie Ihre persönlichen Daten einsehen und bearbeiten."
        );

        H3 featuresTitle = new H3("Was Sie hier ändern können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Vorname und Nachname"));
        features.add(new ListItem("E-Mail-Adresse"));
        features.add(new ListItem("Telefonnummer"));
        features.add(new ListItem("Adresse"));
        features.add(new ListItem("Passwort ändern"));
        features.add(new ListItem("Profilbild hochladen (optional)"));

        H3 howToTitle = new H3("So bearbeiten Sie Ihr Profil:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Klicken Sie oben rechts auf Ihren Namen oder auf 'Profil' im Menü"));
        howTo.add(new ListItem("Bearbeiten Sie die gewünschten Felder"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern', um die Änderungen zu übernehmen"));
        howTo.add(new ListItem("Um Ihr Passwort zu ändern, klicken Sie auf 'Passwort ändern'"));

        Div tippBox = createTippBox(
                "Wichtig: Halten Sie Ihre Kontaktdaten aktuell, damit Sie wichtige Benachrichtigungen " +
                "zu Ihren Einträgen und Vereinen erhalten können."
        );

        // Placeholder for screenshot
        Div imagePlaceholder = createImagePlaceholder("profil-bearbeiten.png", "Profil bearbeiten");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createSection(String id, String title, VaadinIcon icon) {
        Div section = new Div();
        section.setId(id);
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("scroll-margin-top", "100px"); // For proper scrolling with fixed header

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        com.vaadin.flow.component.icon.Icon iconComponent = icon.create();
        iconComponent.setSize("32px");
        iconComponent.getStyle().set("color", "var(--lumo-primary-color)");

        H2 sectionTitle = new H2(title);
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-header-text-color)");

        header.add(iconComponent, sectionTitle);
        section.add(header);

        return section;
    }

    private Div createImagePlaceholder(String imageName, String description) {
        Div placeholder = new Div();
        placeholder.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "2px dashed var(--lumo-contrast-30pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xl)")
                .set("margin-top", "var(--lumo-space-m)")
                .set("text-align", "center")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("min-height", "300px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center");

        com.vaadin.flow.component.icon.Icon imageIcon = VaadinIcon.PICTURE.create();
        imageIcon.setSize("64px");
        imageIcon.getStyle()
                .set("color", "var(--lumo-contrast-30pct)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph text = new Paragraph("Bild-Platzhalter: " + imageName);
        text.getStyle()
                .set("font-weight", "600")
                .set("margin", "0");

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin", "var(--lumo-space-xs) 0 0 0");

        Paragraph hint = new Paragraph("Hier können Sie ein Screenshot einfügen");
        hint.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-style", "italic")
                .set("margin", "var(--lumo-space-s) 0 0 0");

        placeholder.add(imageIcon, text, desc, hint);
        return placeholder;
    }

    private Div createTippBox(String text) {
        Div tippBox = new Div();
        tippBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        com.vaadin.flow.component.icon.Icon icon = VaadinIcon.LIGHTBULB.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--lumo-primary-color)");

        Paragraph tippText = new Paragraph(text);
        tippText.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-body-text-color)");

        content.add(icon, tippText);
        tippBox.add(content);
        return tippBox;
    }

    private String getSectionIdFromRoute(String route) {
        // Map route names to section IDs
        if (route.contains("dashboard")) return "dashboard";
        if (route.contains("meine-eintraege")) return "meine-eintraege";
        if (route.contains("neuer-eintrag")) return "neuer-eintrag";
        if (route.contains("meine-vereine")) return "meine-vereine";
        if (route.contains("profil")) return "profil";
        return "dashboard"; // default
    }
}
