package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.SchiesstandRepository;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchiesstandServiceTest {

    @Mock
    private SchiesstandRepository schiesstandRepository;

    @InjectMocks
    private SchiesstandService service;

    private Schiesstand schiesstand;
    private Verein verein;

    @BeforeEach
    void setUp() {
        verein = TestDataFactory.createVerein(1L, "Verein A");
        schiesstand = TestDataFactory.createSchiesstand(1L, "Stand 1", verein);
    }

    @Test
    void testErstelleSchiesstand() {
        when(schiesstandRepository.save(any(Schiesstand.class))).thenReturn(schiesstand);

        service.erstelleSchiesstand(schiesstand);

        verify(schiesstandRepository).save(schiesstand);
    }

    @Test
    void testFindeSchiesstand() {
        when(schiesstandRepository.findById(1L)).thenReturn(Optional.of(schiesstand));

        Optional<Schiesstand> result = service.findeSchiesstand(1L);

        assertTrue(result.isPresent());
        assertEquals(schiesstand, result.get());
    }

    @Test
    void testFindeAlleSchiesstaendeEntities() {
        List<Schiesstand> staende = Arrays.asList(schiesstand);
        when(schiesstandRepository.findAllWithVerein()).thenReturn(staende);

        List<Schiesstand> result = service.findeAlleSchiesstaendeEntities();

        assertEquals(1, result.size());
        verify(schiesstandRepository).findAllWithVerein();
    }

    @Test
    void testAktualisiereSchiesstand() {
        when(schiesstandRepository.save(any(Schiesstand.class))).thenReturn(schiesstand);

        service.aktualisiereSchiesstand(schiesstand);

        verify(schiesstandRepository).save(schiesstand);
    }

    @Test
    void testLoescheSchiesstandOhneEintraege() {
        schiesstand.setEintraege(new HashSet<>());
        when(schiesstandRepository.findById(1L)).thenReturn(Optional.of(schiesstand));
        doNothing().when(schiesstandRepository).delete(schiesstand);

        service.loescheSchiesstand(1L);

        verify(schiesstandRepository).delete(schiesstand);
    }

    @Test
    void testLoescheSchiesstandMitEintraegenWirftException() {
        schiesstand.setEintraege(new HashSet<>(Arrays.asList(new de.suchalla.schiessbuch.model.entity.SchiessnachweisEintrag())));
        when(schiesstandRepository.findById(1L)).thenReturn(Optional.of(schiesstand));

        assertThrows(IllegalStateException.class, () -> {
            service.loescheSchiesstand(1L);
        });

        verify(schiesstandRepository, never()).delete(any());
    }

    @Test
    void testLoescheSchiesstandNichtGefunden() {
        when(schiesstandRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            service.loescheSchiesstand(999L);
        });
    }
}

