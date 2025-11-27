package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.repository.*;
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
class VerbandServiceTest {

    @Mock
    private VerbandRepository verbandRepository;

    @Mock
    private VereinRepository vereinRepository;

    @Mock
    private DigitalesZertifikatRepository zertifikatRepository;

    @Mock
    private VereinsmitgliedschaftRepository mitgliedschaftRepository;

    @Mock
    private DisziplinRepository disziplinRepository;

    @Mock
    private VereinsmitgliedschaftService vereinsmitgliedschaftService;

    @Mock

    @Mock

    @InjectMocks
    private VerbandService service;

    private Verband verband;
    private Verein verein;
    private Benutzer benutzer;

    @BeforeEach
    void setUp() {
        verband = TestDataFactory.createVerband(1L, "DSB");
        verein = TestDataFactory.createVerein(1L, "Verein A");
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
    }

    @Test
    void testErstelleVerband() {
        when(verbandRepository.existsByName("DSB")).thenReturn(false);
        when(verbandRepository.save(any(Verband.class))).thenReturn(verband);

        service.erstelleVerband(verband);

        verify(verbandRepository).existsByName("DSB");
        verify(verbandRepository).save(verband);
    }

    @Test
    void testErstelleVerbandNameExistiertBereits() {
        when(verbandRepository.existsByName("DSB")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            service.erstelleVerband(verband);
        });

        verify(verbandRepository, never()).save(any());
    }

    @Test
    void testFindeVerband() {
        when(verbandRepository.findById(1L)).thenReturn(Optional.of(verband));

        Verband result = service.findeVerband(1L);

        assertNotNull(result);
        assertEquals(verband, result);
    }

    @Test
    void testFindeAlleVerbaende() {
        List<Verband> verbaende = Arrays.asList(verband);
        List<Verband> dtos = Arrays.asList(Verband.builder().id(1L).name("DSB").build());

        when(verbandRepository.findAllWithVereine()).thenReturn(verbaende);
        when(verbandMapper.toDTOList(verbaende)).thenReturn(dtos);

        List<Verband> result = service.findeAlleVerbaende();

        assertEquals(1, result.size());
        verify(verbandRepository).findAllWithVereine();
    }

    @Test
    void testIstMitgliedImVerband() {
        when(vereinsmitgliedschaftService.findeVerbaendeVonBenutzer(benutzer))
                .thenReturn(Arrays.asList(Verband.builder().id(1L).build()));

        boolean result = service.istMitgliedImVerband(benutzer, 1L);

        assertTrue(result);
    }

    @Test
    void testIstMitgliedImVerbandFalse() {
        when(vereinsmitgliedschaftService.findeVerbaendeVonBenutzer(benutzer))
                .thenReturn(Arrays.asList(Verband.builder().id(2L).build()));

        boolean result = service.istMitgliedImVerband(benutzer, 1L);

        assertFalse(result);
    }

    @Test
    void testBeitretenZuVerband() {
        verband.getVereine().add(verein);
        Vereinsmitgliedschaft mitgliedschaft = TestDataFactory.createMitgliedschaft(1L, benutzer, verein, MitgliedschaftsStatus.BEANTRAGT);

        when(verbandRepository.findById(1L)).thenReturn(Optional.of(verband));
        when(vereinRepository.findByVerbaendeContaining(verband)).thenReturn(Arrays.asList(verein));
        when(vereinsmitgliedschaftService.vereinBeitreten(benutzer, verein)).thenReturn(mitgliedschaft);
        doNothing().when(vereinsmitgliedschaftService).genehmigeAnfrage(1L);

        service.beitretenZuVerband(benutzer, 1L);

        verify(vereinsmitgliedschaftService).vereinBeitreten(benutzer, verein);
        verify(vereinsmitgliedschaftService).genehmigeAnfrage(1L);
    }

    @Test
    void testLoescheVerband() {
        when(disziplinRepository.findByVerbandId(1L)).thenReturn(List.of());
        doNothing().when(verbandRepository).deleteById(1L);

        service.loescheVerband(1L);

        verify(verbandRepository).deleteById(1L);
    }

    @Test
    void testFindeVerein() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(verein));
        Verein result = service.findeVerein(1L);

        assertNotNull(result);
        assertEquals(verein, result);
    }

    @Test
    void testLoescheVereinMitAktivenMitgliedern() {
        Vereinsmitgliedschaft aktiveMitgliedschaft = TestDataFactory.createMitgliedschaft(1L, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        verein.getMitgliedschaften().add(aktiveMitgliedschaft);

        when(vereinRepository.findById(1L)).thenReturn(Optional.of(verein));
        when(zertifikatRepository.findByVerein(verein)).thenReturn(List.of());
        when(mitgliedschaftRepository.save(any(Vereinsmitgliedschaft.class))).thenReturn(aktiveMitgliedschaft);

        // Der Service markiert Mitgliedschaften als inaktiv und löscht dann den Verein
        service.loescheVerein(1L);

        verify(mitgliedschaftRepository).save(aktiveMitgliedschaft);
        verify(vereinRepository).delete(verein);
    }
}

