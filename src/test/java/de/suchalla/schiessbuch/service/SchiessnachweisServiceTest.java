package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.SchiessnachweisEintragMapper;
import de.suchalla.schiessbuch.model.dto.SchiessnachweisEintrag;
import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchiessnachweisServiceTest {

    @Mock
    private SchiessnachweisEintragRepository eintragRepository;

    @Mock
    private SchiessnachweisEintragMapper eintragMapper;

    @InjectMocks
    private SchiessnachweisService service;

    private Benutzer schuetze;
    private Schiesstand schiesstand;
    private Disziplin disziplin;
    private SchiessnachweisEintrag eintrag;

    @BeforeEach
    void setUp() {
        schuetze = TestDataFactory.createBenutzer(1L, "schuetze@example.com");
        Verein verein = TestDataFactory.createVerein(1L, "Verein A");
        schiesstand = TestDataFactory.createSchiesstand(1L, "Stand 1", verein);
        Verband verband = TestDataFactory.createVerband(1L, "DSB");
        disziplin = TestDataFactory.createDisziplin(1L, "Luftgewehr", verband);
        eintrag = TestDataFactory.createEintrag(1L, schuetze, disziplin, schiesstand, LocalDate.now());
    }

    @Test
    void testErstelleEintrag() {
        when(eintragRepository.save(any(SchiessnachweisEintrag.class))).thenReturn(eintrag);

        service.erstelleEintrag(eintrag);

        verify(eintragRepository).save(eintrag);
        assertEquals(EintragStatus.UNSIGNIERT, eintrag.getStatus());
    }

    @Test
    void testFindeEintraegeImZeitraum() {
        LocalDate von = LocalDate.now().minusDays(7);
        LocalDate bis = LocalDate.now();
        List<SchiessnachweisEintrag> entities = Arrays.asList(eintrag);
        List<SchiessnachweisEintrag> dtos = Arrays.asList(new SchiessnachweisEintrag());

        when(eintragRepository.findBySchuetzeAndDatumBetween(schuetze, von, bis)).thenReturn(entities);
        when(eintragMapper.toListDTOList(entities)).thenReturn(dtos);

        List<SchiessnachweisEintrag> result = service.findeEintraegeImZeitraum(schuetze, von, bis);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eintragRepository).findBySchuetzeAndDatumBetween(schuetze, von, bis);
    }

    @Test
    void testSigniereEintrag() {
        Benutzer aufseher = TestDataFactory.createBenutzer(2L, "aufseher@example.com");
        eintrag.setStatus(EintragStatus.UNSIGNIERT);

        when(eintragRepository.findById(1L)).thenReturn(Optional.of(eintrag));
        when(eintragRepository.save(any(SchiessnachweisEintrag.class))).thenReturn(eintrag);

        service.signiereEintrag(1L, aufseher, "signature123");

        verify(eintragRepository).findById(1L);
        verify(eintragRepository).save(eintrag);
        assertEquals(EintragStatus.SIGNIERT, eintrag.getStatus());
        assertEquals(aufseher, eintrag.getAufseher());
        assertEquals("signature123", eintrag.getDigitaleSignatur());
        assertNotNull(eintrag.getSigniertAm());
    }

    @Test
    void testSigniereEintragBereitsSigniert() {
        eintrag.setStatus(EintragStatus.SIGNIERT);

        when(eintragRepository.findById(1L)).thenReturn(Optional.of(eintrag));

        assertThrows(IllegalStateException.class, () -> {
            service.signiereEintrag(1L, schuetze, "sig");
        });

        verify(eintragRepository, never()).save(any());
    }

    @Test
    void testLehneEintragAb() {
        Benutzer aufseher = TestDataFactory.createBenutzer(2L, "aufseher@example.com");
        eintrag.setStatus(EintragStatus.UNSIGNIERT);

        when(eintragRepository.findById(1L)).thenReturn(Optional.of(eintrag));
        when(eintragRepository.save(any(SchiessnachweisEintrag.class))).thenReturn(eintrag);

        service.lehneEintragAb(1L, aufseher, "Fehlerhafte Daten");

        verify(eintragRepository).save(eintrag);
        assertEquals(EintragStatus.ABGELEHNT, eintrag.getStatus());
        assertEquals("Fehlerhafte Daten", eintrag.getAblehnungsgrund());
    }

    @Test
    void testLoescheEintrag() {
        eintrag.setStatus(EintragStatus.UNSIGNIERT);

        when(eintragRepository.findById(1L)).thenReturn(Optional.of(eintrag));
        doNothing().when(eintragRepository).delete(eintrag);

        service.loescheEintrag(1L);

        verify(eintragRepository).delete(eintrag);
    }

    @Test
    void testLoescheEintragSigniertWirftException() {
        eintrag.setStatus(EintragStatus.SIGNIERT);

        when(eintragRepository.findById(1L)).thenReturn(Optional.of(eintrag));

        assertThrows(IllegalStateException.class, () -> {
            service.loescheEintrag(1L);
        });

        verify(eintragRepository, never()).delete(any());
    }

    @Test
    void testZaehleUnsignierteEintraege() {
        when(eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.UNSIGNIERT)).thenReturn(5L);

        long count = service.zaehleUnsignierteEintraege(schuetze);

        assertEquals(5L, count);
        verify(eintragRepository).countBySchuetzeAndStatus(schuetze, EintragStatus.UNSIGNIERT);
    }
}

