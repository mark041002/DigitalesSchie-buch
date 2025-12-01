package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.DisziplinRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service für Disziplinen und Schießstände.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DisziplinService {

    private final DisziplinRepository disziplinRepository;
    private final SchiesstandRepository schiesstandRepository;
    private final SchiessnachweisEintragRepository eintragRepository;

    /**
     * Erstellt eine neue Disziplin.
     *
     * @param disziplin Die zu erstellende Disziplin
     */
    public void erstelleDisziplin(Disziplin disziplin) {
        disziplinRepository.save(disziplin);
    }


    /**
     * Findet alle nicht-archivierten Disziplinen eines Verbands.
     * Diese Methode wird für die Auswahl bei neuen Einträgen verwendet.
     *
     * @param verbandid Die Verband-ID
     * @return Liste der nicht-archivierten Disziplinen
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeDisziplinenVonVerband(Long verbandid) {
        List<Disziplin> entities = disziplinRepository.findByVerbandIdAndArchiviert(verbandid, false);
        return entities;
    }

    /**
     * Archiviert eine Disziplin statt sie zu löschen.
     * Archivierte Disziplinen bleiben für bestehende Schießnachweis-Einträge verfügbar,
     * werden aber nicht mehr bei der Auswahl für neue Einträge angezeigt.
     *
     * @param disziplinId Die Disziplin-ID
     */
    public void loescheDisziplin(Long disziplinId) {
        Disziplin disziplin = disziplinRepository.findById(disziplinId)
                .orElseThrow(() -> new IllegalArgumentException("Disziplin nicht gefunden"));
        disziplin.setArchiviert(true);
        disziplinRepository.save(disziplin);
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
     * @return Der Schießstand
     * @throws IllegalArgumentException wenn der Schießstand nicht gefunden wird
     */
    @Transactional(readOnly = true)
    public Schiesstand findeSchiesstand(Long id) {
        return schiesstandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schießstand mit ID " + id + " nicht gefunden"));
    }

    /**
     * Findet alle Schießstände als DTOs.
     *
     * @return Liste aller Schießstände als DTOs
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> findeAlleSchiesstaende() {
        List<Schiesstand> entities = schiesstandRepository.findAllWithVerein();
        return entities;
    }

    /**
     * Findet alle nicht-archivierten Disziplinen eines Verbands als Entities (für interne Verwendung).
     *
     * @param verbandId Die Verband-ID
     * @return Liste der nicht-archivierten Disziplinen als Entities
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeDisziplinenVonVerbandEntities(Long verbandId) {
        return disziplinRepository.findByVerbandIdAndArchiviert(verbandId, false);
    }

    /**
     * Findet alle Disziplinen eines Verbands (auch archivierte) für Admin-Ansicht.
     *
     * @param verbandId Die Verband-ID
     * @return Liste aller Disziplinen
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeAlleDisziplinenVonVerband(Long verbandId) {
        return disziplinRepository.findAllByVerbandId(verbandId);
    }

    /**
     * Aktiviert eine archivierte Disziplin wieder.
     *
     * @param disziplinId Die Disziplin-ID
     */
    public void aktiviereDisziplin(Long disziplinId) {
        Disziplin disziplin = disziplinRepository.findById(disziplinId)
                .orElseThrow(() -> new IllegalArgumentException("Disziplin nicht gefunden"));
        disziplin.setArchiviert(false);
        disziplinRepository.save(disziplin);
    }

    /**
     * Zählt die Anzahl der Einträge für eine bestimmte Disziplin.
     *
     * @param disziplinId Die Disziplin-ID
     * @return Anzahl der Einträge
     */
    @Transactional(readOnly = true)
    public long zaehleEintraegeVonDisziplin(Long disziplinId) {
        Disziplin disziplin = disziplinRepository.findById(disziplinId)
                .orElseThrow(() -> new IllegalArgumentException("Disziplin nicht gefunden"));
        return eintragRepository.countByDisziplin(disziplin);
    }

    /**
     * Löscht eine Disziplin endgültig inklusive aller zugehörigen Einträge.
     * WARNUNG: Diese Aktion kann nicht rückgängig gemacht werden!
     * Sollte nur für archivierte Disziplinen verwendet werden.
     *
     * @param disziplinId Die Disziplin-ID
     */
    public void loescheDisziplinMitEintraegen(Long disziplinId) {
        Disziplin disziplin = disziplinRepository.findById(disziplinId)
                .orElseThrow(() -> new IllegalArgumentException("Disziplin nicht gefunden"));

        // Sicherheitscheck: Nur archivierte Disziplinen dürfen gelöscht werden
        if (!Boolean.TRUE.equals(disziplin.getArchiviert())) {
            throw new IllegalStateException("Nur archivierte Disziplinen können gelöscht werden. Bitte archivieren Sie die Disziplin zuerst.");
        }

        // Lösche alle Einträge, die diese Disziplin verwenden
        var eintraege = eintragRepository.findByDisziplin(disziplin);
        eintragRepository.deleteAll(eintraege);

        // Lösche die Disziplin selbst
        disziplinRepository.delete(disziplin);
    }

    /**
     * Findet alle Schießstände als Entities (für interne Verwendung).
     *
     * @return Liste aller Schießstände als Entities
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> findeAlleSchiesstaendeEntities() {
        return schiesstandRepository.findAllWithVerein();
    }

    /**
     * Aktualisiert einen Schießstand.
     *
     * @param schiesstand Der zu aktualisierende Schießstand
     */
    public void aktualisiereSchiesstand(Schiesstand schiesstand) {
        schiesstandRepository.save(schiesstand);
    }

    /**
     * Löscht einen Schießstand.
     *
     * @param schiesstandId Die Schießstand-ID
     * @throws IllegalStateException wenn Schießstand noch Einträge hat
     */
    public void loescheSchiesstand(Long schiesstandId) {
        Schiesstand schiesstand = schiesstandRepository.findById(schiesstandId)
                .orElseThrow(() -> new IllegalArgumentException("Schießstand nicht gefunden"));

        // Prüfe über Repository, ob Einträge vorhanden sind
        if (!eintragRepository.findBySchiesstand(schiesstand).isEmpty()) {
            throw new IllegalStateException(
                    "Schießstand kann nicht gelöscht werden, da noch Einträge vorhanden sind");
        }

        schiesstandRepository.delete(schiesstand);
    }

    /**
     * Findet den ersten berechtigten Schießstand für einen Benutzer.
     * Ein Benutzer ist berechtigt, wenn er:
     * - Als Aufseher direkt im Schießstand eingetragen ist, oder
     * - Aufseher oder Vereinschef im Verein des Schießstands ist
     *
     * @param benutzer Der Benutzer
     * @return Der erste berechtigte Schießstand
     * @throws IllegalArgumentException wenn der Benutzer null ist oder kein berechtigter Schießstand gefunden wird
     */
    @Transactional(readOnly = true)
    public Schiesstand findeBerechtigtenSchiesstand(de.suchalla.schiessbuch.model.entity.Benutzer benutzer) {
        if (benutzer == null) {
            throw new IllegalArgumentException("Benutzer darf nicht null sein");
        }

        List<Schiesstand> alleSchiesstaende = schiesstandRepository.findAllWithVerein();

        return alleSchiesstaende.stream()
                .filter(schiesstand -> {
                    // Prüfe ob Benutzer als Aufseher direkt im Schießstand eingetragen ist
                    boolean istStandaufseher = schiesstand.getAufseher() != null &&
                            schiesstand.getAufseher().getId().equals(benutzer.getId());

                    // Prüfe ob Benutzer Aufseher ODER Vereinschef im Verein des Schießstands ist
                    boolean istVereinsAufseherOderChef = false;
                    if (benutzer.getVereinsmitgliedschaften() != null && !benutzer.getVereinsmitgliedschaften().isEmpty()
                            && schiesstand.getVerein() != null) {
                        istVereinsAufseherOderChef = benutzer.getVereinsmitgliedschaften().stream()
                                .anyMatch(m -> m.getVerein() != null && m.getVerein().getId().equals(schiesstand.getVerein().getId()) &&
                                        (Boolean.TRUE.equals(m.getIstAufseher()) ||
                                                Boolean.TRUE.equals(m.getIstVereinschef())));
                    }

                    return istStandaufseher || istVereinsAufseherOderChef;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Kein berechtigter Schießstand für Benutzer " + benutzer.getEmail() + " gefunden"));
    }

    /**
     * Findet alle Schießstände eines Vereins.
     *
     * @param verein Der Verein
     * @return Liste der Schießstände
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> findeSchiesstaendeVonVerein(Verein verein) {
        if (verein == null) {
            return List.of();
        }
        return schiesstandRepository.findByVerein(verein);
    }
}
