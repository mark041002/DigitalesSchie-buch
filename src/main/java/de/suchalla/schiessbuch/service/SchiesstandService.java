package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SchiesstandService {
    private final SchiesstandRepository schiesstandRepository;
    private final PkiService pkiService;

    @Autowired
    public SchiesstandService(SchiesstandRepository schiesstandRepository, PkiService pkiService) {
        this.schiesstandRepository = schiesstandRepository;
        this.pkiService = pkiService;
    }

    public List<Schiesstand> findAll() {
        return schiesstandRepository.findAll();
    }

    public Optional<Schiesstand> findById(Long id) {
        return schiesstandRepository.findById(id);
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

    @Transactional
    public void deleteById(Long id) {
        schiesstandRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return schiesstandRepository.existsById(id);
    }

    public long count() {
        return schiesstandRepository.count();
    }

    public List<Schiesstand> findByVerein(Verein verein) {
        return schiesstandRepository.findByVerein(verein);
    }

    public List<Schiesstand> findByTyp(SchiesstandTyp typ) {
        return schiesstandRepository.findByTyp(typ);
    }

    public List<Schiesstand> findByNameContainingIgnoreCase(String name) {
        return schiesstandRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Schiesstand> findAllWithVerein() {
        return schiesstandRepository.findAllWithVerein();
    }

    /**
     * Erstellt Zertifikate für alle gewerblichen Schießstände, die noch keines haben.
     * Nützlich für Migration oder nachträgliche Erstellung.
     */
    @Transactional
    public void createCertificatesForAllCommercialStands() {
        log.info("Starte Erstellung von Zertifikaten für gewerbliche Schießstände...");
        List<Schiesstand> gewerblicheStaende = findByTyp(SchiesstandTyp.GEWERBLICH);

        int created = 0;
        for (Schiesstand schiesstand : gewerblicheStaende) {
            if (schiesstand.getAufseher() != null) {
                try {
                    log.info("Erstelle Zertifikat für Schießstandaufseher: {} am Schießstand: {}",
                            schiesstand.getAufseher().getVollstaendigerName(), schiesstand.getName());
                    pkiService.createSchiesstandaufseheCertificate(schiesstand.getAufseher(), schiesstand);
                    created++;
                } catch (Exception e) {
                    log.error("Fehler beim Erstellen des Zertifikats für Schießstand: " + schiesstand.getName(), e);
                }
            } else {
                log.warn("Schießstand {} hat keinen Aufseher zugewiesen, überspringe Zertifikatserstellung",
                        schiesstand.getName());
            }
        }

        log.info("Zertifikatserstellung abgeschlossen. {} Zertifikate erstellt.", created);
    }

}
