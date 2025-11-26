package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.repository.VereinRepository;
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
 * Unit-Tests für VereinService mit Mockito.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class VereinServiceTest {

    @Mock
    private VereinRepository vereinRepository;

    @InjectMocks
    private VereinService vereinService;

    private Verein testVerein;
    private Verband testVerband;

    @BeforeEach
    void setUp() {
        testVerband = Verband.builder()
                .id(1L)
                .name("DSB")
                .beschreibung("Beschreibung DSB")
                .build();

        testVerein = Verein.builder()
                .id(1L)
                .name("Schützenverein Teststadt")
                .adresse("Schützenstraße 1, 12345 Teststadt")
                .beschreibung("Traditionsreicher Verein")
                .verbaende(new HashSet<>(Arrays.asList(testVerband)))
                .build();
    }

    @Test
    void testAktualisiereVerein_Success() {
        Verein existierend = Verein.builder()
                .id(1L)
                .name("Alter Name")
                .adresse("Alte Adresse")
                .beschreibung("Alte Beschreibung")
                .verbaende(new HashSet<>())
                .build();

        when(vereinRepository.findById(1L)).thenReturn(Optional.of(existierend));
        when(vereinRepository.save(any(Verein.class))).thenReturn(existierend);

        vereinService.aktualisiereVerein(testVerein);

        verify(vereinRepository).findById(1L);
        verify(vereinRepository).save(existierend);

        // Prüfe, dass die Werte aktualisiert wurden
        assertEquals("Schützenverein Teststadt", existierend.getName());
        assertEquals("Schützenstraße 1, 12345 Teststadt", existierend.getAdresse());
        assertEquals("Traditionsreicher Verein", existierend.getBeschreibung());
        assertEquals(1, existierend.getVerbaende().size());
    }

    @Test
    void testAktualisiereVerein_NoId() {
        Verein vereinOhneId = Verein.builder()
                .name("Test")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            vereinService.aktualisiereVerein(vereinOhneId);
        });

        verify(vereinRepository, never()).save(any());
    }

    @Test
    void testAktualisiereVerein_NotFound() {
        when(vereinRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            vereinService.aktualisiereVerein(testVerein);
        });

        verify(vereinRepository).findById(1L);
        verify(vereinRepository, never()).save(any());
    }

    @Test
    void testAktualisiereVerein_ClearVerbaende() {
        Verein existierend = Verein.builder()
                .id(1L)
                .name("Test")
                .verbaende(new HashSet<>(Arrays.asList(testVerband)))
                .build();

        Verein updateVerein = Verein.builder()
                .id(1L)
                .name("Test Updated")
                .verbaende(new HashSet<>())
                .build();

        when(vereinRepository.findById(1L)).thenReturn(Optional.of(existierend));
        when(vereinRepository.save(any(Verein.class))).thenReturn(existierend);

        vereinService.aktualisiereVerein(updateVerein);

        verify(vereinRepository).findById(1L);
        verify(vereinRepository).save(existierend);
        assertTrue(existierend.getVerbaende().isEmpty());
    }

    @Test
    void testAktualisiereVerein_NullVerbaende() {
        Verein existierend = Verein.builder()
                .id(1L)
                .name("Test")
                .verbaende(new HashSet<>(Arrays.asList(testVerband)))
                .build();

        Verein updateVerein = Verein.builder()
                .id(1L)
                .name("Test Updated")
                .verbaende(null)
                .build();

        when(vereinRepository.findById(1L)).thenReturn(Optional.of(existierend));
        when(vereinRepository.save(any(Verein.class))).thenReturn(existierend);

        vereinService.aktualisiereVerein(updateVerein);

        verify(vereinRepository).findById(1L);
        verify(vereinRepository).save(existierend);
        assertTrue(existierend.getVerbaende().isEmpty());
    }

    @Test
    void testFindAllVereinsnamen() {
        List<String> vereinsnamen = Arrays.asList("Verein A", "Verein B", "Verein C");
        when(vereinRepository.findAllNames()).thenReturn(vereinsnamen);

        List<String> result = vereinService.findAllVereinsnamen();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Verein A"));
        assertTrue(result.contains("Verein B"));
        assertTrue(result.contains("Verein C"));
        verify(vereinRepository).findAllNames();
    }

    @Test
    void testFindAllVereinsnamen_Empty() {
        when(vereinRepository.findAllNames()).thenReturn(Arrays.asList());

        List<String> result = vereinService.findAllVereinsnamen();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(vereinRepository).findAllNames();
    }
}
