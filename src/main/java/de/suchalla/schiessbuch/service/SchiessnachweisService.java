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
     * Findet alle Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der Einträge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        // Nutze die sichere Query mit JOIN FETCH, damit Schiesstand und Verein geladen werden
        List<SchiessnachweisEintrag> result = eintragRepository.findBySchuetzeAndDatumBetweenWithVerein(schuetze, von, bis);
        // Touch Verein-Name, um sicher zu gehen, dass die Proxy-Objekte initialisiert sind
        result.forEach(e -> {
            if (e.getSchiesstand() != null && e.getSchiesstand().getVerein() != null) {
                e.getSchiesstand().getVerein().getName();
            }
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeFuerSchuetze(Benutzer schuetze) {
        // EntityGraph für findBySchuetze wurde aktualisiert, das lädt ebenfalls Schiesstand.verein
        List<SchiessnachweisEintrag> result = eintragRepository.findBySchuetze(schuetze);
        result.forEach(e -> {
            if (e.getSchiesstand() != null && e.getSchiesstand().getVerein() != null) {
                e.getSchiesstand().getVerein().getName();
            }
        });
        return result;
    }

    /**
     * Findet alle signierten Einträge eines Schützen in einem Zeitraum.
     *
     * @param schuetze Der Schütze
     * @param von Start-Datum
     * @param bis End-Datum
     * @return Liste der signierten Einträge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeSignierteEintraegeImZeitraum(Benutzer schuetze, LocalDate von, LocalDate bis) {
        List<SchiessnachweisEintrag> result = eintragRepository.findBySchuetzeAndDatumBetweenAndStatusWithVerein(
                schuetze, von, bis, EintragStatus.SIGNIERT);
        result.forEach(e -> {
            if (e.getSchiesstand() != null && e.getSchiesstand().getVerein() != null) {
                e.getSchiesstand().getVerein().getName();
            }
        });
        return result;
    }

    /**
     * Findet alle unsignierten Einträge an einem Schießstand.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der unsignierten Einträge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeUnsignierteEintraege(Schiesstand schiesstand) {
        List<SchiessnachweisEintrag> result = eintragRepository.findBySchiesstandAndStatusWithVerein(schiesstand, EintragStatus.UNSIGNIERT);
        result.forEach(e -> {
            if (e.getSchiesstand() != null && e.getSchiesstand().getVerein() != null) {
                e.getSchiesstand().getVerein().getName();
            }
        });
        return result;
    }

    /**
     * Findet alle Einträge an einem Schießstand.
     *
     * @param schiesstand Der Schießstand
     * @return Liste der Einträge
     */
    @Transactional(readOnly = true)
    public List<SchiessnachweisEintrag> findeEintraegeAnSchiesstand(Schiesstand schiesstand) {
        List<SchiessnachweisEintrag> result = eintragRepository.findBySchiesstandWithVerein(schiesstand);
        result.forEach(e -> {
            if (e.getSchiesstand() != null && e.getSchiesstand().getVerein() != null) {
                e.getSchiesstand().getVerein().getName();
            }
        });
        return result;
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
     * Löscht einen unsignierten Eintrag.
     *
     * @param eintragId Die Eintrags-ID
     * @throws IllegalStateException wenn Eintrag bereits signiert
     */
    @Transactional
    public void loescheEintrag(Long eintragId) {
        SchiessnachweisEintrag eintrag = eintragRepository.findByIdWithDetails(eintragId)
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