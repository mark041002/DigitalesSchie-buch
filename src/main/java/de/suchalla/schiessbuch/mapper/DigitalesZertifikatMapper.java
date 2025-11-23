package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.DigitalesZertifikatDTO;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import org.springframework.stereotype.Component;

/**
 * Mapper für DigitalesZertifikat-Entity <-> DigitalesZertifikatDTO.
 * Das privateKeyPEM-Feld wird aus Sicherheitsgründen nicht in DTOs gemappt.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class DigitalesZertifikatMapper implements BaseMapper<DigitalesZertifikat, DigitalesZertifikatDTO> {

    @Override
    public DigitalesZertifikatDTO toDTO(DigitalesZertifikat entity) {
        if (entity == null) {
            return null;
        }

        DigitalesZertifikatDTO.DigitalesZertifikatDTOBuilder builder = DigitalesZertifikatDTO.builder()
                .id(entity.getId())
                .zertifikatsTyp(entity.getZertifikatsTyp())
                .seriennummer(entity.getSeriennummer())
                .subjectDN(entity.getSubjectDN())
                .issuerDN(entity.getIssuerDN())
                .zertifikatPEM(entity.getZertifikatPEM())
                .gueltigSeit(entity.getGueltigSeit())
                .gueltigBis(entity.getGueltigBis())
                .widerrufen(entity.isWiderrufen())
                .widerrufenAm(entity.getWiderrufenAm())
                .widerrufsGrund(entity.getWiderrufsGrund())
                .erstelltAm(entity.getErstelltAm())
                .aktualisiertAm(entity.getAktualisiertAm());

        if (entity.getBenutzer() != null) {
            builder.benutzerId(entity.getBenutzer().getId())
                   .benutzerVollstaendigerName(entity.getBenutzer().getVollstaendigerName())
                   .benutzerEmail(entity.getBenutzer().getEmail());
        }
        if (entity.getVerein() != null) {
            builder.vereinId(entity.getVerein().getId())
                   .vereinName(entity.getVerein().getName())
                   .vereinAdresse(entity.getVerein().getAdresse());
        }
        if (entity.getSchiesstand() != null) {
            builder.schiesstandId(entity.getSchiesstand().getId())
                   .schiesstandName(entity.getSchiesstand().getName());
        }
        if (entity.getParentZertifikat() != null) {
            builder.parentZertifikatId(entity.getParentZertifikat().getId());
        }

        return builder.build();
    }

    // Note: toDTOList() wird von BaseMapper bereitgestellt
}

