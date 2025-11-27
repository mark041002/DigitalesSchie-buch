package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service für Schießstandsverwaltung.
 *
 * Kapselt alle CRUD-Operationen und bietet Lese-Methoden als Entities.
 *
 * @author Markus Suchalla
 * @version 1.0.4
 */
@Service
@Slf4j
public class SchiesstandService {
    private final SchiesstandRepository schiesstandRepository;
    private final SchiessnachweisEintragRepository eintragRepository;
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final BenutzerRepository benutzerRepository;
    private final PkiService pkiService;

    /**
     * Konstruktor.
     *
     * @param schiesstandRepository Repository für Schießstände
     * @param eintragRepository Repository für Schießnachweis-Einträge
     * @param zertifikatRepository Repository für digitale Zertifikate
     * @param benutzerRepository Repository für Benutzer
     * @param pkiService Service für PKI-Zertifikatsverwaltung
     */
    public SchiesstandService(SchiesstandRepository schiesstandRepository,
                              SchiessnachweisEintragRepository eintragRepository,
                              DigitalesZertifikatRepository zertifikatRepository,
                              BenutzerRepository benutzerRepository,
                              PkiService pkiService) {
        this.schiesstandRepository = schiesstandRepository;
        this.eintragRepository = eintragRepository;
        this.zertifikatRepository = zertifikatRepository;
        this.benutzerRepository = benutzerRepository;
        this.pkiService = pkiService;
    }

    /**
     * Erstellt einen neuen Schießstand.
     *
     * @param schiesstand Der zu erstellende Schießstand
     */
    public void erstelleSchiesstand(Schiesstand schiesstand) {
        schiesstandRepository.save(schiesstand);
    }

    /**
     * Findet einen Schießstand anhand der ID.
     *
     * @param id Die Schießstand-ID
     * @return Optional mit Schießstand
     */
    @Transactional(readOnly = true)
    public Optional<Schiesstand> findeSchiesstand(Long id) {
        return schiesstandRepository.findById(id);
    }

    /**
     * Findet alle Schießstände als Entities mit EAGER-Laden der zugehörigen
     * Vereine und Aufseher.
     *
     * @return Liste aller Schießstände (Entities)
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> findeAlleSchiesstaendeEntities() {
        return schiesstandRepository.findAllWithVerein();
    }

    /**
     * Aktualisiert einen bestehenden Schießstand.
     *
     * @param schiesstand Der zu aktualisierende Schießstand
     */
    public void aktualisiereSchiesstand(Schiesstand schiesstand) {
        schiesstandRepository.save(schiesstand);
    }

    /**
     * Löscht einen Schießstand, sofern keine Einträge mehr vorhanden sind.
     *
     * @param schiesstandId Die Schießstand-ID
     * @throws IllegalArgumentException Wenn der Schießstand nicht existiert
     */
    @Transactional
    public void loescheSchiesstand(Long schiesstandId) {
        Schiesstand schiesstand = schiesstandRepository.findById(schiesstandId)
                .orElseThrow(() -> new IllegalArgumentException("Schießstand nicht gefunden"));

        // Lösche alle zugehörigen Zertifikate
        List<DigitalesZertifikat> zertifikate = zertifikatRepository.findBySchiesstand(schiesstand);
        zertifikatRepository.deleteAll(zertifikate);

        // Lösche alle zugehörigen Einträge
        List<SchiessnachweisEintrag> eintraege = eintragRepository.findBySchiesstand(schiesstand);
        eintragRepository.deleteAll(eintraege);
        log.info("Gelöschte {} Einträge und {} Zertifikate für Schießstand {}", eintraege.size(), zertifikate.size(), schiesstandId);

        // Lösche den Schießstand selbst
        schiesstandRepository.delete(schiesstand);
        log.info("Schießstand {} erfolgreich gelöscht", schiesstandId);
    }

