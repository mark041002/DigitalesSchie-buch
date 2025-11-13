package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final DigitalesZertifikatRepository zertifikatRepository;

    /**
     * Beantragt eine Vereinsmitgliedschaft.
     *
     * @param benutzer Der Benutzer
     * @param vereinId Die Vereins-ID
     * @return Die erstellte Mitgliedschaft
     * @throws IllegalArgumentException wenn der Verein nicht existiert oder bereits eine aktive Mitgliedschaft besteht
     */
    public Vereinsmitgliedschaft beantragenMitgliedschaft(Benutzer benutzer, Long vereinId) {
        Verein verein = vereinRepository.findById(vereinId)
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
     */
    public void genehmigeAnfrage(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.AKTIV);
        mitgliedschaft.setAktiv(true);

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Lehnt eine Beitrittsanfrage ab.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     */
    public void lehneAnfrageAb(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.ABGELEHNT);
        mitgliedschaft.setAktiv(false);

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Lehnt eine Beitrittsanfrage mit Begründung ab.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @param grund            Der Ablehnungsgrund
     */
    public void lehneAnfrageAbMitGrund(Long mitgliedschaftId, String grund) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftStatus.ABGELEHNT);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAblehnungsgrund(grund);

        mitgliedschaftRepository.save(mitgliedschaft);
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

        mitgliedschaft.setStatus(MitgliedschaftStatus.VERLASSEN);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAustrittDatum(LocalDate.now());

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Löscht eine Mitgliedschaft endgültig aus der Datenbank.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     */
    public void loescheMitgliedschaft(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        // Nur Mitgliedschaften mit Status VERLASSEN, ABGELEHNT oder BEENDET dürfen gelöscht werden
        if (mitgliedschaft.getStatus() != MitgliedschaftStatus.VERLASSEN
            && mitgliedschaft.getStatus() != MitgliedschaftStatus.ABGELEHNT
            && mitgliedschaft.getStatus() != MitgliedschaftStatus.BEENDET) {
            throw new IllegalArgumentException("Nur beendete, abgelehnte oder verlassene Mitgliedschaften können gelöscht werden");
        }

        mitgliedschaftRepository.deleteById(mitgliedschaftId);
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
     * Wenn der Aufseher-Status entzogen wird, wird das gueltigBis-Datum des Zertifikats auf jetzt gesetzt.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     * @param istAufseher      Der neue Aufseher-Status
     */
    public void setzeAufseherStatus(Long mitgliedschaftId, boolean istAufseher) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        if (!mitgliedschaft.getAktiv() || mitgliedschaft.getStatus() != MitgliedschaftStatus.AKTIV) {
            throw new IllegalArgumentException("Nur aktive Mitglieder können zu Aufsehern ernannt werden");
        }

        boolean warVorherAufseher = Boolean.TRUE.equals(mitgliedschaft.getIstAufseher());
        mitgliedschaft.setIstAufseher(istAufseher);

        // Wenn Aufseher-Status entzogen wird, Zertifikat-Gültigkeit beenden
        if (warVorherAufseher && !istAufseher) {
            Optional<DigitalesZertifikat> zertifikat = zertifikatRepository.findByBenutzer(mitgliedschaft.getBenutzer());
            if (zertifikat.isPresent()) {
                DigitalesZertifikat cert = zertifikat.get();
                // Setze gueltigBis auf jetzt, damit das Zertifikat ab jetzt ungültig ist
                cert.setGueltigBis(LocalDateTime.now());
                zertifikatRepository.save(cert);
            }
        }

        mitgliedschaftRepository.save(mitgliedschaft);
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
                .filter((Vereinsmitgliedschaft m) -> m.getStatus() == MitgliedschaftStatus.AKTIV && m.getAktiv())
                .flatMap((Vereinsmitgliedschaft m) -> m.getVerein().getVerbaende().stream())
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

    /**
     * Gibt alle Mitgliedschaften für einen Verein zurück (alle Status).
     *
     * @param verein Der Verein
     * @return Liste aller Mitgliedschaften
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeAlleMitgliedschaften(Verein verein) {
        return mitgliedschaftRepository.findByVereinWithDetails(verein);
    }

    /**
     * Gibt Mitgliedschaften für einen Verein nach Status zurück.
     *
     * @param verein Der Verein
     * @param status Der Status
     * @return Liste der Mitgliedschaften
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeMitgliedschaftenNachStatus(Verein verein, MitgliedschaftStatus status) {
        return mitgliedschaftRepository.findByVereinAndStatusWithDetails(verein, status);
    }


    /**
     * Setzt den Vereinschef für einen Verein. Alle anderen werden als nicht-Vereinschef markiert.
     * @param neueChef Die Mitgliedschaft, die Vereinschef werden soll
     * @param alleMitglieder Alle Mitgliedschaften des Vereins
     */
    public void setzeVereinschef(Vereinsmitgliedschaft neueChef, List<Vereinsmitgliedschaft> alleMitglieder) {
        for (Vereinsmitgliedschaft mi : alleMitglieder) {
            if (Boolean.TRUE.equals(mi.getIstVereinschef())) {
                mi.setIstVereinschef(false);
                mitgliedschaftRepository.save(mi);
            }
        }
        neueChef.setIstVereinschef(true);
        mitgliedschaftRepository.save(neueChef);
    }
}
