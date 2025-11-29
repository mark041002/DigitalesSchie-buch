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
 * Hilfe-Seite für Administratoren.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/admin", layout = MainLayout.class)
@PageTitle("Hilfe für Administratoren")
@PermitAll
public class AdminHilfeView extends VerticalLayout implements BeforeEnterObserver {

    private String fromRoute;

    public AdminHilfeView() {
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

        H1 title = new H1("Hilfe für Administratoren");
        title.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("margin-bottom", "var(--lumo-space-s)");

        Paragraph intro = new Paragraph(
                "Als Administrator haben Sie vollständigen Zugriff auf alle Funktionen des Systems. " +
                "Hier finden Sie eine Übersicht über Ihre administrativen Möglichkeiten."
        );
        intro.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "var(--lumo-space-xl)")
                .set("font-size", "var(--lumo-font-size-l)");

        add(backButton, title, intro);

        add(createVerbaendeVerwaltungSection());
        add(createVereineVerwaltungSection());
        add(createSchiesstaendeVerwaltungSection());
        add(createMitgliederVerwaltungSection());
        add(createZertifikateSection());
    }

    private Div createVerbaendeVerwaltungSection() {
        Div section = createSection("verbaende", "Verbänderverwaltung", VaadinIcon.GLOBE);

        Paragraph description = new Paragraph(
                "Verwalten Sie alle Verbände im System, legen Sie neue Verbände an und bearbeiten Sie bestehende."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Verbände im System einsehen"));
        features.add(new ListItem("Neue Verbände anlegen"));
        features.add(new ListItem("Verbandsdetails bearbeiten (Name, Adresse, Kontakt)"));
        features.add(new ListItem("Verbände deaktivieren oder löschen"));
        features.add(new ListItem("Vereinsmitgliedschaften in Verbänden verwalten"));
        features.add(new ListItem("Verbandsstatistiken einsehen"));
        features.add(new ListItem("Beitrittsanträge von Vereinen prüfen und genehmigen"));

        H3 howToTitle = new H3("So legen Sie einen neuen Verband an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Verbände' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Verband'"));
        howTo.add(new ListItem("Geben Sie den Namen des Verbands ein"));
        howTo.add(new ListItem("Füllen Sie Adresse und Kontaktdaten aus"));
        howTo.add(new ListItem("Optional: Laden Sie ein Logo hoch"));
        howTo.add(new ListItem("Geben Sie eine Beschreibung an"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));
        howTo.add(new ListItem("Der Verband ist nun im System verfügbar"));

        Div tippBox = createTippBox(
                "Tipp: Verbände sind die oberste organisatorische Ebene. Vereine können sich Verbänden " +
                "zuordnen, um an Veranstaltungen teilzunehmen und von Verbandsleistungen zu profitieren."
        );

        Div imagePlaceholder = createImagePlaceholder("verbaende-verwaltung.png", "Verbänderverwaltung");

        section.add(description, featuresTitle, features, howToTitle, howTo, tippBox, imagePlaceholder);
        return section;
    }

    private Div createVereineVerwaltungSection() {
        Div section = createSection("vereine", "Vereinsverwaltung", VaadinIcon.BUILDING);

        Paragraph description = new Paragraph(
                "Verwalten Sie alle Vereine im System, legen Sie neue Vereine an und verwalten Sie Mitgliedschaften."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Vereine im System einsehen"));
        features.add(new ListItem("Neue Vereine anlegen"));
        features.add(new ListItem("Vereinsdetails bearbeiten"));
        features.add(new ListItem("Vereinschefs zuweisen oder ändern"));
        features.add(new ListItem("Vereine Verbänden zuordnen"));
        features.add(new ListItem("Vereine deaktivieren oder löschen"));
        features.add(new ListItem("Vereinsmitglieder einsehen"));
        features.add(new ListItem("Schießstände einem Verein zuordnen"));

        H3 howToTitle = new H3("So legen Sie einen neuen Verein an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Vereine' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Verein'"));
        howTo.add(new ListItem("Geben Sie den Vereinsnamen ein"));
        howTo.add(new ListItem("Füllen Sie Adresse und Kontaktdaten aus"));
        howTo.add(new ListItem("Weisen Sie optional einen Vereinschef zu"));
        howTo.add(new ListItem("Ordnen Sie den Verein optional einem Verband zu"));
        howTo.add(new ListItem("Fügen Sie Schießstände hinzu oder ordnen Sie bestehende zu"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));

        H3 vereinschefTitle = new H3("So weisen Sie einen Vereinschef zu:");
        vereinschefTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList vereinschefSteps = new OrderedList();
        vereinschefSteps.add(new ListItem("Öffnen Sie die Vereinsdetails"));
        vereinschefSteps.add(new ListItem("Klicken Sie auf 'Bearbeiten'"));
        vereinschefSteps.add(new ListItem("Suchen Sie unter 'Mitglieder' nach dem gewünschten Benutzer"));
        vereinschefSteps.add(new ListItem("Aktivieren Sie die Checkbox 'Ist Vereinschef'"));
        vereinschefSteps.add(new ListItem("Klicken Sie auf 'Speichern'"));
        vereinschefSteps.add(new ListItem("Der Benutzer erhält automatisch Vereinschef-Rechte"));

        Div warningBox = createWarningBox(
                "Wichtig: Seien Sie vorsichtig beim Löschen von Vereinen. Alle zugehörigen Daten " +
                "(Einträge, Zertifikate, Mitgliedschaften) werden ebenfalls gelöscht!"
        );

        Div tippBox = createTippBox(
                "Tipp: Bevor Sie einen Verein löschen, prüfen Sie, ob noch aktive Mitgliedschaften " +
                "oder wichtige Einträge vorhanden sind. Erwägen Sie stattdessen eine Deaktivierung."
        );

        Div imagePlaceholder = createImagePlaceholder("vereine-verwaltung.png", "Vereinsverwaltung");

        section.add(description, featuresTitle, features, howToTitle, howTo, vereinschefTitle, vereinschefSteps,
                   warningBox, tippBox, imagePlaceholder);
        return section;
    }

    private Div createSchiesstaendeVerwaltungSection() {
        Div section = createSection("schiesstaende", "Schießständeverwaltung", VaadinIcon.CROSSHAIRS);

        Paragraph description = new Paragraph(
                "Verwalten Sie alle Schießstände im System, legen Sie neue Stände an und weisen Sie Aufseher zu."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Schießstände im System einsehen"));
        features.add(new ListItem("Neue Schießstände anlegen"));
        features.add(new ListItem("Schießstanddetails bearbeiten"));
        features.add(new ListItem("Schießstandaufseher zuweisen"));
        features.add(new ListItem("Schießstände Vereinen zuordnen"));
        features.add(new ListItem("Verfügbare Disziplinen und Bahnen verwalten"));
        features.add(new ListItem("Schießstände deaktivieren oder löschen"));
        features.add(new ListItem("Öffnungszeiten und Kapazitäten verwalten"));

        H3 howToTitle = new H3("So legen Sie einen neuen Schießstand an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Schießstände' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Schießstand'"));
        howTo.add(new ListItem("Geben Sie den Namen des Schießstands ein"));
        howTo.add(new ListItem("Füllen Sie Adresse und Kontaktdaten aus"));
        howTo.add(new ListItem("Geben Sie Öffnungszeiten an"));
        howTo.add(new ListItem("Tragen Sie verfügbare Disziplinen ein (z.B. Luftgewehr, KK-Gewehr)"));
        howTo.add(new ListItem("Geben Sie die Anzahl der Bahnen an"));
        howTo.add(new ListItem("Weisen Sie optional einen Schießstandaufseher zu"));
        howTo.add(new ListItem("Ordnen Sie den Stand einem oder mehreren Vereinen zu"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));

        H3 aufseherTitle = new H3("So weisen Sie einen Schießstandaufseher zu:");
        aufseherTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList aufseherSteps = new OrderedList();
        aufseherSteps.add(new ListItem("Öffnen Sie die Schießstanddetails"));
        aufseherSteps.add(new ListItem("Klicken Sie auf 'Aufseher zuweisen'"));
        aufseherSteps.add(new ListItem("Suchen Sie nach dem gewünschten Benutzer"));
        aufseherSteps.add(new ListItem("Der Benutzer erhält automatisch die Rolle 'Schießstandaufseher'"));
        aufseherSteps.add(new ListItem("Der Aufseher kann nun Einträge für diesen Stand verwalten"));

        Div tippBox = createTippBox(
                "Tipp: Schießstände können mehreren Vereinen zugeordnet werden. Dies ist sinnvoll, " +
                "wenn mehrere Vereine den gleichen Stand nutzen."
        );

        Div imagePlaceholder = createImagePlaceholder("schiesstaende-verwaltung.png", "Schießständeverwaltung");

        section.add(description, featuresTitle, features, howToTitle, howTo, aufseherTitle, aufseherSteps,
                   tippBox, imagePlaceholder);
        return section;
    }

    private Div createMitgliederVerwaltungSection() {
        Div section = createSection("mitglieder", "Mitgliederverwaltung", VaadinIcon.USERS);

        Paragraph description = new Paragraph(
                "Verwalten Sie alle Benutzer im System, legen Sie neue Benutzer an und weisen Sie Rollen zu."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Benutzer im System einsehen"));
        features.add(new ListItem("Neue Benutzer anlegen"));
        features.add(new ListItem("Benutzerdaten bearbeiten (Name, E-Mail, Kontakt)"));
        features.add(new ListItem("Systemrollen zuweisen (Schütze, Admin, Schießstandaufseher)"));
        features.add(new ListItem("Passwörter zurücksetzen"));
        features.add(new ListItem("Benutzer deaktivieren oder löschen"));
        features.add(new ListItem("Vereinsmitgliedschaften einsehen und verwalten"));
        features.add(new ListItem("Benutzeraktivitäten und Statistiken einsehen"));

        H3 howToTitle = new H3("So legen Sie einen neuen Benutzer an:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Mitglieder' im Admin-Menü"));
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Benutzer'"));
        howTo.add(new ListItem("Geben Sie Vor- und Nachname ein"));
        howTo.add(new ListItem("Geben Sie eine E-Mail-Adresse an"));
        howTo.add(new ListItem("Setzen Sie ein temporäres Passwort"));
        howTo.add(new ListItem("Wählen Sie die Systemrolle aus (Standard: Schütze)"));
        howTo.add(new ListItem("Optional: Fügen Sie Kontaktdaten hinzu"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern'"));
        howTo.add(new ListItem("Der Benutzer kann sich nun anmelden und sein Passwort ändern"));

        H3 rollenTitle = new H3("Verfügbare Systemrollen:");
        rollenTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList rollen = new UnorderedList();
        rollen.add(new ListItem("Schütze: Standardrolle, kann Einträge erstellen und eigene Daten verwalten"));
        rollen.add(new ListItem("Admin: Vollzugriff auf alle administrativen Funktionen"));
        rollen.add(new ListItem("Schießstandaufseher: Kann einen bestimmten Schießstand verwalten"));

        Paragraph rollenHinweis = new Paragraph(
                "Aufseher und Vereinschef sind keine Systemrollen, sondern Berechtigungen " +
                "innerhalb eines Vereins, die vom Vereinschef vergeben werden."
        );
        rollenHinweis.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "var(--lumo-space-s)");

        Div warningBox = createWarningBox(
                "Wichtig: Die Admin-Rolle hat vollständigen Zugriff auf das gesamte System. " +
                "Vergeben Sie diese Rolle nur an vertrauenswürdige Personen!"
        );

        Div tippBox = createTippBox(
                "Tipp: Beim Löschen eines Benutzers bleiben seine Einträge erhalten, werden aber " +
                "als 'gelöschter Benutzer' markiert. So bleibt die Datenintegrität gewahrt."
        );

        Div imagePlaceholder = createImagePlaceholder("mitglieder-verwaltung.png", "Mitgliederverwaltung");

        section.add(description, featuresTitle, features, howToTitle, howTo, rollenTitle, rollen, rollenHinweis,
                   warningBox, tippBox, imagePlaceholder);
        return section;
    }

    private Div createZertifikateSection() {
        Div section = createSection("zertifikate", "Alle Zertifikate", VaadinIcon.DIPLOMA);

        Paragraph description = new Paragraph(
                "Sehen Sie alle im System ausgestellten Zertifikate ein und verwalten Sie diese."
        );

        H3 featuresTitle = new H3("Was Sie hier tun können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Alle Zertifikate systemweit einsehen"));
        features.add(new ListItem("Nach Verein, Schütze, Typ oder Aussteller filtern"));
        features.add(new ListItem("Zertifikatsdetails anzeigen"));
        features.add(new ListItem("Zertifikate als PDF herunterladen"));
        features.add(new ListItem("Ablaufende Zertifikate einsehen"));
        features.add(new ListItem("Zertifikate widerrufen (im Ausnahmefall)"));
        features.add(new ListItem("Statistiken über ausgestellte Zertifikate"));
        features.add(new ListItem("Zertifikate exportieren (Excel, PDF)"));

        H3 howToTitle = new H3("So nutzen Sie die Zertifikatsverwaltung:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Navigieren Sie zu 'Alle Zertifikate' im Admin-Menü"));
        howTo.add(new ListItem("Sie sehen eine Übersicht aller Zertifikate im System"));
        howTo.add(new ListItem("Nutzen Sie die umfangreichen Filter-Optionen"));
        howTo.add(new ListItem("Klicken Sie auf ein Zertifikat, um Details anzuzeigen"));
        howTo.add(new ListItem("Prüfen Sie die Gültigkeit und den QR-Code"));
        howTo.add(new ListItem("Bei Bedarf: Widerrufen Sie ungültige Zertifikate"));
        howTo.add(new ListItem("Exportieren Sie Zertifikatsdaten für Berichte"));

        H3 widerrufTitle = new H3("Wann sollte ein Zertifikat widerrufen werden:");
        widerrufTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList widerrufGruende = new UnorderedList();
        widerrufGruende.add(new ListItem("Das Zertifikat wurde irrtümlich ausgestellt"));
        widerrufGruende.add(new ListItem("Die Qualifikation des Schützen wurde zurückgezogen"));
        widerrufGruende.add(new ListItem("Es wurden Unstimmigkeiten oder Betrug festgestellt"));
        widerrufGruende.add(new ListItem("Der Schütze hat gegen Sicherheitsbestimmungen verstoßen"));

        Div warningBox = createWarningBox(
                "Achtung: Das Widerrufen eines Zertifikats ist eine schwerwiegende Maßnahme. " +
                "Dokumentieren Sie immer den Grund für einen Widerruf!"
        );

        Div tippBox = createTippBox(
                "Tipp: Nutzen Sie die Filter-Funktion, um schnell ablaufende Zertifikate zu finden " +
                "und die zuständigen Vereine oder Aufseher zu informieren."
        );

        Div imagePlaceholder = createImagePlaceholder("alle-zertifikate.png", "Alle Zertifikate Übersicht");

        section.add(description, featuresTitle, features, howToTitle, howTo, widerrufTitle, widerrufGruende,
                   warningBox, tippBox, imagePlaceholder);
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
        iconComponent.getStyle().set("color", "var(--lumo-error-color)");

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
        if (route.contains("verband") && route.contains("verwaltung")) return "verbaende";
        if (route.contains("verein") && route.contains("verwaltung")) return "vereine";
        if (route.contains("schiesstand") || route.contains("schiesstaend")) return "schiesstaende";
        if (route.contains("mitglied") && route.contains("verwaltung")) return "mitglieder";
        if (route.contains("zertifikat")) return "zertifikate";
        return "verbaende";
    }
}
