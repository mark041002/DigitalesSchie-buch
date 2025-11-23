package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import lombok.*;

/**
 * Vereinfachtes DTO für Benutzer-Listen mit reduzierten Feldern für bessere Performance
 * in Grid-Ansichten und Auswahlfeldern.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenutzerListDTO {

    private Long id;
    private String vorname;
    private String nachname;
    private String email;
    private BenutzerRolle rolle;

    /**
     * Hilfsmethode für vollständigen Namen.
     * @return Vorname + Nachname
     */
    public String getVollstaendigerName() {
        return vorname + " " + nachname;
    }
}
