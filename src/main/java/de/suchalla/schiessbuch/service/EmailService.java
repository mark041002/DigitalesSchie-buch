package de.suchalla.schiessbuch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service für E-Mail-Versand.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sendet eine Passwort-Reset-E-Mail.
     *
     * @param empfaengerEmail Die E-Mail-Adresse des Empfängers
     * @param resetToken Das Reset-Token
     */
    public void sendePasswortResetEmail(String empfaengerEmail, String resetToken) {
        try {
            SimpleMailMessage nachricht = new SimpleMailMessage();
            nachricht.setTo(empfaengerEmail);
            nachricht.setSubject("Passwort zurücksetzen - Digitales Schießbuch");
            nachricht.setText("Bitte verwenden Sie folgenden Link, um Ihr Passwort zurückzusetzen:\n\n" +
                    "http://localhost:8000/passwort-zuruecksetzen?token=" + resetToken + "\n\n" +
                    "Dieser Link ist 24 Stunden gültig.\n\n" +
                    "Falls Sie diese E-Mail nicht angefordert haben, ignorieren Sie sie bitte.");

            mailSender.send(nachricht);
            log.info("Passwort-Reset-E-Mail an {} gesendet", empfaengerEmail);
        } catch (Exception e) {
            log.error("Fehler beim Senden der E-Mail an {}: {}", empfaengerEmail, e.getMessage());
        }
    }

    /**
     * Sendet eine allgemeine Benachrichtigungs-E-Mail.
     *
     * @param empfaengerEmail Die E-Mail-Adresse des Empfängers
     * @param betreff Der E-Mail-Betreff
     * @param nachrichtenText Der Nachrichtentext
     */
    public void sendeBenachrichtigungsEmail(String empfaengerEmail, String betreff, String nachrichtenText) {
        try {
            SimpleMailMessage nachricht = new SimpleMailMessage();
            nachricht.setTo(empfaengerEmail);
            nachricht.setSubject(betreff);
            nachricht.setText(nachrichtenText);

            mailSender.send(nachricht);
            log.info("Benachrichtigungs-E-Mail an {} gesendet", empfaengerEmail);
        } catch (Exception e) {
            log.error("Fehler beim Senden der E-Mail an {}: {}", empfaengerEmail, e.getMessage());
        }
    }
}

