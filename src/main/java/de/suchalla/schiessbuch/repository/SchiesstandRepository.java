package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für Schiesstand-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface SchiesstandRepository extends JpaRepository<Schiesstand, Long> {

    /**
     * Findet alle Schießstände eines Vereins.
     *
     * @param verein Der Verein
     * @return Liste der Schießstände
     */
    List<Schiesstand> findByVerein(Verein verein);

    /**
     * Findet alle Schießstände mit Verein und Aufseher.
     *
     * @return Liste aller Schießstände mit Verein und Aufseher
     */
    @Query("SELECT DISTINCT s FROM Schiesstand s LEFT JOIN FETCH s.verein LEFT JOIN FETCH s.aufseher")
    List<Schiesstand> findAllWithVerein();

    /**
     * Findet alle Schießstände mit Verein und Aufseher für die Vereine eines Benutzers.
     *
     * @param vereine Liste der Vereine
     * @return Liste der Schießstände mit Verein und Aufseher
     */
    @Query("SELECT DISTINCT s FROM Schiesstand s LEFT JOIN FETCH s.verein LEFT JOIN FETCH s.aufseher WHERE s.verein IN :vereine")
    List<Schiesstand> findByVereinInWithFetch(List<Verein> vereine);
}
