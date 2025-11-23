package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.DisziplinDTO;
import de.suchalla.schiessbuch.model.entity.Disziplin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für DisziplinMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class DisziplinMapperTest {

    private DisziplinMapper mapper;
    private Disziplin testDisziplin;

    @BeforeEach
    void setUp() {
        mapper = new DisziplinMapper();
        testDisziplin = Disziplin.builder()
            .id(1L)
            .kennziffer("LG-10m")
            .programm("Luftgewehr 10m - Standardprogramm")
            .build();
    }

    @Test
    void testToDTO_MapsAllFields() {
        DisziplinDTO dto = mapper.toDTO(testDisziplin);

        assertNotNull(dto);
        assertEquals(testDisziplin.getId(), dto.getId());
        assertEquals(testDisziplin.getKennziffer(), dto.getKennziffer());
        assertEquals(testDisziplin.getProgramm(), dto.getProgramm());
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTOList_MapsMultiple() {
        Disziplin disziplin2 = Disziplin.builder()
            .id(2L)
            .kennziffer("LP-10m")
            .programm("Luftpistole 10m - Kurzprogramm")
            .build();

        List<DisziplinDTO> dtos = mapper.toDTOList(List.of(testDisziplin, disziplin2));

        assertEquals(2, dtos.size());
        assertEquals("LG-10m", dtos.get(0).getKennziffer());
        assertEquals("LP-10m", dtos.get(1).getKennziffer());
    }
}
