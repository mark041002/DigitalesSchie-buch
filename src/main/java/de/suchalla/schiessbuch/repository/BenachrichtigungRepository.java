package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benachrichtigung;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.enums.BenachrichtigungsTyp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository für Benachrichtigung-Entitäten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface BenachrichtigungRepository extends JpaRepository<Benachrichtigung, Long> {

    /**
     * Findet alle Benachrichtigungen eines Empfängers.
     *
     * @param empfaenger Der Empfänger
     * @return Liste der Benachrichtigungen
     */
    @EntityGraph(attributePaths = {"empfaenger", "eintrag"})
    List<Benachrichtigung> findByEmpfaengerOrderByErstelltAmDesc(Benutzer empfaenger);

    /**
     * Findet alle ungelesenen Benachrichtigungen eines Empfängers.
     *
     * @param empfaenger Der Empfänger
     * @param gelesen Gelesen-Status
     * @return Liste der Benachrichtigungen
     */
    @EntityGraph(attributePaths = {"empfaenger", "eintrag"})
    List<Benachrichtigung> findByEmpfaengerAndGelesenOrderByErstelltAmDesc(Benutzer empfaenger, Boolean gelesen);

    /**
     * Findet Benachrichtigungen nach Typ.
     *
     * @param empfaenger Der Empfänger
     * @param typ Der Benachrichtigungstyp
     * @return Liste der Benachrichtigungen
     */
    @EntityGraph(attributePaths = {"empfaenger", "eintrag"})
    List<Benachrichtigung> findByEmpfaengerAndTypOrderByErstelltAmDesc(Benutzer empfaenger, BenachrichtigungsTyp typ);

    /**
     * Zählt ungelesene Benachrichtigungen.
     *
     * @param empfaenger Der Empfänger
     * @param gelesen Gelesen-Status
     * @return Anzahl ungelesener Benachrichtigungen
     */
    long countByEmpfaengerAndGelesen(Benutzer empfaenger, Boolean gelesen);
}
