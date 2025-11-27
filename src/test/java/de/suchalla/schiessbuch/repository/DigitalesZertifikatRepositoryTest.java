package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Schiesstand;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DigitalesZertifikatRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DigitalesZertifikatRepository repository;

    private Benutzer benutzer;
    private Verein verein;
    private Schiesstand schiesstand;

    @BeforeEach
    void init() {
        benutzer = TestDataFactory.createBenutzer(null, "benutzer@example.com");
        verein = TestDataFactory.createVerein(null, "Verein A");
        schiesstand = TestDataFactory.createSchiesstand(null, "Stand A", verein);
        em.persist(benutzer);
        em.persist(verein);
        em.persist(schiesstand);
    }

    private DigitalesZertifikat persistZertifikat(String typ, Benutzer b, Verein v, Schiesstand s, boolean widerrufen) {
        DigitalesZertifikat z = DigitalesZertifikat.builder()
                .zertifikatsTyp(typ)
                .seriennummer(java.util.UUID.randomUUID().toString())
                .subjectDN("CN=" + typ)
                .issuerDN("CN=Issuer")
                .zertifikatPEM("PEM")
                .privateKeyPEM("KEY")
                .gueltigSeit(LocalDateTime.now().minusDays(1))
                .gueltigBis(null)
                .widerrufen(widerrufen)
                .benutzer(b)
                .verein(v)
                .schiesstand(s)
                .build();
        em.persist(z);
        return z;
    }

    @Test
    void testFindByZertifikatsTyp() {
        persistZertifikat("ROOT", null, null, null, false);
        Optional<DigitalesZertifikat> found = repository.findByZertifikatsTyp("ROOT");
        assertTrue(found.isPresent());
        assertEquals("ROOT", found.get().getZertifikatsTyp());
    }

    @Test
    void testFindByBenutzer() {
        DigitalesZertifikat z = persistZertifikat("AUFSEHER", benutzer, null, null, false);
        Optional<DigitalesZertifikat> found = repository.findByBenutzer(benutzer);
        assertTrue(found.isPresent());
        assertEquals(z.getId(), found.get().getId());
    }

    @Test
    void testExistsByBenutzer() {
        assertFalse(repository.existsByBenutzer(benutzer));
        persistZertifikat("AUFSEHER", benutzer, null, null, false);
        assertTrue(repository.existsByBenutzer(benutzer));
    }

    @Test
    void testFindByVereinAndZertifikatsTyp() {
        DigitalesZertifikat z = persistZertifikat("VEREIN", null, verein, null, false);
        Optional<DigitalesZertifikat> found = repository.findByVereinAndZertifikatsTyp(verein, "VEREIN");
        assertTrue(found.isPresent());
        assertEquals(z.getId(), found.get().getId());
    }

    @Test
    void testExistsByVereinAndZertifikatsTyp() {
        assertFalse(repository.existsByVereinAndZertifikatsTyp(verein, "VEREIN"));
        persistZertifikat("VEREIN", null, verein, null, false);
        assertTrue(repository.existsByVereinAndZertifikatsTyp(verein, "VEREIN"));
    }

    @Test
    void testFindByBenutzerAndSchiesstand() {
        DigitalesZertifikat z = persistZertifikat("SCHIESSTANDAUFSEHER", benutzer, null, schiesstand, false);
        Optional<DigitalesZertifikat> found = repository.findByBenutzerAndSchiesstand(benutzer, schiesstand);
        assertTrue(found.isPresent());
        assertEquals(z.getId(), found.get().getId());
    }

    @Test
    void testExistsByBenutzerAndSchiesstand() {
        assertFalse(repository.existsByBenutzerAndSchiesstand(benutzer, schiesstand));
        persistZertifikat("SCHIESSTANDAUFSEHER", benutzer, null, schiesstand, false);
        assertTrue(repository.existsByBenutzerAndSchiesstand(benutzer, schiesstand));
    }

    @Test
    void testFindAllGueltigeWithDetailsAndMitgliedschaften() {
        persistZertifikat("VEREIN", null, verein, null, false);
        persistZertifikat("AUFSEHER", benutzer, verein, null, false);
        persistZertifikat("SCHIESSTANDAUFSEHER", benutzer, null, schiesstand, true); // widerrufen -> nicht gültig
        List<DigitalesZertifikat> list = repository.findAllGueltigeWithDetailsAndMitgliedschaften();
        assertEquals(2, list.size());
    }

    @Test
    void testFindAllWiderrufeneWithDetailsAndMitgliedschaften() {
        persistZertifikat("VEREIN", null, verein, null, true);
        persistZertifikat("AUFSEHER", benutzer, verein, null, true);
        persistZertifikat("SCHIESSTANDAUFSEHER", benutzer, null, schiesstand, false);
        List<DigitalesZertifikat> list = repository.findAllWiderrufeneWithDetailsAndMitgliedschaften();
        assertEquals(2, list.size());
    }

    @Test
    void testFindBySeriennummerWithDetails() {
        DigitalesZertifikat z = persistZertifikat("VEREIN", null, verein, null, false);
        Optional<DigitalesZertifikat> found = repository.findBySeriennummerWithDetails(z.getSeriennummer());
        assertTrue(found.isPresent());
        assertEquals(z.getSeriennummer(), found.get().getSeriennummer());
        assertEquals(verein.getName(), found.get().getVerein().getName());
    }
}

