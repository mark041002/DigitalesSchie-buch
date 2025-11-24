package de.suchalla.schiessbuch.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final VereinsmitgliedschaftRepository mitgliedschaftRepository;
    @Value("${email.enabled}")
    private boolean emailEnabled;
    @Value("${spring.mail.from}")
    private String from;
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Konstruktor für den E-Mail-Service.
     *
     * @param mailSender Mail-Sender zum Versenden von MIME-Nachrichten
     * @param mitgliedschaftRepository Repository zur Abfrage von Vereinsmitgliedschaften (für Benachrichtigungen)
     */
    public EmailService(JavaMailSender mailSender, VereinsmitgliedschaftRepository mitgliedschaftRepository) {
        this.mailSender = mailSender;
        this.mitgliedschaftRepository = mitgliedschaftRepository;
    }

    @jakarta.annotation.PostConstruct
    public void logConfiguration() {
        log.info("EmailService konfiguriert - Base-URL: {}, E-Mail aktiviert: {}, Von-Adresse: {}", baseUrl, emailEnabled, from);
    }

    /**
     * Sendet eine HTML-E-Mail basierend auf einem Template.
     *
     * @param to Empfänger-Adresse
     * @param subject Betreff der E-Mail
     * @param templateName Dateiname des HTML-Templates in `resources/templates`
     * @param variables Map mit Template-Variablen (Platzhalter im Format `{{name}}`)
     */
    public void sendMail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("E-Mail-Versand deaktiviert. E-Mail an {} würde nicht gesendet.", to);
            return;
        }
        try {
            log.info("Sende E-Mail an {} mit Betreff: {}", to, subject);
            String body = renderTemplate(templateName, variables);
            log.debug("E-Mail-Body geladen: {}", body);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(from);
            mailSender.send(message);
            log.info("E-Mail erfolgreich an {} gesendet", to);
        } catch (Exception e) {
            log.error("Fehler beim E-Mail-Versand an {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Ermittelt die Basis-URL, die in Links innerhalb von E-Mails verwendet wird.
     *
     * @return Basis-URL als String
     */
    private String resolveBaseUrl() {
        return baseUrl;
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        try (InputStream is = EmailService.class.getResourceAsStream("/templates/" + templateName)) {
            if (is == null) {
                log.error("Template nicht gefunden: /templates/{}", templateName);
                return "";
            }
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                String template = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                // Wenn der Aufrufer nur ein Token übergibt, erzeugen wir hier die benötigten Links
                if (variables != null) {
                    // Verifizierungs-E-Mail: baue verificationLink aus token
                    // Erkennen sowohl von Templates mit "verify" als auch "verification"
                    String tn = templateName.toLowerCase();
                    if (!variables.containsKey("verificationLink") && variables.containsKey("token") && tn.contains("verif")) {
                        String token = variables.get("token").toString();
                        variables.put("verificationLink", resolveBaseUrl() + "/email-verifizieren?token=" + token);
                    }
                    // Passwort-Reset: baue resetLink aus token
                    if (!variables.containsKey("resetLink") && variables.containsKey("token") && templateName.toLowerCase().contains("password")) {
                        String token = variables.get("token").toString();
                        variables.put("resetLink", resolveBaseUrl() + "/passwort-reset?token=" + token);
                    }

                    // Ersetze alle Platzhalter im Template
                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
                        if (entry.getValue() != null) {
                            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
                        }
                    }
                }
                return template;
            }
        } catch (Exception e) {
            log.error("Fehler beim Laden des Templates {}: {}", templateName, e.getMessage(), e);
            return "";
        }
    }

    @Async
    public void notifySignatureRequest(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag) {
        de.suchalla.schiessbuch.model.entity.Verein verein = eintrag.getSchiesstand() != null ? eintrag.getSchiesstand().getVerein() : null;
        if (verein == null) {
            log.warn("Kein Verein für Eintrag {} gefunden, überspringe Signatur-Benachrichtigung", eintrag.getId());
            return;
        }

        Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("username", "Empfänger");
        vars.put("entryId", eintrag.getId());
        String actionUrl = resolveBaseUrl() + "/eintraege-verwaltung";
        if (eintrag.getSchiesstand() != null && eintrag.getSchiesstand().getId() != null) {
            actionUrl += "?schiesstandId=" + eintrag.getSchiesstand().getId();
        }
        vars.put("actionUrl", actionUrl);

        java.util.Set<Long> sentUserIds = new java.util.HashSet<>();
        java.util.Set<String> sentEmails = new java.util.HashSet<>();

        java.util.List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> chefs =
                mitgliedschaftRepository.findByVereinAndIstVereinschef(verein, true);
        for (var m : chefs) {
            var b = m.getBenutzer();
            if (b == null) continue;
            Long uid = b.getId();
            String email = b.getEmail();
            if ((uid != null && sentUserIds.contains(uid)) || (email != null && sentEmails.contains(email))) continue;
            if (email == null || email.isBlank()) {
                log.warn("Benutzer {} hat keine E-Mail-Adresse, überspringe Benachrichtigung", b);
                continue;
            }
            if (Boolean.TRUE.equals(b.isEmailNotificationsEnabled())) {
                this.sendMail(email, "Digitales Schießbuch - Eintrag zur Signatur", "signature-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }

        java.util.List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> aufseher =
                mitgliedschaftRepository.findByVereinAndIstAufseher(verein, true);
        for (var m : aufseher) {
            var b = m.getBenutzer();
            if (b == null) continue;
            Long uid = b.getId();
            String email = b.getEmail();
            if ((uid != null && sentUserIds.contains(uid)) || (email != null && sentEmails.contains(email))) continue;
            if (email == null || email.isBlank()) {
                log.warn("Benutzer {} hat keine E-Mail-Adresse, überspringe Benachrichtigung", b);
                continue;
            }
            if (Boolean.TRUE.equals(b.isEmailNotificationsEnabled())) {
                this.sendMail(email, "Digitales Schießbuch - Eintrag zur Signatur", "signature-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }
    }

    @Async
    public void notifyMembershipRequest(de.suchalla.schiessbuch.model.entity.Verein verein, de.suchalla.schiessbuch.model.entity.Benutzer antragsteller) {
        Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("username", "Empfänger");
        vars.put("applicantName", antragsteller.getVollstaendigerName());
        vars.put("vereinName", verein.getName());
        String actionUrlMembership = resolveBaseUrl() + "/verein-details";
        if (verein != null && verein.getId() != null) {
            actionUrlMembership += "?vereinId=" + verein.getId();
        }
        vars.put("actionUrl", actionUrlMembership);

        java.util.Set<Long> sentUserIds = new java.util.HashSet<>();
        java.util.Set<String> sentEmails = new java.util.HashSet<>();

        java.util.List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> chefs =
                mitgliedschaftRepository.findByVereinAndIstVereinschef(verein, true);
        for (var m : chefs) {
            var b = m.getBenutzer();
            if (b == null) continue;
            Long uid = b.getId();
            String email = b.getEmail();
            if ((uid != null && sentUserIds.contains(uid)) || (email != null && sentEmails.contains(email))) continue;
            if (email == null || email.isBlank()) {
                log.warn("Benutzer {} hat keine E-Mail-Adresse, überspringe Benachrichtigung", b);
                continue;
            }
            if (Boolean.TRUE.equals(b.isEmailNotificationsEnabled())) {
                this.sendMail(email, "Digitales Schießbuch - Neue Beitrittsanfrage", "membership-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }
    }

    @Async
    public void notifyEntrySigned(de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag) {
        de.suchalla.schiessbuch.model.entity.Benutzer schuetze = eintrag.getSchuetze();
        if (schuetze != null && schuetze.isEmailNotificationsEnabled()) {
            Map<String, Object> vars = new java.util.HashMap<>();
            vars.put("username", schuetze.getVollstaendigerName());
            vars.put("entryId", eintrag.getId());
            vars.put("actionUrl", resolveBaseUrl() + "/meine-eintraege");
            this.sendMail(schuetze.getEmail(), "Digitales Schießbuch - Eintrag signiert", "entry-signed.html", vars);
        }
    }  
}

