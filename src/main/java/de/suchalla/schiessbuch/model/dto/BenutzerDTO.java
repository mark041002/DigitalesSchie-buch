package de.suchalla.schiessbuch.model.dto;

import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO für Benutzer-Daten ohne sensible Informationen.
 *
 *   Das passwort-Feld der Entity wird aus Sicherheitsgründen
 *   NICHT in dieses DTO gemappt, um zu verhindern, dass Passwort-Hashes versehentlich
 *   an Clients gesendet werden.</li>
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenutzerDTO {

    private Long id;
    private String vorname;
    private String nachname;
    private String email;
    private BenutzerRolle rolle;
    private boolean emailVerifiziert;
    private boolean emailNotificationsEnabled;
    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;

    /**
     * Hilfsmethode für vollständigen Namen.
     * @return Vorname + Nachname
     */
    public String getVollstaendigerName() {
        return vorname + " " + nachname;
    }
}