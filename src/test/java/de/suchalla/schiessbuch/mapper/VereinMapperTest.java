package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VereinDTO;
import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für VereinMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class VereinMapperTest {

    private VereinMapper mapper;
    private Verein testVerein;
    private Verband testVerband;

    @BeforeEach
    void setUp() {
        mapper = new VereinMapper();
        
        testVerband = Verband.builder()
                .id(1L)
                .name("Deutscher Schützenbund")
                .build();
        
        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .vereinsNummer("TV-123")
                .adresse("Vereinsstraße 1")
                .build();
        testVerein.getVerbaende().add(testVerband);
    }

    @Test
    void testToDTO_MapsAllFields() {
        VereinDTO dto = mapper.toDTO(testVerein);

        assertNotNull(dto);
        assertEquals(testVerein.getId(), dto.getId());
        assertEquals(testVerein.getName(), dto.getName());
        assertEquals(testVerein.getVereinsNummer(), dto.getVereinsNummer());
        assertEquals(testVerein.getAdresse(), dto.getAdresse());
        assertEquals(1, dto.getVerbandIds().size());
        assertEquals(testVerband.getId(), dto.getVerbandIds().get(0));
        assertEquals(testVerband.getName(), dto.getVerbandNamen().get(0));
    }

    @Test
    void testToDTO_HandlesNullVerband() {
        testVerein.getVerbaende().clear();
        VereinDTO dto = mapper.toDTO(testVerein);

        assertNotNull(dto);
        assertTrue(dto.getVerbandIds().isEmpty());
        assertTrue(dto.getVerbandNamen().isEmpty());
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTOList() {
        Verein verein2 = Verein.builder()
                .id(2L)
                .name("Zweiter Verein")
                .vereinsNummer("ZV-456")
                .build();

        List<VereinDTO> dtos = mapper.toDTOList(List.of(testVerein, verein2));

        assertEquals(2, dtos.size());
        assertEquals("Testverein", dtos.get(0).getName());
        assertEquals("Zweiter Verein", dtos.get(1).getName());
    }
}
