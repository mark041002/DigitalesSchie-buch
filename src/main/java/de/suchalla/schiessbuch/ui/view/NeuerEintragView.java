package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View zum Erstellen neuer Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@Route(value = "neuer-eintrag", layout = MainLayout.class)
@PageTitle("Neuer Eintrag | Digitales Schießbuch")
@PermitAll
public class NeuerEintragView extends VerticalLayout {

    private final SecurityService securityService;
    private final SchiessnachweisService schiessnachweisService;
    private final DisziplinService disziplinService;
    private final VereinsmitgliedschaftService vereinsmitgliedschaftService;

    private final DatePicker datum = new DatePicker("Datum");
    private final ComboBox<Schiesstand> schiesstand = new ComboBox<>("Schießstand");
    private final ComboBox<Verband> verband = new ComboBox<>("Verband");
    private final ComboBox<Disziplin> disziplin = new ComboBox<>("Disziplin");
    private final TextField kaliber = new TextField("Kaliber");
    private final ComboBox<Waffenart> waffenart = new ComboBox<>("Waffenart");
    private final IntegerField anzahlSchuesse = new IntegerField("Anzahl Schüsse");
    private final TextArea bemerkung = new TextArea("Bemerkung");

    private final Benutzer currentUser;

    public NeuerEintragView(SecurityService securityService,
                            SchiessnachweisService schiessnachweisService,
                            DisziplinService disziplinService,
                            VereinsmitgliedschaftService vereinsmitgliedschaftService) {
        this.securityService = securityService;
        this.schiessnachweisService = schiessnachweisService;
        this.disziplinService = disziplinService;
        this.vereinsmitgliedschaftService = vereinsmitgliedschaftService;

        this.currentUser = securityService.getAuthenticatedUser().orElse(null);

        setSpacing(true);
        setPadding(true);

        createContent();
    }

    /**
     * Erstellt den Inhalt der View.
     */
    private void createContent() {
        add(new H2("Neuer Schießnachweis-Eintrag"));

        // Formular konfigurieren
        datum.setValue(LocalDate.now());
        datum.setRequired(true);

        // Schießstand: Mit Suchfunktion - jetzt zuerst
        schiesstand.setItems(disziplinService.findeAlleSchiesstaende());
        schiesstand.setItemLabelGenerator(Schiesstand::getName);
        schiesstand.setRequired(true);
        schiesstand.setPlaceholder("Schießstand auswählen");
        schiesstand.setClearButtonVisible(true);

        // Verband: Nur Verbände, bei denen der Benutzer angemeldet ist
        List<Verband> benutzerVerbaende = getBenutzerverbaende();
        verband.setItems(benutzerVerbaende);
        verband.setItemLabelGenerator(Verband::getName);
        verband.setRequired(true);
        verband.setPlaceholder("Verband auswählen");
        verband.addValueChangeListener(e -> onVerbandChanged());

        // Disziplin: Zunächst deaktiviert, wird aktiviert wenn Verband ausgewählt
        disziplin.setEnabled(false);
        disziplin.setRequired(true);
        disziplin.setPlaceholder("Bitte zuerst Verband auswählen");
        disziplin.setItemLabelGenerator(Disziplin::getName);

        // Waffenart: Dropdown mit Enum
        waffenart.setItems(Waffenart.values());
        waffenart.setItemLabelGenerator(Waffenart::getAnzeigeText);
        waffenart.setRequired(true);
        waffenart.setPlaceholder("Kurzwaffe oder Langwaffe");

        kaliber.setRequired(true);
        kaliber.setPlaceholder("z.B. 9mm, .22 LR");

        anzahlSchuesse.setMin(1);
        anzahlSchuesse.setStepButtonsVisible(true);

        bemerkung.setMaxLength(500);
        bemerkung.setHelperText("Optional");

        FormLayout formLayout = new FormLayout();
        formLayout.add(datum, schiesstand, verband, disziplin, waffenart, kaliber, anzahlSchuesse, bemerkung);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button speichern = new Button("Speichern", e -> speichereEintrag());
        speichern.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button abbrechen = new Button("Abbrechen", e -> formularZuruecksetzen());

        add(formLayout, new com.vaadin.flow.component.orderedlayout.HorizontalLayout(speichern, abbrechen));
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
        return vereinsmitgliedschaftService.findeVerbaendeVonBenutzer(currentUser);
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

            // Filtere Disziplinen nach ausgewähltem Verband
            List<Disziplin> verfuegbareDisziplinen = disziplinService.findeAlleDisziplinen().stream()
                    .filter(d -> d.getVerband() != null && d.getVerband().getId().equals(verband.getValue().getId()))
                    .collect(Collectors.toList());

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
                    .bemerkung(bemerkung.getValue())
                    .build();

            schiessnachweisService.erstelleEintrag(eintrag);

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
