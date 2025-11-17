package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.DisziplinMapper;
import de.suchalla.schiessbuch.mapper.SchiesstandMapper;
import de.suchalla.schiessbuch.model.dto.DisziplinDTO;
import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
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
    private final DisziplinMapper disziplinMapper;
    private final SchiesstandMapper schiesstandMapper;

    /**
     * Erstellt eine neue Disziplin.
     *
     * @param disziplin Die zu erstellende Disziplin
     */
    public void erstelleDisziplin(Disziplin disziplin) {
        disziplinRepository.save(disziplin);
    }


    /**
     * Findet alle Disziplinen eines Verbands.
     *
     * @param verbandid Die Verband-ID
     * @return Liste der Disziplinen als DTOs
     */
    @Transactional(readOnly = true)
    public List<DisziplinDTO> findeDisziplinenVonVerband(Long verbandid) {
        List<Disziplin> entities = disziplinRepository.findByVerbandId(verbandid);
        return disziplinMapper.toDTOList(entities);
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
     * Findet alle Schießstände als DTOs.
     *
     * @return Liste aller Schießstände als DTOs
     */
    @Transactional(readOnly = true)
    public List<SchiesstandDTO> findeAlleSchiesstaende() {
        List<Schiesstand> entities = schiesstandRepository.findAllWithVerein();
        return schiesstandMapper.toDTOList(entities);
    }

    /**
     * Findet alle Disziplinen eines Verbands als Entities (für interne Verwendung).
     *
     * @param verbandId Die Verband-ID
     * @return Liste der Disziplinen als Entities
     */
    @Transactional(readOnly = true)
    public List<Disziplin> findeDisziplinenVonVerbandEntities(Long verbandId) {
        return disziplinRepository.findByVerbandId(verbandId);
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

        if (!schiesstand.getEintraege().isEmpty()) {
            throw new IllegalStateException(
                    "Schießstand kann nicht gelöscht werden, da noch Einträge vorhanden sind");
        }

        schiesstandRepository.delete(schiesstand);
    }
}
