package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.VereinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service für Vereinsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VereinService {

    private final VereinRepository vereinRepository;

    /**
     * Findet einen Verein anhand der ID.
     *
     * @param id Die Vereins-ID
     * @return Optional mit Verein
     */
    @Transactional(readOnly = true)
    public Optional<Verein> findeVerein(Long id) {
        return vereinRepository.findById(id);
    }

    /**
     * Gibt alle Vereine zurück.
     *
     * @return Liste aller Vereine
     */
    @Transactional(readOnly = true)
    public List<Verein> findeAlleVereine() {
        return vereinRepository.findAllWithMitgliedschaften();
    }

    /**
     * Aktualisiert die Daten eines Vereins.
     *
     * @param verein Der Verein mit aktualisierten Daten
     * @return Der aktualisierte Verein
     */
    public Verein aktualisiereVerein(Verein verein) {
        if (verein.getId() == null) {
            throw new IllegalArgumentException("Verein muss eine ID haben");
        }

        Verein existierend = vereinRepository.findById(verein.getId())
                .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        // Aktualisiere die Felder
        existierend.setName(verein.getName());
        existierend.setVereinsNummer(verein.getVereinsNummer());
        existierend.setAdresse(verein.getAdresse());
        existierend.setBeschreibung(verein.getBeschreibung());

        return vereinRepository.save(existierend);
    }

    /**
     * Erstellt einen neuen Verein.
     *
     * @param verein Der neue Verein
     * @return Der erstellte Verein
     */
    public Verein erstelleVerein(Verein verein) {
        return vereinRepository.save(verein);
    }

    /**
     * Löscht einen Verein.
     *
     * @param id Die Vereins-ID
     */
    public void loescheVerein(Long id) {
        vereinRepository.deleteById(id);
    }
}
