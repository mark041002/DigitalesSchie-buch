package de.suchalla.schiessbuch.service;
import de.suchalla.schiessbuch.mapper.DigitalesZertifikatMapper;
import de.suchalla.schiessbuch.model.dto.DigitalesZertifikatDTO;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service für die öffentliche Verifizierung von Zertifikaten.
 * Ermöglicht es Personen und Behörden, die Echtheit von Zertifikaten zu überprüfen.
 *
 * @author Markus Suchalla
 * @version 1.0.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZertifikatVerifizierungsService {

    private final DigitalesZertifikatRepository zertifikatRepository;
    private final DigitalesZertifikatMapper zertifikatMapper;

    /**
     * Verifiziert ein Zertifikat anhand seiner Seriennummer.
     * Gibt alle relevanten Informationen als DTO zurück.
     *
     * @param seriennummer Die zu verifizierende Seriennummer
     * @return Das Zertifikat als DTO oder null, wenn nicht gefunden
     */
    @Transactional(readOnly = true)
    public DigitalesZertifikatDTO verifiziere(String seriennummer) {
        if (seriennummer == null || seriennummer.trim().isEmpty()) {
            log.warn("Verifizierung mit leerer Seriennummer aufgerufen");
            return null;
        }

        log.info("Verifiziere Zertifikat mit Seriennummer: {}", seriennummer);

        // Zertifikat mit allen Details laden (EAGER loading von Benutzer und Verein)
        return zertifikatRepository.findBySeriennummerWithDetails(seriennummer)
                .map(zertifikatMapper::toDTO)
                .orElse(null);
    }

    /**
     * Interne Methode für Services, die das vollständige Entity benötigen.
     *
     * @param seriennummer Die Seriennummer
     * @return Das vollständige Entity
     */
    @Transactional(readOnly = true)
    public DigitalesZertifikat verifizierInternal(String seriennummer) {
        if (seriennummer == null || seriennummer.trim().isEmpty()) {
            return null;
        }
        return zertifikatRepository.findBySeriennummerWithDetails(seriennummer)
                .orElse(null);
    }

}
