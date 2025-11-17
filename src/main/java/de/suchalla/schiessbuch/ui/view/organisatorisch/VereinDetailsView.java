package de.suchalla.schiessbuch.ui.view.organisatorisch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.VereinService;
import de.suchalla.schiessbuch.service.VerbandService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * View für Vereinschefs zur Bearbeitung von Vereinsdetails.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "verein-details", layout = MainLayout.class)
@PageTitle("Vereinsdetails | Digitales Schießbuch")
@RolesAllowed({"VEREINS_CHEF", "ADMIN"})
@Slf4j
public class VereinDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final VereinService vereinService;
    private final VerbandService verbandService;
    private final VereinsmitgliedschaftService mitgliedschaftService;
    private final VereinRepository vereinRepository;
    private final Benutzer currentUser;

    private final TextField nameField = new TextField("Vereinsname");
    private final TextField vereinsNummerField = new TextField("Vereinsnummer");
    private final TextField adresseField = new TextField("Adresse");
    private final TextArea beschreibungField = new TextArea("Beschreibung");
    private final MultiSelectComboBox<Verband> verbaendeField = new MultiSelectComboBox<>("Verbände");

    private Verein aktuellerVerein;

    public VereinDetailsView(SecurityService securityService,
                             VereinService vereinService,
                             VerbandService verbandService,
                             VereinsmitgliedschaftService mitgliedschaftService,
                             VereinRepository vereinRepository) {
        this.vereinService = vereinService;
        this.verbandService = verbandService;
        this.mitgliedschaftService = mitgliedschaftService;
        this.vereinRepository = vereinRepository;
        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Überprüfe, ob ein Vereins-Parameter übergeben wurde
        Optional<String> vereinIdParam = event.getLocation().getQueryParameters()
                .getParameters().getOrDefault("vereinId", List.of()).stream().findFirst();

        if (vereinIdParam.isPresent()) {
            try {
                Long vereinId = Long.parseLong(vereinIdParam.get());
                Optional<Verein> vereinOptional = verbandService.findeVerein(vereinId);
                if (vereinOptional.isPresent()) {
                    aktuellerVerein = vereinOptional.get();
                    ladeVereinsdaten();
                } else {
                    Notification.show("Verein nicht gefunden").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (NumberFormatException e) {
                Notification.show("Ungültige Vereins-ID").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } else {
            // Fallback: Lade den ersten Verein, bei dem der Benutzer Vereinschef ist
            ladeErsteVereinschef();
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

        H2 title = new H2("Vereinsdetails bearbeiten");
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
                "Hier können Sie die Details Ihres Vereins bearbeiten und verwalten. Änderungen werden sofort gespeichert."
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
        vereinsNummerField.setWidthFull();
        adresseField.setWidthFull();
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("150px");
        verbaendeField.setRequired(true);
        verbaendeField.setItems(verbandService.findeAlleVerbaendeEntities());
        verbaendeField.setItemLabelGenerator(Verband::getName);
        verbaendeField.setPlaceholder("Verbände auswählen...");
        verbaendeField.setWidthFull();
        formLayout.add(nameField, vereinsNummerField, adresseField, beschreibungField, verbaendeField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(adresseField, 2);
        formLayout.setColspan(beschreibungField, 2);
        formLayout.setColspan(verbaendeField, 2);

        // Buttons mit Icons
        Button speichernButton = new Button("Speichern", e -> speichereVereinsdaten());
        speichernButton.setIcon(VaadinIcon.CHECK.create());
        speichernButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button abbrechenButton = new Button("Abbrechen", e -> ladeVereinsdaten());
        abbrechenButton.setIcon(VaadinIcon.CLOSE.create());

        HorizontalLayout buttonLayout = new HorizontalLayout(speichernButton, abbrechenButton);

        formContainer.add(formLayout, buttonLayout);
        contentWrapper.add(formContainer);
        add(contentWrapper);
    }

    /**
     * Lädt die Vereinsdaten.
     */
    private void ladeVereinsdaten() {
        if (aktuellerVerein == null) {
            Notification.show("Kein Verein ausgewählt")
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        nameField.setValue(aktuellerVerein.getName() != null ? aktuellerVerein.getName() : "");
        vereinsNummerField.setValue(aktuellerVerein.getVereinsNummer() != null ?
                aktuellerVerein.getVereinsNummer() : "");
        adresseField.setValue(aktuellerVerein.getAdresse() != null ? aktuellerVerein.getAdresse() : "");
        beschreibungField.setValue(aktuellerVerein.getBeschreibung() != null ?
                aktuellerVerein.getBeschreibung() : "");
        verbaendeField.setValue(aktuellerVerein.getVerbaende() != null ?
                aktuellerVerein.getVerbaende() : new HashSet<>());
    }

    /**
     * Lädt den ersten Verein, bei dem der Benutzer Vereinschef ist.
     */
    private void ladeErsteVereinschef() {
        if (currentUser == null) {
            return;
        }

        mitgliedschaftService.findeMitgliedschaften(currentUser).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                .findFirst()
                .ifPresent(dto -> {
                    Long vereinId = dto.getVereinId();
                    aktuellerVerein = vereinRepository.findById(vereinId).orElse(null);
                    ladeVereinsdaten();
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

        if (verbaendeField.getValue() == null || verbaendeField.getValue().isEmpty()) {
            Notification.show("Mindestens ein Verband ist erforderlich")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            aktuellerVerein.setName(nameField.getValue());
            aktuellerVerein.setVereinsNummer(vereinsNummerField.getValue());
            aktuellerVerein.setAdresse(adresseField.getValue());
            aktuellerVerein.setBeschreibung(beschreibungField.getValue());
            aktuellerVerein.setVerbaende(verbaendeField.getValue());

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
