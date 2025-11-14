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

@Repository
public interface VereinsmitgliedschaftRepository extends JpaRepository<Vereinsmitgliedschaft, Long> {

    /**
     * Findet die (optionale) Mitgliedschaft eines Benutzers.
     *
     * Hinweis: Ein Benutzer kann mehrere Mitgliedschaften haben. Diese Methode gibt alle Mitgliedschaften zurück.
     *
     * @param benutzer Der Benutzer
     * @return Liste der Mitgliedschaften
     */
    List<Vereinsmitgliedschaft> findByBenutzer(Benutzer benutzer);

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
     * Findet alle Mitgliedschaften eines Vereins.
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
     * Findet alle Mitgliedschaften eines Vereins mit allen Statussen.
     * @TODO KANN MAN DAS ÄNDERN?
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.benutzer " +
           "LEFT JOIN FETCH m.verein " +
           "WHERE m.verein = :verein")
    List<Vereinsmitgliedschaft> findByVereinWithDetails(@Param("verein") Verein verein);

    // Finde Vereinsmitgliedschaften, bei denen das Mitglied als Vereinschef markiert ist
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.benutzer " +
           "LEFT JOIN FETCH m.verein " +
           "WHERE m.verein = :verein AND m.istVereinschef = true")
    List<Vereinsmitgliedschaft> findByVereinAndIstVereinschefTrue(@Param("verein") Verein verein);

    // Finde Vereinsmitgliedschaften, bei denen das Mitglied als Aufseher markiert ist
    @Query("SELECT DISTINCT m FROM Vereinsmitgliedschaft m " +
           "LEFT JOIN FETCH m.benutzer " +
           "LEFT JOIN FETCH m.verein " +
           "WHERE m.verein = :verein AND m.istAufseher = true")
    List<Vereinsmitgliedschaft> findByVereinAndIstAufseherTrue(@Param("verein") Verein verein);
}
