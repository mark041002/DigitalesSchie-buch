package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.dto.BenutzerListDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper für Benutzer-Entity <-> Benutzer-DTOs.
 * Das password-Feld wird aus Sicherheitsgründen nicht in DTOs gemappt.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class BenutzerMapper implements BaseMapper<Benutzer, BenutzerDTO> {

    /**
     * Konvertiert Benutzer-Entity zu vollständigem DTO (ohne Passwort).
     */
    public BenutzerDTO toDTO(Benutzer entity) {
        if (entity == null) {
            return null;
        }

        return BenutzerDTO.builder()
                .id(entity.getId())
                .vorname(entity.getVorname())
                .nachname(entity.getNachname())
                .email(entity.getEmail())
                .rolle(entity.getRolle())
                .emailVerifiziert(entity.isEmailVerifiziert())
                .emailNotificationsEnabled(entity.isEmailNotificationsEnabled())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm())
                .build();
    }

    /**
     * Konvertiert Benutzer-Entity zu Listen-DTO (minimal).
     */
    public BenutzerListDTO toListDTO(Benutzer entity) {
        if (entity == null) {
            return null;
        }

        return BenutzerListDTO.builder()
                .id(entity.getId())
                .vorname(entity.getVorname())
                .nachname(entity.getNachname())
                .email(entity.getEmail())
                .rolle(entity.getRolle())
                .build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt

    /**
     * Konvertiert Liste von Entities zu Liste von Listen-DTOs.
     */
    public List<BenutzerListDTO> toListDTOList(List<Benutzer> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toListDTO)
                .collect(Collectors.toList());
    }
}
