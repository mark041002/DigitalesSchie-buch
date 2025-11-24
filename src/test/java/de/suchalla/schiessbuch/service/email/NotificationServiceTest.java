package de.suchalla.schiessbuch.service.email;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private jakarta.mail.internet.MimeMessage mimeMessage; // placeholder to satisfy injection if needed

    @Mock
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Mock
    private VereinsmitgliedschaftRepository mitgliedschaftRepository;

    private EmailService emailService;

    private EmailService spyService;

    private Benutzer schuetze;
    private Benutzer chef;
    private Verein verein;
    private Schiesstand schiesstand;
    private SchiessnachweisEintrag eintrag;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, mitgliedschaftRepository);
        spyService = org.mockito.Mockito.spy(emailService);
        ReflectionTestUtils.setField(spyService, "configuredBaseUrl", "http://localhost:8000");

        schuetze = TestDataFactory.createBenutzer(1L, "schuetze@example.com");
        chef = TestDataFactory.createBenutzer(2L, "chef@example.com");
        verein = TestDataFactory.createVerein(1L, "Verein A");
        schiesstand = TestDataFactory.createSchiesstand(1L, "Stand 1", verein);

        Verband verband = TestDataFactory.createVerband(1L, "DSB");
        Disziplin disziplin = TestDataFactory.createDisziplin(1L, "Luftgewehr", verband);
        eintrag = TestDataFactory.createEintrag(1L, schuetze, disziplin, schiesstand, LocalDate.now());
    }

    @Test
    void testNotifySignatureRequest() {
        Vereinsmitgliedschaft chefMitgliedschaft = TestDataFactory.createMitgliedschaft(1L, chef, verein, MitgliedschaftsStatus.AKTIV);
        chefMitgliedschaft.setIstVereinschef(true);

        when(mitgliedschaftRepository.findByVereinAndIstVereinschef(verein, true)).thenReturn(Arrays.asList(chefMitgliedschaft));
        when(mitgliedschaftRepository.findByVereinAndIstAufseher(verein, true)).thenReturn(Arrays.asList());
        org.mockito.Mockito.doNothing().when(spyService).sendMail(anyString(), anyString(), anyString(), anyMap());

        spyService.notifySignatureRequest(eintrag);

        org.mockito.Mockito.verify(spyService, times(1)).sendMail(eq("chef@example.com"), anyString(), anyString(), anyMap());
    }

    @Test
    void testNotifySignatureRequestOhneVerein() {
        eintrag.setSchiesstand(null);

        spyService.notifySignatureRequest(eintrag);

        org.mockito.Mockito.verify(spyService, never()).sendMail(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void testNotifyMembershipRequest() {
        Vereinsmitgliedschaft chefMitgliedschaft = TestDataFactory.createMitgliedschaft(1L, chef, verein, MitgliedschaftsStatus.AKTIV);
        chefMitgliedschaft.setIstVereinschef(true);

        when(mitgliedschaftRepository.findByVereinAndIstVereinschef(verein, true)).thenReturn(Arrays.asList(chefMitgliedschaft));
        org.mockito.Mockito.doNothing().when(spyService).sendMail(anyString(), anyString(), anyString(), anyMap());

        spyService.notifyMembershipRequest(verein, schuetze);

        org.mockito.Mockito.verify(spyService, times(1)).sendMail(eq("chef@example.com"), anyString(), anyString(), anyMap());
    }

    @Test
    void testNotifyEntrySigned() {
        schuetze.setEmailNotificationsEnabled(true);
        org.mockito.Mockito.doNothing().when(spyService).sendMail(anyString(), anyString(), anyString(), anyMap());

        spyService.notifyEntrySigned(eintrag);

        org.mockito.Mockito.verify(spyService, times(1)).sendMail(eq("schuetze@example.com"), anyString(), anyString(), anyMap());
    }

    @Test
    void testNotifyEntrySignedNotificationsDisabled() {
        schuetze.setEmailNotificationsEnabled(false);

        spyService.notifyEntrySigned(eintrag);

        org.mockito.Mockito.verify(spyService, never()).sendMail(anyString(), anyString(), anyString(), anyMap());
    }
}

