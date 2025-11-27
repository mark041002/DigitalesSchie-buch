package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Disziplin;
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
 * Repository-Tests für DisziplinRepository mit H2 In-Memory-Datenbank.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
class DisziplinRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DisziplinRepository disziplinRepository;

    private Disziplin testDisziplin;
    private Verband testVerband;

    @BeforeEach
    void setUp() {
        testVerband = Verband.builder()
            .name("DSB")
            .beschreibung("Beschreibung DSB")
            .build();
        entityManager.persist(testVerband);

        testDisziplin = Disziplin.builder()
            .kennziffer("LG-10m")
            .programm("Sportschießen mit Luftgewehr auf 10 Meter")
            .verband(testVerband)
            .build();
    }

    @Test
    void testSaveDisziplin() {
        Disziplin saved = disziplinRepository.save(testDisziplin);

        assertNotNull(saved.getId());
        assertEquals("LG-10m", saved.getKennziffer());
        assertEquals("Sportschießen mit Luftgewehr auf 10 Meter", saved.getProgramm());
    }

    @Test
    void testFindById() {
        Disziplin saved = entityManager.persistAndFlush(testDisziplin);

        Optional<Disziplin> found = disziplinRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("LG-10m", found.get().getKennziffer());
    }

    @Test
    void testFindByVerbandId() {
        Disziplin disziplin1 = Disziplin.builder()
            .kennziffer("D-1")
            .verband(testVerband)
            .build();

        Disziplin disziplin2 = Disziplin.builder()
            .kennziffer("D-2")
            .verband(testVerband)
            .build();

        entityManager.persist(disziplin1);
        entityManager.persist(disziplin2);
        entityManager.flush();

        List<Disziplin> disziplinen = disziplinRepository.findByVerbandId(testVerband.getId());

        assertEquals(2, disziplinen.size());
        assertTrue(disziplinen.stream().anyMatch(d -> d.getKennziffer().equals("D-1")));
        assertTrue(disziplinen.stream().anyMatch(d -> d.getKennziffer().equals("D-2")));
    }

    @Test
    void testUpdateDisziplin() {
        Disziplin saved = entityManager.persistAndFlush(testDisziplin);

        saved.setKennziffer("KK-50m");
        saved.setProgramm("Präzisionsschießen mit Kleinkalibergewehr");
        disziplinRepository.save(saved);

        Disziplin updated = entityManager.find(Disziplin.class, saved.getId());
        assertEquals("KK-50m", updated.getKennziffer());
        assertEquals("Präzisionsschießen mit Kleinkalibergewehr", updated.getProgramm());
    }

    @Test
    void testDeleteDisziplin() {
        Disziplin saved = entityManager.persistAndFlush(testDisziplin);
        Long id = saved.getId();

        disziplinRepository.delete(saved);
        entityManager.flush();

        Disziplin deleted = entityManager.find(Disziplin.class, id);
        assertNull(deleted);
    }

    @Test
    void testFindAll() {
        Disziplin disziplin1 = Disziplin.builder()
            .kennziffer("D-1")
            .verband(testVerband)
            .build();

        Disziplin disziplin2 = Disziplin.builder()
            .kennziffer("D-2")
            .verband(testVerband)
            .build();

        entityManager.persist(disziplin1);
        entityManager.persist(disziplin2);
        entityManager.flush();

        var allDisziplinen = disziplinRepository.findAll();

        assertEquals(2, allDisziplinen.size());
    }

    @Test
    void testTimestampsAreSetOnPersist() {
        Disziplin saved = disziplinRepository.save(testDisziplin);
        entityManager.flush();

        assertNotNull(saved.getErstelltAm());
        assertNotNull(saved.getAktualisiertAm());
    }
}
