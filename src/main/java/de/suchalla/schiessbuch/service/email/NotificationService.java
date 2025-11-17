package de.suchalla.schiessbuch.service.email;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service für kontextbezogene Benachrichtigungen per E-Mail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final VereinsmitgliedschaftRepository mitgliedschaftRepository;

    @Value("${mail.base.url:http://localhost:8000}")
    private String baseUrl;

    /**
     * Notifiziert Vereinschefs und Aufseher, dass ein Eintrag zur Signatur offen ist.
     * In realer Anwendung sollten hier die Rollen/Verknüpfung geprüft werden (Vereinschef-Rolle usw.).
     */
    public void notifySignatureRequest(SchiessnachweisEintrag eintrag) {
        Verein verein = eintrag.getSchiesstand() != null ? eintrag.getSchiesstand().getVerein() : null;
        if (verein == null) {
            log.warn("Kein Verein für Eintrag {} gefunden, überspringe Signatur-Benachrichtigung", eintrag.getId());
            return;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("username", "Empfänger");
        vars.put("entryId", eintrag.getId());
        // Ziel-URL: Eintragsverwaltung, optionaler Schiesstand-Parameter
        String actionUrl = baseUrl + "/eintraege-verwaltung";
        if (eintrag.getSchiesstand() != null && eintrag.getSchiesstand().getId() != null) {
            actionUrl += "?schiesstandId=" + eintrag.getSchiesstand().getId();
        }
        vars.put("actionUrl", actionUrl);

        // Verhindere doppelte Mails: gleiche Benutzer könnte sowohl Vereinschef als auch Aufseher sein
        java.util.Set<Long> sentUserIds = new java.util.HashSet<>();
        java.util.Set<String> sentEmails = new java.util.HashSet<>();

        // Vereinschefs
        List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> chefs =
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
                emailService.sendMail(email, "Digitales Schießbuch - Eintrag zur Signatur", "signature-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }

        // Aufseher
        List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> aufseher =
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
                emailService.sendMail(email, "Digitales Schießbuch - Eintrag zur Signatur", "signature-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }
    }

    /**
     * Notifiziert Vereinschef, wenn eine Beitrittsanfrage für den Verein eingeht.
     */
    public void notifyMembershipRequest(Verein verein, Benutzer antragsteller) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("username", "Empfänger");
        vars.put("applicantName", antragsteller.getVollstaendigerName());
        vars.put("vereinName", verein.getName());
        // Ziel-URL: Vereinsdetails für den entsprechenden Verein (vereinId nur anhängen, wenn vorhanden)
        String actionUrlMembership = baseUrl + "/verein-details";
        if (verein != null && verein.getId() != null) {
            actionUrlMembership += "?vereinId=" + verein.getId();
        }
        vars.put("actionUrl", actionUrlMembership);

        // Verhindere doppelte Mails
        java.util.Set<Long> sentUserIds = new java.util.HashSet<>();
        java.util.Set<String> sentEmails = new java.util.HashSet<>();

        List<de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft> chefs =
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
                emailService.sendMail(email, "Digitales Schießbuch - Neue Beitrittsanfrage", "membership-request.html", vars);
                if (uid != null) sentUserIds.add(uid);
                sentEmails.add(email);
            }
        }
    }

    /**
     * Notifiziert den Schützen, dass sein Eintrag signiert wurde.
     */
    public void notifyEntrySigned(SchiessnachweisEintrag eintrag) {
        Benutzer schuetze = eintrag.getSchuetze();
        if (schuetze != null && schuetze.isEmailNotificationsEnabled()) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("username", schuetze.getVollstaendigerName());
            vars.put("entryId", eintrag.getId());
            // Ziel-URL: Meine Einträge
            vars.put("actionUrl", baseUrl + "/meine-eintraege");
            emailService.sendMail(schuetze.getEmail(), "Digitales Schießbuch - Eintrag signiert", "entry-signed.html", vars);
        }
    }
}
