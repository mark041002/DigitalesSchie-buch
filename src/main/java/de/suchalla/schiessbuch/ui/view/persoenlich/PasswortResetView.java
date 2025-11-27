package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.suchalla.schiessbuch.service.BenutzerService;
import lombok.extern.slf4j.Slf4j;

@Route("passwort-reset")
@PageTitle("Passwort zurücksetzen | Digitales Schießbuch")
@AnonymousAllowed
@Slf4j
public class PasswortResetView extends VerticalLayout {
    private final BenutzerService benutzerService;

    private final PasswordField neuesPasswort = new PasswordField("Neues Passwort");
    private final PasswordField bestaetigenPasswort = new PasswordField("Passwort bestätigen");
    private final Button resetButton = new Button("Passwort ändern");

    public PasswortResetView(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setMaxWidth("500px");
        getStyle().set("margin", "0 auto");

        createForm();
    }

    private void createForm() {
        H1 title = new H1("Passwort zurücksetzen");

        Paragraph beschreibung = new Paragraph(
            "Geben Sie ein neues Passwort ein."
        );

        neuesPasswort.setRequired(true);
        neuesPasswort.setMinLength(6);
        neuesPasswort.setHelperText("Mindestens 6 Zeichen");
        neuesPasswort.setWidthFull();

        bestaetigenPasswort.setRequired(true);
        bestaetigenPasswort.setWidthFull();

        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resetButton.setWidthFull();
        resetButton.addClickListener(e -> resetPasswort());

        add(title, beschreibung, neuesPasswort, bestaetigenPasswort, resetButton);
    }

    private void resetPasswort() {
        String token = getTokenFromUrl();

        if (token == null || token.isEmpty()) {
            Notification.show("Ungültiger Link. Bitte fordern Sie einen neuen Link an.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (neuesPasswort.getValue() == null || neuesPasswort.getValue().length() < 6) {
            Notification.show("Passwort muss mindestens 6 Zeichen lang sein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!neuesPasswort.getValue().equals(bestaetigenPasswort.getValue())) {
            Notification.show("Passwörter stimmen nicht überein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Die Methode resetPasswortMitToken verwendet jetzt UserToken
            boolean success = benutzerService.resetPasswortMitToken(token, neuesPasswort.getValue());
            if (success) {
                Notification notification = Notification.show("Passwort erfolgreich geändert. Sie können sich jetzt anmelden.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);

                // Zur Login-Seite weiterleiten
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            } else {
                Notification.show("Token ungültig oder abgelaufen. Bitte fordern Sie einen neuen Link an.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Fehler beim Passwort-Reset", e);
            Notification.show("Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getTokenFromUrl() {
        if (getUI().isPresent()) {
            QueryParameters params = getUI().get().getInternals().getActiveViewLocation().getQueryParameters();
            return params.getParameters().getOrDefault("token", java.util.Collections.singletonList("")).get(0);
        }
        return "";
    }
}
