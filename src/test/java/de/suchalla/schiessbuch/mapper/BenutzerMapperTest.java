package de.suchalla.schiessbuch.mapper;

import de.suchalla.schiessbuch.model.dto.*;
import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für BenutzerMapper.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class BenutzerMapperTest {

    private BenutzerMapper mapper;
    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        mapper = new BenutzerMapper();
        
        testBenutzer = Benutzer.builder()
                .id(1L)
                .email("test@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("encodedPassword")
                .rolle(BenutzerRolle.SCHUETZE)
                .emailVerifiziert(true)
                .emailNotificationsEnabled(true)
                .erstelltAm(LocalDateTime.now().minusDays(10))
                .aktualisiertAm(LocalDateTime.now())
                .build();
    }

    @Test
    void testToDTO_MapsAllFieldsCorrectly() {
        BenutzerDTO dto = mapper.toDTO(testBenutzer);

        assertNotNull(dto);
        assertEquals(testBenutzer.getId(), dto.getId());
        assertEquals(testBenutzer.getVorname(), dto.getVorname());
        assertEquals(testBenutzer.getNachname(), dto.getNachname());
        assertEquals(testBenutzer.getEmail(), dto.getEmail());
        assertEquals(testBenutzer.getRolle(), dto.getRolle());
        assertEquals(testBenutzer.isEmailVerifiziert(), dto.isEmailVerifiziert());
        assertEquals(testBenutzer.isEmailNotificationsEnabled(), dto.isEmailNotificationsEnabled());
        assertEquals(testBenutzer.getErstelltAm(), dto.getErstelltAm());
        assertEquals(testBenutzer.getAktualisiertAm(), dto.getAktualisiertAm());
    }

    @Test
    void testToDTO_DoesNotIncludePassword() {
        BenutzerDTO dto = mapper.toDTO(testBenutzer);

        assertNotNull(dto);
        // Prüfe dass kein "passwort" Feld im DTO existiert
        assertFalse(java.util.Arrays.stream(dto.getClass().getDeclaredFields())
                .anyMatch(f -> f.getName().equals("passwort")));
    }

    @Test
    void testToDTO_HandlesNull() {
        BenutzerDTO dto = mapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    void testToListDTO_MapsMinimalFields() {
        BenutzerListDTO listDto = mapper.toListDTO(testBenutzer);

        assertNotNull(listDto);
        assertEquals(testBenutzer.getId(), listDto.getId());
        assertEquals(testBenutzer.getVorname(), listDto.getVorname());
        assertEquals(testBenutzer.getNachname(), listDto.getNachname());
        assertEquals(testBenutzer.getEmail(), listDto.getEmail());
        assertEquals(testBenutzer.getRolle(), listDto.getRolle());
    }

    @Test
    void testToListDTO_HandlesNull() {
        BenutzerListDTO listDto = mapper.toListDTO(null);
        assertNull(listDto);
    }

    @Test
    void testToListDTOList_MapsMultipleEntities() {
        Benutzer benutzer1 = Benutzer.builder()
                .id(1L)
                .email("user1@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        Benutzer benutzer2 = Benutzer.builder()
                .id(2L)
                .email("user2@example.com")
                .vorname("Anna")
                .nachname("Schmidt")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        List<BenutzerListDTO> listDtos = mapper.toListDTOList(List.of(benutzer1, benutzer2));

        assertNotNull(listDtos);
        assertEquals(2, listDtos.size());
        assertEquals("Max", listDtos.get(0).getVorname());
        assertEquals("Anna", listDtos.get(1).getVorname());
    }

    @Test
    void testToListDTOList_HandlesNull() {
        List<BenutzerListDTO> listDtos = mapper.toListDTOList(null);
        assertNotNull(listDtos);
        assertTrue(listDtos.isEmpty());
    }

    @Test
    void testToListDTOList_HandlesEmptyList() {
        List<BenutzerListDTO> listDtos = mapper.toListDTOList(List.of());
        assertNotNull(listDtos);
        assertTrue(listDtos.isEmpty());
    }

    @Test
    void testToDTOList_FromBaseMapper() {
        Benutzer benutzer1 = Benutzer.builder()
                .id(1L)
                .email("user1@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .build();

        List<BenutzerDTO> dtos = mapper.toDTOList(List.of(benutzer1));

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals("Max", dtos.get(0).getVorname());
    }

    @Test
    void testMapping_WithDifferentRoles() {
        BenutzerRolle[] roles = {BenutzerRolle.SCHUETZE, BenutzerRolle.AUFSEHER, 
                                  BenutzerRolle.VEREINS_CHEF, BenutzerRolle.ADMIN};

        for (BenutzerRolle rolle : roles) {
            Benutzer benutzer = Benutzer.builder()
                    .id(1L)
                    .email("test@example.com")
                    .vorname("Test")
                    .nachname("User")
                    .passwort("password")
                    .rolle(rolle)
                    .build();

            BenutzerDTO dto = mapper.toDTO(benutzer);
            assertEquals(rolle, dto.getRolle());
        }
    }
}
