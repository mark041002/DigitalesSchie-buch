package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragDetailDTO;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragListDTO;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper für SchiessnachweisEintrag-Entity <-> DTOs.
 *
 * Besonderheit: DTOs haben flache Struktur (schuetzeVorname, disziplinName, etc.)
 * statt verschachtelte Objekte, um LazyInitializationException zu vermeiden.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class SchiessnachweisEintragMapper implements BaseMapper<SchiessnachweisEintrag, SchiessnachweisEintragListDTO> {

    /**
     * Konvertiert Entity zu Listen-DTO (für Grid/Tabellen).
     * Setzt voraus, dass alle benötigten Beziehungen bereits geladen sind!
     */
    @Override
    public SchiessnachweisEintragListDTO toDTO(SchiessnachweisEintrag entity) {
        if (entity == null) {
            return null;
        }

        SchiessnachweisEintragListDTO.SchiessnachweisEintragListDTOBuilder builder = SchiessnachweisEintragListDTO.builder()
                .id(entity.getId())
                .datum(entity.getDatum())
                .anzahlSchuesse(entity.getAnzahlSchuesse())
                .ergebnis(entity.getErgebnis())
                .kaliber(entity.getKaliber())
                .waffenart(entity.getWaffenart())
                .status(entity.getStatus())
                .signiertAm(entity.getSigniertAm());

        // Schütze (sollte geladen sein)
        if (entity.getSchuetze() != null) {
            builder.schuetzeId(entity.getSchuetze().getId())
                    .schuetzeVorname(entity.getSchuetze().getVorname())
                    .schuetzeNachname(entity.getSchuetze().getNachname());
        }

        // Disziplin (sollte geladen sein)
        if (entity.getDisziplin() != null) {
            builder.disziplinId(entity.getDisziplin().getId())
                // Für die Tabellenansicht verwenden wir die Kennziffer als Label
                .disziplinName(entity.getDisziplin().getKennziffer())
                .disziplinProgramm(entity.getDisziplin().getProgramm())
                .disziplinWaffeKlasse(entity.getDisziplin().getWaffeKlasse());
        }

        // Schießstand und Verein (sollten geladen sein)
        if (entity.getSchiesstand() != null) {
            builder.schiesstandId(entity.getSchiesstand().getId())
                    .schiesstandName(entity.getSchiesstand().getName());

            if (entity.getSchiesstand().getVerein() != null) {
                builder.vereinId(entity.getSchiesstand().getVerein().getId())
                        .vereinName(entity.getSchiesstand().getVerein().getName());
            }
        }

        // Aufseher (optional)
        if (entity.getAufseher() != null) {
            builder.aufseherId(entity.getAufseher().getId())
                    .aufseherVorname(entity.getAufseher().getVorname())
                    .aufseherNachname(entity.getAufseher().getNachname());
        }

        // Zertifikat (optional)
        if (entity.getZertifikat() != null) {
            builder.zertifikatId(entity.getZertifikat().getId())
                    .zertifikatSeriennummer(entity.getZertifikat().getSeriennummer());
        }

        return builder.build();
    }

    /**
     * Konvertiert Entity zu Detail-DTO (für Detailansichten).
     * Setzt voraus, dass alle benötigten Beziehungen bereits geladen sind!
     */
    public SchiessnachweisEintragDetailDTO toDetailDTO(SchiessnachweisEintrag entity) {
        if (entity == null) {
            return null;
        }

        SchiessnachweisEintragDetailDTO.SchiessnachweisEintragDetailDTOBuilder builder = SchiessnachweisEintragDetailDTO.builder()
                .id(entity.getId())
                .datum(entity.getDatum())
                .anzahlSchuesse(entity.getAnzahlSchuesse())
                .ergebnis(entity.getErgebnis())
                .kaliber(entity.getKaliber())
                .waffenart(entity.getWaffenart())
                .bemerkung(entity.getBemerkung())
                .status(entity.getStatus())
                .istSigniert(entity.getIstSigniert())
                .signiertAm(entity.getSigniertAm())
                .digitaleSignatur(entity.getDigitaleSignatur())
                .ablehnungsgrund(entity.getAblehnungsgrund())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm());

        // Schütze
        if (entity.getSchuetze() != null) {
            builder.schuetzeId(entity.getSchuetze().getId())
                    .schuetzeVorname(entity.getSchuetze().getVorname())
                    .schuetzeNachname(entity.getSchuetze().getNachname())
                    .schuetzeEmail(entity.getSchuetze().getEmail());
        }

        // Disziplin
        if (entity.getDisziplin() != null) {
            builder.disziplinId(entity.getDisziplin().getId())
                // In Detailansichten die Kennziffer als primäre Kennung zeigen
                .disziplinName(entity.getDisziplin().getKennziffer())
                .disziplinProgramm(entity.getDisziplin().getProgramm())
                .disziplinWaffeKlasse(entity.getDisziplin().getWaffeKlasse());
        }

        // Schießstand
        if (entity.getSchiesstand() != null) {
            builder.schiesstandId(entity.getSchiesstand().getId())
                    .schiesstandName(entity.getSchiesstand().getName())
                    .schiesstandTyp(entity.getSchiesstand().getTyp() != null ?
                            entity.getSchiesstand().getTyp().name() : null);

            // Verein über Schießstand
            if (entity.getSchiesstand().getVerein() != null) {
                builder.vereinId(entity.getSchiesstand().getVerein().getId())
                        .vereinName(entity.getSchiesstand().getVerein().getName())
                        .vereinsNummer(entity.getSchiesstand().getVerein().getVereinsNummer());
            }
        }

        // Aufseher (optional)
        if (entity.getAufseher() != null) {
            builder.aufseherId(entity.getAufseher().getId())
                    .aufseherVorname(entity.getAufseher().getVorname())
                    .aufseherNachname(entity.getAufseher().getNachname())
                    .aufseherEmail(entity.getAufseher().getEmail());
        }

        // Zertifikat (optional)
        if (entity.getZertifikat() != null) {
            builder.zertifikatId(entity.getZertifikat().getId())
                    .zertifikatSeriennummer(entity.getZertifikat().getSeriennummer());
        }

        return builder.build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt

    /**
     * Konvertiert Liste von Entities zu Liste von Listen-DTOs.
     */
    public List<SchiessnachweisEintragListDTO> toListDTOList(List<SchiessnachweisEintrag> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Konvertiert Liste von Entities zu Liste von Detail-DTOs.
     */
    public List<SchiessnachweisEintragDetailDTO> toDetailDTOList(List<SchiessnachweisEintrag> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }
}
