package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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
    List<Verein> findByVerband(Verband verband);

    /**
     * Findet einen Verein anhand der Vereinsnummer.
     *
     * @param vereinsNummer Die Vereinsnummer
     * @return Optional mit Verein
     */
    Optional<Verein> findByVereinsNummer(String vereinsNummer);

    /**
     * Findet Vereine nach Namen (Teilstring-Suche).
     *
     * @param name Suchbegriff
     * @return Liste der Vereine
     */
    List<Verein> findByNameContainingIgnoreCase(String name);

    /**
     * Findet alle Vereine mit eager loading des Verbands und Mitgliedschaften.
     *
     * @return Liste aller Vereine
     */
    @Override
    @EntityGraph(attributePaths = {"verband", "mitgliedschaften"})
    List<Verein> findAll();

    /**
     * Findet einen Verein mit geladenen Mitgliedschaften und Verband anhand der ID.
     *
     * @param id Die Vereins-ID
     * @return Optional mit Verein
     */
    @Override
    @EntityGraph(attributePaths = {"verband", "mitgliedschaften"})
    Optional<Verein> findById(Long id);
}
