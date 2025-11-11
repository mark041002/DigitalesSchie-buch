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
 * Repository für digitale Zertifikate.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Repository
public interface DigitalesZertifikatRepository extends JpaRepository<DigitalesZertifikat, Long> {

    /**
     * Findet Zertifikat nach Typ (ROOT, VEREIN, AUFSEHER)
     */
    Optional<DigitalesZertifikat> findByZertifikatsTyp(String zertifikatsTyp);

    /**
     * Findet Zertifikat nach Seriennummer
     */
    Optional<DigitalesZertifikat> findBySeriennummer(String seriennummer);

    /**
     * Findet Zertifikat nach Seriennummer mit EAGER loading aller Details
     * (für öffentliche Verifizierung)
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.seriennummer = :seriennummer")
    Optional<DigitalesZertifikat> findBySeriennummerWithDetails(String seriennummer);

    /**
     * Findet Zertifikat für einen Benutzer (Aufseher)
     */
    Optional<DigitalesZertifikat> findByBenutzer(Benutzer benutzer);

    /**
     * Findet Vereinszertifikat
     */
    Optional<DigitalesZertifikat> findByVereinAndZertifikatsTyp(Verein verein, String zertifikatsTyp);

    /**
     * Findet alle Zertifikate eines Vereins
     */
    List<DigitalesZertifikat> findByVerein(Verein verein);

    /**
     * Findet alle gültigen Zertifikate eines Vereins
     */
    List<DigitalesZertifikat> findByVereinAndWiderrufenFalse(Verein verein);

    /**
     * Findet alle Zertifikate mit EAGER loading von Benutzer, Verein und Schießstand
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat")
    List<DigitalesZertifikat> findAllWithDetails();

    /**
     * Findet Zertifikat eines Benutzers mit EAGER loading
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.benutzer = :benutzer")
    Optional<DigitalesZertifikat> findByBenutzerWithDetails(Benutzer benutzer);

    /**
     * Findet alle Zertifikate eines Vereins mit EAGER loading
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.verein LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.verein = :verein")
    List<DigitalesZertifikat> findByVereinWithDetails(Verein verein);

    /**
     * Prüft, ob ein Benutzer bereits ein Zertifikat hat
     */
    boolean existsByBenutzer(Benutzer benutzer);

    /**
     * Prüft, ob ein Verein bereits ein Zertifikat hat
     */
    boolean existsByVereinAndZertifikatsTyp(Verein verein, String zertifikatsTyp);

    /**
     * Findet Zertifikat für einen Benutzer an einem Schießstand (Schießstandaufseher)
     */
    Optional<DigitalesZertifikat> findByBenutzerAndSchiesstand(Benutzer benutzer, Schiesstand schiesstand);

    /**
     * Findet alle Zertifikate eines Schießstands
     */
    List<DigitalesZertifikat> findBySchiesstand(Schiesstand schiesstand);

    /**
     * Findet alle Zertifikate eines Schießstands mit EAGER loading
     */
    @Query("SELECT z FROM DigitalesZertifikat z LEFT JOIN FETCH z.benutzer LEFT JOIN FETCH z.schiesstand LEFT JOIN FETCH z.parentZertifikat WHERE z.schiesstand = :schiesstand")
    List<DigitalesZertifikat> findBySchiesstandWithDetails(Schiesstand schiesstand);

    /**
     * Prüft, ob ein Benutzer bereits ein Zertifikat für einen Schießstand hat
     */
    boolean existsByBenutzerAndSchiesstand(Benutzer benutzer, Schiesstand schiesstand);
}
