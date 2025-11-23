package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VerbandDTO;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.springframework.stereotype.Component;

/**
 * Mapper für Verband-Entity <-> VerbandDTO.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class VerbandMapper implements BaseMapper<Verband, VerbandDTO> {

    @Override
    public VerbandDTO toDTO(Verband entity) {
        if (entity == null) {
            return null;
        }

        return VerbandDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .beschreibung(entity.getBeschreibung())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm())
                .build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}
