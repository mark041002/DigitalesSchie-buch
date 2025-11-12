package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.VereinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
     * Aktualisiert die Daten eines Vereins.
     *
     * @param verein Der Verein mit aktualisierten Daten
     */
    public void aktualisiereVerein(Verein verein) {
        if (verein.getId() == null) {
            throw new IllegalArgumentException("Verein muss eine ID haben");
        }

        Verein existierend = vereinRepository.findById(verein.getId()).orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        // Aktualisiere die Felder
        existierend.setName(verein.getName());
        existierend.setVereinsNummer(verein.getVereinsNummer());
        existierend.setAdresse(verein.getAdresse());
        existierend.setBeschreibung(verein.getBeschreibung());

        // Aktualisiere Verbände (Many-to-Many): Clear und neu hinzufügen
        if (verein.getVerbaende() != null && !verein.getVerbaende().isEmpty()) {
            existierend.getVerbaende().clear();
            existierend.getVerbaende().addAll(verein.getVerbaende());
        } else {
            existierend.getVerbaende().clear();
        }

        vereinRepository.save(existierend);
    }

    /**
     * Findet einen Verein mit geladenen Mitgliedschaften und Verbänden anhand der ID.
     *
     * @param id Die Vereins-ID
     * @return Optional mit Verein
     */
    public Verein findById(Long id){
        return vereinRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));
    }
}
