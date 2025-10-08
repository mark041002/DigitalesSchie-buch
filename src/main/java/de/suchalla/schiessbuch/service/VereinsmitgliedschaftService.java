package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Service für Vereinsmitgliedschaftsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VereinsmitgliedschaftService {

    private final VereinsmitgliedschaftRepository mitgliedschaftRepository;
    private final VereinRepository vereinRepository;

    /**
     * Beantragt eine Vereinsmitgliedschaft.
     *
     * @param benutzer Der Benutzer
     * @param vereinId Die Vereins-ID
     * @return Die erstellte Mitgliedschaft
     * @throws IllegalArgumentException wenn der Verein nicht existiert oder bereits eine aktive Mitgliedschaft besteht
     */
    public Vereinsmitgliedschaft beantragenMitgliedschaft(Benutzer benutzer, Long vereinId) {
        Verein verein = vereinRepository.findByIdWithMitgliedschaften(vereinId)
                .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        // Prüfen, ob bereits eine aktive oder beantragte Mitgliedschaft existiert
        // Verwende Liste, falls es aufgrund früherer Fehler mehrere Einträge gibt
        List<Vereinsmitgliedschaft> existierende = mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein);
        if (!existierende.isEmpty()) {
            // Falls bereits eine aktive Mitgliedschaft existiert -> Abbruch
            boolean hatAktive = existierende.stream()
                    .anyMatch(m -> m.getStatus() == MitgliedschaftStatus.AKTIV && Boolean.TRUE.equals(m.getAktiv()));
            if (hatAktive) {
                throw new IllegalArgumentException("Es besteht bereits eine aktive Mitgliedschaft für diesen Verein");
            }
            // Falls schon eine Anfrage besteht -> Abbruch
            boolean hatAnfrage = existierende.stream()
                    .anyMatch(m -> m.getStatus() == MitgliedschaftStatus.BEANTRAGT);
            if (hatAnfrage) {
                throw new IllegalArgumentException("Es besteht bereits eine offene Beitrittsanfrage für diesen Verein");
            }
            // Wenn nur ältere (beendete/abgelehnte) Einträge vorhanden sind, erzeugen wir eine neue Anfrage
        }

        Vereinsmitgliedschaft mitgliedschaft = Vereinsmitgliedschaft.builder()
                .benutzer(benutzer)
                .verein(verein)
                .status(MitgliedschaftStatus.BEANTRAGT)
                .beitrittDatum(LocalDate.now())
                .aktiv(false)
                .build();

        try {
            return mitgliedschaftRepository.save(mitgliedschaft);
        } catch (DataIntegrityViolationException ex) {
            // Falls aufgrund konkurrierender Requests ein Duplikat in der DB entstanden ist,
            // laden wir die vorhandenen Einträge neu und reagieren entsprechend.
            existierende = mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein);
            if (!existierende.isEmpty()) {
                boolean hatAktive = existierende.stream()
                        .anyMatch(m -> m.getStatus() == MitgliedschaftStatus.AKTIV && Boolean.TRUE.equals(m.getAktiv()));
                if (hatAktive) {
                    throw new IllegalArgumentException("Es besteht bereits eine aktive Mitgliedschaft für diesen Verein");
                }
                boolean hatAnfrage = existierende.stream()
                        .anyMatch(m -> m.getStatus() == MitgliedschaftStatus.BEANTRAGT);
                if (hatAnfrage) {
                    throw new IllegalArgumentException("Es besteht bereits eine offene Beitrittsanfrage für diesen Verein");
                }
                // Andernfalls geben wir den ersten gefundenen Datensatz zurück (Falle älterer, beendeter Einträge)
                return existierende.get(0);
            }
            throw ex;
        }
    }

    /**
     * Genehmigt eine Beitrittsanfrage.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @return Die aktualisierte Mitgliedschaft
     */
    public Vereinsmitgliedschaft genehmigeAnfrage(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.AKTIV);
        mitgliedschaft.setAktiv(true);

        return mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Lehnt eine Beitrittsanfrage ab.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @return Die aktualisierte Mitgliedschaft
     */
    public Vereinsmitgliedschaft lehneAnfrageAb(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.ABGELEHNT);
        mitgliedschaft.setAktiv(false);

        return mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Lehnt eine Beitrittsanfrage mit Begründung ab.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @param grund Der Ablehnungsgrund
     * @return Die aktualisierte Mitgliedschaft
     */
    public Vereinsmitgliedschaft lehneAnfrageAbMitGrund(Long mitgliedschaftId, String grund) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.ABGELEHNT);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAblehnungsgrund(grund);

        return mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Gibt alle Beitrittsanfragen für einen Verein zurück.
     *
     * @param verein Der Verein
     * @return Liste der Anfragen
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeBeitrittsanfragen(Verein verein) {
        return mitgliedschaftRepository.findByVereinAndStatusWithDetails(verein, MitgliedschaftStatus.BEANTRAGT);
    }

    /**
     * Gibt alle aktiven Mitgliedschaften für einen Verein zurück.
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeAktiveMitgliedschaften(Verein verein) {
        return mitgliedschaftRepository.findByVereinAndStatusWithDetails(verein, MitgliedschaftStatus.AKTIV);
    }

    /**
     * Gibt alle Mitgliedschaften eines Benutzers zurück.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeMitgliedschaften(Benutzer benutzer) {
        return mitgliedschaftRepository.findByBenutzer(benutzer);
    }

    /**
     * Gibt alle Vereine zurück.
     *
     * @return Liste aller Vereine
     */
    @Transactional(readOnly = true)
    public List<Verein> findeAlleVereine() {
        return vereinRepository.findAll();
    }

    /**
     * Findet einen Verein nach ID.
     *
     * @param id Die Vereins-ID
     * @return Optional mit Verein
     */
    @Transactional(readOnly = true)
    public Optional<Verein> findeVerein(Long id) {
        return vereinRepository.findById(id);
    }

    /**
     * Lässt einen Benutzer einem Verein beitreten (erstellt eine Beitrittsanfrage).
     *
     * @param benutzer Der Benutzer
     * @param verein Der Verein
     * @return Die erstellte Mitgliedschaft
     */
    public Vereinsmitgliedschaft vereinBeitreten(Benutzer benutzer, Verein verein) {
        return beantragenMitgliedschaft(benutzer, verein.getId());
    }

    /**
     * Lässt einen Benutzer einen Verein verlassen.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     */
    public void vereinVerlassen(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.BEENDET);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAustrittDatum(LocalDate.now());

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Findet alle Mitgliedschaften eines Benutzers.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeMitgliedschaftenVonBenutzer(Benutzer benutzer) {
        return findeMitgliedschaften(benutzer);
    }

    /**
     * Setzt den Aufseher-Status einer Mitgliedschaft.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @param istAufseher Der neue Aufseher-Status
     * @return Die aktualisierte Mitgliedschaft
     */
    public Vereinsmitgliedschaft setzeAufseherStatus(Long mitgliedschaftId, boolean istAufseher) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        if (!mitgliedschaft.getAktiv() || mitgliedschaft.getStatus() != MitgliedschaftStatus.AKTIV) {
            throw new IllegalArgumentException("Nur aktive Mitglieder können zu Aufsehern ernannt werden");
        }

        mitgliedschaft.setIstAufseher(istAufseher);
        return mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Gibt die Verbände zurück, bei denen der Benutzer aktive Mitgliedschaften hat.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Verbände
     */
    @Transactional(readOnly = true)
    public List<de.suchalla.schiessbuch.model.entity.Verband> findeVerbaendeVonBenutzer(Benutzer benutzer) {
        return mitgliedschaftRepository.findByBenutzer(benutzer).stream()
                .filter(m -> m.getStatus() == MitgliedschaftStatus.AKTIV && m.getAktiv())
                .map(m -> m.getVerein().getVerband())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Entfernt ein Mitglied aus dem Verein (für Vereinschefs).
     * Setzt den Status auf BEENDET und deaktiviert die Mitgliedschaft.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     */
    public void mitgliedEntfernen(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.BEENDET);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAustrittDatum(LocalDate.now());

        mitgliedschaftRepository.save(mitgliedschaft);
    }
}
