package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VereinsmigliedschaftDTO;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import org.springframework.stereotype.Component;

/**
 * Mapper für Vereinsmitgliedschaft-Entity <-> VereinsmigliedschaftDTO.
 *
 * Achtung: Vereinsmitgliedschaft lädt Benutzer und Verein EAGER.
 * Der Mapper setzt voraus, dass diese geladen sind.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class VereinsmigliedschaftMapper implements BaseMapper<Vereinsmitgliedschaft, VereinsmigliedschaftDTO> {

    @Override
    public VereinsmigliedschaftDTO toDTO(Vereinsmitgliedschaft entity) {
        if (entity == null) {
            return null;
        }

        VereinsmigliedschaftDTO.VereinsmigliedschaftDTOBuilder builder = VereinsmigliedschaftDTO.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .beitrittDatum(entity.getBeitrittDatum())
                .austrittDatum(entity.getAustrittDatum())
                .istAufseher(entity.getIstAufseher())
                .istVereinschef(entity.getIstVereinschef())
                .aktiv(entity.getAktiv())
                .ablehnungsgrund(entity.getAblehnungsgrund())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm());

        // Benutzer (EAGER geladen)
        if (entity.getBenutzer() != null) {
            builder.benutzerId(entity.getBenutzer().getId())
                    .benutzerVorname(entity.getBenutzer().getVorname())
                    .benutzerNachname(entity.getBenutzer().getNachname())
                    .benutzerEmail(entity.getBenutzer().getEmail());
        }

        // Verein (EAGER geladen)
        if (entity.getVerein() != null) {
            builder.vereinId(entity.getVerein().getId())
                    .vereinName(entity.getVerein().getName())
                    .vereinsNummer(entity.getVerein().getVereinsNummer());
        }

        return builder.build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}

