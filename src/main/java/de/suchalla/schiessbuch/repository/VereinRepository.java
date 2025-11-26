package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Verein-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface VereinRepository extends JpaRepository<Verein, Long> {

    /**
     * Findet alle Vereine eines Verbands.
     *
     * @param verband Der Verband
     * @return Liste der Vereine
     */
    List<Verein> findByVerbaendeContaining(Verband verband);

    /**
     * Findet einen Verein anhand der Vereinsnummer.
     *
     * @param vereinsNummer Die Vereinsnummer
     * @return Optional mit Verein
     */
    // Method findByVereinsNummer removed as Vereinsnummer was deprecated


    /**
     * Findet alle Vereine mit Verband und Mitgliedschaften.
     *
     * @return Liste aller Vereine
     */
    @EntityGraph(attributePaths = {"verbaende", "mitgliedschaften", "mitgliedschaften.benutzer"})
    List<Verein> findAll();

    /**
     * Findet einen Verein mit geladenen Mitgliedschaften und Verbänden anhand der ID.
     *
     * @param id Die Vereins-ID
     * @return Optional mit Verein
     */
    @EntityGraph(attributePaths = {"verbaende", "mitgliedschaften", "mitgliedschaften.benutzer"})
    Optional<Verein> findById(Long id);

    /**
     * Liefert nur die Namen aller Vereine (als Strings). Wird verwendet, um LazyInitializationExceptions
     * zu vermeiden, wenn die View nur die Namen für einen Filter benötigt.
     *
     * @return Liste mit Vereinsnamen
     */
    @Query("SELECT v.name FROM Verein v")
    List<String> findAllNames();
}
