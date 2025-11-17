package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.VerbandMapper;
import de.suchalla.schiessbuch.mapper.VereinMapper;
import de.suchalla.schiessbuch.model.dto.VerbandDTO;
import de.suchalla.schiessbuch.model.dto.VereinDTO;
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
    private final VerbandMapper verbandMapper;
    private final VereinMapper vereinMapper;

    /**
     * Erstellt einen neuen Verband.
     *
     * @param verband Der zu erstellende Verband
     * @throws IllegalArgumentException wenn Verbandsname bereits existiert
     */
    public void erstelleVerband(Verband verband) {
        if (verbandRepository.existsByName(verband.getName())) {
            throw new IllegalArgumentException("Verband mit diesem Namen existiert bereits");
        }
        verbandRepository.save(verband);
    }

    /**
     * Findet einen Verband anhand der ID.
     *
     * @param id Die Verbands-ID
     * @return Optional mit Verband
     */
    @Transactional(readOnly = true)
    public Verband findeVerband(Long id) {
        return verbandRepository.findById(id).orElse(null);
    }

    /**
     * Findet alle Verbände als DTOs.
     *
     * @return Liste aller Verbände als DTOs
     */
    @Transactional(readOnly = true)
    public List<VerbandDTO> findeAlleVerbaende() {
        List<Verband> entities = verbandRepository.findAllWithVereine();
        return verbandMapper.toDTOList(entities);
    }

    /**
     * Findet alle Verbände als Entities (für interne Verwendung wie ComboBoxen).
     *
     * @return Liste aller Verbände als Entities
     */
    @Transactional(readOnly = true)
    public List<Verband> findeAlleVerbaendeEntities() {
        return verbandRepository.findAllWithVereine();
    }

    /**
     * Alias für EAGER-Laden aller Verbände als DTOs (wird in UI verwendet).
     *
     * @return Liste aller Verbände als DTOs
     */
    @Transactional(readOnly = true)
    public List<VerbandDTO> findeAlleVerbaendeMitVereinen() {
        return findeAlleVerbaende();
    }

    /**
     * EAGER-Laden aller Verbände als Entities (für interne Verwendung).
     *
     * @return Liste aller Verbände als Entities
     */
    @Transactional(readOnly = true)
    public List<Verband> findeAlleVerbaendeMitVereinenEntities() {
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
        Verband verband = findeVerband(verbandId);
        if(verband == null)
            throw new IllegalArgumentException("Verband nicht gefunden");

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
        List<Vereinsmitgliedschaft> mitgliedschaften = vereinsmitgliedschaftService.findeMitgliedschaftenVonBenutzerEntities(benutzer);
        mitgliedschaften.stream()
                .filter(m -> m.getVerein() != null && m.getVerein().getVerbaende() != null)
                .filter(m -> m.getVerein().getVerbaende().stream()
                        .anyMatch(v -> verbandId.equals(v.getId())))
                .forEach(m -> vereinsmitgliedschaftService.mitgliedEntfernen(m.getId()));
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
     * Findet einen Verein anhand der Vereinsnummer.
     *
     * @param vereinsNummer Die Vereinsnummer
     * @return Optional mit Verein
     */
    @Transactional(readOnly = true)
    public Optional<Verein> findeVereinByVereinsNummer(String vereinsNummer) {
        if (vereinsNummer == null) {
            return Optional.empty();
        }
        return vereinRepository.findByVereinsNummer(vereinsNummer.trim());
    }

    /**
     * Findet alle Vereine als DTOs.
     *
     * @return Liste aller Vereine als DTOs
     */
    @Transactional(readOnly = true)
    public List<VereinDTO> findeAlleVereine() {
        List<Verein> entities = vereinRepository.findAll();
        return vereinMapper.toDTOList(entities);
    }

    /**
     * Findet alle Vereine als Entities (für interne Verwendung wie ComboBoxen).
     *
     * @return Liste aller Vereine als Entities
     */
    @Transactional(readOnly = true)
    public List<Verein> findeAlleVereineEntities() {
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
        return vereinRepository.findByVerbaendeContaining(verband);
    }

    /**
     * Löscht einen Verein.
     *
     * @param vereinId Die Vereins-ID
     * @throws IllegalStateException wenn Verein noch Mitglieder hat
     */
    public void loescheVerein(Long vereinId) {
        Verein verein = vereinRepository.findById(vereinId)
                .orElseThrow(() -> new IllegalArgumentException("Verein nicht gefunden"));

        if (!verein.getMitgliedschaften().isEmpty()) {
            throw new IllegalStateException(
                    "Verein kann nicht gelöscht werden, da noch Mitglieder vorhanden sind");
        }

        vereinRepository.delete(verein);
    }
}
