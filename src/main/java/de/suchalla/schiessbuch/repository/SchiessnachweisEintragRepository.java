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
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetween(Benutzer schuetze, LocalDate von, LocalDate bis);

    /**
     * Sicher: Findet alle Einträge eines Schützen in einem Zeitraum und lädt Schiesstand sowie Verein via JOIN FETCH.
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH s.verein " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schuetze = :schuetze AND e.datum BETWEEN :von AND :bis")
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenWithVerein(@Param("schuetze") Benutzer schuetze, @Param("von") LocalDate von, @Param("bis") LocalDate bis);

    /**
     * Findet alle signierten Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @param status Der Status
     * @return Liste der Einträge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenAndStatus(
            Benutzer schuetze, LocalDate von, LocalDate bis, EintragStatus status);

    /**
     * Sicher: wie oben, aber mit Status-Filter.
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH s.verein " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schuetze = :schuetze AND e.datum BETWEEN :von AND :bis AND e.status = :status")
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenAndStatusWithVerein(@Param("schuetze") Benutzer schuetze, @Param("von") LocalDate von, @Param("bis") LocalDate bis, @Param("status") EintragStatus status);

    /**
     * Findet alle Einträge an einem SchieÃŸstand.
     *
     * @param schiesstand Der SchieÃŸstand
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Sicher: Findet alle Einträge an einem Schießstand und lädt Verein mit.
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH s.verein " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schiesstand = :schiesstand")
    List<SchiessnachweisEintrag> findBySchiesstandWithVerein(@Param("schiesstand") Schiesstand schiesstand);

    /**
     * Findet alle EintrÃ¤ge an einem SchieÃŸstand mit bestimmtem Status.
     *
     * @param schiesstand Der SchieÃŸstand
     * @param status Der Status
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstandAndStatus(Schiesstand schiesstand, EintragStatus status);

    /**
     * Sicher: wie oben, mit Status-Filter.
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
            "LEFT JOIN FETCH e.schuetze " +
            "LEFT JOIN FETCH e.disziplin " +
            "LEFT JOIN FETCH e.schiesstand s " +
            "LEFT JOIN FETCH s.verein " +
            "LEFT JOIN FETCH e.aufseher " +
            "LEFT JOIN FETCH e.zertifikat " +
            "WHERE e.schiesstand = :schiesstand AND e.status = :status")
    List<SchiessnachweisEintrag> findBySchiesstandAndStatusWithVerein(@Param("schiesstand") Schiesstand schiesstand, @Param("status") EintragStatus status);


    /**
     * ZÃ¤hlt EintrÃ¤ge eines SchÃ¼tzen mit bestimmtem Status.
     *
     * @param schuetze Der SchÃ¼tze
     * @param status Der Status
     * @return Anzahl der EintrÃ¤ge
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
     * Findet alle Einträge eines Schützen ohne Datumsfilter.
     *
     * @param schuetze Der Schütze
     * @return Liste der Einträge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetze(Benutzer schuetze);

}