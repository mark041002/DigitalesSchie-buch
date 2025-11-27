package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository-Tests für BenutzerRepository mit H2 In-Memory-Datenbank.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
class BenutzerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BenutzerRepository benutzerRepository;

    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        testBenutzer = Benutzer.builder()
                .email("test@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("encodedPassword")
                .rolle(BenutzerRolle.SCHUETZE)
                .emailVerifiziert(false)
                .emailNotificationsEnabled(true)
                .build();
    }

    @Test
    void testSaveBenutzer() {
        Benutzer saved = benutzerRepository.save(testBenutzer);

        assertNotNull(saved.getId());
        assertEquals("test@example.com", saved.getEmail());
        assertEquals("Max", saved.getVorname());
        assertEquals("Mustermann", saved.getNachname());
    }

    @Test
    void testFindById() {
        Benutzer saved = entityManager.persistAndFlush(testBenutzer);

        Optional<Benutzer> found = benutzerRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("Max", found.get().getVorname());
    }

    @Test
    void testFindByEmail() {
        entityManager.persistAndFlush(testBenutzer);

        Optional<Benutzer> found = benutzerRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("Max", found.get().getVorname());
        assertEquals("Mustermann", found.get().getNachname());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<Benutzer> found = benutzerRepository.findByEmail("notfound@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        entityManager.persistAndFlush(testBenutzer);

        assertTrue(benutzerRepository.existsByEmail("test@example.com"));
        assertFalse(benutzerRepository.existsByEmail("notfound@example.com"));
    }

    @Test
    void testUpdateBenutzer() {
        Benutzer saved = entityManager.persistAndFlush(testBenutzer);

        saved.setVorname("Anna");
        saved.setNachname("Schmidt");
        benutzerRepository.save(saved);

        Benutzer updated = entityManager.find(Benutzer.class, saved.getId());
        assertEquals("Anna", updated.getVorname());
        assertEquals("Schmidt", updated.getNachname());
    }

    @Test
    void testDeleteBenutzer() {
        Benutzer saved = entityManager.persistAndFlush(testBenutzer);
        Long id = saved.getId();

        benutzerRepository.delete(saved);
        entityManager.flush();

        Benutzer deleted = entityManager.find(Benutzer.class, id);
        assertNull(deleted);
    }

    @Test
    void testFindAll() {
        Benutzer benutzer1 = Benutzer.builder()
                .email("user1@example.com")
                .vorname("User1")
                .nachname("Test1")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        Benutzer benutzer2 = Benutzer.builder()
                .email("user2@example.com")
                .vorname("User2")
                .nachname("Test2")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        entityManager.persist(benutzer1);
        entityManager.persist(benutzer2);
        entityManager.flush();

        var allBenutzer = benutzerRepository.findAll();

        assertEquals(2, allBenutzer.size());
    }

    @Test
    void testTimestampsAreSetOnPersist() {
        Benutzer saved = benutzerRepository.save(testBenutzer);
        entityManager.flush();

        assertNotNull(saved.getErstelltAm());
        assertNotNull(saved.getAktualisiertAm());
    }

    @Test
    void testTimestampsAreUpdatedOnUpdate() throws InterruptedException {
        Benutzer saved = benutzerRepository.save(testBenutzer);
        entityManager.flush();

        var erstelltAm = saved.getErstelltAm();

        Thread.sleep(10);

        saved.setVorname("Updated");
        benutzerRepository.save(saved);
        entityManager.flush();

        assertEquals(erstelltAm, saved.getErstelltAm());
        assertNotNull(saved.getAktualisiertAm());
    }
}

