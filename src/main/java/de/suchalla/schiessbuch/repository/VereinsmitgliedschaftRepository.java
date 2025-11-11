package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Vereinsmitgliedschaft-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface VereinsmitgliedschaftRepository extends JpaRepository<Vereinsmitgliedschaft, Long> {

    /**
     * Findet alle Mitgliedschaften eines Benutzers.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByBenutzer(Benutzer benutzer);

    /**
     * Findet alle aktiven Mitgliedschaften eines Benutzers.
     *
     * @param benutzer Der Benutzer
     * @param aktiv Aktiv-Status
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByBenutzerAndAktiv(Benutzer benutzer, Boolean aktiv);

    /**
     * Findet alle Mitgliedschaften eines Vereins.
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByVerein(Verein verein);

    /**
     * Findet alle aktiven Mitgliedschaften eines Vereins.
     *
     * @param verein Der Verein
     * @param aktiv Aktiv-Status
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByVereinAndAktiv(Verein verein, Boolean aktiv);

    /**
     * Findet alle Mitgliedschaften eines Vereins mit bestimmtem Status.
     *
     * @param verein Der Verein
     * @param status Der Status
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByVereinAndStatus(Verein verein, MitgliedschaftStatus status);

    /**
     * Prüft, ob eine Mitgliedschaft bereits existiert.
     *
     * @param benutzer Der Benutzer
     * @param verein Der Verein
     * @return Optional mit Mitgliedschaft
     */
    Optional<Vereinsmitgliedschaft> findByBenutzerAndVerein(Benutzer benutzer, Verein verein);

    /**
     * Gibt alle Mitgliedschaften eines Benutzers für einen Verein zurück (kann mehrere Einträge enthalten).
     * Wird verwendet, um mit eventuell vorhandenen Duplikaten robust umzugehen.
     *
     * @param benutzer Der Benutzer
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findAllByBenutzerAndVerein(Benutzer benutzer, Verein verein);

    /**
     * Findet alle Mitgliedschaften eines Benutzers mit eager loading.
     * Die Verbände werden bereits via @ManyToMany(fetch = FetchType.EAGER) in Verein geladen.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften
     */
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.verein v " +
           "WHERE m.benutzer = :benutzer")
    List<Vereinsmitgliedschaft> findByBenutzerWithDetails(@Param("benutzer") Benutzer benutzer);

    /**
     * Findet alle Mitgliedschaften eines Vereins mit eager loading.
     *
     * @param verein Der Verein
     * @param status Der Status
     * @return Liste der Mitgliedschaften
     */
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.benutzer " +
           "LEFT JOIN FETCH m.verein " +
           "WHERE m.verein = :verein AND m.status = :status")
    List<Vereinsmitgliedschaft> findByVereinAndStatusWithDetails(
            @Param("verein") Verein verein,
            @Param("status") MitgliedschaftStatus status);

    /**
     * Findet alle Mitgliedschaften eines Vereins mit eager loading (alle Status).
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.benutzer " +
           "LEFT JOIN FETCH m.verein " +
           "WHERE m.verein = :verein")
    List<Vereinsmitgliedschaft> findByVereinWithDetails(@Param("verein") Verein verein);
}


