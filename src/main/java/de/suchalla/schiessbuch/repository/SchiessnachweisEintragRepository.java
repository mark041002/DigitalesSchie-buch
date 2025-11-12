package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository für SchiessnachweisEintrag-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface SchiessnachweisEintragRepository extends JpaRepository<SchiessnachweisEintrag, Long> {

    /**
     * Findet alle Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetween(Benutzer schuetze, LocalDate von, LocalDate bis);

    /**
     * Findet alle signierten Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @param status Der Status
     * @return Liste der Eintrage
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenAndStatus(
            Benutzer schuetze, LocalDate von, LocalDate bis, EintragStatus status);

    /**
     * Findet alle Einträge an einem Schießstand.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der Eintrage
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Findet alle Einträge an einem Schießstand mit bestimmtem Status.
     *
     * @param schiesstand Der Schießstand
     * @param status Der Status
     * @return Liste der Eintrage
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstandAndStatus(Schiesstand schiesstand, EintragStatus status);

    /**
     * Findet alle Eintrage eines Schützen mit eager loading aller Beziehungen.
     *
     * @param schuetze Der Schütze
     * @return Liste der Eintrage
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin d " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schuetze = :schuetze")
    List<SchiessnachweisEintrag> findBySchuetzeWithDetails(@Param("schuetze") Benutzer schuetze);

    /**
     * Findet alle Eintrage eines Schießstands für einen bestimmten Schützen.
     *
     * @param schiesstand Der Schießstand
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Eintrage
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schiesstand = :schiesstand " +
            "AND e.schuetze = :schuetze AND e.datum BETWEEN :von AND :bis ORDER BY e.datum DESC")
    List<SchiessnachweisEintrag> findBySchiesstandUndSchuetzeImZeitraum(
            @Param("schiesstand") Schiesstand schiesstand,
            @Param("schuetze") Benutzer schuetze,
            @Param("von") LocalDate von,
            @Param("bis") LocalDate bis);

    /**
     * Zahlt Eintrage eines Schützen mit bestimmtem Status.
     *
     * @param schuetze Der Schütze
     * @param status Der Status
     * @return Anzahl der Eintrage
     */
    long countBySchuetzeAndStatus(Benutzer schuetze, EintragStatus status);

    /**
     * Findet einen Eintrag mit allen Beziehungen.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.id = :id")
    Optional<SchiessnachweisEintrag> findByIdWithDetails(@Param("id") Long id);

    /**
     * Findet einen Eintrag mit allen Beziehungen inkl. Verein über Schiesstand.
     * Verhindert LazyInitializationException beim Zugriff auf schiesstand.verein.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH s.verein " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.id = :id")
    Optional<SchiessnachweisEintrag> findByIdWithVerein(@Param("id") Long id);

    /**
     * Findet alle unsignierten Eintrage (OFFEN oder UNSIGNIERT) für eine Liste von Schießstanden.
     *
     * @param schiesstaende Liste der Schießstande
     * @return Liste der unsignierten Eintrage
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE s IN :schiesstaende " +
            "AND (e.status = 'OFFEN' OR e.status = 'UNSIGNIERT') " +
            "ORDER BY e.datum DESC")
    List<SchiessnachweisEintrag> findUnsignierteEintraegeForSchiesstaende(@Param("schiesstaende") List<Schiesstand> schiesstaende);

}