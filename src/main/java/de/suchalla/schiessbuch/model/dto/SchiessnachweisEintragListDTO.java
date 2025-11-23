package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.EintragStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO für Schießnachweis-Einträge in Listen/Tabellen mit flacher Struktur.
 * Vermeidet LazyInitializationException und N+1 Queries durch Denormalisierung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchiessnachweisEintragListDTO {

    private Long id;
    private LocalDate datum;
    private Integer anzahlSchuesse;
    private String ergebnis;
    private String kaliber;
    private String waffenart;
    private EintragStatus status;
    private LocalDateTime signiertAm;

    // Flache Felder statt verschachtelte Entities
    private Long schuetzeId;
    private String schuetzeVorname;
    private String schuetzeNachname;

    private Long disziplinId;
    private String disziplinName;
    private String disziplinProgramm;
    private String disziplinWaffeKlasse;

    private Long schiesstandId;
    private String schiesstandName;

    private Long vereinId;  // über Schiesstand -> Verein
    private String vereinName;

    private Long aufseherId;
    private String aufseherVorname;
    private String aufseherNachname;

    private Long zertifikatId;
    private String zertifikatSeriennummer;

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
