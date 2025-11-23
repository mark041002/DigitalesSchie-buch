package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.VerbandDTO;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für VerbandMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class VerbandMapperTest {

    private VerbandMapper mapper;
    private Verband testVerband;

    @BeforeEach
    void setUp() {
        mapper = new VerbandMapper();
        testVerband = Verband.builder()
                .id(1L)
                .name("Deutscher Schützenbund")
                .beschreibung("Dachverband für Schützenvereine")
                .build();
    }

    @Test
    void testToDTO_MapsAllFields() {
        VerbandDTO dto = mapper.toDTO(testVerband);

        assertNotNull(dto);
        assertEquals(testVerband.getId(), dto.getId());
        assertEquals(testVerband.getName(), dto.getName());
        assertEquals(testVerband.getBeschreibung(), dto.getBeschreibung());
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTOList() {
        Verband verband2 = Verband.builder()
                .id(2L)
                .name("Bayerischer Sportschützenbund")
                .build();

        List<VerbandDTO> dtos = mapper.toDTOList(List.of(testVerband, verband2));

        assertEquals(2, dtos.size());
        assertEquals("Deutscher Schützenbund", dtos.get(0).getName());
        assertEquals("Bayerischer Sportschützenbund", dtos.get(1).getName());
    }
}
