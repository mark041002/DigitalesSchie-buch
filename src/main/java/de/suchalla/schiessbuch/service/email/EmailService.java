package de.suchalla.schiessbuch.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${email.enabled}")
    private boolean emailEnabled;
    @Value("${spring.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMail(String to, String subject, String templateName, Map<String, Object> variables) {
        if (!emailEnabled) {
            log.info("E-Mail-Versand deaktiviert. E-Mail an {} würde nicht gesendet.", to);
            return;
        }
        try {
            log.info("Sende E-Mail an {} mit Betreff: {}", to, subject);
            String body = EmailTemplateLoader.loadTemplate(templateName, variables);
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
}

