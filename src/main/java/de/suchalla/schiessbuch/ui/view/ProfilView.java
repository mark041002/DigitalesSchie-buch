package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.BenutzerService;
import jakarta.annotation.security.PermitAll;

/**
 * View für Benutzerprofil.
 *
 * Layout-Änderung: E-Mail und Passwort nebeneinander unter Profilinformationen.
 *
 * @author Markus Suchalla
 * @version 1.0.2 (responsive)
 */
@Route(value = "profil", layout = MainLayout.class)
@PageTitle("Profil | Digitales Schießbuch")
@PermitAll
public class ProfilView extends VerticalLayout {

    private final SecurityService securityService;
    private final BenutzerService benutzerService;

    private final EmailField emailField = new EmailField("E-Mail");
    private final PasswordField neuesPasswortField = new PasswordField("Neues Passwort");
    private final PasswordField passwortBestaetigenField = new PasswordField("Passwort bestätigen");

    private final Benutzer currentUser;

    public ProfilView(SecurityService securityService, BenutzerService benutzerService) {
        this.securityService = securityService;
        this.benutzerService = benutzerService;

        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        setMaxWidth("1400px");
        getStyle().set("margin", "0 auto")
                .set("padding", "var(--lumo-space-m)");

        createContent();
    }

    private void createContent() {
        if (currentUser == null) {
            return;
        }

        // Header
        H2 title = new H2("Mein Profil");
        title.getStyle()
                .set("margin-top", "var(--lumo-space-l)")
                .set("margin-bottom", "var(--lumo-space-m)");
        add(title);

        // Profilinformationen (oben, volle Breite)
        Div infoCard = createInfoCard();
        infoCard.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        infoCard.setWidthFull();
        add(infoCard);

        // Row: E-Mail und Passwort nebeneinander (auf kleinen Bildschirmen umbruch)
        FlexLayout emailPassRow = new FlexLayout();
        emailPassRow.setWidthFull();
        emailPassRow.setFlexDirection(FlexDirection.ROW);
        emailPassRow.setFlexWrap(FlexWrap.WRAP);
        emailPassRow.getStyle().set("gap", "var(--lumo-space-m)");

        Div emailCard = createEmailCard();
        emailCard.getStyle().set("flex", "1 1 320px").set("min-width", "260px");
        emailCard.setWidthFull();

        Div passwortCard = createPasswortCard();
        passwortCard.getStyle().set("flex", "1 1 320px").set("min-width", "260px");
        passwortCard.setWidthFull();

        emailPassRow.add(emailCard, passwortCard);
        // Beide Spalten dürfen gleichmäßig wachsen
        emailPassRow.setFlexGrow(1, emailCard);
        emailPassRow.setFlexGrow(1, passwortCard);

        add(emailPassRow);

        // Gefahrenzone Card (volle Breite unter den Feldern)
        Div dangerCard = createDangerCard();
        dangerCard.getStyle().set("margin-top", "var(--lumo-space-m)");
        dangerCard.setWidthFull();
        add(dangerCard);
    }

    private Div createInfoCard() {
        Div card = new Div();
        card.addClassName("card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)");

        H3 cardTitle = new H3("Benutzerinformationen");
        cardTitle.getStyle().set("margin-top", "0");

        Div infoGrid = new Div();
        infoGrid.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)");

        infoGrid.add(
                createInfoItem("Name", currentUser.getVollstaendigerName(), VaadinIcon.USER),
                createInfoItem("Rolle", getRollenText(currentUser.getRolle().toString()), VaadinIcon.DIPLOMA),
                createInfoItem("E-Mail", currentUser.getEmail(), VaadinIcon.ENVELOPE)
        );

        card.add(cardTitle, infoGrid);
        return card;
    }

    private Div createInfoItem(String label, String value, VaadinIcon icon) {
        Div item = new Div();
        item.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)");

        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", "var(--lumo-primary-color)");

        Div textDiv = new Div();
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("display", "block");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "500")
                .set("display", "block");

        textDiv.add(labelSpan, valueSpan);
        item.add(iconSpan, textDiv);
        return item;
    }

    private Div createEmailCard() {
        Div card = new Div();
        card.addClassName("card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)")
                .set("height", "100%");

        H3 cardTitle = new H3("E-Mail ändern");
        cardTitle.getStyle().set("margin-top", "0");

        emailField.setValue(currentUser.getEmail());
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());

        Button speichernButton = new Button("E-Mail speichern", e -> aendereEmail());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        speichernButton.setIcon(VaadinIcon.CHECK.create());
        speichernButton.setWidthFull();

        card.add(cardTitle, emailField, speichernButton);
        return card;
    }

    private Div createPasswortCard() {
        Div card = new Div();
        card.addClassName("card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)");

        H3 cardTitle = new H3("Passwort ändern");
        cardTitle.getStyle().set("margin-top", "0");

        neuesPasswortField.setWidthFull();
        neuesPasswortField.setPrefixComponent(VaadinIcon.LOCK.create());
        neuesPasswortField.setHelperText("Mindestens 6 Zeichen");

        passwortBestaetigenField.setWidthFull();
        passwortBestaetigenField.setPrefixComponent(VaadinIcon.LOCK.create());

        Button passwortButton = new Button("Passwort ändern", e -> aenderePasswort());
        passwortButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        passwortButton.setIcon(VaadinIcon.KEY.create());
        passwortButton.setWidthFull();

        card.add(cardTitle, neuesPasswortField, passwortBestaetigenField, passwortButton);
        return card;
    }

    private Div createDangerCard() {
        Div card = new Div();
        card.addClassName("card");
        card.getStyle()
                .set("background", "var(--lumo-error-color-10pct)")
                .set("border", "1px solid var(--lumo-error-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)");

        H3 cardTitle = new H3("Gefahrenzone");
        cardTitle.getStyle()
                .set("margin-top", "0")
                .set("color", "var(--lumo-error-text-color)");

        Span warnung = new Span("Account unwiderruflich löschen. Diese Aktion kann nicht rückgängig gemacht werden.");
        warnung.getStyle()
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-error-text-color)");

        Button loeschenButton = new Button("Account löschen", e -> loescheAccount());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        loeschenButton.setIcon(VaadinIcon.TRASH.create());
        loeschenButton.setWidthFull();

        card.add(cardTitle, warnung, loeschenButton);
        return card;
    }

    private String getRollenText(String rolle) {
        return switch (rolle) {
            case "SCHUETZE" -> "Schütze";
            case "AUFSEHER" -> "Aufseher";
            case "VEREINS_CHEF" -> "Vereinschef";
            case "VEREINS_ADMIN" -> "Vereins-Admin";
            case "SOFTWARE_ADMIN" -> "Administrator";
            default -> rolle;
        };
    }

    private void aendereEmail() {
        try {
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
        } catch (Exception e) {
            Notification.show("Fehler: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
                benutzerService.loescheBenutzer(currentUser.getId());
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