package de.suchalla.schiessbuch.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für Vereine mit denormalisierten Verbandsfeldern.
 * Die ManyToMany-Beziehung zu Verbänden wird als Liste von IDs dargestellt.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VereinDTO {

    private Long id;
    private String name;
    private String vereinsNummer;
    private String adresse;
    private String beschreibung;

    private List<Long> verbandIds;
    private List<String> verbandNamen;
    private int mitgliederAnzahl;
    
    private String vereinschefName;
    private Long vereinschefId;

    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;
}
