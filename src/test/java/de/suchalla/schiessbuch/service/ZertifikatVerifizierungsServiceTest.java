package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.DigitalesZertifikatMapper;
import de.suchalla.schiessbuch.model.dto.DigitalesZertifikatDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZertifikatVerifizierungsServiceTest {

    @Mock
    private DigitalesZertifikatRepository zertifikatRepository;

    @Mock
    private DigitalesZertifikatMapper zertifikatMapper;

    @InjectMocks
    private ZertifikatVerifizierungsService service;

    private DigitalesZertifikat zertifikat;
    private Benutzer benutzer;

    @BeforeEach
    void setUp() {
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
        zertifikat = TestDataFactory.createZertifikat(1L, "AUFSEHER", benutzer);
    }

    @Test
    void testVerifiziereErfolgreich() {
        String seriennummer = zertifikat.getSeriennummer();
        DigitalesZertifikatDTO dto = DigitalesZertifikatDTO.builder()
                .id(zertifikat.getId())
                .seriennummer(seriennummer)
                .build();

        when(zertifikatRepository.findBySeriennummerWithDetails(seriennummer)).thenReturn(Optional.of(zertifikat));
        when(zertifikatMapper.toDTO(zertifikat)).thenReturn(dto);

        DigitalesZertifikatDTO result = service.verifiziere(seriennummer);

        assertNotNull(result);
        assertEquals(seriennummer, result.getSeriennummer());
        verify(zertifikatRepository).findBySeriennummerWithDetails(seriennummer);
        verify(zertifikatMapper).toDTO(zertifikat);
    }

    @Test
    void testVerifiziereNichtGefunden() {
        String seriennummer = "nicht-existent";

        when(zertifikatRepository.findBySeriennummerWithDetails(seriennummer)).thenReturn(Optional.empty());

        DigitalesZertifikatDTO result = service.verifiziere(seriennummer);

        assertNull(result);
        verify(zertifikatRepository).findBySeriennummerWithDetails(seriennummer);
        verify(zertifikatMapper, never()).toDTO(any());
    }

    @Test
    void testVerifiziereNullSeriennummer() {
        DigitalesZertifikatDTO result = service.verifiziere(null);

        assertNull(result);
        verify(zertifikatRepository, never()).findBySeriennummerWithDetails(any());
    }

    @Test
    void testVerifiziereLeerStringSeriennummer() {
        DigitalesZertifikatDTO result = service.verifiziere("   ");

        assertNull(result);
        verify(zertifikatRepository, never()).findBySeriennummerWithDetails(any());
    }
}

