package de.suchalla.schiessbuch.model.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO für Verbände mit einfacher Struktur ohne Beziehungen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerbandDTO {

    private Long id;
    private String name;
    private String beschreibung;
    private LocalDateTime erstelltAm;
    private LocalDateTime aktualisiertAm;
}
