package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VereinService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

/**
 * View für Vereinschefs zur Bearbeitung von Vereinsdetails.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "verein-details", layout = MainLayout.class)
@PageTitle("Vereinsdetails | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "VEREINS_ADMIN", "ADMIN"})
@Slf4j
public class VereinDetailsView extends VerticalLayout {

    private final VereinService vereinService;
    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final Benutzer currentUser;

    private final TextField nameField = new TextField("Vereinsname");
    private final TextField vereinsNummerField = new TextField("Vereinsnummer");
    private final TextField adresseField = new TextField("Adresse");
    private final TextArea beschreibungField = new TextArea("Beschreibung");

    private Verein aktuellerVerein;

    public VereinDetailsView(SecurityService securityService,
                             VereinService vereinService,
                             VereinsmitgliedschaftService mitgliedschaftService) {
        this.vereinService = vereinService;
        this.mitgliedschaftService = mitgliedschaftService;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(true);
        setPadding(true);

        createContent();
        ladeVereinsdaten();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Vereinsdetails bearbeiten"));

        // Formular
        FormLayout formLayout = new FormLayout();

        nameField.setRequired(true);
        nameField.setWidthFull();

        vereinsNummerField.setWidthFull();

        adresseField.setWidthFull();

        beschreibungField.setWidthFull();
        beschreibungField.setHeight("150px");

        formLayout.add(nameField, vereinsNummerField, adresseField, beschreibungField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(adresseField, 2);
        formLayout.setColspan(beschreibungField, 2);

        // Buttons
        Button speichernButton = new Button("Speichern", e -> speichereVereinsdaten());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button abbrechenButton = new Button("Abbrechen", e -> ladeVereinsdaten());

        HorizontalLayout buttonLayout = new HorizontalLayout(speichernButton, abbrechenButton);

        add(formLayout, buttonLayout);
    }

    /**
     * Lädt die Vereinsdaten.
     */
    private void ladeVereinsdaten() {
        if (currentUser == null) {
            return;
        }

        // Lade den ersten Verein, bei dem der Benutzer Vereinschef ist
        // In einer echten Anwendung würde man hier eine Auswahl anbieten
        mitgliedschaftService.findeMitgliedschaften(currentUser).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                .findFirst()
                .ifPresent(mitgliedschaft -> {
                    aktuellerVerein = mitgliedschaft.getVerein();
                    nameField.setValue(aktuellerVerein.getName() != null ? aktuellerVerein.getName() : "");
                    vereinsNummerField.setValue(aktuellerVerein.getVereinsNummer() != null ?
                            aktuellerVerein.getVereinsNummer() : "");
                    adresseField.setValue(aktuellerVerein.getAdresse() != null ? aktuellerVerein.getAdresse() : "");
                    beschreibungField.setValue(aktuellerVerein.getBeschreibung() != null ?
                            aktuellerVerein.getBeschreibung() : "");
                });

        if (aktuellerVerein == null) {
            Notification.show("Kein Verein gefunden, bei dem Sie Vereinschef sind")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    /**
     * Speichert die Vereinsdaten.
     */
    private void speichereVereinsdaten() {
        if (aktuellerVerein == null) {
            Notification.show("Kein Verein ausgewählt")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (nameField.getValue().trim().isEmpty()) {
            Notification.show("Vereinsname darf nicht leer sein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            aktuellerVerein.setName(nameField.getValue());
            aktuellerVerein.setVereinsNummer(vereinsNummerField.getValue());
            aktuellerVerein.setAdresse(adresseField.getValue());
            aktuellerVerein.setBeschreibung(beschreibungField.getValue());

            vereinService.aktualisiereVerein(aktuellerVerein);

            Notification.show("Vereinsdaten erfolgreich gespeichert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            log.info("Vereinsdaten aktualisiert für Verein: {}", aktuellerVerein.getName());
        } catch (Exception e) {
            log.error("Fehler beim Speichern der Vereinsdaten", e);
            Notification.show("Fehler beim Speichern: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}

