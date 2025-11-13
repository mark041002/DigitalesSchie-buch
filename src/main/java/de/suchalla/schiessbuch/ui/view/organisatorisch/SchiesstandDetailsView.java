package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * View für Schießstandaufseher zur Bearbeitung von Schießstanddetails.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "schiesstand-details", layout = MainLayout.class)
@PageTitle("Schießstanddetails | Digitales Schießbuch")
@RolesAllowed({"SCHIESSSTAND_AUFSEHER", "ADMIN"})
@Slf4j
public class SchiesstandDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final DisziplinService disziplinService;
    private final VerbandService verbandService;
    private final Benutzer currentUser;

    private final TextField nameField = new TextField("Schießstandname");
    private final TextField adresseField = new TextField("Adresse");
    private final ComboBox<Verein> vereinComboBox = new ComboBox<>("Verein");

    private Schiesstand aktuellerSchiesstand;

    public SchiesstandDetailsView(SecurityService securityService,
                                  DisziplinService disziplinService,
                                  VerbandService verbandService) {
        this.disziplinService = disziplinService;
        this.verbandService = verbandService;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Überprüfe, ob ein Schießstand-Parameter übergeben wurde
        Optional<String> schiesstandIdParam = event.getLocation().getQueryParameters()
                .getParameters().getOrDefault("schiesstandId", List.of()).stream().findFirst();

        if (schiesstandIdParam.isPresent()) {
            try {
                Long schiesstandId = Long.parseLong(schiesstandIdParam.get());
                Optional<Schiesstand> schiesstandOptional = disziplinService.findeSchiesstand(schiesstandId);
                if (schiesstandOptional.isPresent()) {
                    aktuellerSchiesstand = schiesstandOptional.get();
                    ladeSchiesstanddaten();
                } else {
                    Notification.show("Schießstand nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (NumberFormatException e) {
                Notification.show("Ungültige Schießstand-ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            // Fallback: Lade den ersten Schießstand, bei dem der Benutzer Aufseher ist
            ladeErsterSchiesstand();
        }
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Schießstanddetails bearbeiten");
        title.getStyle().set("margin", "0");
        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling und 100% Breite
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();
        infoBox.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("border-left", "4px solid var(--lumo-primary-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        Paragraph description = new Paragraph(
                "Hier können Sie die Details Ihres Schießstands bearbeiten und verwalten. Änderungen werden sofort gespeichert."
        );
        description.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");
        infoBox.add(infoIcon, description);
        contentWrapper.add(infoBox);

        // Form-Container mit weißem Hintergrund und modernen Styles
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        formContainer.setWidthFull();
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("box-sizing", "border-box");

        // Formular
        FormLayout formLayout = new FormLayout();
        nameField.setRequired(true);
        nameField.setWidthFull();
        adresseField.setWidthFull();


        vereinComboBox.setItems(verbandService.findeAlleVereine());
        vereinComboBox.setItemLabelGenerator(Verein::getName);
        vereinComboBox.setPlaceholder("Verein auswählen...");
        vereinComboBox.setWidthFull();

        formLayout.add(nameField, adresseField, vereinComboBox);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(adresseField, 2);

        // Buttons mit Icons
        Button speichernButton = new Button("Speichern", e -> speichereSchiesstanddaten());
        speichernButton.setIcon(VaadinIcon.CHECK.create());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button abbrechenButton = new Button("Abbrechen", e -> ladeSchiesstanddaten());
        abbrechenButton.setIcon(VaadinIcon.CLOSE.create());

        HorizontalLayout buttonLayout = new HorizontalLayout(speichernButton, abbrechenButton);

        formContainer.add(formLayout, buttonLayout);
        contentWrapper.add(formContainer);
        add(contentWrapper);
    }

    /**
     * Lädt die Schießstanddaten.
     */
    private void ladeSchiesstanddaten() {
        if (aktuellerSchiesstand == null) {
            Notification.show("Kein Schießstand ausgewählt")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        nameField.setValue(aktuellerSchiesstand.getName() != null ? aktuellerSchiesstand.getName() : "");
        adresseField.setValue(aktuellerSchiesstand.getAdresse() != null ? aktuellerSchiesstand.getAdresse() : "");
        vereinComboBox.setValue(aktuellerSchiesstand.getVerein());

        // Verein-ComboBox nur bei vereinsgebundenen Schießständen anzeigen
        vereinComboBox.setVisible(aktuellerSchiesstand.getTyp() == SchiesstandTyp.VEREINSGEBUNDEN);
    }

    /**
     * Lädt den ersten Schießstand, bei dem der Benutzer Aufseher ist.
     */
    private void ladeErsterSchiesstand() {
        if (currentUser == null) {
            return;
        }

        List<Schiesstand> schiesstaende = disziplinService.findeAlleSchiesstaende();
        schiesstaende.stream()
                .filter(s -> s.getAufseher() != null && s.getAufseher().getId().equals(currentUser.getId()))
                .findFirst()
                .ifPresent(schiesstand -> {
                    aktuellerSchiesstand = schiesstand;
                    ladeSchiesstanddaten();
                });

        if (aktuellerSchiesstand == null) {
            Notification.show("Kein Schießstand gefunden, bei dem Sie Aufseher sind")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
        }
    }

    /**
     * Speichert die Schießstanddaten.
     */
    private void speichereSchiesstanddaten() {
        if (aktuellerSchiesstand == null) {
            Notification.show("Kein Schießstand ausgewählt")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (nameField.getValue().trim().isEmpty()) {
            Notification.show("Schießstandname darf nicht leer sein")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            aktuellerSchiesstand.setName(nameField.getValue());
            aktuellerSchiesstand.setAdresse(adresseField.getValue());
            aktuellerSchiesstand.setVerein(vereinComboBox.getValue());

            disziplinService.aktualisiereSchiesstand(aktuellerSchiesstand);

            Notification.show("Schießstanddaten erfolgreich gespeichert")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            log.info("Schießstanddaten aktualisiert für Schießstand: {}", aktuellerSchiesstand.getName());
        } catch (Exception e) {
            log.error("Fehler beim Speichern der Schießstanddaten", e);
            Notification.show("Fehler beim Speichern: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

}

