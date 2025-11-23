package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO für Vereinsmitgliedschaften mit flacher Struktur.
 * Vermeidet N+1 Queries durch Denormalisierung der Benutzer- und Vereinsfelder.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VereinsmigliedschaftDTO {

    private Long id;
    private MitgliedschaftsStatus status;
    private LocalDate beitrittDatum;
    private LocalDate austrittDatum;
    private Boolean istAufseher;
    private Boolean istVereinschef;
    private Boolean aktiv;
    private String ablehnungsgrund;

    // Flache Benutzer-Felder
    private Long benutzerId;
    private String benutzerVorname;
    private String benutzerNachname;
    private String benutzerEmail;

    // Flache Vereins-Felder
    private Long vereinId;
    private String vereinName;
    private String vereinsNummer;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;

    /**
     * Hilfsmethode für vollständigen Benutzer-Namen.
     */
    public String getBenutzerVollstaendigerName() {
        return benutzerVorname + " " + benutzerNachname;
    }
}
