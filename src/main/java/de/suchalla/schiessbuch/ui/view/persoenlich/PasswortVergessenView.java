package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.suchalla.schiessbuch.service.email.EmailService;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.service.BenutzerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Route("passwort-vergessen")
@PageTitle("Passwort vergessen | Digitales Schießbuch")
@AnonymousAllowed
@Slf4j
public class PasswortVergessenView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final BenutzerRepository benutzerRepository;
    private final EmailService emailService;

    private final EmailField emailField = new EmailField("E-Mail-Adresse");
    private final Button sendenButton = new Button("Link zum Zurücksetzen senden");

    @Autowired
    public PasswortVergessenView(BenutzerService benutzerService, BenutzerRepository benutzerRepository, EmailService emailService) {
        this.benutzerService = benutzerService;
        this.benutzerRepository = benutzerRepository;
        this.emailService = emailService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setMaxWidth("500px");
        getStyle().set("margin", "0 auto");

        createForm();
    }

    private void createForm() {
        H1 title = new H1("Passwort vergessen");

        Paragraph beschreibung = new Paragraph(
            "Geben Sie Ihre E-Mail-Adresse ein und wir senden Ihnen einen Link zum Zurücksetzen Ihres Passworts."
        );

        emailField.setRequired(true);
        emailField.setWidthFull();
        emailField.setErrorMessage("Bitte geben Sie eine gültige E-Mail-Adresse ein");

        sendenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendenButton.setWidthFull();
        sendenButton.addClickListener(e -> sendeResetLink());

        RouterLink loginLink = new RouterLink("Zurück zur Anmeldung", LoginView.class);

        add(title, beschreibung, emailField, sendenButton, loginLink);
    }

    private String getBaseUrl() {
        return "http://localhost:8000";
    }

    private void sendeResetLink() {
        String email = emailField.getValue();
        if (email == null || email.trim().isEmpty()) {
            Notification.show("Bitte geben Sie Ihre E-Mail-Adresse ein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Benutzer suchen
            Benutzer benutzer = benutzerRepository.findByEmail(email).orElse(null);

            // Aus Sicherheitsgründen immer eine Erfolgsmeldung anzeigen, auch wenn die E-Mail nicht existiert
            if (benutzer != null) {
                String token = benutzerService.erstellePasswortResetToken(benutzer);
                String baseUrl = getBaseUrl();
                String resetLink = baseUrl + "/passwort-reset?token=" + token;

                Map<String, Object> vars = new HashMap<>();
                vars.put("username", benutzer.getVollstaendigerName());
                vars.put("resetLink", resetLink);

                emailService.sendMail(benutzer.getEmail(), "Passwort zurücksetzen", "password-reset.html", vars);
                log.info("Passwort-Reset-Link an {} gesendet", email);
            }

            Notification notification = Notification.show(
                "Falls ein Konto mit dieser E-Mail-Adresse existiert, wurde ein Link zum Zurücksetzen des Passworts gesendet."
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(5000);

            // Formular leeren
            emailField.clear();

        } catch (Exception e) {
            log.error("Fehler beim Senden des Passwort-Reset-Links", e);
            Notification.show("Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
