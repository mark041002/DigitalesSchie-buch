package de.suchalla.schiessbuch.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO für Disziplinen mit denormalisiertem Verbandsfeld.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisziplinDTO {

    private Long id;
    private String kennziffer;
    private String programm;
    private String waffeKlasse;

    // Flache Felder für Verband-Beziehung
    private Long verbandId;
    private String verbandName;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;
}
