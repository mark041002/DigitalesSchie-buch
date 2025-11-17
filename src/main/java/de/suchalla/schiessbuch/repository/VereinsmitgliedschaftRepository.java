package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für Vereinsmitgliedschaft-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
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
     * Findet alle Mitgliedschaften eines Vereins mit bestimmtem Status.
     * Lädt Benutzer und Verein via @EntityGraph.
     *
     * @param verein Der Verein
     * @param status Der Status
     * @return Liste der Mitgliedschaften
     */
    @EntityGraph(attributePaths = {"benutzer", "verein"})
    List<Vereinsmitgliedschaft> findByVereinAndStatus(Verein verein, MitgliedschaftStatus status);

    /**
     * Findet alle Mitgliedschaften eines Vereins.
     * Lädt Benutzer und Verein via @EntityGraph.
     *
     * @param verein Der Verein
     * @return Liste der Mitgliedschaften
     */
    @EntityGraph(attributePaths = {"benutzer", "verein"})
    List<Vereinsmitgliedschaft> findByVerein(Verein verein);

    /**
     * Findet Vereinsmitgliedschaften, bei denen das Mitglied als Vereinschef markiert ist.
     * Lädt Benutzer und Verein via @EntityGraph.
     *
     * @param verein Der Verein
     * @param istVereinschef Flag für Vereinschef
     * @return Liste der Mitgliedschaften
     */
    @EntityGraph(attributePaths = {"benutzer", "verein"})
    List<Vereinsmitgliedschaft> findByVereinAndIstVereinschef(Verein verein, boolean istVereinschef);

    /**
     * Findet Vereinsmitgliedschaften, bei denen das Mitglied als Aufseher markiert ist.
     * Lädt Benutzer und Verein via @EntityGraph.
     *
     * @param verein Der Verein
     * @param istAufseher Flag für Aufseher
     * @return Liste der Mitgliedschaften
     */
    @EntityGraph(attributePaths = {"benutzer", "verein"})
    List<Vereinsmitgliedschaft> findByVereinAndIstAufseher(Verein verein, boolean istAufseher);
}
