package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.service.BenutzerService;
import lombok.extern.slf4j.Slf4j;

/**
 * Registrierungs-View für neue Benutzer.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route("register")
@PageTitle("Registrierung | Digitales Schießbuch")
@AnonymousAllowed
@Slf4j
public class RegisterView extends VerticalLayout {

    private final BenutzerService benutzerService;

    private final TextField vornameField = new TextField("Vorname");
    private final TextField nachnameField = new TextField("Nachname");
    private final EmailField emailField = new EmailField("E-Mail-Adresse");
    private final PasswordField passwortField = new PasswordField("Passwort");
    private final PasswordField passwortBestaetigenField = new PasswordField("Passwort bestätigen");
    private final Button registerButton = new Button("Registrieren");

    private final Binder<Benutzer> binder = new BeanValidationBinder<>(Benutzer.class);

    public RegisterView(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setMaxWidth("500px");
        getStyle().set("margin", "0 auto");

        createForm();
    }

    /**
     * Erstellt das Registrierungsformular.
     */
    private void createForm() {
        H1 title = new H1("Registrierung");

        Paragraph beschreibung = new Paragraph(
            "Erstellen Sie ein neues Konto für das Digitale Schießbuch."
        );

        // Felder konfigurieren
        vornameField.setRequired(true);
        vornameField.setWidthFull();

        nachnameField.setRequired(true);
        nachnameField.setWidthFull();

        emailField.setRequired(true);
        emailField.setErrorMessage("Bitte geben Sie eine gültige E-Mail-Adresse ein");
        emailField.setWidthFull();

        passwortField.setRequired(true);
        passwortField.setMinLength(6);
        passwortField.setHelperText("Mindestens 6 Zeichen");
        passwortField.setWidthFull();

        passwortBestaetigenField.setRequired(true);
        passwortBestaetigenField.setWidthFull();

        // Binder konfigurieren
        binder.forField(vornameField)
                .asRequired("Vorname ist erforderlich")
                .bind(Benutzer::getVorname, Benutzer::setVorname);

        binder.forField(nachnameField)
                .asRequired("Nachname ist erforderlich")
                .bind(Benutzer::getNachname, Benutzer::setNachname);

        binder.forField(emailField)
                .asRequired("E-Mail ist erforderlich")
                .bind(Benutzer::getEmail, Benutzer::setEmail);

        binder.forField(passwortField)
                .asRequired("Passwort ist erforderlich")
                .withValidator(pass -> pass.length() >= 6, "Passwort muss mindestens 6 Zeichen haben")
                .bind(Benutzer::getPasswort, Benutzer::setPasswort);

        // Vor- und Nachname in einer Zeile mit flexiblem Layout
        HorizontalLayout nameLayout = new HorizontalLayout(vornameField, nachnameField);
        nameLayout.setWidthFull();
        nameLayout.setFlexGrow(1, vornameField, nachnameField);
        nameLayout.getStyle().set("flex-wrap", "wrap");
        nameLayout.getStyle().set("gap", "var(--lumo-space-m)");

        // Register-Button
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> registrieren());

        // Login-Link
        RouterLink loginLink = new RouterLink("Bereits registriert? Hier anmelden", LoginView.class);

        add(
            title,
            beschreibung,
            nameLayout,
            emailField,
            passwortField,
            passwortBestaetigenField,
            registerButton,
            loginLink
        );
    }

    /**
     * Führt die Registrierung durch.
     */
    private void registrieren() {
        try {
            // Passwörter vergleichen
            if (!passwortField.getValue().equals(passwortBestaetigenField.getValue())) {
                Notification.show("Die Passwörter stimmen nicht überein")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Benutzer validieren und erstellen
            Benutzer benutzer = new Benutzer();
            binder.writeBean(benutzer);

            // Registrierung durchführen
            benutzerService.registriereBenutzer(benutzer);

            Notification notification = Notification.show(
                "Registrierung erfolgreich! Sie können sich jetzt anmelden."
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);

            // Zur Login-Seite weiterleiten
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));

        } catch (ValidationException e) {
            Notification.show("Bitte überprüfen Sie Ihre Eingaben")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Fehler bei der Registrierung", e);
            Notification.show("Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
