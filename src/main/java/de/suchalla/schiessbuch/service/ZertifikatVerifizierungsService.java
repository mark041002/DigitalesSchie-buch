package de.suchalla.schiessbuch.service;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service für die öffentliche Verifizierung von Zertifikaten.
 * Ermöglicht es Personen und Behörden, die Echtheit von Zertifikaten zu überprüfen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZertifikatVerifizierungsService {

    private final DigitalesZertifikatRepository zertifikatRepository;

    /**
     * Verifiziert ein Zertifikat anhand seiner Seriennummer.
     * Gibt alle relevanten Informationen zurück, die für die Verifizierung
     * durch Behörden wichtig sind.
     *
     * @param seriennummer Die zu verifizierende Seriennummer
     * @return Das Zertifikat mit allen Details oder null, wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public DigitalesZertifikat verifiziere(String seriennummer) {
        if (seriennummer == null || seriennummer.trim().isEmpty()) {
            log.warn("Verifizierung mit leerer Seriennummer aufgerufen");
            return null;
        }

        log.info("Verifiziere Zertifikat mit Seriennummer: {}", seriennummer);

        // Zertifikat mit allen Details laden (EAGER loading von Benutzer und Verein)
        return zertifikatRepository.findBySeriennummerWithDetails(seriennummer)
                .orElse(null);
    }

    /**
     * Prüft, ob ein Zertifikat existiert und gültig ist.
     *
     * @param seriennummer Die zu prüfende Seriennummer
     * @return true wenn das Zertifikat existiert und gültig ist
     */
    @Transactional(readOnly = true)
    public boolean istZertifikatGueltig(String seriennummer) {
        return zertifikatRepository.findBySeriennummer(seriennummer)
                .map(DigitalesZertifikat::istGueltig)
                .orElse(false);
    }

    /**
     * Gibt die Anzahl der Verifizierungen eines bestimmten Zertifikats zurück.
     * Dies könnte für Audit-Zwecke erweitert werden.
     *
     * @param seriennummer Die Seriennummer
     * @return true wenn das Zertifikat existiert
     */
    @Transactional(readOnly = true)
    public boolean existiert(String seriennummer) {
        return zertifikatRepository.findBySeriennummer(seriennummer).isPresent();
    }

    /**
     * Prüft, ob ein Zertifikat zu einem bestimmten Zeitpunkt gültig war.
     *
     * @param zertifikat Das Zertifikat
     * @param zeitpunkt Der zu prüfende Zeitpunkt
     * @return true, wenn das Zertifikat zu diesem Zeitpunkt gültig war
     */
    @Transactional(readOnly = true)
    public boolean warZertifikatGueltigAm(DigitalesZertifikat zertifikat, LocalDateTime zeitpunkt) {
        if (zertifikat == null || zeitpunkt == null) {
            return false;
        }
        return zertifikat.getGueltigSeit() != null && zertifikat.getGueltigBis() != null
                && !zeitpunkt.isBefore(zertifikat.getGueltigSeit())
                && !zeitpunkt.isAfter(zertifikat.getGueltigBis())
                && zertifikat.istGueltig();
    }
}
