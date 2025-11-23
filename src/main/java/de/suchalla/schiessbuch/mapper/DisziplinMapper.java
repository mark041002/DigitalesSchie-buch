package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.DisziplinDTO;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import org.springframework.stereotype.Component;


/**
 * Mapper für Disziplin-Entity <-> DisziplinDTO.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class DisziplinMapper implements BaseMapper<Disziplin, DisziplinDTO> {

    @Override
    public DisziplinDTO toDTO(Disziplin entity) {
        if (entity == null) {
            return null;
        }

        DisziplinDTO.DisziplinDTOBuilder builder = DisziplinDTO.builder()
            .id(entity.getId())
            .kennziffer(entity.getKennziffer())
            .programm(entity.getProgramm())
            .waffeKlasse(entity.getWaffeKlasse())
            .erstelltAm(entity.getErstelltAm())
            .aktualisiertAm(entity.getAktualisiertAm());

        // Verband (LAZY - sollte geladen sein wenn benötigt)
        if (entity.getVerband() != null) {
                builder.verbandId(entity.getVerband().getId())
                    .verbandName(entity.getVerband().getName());
        }

        return builder.build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}
