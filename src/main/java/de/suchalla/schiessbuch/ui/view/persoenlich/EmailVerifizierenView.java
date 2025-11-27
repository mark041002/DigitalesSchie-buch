package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.suchalla.schiessbuch.service.BenutzerService;
import lombok.extern.slf4j.Slf4j;

@Route("email-verifizieren")
@PageTitle("E-Mail Verifizierung | Digitales Schießbuch")
@AnonymousAllowed
@Slf4j
public class EmailVerifizierenView extends VerticalLayout {
    private final BenutzerService benutzerService;

    public EmailVerifizierenView(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setMaxWidth("500px");
        getStyle().set("margin", "0 auto");

        createForm();
    }

    private void createForm() {
        H1 title = new H1("E-Mail Verifizierung");
        add(title);

        Paragraph beschreibung = new Paragraph("Klicken Sie auf den Button, um Ihre E-Mail-Adresse zu bestätigen.");
        add(beschreibung);

        com.vaadin.flow.component.button.Button verifyButton = new com.vaadin.flow.component.button.Button("E-Mail bestätigen");
        verifyButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        verifyButton.addClickListener(e -> verifiziereToken());

        add(verifyButton);
    }

    private void verifiziereToken() {
        String token = getTokenFromUrl();
        if (token == null || token.isEmpty()) {
            Notification.show("Ungültiger Verifizierungslink.").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            boolean success = benutzerService.bestaetigeEmail(token);
            if (success) {
                // Dialog anzeigen und nach kurzer Zeit zur Startseite weiterleiten
                Dialog dialog = new Dialog();
                dialog.setCloseOnEsc(false);
                dialog.setCloseOnOutsideClick(false);
                dialog.add(new Paragraph("E-Mail erfolgreich bestätigt. Sie werden zur Startseite weitergeleitet."));
                Button ok = new Button("Zur Startseite", e -> {
                    dialog.close();
                    getUI().ifPresent(ui -> ui.navigate("/"));
                });
                ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                dialog.add(ok);
                dialog.open();
            } else {
                Notification.show("Verifizierungslink ungültig oder abgelaufen.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            log.error("Fehler bei der E-Mail-Verifizierung", e);
            Notification.show("Ein Fehler ist aufgetreten.").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String getTokenFromUrl() {
        if (getUI().isPresent()) {
            try {
                return getUI().get().getInternals().getActiveViewLocation().getQueryParameters()
                        .getParameters().getOrDefault("token", java.util.Collections.singletonList("")).get(0);
            } catch (Exception e) {
                log.warn("Token aus URL konnte nicht gelesen werden: {}", e.getMessage());
                return "";
            }
        }
        return "";
    }
}
