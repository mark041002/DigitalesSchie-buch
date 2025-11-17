package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.SchiesstandMapper;
import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SchiesstandService {
    private final SchiesstandRepository schiesstandRepository;
    private final PkiService pkiService;
    private final SchiesstandMapper schiesstandMapper;

    @Autowired
    public SchiesstandService(SchiesstandRepository schiesstandRepository, PkiService pkiService,
                              SchiesstandMapper schiesstandMapper) {
        this.schiesstandRepository = schiesstandRepository;
        this.pkiService = pkiService;
        this.schiesstandMapper = schiesstandMapper;
    }

    @Transactional(readOnly = true)
    public List<SchiesstandDTO> findAll() {
        List<Schiesstand> entities = schiesstandRepository.findAll();
        return schiesstandMapper.toDTOList(entities);
    }

    @Transactional
    public Schiesstand save(Schiesstand schiesstand) {
        Schiesstand saved = schiesstandRepository.save(schiesstand);

        // Automatisch Zertifikat für Schießstandaufseher erstellen, wenn:
        // 1. Es ein gewerblicher Schießstand ist
        // 2. Ein Aufseher zugewiesen ist
        if (saved.getTyp() == SchiesstandTyp.GEWERBLICH && saved.getAufseher() != null) {
            try {
                log.info("Erstelle Zertifikat für Schießstandaufseher: {} am Schießstand: {}",
                        saved.getAufseher().getVollstaendigerName(), saved.getName());
                pkiService.createSchiesstandaufseheCertificate(saved.getAufseher(), saved);
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Schießstandaufseher-Zertifikats", e);
                // Wir werfen den Fehler nicht weiter, damit der Schießstand trotzdem gespeichert wird
            }
        }

        return saved;
    }

}
