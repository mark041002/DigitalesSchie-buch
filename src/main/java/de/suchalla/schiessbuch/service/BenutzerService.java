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
    private final EmailService emailService;

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
        benutzer.setAktiv(true);

        return benutzerRepository.save(benutzer);
    }

    /**
     * Findet einen Benutzer anhand der ID.
     *
     * @param id Die Benutzer-ID
     * @return Optional mit Benutzer
     */
    @Transactional(readOnly = true)
    public Optional<Benutzer> findeBenutzer(Long id) {
        return benutzerRepository.findById(id);
    }

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Optional mit Benutzer
     */
    @Transactional(readOnly = true)
    public Optional<Benutzer> findeBenutzerNachEmail(String email) {
        return benutzerRepository.findByEmail(email);
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
     * @return Der aktualisierte Benutzer
     */
    public Benutzer aktualisiereBenutzer(Benutzer benutzer) {
        return benutzerRepository.save(benutzer);
    }

    /**
     * Ändert das Passwort eines Benutzers.
     *
     * @param benutzerId Die Benutzer-ID
     * @param altesPasswort Das alte Passwort
     * @param neuesPasswort Das neue Passwort
     * @throws IllegalArgumentException wenn alte Passwort nicht korrekt ist
     */
    public void aenderePasswort(Long benutzerId, String altesPasswort, String neuesPasswort) {
        Benutzer benutzer = benutzerRepository.findById(benutzerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        if (!passwordEncoder.matches(altesPasswort, benutzer.getPasswort())) {
            throw new IllegalArgumentException("Altes Passwort ist nicht korrekt");
        }

        benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
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
     * Initiiert einen Passwort-Reset.
     *
     * @param email Die E-Mail-Adresse des Benutzers
     * @return true wenn erfolgreich
     */
    public boolean initiierePasswortReset(String email) {
        Optional<Benutzer> benutzerOpt = benutzerRepository.findByEmail(email);
        if (benutzerOpt.isEmpty()) {
            return false;
        }

        Benutzer benutzer = benutzerOpt.get();
        String token = UUID.randomUUID().toString();
        benutzer.setResetToken(token);
        benutzer.setResetTokenAblauf(LocalDateTime.now().plusHours(24));
        benutzerRepository.save(benutzer);

        emailService.sendePasswortResetEmail(benutzer.getEmail(), token);
        return true;
    }

    /**
     * Setzt das Passwort mittels Reset-Token zurück.
     *
     * @param token Das Reset-Token
     * @param neuesPasswort Das neue Passwort
     * @throws IllegalArgumentException wenn Token ungültig oder abgelaufen
     */
    public void setzePasswortZurueck(String token, String neuesPasswort) {
        Benutzer benutzer = benutzerRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Ungültiges Reset-Token"));

        if (benutzer.getResetTokenAblauf().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset-Token ist abgelaufen");
        }

        benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
        benutzer.setResetToken(null);
        benutzer.setResetTokenAblauf(null);
        benutzerRepository.save(benutzer);
    }

    /**
     * Ändert die Rolle eines Benutzers.
     *
     * @param benutzerId Die Benutzer-ID
     * @param neueRolle Die neue Rolle
     * @return Der aktualisierte Benutzer
     */
    public Benutzer aendereRolle(Long benutzerId, BenutzerRolle neueRolle) {
        Benutzer benutzer = benutzerRepository.findById(benutzerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        benutzer.setRolle(neueRolle);
        return benutzerRepository.save(benutzer);
    }

    /**
     * Deaktiviert einen Benutzer.
     *
     * @param benutzerId Die Benutzer-ID
     */
    public void deaktiviereBenutzer(Long benutzerId) {
        Benutzer benutzer = benutzerRepository.findById(benutzerId)
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        benutzer.setAktiv(false);
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
        benutzer.setAktiv(false);
        benutzerRepository.save(benutzer);
    }
}
