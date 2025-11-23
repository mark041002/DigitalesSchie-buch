package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für {@link DigitalesZertifikat} Entitäten.
 *
 * Bietet Such- und Prüfmethoden für Zertifikate inkl. Varianten mit EAGER-Laden
 * der zugehörigen Entitäten (Benutzer, Verein, Schießstand, übergeordnetes Zertifikat).
 *
 * @author Markus Suchalla
 * @version 1.0.2
 */
@Repository
public interface DigitalesZertifikatRepository extends JpaRepository<DigitalesZertifikat, Long> {

    /**
     * Findet ein Zertifikat anhand seines Typs.
     * Mögliche Typen: ROOT, VEREIN, AUFSEHER, SCHIESSTANDAUFSEHER u. a.
     *
     * @param zertifikatsTyp Der Zertifikatstyp
     * @return Optional mit gefundenem Zertifikat
     */
    Optional<DigitalesZertifikat> findByZertifikatsTyp(String zertifikatsTyp);

    /**
     * Findet ein Zertifikat anhand der Seriennummer und lädt alle wichtigen
     * Referenzen EAGER (Benutzer, Verein, Schießstand, Parent-Zertifikat).
     * Wird u. a. für die öffentliche Verifizierung verwendet.
     *
     * @param seriennummer Die Seriennummer
     * @return Optional mit Zertifikat inkl. Details
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.seriennummer = :seriennummer")
    Optional<DigitalesZertifikat> findBySeriennummerWithDetails(String seriennummer);

    /**
     * Findet ein Zertifikat für einen Benutzer (typischerweise Aufseher-Zertifikat).
     *
     * @param benutzer Der Benutzer
     * @return Optional mit Zertifikat
     */
    Optional<DigitalesZertifikat> findByBenutzer(Benutzer benutzer);

    /**
     * Findet ein Vereinszertifikat für den übergebenen Verein und Typ.
     *
     * @param verein          Der Verein
     * @param zertifikatsTyp  Der Zertifikatstyp (z. B. "VEREIN")
     * @return Optional mit Zertifikat
     */
    Optional<DigitalesZertifikat> findByVereinAndZertifikatsTyp(Verein verein, String zertifikatsTyp);

    /**
     * Findet alle Zertifikate eines Vereins.
     *
     * @param verein Der Verein
     * @return Liste der Zertifikate des Vereins
     */
    List<DigitalesZertifikat> findByVerein(Verein verein);

    /**
     * Findet alle Zertifikate eines Schießstands.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der Zertifikate des Schießstands
     */
    List<DigitalesZertifikat> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Findet alle Zertifikate mit EAGER-Laden von Benutzer (inkl. Mitgliedschaften),
     * Verein, Schießstand und Parent-Zertifikat.
     *
     * @return Liste aller Zertifikate inkl. Details
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer b LEFT JOIN FETCH b.vereinsmitgliedschaften LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat")
    List<DigitalesZertifikat> findAllWithDetailsAndMitgliedschaften();

    /**
     * Prüft, ob für den Benutzer bereits ein Zertifikat existiert.
     *
     * @param benutzer Der Benutzer
     * @return true, wenn bereits ein Zertifikat existiert
     */
    boolean existsByBenutzer(Benutzer benutzer);

    /**
     * Prüft, ob für den Verein ein Zertifikat eines bestimmten Typs existiert.
     *
     * @param verein         Der Verein
     * @param zertifikatsTyp Der Zertifikatstyp (z. B. "VEREIN")
     * @return true, wenn ein entsprechendes Zertifikat existiert
     */
    boolean existsByVereinAndZertifikatsTyp(Verein verein, String zertifikatsTyp);

    /**
     * Findet das Zertifikat für einen Benutzer an einem bestimmten Schießstand
     * (Schießstandaufseher-Zertifikat).
     *
     * @param benutzer    Der Benutzer
     * @param schiesstand Der Schießstand
     * @return Optional mit Zertifikat
     */
    Optional<DigitalesZertifikat> findByBenutzerAndSchiesstand(Benutzer benutzer, Schiesstand schiesstand);

    /**
     * Prüft, ob ein Benutzer bereits ein Zertifikat für einen Schießstand besitzt.
     *
     * @param benutzer    Der Benutzer
     * @param schiesstand Der Schießstand
     * @return true, wenn bereits ein Zertifikat existiert
     */
    boolean existsByBenutzerAndSchiesstand(Benutzer benutzer, Schiesstand schiesstand);

    /**
     * Löscht alle Zertifikate eines Benutzers anhand der Benutzer-ID.
     * Hilfreich beim Entfernen eines Benutzers, ohne die Entity zu laden.
     */
    void deleteAllByBenutzerId(Long benutzerId);

    /**
     * Findet alle gültigen (nicht widerrufenen) Zertifikate mit EAGER-Laden von
     * Benutzer (inkl. Mitgliedschaften), Verein, Schießstand und Parent-Zertifikat.
     *
     * @return Liste gültiger Zertifikate inkl. Details
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer b LEFT JOIN FETCH b.vereinsmitgliedschaften LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.widerrufen = false OR z.widerrufen IS NULL")
    List<DigitalesZertifikat> findAllGueltigeWithDetailsAndMitgliedschaften();

    /**
     * Findet alle widerrufenen Zertifikate mit EAGER-Laden von
     * Benutzer (inkl. Mitgliedschaften), Verein, Schießstand und Parent-Zertifikat.
     *
     * @return Liste widerrufener Zertifikate inkl. Details
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer b LEFT JOIN FETCH b.vereinsmitgliedschaften LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.widerrufen = true")
    List<DigitalesZertifikat> findAllWiderrufeneWithDetailsAndMitgliedschaften();
}
