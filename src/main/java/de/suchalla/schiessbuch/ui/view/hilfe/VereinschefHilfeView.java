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
 * Hilfe-Seite für Vereinschefs.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/vereinschef", layout = MainLayout.class)
@PageTitle("Hilfe für Vereinschefs")
@PermitAll
public class VereinschefHilfeView extends VerticalLayout implements BeforeEnterObserver {

    private String fromRoute;

    public VereinschefHilfeView() {
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

        if (fromRoute != null && !fromRoute.isEmpty()) {
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => { const element = document.getElementById($0); if(element) element.scrollIntoView({behavior: 'smooth', block: 'start'}); }, 100)",
                    getSectionIdFromRoute(fromRoute)
            );
        }
    }

    private void createContent() {
        Button backButton = new Button("← Zurück zur Hilfe-Übersicht", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("hilfe")));
        backButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H1 title = new H1("Hilfe für Vereinschefs");
        title.getStyle()
                .set("color", "var(--lumo-warning-color)")
                .set("margin-bottom", "var(--lumo-space-s)");

        Paragraph intro = new Paragraph(
                "Hier finden Sie eine Übersicht über alle Funktionen, die Ihnen als Vereinschef zur Verfügung stehen."
        );
        intro.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-xl)")
                .set("font-size", "var(--lumo-font-size-l)");

        add(backButton, title, intro);

        // Reihenfolge für Vereinschef: erst Eintragsverwaltung, dann Vereinsdetails, dann Mitgliedsverwaltung
        add(createEintragsverwaltungSection());
        add(createVereinDetailsSection());
        add(createMitgliedsverwaltungSection());
        add(createZertifikateSection());
        add(createVerbaendeSection());
    }

    private Div createVereinDetailsSection() {
        Div section = createSection("verein-details", "Vereinsdetails", VaadinIcon.COG);

        Paragraph description = new Paragraph(
                "Als Vereinschef können Sie die Details Ihres Vereins verwalten und aktualisieren."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Vereinsinformationen einsehen und bearbeiten"));
        features.add(new ListItem("Name, Adresse und Kontaktdaten aktualisieren"));
        features.add(new ListItem("Logo des Vereins hochladen"));
        features.add(new ListItem("Beschreibung und wichtige Informationen hinterlegen"));
        features.add(new ListItem("Zugeordnete Schießstände verwalten"));
        features.add(new ListItem("Mitgliederstatistiken einsehen"));

        H3 howToTitle = new H3("So bearbeiten Sie die Vereinsdetails:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Vereinsdetails' im Menü"));
        howTo.add(new ListItem("Sehen Sie sich die aktuellen Informationen an"));
        howTo.add(new ListItem("Klicken Sie auf 'Bearbeiten'"));
        howTo.add(new ListItem("Aktualisieren Sie die gewünschten Felder"));
        howTo.add(new ListItem("Optional: Laden Sie ein neues Vereinslogo hoch"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));
        howTo.add(new ListItem("Die Änderungen sind sofort für alle Vereinsmitglieder sichtbar"));

        Div tippBox = createTippBox(
                "Tipp: Halten Sie die Kontaktdaten immer aktuell, damit interessierte Schützen " +
                "und Admins Sie bei Fragen erreichen können."
        );

        Div imagePlaceholder = createImagePlaceholder("vereinsdetails.png", "Vereinsdetails bearbeiten");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createMitgliedsverwaltungSection() {
        Div section = createSection("mitgliedsverwaltung", "Mitgliedsverwaltung", VaadinIcon.USERS);

        Paragraph description = new Paragraph(
                "Verwalten Sie die Mitglieder Ihres Vereins, weisen Sie Rollen zu und fügen Sie neue Mitglieder hinzu."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Vereinsmitglieder in einer Übersicht sehen"));
        features.add(new ListItem("Neue Mitglieder zum Verein hinzufügen"));
        features.add(new ListItem("Aufseher-Rechte an Mitglieder vergeben oder entziehen"));
        features.add(new ListItem("Mitglieder aus dem Verein entfernen"));
        features.add(new ListItem("Mitgliedschaftsstatus einsehen (aktiv, inaktiv)"));
        features.add(new ListItem("Kontaktdaten der Mitglieder einsehen"));
        features.add(new ListItem("Nach Mitgliedern suchen und filtern"));

        H3 howToTitle = new H3("So fügen Sie ein neues Mitglied hinzu:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Mitgliedsverwaltung' im Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neues Mitglied hinzufügen'"));
        howTo.add(new ListItem("Suchen Sie nach dem Benutzer (Schütze muss bereits im System registriert sein)"));
        howTo.add(new ListItem("Wählen Sie optional aus, ob das Mitglied Aufseher-Rechte erhalten soll"));
        howTo.add(new ListItem("Klicken Sie auf 'Hinzufügen'"));
        howTo.add(new ListItem("Das neue Mitglied erhält automatisch eine Benachrichtigung"));

        H3 aufseherTitle = new H3("So ernennen Sie einen Aufseher:");
        aufseherTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList aufseherSteps = new OrderedList();
        aufseherSteps.add(new ListItem("Gehen Sie zur Mitgliedsliste"));
        aufseherSteps.add(new ListItem("Klicken Sie auf das gewünschte Mitglied"));
        aufseherSteps.add(new ListItem("Aktivieren Sie die Checkbox 'Ist Aufseher'"));
        aufseherSteps.add(new ListItem("Klicken Sie auf 'Speichern'"));
        aufseherSteps.add(new ListItem("Das Mitglied hat nun Aufseher-Rechte und kann Einträge bestätigen"));

        Div warningBox = createWarningBox(
                "Wichtig: Aufseher haben erweiterte Rechte. Vergeben Sie diese Rolle nur an vertrauenswürdige " +
                "Mitglieder, die mit den Richtlinien vertraut sind."
        );

        Div tippBox = createTippBox(
                "Tipp: Sie können auch sich selbst als Aufseher eintragen, wenn Sie zusätzlich " +
                "Einträge bestätigen möchten."
        );

        Div imagePlaceholder = createImagePlaceholder("mitgliedsverwaltung.png", "Mitgliedsverwaltung Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, aufseherTitle, aufseherSteps,
                   warningBox, tippBox, imagePlaceholder);
        return section;
    }

    private Div createEintragsverwaltungSection() {
        Div section = createSection("eintragsverwaltung", "Eintragsverwaltung", VaadinIcon.RECORDS);

        Paragraph description = new Paragraph(
                "Als Vereinschef haben Sie Zugriff auf alle Einträge Ihres Vereins und können diese einsehen."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Einträge des Vereins einsehen"));
        features.add(new ListItem("Nach verschiedenen Kriterien filtern (Datum, Schütze, Status, Disziplin)"));
        features.add(new ListItem("Statistiken über Vereinsaktivitäten anzeigen"));
        features.add(new ListItem("Einträge als Excel oder PDF exportieren"));
        features.add(new ListItem("Übersicht über unbestätigte Einträge"));

        H3 howToTitle = new H3("So nutzen Sie die Eintragsverwaltung:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Eintragsverwaltung' im Menü"));
        howTo.add(new ListItem("Wählen Sie Ihren Verein aus (falls Sie mehrere Vereine leiten)"));
        howTo.add(new ListItem("Sie sehen eine Übersicht aller Einträge"));
        howTo.add(new ListItem("Nutzen Sie die Filter, um bestimmte Einträge zu finden"));
        howTo.add(new ListItem("Klicken Sie auf einen Eintrag, um Details anzuzeigen"));
        howTo.add(new ListItem("Exportieren Sie Daten für Berichte oder Statistiken"));

        Div tippBox = createTippBox(
                "Hinweis: Als Vereinschef können Sie keine Einträge bestätigen oder ablehnen. " +
                "Diese Aufgabe liegt bei den Aufsehern. Sie können jedoch alle Einträge einsehen."
        );

        Div imagePlaceholder = createImagePlaceholder("eintragsverwaltung-vereinschef.png", "Eintragsverwaltung für Vereinschefs");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createZertifikateSection() {
        Div section = createSection("zertifikate", "Vereins-Zertifikate", VaadinIcon.DIPLOMA);

        Paragraph description = new Paragraph(
                "Verwalten Sie alle Zertifikate, die innerhalb Ihres Vereins ausgestellt wurden."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Zertifikate des Vereins einsehen"));
        features.add(new ListItem("Zertifikate nach Schütze, Typ oder Datum filtern"));
        features.add(new ListItem("Zertifikate als PDF herunterladen"));
        features.add(new ListItem("Übersicht über ablaufende Zertifikate"));
        features.add(new ListItem("Statistiken über ausgestellte Zertifikate"));

        H3 howToTitle = new H3("So nutzen Sie die Zertifikatsverwaltung:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Vereins-Zertifikate' im Menü"));
        howTo.add(new ListItem("Sie sehen eine Übersicht aller ausgestellten Zertifikate"));
        howTo.add(new ListItem("Nutzen Sie die Filter-Funktionen für gezielte Suchen"));
        howTo.add(new ListItem("Klicken Sie auf ein Zertifikat, um Details anzuzeigen"));
        howTo.add(new ListItem("Laden Sie Zertifikate bei Bedarf als PDF herunter"));

        Div tippBox = createTippBox(
                "Hinweis: Nur Aufseher können Zertifikate ausstellen. Als Vereinschef können Sie " +
                "diese jedoch einsehen und verwalten."
        );

        Div imagePlaceholder = createImagePlaceholder("vereins-zertifikate.png", "Vereins-Zertifikate Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createVerbaendeSection() {
        Div section = createSection("verbaende", "Verbände", VaadinIcon.GLOBE);

        Paragraph description = new Paragraph(
                "Verwalten Sie die Verbandszugehörigkeit Ihres Vereins."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle verfügbaren Verbände einsehen"));
        features.add(new ListItem("Mitgliedschaft in Verbänden beantragen"));
        features.add(new ListItem("Aktuelle Verbandszugehörigkeiten anzeigen"));
        features.add(new ListItem("Details zu Verbänden einsehen"));
        features.add(new ListItem("Kontaktdaten der Verbände abrufen"));

        H3 howToTitle = new H3("So treten Sie einem Verband bei:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Verbände' im Menü"));
        howTo.add(new ListItem("Sehen Sie sich die verfügbaren Verbände an"));
        howTo.add(new ListItem("Klicken Sie auf einen Verband, um Details zu sehen"));
        howTo.add(new ListItem("Klicken Sie auf 'Beitrittsantrag stellen'"));
        howTo.add(new ListItem("Füllen Sie das Antragsformular aus"));
        howTo.add(new ListItem("Senden Sie den Antrag ab"));
        howTo.add(new ListItem("Der Verband wird Ihren Antrag prüfen und Sie benachrichtigen"));

        Div tippBox = createTippBox(
                "Tipp: Die Mitgliedschaft in einem Verband kann für Ihr Verein viele Vorteile bringen, " +
                "wie z.B. Versicherungsschutz, Veranstaltungen und Netzwerkmöglichkeiten."
        );

        Div imagePlaceholder = createImagePlaceholder("verbaende.png", "Verbände Übersicht");

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
                .set("scroll-margin-top", "100px");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        com.vaadin.flow.component.icon.Icon iconComponent = icon.create();
        iconComponent.setSize("32px");
        iconComponent.getStyle().set("color", "var(--lumo-warning-color)");

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
                .set("background", "var(--lumo-warning-color-10pct)")
                .set("border-left", "4px solid var(--lumo-warning-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        com.vaadin.flow.component.icon.Icon icon = VaadinIcon.LIGHTBULB.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--lumo-warning-color)");

        Paragraph tippText = new Paragraph(text);
        tippText.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-body-text-color)");

        content.add(icon, tippText);
        tippBox.add(content);
        return tippBox;
    }

    private Div createWarningBox(String text) {
        Div warningBox = new Div();
        warningBox.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("border-left", "4px solid var(--lumo-error-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        com.vaadin.flow.component.icon.Icon icon = VaadinIcon.WARNING.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--lumo-error-color)");

        Paragraph warningText = new Paragraph(text);
        warningText.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-body-text-color)");

        content.add(icon, warningText);
        warningBox.add(content);
        return warningBox;
    }

    private String getSectionIdFromRoute(String route) {
        if (route.contains("vereindetails") || route.contains("verein-details")) return "verein-details";
        if (route.contains("mitglied")) return "mitgliedsverwaltung";
        if (route.contains("eintraege") || route.contains("verwaltung")) return "eintragsverwaltung";
        if (route.contains("zertifikat")) return "zertifikate";
        if (route.contains("verband")) return "verbaende";
        return "verein-details";
    }
}
