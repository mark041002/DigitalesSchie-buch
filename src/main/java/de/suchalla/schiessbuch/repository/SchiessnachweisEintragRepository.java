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
     * Findet alle Einträge an einem SchieÃŸstand.
     *
     * @param schiesstand Der SchieÃŸstand
     * @return Liste der EintrÃ¤ge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchiesstand(Schiesstand schiesstand);

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
     * ZÃ¤hlt EintrÃ¤ge eines SchÃ¼tzen mit bestimmtem Status.
     *
     * @param schuetze Der SchÃ¼tze
     * @param status Der Status
     * @return Anzahl der EintrÃ¤ge
     */
    long countBySchuetzeAndStatus(Benutzer schuetze, EintragStatus status);

    // Note: findByIdWithDetails wurde entfernt - findById mit @EntityGraph ersetzt alle WITH...() Methoden

    /**
     * Findet einen Eintrag mit allen Beziehungen inkl. Verein über Schiesstand.
     * Verhindert LazyInitializationException beim Zugriff auf schiesstand.verein.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    Optional<SchiessnachweisEintrag> findById(Long id);

    /**
     * Findet alle Einträge eines Schützen ohne Datumsfilter.
     *
     * @param schuetze Der Schütze
     * @return Liste der Einträge
     */
    @EntityGraph(attributePaths = {"schuetze", "disziplin", "schiesstand", "schiesstand.verein", "aufseher", "zertifikat"})
    List<SchiessnachweisEintrag> findBySchuetze(Benutzer schuetze);

}