    /**
     * Setzt einen neuen Aufseher/Inhaber für einen gewerblichen Schießstand.
     * Erstellt ein Zertifikat für den neuen Aufseher und widerruft das alte.
     *
     * @param schiesstand Der Schießstand
     * @param neuerAufseher Der neue Aufseher
     */
    @Transactional
    public void setzeSchiesstandAufseher(Schiesstand schiesstand, Benutzer neuerAufseher) {
        Benutzer alterAufseher = schiesstand.getAufseher();
        
        // Altes Zertifikat widerrufen, falls vorhanden
        if (alterAufseher != null) {
            Optional<DigitalesZertifikat> altesZertifikat = zertifikatRepository
                    .findByBenutzerAndSchiesstand(alterAufseher, schiesstand);
            if (altesZertifikat.isPresent()) {
                DigitalesZertifikat zert = altesZertifikat.get();
                zert.setWiderrufen(true);
                zert.setWiderrufenAm(LocalDateTime.now());
                zert.setWiderrufsGrund("Schießstand-Aufseher-Funktion beendet");
                zertifikatRepository.save(zert);
                log.info("Zertifikat von {} für Schießstand {} widerrufen (SN: {})", 
                    alterAufseher.getVollstaendigerName(), schiesstand.getName(), zert.getSeriennummer());
            }
            
            // Rolle auf SCHUETZE zurücksetzen, falls keine anderen Aufseherfunktionen
            boolean hatAndereAufseherFunktion = alterAufseher.getVereinsmitgliedschaften().stream()
                .anyMatch(m -> Boolean.TRUE.equals(m.getIstAufseher()) || Boolean.TRUE.equals(m.getIstVereinschef()));
            
            if (!hatAndereAufseherFunktion && alterAufseher.getRolle() == BenutzerRolle.SCHIESSSTAND_AUFSEHER) {
                alterAufseher.setRolle(BenutzerRolle.SCHUETZE);
                benutzerRepository.save(alterAufseher);
                log.info("Rolle von {} auf SCHUETZE zurückgesetzt", alterAufseher.getVollstaendigerName());
            }
        }
        
        // Neuen Aufseher setzen
        if (neuerAufseher != null) {
            schiesstand.setAufseher(neuerAufseher);
            schiesstandRepository.save(schiesstand);
            
            // Zertifikat für neuen Aufseher erstellen
            try {
                // Prüfen, ob bereits ein Zertifikat für diesen Schießstand existiert
                Optional<DigitalesZertifikat> bestehendesZertifikat = 
                    zertifikatRepository.findByBenutzerAndSchiesstand(neuerAufseher, schiesstand);
                
                if (bestehendesZertifikat.isEmpty()) {
                    DigitalesZertifikat neuesZertifikat = pkiService.createSchiesstandaufseheCertificate(neuerAufseher, schiesstand);
                    log.info("Zertifikat für neuen Schießstand-Aufseher {} erstellt (SN: {})", 
                        neuerAufseher.getVollstaendigerName(), neuesZertifikat.getSeriennummer());
                } else {
                    log.info("Schießstand-Aufseher {} hat bereits ein gültiges Zertifikat für Schießstand {}", 
                        neuerAufseher.getVollstaendigerName(), schiesstand.getName());
                }
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Schießstand-Aufseher-Zertifikats für {}", 
                    neuerAufseher.getVollstaendigerName(), e);
                throw new RuntimeException("Zertifikat konnte nicht erstellt werden: " + e.getMessage(), e);
            }
            
            // Rolle auf SCHIESSSTAND_AUFSEHER setzen
            if (neuerAufseher.getRolle() != BenutzerRolle.SCHIESSSTAND_AUFSEHER && 
                neuerAufseher.getRolle() != BenutzerRolle.ADMIN) {
                neuerAufseher.setRolle(BenutzerRolle.SCHIESSSTAND_AUFSEHER);
                benutzerRepository.save(neuerAufseher);
                log.info("Rolle von {} auf SCHIESSSTAND_AUFSEHER gesetzt", neuerAufseher.getVollstaendigerName());
            }
        }
    }
}
