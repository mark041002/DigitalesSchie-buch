package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für Benutzer-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Optional mit Benutzer
     */
    Optional<Benutzer> findByEmail(String email);

    /**
     * Prüft, ob ein Benutzer mit gegebener E-Mail existiert.
     *
     * @param email E-Mail-Adresse
     * @return true wenn vorhanden
     */
    boolean existsByEmail(String email);

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse mit eager loading der Vereinsmitgliedschaften.
     *
     * @param email Die E-Mail-Adresse
     * @return Optional mit Benutzer
     */
    @Query("SELECT b FROM Benutzer b LEFT JOIN FETCH b.vereinsmitgliedschaften WHERE b.email = :email")
    Optional<Benutzer> findByEmailWithMitgliedschaften(@Param("email") String email);

}
