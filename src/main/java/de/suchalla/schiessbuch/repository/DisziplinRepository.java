package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * @param verbandId Die ID des Verbands
     * @return Liste der Disziplinen
     */
    List<Disziplin> findByVerbandId(Long verbandId);

    /**
     * Findet alle nicht-archivierten Disziplinen eines Verbands.
     *
     * @param verbandId Die ID des Verbands
     * @param archiviert Der Archivierungsstatus (false für aktive Disziplinen)
     * @return Liste der nicht-archivierten Disziplinen
     */
    List<Disziplin> findByVerbandIdAndArchiviert(Long verbandId, Boolean archiviert);

    /**
     * Findet alle Disziplinen eines Verbands (auch archivierte).
     *
     * @param verbandId Die ID des Verbands
     * @return Liste aller Disziplinen
     */
    List<Disziplin> findAllByVerbandId(Long verbandId);
}
