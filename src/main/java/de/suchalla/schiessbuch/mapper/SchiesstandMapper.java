package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import org.springframework.stereotype.Component;

/**
 * Mapper für Schiesstand-Entity <-> SchiesstandDTO.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class SchiesstandMapper implements BaseMapper<Schiesstand, SchiesstandDTO> {

    @Override
    public SchiesstandDTO toDTO(Schiesstand entity) {
        if (entity == null) {
            return null;
        }

        SchiesstandDTO.SchiesstandDTOBuilder builder = SchiesstandDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .typ(entity.getTyp())
                .adresse(entity.getAdresse())
                .beschreibung(entity.getBeschreibung())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm());

        // Verein (LAZY - sollte geladen sein wenn benötigt)
        if (entity.getVerein() != null) {
            builder.vereinId(entity.getVerein().getId())
                    .vereinName(entity.getVerein().getName());
        }

        // Aufseher (LAZY - sollte geladen sein wenn benötigt)
        if (entity.getAufseher() != null) {
            builder.aufseherId(entity.getAufseher().getId())
                    .aufseherVorname(entity.getAufseher().getVorname())
                    .aufseherNachname(entity.getAufseher().getNachname());
        }

        return builder.build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}
