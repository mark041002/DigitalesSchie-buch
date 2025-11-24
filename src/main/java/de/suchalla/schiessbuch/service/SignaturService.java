package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import de.suchalla.schiessbuch.service.email.EmailService;
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
    private final SchiessnachweisService schiessnachweisService;
    private final EmailService notificationService;

    /**
     * Signiert einen Schießnachweis-Eintrag anhand der ID.
     */
    @Transactional
    public void signEintragMitId(Long eintragId, Benutzer aufseher, Verein verein) {
        SchiessnachweisEintrag eintrag = eintragRepository.findById(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));
        signEintrag(eintrag, aufseher, verein);
    }

    /**
     * Signiert einen Schießnachweis-Eintrag mit dem Zertifikat des Aufsehers.
     *
     * @param eintrag Der zu signierende Eintrag
     * @param aufseher Der Aufseher, der signiert
     * @param verein Der Verein, in dem signiert wird
     * @throws RuntimeException wenn die Signierung fehlschlägt
     */
    @Transactional
    public void signEintrag(SchiessnachweisEintrag eintrag, Benutzer aufseher, Verein verein) {
        try {
            log.info("Starte Signierung für Eintrag-ID: {} durch Aufseher: {}", eintrag.getId(), aufseher.getId());

            DigitalesZertifikat aufseherZertifikat = zertifikatRepository.findByBenutzer(aufseher)
                    .orElseGet(() -> {
                        log.info("Erstelle neues Zertifikat für Aufseher: {}", aufseher.getId());
                        return pkiService.createAufseherCertificate(aufseher, verein);
                    });

            if (!aufseherZertifikat.istGueltig()) {
                throw new RuntimeException("Zertifikat des Aufsehers ist nicht gültig oder wurde widerrufen");
            }

            String dataToSign = buildSignatureData(eintrag);
            String signature = pkiService.signData(dataToSign, aufseherZertifikat);

            eintrag.setAufseher(aufseher);
            eintrag.setDigitaleSignatur(signature);
            eintrag.setZertifikat(aufseherZertifikat);

            schiessnachweisService.signiereEintrag(eintrag.getId(), aufseher, signature);
            log.info("Eintrag {} erfolgreich signiert", eintrag.getId());

            // Benachrichtige den Schützen asynchron über die erfolgreiche Signierung
            try {
                notificationService.notifyEntrySigned(eintrag);
            } catch (Exception nEx) {
                log.warn("Fehler beim Senden der Signierungs-Benachrichtigung: {}", nEx.getMessage());
            }

        } catch (Exception e) {
            log.error("Fehler beim Signieren des Eintrags {}: {}", eintrag.getId(), e.getMessage());
            throw new RuntimeException("Eintrag konnte nicht signiert werden: " + e.getMessage(), e);
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
                dateFormatter.format(eintrag.getDatum()),
                eintrag.getDisziplin().getKennziffer(),
                eintrag.getSchiesstand().getName(),
                eintrag.getAnzahlSchuesse() != null ? eintrag.getAnzahlSchuesse() : 0,
                eintrag.getErgebnis() != null ? eintrag.getErgebnis() : "",
                eintrag.getKaliber() != null ? eintrag.getKaliber() : "",
                eintrag.getWaffenart() != null ? eintrag.getWaffenart() : ""
        );
    }
}
