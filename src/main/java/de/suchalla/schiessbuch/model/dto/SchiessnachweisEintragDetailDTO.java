package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.EintragStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO für Schießnachweis-Eintrag Details mit allen Feldern inklusive
 * Bemerkungen, Ablehnungsgründen und Signatur-Informationen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchiessnachweisEintragDetailDTO {

    private Long id;
    private LocalDate datum;
    private Integer anzahlSchuesse;
    private String ergebnis;
    private String kaliber;
    private String waffenart;
    private String bemerkung;
    private EintragStatus status;
    private Boolean istSigniert;
    private LocalDateTime signiertAm;
    private String digitaleSignatur;
    private String ablehnungsgrund;

    // Flache Felder für Beziehungen
    private Long schuetzeId;
    private String schuetzeVorname;
    private String schuetzeNachname;
    private String schuetzeEmail;

    private Long disziplinId;
    private String disziplinName;
    private String disziplinBeschreibung;
    private String disziplinProgramm;
    private String disziplinWaffeKlasse;

    private Long schiesstandId;
    private String schiesstandName;
    private String schiesstandTyp;

    private Long vereinId;
    private String vereinName;
    private String vereinsNummer;

    private Long aufseherId;
    private String aufseherVorname;
    private String aufseherNachname;
    private String aufseherEmail;

    private Long zertifikatId;
    private String zertifikatSeriennummer;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;

    /**
     * Hilfsmethode für vollständigen Schützen-Namen.
     */
    public String getSchuetzeVollstaendigerName() {
        return schuetzeVorname + " " + schuetzeNachname;
    }

    /**
     * Hilfsmethode für vollständigen Aufseher-Namen.
     */
    public String getAufseherVollstaendigerName() {
        if (aufseherVorname == null || aufseherNachname == null) {
            return "-";
        }
        return aufseherVorname + " " + aufseherNachname;
    }
}
