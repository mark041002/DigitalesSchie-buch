package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Disziplin;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.repository.DisziplinRepository;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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

        when(disziplinRepository.findByVerbandId(verbandId)).thenReturn(disziplinen);

        List<Disziplin> result = disziplinService.findeDisziplinenVonVerband(verbandId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(disziplinRepository).findByVerbandId(verbandId);
    }

    @Test
    void testLoescheDisziplin() {
        Long disziplinId = 1L;
        doNothing().when(disziplinRepository).deleteById(disziplinId);

        disziplinService.loescheDisziplin(disziplinId);

        verify(disziplinRepository).deleteById(disziplinId);
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

        Optional<Schiesstand> result = disziplinService.findeSchiesstand(schiesstandId);

        assertTrue(result.isPresent());
        assertEquals(testSchiesstand, result.get());
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

        when(disziplinRepository.findByVerbandId(verbandId)).thenReturn(disziplinen);

        List<Disziplin> result = disziplinService.findeDisziplinenVonVerbandEntities(verbandId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDisziplin, result.get(0));
        verify(disziplinRepository).findByVerbandId(verbandId);
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
        testSchiesstand.setEintraege(new HashSet<>());

        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.of(testSchiesstand));
        doNothing().when(schiesstandRepository).delete(testSchiesstand);

        disziplinService.loescheSchiesstand(schiesstandId);

        verify(schiesstandRepository).findById(schiesstandId);
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
        testSchiesstand.setEintraege(new HashSet<>(Arrays.asList(
                new de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag()
        )));

        when(schiesstandRepository.findById(schiesstandId)).thenReturn(Optional.of(testSchiesstand));

        assertThrows(IllegalStateException.class, () -> {
            disziplinService.loescheSchiesstand(schiesstandId);
        });

        verify(schiesstandRepository).findById(schiesstandId);
        verify(schiesstandRepository, never()).delete(any());
    }
}

