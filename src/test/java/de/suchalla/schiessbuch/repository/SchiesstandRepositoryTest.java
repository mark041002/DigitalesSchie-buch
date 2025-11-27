package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SchiesstandRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SchiesstandRepository repository;

    private Verein verein;

    @BeforeEach
    void init() {
        verein = TestDataFactory.createVerein(null, "Verein A");
        em.persist(verein);
        em.flush();
    }

    @Test
    void testSaveSchiesstand() {
        Schiesstand schiesstand = TestDataFactory.createSchiesstand(null, "Stand 1", verein);
        Schiesstand saved = repository.save(schiesstand);

        assertNotNull(saved.getId());
        assertEquals("Stand 1", saved.getName());
    }

    @Test
    void testFindByVerein() {
        // Ein Schießstand für den initialen Verein
        Schiesstand s1 = TestDataFactory.createSchiesstand(null, "Stand 1", verein);
        em.persist(s1);

        Verein other = TestDataFactory.createVerein(null, "Verein B");
        em.persist(other);
        Schiesstand s2 = TestDataFactory.createSchiesstand(null, "Stand 2", other);
        em.persist(s2);

        em.flush();

        List<Schiesstand> found = repository.findByVerein(verein);
        // Es darf nur ein Schießstand pro Verein geben
        assertEquals(1, found.size());
    }

    @Test
    void testFindAllWithVerein() {
        Schiesstand s1 = TestDataFactory.createSchiesstand(null, "Stand A", verein);
        em.persist(s1);
        em.flush();

        List<Schiesstand> list = repository.findAllWithVerein();
        assertFalse(list.isEmpty());
        assertNotNull(list.get(0).getVerein());
    }

    @Test
    void testDeleteSchiesstand() {
        Schiesstand s = TestDataFactory.createSchiesstand(null, "ToDelete", verein);
        em.persist(s);
        em.flush();
        Long id = s.getId();

        repository.delete(s);
        em.flush();

        assertNull(em.find(Schiesstand.class, id));
    }
}
