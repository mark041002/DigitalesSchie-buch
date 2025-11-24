package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.VerbandMapper;
import de.suchalla.schiessbuch.mapper.VereinsmigliedschaftMapper;
import de.suchalla.schiessbuch.model.dto.VerbandDTO;
import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.service.email.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service für Vereinsmitgliedschaftsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VereinsmitgliedschaftService {

    private final VereinsmitgliedschaftRepository mitgliedschaftRepository;
    private final VereinRepository vereinRepository;
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final BenutzerRepository benutzerRepository;
    private final PkiService pkiService;
    private final VereinsmigliedschaftMapper vereinsmigliedschaftMapper;
    private final VerbandMapper verbandMapper;
    private final EmailService notificationService;

    /**
     * Beantragt eine Vereinsmitgliedschaft.
     *
     * @param benutzer Der Benutzer
     * @param vereinId Die Vereins-ID
     * @return Die erstellte Mitgliedschaft
     * @throws IllegalArgumentException wenn der Verein nicht existiert oder bereits eine aktive Mitgliedschaft besteht
     */
    public Vereinsmitgliedschaft beantragenMitgliedschaft(Benutzer benutzer, Long vereinId) {
        return beantragenMitgliedschaft(benutzer, vereinId, false);
    }

    /**
     * Beantragt eine Vereinsmitgliedschaft mit Option, Benachrichtigung zu unterdrücken.
     * @param benutzer Der Benutzer
     * @param vereinId Die Vereins-ID
     * @param suppressNotification Wenn true, wird keine Beitrittsanfrage-E-Mail verschickt
     * @return Die erstellte Mitgliedschaft
     */
    public Vereinsmitgliedschaft beantragenMitgliedschaft(Benutzer benutzer, Long vereinId, boolean suppressNotification) {
        Verein verein = vereinRepository.findById(vereinId)
                .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        // Prüfen, ob bereits eine aktive oder beantragte Mitgliedschaft existiert
        // Verwende Liste, falls es aufgrund früherer Fehler mehrere Einträge gibt
        List<Vereinsmitgliedschaft> existierende = mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein);
        if (!existierende.isEmpty()) {
            // Falls bereits eine aktive Mitgliedschaft existiert -> Abbruch
            boolean hatAktive = existierende.stream()
                    .anyMatch(m -> m.getStatus() == MitgliedschaftsStatus.AKTIV && Boolean.TRUE.equals(m.getAktiv()));
            if (hatAktive) {
                throw new IllegalArgumentException("Es besteht bereits eine aktive Mitgliedschaft für diesen Verein");
            }
            // Falls schon eine Anfrage besteht -> Abbruch
            boolean hatAnfrage = existierende.stream()
                    .anyMatch(m -> m.getStatus() == MitgliedschaftsStatus.BEANTRAGT);
            if (hatAnfrage) {
                throw new IllegalArgumentException("Es besteht bereits eine offene Beitrittsanfrage für diesen Verein");
            }
            // Wenn nur ältere (beendete/abgelehnte) Einträge vorhanden sind, erzeugen wir eine neue Anfrage
        }

        Vereinsmitgliedschaft mitgliedschaft = Vereinsmitgliedschaft.builder()
                .benutzer(benutzer)
                .verein(verein)
                .status(MitgliedschaftsStatus.BEANTRAGT)
                .beitrittDatum(LocalDate.now())
                .aktiv(false)
                .build();

        Vereinsmitgliedschaft saved = null;
        try {
            saved = mitgliedschaftRepository.save(mitgliedschaft);
        } catch (DataIntegrityViolationException ex) {
            // Falls aufgrund konkurrierender Requests ein Duplikat in der DB entstanden ist,
            // laden wir die vorhandenen Einträge neu und reagieren entsprechend.
            existierende = mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein);
            if (!existierende.isEmpty()) {
                boolean hatAktive = existierende.stream()
                        .anyMatch(m -> m.getStatus() == MitgliedschaftsStatus.AKTIV && Boolean.TRUE.equals(m.getAktiv()));
                if (hatAktive) {
                    throw new IllegalArgumentException("Es besteht bereits eine aktive Mitgliedschaft für diesen Verein");
                }
                boolean hatAnfrage = existierende.stream()
                        .anyMatch(m -> m.getStatus() == MitgliedschaftsStatus.BEANTRAGT);
                if (hatAnfrage) {
                    throw new IllegalArgumentException("Es besteht bereits eine offene Beitrittsanfrage für diesen Verein");
                }
                // Andernfalls geben wir den ersten gefundenen Datensatz zurück (Falle älterer, beendeter Einträge)
                saved = existierende.get(0);
            }
            throw ex;
        }

        // Nur Benachrichtigung senden, wenn nicht unterdrückt
        if (!suppressNotification) {
            try {
                notificationService.notifyMembershipRequest(verein, benutzer);
            } catch (Exception nEx) {
                log.warn("Fehler beim Senden der Beitrittsanfrage-Benachrichtigung: {}", nEx.getMessage());
            }
        }

        return saved;
    }

    /**
     * Genehmigt eine Beitrittsanfrage.
     *
     * @param mitgliedschaftId Die Mitgliedschafts-ID
     */
    public void genehmigeAnfrage(Long mitgliedschaftId) {
        Vereinsmitgliedschaft mitgliedschaft = mitgliedschaftRepository.findById(mitgliedschaftId)
                .orElseThrow(() -> new IllegalArgumentException("Mitgliedschaft nicht gefunden"));

        mitgliedschaft.setStatus(MitgliedschaftsStatus.AKTIV);
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

        mitgliedschaft.setStatus(MitgliedschaftsStatus.ABGELEHNT);
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

        mitgliedschaft.setStatus(MitgliedschaftsStatus.ABGELEHNT);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAblehnungsgrund(grund);

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Gibt alle Beitrittsanfragen für einen Verein als DTOs zurück.
     *
     * @param verein Der Verein
     * @return Liste der Anfragen als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinsmigliedschaftDTO> findeBeitrittsanfragen(Verein verein) {
        List<Vereinsmitgliedschaft> entities = mitgliedschaftRepository.findByVereinAndStatus(verein, MitgliedschaftsStatus.BEANTRAGT);
        return vereinsmigliedschaftMapper.toDTOList(entities);
    }

    /**
     * Gibt alle aktiven Mitgliedschaften für einen Verein als DTOs zurück.
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinsmigliedschaftDTO> findeAktiveMitgliedschaften(Verein verein) {
        List<Vereinsmitgliedschaft> entities = mitgliedschaftRepository.findByVereinAndStatus(verein, MitgliedschaftsStatus.AKTIV);
        return vereinsmigliedschaftMapper.toDTOList(entities);
    }

    /**
     * Findet alle aktiven Mitgliedschaften eines Vereins als Entities (für interne Verwendung).
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften als Entities
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeAktiveMitgliedschaftenEntities(Verein verein) {
        return mitgliedschaftRepository.findByVereinAndStatus(verein, MitgliedschaftsStatus.AKTIV);
    }

    /**
     * Gibt alle Mitgliedschaften eines Benutzers als DTOs zurück.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinsmigliedschaftDTO> findeMitgliedschaften(Benutzer benutzer) {
        List<Vereinsmitgliedschaft> entities = mitgliedschaftRepository.findByBenutzer(benutzer);
        return vereinsmigliedschaftMapper.toDTOList(entities);
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

        mitgliedschaft.setStatus(MitgliedschaftsStatus.VERLASSEN);
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
        if (mitgliedschaft.getStatus() != MitgliedschaftsStatus.VERLASSEN
            && mitgliedschaft.getStatus() != MitgliedschaftsStatus.ABGELEHNT
            && mitgliedschaft.getStatus() != MitgliedschaftsStatus.BEENDET) {
            throw new IllegalArgumentException("Nur beendete, abgelehnte oder verlassene Mitgliedschaften können gelöscht werden");
        }

        mitgliedschaftRepository.deleteById(mitgliedschaftId);
    }

    /**
     * Findet alle Mitgliedschaften eines Benutzers als Entities (für interne Verwendung).
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften als Entities
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeMitgliedschaftenVonBenutzerEntities(Benutzer benutzer) {
        return mitgliedschaftRepository.findByBenutzer(benutzer).stream()
                .filter(m -> Boolean.TRUE.equals(m.getAktiv()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Findet alle Mitgliedschaften eines Benutzers als Entities (inkl. Anfragen und beendete Mitgliedschaften).
     * Diese Methode liefert alle Status zurück und wird von UI-Views verwendet, die auch BEANTRAGT/ABGELEHNT sehen sollen.
     *
     * @param benutzer Der Benutzer
     * @return Liste aller Mitgliedschaften als Entities
     */
    @Transactional(readOnly = true)
    public List<Vereinsmitgliedschaft> findeAlleMitgliedschaftenVonBenutzerEntities(Benutzer benutzer) {
        return mitgliedschaftRepository.findByBenutzer(benutzer);
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

        if (!mitgliedschaft.getAktiv() || mitgliedschaft.getStatus() != MitgliedschaftsStatus.AKTIV) {
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
     * @return Liste der Verbände als DTOs
     */
    @Transactional(readOnly = true)
    public List<VerbandDTO> findeVerbaendeVonBenutzer(Benutzer benutzer) {
        List<de.suchalla.schiessbuch.model.entity.Verband> entities =
                mitgliedschaftRepository.findByBenutzer(benutzer).stream()
                .filter((Vereinsmitgliedschaft m) -> m.getStatus() == MitgliedschaftsStatus.AKTIV && m.getAktiv())
                .flatMap((Vereinsmitgliedschaft m) -> m.getVerein().getVerbaende().stream())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        return verbandMapper.toDTOList(entities);
    }

    /**
     * Gibt die Verbände zurück, bei denen der Benutzer aktive Mitgliedschaften hat (als Entities).
     *
     * @param benutzer Der Benutzer
     * @return Liste der Verbände als Entities
     */
    @Transactional(readOnly = true)
    public List<de.suchalla.schiessbuch.model.entity.Verband> findeVerbaendeVonBenutzerEntities(Benutzer benutzer) {
        return mitgliedschaftRepository.findByBenutzer(benutzer).stream()
                .filter((Vereinsmitgliedschaft m) -> m.getStatus() == MitgliedschaftsStatus.AKTIV && m.getAktiv())
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

        mitgliedschaft.setStatus(MitgliedschaftsStatus.BEENDET);
        mitgliedschaft.setAktiv(false);
        mitgliedschaft.setAustrittDatum(LocalDate.now());

        mitgliedschaftRepository.save(mitgliedschaft);
    }

    /**
     * Gibt alle Mitgliedschaften für einen Verein als DTOs zurück (alle Status).
     *
     * @param verein Der Verein
     * @return Liste aller Mitgliedschaften als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinsmigliedschaftDTO> findeAlleMitgliedschaften(Verein verein) {
        List<Vereinsmitgliedschaft> entities = mitgliedschaftRepository.findByVerein(verein);
        return vereinsmigliedschaftMapper.toDTOList(entities);
    }

    /**
     * Gibt Mitgliedschaften für einen Verein nach Status als DTOs zurück.
     *
     * @param verein Der Verein
     * @param status Der Status
     * @return Liste der Mitgliedschaften als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinsmigliedschaftDTO> findeMitgliedschaftenNachStatus(Verein verein, MitgliedschaftsStatus status) {
        List<Vereinsmitgliedschaft> entities = mitgliedschaftRepository.findByVereinAndStatus(verein, status);
        return vereinsmigliedschaftMapper.toDTOList(entities);
    }


    /**
     * Setzt einen neuen Vereinschef für einen Verein.
     * Alle anderen Mitglieder werden als nicht-Vereinschef markiert.
     * Erstellt ein Zertifikat für den neuen Vereinschef und widerruft das alte.
     *
     * @param neueChef Die Mitgliedschaft, die zum Vereinschef ernannt werden soll
     * @param alleMitglieder Alle Mitgliedschaften des Vereins
     */
    public void setzeVereinschef(Vereinsmitgliedschaft neueChef, List<Vereinsmitgliedschaft> alleMitglieder) {
        // Alten Vereinschef finden und Zertifikat widerrufen
        for (Vereinsmitgliedschaft mi : alleMitglieder) {
            if (Boolean.TRUE.equals(mi.getIstVereinschef())) {
                mi.setIstVereinschef(false);
                mitgliedschaftRepository.save(mi);

                // Zertifikat des alten Vereinschefs widerrufen
                Benutzer alterChefDetached = mi.getBenutzer();
                if (alterChefDetached != null && alterChefDetached.getId() != null) {
                    Benutzer alterChef = benutzerRepository.findById(alterChefDetached.getId()).orElse(alterChefDetached);

                    Optional<DigitalesZertifikat> altesZertifikat = zertifikatRepository.findByBenutzer(alterChef);
                    if (altesZertifikat.isPresent()) {
                        DigitalesZertifikat zert = altesZertifikat.get();
                        zert.setWiderrufen(true);
                        zert.setWiderrufenAm(LocalDateTime.now());
                        zert.setWiderrufsGrund("Vereinschef-Funktion beendet");
                        zertifikatRepository.save(zert);
                        log.info("Zertifikat von {} widerrufen (SN: {})", alterChef.getVollstaendigerName(), zert.getSeriennummer());
                    }

                    // Rolle auf SCHUETZE zurücksetzen, falls keine anderen Aufseherfunktionen
                    List<Vereinsmitgliedschaft> mitgliedschaftenDesBenutzers = mitgliedschaftRepository.findByBenutzer(alterChef);
                    boolean hatAndereAufseherFunktion = mitgliedschaftenDesBenutzers.stream()
                            .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()));

                    if (!hatAndereAufseherFunktion && alterChef.getRolle() == BenutzerRolle.VEREINS_CHEF) {
                        alterChef.setRolle(BenutzerRolle.SCHUETZE);
                        benutzerRepository.save(alterChef);
                        log.info("Rolle von {} auf SCHUETZE zurückgesetzt", alterChef.getVollstaendigerName());
                    }
                }
            }
        }

        // Neuen Vereinschef setzen
        neueChef.setIstVereinschef(true);
        mitgliedschaftRepository.save(neueChef);

        // Zertifikat für neuen Vereinschef erstellen
        Benutzer neuerChefDetached = neueChef.getBenutzer();
        if (neuerChefDetached != null && neuerChefDetached.getId() != null) {
            Benutzer neuerChef = benutzerRepository.findById(neuerChefDetached.getId()).orElse(neuerChefDetached);

            Verein verein = mitgliedschaftRepository.findByBenutzer(neuerChef).stream()
                    .map(Vereinsmitgliedschaft::getVerein)
                    .findFirst()
                    .orElse(null);

            try {
                // Prüfen, ob bereits ein Zertifikat existiert
                Optional<DigitalesZertifikat> bestehendesZertifikat = zertifikatRepository.findByBenutzer(neuerChef);
                if (bestehendesZertifikat.isEmpty()) {
                    DigitalesZertifikat neuesZertifikat = pkiService.createAufseherCertificate(neuerChef, verein);
                    log.info("Zertifikat für neuen Vereinschef {} erstellt (SN: {})", 
                            neuerChef.getVollstaendigerName(), neuesZertifikat.getSeriennummer());
                } else {
                    log.info("Vereinschef {} hat bereits ein gültiges Zertifikat", neuerChef.getVollstaendigerName());
                }
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Vereinschef-Zertifikats für {}", neuerChef.getVollstaendigerName(), e);
                throw new RuntimeException("Zertifikat konnte nicht erstellt werden: " + e.getMessage(), e);
            }

            // Rolle auf VEREINS_CHEF setzen
            if (neuerChef.getRolle() != BenutzerRolle.VEREINS_CHEF && neuerChef.getRolle() != BenutzerRolle.ADMIN) {
                neuerChef.setRolle(BenutzerRolle.VEREINS_CHEF);
                benutzerRepository.save(neuerChef);
                log.info("Rolle von {} auf VEREINS_CHEF gesetzt", neuerChef.getVollstaendigerName());
            }
        }
    }
}
