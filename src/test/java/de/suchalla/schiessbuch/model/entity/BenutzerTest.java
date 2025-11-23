package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.mapper.BenutzerMapper;
import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.dto.BenutzerListDTO;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Benutzer-Entity.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
class BenutzerTest {

    private Benutzer benutzer;
    private BenutzerMapper mapper;

    @BeforeEach
    void setUp() {
        benutzer = Benutzer.builder()
                .id(1L)
                .email("test@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("encodedPassword")
                .rolle(BenutzerRolle.SCHUETZE)
                .emailVerifiziert(false)
                .emailNotificationsEnabled(true)
                .build();
        mapper = new BenutzerMapper();
    }

    @Test
    void testBenutzerBuilder() {
        assertNotNull(benutzer);
        assertEquals(1L, benutzer.getId());
        assertEquals("test@example.com", benutzer.getEmail());
        assertEquals("Max", benutzer.getVorname());
        assertEquals("Mustermann", benutzer.getNachname());
        assertEquals(BenutzerRolle.SCHUETZE, benutzer.getRolle());
        assertFalse(benutzer.isEmailVerifiziert());
        assertTrue(benutzer.isEmailNotificationsEnabled());
    }

    @Test
    void testGetVollstaendigerName() {
        String vollstaendigerName = benutzer.getVollstaendigerName();
        assertEquals("Max Mustermann", vollstaendigerName);
    }

    @Test
    void testDefaultRolle() {
        Benutzer neuerBenutzer = Benutzer.builder()
                .email("neu@example.com")
                .vorname("Hans")
                .nachname("Mueller")
                .passwort("password")
                .build();

        assertEquals(BenutzerRolle.SCHUETZE, neuerBenutzer.getRolle());
    }

    @Test
    void testDefaultEmailNotificationsEnabled() {
        Benutzer neuerBenutzer = Benutzer.builder()
                .email("neu@example.com")
                .vorname("Hans")
                .nachname("Mueller")
                .passwort("password")
                .build();

        assertTrue(neuerBenutzer.isEmailNotificationsEnabled());
    }

    @Test
    void testDefaultEmailVerifiziert() {
        Benutzer neuerBenutzer = Benutzer.builder()
                .email("neu@example.com")
                .vorname("Hans")
                .nachname("Mueller")
                .passwort("password")
                .build();

        assertFalse(neuerBenutzer.isEmailVerifiziert());
    }

    @Test
    void testSetterMethods() {
        benutzer.setVorname("Anna");
        benutzer.setNachname("Schmidt");
        benutzer.setEmail("anna.schmidt@example.com");
        benutzer.setEmailVerifiziert(true);

        assertEquals("Anna", benutzer.getVorname());
        assertEquals("Schmidt", benutzer.getNachname());
        assertEquals("anna.schmidt@example.com", benutzer.getEmail());
        assertTrue(benutzer.isEmailVerifiziert());
    }

    @Test
    void testEqualsAndHashCode() {
        Benutzer benutzer1 = Benutzer.builder()
                .id(1L)
                .email("test1@example.com")
                .build();

        Benutzer benutzer2 = Benutzer.builder()
                .id(1L)
                .email("test2@example.com")
                .build();

        Benutzer benutzer3 = Benutzer.builder()
                .id(2L)
                .email("test1@example.com")
                .build();

        // Gleiche ID -> gleich
        assertEquals(benutzer1, benutzer2);
        assertEquals(benutzer1.hashCode(), benutzer2.hashCode());

        // Verschiedene ID -> ungleich
        assertNotEquals(benutzer1, benutzer3);
    }

    @Test
    void testToString() {
        String toString = benutzer.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Max"));
        assertTrue(toString.contains("Mustermann"));
    }

    @Test
    void testPrePersistSetsTimestamps() {
        Benutzer neuerBenutzer = new Benutzer();
        assertNull(neuerBenutzer.getErstelltAm());
        assertNull(neuerBenutzer.getAktualisiertAm());

        neuerBenutzer.onCreate();

        assertNotNull(neuerBenutzer.getErstelltAm());
        assertNotNull(neuerBenutzer.getAktualisiertAm());
    }

    @Test
    void testPreUpdateSetsAktualisiertAm() throws InterruptedException {
        benutzer.onCreate();
        var erstelltAm = benutzer.getErstelltAm();

        Thread.sleep(10); // Kleine Verzögerung für unterschiedliche Timestamps

        benutzer.onUpdate();

        assertEquals(erstelltAm, benutzer.getErstelltAm());
        assertNotNull(benutzer.getAktualisiertAm());
        assertTrue(benutzer.getAktualisiertAm().isAfter(erstelltAm) ||
                   benutzer.getAktualisiertAm().isEqual(erstelltAm));
    }

    @Test
    void testEntityToDtoMapping() {
        BenutzerDTO dto = mapper.toDTO(benutzer);

        assertNotNull(dto);
        assertEquals(benutzer.getId(), dto.getId());
        assertEquals(benutzer.getVorname(), dto.getVorname());
        assertEquals(benutzer.getNachname(), dto.getNachname());
        assertEquals(benutzer.getEmail(), dto.getEmail());
        assertEquals(benutzer.getRolle(), dto.getRolle());
        assertEquals(benutzer.isEmailVerifiziert(), dto.isEmailVerifiziert());
        assertEquals(benutzer.isEmailNotificationsEnabled(), dto.isEmailNotificationsEnabled());

        // Passwort sollte nicht im DTO sein - prüfe, dass kein "passwort" Feld existiert
        assertTrue(java.util.Arrays.stream(dto.getClass().getDeclaredFields())
                .noneMatch(f -> f.getName().equals("passwort")));
    }

    @Test
    void testEntityToListDtoMapping() {
        BenutzerListDTO listDto = mapper.toListDTO(benutzer);

        assertNotNull(listDto);
        assertEquals(benutzer.getId(), listDto.getId());
        assertEquals(benutzer.getVorname(), listDto.getVorname());
        assertEquals(benutzer.getNachname(), listDto.getNachname());
        assertEquals(benutzer.getEmail(), listDto.getEmail());
        assertEquals(benutzer.getRolle(), listDto.getRolle());
        assertEquals(benutzer.getVollstaendigerName(), listDto.getVollstaendigerName());
    }

    @Test
    void testEntityToDtoMappingNull() {
        assertNull(mapper.toDTO(null));
        assertNull(mapper.toListDTO(null));
    }
}

