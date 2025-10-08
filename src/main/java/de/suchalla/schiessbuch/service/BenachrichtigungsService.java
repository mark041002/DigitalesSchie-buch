package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benachrichtigung;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.BenachrichtigungsTyp;
import de.suchalla.schiessbuch.repository.BenachrichtigungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service für Benachrichtigungen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BenachrichtigungsService {

    private final BenachrichtigungRepository benachrichtigungRepository;

    /**
     * Erstellt eine neue Benachrichtigung.
     *
     * @param empfaenger Der Empfänger
     * @param titel Der Titel
     * @param nachricht Die Nachricht
     * @param typ Der Benachrichtigungstyp
     * @return Die gespeicherte Benachrichtigung
     */
    public Benachrichtigung erstelleBenachrichtigung(Benutzer empfaenger, String titel,
                                                      String nachricht, BenachrichtigungsTyp typ) {
        Benachrichtigung benachrichtigung = Benachrichtigung.builder()
                .empfaenger(empfaenger)
                .titel(titel)
                .nachricht(nachricht)
                .typ(typ)
                .gelesen(false)
                .build();

        return benachrichtigungRepository.save(benachrichtigung);
    }

    /**
     * Benachrichtigt einen Schützen über einen signierten Eintrag.
     *
     * @param schuetze Der Schütze
     * @param eintrag Der Eintrag
     */
    public void benachrichtigeEintragSigniert(Benutzer schuetze, SchiessnachweisEintrag eintrag) {
        String titel = "Eintrag wurde signiert";
        String nachricht = String.format(
                "Ihr Schießnachweis-Eintrag vom %s wurde vom Aufseher signiert.",
                eintrag.getDatum()
        );

        Benachrichtigung benachrichtigung = Benachrichtigung.builder()
                .empfaenger(schuetze)
                .titel(titel)
                .nachricht(nachricht)
                .typ(BenachrichtigungsTyp.EINTRAG_SIGNIERT)
                .eintrag(eintrag)
                .gelesen(false)
                .build();

        benachrichtigungRepository.save(benachrichtigung);
    }

    /**
     * Benachrichtigt einen Schützen über einen abgelehnten Eintrag.
     *
     * @param schuetze Der Schütze
     * @param eintrag Der Eintrag
     * @param grund Der Ablehnungsgrund
     */
    public void benachrichtigeEintragAbgelehnt(Benutzer schuetze, SchiessnachweisEintrag eintrag, String grund) {
        String titel = "Eintrag wurde abgelehnt";
        String nachricht = String.format(
                "Ihr Schießnachweis-Eintrag vom %s wurde abgelehnt.\n\nGrund: %s",
                eintrag.getDatum(), grund
        );

        Benachrichtigung benachrichtigung = Benachrichtigung.builder()
                .empfaenger(schuetze)
                .titel(titel)
                .nachricht(nachricht)
                .typ(BenachrichtigungsTyp.EINTRAG_ABGELEHNT)
                .eintrag(eintrag)
                .gelesen(false)
                .build();

        benachrichtigungRepository.save(benachrichtigung);
    }

    /**
     * Benachrichtigt einen Benutzer über eine bestätigte Mitgliedschaft.
     *
     * @param benutzer Der Benutzer
     * @param vereinsname Der Name des Vereins
     */
    public void benachrichtigeMitgliedschaftBestaetigt(Benutzer benutzer, String vereinsname) {
        String titel = "Vereinsmitgliedschaft bestätigt";
        String nachricht = String.format(
                "Ihre Mitgliedschaft im Verein '%s' wurde bestätigt.",
                vereinsname
        );

        erstelleBenachrichtigung(benutzer, titel, nachricht, BenachrichtigungsTyp.MITGLIEDSCHAFT_BESTAETIGT);
    }

    /**
     * Benachrichtigt einen Benutzer über eine beendete Mitgliedschaft.
     *
     * @param benutzer Der Benutzer
     * @param vereinsname Der Name des Vereins
     */
    public void benachrichtigeMitgliedschaftBeendet(Benutzer benutzer, String vereinsname) {
        String titel = "Vereinsmitgliedschaft beendet";
        String nachricht = String.format(
                "Ihre Mitgliedschaft im Verein '%s' wurde beendet.",
                vereinsname
        );

        erstelleBenachrichtigung(benutzer, titel, nachricht, BenachrichtigungsTyp.MITGLIEDSCHAFT_BEENDET);
    }

    /**
     * Benachrichtigt einen Benutzer über die Ernennung zum Aufseher.
     *
     * @param benutzer Der Benutzer
     * @param vereinsname Der Name des Vereins
     */
    public void benachrichtigeAufseherErnannt(Benutzer benutzer, String vereinsname) {
        String titel = "Sie wurden zum Aufseher ernannt";
        String nachricht = String.format(
                "Sie wurden im Verein '%s' zum Aufseher ernannt.",
                vereinsname
        );

        erstelleBenachrichtigung(benutzer, titel, nachricht, BenachrichtigungsTyp.AUFSEHER_ERNANNT);
    }

    /**
     * Findet alle Benachrichtigungen eines Empfängers.
     *
     * @param empfaenger Der Empfänger
     * @return Liste der Benachrichtigungen
     */
    @Transactional(readOnly = true)
    public List<Benachrichtigung> findeBenachrichtigungen(Benutzer empfaenger) {
        return benachrichtigungRepository.findByEmpfaengerOrderByErstelltAmDesc(empfaenger);
    }

    /**
     * Findet alle ungelesenen Benachrichtigungen eines Empfängers.
     *
     * @param empfaenger Der Empfänger
     * @return Liste der ungelesenen Benachrichtigungen
     */
    @Transactional(readOnly = true)
    public List<Benachrichtigung> findeUngelesene(Benutzer empfaenger) {
        return benachrichtigungRepository.findByEmpfaengerAndGelesenOrderByErstelltAmDesc(empfaenger, false);
    }

    /**
     * Zählt ungelesene Benachrichtigungen.
     *
     * @param empfaenger Der Empfänger
     * @return Anzahl ungelesener Benachrichtigungen
     */
    @Transactional(readOnly = true)
    public long zaehleUngelesene(Benutzer empfaenger) {
        return benachrichtigungRepository.countByEmpfaengerAndGelesen(empfaenger, false);
    }

    /**
     * Markiert eine Benachrichtigung als gelesen.
     *
     * @param benachrichtigungId Die Benachrichtigungs-ID
     */
    public void markiereAlsGelesen(Long benachrichtigungId) {
        Benachrichtigung benachrichtigung = benachrichtigungRepository.findById(benachrichtigungId)
                .orElseThrow(() -> new IllegalArgumentException("Benachrichtigung nicht gefunden"));

        benachrichtigung.markiereAlsGelesen();
        benachrichtigungRepository.save(benachrichtigung);
    }

    /**
     * Markiert alle Benachrichtigungen eines Empfängers als gelesen.
     *
     * @param empfaenger Der Empfänger
     */
    public void markiereAlleAlsGelesen(Benutzer empfaenger) {
        List<Benachrichtigung> ungelesene = findeUngelesene(empfaenger);
        ungelesene.forEach(Benachrichtigung::markiereAlsGelesen);
        benachrichtigungRepository.saveAll(ungelesene);
    }

    /**
     * Löscht eine Benachrichtigung.
     *
     * @param benachrichtigungId Die Benachrichtigungs-ID
     */
    public void loescheBenachrichtigung(Long benachrichtigungId) {
        benachrichtigungRepository.deleteById(benachrichtigungId);
    }
}

