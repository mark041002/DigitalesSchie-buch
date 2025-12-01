package de.suchalla.schiessbuch.ui.view.hilfe.schuetze;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Profil (Schütze).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/schuetze/profil", layout = MainLayout.class)
@PageTitle("Hilfe: Profil")
@PermitAll
public class ProfilHilfeView extends BaseHilfeView {

    public ProfilHilfeView() {
        super("schuetze", "profil", "var(--lumo-primary-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Profil bearbeiten", VaadinIcon.USER);

        Paragraph intro = new Paragraph(
                "In Ihrem Profil können Sie Ihre persönlichen Daten einsehen und bearbeiten. " +
                "Halten Sie Ihre Kontaktdaten aktuell, damit Sie wichtige Benachrichtigungen erhalten."
        );

        H3 featuresTitle = new H3("Was Sie hier ändern können:");
        featuresTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        UnorderedList features = new UnorderedList();
        features.add(new ListItem("Namen ändern"));
        features.add(new ListItem("E-Mail-Adresse (für Login und Benachrichtigungen)"));
        features.add(new ListItem("Passwort ändern"));
        features.add(new ListItem("Account endgültig löschen"));

        H3 howToTitle = new H3("So bearbeiten Sie Ihr Profil:");
        howToTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList howTo = new OrderedList();
        howTo.add(new ListItem("Klicken Sie oben rechts auf Ihren Namen oder auf 'Profil' im Menü"));
        howTo.add(new ListItem("Sie sehen Ihre aktuellen Profildaten"));
        howTo.add(new ListItem("Klicken Sie auf 'Bearbeiten', um Änderungen vorzunehmen"));
        howTo.add(new ListItem("Bearbeiten Sie die gewünschten Felder"));
        howTo.add(new ListItem("Klicken Sie auf 'Speichern', um die Änderungen zu übernehmen"));
        howTo.add(new ListItem("Eine Bestätigung erscheint, wenn die Änderung erfolgreich war"));

        H3 passwordTitle = new H3("So ändern Sie Ihr Passwort:");
        passwordTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        OrderedList passwordSteps = new OrderedList();
        passwordSteps.add(new ListItem("Gehen Sie zu Ihrem Profil"));
        passwordSteps.add(new ListItem("Geben Sie Ihr neues Passwort ein"));
        passwordSteps.add(new ListItem("Bestätigen Sie das neue Passwort"));
        passwordSteps.add(new ListItem("Klicken Sie auf 'Passwort ändern'"));
        passwordSteps.add(new ListItem("Sie werden automatisch ausgeloggt und müssen sich mit dem neuen Passwort anmelden"));





        section.add(intro, featuresTitle, features, howToTitle, howTo, passwordTitle, passwordSteps);
        addToContent(section);
    }
}
