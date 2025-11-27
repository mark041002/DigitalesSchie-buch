package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.model.entity.Vereinsmitgliedschaft;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
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
class VereinsmitgliedschaftRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VereinsmitgliedschaftRepository repository;

    private Benutzer benutzer;
    private Verein verein;

    @BeforeEach
    void init() {
        benutzer = TestDataFactory.createBenutzer(null, "user@example.com");
        verein = TestDataFactory.createVerein(null, "Verein A");
        em.persist(benutzer);
        em.persist(verein);
        em.flush();
    }

    @Test
    void testSaveMitgliedschaft() {
        Vereinsmitgliedschaft m = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        Vereinsmitgliedschaft saved = repository.save(m);

        assertNotNull(saved.getId());
        assertEquals(MitgliedschaftsStatus.AKTIV, saved.getStatus());
    }

    @Test
    void testFindByBenutzer() {
        Vereinsmitgliedschaft m = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        em.persist(m);
        em.flush();

        List<Vereinsmitgliedschaft> found = repository.findByBenutzer(benutzer);
        assertEquals(1, found.size());
        assertEquals(benutzer.getId(), found.get(0).getBenutzer().getId());
    }

    @Test
    void testFindAllByBenutzerAndVerein() {
        Vereinsmitgliedschaft m1 = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        em.persist(m1);
        em.flush();

        List<Vereinsmitgliedschaft> found = repository.findAllByBenutzerAndVerein(benutzer, verein);
        assertEquals(1, found.size());
    }

    @Test
    void testFindByVereinAndStatus() {
        Vereinsmitgliedschaft m1 = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.BEANTRAGT);
        em.persist(m1);
        em.flush();

        List<Vereinsmitgliedschaft> found = repository.findByVereinAndStatus(verein, MitgliedschaftsStatus.BEANTRAGT);
        assertEquals(1, found.size());
        assertEquals(MitgliedschaftsStatus.BEANTRAGT, found.get(0).getStatus());
    }

    @Test
    void testFindByVereinAndIstVereinschef() {
        Vereinsmitgliedschaft m = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        m.setIstVereinschef(true);
        em.persist(m);
        em.flush();

        List<Vereinsmitgliedschaft> found = repository.findByVereinAndIstVereinschef(verein, true);
        assertEquals(1, found.size());
        assertTrue(found.get(0).getIstVereinschef());
    }

    @Test
    void testFindByVereinAndIstAufseher() {
        Vereinsmitgliedschaft m = TestDataFactory.createMitgliedschaft(null, benutzer, verein, MitgliedschaftsStatus.AKTIV);
        m.setIstAufseher(true);
        em.persist(m);
        em.flush();

        List<Vereinsmitgliedschaft> found = repository.findByVereinAndIstAufseher(verein, true);
        assertEquals(1, found.size());
        assertTrue(found.get(0).getIstAufseher());
    }
}

