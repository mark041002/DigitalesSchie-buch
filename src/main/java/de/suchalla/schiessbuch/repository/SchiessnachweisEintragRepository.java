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
     * Findet alle EintrÃ¤ge eines SchÃ¼tzen in einem Zeitraum.
     *
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetween(Benutzer schuetze, LocalDate von, LocalDate bis);

    /**
     * Findet alle signierten EintrÃ¤ge eines SchÃ¼tzen in einem Zeitraum.
     *
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @param status Der Status
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenAndStatus(
            Benutzer schuetze, LocalDate von, LocalDate bis, EintragStatus status);

    /**
     * Findet alle EintrÃ¤ge an einem SchieÃŸstand.
     *
     * @param schiesstand Der SchieÃŸstand
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Findet alle EintrÃ¤ge an einem SchieÃŸstand mit bestimmtem Status.
     *
     * @param schiesstand Der SchieÃŸstand
     * @param status Der Status
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstandAndStatus(Schiesstand schiesstand, EintragStatus status);

    /**
     * Findet alle EintrÃ¤ge eines SchÃ¼tzen mit eager loading aller Beziehungen.
     *
     * @param schuetze Der SchÃ¼tze
     * @return Liste der EintrÃ¤ge
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
     * Findet alle EintrÃ¤ge eines SchieÃŸstands fÃ¼r einen bestimmten SchÃ¼tzen.
     *
     * @param schiesstand Der SchieÃŸstand
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der EintrÃ¤ge
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
     * Findet alle unsignierten EintrÃ¤ge (OFFEN oder UNSIGNIERT) fÃ¼r eine Liste von SchieÃŸstÃ¤nden.
     *
     * @param schiesstaende Liste der SchieÃŸstÃ¤nde
     * @return Liste der unsignierten EintrÃ¤ge
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