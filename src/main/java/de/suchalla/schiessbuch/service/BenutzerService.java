package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service für Benutzer-Verwaltung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BenutzerService {

    private final BenutzerRepository benutzerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registriert einen neuen Benutzer.
     *
     * @param benutzer Der zu registrierende Benutzer
     * @return Der gespeicherte Benutzer
     * @throws IllegalArgumentException wenn E-Mail bereits existiert
     */
    public Benutzer registriereBenutzer(Benutzer benutzer) {
        if (benutzerRepository.existsByEmail(benutzer.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits registriert");
        }

        benutzer.setPasswort(passwordEncoder.encode(benutzer.getPasswort()));
        benutzer.setRolle(BenutzerRolle.SCHUETZE);

        return benutzerRepository.save(benutzer);
    }

    /**
     * Gibt alle Benutzer zurück.
     *
     * @return Liste aller Benutzer
     */
    @Transactional(readOnly = true)
    public List<Benutzer> findAlleBenutzer() {
        return benutzerRepository.findAll();
    }

    /**
     * Findet alle Benutzer mit einer bestimmten Rolle.
     *
     * @param rolle Die Benutzerrolle
     * @return Liste der Benutzer mit dieser Rolle
     */
    @Transactional(readOnly = true)
    public List<Benutzer> findByRolle(BenutzerRolle rolle) {
        return benutzerRepository.findByRolle(rolle);
    }
    /**
     * Aktualisiert einen Benutzer.
     *
     * @param benutzer Der zu aktualisierende Benutzer
     */
    public void aktualisiereBenutzer(Benutzer benutzer) {
        benutzerRepository.save(benutzer);
    }

    /**
     * Ändert das Passwort eines Benutzers ohne Überprüfung des alten Passworts.
     *
     * @param benutzerId Die Benutzer-ID
     * @param neuesPasswort Das neue Passwort
     */
    public void aenderePasswortOhneAltes(Long benutzerId, String neuesPasswort) {
        Benutzer benutzer = benutzerRepository.findById(benutzerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
        benutzerRepository.save(benutzer);
    }

    /**
     * Löscht einen Benutzer und alle personenbezogenen Daten.
     *
     * @param benutzerId Die Benutzer-ID
     */
    public void loescheBenutzer(Long benutzerId) {
        Benutzer benutzer = benutzerRepository.findById(benutzerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        // Alle persönlichen Daten anonymisieren statt komplett zu löschen
        benutzer.setVorname("Gelöscht");
        benutzer.setNachname("Gelöscht");
        benutzer.setEmail("geloescht_" + benutzerId + "@deleted.local");
        benutzerRepository.save(benutzer);
    }
}
