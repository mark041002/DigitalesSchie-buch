package de.suchalla.schiessbuch.ui.view.persoenlich;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.enums.Waffenart;
import de.suchalla.schiessbuch.security.SecurityService;
import de.suchalla.schiessbuch.service.DisziplinService;
import de.suchalla.schiessbuch.service.SchiessnachweisService;
import de.suchalla.schiessbuch.service.VereinsmitgliedschaftService;
import de.suchalla.schiessbuch.service.email.NotificationService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.security.PermitAll;
import java.time.LocalDate;
import java.util.List;

/**
 * View zum Erstellen neuer Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@Route(value = "neuer-eintrag", layout = MainLayout.class)
@PageTitle("Neuer Eintrag | Digitales Schießbuch")
@PermitAll
@Slf4j
public class NeuerEintragView extends VerticalLayout {

    private final SchiessnachweisService schiessnachweisService;
    private final DisziplinService disziplinService;
    private final VereinsmitgliedschaftService vereinsmitgliedschaftService;
    private final NotificationService notificationService;

    private final DatePicker datum = new DatePicker("Datum");
    private final ComboBox<Schiesstand> schiesstand = new ComboBox<>("Schießstand");
    private final ComboBox<Verband> verband = new ComboBox<>("Verband");
    private final ComboBox<Disziplin> disziplin = new ComboBox<>("Disziplin");
    private final TextField kaliber = new TextField("Kaliber");
    private final ComboBox<Waffenart> waffenart = new ComboBox<>("Waffenart");
    private final IntegerField anzahlSchuesse = new IntegerField("Anzahl Schüsse");
    private final TextArea bemerkung = new TextArea("Bemerkung");
    private final TextField ergebnis = new TextField("Ergebnis");

    private final Benutzer currentUser;

    public NeuerEintragView(SchiessnachweisService schiessnachweisService,
                            DisziplinService disziplinService,
                            VereinsmitgliedschaftService vereinsmitgliedschaftService,
                            SecurityService securityService,
                            NotificationService notificationService) {
        this.schiessnachweisService = schiessnachweisService;
        this.disziplinService = disziplinService;
        this.vereinsmitgliedschaftService = vereinsmitgliedschaftService;
        this.notificationService = notificationService;

        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
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
        contentWrapper.setMaxWidth("1200px"); // Formulare etwas schmaler

        // Header-Bereich mit modernem Styling - gleiche Breite wie FormCard
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Neuer Schießnachweis-Eintrag");
        title.getStyle().set("margin", "0");
        header.add(title);
        contentWrapper.add(header);

        // Info-Box direkt unter dem Header
        Div infoBoxHeader = new Div();
        infoBoxHeader.addClassName("info-box");
        Icon infoIconHeader = VaadinIcon.INFO_CIRCLE.create();
        infoIconHeader.setSize("20px");
        Span infoTextHeader = new Span("Dokumentieren Sie Ihre Schießaktivität");
        infoTextHeader.getStyle().set("color", "var(--lumo-primary-text-color)");
        infoBoxHeader.add(infoIconHeader, infoTextHeader);
        contentWrapper.add(infoBoxHeader);

        // Formular in Card-Layout
        Div formCard = new Div();
        formCard.addClassName("form-container");
        formCard.setWidthFull();

        // Formular konfigurieren - nur wichtige Felder mit Icons
        datum.setValue(LocalDate.now());
        datum.setRequired(true);

        // Schießstand: Mit Icon
        schiesstand.setItems(disziplinService.findeAlleSchiesstaendeEntities());
        schiesstand.setItemLabelGenerator(Schiesstand::getName);
        schiesstand.setRequired(true);
        schiesstand.setPlaceholder("Schießstand auswählen");
        schiesstand.setClearButtonVisible(true);
        schiesstand.setPrefixComponent(VaadinIcon.BUILDING.create());

        // Verband: Mit Icon
        List<Verband> benutzerVerbaende = getBenutzerverbaende();
        verband.setItems(benutzerVerbaende);
        verband.setItemLabelGenerator(Verband::getName);
        verband.setRequired(true);
        verband.setPlaceholder("Verband auswählen");
        verband.setPrefixComponent(VaadinIcon.USERS.create());
        verband.addValueChangeListener(e -> onVerbandChanged());

        // Disziplin: OHNE Icon
        disziplin.setEnabled(false);
        disziplin.setRequired(true);
        disziplin.setPlaceholder("Bitte zuerst Verband auswählen");
        disziplin.setItemLabelGenerator(Disziplin::getName);

        // Waffenart: OHNE Icon
        waffenart.setItems(Waffenart.values());
        waffenart.setItemLabelGenerator(Waffenart::getAnzeigeText);
        waffenart.setRequired(true);
        waffenart.setPlaceholder("Kurzwaffe oder Langwaffe");

        // Kaliber: OHNE Icon
        kaliber.setRequired(true);
        kaliber.setPlaceholder("z.B. 9mm, .22 LR");
        kaliber.setClearButtonVisible(true);

        // Anzahl Schüsse: OHNE Icon
        anzahlSchuesse.setMin(1);
        anzahlSchuesse.setStepButtonsVisible(true);

        // Ergebnis: OHNE Icon
        ergebnis.setClearButtonVisible(true);
        ergebnis.setPlaceholder("Ergebnis eintragen");

        // Bemerkung: OHNE Icon
        bemerkung.setMaxLength(500);
        bemerkung.setHelperText("Optional - max. 500 Zeichen");

        FormLayout formLayout = new FormLayout();
        formLayout.add(datum, schiesstand, verband, disziplin, waffenart, kaliber, anzahlSchuesse, ergebnis, bemerkung);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(bemerkung, 2);

        // Button-Bereich
        Button speichern = new Button("Speichern", new Icon(VaadinIcon.CHECK));
        speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        speichern.addClickListener(e -> speichereEintrag());

        Button abbrechen = new Button("Abbrechen", new Icon(VaadinIcon.CLOSE));
        abbrechen.addClickListener(e -> formularZuruecksetzen());

        HorizontalLayout buttonLayout = new HorizontalLayout(speichern, abbrechen);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Info-Box für Hinweise - jetzt ganz unten
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");
        Span infoText = new Span("Alle mit * markierten Felder sind Pflichtfelder.");
        infoText.getStyle().set("color", "var(--lumo-primary-text-color)");
        infoBox.add(infoIcon, infoText);

        formCard.add(formLayout, buttonLayout, infoBox);
        contentWrapper.add(formCard);
        add(contentWrapper);
    }

    /**
     * Gibt die Verbände zurück, bei denen der Benutzer angemeldet ist.
     *
     * @return Liste der Verbände
     */
    private List<Verband> getBenutzerverbaende() {
        if (currentUser == null) {
            return List.of();
        }

        // Verwende Service-Methode statt direkten Zugriff auf lazy Collection
        return vereinsmitgliedschaftService.findeVerbaendeVonBenutzerEntities(currentUser);
    }

    /**
     * Handler für Verband-Änderung: Aktualisiert verfügbare Disziplinen.
     */
    private void onVerbandChanged() {
        updateDisziplinVerfuegbarkeit();
    }

    /**
     * Aktualisiert die Verfügbarkeit und Items der Disziplin-ComboBox.
     */
    private void updateDisziplinVerfuegbarkeit() {
        if (verband.getValue() != null) {
            // Aktiviere Disziplin und lade passende Disziplinen
            disziplin.setEnabled(true);

            // Lade Disziplinen direkt für den ausgewählten Verband
            List<Disziplin> verfuegbareDisziplinen = disziplinService.findeDisziplinenVonVerbandEntities(verband.getValue().getId());

            disziplin.setItems(verfuegbareDisziplinen);
            disziplin.setPlaceholder("Disziplin auswählen");

            if (verfuegbareDisziplinen.isEmpty()) {
                disziplin.setPlaceholder("Keine Disziplinen für diesen Verband verfügbar");
            }
        } else {
            // Deaktiviere Disziplin wenn Verband nicht ausgewählt
            disziplin.setEnabled(false);
            disziplin.clear();
            disziplin.setPlaceholder("Bitte zuerst Verband auswählen");
        }
    }

    /**
     * Speichert den Eintrag.
     */
    private void speichereEintrag() {
        if (!validateForm()) {
            return;
        }

        try {
            SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                    .schuetze(currentUser)
                    .datum(datum.getValue())
                    .disziplin(disziplin.getValue())
                    .kaliber(kaliber.getValue())
                    .waffenart(waffenart.getValue().getAnzeigeText())
                    .schiesstand(schiesstand.getValue())
                    .anzahlSchuesse(anzahlSchuesse.getValue())
                    .ergebnis(ergebnis.getValue())
                    .bemerkung(bemerkung.getValue())
                    .build();

            schiessnachweisService.erstelleEintrag(eintrag);

            // Benachrichtigung: Informiere Vereinschefs/Aufseher über neuen Eintrag zur Signatur
            try {
                notificationService.notifySignatureRequest(eintrag);
            } catch (Exception nEx) {
                log.warn("Fehler beim Senden der Signatur-Benachrichtigung: {}", nEx.getMessage());
            }

            Notification.show("Eintrag erfolgreich erstellt")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            formularZuruecksetzen();
            getUI().ifPresent(ui -> ui.navigate(MeineEintraegeView.class));

        } catch (Exception e) {
            Notification.show("Fehler beim Speichern: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Validiert das Formular.
     *
     * @return true wenn gültig
     */
    private boolean validateForm() {
        if (datum.getValue() == null) {
            Notification.show("Bitte wählen Sie ein Datum").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (verband.getValue() == null) {
            Notification.show("Bitte wählen Sie einen Verband").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (schiesstand.getValue() == null) {
            Notification.show("Bitte wählen Sie einen Schießstand").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (disziplin.getValue() == null) {
            Notification.show("Bitte wählen Sie eine Disziplin").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (waffenart.getValue() == null) {
            Notification.show("Bitte wählen Sie eine Waffenart").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (kaliber.isEmpty()) {
            Notification.show("Bitte geben Sie das Kaliber an").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }

    /**
     * Setzt das Formular zurück.
     */
    private void formularZuruecksetzen() {
        datum.setValue(LocalDate.now());
        ergebnis.clear();
        verband.clear();
        schiesstand.clear();
        disziplin.clear();
        disziplin.setEnabled(false);
        kaliber.clear();
        waffenart.clear();
        anzahlSchuesse.clear();
        bemerkung.clear();
    }
}
