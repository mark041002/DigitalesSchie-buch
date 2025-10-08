package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Verband;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Verband-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface VerbandRepository extends JpaRepository<Verband, Long> {

    /**
     * Findet einen Verband anhand des Namens.
     *
     * @param name Der Verbandsname
     * @return Optional mit Verband
     */
    Optional<Verband> findByName(String name);

    /**
     * Prüft, ob ein Verband mit dem Namen existiert.
     *
     * @param name Der Verbandsname
     * @return true wenn existiert
     */
    boolean existsByName(String name);

    /**
     * Findet alle Verbände mit eager loading der Vereine.
     *
     * @return Liste aller Verbände mit Vereinen
     */
    @Query("SELECT DISTINCT v FROM Verband v LEFT JOIN FETCH v.vereine")
    List<Verband> findAllWithVereine();
}
