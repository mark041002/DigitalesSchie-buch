package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.repository.VereinsmitgliedschaftRepository;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VereinsmitgliedschaftServiceTest {

    @Mock
    private VereinsmitgliedschaftRepository mitgliedschaftRepository;

    @Mock
    private VereinRepository vereinRepository;

    @Mock
    private DigitalesZertifikatRepository zertifikatRepository;

    @Mock
    private de.suchalla.schiessbuch.repository.BenutzerRepository benutzerRepository;

    @Mock
    private EmailService notificationService;

    @InjectMocks
    private VereinsmitgliedschaftService service;

    private Benutzer benutzer;
    private Verein verein;
    private Vereinsmitgliedschaft mitgliedschaft;

    @BeforeEach
    void setUp() {
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
        verein = TestDataFactory.createVerein(1L, "Verein A");
        mitgliedschaft = TestDataFactory.createMitgliedschaft(1L, benutzer, verein, MitgliedschaftsStatus.BEANTRAGT);
    }

    @Test
    void testBeantragenMitgliedschaft() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(verein));
        when(mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein)).thenReturn(Arrays.asList());
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);
        doNothing().when(notificationService).notifyMembershipRequest(verein, benutzer);

        Vereinsmitgliedschaft result = service.beantragenMitgliedschaft(benutzer, 1L);

        assertNotNull(result);
        verify(mitgliedschaftRepository).save(any(Vereinsmitgliedschaft.class));
        verify(notificationService).notifyMembershipRequest(verein, benutzer);
    }

    @Test
    void testBeantragenMitgliedschaftBereitsAktiv() {
        Vereinsmitgliedschaft aktiv = TestDataFactory.createMitgliedschaft(2L, benutzer, verein, MitgliedschaftsStatus.AKTIV);

        when(vereinRepository.findById(1L)).thenReturn(Optional.of(verein));
        when(mitgliedschaftRepository.findAllByBenutzerAndVerein(benutzer, verein)).thenReturn(Arrays.asList(aktiv));

        assertThrows(IllegalArgumentException.class, () -> {
            service.beantragenMitgliedschaft(benutzer, 1L);
        });

        verify(mitgliedschaftRepository, never()).save(any());
    }

    @Test
    void testGenehmigeAnfrage() {
        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);

        service.genehmigeAnfrage(1L);

        verify(mitgliedschaftRepository).save(mitgliedschaft);
        assertEquals(MitgliedschaftsStatus.AKTIV, mitgliedschaft.getStatus());
        assertTrue(mitgliedschaft.getAktiv());
    }

    @Test
    void testLehneAnfrageAb() {
        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);

        service.lehneAnfrageAb(1L);

        verify(mitgliedschaftRepository).save(mitgliedschaft);
        assertEquals(MitgliedschaftsStatus.ABGELEHNT, mitgliedschaft.getStatus());
        assertFalse(mitgliedschaft.getAktiv());
    }

    @Test
    void testLehneAnfrageAbMitGrund() {
        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);

        service.lehneAnfrageAbMitGrund(1L, "Fehlende Dokumente");

        verify(mitgliedschaftRepository).save(mitgliedschaft);
        assertEquals(MitgliedschaftsStatus.ABGELEHNT, mitgliedschaft.getStatus());
        assertEquals("Fehlende Dokumente", mitgliedschaft.getAblehnungsgrund());
    }

    @Test
    void testVereinVerlassen() {
        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);

        service.vereinVerlassen(1L);

        verify(mitgliedschaftRepository).save(mitgliedschaft);
        assertEquals(MitgliedschaftsStatus.VERLASSEN, mitgliedschaft.getStatus());
        assertFalse(mitgliedschaft.getAktiv());
        assertNotNull(mitgliedschaft.getAustrittDatum());
    }

    @Test
    void testLoescheMitgliedschaft() {
        mitgliedschaft.setStatus(MitgliedschaftsStatus.VERLASSEN);

        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        doNothing().when(mitgliedschaftRepository).deleteById(1L);

        service.loescheMitgliedschaft(1L);

        verify(mitgliedschaftRepository).deleteById(1L);
    }

    @Test
    void testLoescheMitgliedschaftAktivWirftException() {
        mitgliedschaft.setStatus(MitgliedschaftsStatus.AKTIV);

        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));

        assertThrows(IllegalArgumentException.class, () -> {
            service.loescheMitgliedschaft(1L);
        });

        verify(mitgliedschaftRepository, never()).deleteById(any());
    }

    @Test
    void testSetzeAufseherStatus() {
        mitgliedschaft.setStatus(MitgliedschaftsStatus.AKTIV);
        mitgliedschaft.setAktiv(true);

        // Mock-Zertifikat erstellen, damit kein neues erstellt werden muss
        DigitalesZertifikat mockZertifikat = DigitalesZertifikat.builder()
                .id(1L)
                .seriennummer("test-123")
                .build();

        when(mitgliedschaftRepository.findById(1L)).thenReturn(Optional.of(mitgliedschaft));
        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(benutzer));
        when(zertifikatRepository.findByBenutzer(benutzer)).thenReturn(Optional.of(mockZertifikat));
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(mitgliedschaft);

        service.setzeAufseherStatus(1L, true);

        verify(mitgliedschaftRepository).save(mitgliedschaft);
        assertTrue(mitgliedschaft.getIstAufseher());
    }

    @Test
    void testFindeBeitrittsanfragen() {
        List<Vereinsmitgliedschaft> entities = Arrays.asList(mitgliedschaft);

        when(mitgliedschaftRepository.findByVereinAndStatus(verein, MitgliedschaftsStatus.BEANTRAGT)).thenReturn(entities);

        List<Vereinsmitgliedschaft> result = service.findeBeitrittsanfragen(verein);

        assertEquals(1, result.size());
        verify(mitgliedschaftRepository).findByVereinAndStatus(verein, MitgliedschaftsStatus.BEANTRAGT);
    }
}

