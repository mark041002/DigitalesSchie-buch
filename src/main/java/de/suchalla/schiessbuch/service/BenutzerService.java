package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.BenutzerMapper;
import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.UserToken;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

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
    private final UserTokenRepository userTokenRepository;
    private final DigitalesZertifikatRepository digitalesZertifikatRepository;
    private final BenutzerMapper benutzerMapper;

    /**
     * Registriert einen neuen Benutzer.
     *
     * @param benutzer Der zu registrierende Benutzer
     * @throws IllegalArgumentException wenn E-Mail bereits existiert
     */
    public void registriereBenutzer(Benutzer benutzer) {
        if (benutzerRepository.existsByEmail(benutzer.getEmail())) {
            throw new IllegalArgumentException("E-Mail bereits registriert");
        }

        benutzer.setPasswort(passwordEncoder.encode(benutzer.getPasswort()));
        benutzer.setRolle(BenutzerRolle.SCHUETZE);

        benutzerRepository.save(benutzer);
    }

    /**
     * Gibt alle Benutzer als DTOs zurück (OHNE password-Hash!).
     *
     * @return Liste aller Benutzer als DTOs
     */
    @Transactional(readOnly = true)
    public List<BenutzerDTO> findAlleBenutzer() {
        List<Benutzer> entities = benutzerRepository.findAll();
        return benutzerMapper.toDTOList(entities);
    }

    /**
     * Gibt alle Benutzer als Entities zurück (für interne Verwendung wie ComboBoxen).
     *
     * @return Liste aller Benutzer als Entities
     */
    @Transactional(readOnly = true)
    public List<Benutzer> findAlleBenutzerEntities() {
        return benutzerRepository.findAll();
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
     * @param benutzer Der Benutzer
     */
    public void loescheBenutzer(Benutzer benutzer) {
        // Sicherstellen, dass wir auf einer managed-Instanz arbeiten to avoid transient/merge issues
        if (benutzer == null) {
            return;
        }

        if (benutzer.getId() == null) {
            // Kein ID verfügbar - versuchen, direkt zu löschen (keine bessere Option)
            benutzerRepository.delete(benutzer);
            return;
        }

        Benutzer managed = benutzerRepository.findById(benutzer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        // Zuerst alle zugehörigen Zertifikate löschen (DB FK verhindert sonst das Löschen des Benutzers)
        digitalesZertifikatRepository.deleteAllByBenutzerId(managed.getId());

        // Dann alle zugehörigen UserTokens löschen
        userTokenRepository.deleteAllByBenutzer(managed);

        // Anschließend den Benutzer löschen (Children wie Vereinsmitgliedschaften/Schiessnachweise werden per Cascade entfernt)
        benutzerRepository.delete(managed);
    }

    /**
     * Löscht einen Benutzer anhand der ID. Diese Variante vermeidet das Übergeben von ggf. detached Entities
     * und löscht zuerst alle zugehörigen Tokens per Benutzer-ID.
     *
     * @param benutzerId ID des zu löschenden Benutzers
     */
    public void loescheBenutzerById(Long benutzerId) {
        if (benutzerId == null) {
            return;
        }

        if (!benutzerRepository.existsById(benutzerId)) {
            throw new IllegalArgumentException("Benutzer nicht gefunden");
        }

        // Zuerst alle zugehörigen Zertifikate per Benutzer-ID löschen (vermeidet FK-Constraint)
        digitalesZertifikatRepository.deleteAllByBenutzerId(benutzerId);

        // Tokens per ID löschen (vermeidet das Laden einer transient/ detached Benutzer-Instanz)
        userTokenRepository.deleteAllByBenutzerId(benutzerId);

        // Benutzer direkt per Id löschen
        benutzerRepository.deleteById(benutzerId);
    }

    /**
     * Erstellt einen Token für die E-Mail-Bestätigung.
     */
    public String erstelleVerifizierungsToken(Benutzer benutzer) {
        return createToken(benutzer, UserTokenTyp.VERIFICATION, Duration.ofDays(1));
    }

    /**
     * Bestätigt die E-Mail-Adresse anhand des Tokens.
     */
    public boolean bestaetigeEmail(String token) {
        return validateAndProcessToken(token, UserTokenTyp.VERIFICATION, benutzer -> {
            benutzer.setEmailVerifiziert(true);
            benutzerRepository.save(benutzer);
        });
    }

    /**
     * Erstellt einen Token für Passwort-Reset.
     */
    public String erstellePasswortResetToken(Benutzer benutzer) {
        return createToken(benutzer, UserTokenTyp.PASSWORD_RESET, Duration.ofHours(2));
    }

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse
     * @return Benutzer oder null
     */
    @Transactional(readOnly = true)
    public Benutzer findeBenutzerByEmail(String email) {
        return benutzerRepository.findByEmail(email).orElse(null);
    }

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse mit geladenen Mitgliedschaften.
     *
     * @param email Die E-Mail-Adresse
     * @return Benutzer oder null
     */
    @Transactional(readOnly = true)
    public Benutzer findeBenutzerByEmailWithMitgliedschaften(String email) {
        return benutzerRepository.findByEmailWithMitgliedschaften(email).orElse(null);
    }

    /**
     * Setzt das Passwort anhand eines gültigen Reset-Tokens zurück.
     */
    public boolean resetPasswortMitToken(String token, String neuesPasswort) {
        return validateAndProcessToken(token, UserTokenTyp.PASSWORD_RESET, benutzer -> {
            benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
            benutzerRepository.save(benutzer);
        });
    }

    // ==================== Private Helper-Methoden ====================

    /**
     * Erstellt einen Token mit gegebenem Typ und Gültigkeitsdauer.
     * Reduziert Code-Duplikation bei Token-Erstellung.
     *
     * @param benutzer Der Benutzer für den Token
     * @param typ Der Token-Typ (VERIFICATION oder PASSWORD_RESET)
     * @param validity Die Gültigkeitsdauer des Tokens
     * @return Der generierte Token-String
     */
    private String createToken(Benutzer benutzer, UserTokenTyp typ, Duration validity) {
        String token = java.util.UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plus(validity);
        // Falls bereits ein Token für diesen Benutzer existiert (Unique-Constraint auf benutzer_id),
        // dann löschen wir es zuerst, bevor wir einen neuen Token anlegen. Das verhindert
        // SQL-Fehler beim Insert (duplicate key on benutzer_id).
        if (benutzer != null && benutzer.getId() != null) {
            userTokenRepository.deleteAllByBenutzerId(benutzer.getId());
        } else if (benutzer != null) {
            userTokenRepository.deleteAllByBenutzer(benutzer);
        }

        UserToken userToken = new UserToken(token, expiryDate, typ, benutzer);
        userTokenRepository.save(userToken);
        return token;
    }

    /**
     * Validiert einen Token und führt bei Erfolg die gegebene Aktion aus.
     * Reduziert Code-Duplikation bei Token-Validierung.
     *
     * @param token Der zu validierende Token-String
     * @param expectedType Der erwartete Token-Typ
     * @param onSuccess Die auszuführende Aktion bei erfolgreichem Token
     * @return true wenn Token gültig war und Aktion ausgeführt wurde, sonst false
     */
    private boolean validateAndProcessToken(String token, UserTokenTyp expectedType, Consumer<Benutzer> onSuccess) {
        UserToken userToken = userTokenRepository.findByToken(token).orElse(null);

        if (userToken == null || userToken.getTyp() != expectedType) {
            return false;
        }

        // Token abgelaufen?
        if (userToken.getAblaufdatum().isBefore(LocalDateTime.now())) {
            userTokenRepository.delete(userToken);
            return false;
        }

        // Token gültig - Aktion ausführen
        Benutzer benutzer = userToken.getBenutzer();
        onSuccess.accept(benutzer);
        userTokenRepository.delete(userToken);
        return true;
    }
}
