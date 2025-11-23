package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO für Schießstände mit denormalisierten Vereins- und Aufseherfeldern.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchiesstandDTO {

    private Long id;
    private String name;
    private SchiesstandTyp typ;
    private String adresse;
    private String beschreibung;

    // Flache Felder für Beziehungen
    private Long vereinId;
    private String vereinName;

    private Long aufseherId;
    private String aufseherVorname;
    private String aufseherNachname;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;

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
