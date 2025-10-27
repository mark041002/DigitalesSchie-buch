package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Service für digitale Signaturen mit PKI-Zertifikaten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignaturService {

    private final PkiService pkiService;
    private final DigitalesZertifikatRepository zertifikatRepository;
    private final SchiessnachweisEintragRepository eintragRepository;

    /**
     * Signiert einen Schießnachweis-Eintrag mit dem Zertifikat des Aufsehers.
     */
    @Transactional
    public void signEintrag(SchiessnachweisEintrag eintrag, Benutzer aufseher, Verein verein) {
        try {
            log.info("=== SIGNIERUNG GESTARTET ===");
            log.info("Eintrag-ID: {}", eintrag.getId());
            log.info("Aufseher: {} (ID: {})", aufseher.getVollstaendigerName(), aufseher.getId());
            log.info("Verein: {} (ID: {})", verein.getName(), verein.getId());
            log.info("Eintrag-Status vorher: {}", eintrag.getStatus());
            log.info("Eintrag hat bereits Zertifikat: {}", eintrag.getZertifikat() != null);

            // Zertifikat des Aufsehers laden oder erstellen
            log.info("Lade oder erstelle Zertifikat für Aufseher...");
            DigitalesZertifikat aufseherZertifikat = zertifikatRepository.findByBenutzer(aufseher)
                    .orElseGet(() -> {
                        log.info("Kein Zertifikat gefunden, erstelle neues Zertifikat...");
                        DigitalesZertifikat neuesZert = pkiService.createAufseherCertificate(aufseher, verein);
                        log.info("Neues Zertifikat erstellt mit ID: {}", neuesZert.getId());
                        return neuesZert;
                    });

            log.info("Zertifikat geladen/erstellt - ID: {}", aufseherZertifikat.getId());

            // Prüfen ob Zertifikat gültig ist
            if (!aufseherZertifikat.istGueltig()) {
                log.error("Zertifikat ist NICHT gültig!");
                throw new RuntimeException("Zertifikat des Aufsehers ist nicht gültig oder wurde widerrufen");
            }
            log.info("Zertifikat ist GÜLTIG");

            log.info("=== PKI-ZERTIFIKAT DETAILS ===");
            log.info("Zertifikat-ID: {}", aufseherZertifikat.getId());
            log.info("Zertifikat-Typ: {}", aufseherZertifikat.getZertifikatsTyp());
            log.info("Zertifikat-Seriennummer: {}", aufseherZertifikat.getSeriennummer());
            log.info("Zertifikat-Inhaber: {}", aufseher.getVollstaendigerName());
            log.info("Zertifikat gültig von: {} bis: {}", aufseherZertifikat.getGueltigAb(), aufseherZertifikat.getGueltigBis());
            log.info("==============================");

            // Daten für Signatur zusammenstellen
            String dataToSign = buildSignatureData(eintrag);
            log.info("Daten für Signatur erstellt: {}", dataToSign);

            // Signatur erstellen
            log.info("Erstelle digitale Signatur...");
            String signature = pkiService.signData(dataToSign, aufseherZertifikat);
            log.info("Signatur erstellt (Länge: {})", signature.length());

            // Eintrag aktualisieren
            log.info("Aktualisiere Eintrag...");
            log.info("Setze Aufseher: {}", aufseher.getId());
            eintrag.setAufseher(aufseher);

            log.info("Setze digitale Signatur: {}", signature.substring(0, Math.min(50, signature.length())) + "...");
            eintrag.setDigitaleSignatur(signature);

            log.info("Setze Zertifikat: ID={}, SN={}", aufseherZertifikat.getId(), aufseherZertifikat.getSeriennummer());
            eintrag.setZertifikat(aufseherZertifikat);

            log.info("Rufe signieren() Methode auf...");
            eintrag.signieren(aufseher);

            log.info("Eintrag-Status nach signieren(): {}", eintrag.getStatus());
            log.info("Eintrag.istSigniert: {}", eintrag.getIstSigniert());
            log.info("Eintrag.zertifikat != null: {}", eintrag.getZertifikat() != null);

            log.info("Speichere Eintrag in Datenbank...");
            SchiessnachweisEintrag gespeicherterEintrag = eintragRepository.save(eintrag);

            log.info("Eintrag gespeichert - ID: {}", gespeicherterEintrag.getId());
            log.info("Gespeicherter Eintrag - Status: {}", gespeicherterEintrag.getStatus());
            log.info("Gespeicherter Eintrag - Zertifikat ID: {}",
                    gespeicherterEintrag.getZertifikat() != null ? gespeicherterEintrag.getZertifikat().getId() : "NULL");

            log.info("=== SIGNIERUNG ERFOLGREICH ===");
            log.info("Eintrag {} erfolgreich signiert mit Zertifikat-SN: {}", eintrag.getId(), aufseherZertifikat.getSeriennummer());

        } catch (Exception e) {
            log.error("=== SIGNIERUNG FEHLGESCHLAGEN ===");
            log.error("Fehler beim Signieren des Eintrags", e);
            throw new RuntimeException("Eintrag konnte nicht signiert werden: " + e.getMessage(), e);
        }
    }

    /**
     * Verifiziert die Signatur eines Eintrags.
     */
    public boolean verifyEintrag(SchiessnachweisEintrag eintrag) {
        try {
            if (!eintrag.getIstSigniert() || eintrag.getDigitaleSignatur() == null || eintrag.getZertifikat() == null) {
                log.warn("Eintrag {} ist nicht signiert", eintrag.getId());
                return false;
            }

            // Daten rekonstruieren
            String dataToVerify = buildSignatureData(eintrag);

            // Signatur verifizieren
            boolean valid = pkiService.verifySignature(
                    dataToVerify,
                    eintrag.getDigitaleSignatur(),
                    eintrag.getZertifikat()
            );

            log.info("Signaturverifizierung für Eintrag {}: {}", eintrag.getId(), valid ? "GÜLTIG" : "UNGÜLTIG");
            return valid;

        } catch (Exception e) {
            log.error("Fehler bei der Signaturverifizierung", e);
            return false;
        }
    }

    /**
     * Baut die zu signierenden Daten aus dem Eintrag zusammen.
     */
    private String buildSignatureData(SchiessnachweisEintrag eintrag) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return String.format(
                "ID:%d|Schuetze:%s|Datum:%s|Disziplin:%s|Schiesstand:%s|AnzahlSchuesse:%d|Ergebnis:%s|Kaliber:%s|Waffenart:%s",
                eintrag.getId(),
                eintrag.getSchuetze().getEmail(),
                eintrag.getDatum().format(dateFormatter),
                eintrag.getDisziplin().getName(),
                eintrag.getSchiesstand().getName(),
                eintrag.getAnzahlSchuesse() != null ? eintrag.getAnzahlSchuesse() : 0,
                eintrag.getErgebnis() != null ? eintrag.getErgebnis() : "",
                eintrag.getKaliber() != null ? eintrag.getKaliber() : "",
                eintrag.getWaffenart() != null ? eintrag.getWaffenart() : ""
        );
    }

    /**
     * Erstellt automatisch Zertifikate für einen neuen Aufseher.
     */
    @Transactional
    public DigitalesZertifikat ensureAufseherCertificate(Benutzer aufseher, Verein verein) {
        return zertifikatRepository.findByBenutzer(aufseher)
                .orElseGet(() -> pkiService.createAufseherCertificate(aufseher, verein));
    }

    /**
     * Erstellt automatisch ein Vereinszertifikat.
     */
    @Transactional
    public DigitalesZertifikat ensureVereinCertificate(Verein verein) {
        return zertifikatRepository.findByVereinAndZertifikatsTyp(verein, "VEREIN")
                .orElseGet(() -> pkiService.createVereinCertificate(verein));
    }
}
