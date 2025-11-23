package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.SchiesstandDTO;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für SchiesstandMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class SchiesstandMapperTest {

    private SchiesstandMapper mapper;
    private Schiesstand testSchiesstand;
    private Verein testVerein;

    @BeforeEach
    void setUp() {
        mapper = new SchiesstandMapper();
        
        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .vereinsNummer("TV-123")
                .build();
        
        testSchiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .adresse("Schießstandstraße 1, 12345 Stadt")
                .verein(testVerein)
                .build();
    }

    @Test
    void testToDTO_MapsAllFields() {
        SchiesstandDTO dto = mapper.toDTO(testSchiesstand);

        assertNotNull(dto);
        assertEquals(testSchiesstand.getId(), dto.getId());
        assertEquals(testSchiesstand.getName(), dto.getName());
        assertEquals(testSchiesstand.getAdresse(), dto.getAdresse());
        assertEquals(testVerein.getId(), dto.getVereinId());
        assertEquals(testVerein.getName(), dto.getVereinName());
    }

    @Test
    void testToDTO_HandlesNullVerein() {
        testSchiesstand.setVerein(null);
        SchiesstandDTO dto = mapper.toDTO(testSchiesstand);

        assertNotNull(dto);
        assertNull(dto.getVereinId());
        assertNull(dto.getVereinName());
    }

    @Test
    void testToDTO_HandlesNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToDTOList() {
        Schiesstand stand2 = Schiesstand.builder()
                .id(2L)
                .name("Stand 2")
                .adresse("Andere Straße")
                .verein(testVerein)
                .build();

        List<SchiesstandDTO> dtos = mapper.toDTOList(List.of(testSchiesstand, stand2));

        assertEquals(2, dtos.size());
        assertEquals("Stand 1", dtos.get(0).getName());
        assertEquals("Stand 2", dtos.get(1).getName());
    }
}
