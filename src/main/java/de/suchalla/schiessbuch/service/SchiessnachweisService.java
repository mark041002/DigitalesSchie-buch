package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.SchiessnachweisEintragMapper;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragDetailDTO;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintragListDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service für Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SchiessnachweisService {

    private final SchiessnachweisEintragRepository eintragRepository;
    private final SchiessnachweisEintragMapper eintragMapper;

    /**
     * Erstellt einen neuen Schießnachweis-Eintrag.
     *
     * @param eintrag Der zu erstellende Eintrag
     */
    public void erstelleEintrag(SchiessnachweisEintrag eintrag) {
        eintrag.setStatus(EintragStatus.UNSIGNIERT);
        eintragRepository.save(eintrag);
    }

    /**
     * Findet einen Eintrag anhand der ID und gibt ihn als DetailDTO zurück.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag als DetailDTO
     */
    @Transactional(readOnly = true)
    public Optional<SchiessnachweisEintragDetailDTO> findeEintragMitVerein(Long id) {
        return eintragRepository.findById(id)
                .map(eintragMapper::toDetailDTO);
    }

    /**
     * Findet alle Einträge eines Schützen in einem Zeitraum und gibt sie als ListDTOs zurück.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge als ListDTOs
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintragListDTO> findeEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        List<SchiessnachweisEintrag> entities = eintragRepository.findBySchuetzeAndDatumBetween(schuetze, von, bis);
        return eintragMapper.toListDTOList(entities);
    }

    /**
     * Findet alle Einträge eines Schützen und gibt sie als ListDTOs zurück.
     *
     * @param schuetze Der Schütze
     * @return Liste der Einträge als ListDTOs
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintragListDTO> findeEintraegeFuerSchuetze(Benutzer schuetze) {
        List<SchiessnachweisEintrag> entities = eintragRepository.findBySchuetze(schuetze);
        return eintragMapper.toListDTOList(entities);
    }

    /**
     * Findet alle signierten Einträge eines Schützen in einem Zeitraum als ListDTOs.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der signierten Einträge als ListDTOs
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintragListDTO> findeSignierteEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        List<SchiessnachweisEintrag> entities = eintragRepository.findBySchuetzeAndDatumBetweenAndStatus(
                schuetze, von, bis, EintragStatus.SIGNIERT);
        return eintragMapper.toListDTOList(entities);
    }

    /**
     * Findet alle unsignierten Einträge an einem Schießstand als ListDTOs.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der unsignierten Einträge als ListDTOs
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintragListDTO> findeUnsignierteEintraege(Schiesstand schiesstand) {
        List<SchiessnachweisEintrag> entities = eintragRepository.findBySchiesstandAndStatus(schiesstand, EintragStatus.UNSIGNIERT);
        return eintragMapper.toListDTOList(entities);
    }

    /**
     * Findet alle Einträge an einem Schießstand als ListDTOs.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der Einträge als ListDTOs
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintragListDTO> findeEintraegeAnSchiesstand(Schiesstand schiesstand) {
        List<SchiessnachweisEintrag> entities = eintragRepository.findBySchiesstand(schiesstand);
        return eintragMapper.toListDTOList(entities);
    }

    /**
     * Signiert einen Eintrag.
     *
     * @param eintragId Die Eintrags-ID
     * @param aufseher  Der Aufseher
     * @param signatur  Die digitale Signatur
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public void signiereEintrag(Long eintragId, Benutzer aufseher, String signatur) {
        SchiessnachweisEintrag eintrag = eintragRepository.findById(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() != EintragStatus.UNSIGNIERT) {
            throw new IllegalStateException("Eintrag wurde bereits bearbeitet");
        }

        eintrag.setStatus(EintragStatus.SIGNIERT);
        eintrag.setAufseher(aufseher);
        eintrag.setSigniertAm(LocalDateTime.now());
        eintrag.setDigitaleSignatur(signatur);

        eintragRepository.save(eintrag);
    }

    /**
     * Lehnt einen Eintrag ab.
     *
     * @param eintragId       Die Eintrags-ID
     * @param aufseher        Der Aufseher
     * @param ablehnungsgrund Der Grund der Ablehnung
     * @throws IllegalStateException wenn Eintrag bereits bearbeitet
     */
    @Transactional
    public void lehneEintragAb(Long eintragId, Benutzer aufseher, String ablehnungsgrund) {
        SchiessnachweisEintrag eintrag = eintragRepository.findById(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() != EintragStatus.UNSIGNIERT) {
            throw new IllegalStateException("Eintrag wurde bereits bearbeitet");
        }

        eintrag.setStatus(EintragStatus.ABGELEHNT);
        eintrag.setAufseher(aufseher);
        eintrag.setAblehnungsgrund(ablehnungsgrund);
        eintrag.setSigniertAm(LocalDateTime.now());

        eintragRepository.save(eintrag);
    }


    /**
     * Löscht einen unsignierten Eintrag.
     *
     * @param eintragId Die Eintrags-ID
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public void loescheEintrag(Long eintragId) {
        SchiessnachweisEintrag eintrag = eintragRepository.findById(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() == EintragStatus.SIGNIERT) {
            throw new IllegalStateException("Signierte Einträge können nicht gelöscht werden");
        }

        eintragRepository.delete(eintrag);
    }



    /**
     * Zählt unsignierte Einträge eines Schützen.
     *
     * @param schuetze Der Schütze
     * @return Anzahl unsignierter Einträge
     */
    @Transactional(readOnly = true)
    public long zaehleUnsignierteEintraege(Benutzer schuetze) {
        return eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.UNSIGNIERT);
    }
}