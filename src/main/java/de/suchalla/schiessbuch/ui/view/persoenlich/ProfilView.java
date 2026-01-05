package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.ui.component.ViewComponentHelper;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import java.util.Optional;

/**
 * View für Benutzerprofil.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@Route(value = "profil", layout = MainLayout.class)
@PageTitle("Profil | Digitales Schießbuch")
@PermitAll
public class ProfilView extends VerticalLayout {

    private final SecurityService securityService;
    private final BenutzerService benutzerService;

    private final PasswordField neuesPasswortField = new PasswordField("Neues Passwort");
    private final PasswordField passwortBestaetigenField = new PasswordField("Passwort bestätigen");

    private final Benutzer currentUser;

    private boolean nameEditMode = false;
    private boolean emailEditMode = false;

    private Div infoCard;

    public ProfilView(SecurityService securityService, BenutzerService benutzerService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
    }

    private void createContent() {
        if (currentUser == null) {
            return;
        }

        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        Div header = ViewComponentHelper.createGradientHeader("Mein Profil");
        contentWrapper.add(header);

        Div infoBox = ViewComponentHelper.createInfoBox("Verwalten Sie Ihre persönlichen Daten und Sicherheitseinstellungen.");
        infoBox.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        contentWrapper.add(infoBox);

        // Profilinformationen
        infoCard = createInfoCard();
        infoCard.setWidthFull();
        infoCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        contentWrapper.add(infoCard);

        // Passwort-ändern-Card
        Div passwortCard = createPasswortCard();
        passwortCard.setWidthFull();
        passwortCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        contentWrapper.add(passwortCard);

        // Account löschen Card
        Div dangerCard = createDangerCard();
        dangerCard.setWidthFull();
        contentWrapper.add(dangerCard);

        add(contentWrapper);
    }

    private Div createInfoCard() {
        Div card = ViewComponentHelper.createFormContainer();

        H3 cardTitle = new H3("Benutzerinformationen");
        cardTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-weight", "600");

        // Name und E-Mail sowie die Benachrichtigungs-Div (unterhalb der E-Mail) werden in createEmailRow erzeugt
        card.add(cardTitle, createNameRow(), createEmailRow(card));
        return card;
    }

    // Hilfsmethode: Info-Card erneuern (bekommt aktuellen nameEditMode/emailEditMode Zustand)
    private void refreshInfoCard() {
        if (infoCard == null) {
            return;
        }

        Optional<Component> parentOpt = infoCard.getParent();
        if (parentOpt.isPresent() && parentOpt.get() instanceof HasComponents) {
            Component parentComp = parentOpt.get();
            int idx = parentComp.getChildren().toList().indexOf(infoCard);
            if (idx < 0) idx = 0;
            HasComponents parent = (HasComponents) parentComp;
            parent.remove(infoCard);
            infoCard = createInfoCard();
            infoCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
            int childCount = parentComp.getChildren().toList().size();
            if (idx <= childCount) {
                parent.addComponentAtIndex(idx, infoCard);
            } else {
                parent.add(infoCard);
            }
        } else {
            int idx = getChildren().toList().indexOf(infoCard);
            if (idx < 0) idx = 0;
            remove(infoCard);
            infoCard = createInfoCard();
            infoCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
            addComponentAtIndex(idx, infoCard);
        }
    }

    private Div createNameRow() {
        Div nameRow = new Div();
        nameRow.getStyle()
                .set("display", "flex")
                .set("gap", "var(--lumo-space-m)")
                .set("align-items", "center")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        if (!nameEditMode) {
            Div nameContainer = new Div();
            nameContainer.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("gap", "var(--lumo-space-s)")
                    .set("flex-grow", "1");

            Icon userIcon = VaadinIcon.USER.create();
            userIcon.setSize("20px");
            userIcon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("flex-shrink", "0");

            Span nameText = new Span(currentUser.getVollstaendigerName());
            nameText.getStyle()
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)");

            nameContainer.add(userIcon, nameText);

            Button bearbeiten = new Button("Bearbeiten", VaadinIcon.EDIT.create());
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> {
                nameEditMode = true;
                refreshInfoCard();
            });

            nameRow.add(nameContainer, bearbeiten);
        } else {
            TextField nameField = new TextField();
            nameField.setValue(currentUser.getVollstaendigerName());
            nameField.setWidthFull();
            nameField.getStyle().set("flex-grow", "1");

            Button speichern = new Button("Speichern", e -> {
                String neuerName = nameField.getValue();
                if (neuerName == null || neuerName.trim().isEmpty()) {
                    Notification.show("Bitte geben Sie einen Namen ein")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                // Lade Benutzer neu aus DB, um Detached-State-Fehler zu vermeiden
                Benutzer benutzerToUpdate = benutzerService.findeBenutzerByEmailWithMitgliedschaften(currentUser.getEmail());
                if (benutzerToUpdate == null) {
                    Notification.show("Benutzer nicht gefunden")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                benutzerToUpdate.setVorname(neuerName.split(" ")[0]);
                if (neuerName.split(" ").length > 1) {
                    benutzerToUpdate.setNachname(neuerName.substring(neuerName.indexOf(' ') + 1));
                }
                benutzerService.aktualisiereBenutzer(benutzerToUpdate);
                currentUser.setVorname(benutzerToUpdate.getVorname());
                currentUser.setNachname(benutzerToUpdate.getNachname());
                Notification.show("Name erfolgreich geändert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                nameEditMode = false;
                // Aktualisiere Header-Label (MainLayout) oder lade Seite neu als Fallback
                try {
                    getUI().ifPresent(ui -> {
                        boolean updated = ui.getChildren()
                                .filter(c -> c instanceof MainLayout)
                                .findFirst()
                                .map(c -> {
                                    ((MainLayout) c).updateUsername(benutzerToUpdate.getVollstaendigerName());
                                    return true;
                                }).orElse(false);

                        if (!updated) {
                            ui.getPage().reload();
                        }
                    });
                } catch (Exception ignored) {
                    // Falls irgendwas schiefgeht, reload als Sicherheit
                    getUI().ifPresent(ui -> ui.getPage().reload());
                }
                refreshInfoCard();
            });
            speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            speichern.getStyle().set("flex-shrink", "0");

            nameRow.add(nameField, speichern);
        }
        return nameRow;
    }

    private Div createEmailRow(Div card) {
        Div container = new Div();
        container.setWidthFull();

        Div emailRow = new Div();
        emailRow.getStyle()
                .set("display", "flex")
                .set("gap", "var(--lumo-space-m)")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        if (!emailEditMode) {
            Div emailContainer = new Div();
            emailContainer.getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("gap", "var(--lumo-space-s)")
                    .set("flex-grow", "1");

            Icon emailIcon = VaadinIcon.ENVELOPE.create();
            emailIcon.setSize("20px");
            emailIcon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("flex-shrink", "0");

            Span emailText = new Span(currentUser.getEmail());
            emailText.getStyle()
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)");

            emailContainer.add(emailIcon, emailText);

            Button bearbeiten = new Button("Bearbeiten", VaadinIcon.EDIT.create());
            bearbeiten.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            bearbeiten.addClickListener(e -> {
                emailEditMode = true;
                refreshInfoCard();
            });

            emailRow.add(emailContainer, bearbeiten);
        } else {
            EmailField emailField = new EmailField();
            emailField.setValue(currentUser.getEmail());
            emailField.setWidthFull();
            emailField.getStyle().set("flex-grow", "1");

            Button speichern = new Button("Speichern", e -> {
                String neueEmail = emailField.getValue();
                if (neueEmail == null || neueEmail.trim().isEmpty()) {
                    Notification.show("Bitte geben Sie eine E-Mail-Adresse ein")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                // Lade Benutzer neu aus DB, um Detached-State-Fehler zu vermeiden
                Benutzer benutzerToUpdate = benutzerService.findeBenutzerByEmailWithMitgliedschaften(currentUser.getEmail());
                if (benutzerToUpdate == null) {
                    Notification.show("Benutzer nicht gefunden")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                benutzerToUpdate.setEmail(neueEmail);
                benutzerService.aktualisiereBenutzer(benutzerToUpdate);
                // Aktualisiere auch das lokale currentUser-Objekt
                currentUser.setEmail(benutzerToUpdate.getEmail());
                // Aktualisiere die Authentication, damit der Benutzer eingeloggt bleibt
                try {
                    securityService.refreshAuthentication(benutzerToUpdate);
                } catch (Exception ex) {
                    // Falls Aktualisierung fehlschlägt, fallback: einfach melden, aber nicht automatisch ausloggen
                    Notification.show("Warnung: Anmeldung konnte nicht aktualisiert werden. Bitte neu einloggen.")
                            .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                }
                Notification.show("E-Mail erfolgreich geändert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                emailEditMode = false;
                refreshInfoCard();
            });
            speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            speichern.getStyle().set("flex-shrink", "0");

            emailRow.add(emailField, speichern);
        }

        Div notifRow = new Div();
        notifRow.getStyle()
                .set("display", "flex")
                .set("gap", "var(--lumo-space-m)")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-s)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-top", "var(--lumo-space-s)");

        Div notifContent = new Div();
        notifContent.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)")
                .set("flex-grow", "1");

        Icon notifIcon = VaadinIcon.BELL.create();
        notifIcon.setSize("20px");
        notifIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("flex-shrink", "0");

        Checkbox cbEmailNotifications = new Checkbox("E-Mail-Benachrichtigungen aktivieren");
        try {
            cbEmailNotifications.setValue(Boolean.TRUE.equals(currentUser.isEmailNotificationsEnabled()));
        } catch (Exception ignored) { cbEmailNotifications.setValue(true); }

        notifContent.add(notifIcon, cbEmailNotifications);

        Button savePrefs = new Button("Einstellung speichern", e -> {
            // Lade Benutzer neu aus DB, um Detached-State-Fehler zu vermeiden
            Benutzer benutzerToUpdate = benutzerService.findeBenutzerByEmailWithMitgliedschaften(currentUser.getEmail());
            if (benutzerToUpdate == null) {
                Notification.show("Benutzer nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            benutzerToUpdate.setEmailNotificationsEnabled(cbEmailNotifications.getValue());
            try {
                benutzerService.aktualisiereBenutzer(benutzerToUpdate);
                // Aktualisiere auch das lokale currentUser-Objekt
                currentUser.setEmailNotificationsEnabled(benutzerToUpdate.isEmailNotificationsEnabled());
                Notification.show("Einstellungen gespeichert").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Fehler beim Speichern: " + ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            // nach dem Speichern Ansicht aktualisieren, damit Status reflektiert wird
            refreshInfoCard();
        });
        savePrefs.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        savePrefs.getStyle().set("flex-shrink", "0");

        notifRow.add(notifContent, savePrefs);

        container.add(emailRow, notifRow);
        return container;
    }

    private Div createPasswortCard() {
        Div card = ViewComponentHelper.createFormContainer();

        H3 cardTitle = new H3("Passwort ändern");
        cardTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-weight", "600");

        neuesPasswortField.setWidthFull();
        neuesPasswortField.setPrefixComponent(VaadinIcon.LOCK.create());
        neuesPasswortField.setHelperText("Mindestens 6 Zeichen");

        passwortBestaetigenField.setWidthFull();
        passwortBestaetigenField.setPrefixComponent(VaadinIcon.LOCK.create());

        Button passwortButton = new Button("Passwort ändern", VaadinIcon.KEY.create());
        passwortButton.addClickListener(e -> aenderePasswort());
        passwortButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        passwortButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        card.add(cardTitle, neuesPasswortField, passwortBestaetigenField, passwortButton);
        return card;
    }

    private Div createDangerCard() {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("border", "2px solid var(--lumo-error-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box")
                .set("overflow-wrap", "break-word");

        H3 cardTitle = new H3("Account permanent löschen");
        cardTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("color", "var(--lumo-error-text-color)")
                .set("font-weight", "700");

        Span warnung = new Span("Account unwiderruflich löschen. Diese Aktion kann nicht rückgängig gemacht werden.");
        warnung.getStyle()
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-error-text-color)")
                .set("font-weight", "500")
                .set("word-wrap", "break-word")
                .set("overflow-wrap", "break-word")
                .set("max-width", "100%");

        Button loeschenButton = new Button("Account löschen", VaadinIcon.TRASH.create());
        loeschenButton.addClickListener(e -> loescheAccount());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        card.add(cardTitle, warnung, loeschenButton);
        return card;
    }

    private void aenderePasswort() {
        String neuesPasswort = neuesPasswortField.getValue();
        String passwortBestaetigung = passwortBestaetigenField.getValue();

        if (neuesPasswort == null || neuesPasswort.isEmpty()) {
            Notification.show("Bitte geben Sie ein neues Passwort ein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (neuesPasswort.length() < 6) {
            Notification.show("Passwort muss mindestens 6 Zeichen lang sein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!neuesPasswort.equals(passwortBestaetigung)) {
            Notification.show("Passwörter stimmen nicht überein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            benutzerService.aenderePasswortOhneAltes(currentUser.getId(), neuesPasswort);
            Notification.show("Passwort erfolgreich geändert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            neuesPasswortField.clear();
            passwortBestaetigenField.clear();
            // Nach Passwortänderung Authentication aktualisieren, damit Session gültig bleibt
            try {
                Benutzer updated = benutzerService.findeBenutzerByEmailWithMitgliedschaften(currentUser.getEmail());
                if (updated != null) {
                    securityService.refreshAuthentication(updated);
                    // Synchronisiere das lokale currentUser-Objekt
                    currentUser.setPasswort(updated.getPasswort());
                }
            } catch (Exception ignored) { }
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loescheAccount() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Account wirklich löschen?");
        dialog.setText("Diese Aktion kann nicht rückgängig gemacht werden. Alle Ihre Daten werden unwiderruflich gelöscht.");

        dialog.setCancelable(true);
        dialog.setCancelText("Abbrechen");

        dialog.setConfirmText("Ja, Account löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            try {
                benutzerService.loescheBenutzer(currentUser);
                Notification.show("Account wurde gelöscht. Sie werden abgemeldet.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                securityService.logout();
            } catch (Exception e) {
                Notification.show("Fehler: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }
}
