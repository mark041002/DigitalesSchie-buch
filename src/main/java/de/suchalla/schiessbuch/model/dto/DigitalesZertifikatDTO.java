package de.suchalla.schiessbuch.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO für Digitale Zertifikate ohne den privaten Schlüssel.
 *
 *   Das privateKeyPEM-Feld der Entity wird aus
 *   Sicherheitsgründen NICHT in dieses DTO gemappt. Private Schlüssel sollten niemals
 *   an Clients geschickt werden.
 *   Verifizierung: Enthält denormalisierte Felder für öffentliche Verifizierung
 *       ohne Zugriff auf sensible Schlüsseldaten zu benötigen.</li>
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalesZertifikatDTO {

    private Long id;
    private String zertifikatsTyp;
    private String seriennummer;
    private String subjectDN;
    private String issuerDN;
    private String zertifikatPEM;
    private LocalDateTime gueltigSeit;
    private LocalDateTime gueltigBis;
    private Boolean widerrufen;
    private LocalDateTime widerrufenAm;
    private String widerrufsGrund;

    // Beziehungen als IDs (nicht als verschachtelte Objekte)
    private Long benutzerId;
    private Long vereinId;
    private Long schiesstandId;
    private Long parentZertifikatId;

    // Denormalisierte Felder für Anzeige (für öffentliche Verifizierung)
    private String benutzerVollstaendigerName;
    private String benutzerEmail;
    private String vereinName;
    private String vereinAdresse;
    private String schiesstandName;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;

    /**
     * Prüft, ob das Zertifikat aktuell gültig ist.
     * @return true wenn gültig
     */
    public boolean istGueltig() {
        return !widerrufen &&
                (gueltigSeit.isBefore(LocalDateTime.now())) &&
                (gueltigBis == null || gueltigBis.isAfter(LocalDateTime.now()));
    }
}
