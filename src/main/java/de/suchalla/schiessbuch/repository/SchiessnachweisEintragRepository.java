package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository für SchiessnachweisEintrag-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface SchiessnachweisEintragRepository extends JpaRepository<SchiessnachweisEintrag, Long> {

    /**
     * Findet alle Einträge eines Schützen.
     *
     * @param schuetze Der Schütze
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchuetze(Benutzer schuetze);

    /**
     * Findet alle Einträge eines Schützen mit bestimmtem Status.
     *
     * @param schuetze Der Schütze
     * @param status Der Status
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchuetzeAndStatus(Benutzer schuetze, EintragStatus status);

    /**
     * Findet alle Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetween(Benutzer schuetze, LocalDate von, LocalDate bis);

    /**
     * Findet alle signierten Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @param status Der Status
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchuetzeAndDatumBetweenAndStatus(
            Benutzer schuetze, LocalDate von, LocalDate bis, EintragStatus status);

    /**
     * Findet alle Einträge an einem Schießstand.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Findet alle Einträge an einem Schießstand in einem Zeitraum.
     *
     * @param schiesstand Der Schießstand
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchiesstandAndDatumBetween(
            Schiesstand schiesstand, LocalDate von, LocalDate bis);

    /**
     * Findet alle Einträge an einem Schießstand mit bestimmtem Status.
     *
     * @param schiesstand Der Schießstand
     * @param status Der Status
     * @return Liste der Einträge
     */
    List<SchiessnachweisEintrag> findBySchiesstandAndStatus(Schiesstand schiesstand, EintragStatus status);

    /**
     * Findet alle Einträge eines Schützen mit eager loading aller Beziehungen.
     *
     * @param schuetze Der Schütze
     * @return Liste der Einträge
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
           "LEFT JOIN FETCH e.disziplin d " +
           "LEFT JOIN FETCH e.schiesstand s " +
           "LEFT JOIN FETCH e.aufseher " +
           "WHERE e.schuetze = :schuetze")
    List<SchiessnachweisEintrag> findBySchuetzeWithDetails(@Param("schuetze") Benutzer schuetze);

    /**
     * Findet alle unsignierten Einträge mit eager loading.
     *
     * @return Liste der Einträge
     */
    @Query("SELECT DISTINCT e FROM SchiessnachweisEintrag e " +
           "LEFT JOIN FETCH e.schuetze " +
           "LEFT JOIN FETCH e.disziplin " +
           "LEFT JOIN FETCH e.schiesstand " +
           "WHERE e.istSigniert = false")
    List<SchiessnachweisEintrag> findUnsignierteEintraegeWithDetails();


    /**
     * Findet alle Einträge eines Schießstands für einen bestimmten Schützen.
     *
     * @param schiesstand Der Schießstand
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge
     */
    @Query("SELECT e FROM SchiessnachweisEintrag e WHERE e.schiesstand = :schiesstand " +
           "AND e.schuetze = :schuetze AND e.datum BETWEEN :von AND :bis ORDER BY e.datum DESC")
    List<SchiessnachweisEintrag> findBySchiesstandUndSchuetzeImZeitraum(
            @Param("schiesstand") Schiesstand schiesstand,
            @Param("schuetze") Benutzer schuetze,
            @Param("von") LocalDate von,
            @Param("bis") LocalDate bis);

    /**
     * Zählt Einträge eines Schützen mit bestimmtem Status.
     *
     * @param schuetze Der Schütze
     * @param status Der Status
     * @return Anzahl der Einträge
     */
    long countBySchuetzeAndStatus(Benutzer schuetze, EintragStatus status);
}
