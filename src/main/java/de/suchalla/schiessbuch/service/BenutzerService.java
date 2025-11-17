package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.BenutzerMapper;
import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.UserToken;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        benutzerRepository.delete(benutzer);
    }

    /**
     * Erstellt einen Token für die E-Mail-Bestätigung.
     */
    public String erstelleVerifizierungsToken(Benutzer benutzer) {
        String token = java.util.UUID.randomUUID().toString();
        UserToken userToken = new UserToken(token, java.time.LocalDateTime.now().plusDays(1), UserTokenTyp.VERIFICATION, benutzer);
        userTokenRepository.save(userToken);
        return token;
    }

    /**
     * Bestätigt die E-Mail-Adresse anhand des Tokens.
     */
    public boolean bestaetigeEmail(String token) {
        UserToken userToken = userTokenRepository.findByToken(token).orElse(null);
        if (userToken != null && userToken.getTyp() == UserTokenTyp.VERIFICATION) {
            if (userToken.getAblaufdatum().isAfter(java.time.LocalDateTime.now())) {
                Benutzer benutzer = userToken.getBenutzer();
                benutzer.setEmailVerifiziert(true);
                userTokenRepository.delete(userToken);
                benutzerRepository.save(benutzer);
                return true;
            } else {
                userTokenRepository.delete(userToken);
            }
        }
        return false;
    }

    /**
     * Erstellt einen Token für Passwort-Reset.
     */
    public String erstellePasswortResetToken(Benutzer benutzer) {
        String token = java.util.UUID.randomUUID().toString();
        UserToken userToken = new UserToken(token, java.time.LocalDateTime.now().plusHours(2), UserTokenTyp.PASSWORD_RESET, benutzer);
        userTokenRepository.save(userToken);
        return token;
    }

    /**
     * Setzt das Passwort anhand eines gültigen Reset-Tokens zurück.
     */
    public boolean resetPasswortMitToken(String token, String neuesPasswort) {
        UserToken userToken = userTokenRepository.findByToken(token).orElse(null);
        if (userToken != null && userToken.getTyp() == UserTokenTyp.PASSWORD_RESET) {
            if (userToken.getAblaufdatum().isAfter(java.time.LocalDateTime.now())) {
                Benutzer benutzer = userToken.getBenutzer();
                benutzer.setPasswort(passwordEncoder.encode(neuesPasswort));
                userTokenRepository.delete(userToken);
                benutzerRepository.save(benutzer);
                return true;
            } else {
                userTokenRepository.delete(userToken);
            }
        }
        return false;
    }
}
