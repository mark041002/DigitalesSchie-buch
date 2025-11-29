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
 * Hilfe-Seite für Aufseher und Schießstandaufseher.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/aufseher", layout = MainLayout.class)
@PageTitle("Hilfe für Aufseher")
@PermitAll
public class AufseherHilfeView extends VerticalLayout implements BeforeEnterObserver {

    private String fromRoute;

    public AufseherHilfeView() {
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
    }

    private void createContent() {
        Button backButton = new Button("← Zurück zur Hilfe-Übersicht", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("hilfe")));
        backButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H1 title = new H1("Hilfe für Aufseher & Schießstandaufseher");
        title.getStyle()
                .set("color", "var(--lumo-success-color)")
                .set("margin-bottom", "var(--lumo-space-s)");

        Paragraph intro = new Paragraph(
                "Hier finden Sie eine Übersicht über alle Funktionen für Aufseher und Schießstandaufseher."
        );
        intro.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-xl)")
                .set("font-size", "var(--lumo-font-size-l)");

        add(backButton, title, intro);

        add(createEintragsverwaltungSection());
        add(createZertifikateSection());
        add(createSchiesstandDetailsSection());
    }

    private Div createEintragsverwaltungSection() {
        Div section = createSection("eintragsverwaltung", "Eintragsverwaltung", VaadinIcon.RECORDS);

        Paragraph description = new Paragraph(
                "Als Aufseher sind Sie dafür verantwortlich, Schießeinträge zu überprüfen und zu bestätigen."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Einträge Ihres Vereins/Schießstands einsehen"));
        features.add(new ListItem("Unbestätigte Einträge prüfen"));
        features.add(new ListItem("Einträge bestätigen oder ablehnen"));
        features.add(new ListItem("Einträge nach verschiedenen Kriterien filtern (Datum, Schütze, Status)"));
        features.add(new ListItem("Details zu einzelnen Einträgen anzeigen"));
        features.add(new ListItem("Bestätigte Einträge exportieren"));

        H3 howToTitle = new H3("So verwalten Sie Einträge:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Eintragsverwaltung' im Menü"));
        howTo.add(new ListItem("Sie sehen eine Übersicht aller Einträge"));
        howTo.add(new ListItem("Unbestätigte Einträge werden farblich hervorgehoben"));
        howTo.add(new ListItem("Klicken Sie auf einen Eintrag, um Details anzuzeigen"));
        howTo.add(new ListItem("Prüfen Sie die Angaben sorgfältig"));
        howTo.add(new ListItem("Klicken Sie auf 'Bestätigen', wenn alles korrekt ist"));
        howTo.add(new ListItem("Oder klicken Sie auf 'Ablehnen' und geben Sie einen Grund an"));
        howTo.add(new ListItem("Der Schütze wird automatisch über Ihre Entscheidung informiert"));

        Div warningBox = createWarningBox(
                "Wichtig: Einmal bestätigte Einträge können nicht mehr geändert werden. " +
                "Bitte prüfen Sie alle Angaben sorgfältig, bevor Sie einen Eintrag bestätigen."
        );

        Div tippBox = createTippBox(
                "Tipp: Nutzen Sie die Filter-Funktionen, um schnell unbestätigte Einträge zu finden. " +
                "Sie können auch nach bestimmten Schützen oder Zeiträumen filtern."
        );

        Div imagePlaceholder = createImagePlaceholder("eintragsverwaltung.png", "Eintragsverwaltung Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, warningBox, tippBox, imagePlaceholder);
        return section;
    }

    private Div createZertifikateSection() {
        Div section = createSection("zertifikate", "Zertifikate", VaadinIcon.DIPLOMA);

        Paragraph description = new Paragraph(
                "Als Aufseher können Sie Zertifikate für Schützen ausstellen, die bestimmte Anforderungen erfüllt haben."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Neue Zertifikate für Schützen ausstellen"));
        features.add(new ListItem("Ausgestellte Zertifikate einsehen"));
        features.add(new ListItem("Zertifikate als PDF herunterladen"));
        features.add(new ListItem("Zertifikate widerrufen (bei Bedarf)"));
        features.add(new ListItem("Zertifikate nach Schütze oder Typ filtern"));

        H3 howToTitle = new H3("So stellen Sie ein Zertifikat aus:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Meine Zertifikate' im Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neues Zertifikat ausstellen'"));
        howTo.add(new ListItem("Wählen Sie den Schützen aus"));
        howTo.add(new ListItem("Wählen Sie den Zertifikatstyp (z.B. Sachkundenachweis, Fortgeschrittenen-Zertifikat)"));
        howTo.add(new ListItem("Geben Sie das Ausstellungsdatum und optional ein Ablaufdatum an"));
        howTo.add(new ListItem("Fügen Sie relevante Bemerkungen hinzu"));
        howTo.add(new ListItem("Klicken Sie auf 'Zertifikat ausstellen'"));
        howTo.add(new ListItem("Das Zertifikat wird automatisch mit einem QR-Code zur Verifizierung versehen"));

        Div tippBox = createTippBox(
                "Tipp: Jedes Zertifikat erhält automatisch einen eindeutigen QR-Code, " +
                "mit dem es über die öffentliche Verifizierungsfunktion geprüft werden kann."
        );

        Div imagePlaceholder = createImagePlaceholder("zertifikate-ausstellen.png", "Zertifikat ausstellen");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createSchiesstandDetailsSection() {
        Div section = createSection("schiesstand-details", "Schießstanddetails (nur Schießstandaufseher)", VaadinIcon.COG);

        Paragraph description = new Paragraph(
                "Als Schießstandaufseher können Sie die Details Ihres Schießstands verwalten."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Schießstandinformationen einsehen (Name, Adresse, Kontakt)"));
        features.add(new ListItem("Öffnungszeiten aktualisieren"));
        features.add(new ListItem("Verfügbare Disziplinen verwalten"));
        features.add(new ListItem("Kapazitäten und Bahnen angeben"));
        features.add(new ListItem("Besondere Hinweise für Schützen hinterlegen"));
        features.add(new ListItem("Kontaktdaten aktualisieren"));

        H3 howToTitle = new H3("So bearbeiten Sie die Schießstanddetails:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Schießstanddetails' im Menü"));
        howTo.add(new ListItem("Sehen Sie sich die aktuellen Informationen an"));
        howTo.add(new ListItem("Klicken Sie auf 'Bearbeiten'"));
        howTo.add(new ListItem("Aktualisieren Sie die gewünschten Felder"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));
        howTo.add(new ListItem("Die Änderungen sind sofort für alle Nutzer sichtbar"));

        Div tippBox = createTippBox(
                "Tipp: Halten Sie die Informationen aktuell, besonders Öffnungszeiten und Kontaktdaten, " +
                "damit Schützen immer die richtigen Informationen haben."
        );

        Div imagePlaceholder = createImagePlaceholder("schiesstand-details.png", "Schießstanddetails bearbeiten");

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
        iconComponent.getStyle().set("color", "var(--lumo-success-color)");

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
                .set("background", "var(--lumo-success-color-10pct)")
                .set("border-left", "4px solid var(--lumo-success-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        com.vaadin.flow.component.icon.Icon icon = VaadinIcon.LIGHTBULB.create();
        icon.setSize("24px");
        icon.getStyle().set("color", "var(--lumo-success-color)");

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
        if (route.contains("eintraege") || route.contains("verwaltung")) return "eintragsverwaltung";
        if (route.contains("zertifikat")) return "zertifikate";
        if (route.contains("schiesstand")) return "schiesstand-details";
        return "eintragsverwaltung";
    }
}
