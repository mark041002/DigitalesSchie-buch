package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.PermitAll;

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
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

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
        contentWrapper.setMaxWidth("1000px");

        // Header-Bereich mit modernem Styling
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Mein Profil");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("margin-bottom", "var(--lumo-space-l)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        com.vaadin.flow.component.html.Paragraph beschreibung = new com.vaadin.flow.component.html.Paragraph(
                "Verwalten Sie Ihre persönlichen Daten und Sicherheitseinstellungen."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");
        infoBox.add(infoIcon, beschreibung);
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
        Div card = new Div();
        card.addClassName("form-container");
        card.setWidthFull();

        H3 cardTitle = new H3("Benutzerinformationen");
        cardTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-header-text-color)")
                .set("font-weight", "600");

        card.add(cardTitle, createNameRow(card), createEmailRow(card));
        return card;
    }

    private Div createNameRow(Div card) {
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
                card.removeAll();
                H3 title = new H3("Benutzerinformationen");
                title.getStyle()
                        .set("margin-top", "0")
                        .set("margin-bottom", "var(--lumo-space-m)")
                        .set("color", "var(--lumo-header-text-color)")
                        .set("font-weight", "600");
                card.add(title, createNameRow(card), createEmailRow(card));
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
                currentUser.setVorname(neuerName.split(" ")[0]);
                if (neuerName.split(" ").length > 1) {
                    currentUser.setNachname(neuerName.substring(neuerName.indexOf(' ') + 1));
                }
                benutzerService.aktualisiereBenutzer(currentUser);
                Notification.show("Name erfolgreich geändert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                nameEditMode = false;
                int idx = getChildren().toList().indexOf(infoCard);
                remove(infoCard);
                infoCard = createInfoCard();
                infoCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
                addComponentAtIndex(idx, infoCard);
            });
            speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            speichern.getStyle().set("flex-shrink", "0");

            nameRow.add(nameField, speichern);
        }
        return nameRow;
    }

    private Div createEmailRow(Div card) {
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
                card.removeAll();
                H3 title = new H3("Benutzerinformationen");
                title.getStyle()
                        .set("margin-top", "0")
                        .set("margin-bottom", "var(--lumo-space-m)")
                        .set("color", "var(--lumo-header-text-color)")
                        .set("font-weight", "600");
                card.add(title, createNameRow(card), createEmailRow(card));
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
                currentUser.setEmail(neueEmail);
                benutzerService.aktualisiereBenutzer(currentUser);
                Notification.show("E-Mail erfolgreich geändert")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                emailEditMode = false;
                int idx = getChildren().toList().indexOf(infoCard);
                remove(infoCard);
                infoCard = createInfoCard();
                infoCard.getStyle().set("margin-bottom", "var(--lumo-space-l)");
                addComponentAtIndex(idx, infoCard);
            });
            speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            speichern.getStyle().set("flex-shrink", "0");

            emailRow.add(emailField, speichern);
        }
        return emailRow;
    }

    private Div createPasswortCard() {
        Div card = new Div();
        card.addClassName("form-container");
        card.setWidthFull();

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
