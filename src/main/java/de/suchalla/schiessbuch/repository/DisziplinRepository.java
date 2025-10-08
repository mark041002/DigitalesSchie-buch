package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Disziplin-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface DisziplinRepository extends JpaRepository<Disziplin, Long> {

    /**
     * Findet alle Disziplinen eines Verbands.
     *
     * @param verband Der Verband
     * @return Liste der Disziplinen
     */
    List<Disziplin> findByVerband(Verband verband);

    /**
     * Findet eine Disziplin anhand des Namens und Verbands.
     *
     * @param name Der Disziplinname
     * @param verband Der Verband
     * @return Optional mit Disziplin
     */
    Optional<Disziplin> findByNameAndVerband(String name, Verband verband);

    /**
     * Findet Disziplinen nach Namen (Teilstring-Suche).
     *
     * @param name Suchbegriff
     * @return Liste der Disziplinen
     */
    List<Disziplin> findByNameContainingIgnoreCase(String name);

    /**
     * Findet alle Disziplinen mit eager loading des Verbands.
     *
     * @return Liste aller Disziplinen mit Verband
     */
    @Query("SELECT d FROM Disziplin d LEFT JOIN FETCH d.verband")
    List<Disziplin> findAllWithVerband();
}
