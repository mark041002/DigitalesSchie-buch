package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import de.suchalla.schiessbuch.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

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
    private final EmailService emailService;

    private final TextField vornameField = new TextField("Vorname");
    private final TextField nachnameField = new TextField("Nachname");
    private final EmailField emailField = new EmailField("E-Mail-Adresse");
    private final PasswordField passwortField = new PasswordField("Passwort");
    private final PasswordField passwortBestaetigenField = new PasswordField("Passwort bestätigen");
    private final Button registerButton = new Button("Registrieren");

    private final Binder<Benutzer> binder = new BeanValidationBinder<>(Benutzer.class);

    public RegisterView(BenutzerService benutzerService, EmailService emailService) {
        this.benutzerService = benutzerService;
        this.emailService = emailService;

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

    private String getBaseUrl() {
        return "http://localhost:8000";
    }

    /**
     * Führt die Registrierung durch.
     */
    private void registrieren() {
        try {
            if (!passwortField.getValue().equals(passwortBestaetigenField.getValue())) {
                Notification.show("Die Passwörter stimmen nicht überein")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Benutzer benutzer = new Benutzer();
            binder.writeBean(benutzer);
            benutzer.setEmailVerifiziert(false);
            benutzerService.registriereBenutzer(benutzer);
            
            // Verifizierungslink generieren und E-Mail versenden (mit Fehlerbehandlung)
            String token = benutzerService.erstelleVerifizierungsToken(benutzer);
            String link = getBaseUrl() + "/email-verifizieren?token=" + token;
            
            try {
                Map<String, Object> vars = new HashMap<>();
                vars.put("username", benutzer.getVollstaendigerName());
                vars.put("verificationLink", link);
                emailService.sendMail(benutzer.getEmail(), "Digitales Schießbuch - E-Mail bestätigen", "verification.html", vars);
                log.info("Verifizierungs-E-Mail erfolgreich an {} versendet", benutzer.getEmail());
            } catch (Exception mailException) {
                log.error("Fehler beim Versenden der Verifizierungs-E-Mail an {}", benutzer.getEmail(), mailException);
                // E-Mail-Fehler blockieren nicht die Registrierung
            }
            
            // Dialog anzeigen und zur Homepage weiterleiten
            zeigeVerifizierungsDialog(benutzer.getEmail());

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

    /**
     * Zeigt einen Dialog mit Hinweis zur E-Mail-Verifizierung und leitet zur Homepage weiter.
     */
    private void zeigeVerifizierungsDialog(String email) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        // Dialog-Layout
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.CENTER);
        dialogLayout.getStyle().set("text-align", "center");

        // Icon
        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon.setSize("64px");
        successIcon.setColor("var(--lumo-success-color)");

        // Titel
        H2 titel = new H2("Registrierung erfolgreich!");
        titel.getStyle().set("margin-top", "0");

        // Beschreibung
        Paragraph beschreibung = new Paragraph(
            "Wir haben Ihnen eine E-Mail an " + email + " geschickt. " +
            "Bitte bestätigen Sie Ihre E-Mail-Adresse über den Link in der E-Mail, " +
            "bevor Sie sich anmelden können."
        );
        beschreibung.getStyle()
                .set("max-width", "500px")
                .set("margin", "var(--lumo-space-m) 0");

        // Hinweis
        Paragraph hinweis = new Paragraph(
            "Falls Sie keine E-Mail erhalten haben, überprüfen Sie bitte Ihren Spam-Ordner."
        );
        hinweis.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        // Button
        Button okButton = new Button("Zur Startseite", e -> {
            dialog.close();
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialogLayout.add(successIcon, titel, beschreibung, hinweis, okButton);
        dialog.add(dialogLayout);
        dialog.open();
    }
}
