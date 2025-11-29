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

        Verein existierend = vereinRepository.findById(verein.getId())
                .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        // Aktualisiere die Felder
        existierend.setName(verein.getName());
        existierend.setAdresse(verein.getAdresse());
        existierend.setBeschreibung(verein.getBeschreibung());

        if (verein.getVerbaende() != null && !verein.getVerbaende().isEmpty()) {
            existierend.getVerbaende().clear();
            existierend.getVerbaende().addAll(verein.getVerbaende());
        } else {
            existierend.getVerbaende().clear();
        }

        vereinRepository.save(existierend);
    }

    /**
     * Liefert alle Vereinsnamen als String-Liste. Wrapper um Repository-Methode.
     *
     * @return Liste aller Vereinsnamen
     */
    @Transactional(readOnly = true)
    public java.util.List<String> findAllVereinsnamen() {
        return vereinRepository.findAllNames();
    }

    /**
     * Findet einen Verein anhand seiner ID.
     *
     * @param id Die Vereins-ID
     * @return Der Verein
     * @throws IllegalArgumentException wenn der Verein nicht gefunden wird oder ID null ist
     */
    @Transactional(readOnly = true)
    public Verein findeVerein(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Vereins-ID darf nicht null sein");
        }
        return vereinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Verein mit ID " + id + " nicht gefunden"));
    }

}
