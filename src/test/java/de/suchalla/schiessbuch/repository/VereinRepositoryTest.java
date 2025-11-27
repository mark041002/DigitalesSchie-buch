package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Verband;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository-Tests für VereinRepository mit H2 In-Memory-Datenbank.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
class VereinRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VereinRepository vereinRepository;

    private Verein testVerein;
    private Verband testVerband;

    @BeforeEach
    void setUp() {
        testVerband = Verband.builder()
                .name("Deutscher Schützenbund")
                .beschreibung("Beschreibung Deutscher Schützenbund")
                .build();
        entityManager.persist(testVerband);

        testVerein = Verein.builder()
            .name("Schützenverein Teststadt")
            .adresse("Schützenstraße 1, 12345 Teststadt")
            .beschreibung("Traditionsreicher Verein")
            .build();
        testVerein.getVerbaende().add(testVerband);
    }

    @Test
    void testSaveVerein() {
        Verein saved = vereinRepository.save(testVerein);

        assertNotNull(saved.getId());
        assertEquals("Schützenverein Teststadt", saved.getName());
    }

    @Test
    void testFindById() {
        Verein saved = entityManager.persistAndFlush(testVerein);

        Optional<Verein> found = vereinRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Schützenverein Teststadt", found.get().getName());
        assertEquals(1, found.get().getVerbaende().size());
    }

    // findByVereinsNummer tests removed (field deprecated)

    @Test
    void testFindByVerbaendeContaining() {
        Verein verein1 = Verein.builder()
            .name("Verein 1")
            .build();
        verein1.getVerbaende().add(testVerband);

        Verein verein2 = Verein.builder()
            .name("Verein 2")
            .build();
        verein2.getVerbaende().add(testVerband);

        entityManager.persist(verein1);
        entityManager.persist(verein2);
        entityManager.flush();

        List<Verein> vereine = vereinRepository.findByVerbaendeContaining(testVerband);

        assertEquals(2, vereine.size());
        assertTrue(vereine.stream().anyMatch(v -> v.getName().equals("Verein 1")));
        assertTrue(vereine.stream().anyMatch(v -> v.getName().equals("Verein 2")));
    }

    @Test
    void testFindAllNames() {
        Verein verein1 = Verein.builder()
            .name("Verein A")
            .build();

        Verein verein2 = Verein.builder()
            .name("Verein B")
            .build();

        entityManager.persist(verein1);
        entityManager.persist(verein2);
        entityManager.flush();

        List<String> names = vereinRepository.findAllNames();

        assertEquals(2, names.size());
        assertTrue(names.contains("Verein A"));
        assertTrue(names.contains("Verein B"));
    }

    @Test
    void testUpdateVerein() {
        Verein saved = entityManager.persistAndFlush(testVerein);

        saved.setName("Neuer Vereinsname");
        vereinRepository.save(saved);

        Verein updated = entityManager.find(Verein.class, saved.getId());
        assertEquals("Neuer Vereinsname", updated.getName());
    }

    @Test
    void testDeleteVerein() {
        Verein saved = entityManager.persistAndFlush(testVerein);
        Long id = saved.getId();

        vereinRepository.delete(saved);
        entityManager.flush();

        Verein deleted = entityManager.find(Verein.class, id);
        assertNull(deleted);
    }

    @Test
    void testFindAll() {
        Verein verein1 = Verein.builder()
                .name("Verein 1")
                .build();

        Verein verein2 = Verein.builder()
                .name("Verein 2")
                .build();

        entityManager.persist(verein1);
        entityManager.persist(verein2);
        entityManager.flush();

        var allVereine = vereinRepository.findAll();

        assertEquals(2, allVereine.size());
    }

    @Test
    void testTimestampsAreSetOnPersist() {
        Verein saved = vereinRepository.save(testVerein);
        entityManager.flush();

        assertNotNull(saved.getErstelltAm());
        assertNotNull(saved.getAktualisiertAm());
    }

    @Test
    void testVerbaendeRelationship() {
        Verband verband2 = Verband.builder()
                .name("BDS")
                .beschreibung("Beschreibung BDS")
                .build();
        entityManager.persist(verband2);

        testVerein.getVerbaende().add(verband2);
        Verein saved = entityManager.persistAndFlush(testVerein);

        Verein found = entityManager.find(Verein.class, saved.getId());
        assertEquals(2, found.getVerbaende().size());
    }
}
