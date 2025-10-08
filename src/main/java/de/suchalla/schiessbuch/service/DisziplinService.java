package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.repository.DisziplinRepository;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    /**
     * Erstellt eine neue Disziplin.
     *
     * @param disziplin Die zu erstellende Disziplin
     * @return Die gespeicherte Disziplin
     */
    public Disziplin erstelleDisziplin(Disziplin disziplin) {
        return disziplinRepository.save(disziplin);
    }

    /**
     * Findet eine Disziplin anhand der ID.
     *
     * @param id Die Disziplin-ID
     * @return Optional mit Disziplin
     */
    @Transactional(readOnly = true)
    public Optional<Disziplin> findeDisziplin(Long id) {
        return disziplinRepository.findById(id);
    }

    /**
     * Findet alle Disziplinen.
     *
     * @return Liste aller Disziplinen
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeAlleDisziplinen() {
        return disziplinRepository.findAllWithVerband();
    }

    /**
     * Findet alle Disziplinen eines Verbands.
     *
     * @param verband Der Verband
     * @return Liste der Disziplinen
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeDisziplinenVonVerband(Verband verband) {
        return disziplinRepository.findByVerband(verband);
    }

    /**
     * Aktualisiert eine Disziplin.
     *
     * @param disziplin Die zu aktualisierende Disziplin
     * @return Die aktualisierte Disziplin
     */
    public Disziplin aktualisiereDisziplin(Disziplin disziplin) {
        return disziplinRepository.save(disziplin);
    }

    /**
     * Löscht eine Disziplin.
     *
     * @param disziplinId Die Disziplin-ID
     */
    public void loescheDisziplin(Long disziplinId) {
        disziplinRepository.deleteById(disziplinId);
    }

    /**
     * Erstellt einen neuen Schießstand.
     *
     * @param schiesstand Der zu erstellende Schießstand
     * @return Der gespeicherte Schießstand
     */
    public Schiesstand erstelleSchiesstand(Schiesstand schiesstand) {
        return schiesstandRepository.save(schiesstand);
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
     * Findet alle Schießstände.
     *
     * @return Liste aller Schießstände
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> findeAlleSchiesstaende() {
        return schiesstandRepository.findAllWithVerein();
    }

    /**
     * Sucht Schießstände nach Namen.
     *
     * @param name Suchbegriff
     * @return Liste der Schießstände
     */
    @Transactional(readOnly = true)
    public List<Schiesstand> sucheSchiesstaende(String name) {
        return schiesstandRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Aktualisiert einen Schießstand.
     *
     * @param schiesstand Der zu aktualisierende Schießstand
     * @return Der aktualisierte Schießstand
     */
    public Schiesstand aktualisiereSchiesstand(Schiesstand schiesstand) {
        return schiesstandRepository.save(schiesstand);
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

        if (!schiesstand.getEintraege().isEmpty()) {
            throw new IllegalStateException(
                    "Schießstand kann nicht gelöscht werden, da noch Einträge vorhanden sind");
        }

        schiesstandRepository.delete(schiesstand);
    }
}
