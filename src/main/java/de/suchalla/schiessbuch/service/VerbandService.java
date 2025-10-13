package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.repository.VerbandRepository;
import de.suchalla.schiessbuch.repository.VereinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service für Verbands- und Vereinsverwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VerbandService {

    private final VerbandRepository verbandRepository;
    private final VereinRepository vereinRepository;
    private final VereinsmitgliedschaftService vereinsmitgliedschaftService;

    /**
     * Erstellt einen neuen Verband.
     *
     * @param verband Der zu erstellende Verband
     * @return Der gespeicherte Verband
     * @throws IllegalArgumentException wenn Verbandsname bereits existiert
     */
    public Verband erstelleVerband(Verband verband) {
        if (verbandRepository.existsByName(verband.getName())) {
            throw new IllegalArgumentException("Verband mit diesem Namen existiert bereits");
        }
        return verbandRepository.save(verband);
    }

    /**
     * Findet einen Verband anhand der ID.
     *
     * @param id Die Verbands-ID
     * @return Optional mit Verband
     */
    @Transactional(readOnly = true)
    public Optional<Verband> findeVerband(Long id) {
        return verbandRepository.findById(id);
    }

    /**
     * Findet alle Verbände.
     *
     * @return Liste aller Verbände
     */
    @Transactional(readOnly = true)
    public List<Verband> findeAlleVerbaende() {
        return verbandRepository.findAllWithVereine();
    }

    /**
     * Alias für EAGER-Laden aller Verbände (wird in UI verwendet).
     *
     * @return Liste aller Verbände mit Vereinen
     */
    @Transactional(readOnly = true)
    public List<Verband> findeAlleVerbaendeMitVereinen() {
        return verbandRepository.findAllWithVereine();
    }

    /**
     * Prüft, ob ein Benutzer ein aktives Mitglied in einem Verband ist (über die Vereine).
     *
     * @param benutzer  Der Benutzer
     * @param verbandId Die Verbands-ID
     * @return true wenn der Benutzer in mindestens einem Verein des Verbands aktiv ist
     */
    @Transactional(readOnly = true)
    public boolean istMitgliedImVerband(Benutzer benutzer, Long verbandId) {
        if (benutzer == null || verbandId == null) {
            return false;
        }
        return vereinsmitgliedschaftService.findeVerbaendeVonBenutzer(benutzer).stream()
                .anyMatch(v -> v.getId() != null && v.getId().equals(verbandId));
    }

    /**
     * Lässt einen Benutzer einem Verband beitreten (durch Beitritt zu einem der Vereine des Verbands).
     * Wählt den ersten vorhandenen Verein im Verband als Zielverein.
     *
     * @param benutzer  Der Benutzer
     * @param verbandId Die Verbands-ID
     */
    public void beitretenZuVerband(Benutzer benutzer, Long verbandId) {
        Verband verband = findeVerband(verbandId).orElseThrow(() -> new IllegalArgumentException("Verband nicht gefunden"));
        List<Verein> vereine = findeVereineVonVerband(verband);
        if (vereine.isEmpty()) {
            throw new IllegalArgumentException("Kein Verein im Verband zum Beitreten vorhanden");
        }
        // Beitreten zum ersten Verein des Verbands und automatisch genehmigen
        var mitgliedschaft = vereinsmitgliedschaftService.vereinBeitreten(benutzer, vereine.get(0));
        vereinsmitgliedschaftService.genehmigeAnfrage(mitgliedschaft.getId());
    }

    /**
     * Lässt einen Benutzer aus allen Vereinen eines Verbands austreten (setzt Mitgliedschaften auf beendet).
     *
     * @param benutzer  Der Benutzer
     * @param verbandId Die Verbands-ID
     */
    public void austretenAusVerband(Benutzer benutzer, Long verbandId) {
        if (benutzer == null || verbandId == null) {
            return;
        }
        List<Vereinsmitgliedschaft> mitgliedschaften = vereinsmitgliedschaftService.findeMitgliedschaftenVonBenutzer(benutzer);
        mitgliedschaften.stream()
                .filter(m -> m.getVerein() != null && m.getVerein().getVerband() != null)
                .filter(m -> verbandId.equals(m.getVerein().getVerband().getId()))
                .forEach(m -> vereinsmitgliedschaftService.mitgliedEntfernen(m.getId()));
    }

    /**
     * Aktualisiert einen Verband.
     *
     * @param verband Der zu aktualisierende Verband
     * @return Der aktualisierte Verband
     */
    public Verband aktualisiereVerband(Verband verband) {
        return verbandRepository.save(verband);
    }

    /**
     * Löscht einen Verband.
     *
     * @param verbandId Die Verbands-ID
     */
    public void loescheVerband(Long verbandId) {
        verbandRepository.deleteById(verbandId);
    }

    /**
     * Erstellt einen neuen Verein.
     *
     * @param verein Der zu erstellende Verein
     * @return Der gespeicherte Verein
     */
    public Verein erstelleVerein(Verein verein) {
        return vereinRepository.save(verein);
    }

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
     * Findet alle Vereine.
     *
     * @return Liste aller Vereine
     */
    @Transactional(readOnly = true)
    public List<Verein> findeAlleVereine() {
        return vereinRepository.findAll();
    }

    /**
     * Findet alle Vereine eines Verbands.
     *
     * @param verband Der Verband
     * @return Liste der Vereine
     */
    @Transactional(readOnly = true)
    public List<Verein> findeVereineVonVerband(Verband verband) {
        return vereinRepository.findByVerband(verband);
    }

    /**
     * Findet Vereine nach Namen.
     *
     * @param name Suchbegriff
     * @return Liste der Vereine
     */
    @Transactional(readOnly = true)
    public List<Verein> sucheVereine(String name) {
        return vereinRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Aktualisiert einen Verein.
     *
     * @param verein Der zu aktualisierende Verein
     * @return Der aktualisierte Verein
     */
    public Verein aktualisiereVerein(Verein verein) {
        return vereinRepository.save(verein);
    }

    /**
     * Löscht einen Verein.
     *
     * @param vereinId Die Vereins-ID
     */
    public void loescheVerein(Long vereinId) {
        vereinRepository.deleteById(vereinId);
    }
}
