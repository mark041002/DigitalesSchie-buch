package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Verband;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class VerbandRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VerbandRepository repository;

    @Test
    void testSaveVerband() {
        Verband verband = TestDataFactory.createVerband(null, "DSB");
        Verband saved = repository.save(verband);

        assertNotNull(saved.getId());
        assertEquals("DSB", saved.getName());
    }

    @Test
    void testExistsByName() {
        Verband verband = TestDataFactory.createVerband(null, "TestVerband");
        em.persist(verband);
        em.flush();

        assertTrue(repository.existsByName("TestVerband"));
        assertFalse(repository.existsByName("NichtExistent"));
    }

    @Test
    void testFindAllWithVereine() {
        Verband v1 = TestDataFactory.createVerband(null, "Verband1");
        Verband v2 = TestDataFactory.createVerband(null, "Verband2");
        em.persist(v1);
        em.persist(v2);
        em.flush();

        List<Verband> list = repository.findAllWithVereine();
        assertEquals(2, list.size());
    }

    @Test
    void testDeleteVerband() {
        Verband verband = TestDataFactory.createVerband(null, "ToDelete");
        em.persist(verband);
        em.flush();
        Long id = verband.getId();

        repository.delete(verband);
        em.flush();

        assertNull(em.find(Verband.class, id));
    }
}

