package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.repository.DisziplinRepository;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für DisziplinService mit Mockito.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DisziplinServiceTest {

    @Mock
    private DisziplinRepository disziplinRepository;

    @Mock
    private SchiesstandRepository schiesstandRepository;

    @Mock
    private SchiessnachweisEintragRepository eintragRepository;

    @InjectMocks
    private DisziplinService disziplinService;

    private Disziplin testDisziplin;
    private Schiesstand testSchiesstand;

    @BeforeEach
    void setUp() {
        testDisziplin = Disziplin.builder()
                .id(1L)
            .kennziffer("LG-10m")
            .programm("Luftgewehr 10m")
                .build();

        testSchiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .build();
    }

    @Test
    void testErstelleDisziplin() {
        when(disziplinRepository.save(any(Disziplin.class))).thenReturn(testDisziplin);

        disziplinService.erstelleDisziplin(testDisziplin);

        verify(disziplinRepository).save(testDisziplin);
    }

    @Test
    void testFindeDisziplinenVonVerband() {
        Long verbandId = 1L;
        List<Disziplin> disziplinen = Arrays.asList(testDisziplin);

        when(disziplinRepository.findByVerbandIdAndArchiviert(verbandId, false)).thenReturn(disziplinen);

        List<Disziplin> result = disziplinService.findeDisziplinenVonVerband(verbandId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(disziplinRepository).findByVerbandIdAndArchiviert(verbandId, false);
    }

    @Test
    void testLoescheDisziplin() {
        Long disziplinId = 1L;
        when(disziplinRepository.findById(disziplinId)).thenReturn(Optional.of(testDisziplin));
        when(disziplinRepository.save(any(Disziplin.class))).thenReturn(testDisziplin);

        disziplinService.loescheDisziplin(disziplinId);

        verify(disziplinRepository).findById(disziplinId);
        verify(disziplinRepository).save(testDisziplin);
        assertTrue(testDisziplin.getArchiviert());
    }

    @Test
    void testErstelleSchiesstand() {
        when(schiesstandRepository.save(any(Schiesstand.class))).thenReturn(testSchiesstand);

        disziplinService.erstelleSchiesstand(testSchiesstand);

        verify(schiesstandRepository).save(testSchiesstand);
    }

    @Test
    void testFindeSchiesstand() {
        Long schiesstandId = 1L;
        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.of(testSchiesstand));

        Schiesstand result = disziplinService.findeSchiesstand(schiesstandId);

        assertNotNull(result);
        assertEquals(testSchiesstand, result);
        verify(schiesstandRepository).findById(schiesstandId);
    }

    @Test
    void testFindeAlleSchiesstaende() {
        List<Schiesstand> schiesstaende = Arrays.asList(testSchiesstand);

        when(schiesstandRepository.findAllWithVerein()).thenReturn(schiesstaende);

        List<Schiesstand> result = disziplinService.findeAlleSchiesstaende();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(schiesstandRepository).findAllWithVerein();
    }

    @Test
    void testFindeDisziplinenVonVerbandEntities() {
        Long verbandId = 1L;
        List<Disziplin> disziplinen = Arrays.asList(testDisziplin);

        when(disziplinRepository.findByVerbandIdAndArchiviert(verbandId, false)).thenReturn(disziplinen);

        List<Disziplin> result = disziplinService.findeDisziplinenVonVerbandEntities(verbandId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDisziplin, result.get(0));
        verify(disziplinRepository).findByVerbandIdAndArchiviert(verbandId, false);
    }

    @Test
    void testFindeAlleSchiesstaendeEntities() {
        List<Schiesstand> schiesstaende = Arrays.asList(testSchiesstand);

        when(schiesstandRepository.findAllWithVerein()).thenReturn(schiesstaende);

        List<Schiesstand> result = disziplinService.findeAlleSchiesstaendeEntities();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchiesstand, result.get(0));
        verify(schiesstandRepository).findAllWithVerein();
    }

    @Test
    void testAktualisiereSchiesstand() {
        when(schiesstandRepository.save(any(Schiesstand.class))).thenReturn(testSchiesstand);

        disziplinService.aktualisiereSchiesstand(testSchiesstand);

        verify(schiesstandRepository).save(testSchiesstand);
    }

    @Test
    void testLoescheSchiesstand_Success() {
        Long schiesstandId = 1L;

        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.of(testSchiesstand));
        when(eintragRepository.findBySchiesstand(testSchiesstand)).thenReturn(Collections.emptyList());
        doNothing().when(schiesstandRepository).delete(testSchiesstand);

        disziplinService.loescheSchiesstand(schiesstandId);

        verify(schiesstandRepository).findById(schiesstandId);
        verify(eintragRepository).findBySchiesstand(testSchiesstand);
        verify(schiesstandRepository).delete(testSchiesstand);
    }

    @Test
    void testLoescheSchiesstand_NotFound() {
        Long schiesstandId = 999L;
        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            disziplinService.loescheSchiesstand(schiesstandId);
        });

        verify(schiesstandRepository).findById(schiesstandId);
        verify(schiesstandRepository, never()).delete(any());
    }

    @Test
    void testLoescheSchiesstand_HasEntries() {
        Long schiesstandId = 1L;
        de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag =
                new de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag();

        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.of(testSchiesstand));
        when(eintragRepository.findBySchiesstand(testSchiesstand)).thenReturn(Collections.singletonList(eintrag));

        assertThrows(IllegalStateException.class, () -> {
            disziplinService.loescheSchiesstand(schiesstandId);
        });

        verify(schiesstandRepository).findById(schiesstandId);
        verify(schiesstandRepository, never()).delete(any());
    }

    @Test
    void testFindeAlleDisziplinenVonVerband() {
        Long verbandId = 1L;
        Disziplin archiviert = Disziplin.builder()
                .id(2L)
                .kennziffer("LG-10m-alt")
                .archiviert(true)
                .build();
        List<Disziplin> disziplinen = Arrays.asList(testDisziplin, archiviert);

        when(disziplinRepository.findAllByVerbandId(verbandId)).thenReturn(disziplinen);

        List<Disziplin> result = disziplinService.findeAlleDisziplinenVonVerband(verbandId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(disziplinRepository).findAllByVerbandId(verbandId);
    }

    @Test
    void testAktiviereDisziplin() {
        Long disziplinId = 1L;
        testDisziplin.setArchiviert(true);

        when(disziplinRepository.findById(disziplinId)).thenReturn(Optional.of(testDisziplin));
        when(disziplinRepository.save(any(Disziplin.class))).thenReturn(testDisziplin);

        disziplinService.aktiviereDisziplin(disziplinId);

        verify(disziplinRepository).findById(disziplinId);
        verify(disziplinRepository).save(testDisziplin);
        assertFalse(testDisziplin.getArchiviert());
    }

    @Test
    void testZaehleEintraegeVonDisziplin() {
        Long disziplinId = 1L;

        when(disziplinRepository.findById(disziplinId)).thenReturn(Optional.of(testDisziplin));
        when(eintragRepository.countByDisziplin(testDisziplin)).thenReturn(5L);

        long anzahl = disziplinService.zaehleEintraegeVonDisziplin(disziplinId);

        assertEquals(5L, anzahl);
        verify(disziplinRepository).findById(disziplinId);
        verify(eintragRepository).countByDisziplin(testDisziplin);
    }

    @Test
    void testLoescheDisziplinMitEintraegen_Success() {
        Long disziplinId = 1L;
        testDisziplin.setArchiviert(true);

        de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag1 =
                new de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag();
        de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag eintrag2 =
                new de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag();
        List<de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag> eintraege =
                Arrays.asList(eintrag1, eintrag2);

        when(disziplinRepository.findById(disziplinId)).thenReturn(Optional.of(testDisziplin));
        when(eintragRepository.findByDisziplin(testDisziplin)).thenReturn(eintraege);
        doNothing().when(eintragRepository).deleteAll(eintraege);
        doNothing().when(disziplinRepository).delete(testDisziplin);

        disziplinService.loescheDisziplinMitEintraegen(disziplinId);

        verify(disziplinRepository).findById(disziplinId);
        verify(eintragRepository).findByDisziplin(testDisziplin);
        verify(eintragRepository).deleteAll(eintraege);
        verify(disziplinRepository).delete(testDisziplin);
    }

    @Test
    void testLoescheDisziplinMitEintraegen_NotArchived() {
        Long disziplinId = 1L;
        testDisziplin.setArchiviert(false);

        when(disziplinRepository.findById(disziplinId)).thenReturn(Optional.of(testDisziplin));

        assertThrows(IllegalStateException.class, () -> {
            disziplinService.loescheDisziplinMitEintraegen(disziplinId);
        });

        verify(disziplinRepository).findById(disziplinId);
        verify(eintragRepository, never()).findByDisziplin(any());
        verify(eintragRepository, never()).deleteAll(any());
        verify(disziplinRepository, never()).delete(any());
    }
}

