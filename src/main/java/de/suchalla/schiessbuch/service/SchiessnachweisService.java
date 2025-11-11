package de.suchalla.schiessbuch.service;

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
 * Service fÃ¼r SchieÃŸnachweis-EintrÃ¤ge.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SchiessnachweisService {

    private final SchiessnachweisEintragRepository eintragRepository;

    /**
     * Erstellt einen neuen SchieÃŸnachweis-Eintrag.
     *
     * @param eintrag Der zu erstellende Eintrag
     */
    public void erstelleEintrag(SchiessnachweisEintrag eintrag) {
        eintrag.setStatus(EintragStatus.UNSIGNIERT);
        eintragRepository.save(eintrag);
    }

    /**
     * Findet einen Eintrag anhand der ID inklusive Verein-Daten.
     * Verwendet JOIN FETCH um LazyInitializationException beim Zugriff auf
     * schiesstand.verein zu vermeiden.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag inkl. Verein
     */
    @Transactional(readOnly = true)
    public Optional<SchiessnachweisEintrag> findeEintragMitVerein(Long id) {
        return eintragRepository.findByIdWithVerein(id);
    }

    /**
     * Findet alle EintrÃ¤ge eines SchÃ¼tzen in einem Zeitraum.
     *
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        return eintragRepository.findBySchuetzeAndDatumBetween(schuetze, von, bis);
    }

    /**
     * Findet alle signierten EintrÃ¤ge eines SchÃ¼tzen in einem Zeitraum.
     *
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der signierten EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeSignierteEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        return eintragRepository.findBySchuetzeAndDatumBetweenAndStatus(
                schuetze, von, bis, EintragStatus.SIGNIERT);
    }

    /**
     * Findet alle unsignierten EintrÃ¤ge an einem SchieÃŸstand.
     *
     * @param schiesstand Der SchieÃŸstand
     * @return Liste der unsignierten EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeUnsignierteEintraege(Schiesstand schiesstand) {
        return eintragRepository.findBySchiesstandAndStatus(schiesstand, EintragStatus.UNSIGNIERT);
    }

    /**
     * Findet alle EintrÃ¤ge an einem SchieÃŸstand.
     *
     * @param schiesstand Der SchieÃŸstand
     * @return Liste der EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeAnSchiesstand(Schiesstand schiesstand) {
        return eintragRepository.findBySchiesstand(schiesstand);
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
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
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
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
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
     * LÃ¶scht einen unsignierten Eintrag.
     *
     * @param eintragId Die Eintrags-ID
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public void loescheEintrag(Long eintragId) {
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() == EintragStatus.SIGNIERT) {
            throw new IllegalStateException("Signierte EintrÃ¤ge kÃ¶nnen nicht gelÃ¶scht werden");
        }

        eintragRepository.delete(eintrag);
    }



    /**
     * ZÃ¤hlt unsignierte EintrÃ¤ge eines SchÃ¼tzen.
     *
     * @param schuetze Der SchÃ¼tze
     * @return Anzahl unsignierter EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public long zaehleUnsignierteEintraege(Benutzer schuetze) {
        return eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.UNSIGNIERT);
    }
}