package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VereinDTO;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper für Verein-Entity <-> VereinDTO.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class VereinMapper implements BaseMapper<Verein, VereinDTO> {

    @Override
    public VereinDTO toDTO(Verein entity) {
        if (entity == null) {
            return null;
        }

        // Finde Vereinschef aus Mitgliedschaften
        String vereinschefName = null;
        Long vereinschefId = null;
        if (entity.getMitgliedschaften() != null) {
            var vereinschefMitgliedschaft = entity.getMitgliedschaften().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIstVereinschef()))
                    .findFirst();
            if (vereinschefMitgliedschaft.isPresent()) {
                var benutzer = vereinschefMitgliedschaft.get().getBenutzer();
                if (benutzer != null) {
                    vereinschefName = benutzer.getVollstaendigerName();
                    vereinschefId = benutzer.getId();
                }
            }
        }

        return VereinDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .vereinsNummer(entity.getVereinsNummer())
                .adresse(entity.getAdresse())
                .beschreibung(entity.getBeschreibung())
                .verbandIds(entity.getVerbaende() != null ?
                        entity.getVerbaende().stream()
                                .map(Verband::getId)
                                .collect(Collectors.toList()) : List.of())
                .verbandNamen(entity.getVerbaende() != null ?
                        entity.getVerbaende().stream()
                                .map(Verband::getName)
                                .collect(Collectors.toList()) : List.of())
                .mitgliederAnzahl(entity.getMitgliedschaften() != null ?
                        entity.getMitgliedschaften().size() : 0)
                .vereinschefName(vereinschefName)
                .vereinschefId(vereinschefId)
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm())
                .build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}
