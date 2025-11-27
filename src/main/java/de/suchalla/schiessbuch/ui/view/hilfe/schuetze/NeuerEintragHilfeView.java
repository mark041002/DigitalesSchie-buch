package de.suchalla.schiessbuch.ui.view.hilfe.schuetze;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Neuer Eintrag (Schütze).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze/neuer-eintrag", layout = MainLayout.class)
@PageTitle("Hilfe: Neuer Eintrag")
@PermitAll
public class NeuerEintragHilfeView extends BaseHilfeView {

    public NeuerEintragHilfeView() {
        super("schuetze", "neuer-eintrag", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Neuen Schießeintrag erstellen", VaadinIcon.PLUS);

        Paragraph intro = new Paragraph(
                "Erstellen Sie hier neue Schießeinträge und dokumentieren Sie Ihre Schießaktivitäten. " +
                "Jeder Eintrag muss von einem Aufseher signiert werden, bevor er offiziell wird."
        );

        H3 fieldsTitle = new H3("Welche Felder Sie ausfüllen müssen:");
        fieldsTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList fields = new UnorderedList();
        fields.add(new ListItem("Datum: Wann haben Sie geschossen?"));
        fields.add(new ListItem("Verein: Bei welchem Verein haben Sie geschossen?"));
        fields.add(new ListItem("Schießstand: Auf welchem Stand?"));
        fields.add(new ListItem("Disziplin: Welche Disziplin?"));
        fields.add(new ListItem("Waffenart: Welche Waffenart haben Sie verwendet?"));
        fields.add(new ListItem("Anzahl Schuss: Wie viele Schuss haben Sie abgegeben?"));
        fields.add(new ListItem("Treffer/Ergebnis: Ihr Schießergebnis"));
        fields.add(new ListItem("Notizen: Optionale Bemerkungen (z.B. Wetterbedingungen, besondere Vorkommnisse)"));

        H3 howToTitle = new H3("So erstellen Sie einen neuen Eintrag:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Klicken Sie auf 'Neuer Eintrag' im Menü"));
        howTo.add(new ListItem("Tragen sie die erforderlichen Informationen in die entsprechenden Felder ein (Pflichtfelder sind mit Punkt markiert)"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern', um den Eintrag zu erstellen"));
        howTo.add(new ListItem("Der Eintrag wird nun an die zuständigen Aufseher zur Bestätigung gesendet"));

        H3 afterTitle = new H3("Was passiert nach dem Erstellen:");
        afterTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList afterSteps = new OrderedList();
        afterSteps.add(new ListItem("Ihr Eintrag erscheint mit Status 'Unbestätigt' in 'Meine Einträge'"));
        afterSteps.add(new ListItem("Ein Aufseher Ihres Vereins wird benachrichtigt"));
        afterSteps.add(new ListItem("Der Aufseher prüft und bestätigt oder lehnt Ihren Eintrag ab"));
        afterSteps.add(new ListItem("Sie erhalten eine Benachrichtigung über die Entscheidung"));
        afterSteps.add(new ListItem("Solange der Eintrag unbestätigt ist, können Sie ihn noch löschen"));

        Div tippBox = createTippBox(
                "Tipp: Füllen Sie den Eintrag direkt nach dem Schießen aus, solange alle Details " +
                "noch frisch im Gedächtnis sind. So vermeiden Sie Fehler."
        );

    

        section.add(intro, fieldsTitle, fields, howToTitle, howTo, afterTitle, afterSteps, tippBox);
        addToContent(section);
    }
}
