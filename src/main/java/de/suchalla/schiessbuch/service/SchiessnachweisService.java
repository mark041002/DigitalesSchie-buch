package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Disziplin;
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
    private final BenachrichtigungsService benachrichtigungsService;

    /**
     * Erstellt einen neuen SchieÃŸnachweis-Eintrag.
     *
     * @param eintrag Der zu erstellende Eintrag
     * @return Der gespeicherte Eintrag
     */
    public SchiessnachweisEintrag erstelleEintrag(SchiessnachweisEintrag eintrag) {
        eintrag.setStatus(EintragStatus.UNSIGNIERT);
        return eintragRepository.save(eintrag);
    }

    /**
     * Erstellt einen neuen Eintrag mit allen Parametern.
     *
     * @param schuetze Der SchÃ¼tze
     * @param datum Das Datum
     * @param disziplin Die Disziplin
     * @param kaliber Das Kaliber
     * @param waffenart Die Waffenart
     * @param schiesstand Der SchieÃŸstand
     * @param anzahlSchuesse Anzahl der SchÃ¼sse
     * @param bemerkung Bemerkung
     * @return Der gespeicherte Eintrag
     */
    public SchiessnachweisEintrag erstelleEintrag(Benutzer schuetze, LocalDate datum,
                                                  Disziplin disziplin, String kaliber,
                                                  String waffenart, Schiesstand schiesstand,
                                                  Integer anzahlSchuesse, String bemerkung) {
        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .datum(datum)
                .disziplin(disziplin)
                .kaliber(kaliber)
                .waffenart(waffenart)
                .schiesstand(schiesstand)
                .anzahlSchuesse(anzahlSchuesse)
                .bemerkung(bemerkung)
                .status(EintragStatus.UNSIGNIERT)
                .build();

        return eintragRepository.save(eintrag);
    }

    /**
     * Findet einen Eintrag anhand der ID.
     *
     * @param id Die Eintrags-ID
     * @return Optional mit Eintrag
     */
    @Transactional(readOnly = true)
    public Optional<SchiessnachweisEintrag> findeEintrag(Long id) {
        return eintragRepository.findByIdWithDetails(id);
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
     * Findet alle EintrÃ¤ge eines SchÃ¼tzen.
     *
     * @param schuetze Der SchÃ¼tze
     * @return Liste der EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeVonSchuetze(Benutzer schuetze) {
        return eintragRepository.findBySchuetzeWithDetails(schuetze);
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
    public List<SchiessnachweisEintrag> findeEintraegeImZeitraum(Benutzer schuetze,
                                                                 LocalDate von, LocalDate bis) {
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
    public List<SchiessnachweisEintrag> findeSignierteEintraegeImZeitraum(Benutzer schuetze,
                                                                          LocalDate von, LocalDate bis) {
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
     * Findet EintrÃ¤ge eines SchieÃŸstands fÃ¼r einen bestimmten SchÃ¼tzen im Zeitraum.
     *
     * @param schiesstand Der SchieÃŸstand
     * @param schuetze Der SchÃ¼tze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeAnSchiesstandFuerSchuetze(
            Schiesstand schiesstand, Benutzer schuetze, LocalDate von, LocalDate bis) {
        return eintragRepository.findBySchiesstandUndSchuetzeImZeitraum(schiesstand, schuetze, von, bis);
    }

    /**
     * Signiert einen Eintrag.
     *
     * @param eintragId Die Eintrags-ID
     * @param aufseher Der Aufseher
     * @param signatur Die digitale Signatur
     * @return Der signierte Eintrag
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public SchiessnachweisEintrag signiereEintrag(Long eintragId, Benutzer aufseher, String signatur) {
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() != EintragStatus.UNSIGNIERT) {
            throw new IllegalStateException("Eintrag wurde bereits bearbeitet");
        }

        eintrag.setStatus(EintragStatus.SIGNIERT);
        eintrag.setAufseher(aufseher);
        eintrag.setSigniertAm(LocalDateTime.now());
        eintrag.setDigitaleSignatur(signatur);

        SchiessnachweisEintrag gespeicherterEintrag = eintragRepository.save(eintrag);

        // Benachrichtigung an SchÃ¼tzen senden
        benachrichtigungsService.benachrichtigeEintragSigniert(eintrag.getSchuetze(), eintrag);

        return gespeicherterEintrag;
    }

    /**
     * Lehnt einen Eintrag ab.
     *
     * @param eintragId Die Eintrags-ID
     * @param aufseher Der Aufseher
     * @param ablehnungsgrund Der Grund der Ablehnung
     * @return Der abgelehnte Eintrag
     * @throws IllegalStateException wenn Eintrag bereits bearbeitet
     */
    @Transactional
    public SchiessnachweisEintrag lehneEintragAb(Long eintragId, Benutzer aufseher, String ablehnungsgrund) {
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
                .orElseThrow(() -> new IllegalArgumentException("Eintrag nicht gefunden"));

        if (eintrag.getStatus() != EintragStatus.UNSIGNIERT) {
            throw new IllegalStateException("Eintrag wurde bereits bearbeitet");
        }

        eintrag.setStatus(EintragStatus.ABGELEHNT);
        eintrag.setAufseher(aufseher);
        eintrag.setAblehnungsgrund(ablehnungsgrund);
        eintrag.setSigniertAm(LocalDateTime.now());

        SchiessnachweisEintrag gespeicherterEintrag = eintragRepository.save(eintrag);

        // Benachrichtigung an SchÃ¼tzen senden
        benachrichtigungsService.benachrichtigeEintragAbgelehnt(eintrag.getSchuetze(), eintrag, ablehnungsgrund);

        return gespeicherterEintrag;
    }

    /**
     * Aktualisiert einen unsignierten Eintrag.
     *
     * @param eintrag Der zu aktualisierende Eintrag
     * @return Der aktualisierte Eintrag
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public SchiessnachweisEintrag aktualisiereEintrag(SchiessnachweisEintrag eintrag) {
        if (!eintrag.kannBearbeitetWerden()) {
            throw new IllegalStateException("Signierte EintrÃ¤ge kÃ¶nnen nicht bearbeitet werden");
        }
        return eintragRepository.save(eintrag);
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

        if (!eintrag.kannGeloeschtWerden()) {
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

    /**
     * Findet alle unsignierten EintrÃ¤ge fÃ¼r eine Liste von Vereinen.
     *
     * @param vereine Liste der Vereine
     * @return Liste der unsignierten EintrÃ¤ge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> getUnsignierteEintraegeForVereine(List<de.suchalla.schiessbuch.model.entity.Verein> vereine) {
        return vereine.stream()
                .flatMap(verein -> verein.getSchiesstaende().stream())
                .flatMap(schiesstand -> eintragRepository.findBySchiesstandAndStatus(
                        schiesstand, EintragStatus.UNSIGNIERT).stream())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }
}