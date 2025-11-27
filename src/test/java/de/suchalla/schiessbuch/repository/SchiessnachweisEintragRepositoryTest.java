package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository-Tests für SchiessnachweisEintragRepository mit H2 In-Memory-Datenbank.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
class SchiessnachweisEintragRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SchiessnachweisEintragRepository eintragRepository;

    private Benutzer schuetze;
    private Benutzer aufseher;
    private Disziplin disziplin;
    private Schiesstand schiesstand;
    private Verein verein;
    private Verband verband;

    @BeforeEach
    void setUp() {
        // Verband erstellen
        verband = Verband.builder()
                .name("Deutscher Schützenbund")
                .build();
        entityManager.persist(verband);

        // Verein erstellen
        verein = Verein.builder()
                .name("Testverein")
                .build();
        verein.getVerbaende().add(verband);
        entityManager.persist(verein);

        // Schütze erstellen
        schuetze = Benutzer.builder()
                .email("schuetze@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();
        entityManager.persist(schuetze);

        // Aufseher erstellen
        aufseher = Benutzer.builder()
                .email("aufseher@example.com")
                .vorname("Hans")
                .nachname("Schmidt")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();
        entityManager.persist(aufseher);

        // Disziplin erstellen
        disziplin = Disziplin.builder()
            .kennziffer("LG-10m")
            .programm("Standard-Disziplin")
            .build();
        entityManager.persist(disziplin);

        // Schießstand erstellen
        schiesstand = Schiesstand.builder()
                .name("Stand 1")
                .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                .adresse("Teststraße 1")
                .verein(verein)
                .build();
        entityManager.persist(schiesstand);

        entityManager.flush();
    }

    @Test
    void testSaveSchiessnachweisEintrag() {
        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .kaliber("4.5mm")
                .status(EintragStatus.OFFEN)
                .build();

        SchiessnachweisEintrag saved = eintragRepository.save(eintrag);

        assertNotNull(saved.getId());
        assertEquals(schuetze, saved.getSchuetze());
        assertEquals(disziplin, saved.getDisziplin());
        assertEquals(schiesstand, saved.getSchiesstand());
        assertEquals(EintragStatus.OFFEN, saved.getStatus());
    }

    @Test
    void testFindById() {
        SchiessnachweisEintrag eintrag = createAndPersistEintrag(
                LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);

        Optional<SchiessnachweisEintrag> found = eintragRepository.findById(eintrag.getId());

        assertTrue(found.isPresent());
        assertEquals(eintrag.getId(), found.get().getId());
        // EntityGraph sollte alle Beziehungen laden
        assertNotNull(found.get().getSchuetze());
        assertNotNull(found.get().getDisziplin());
        assertNotNull(found.get().getSchiesstand());
        assertNotNull(found.get().getSchiesstand().getVerein());
    }

    @Test
    void testFindBySchuetze() {
        createAndPersistEintrag(LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(LocalDate.now().minusDays(1), 30, "290 Ringe", EintragStatus.SIGNIERT);

        List<SchiessnachweisEintrag> eintraege = eintragRepository.findBySchuetze(schuetze);

        assertEquals(2, eintraege.size());
        eintraege.forEach(e -> assertEquals(schuetze.getId(), e.getSchuetze().getId()));
    }

    @Test
    void testFindBySchuetzeAndDatumBetween() {
        LocalDate heute = LocalDate.now();
        LocalDate gestern = heute.minusDays(1);
        LocalDate vorgestern = heute.minusDays(2);
        LocalDate vor10Tagen = heute.minusDays(10);

        createAndPersistEintrag(heute, 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(gestern, 30, "290 Ringe", EintragStatus.SIGNIERT);
        createAndPersistEintrag(vor10Tagen, 35, "330 Ringe", EintragStatus.OFFEN);

        List<SchiessnachweisEintrag> eintraege = 
            eintragRepository.findBySchuetzeAndDatumBetween(schuetze, vorgestern, heute);

        assertEquals(2, eintraege.size());
        assertTrue(eintraege.stream()
                .allMatch(e -> !e.getDatum().isBefore(vorgestern) && !e.getDatum().isAfter(heute)));
    }

    @Test
    void testFindBySchuetzeAndDatumBetweenAndStatus() {
        LocalDate heute = LocalDate.now();
        LocalDate gestern = heute.minusDays(1);
        LocalDate vorgestern = heute.minusDays(2);

        createAndPersistEintrag(heute, 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(gestern, 30, "290 Ringe", EintragStatus.SIGNIERT);
        createAndPersistEintrag(vorgestern, 35, "330 Ringe", EintragStatus.SIGNIERT);

        List<SchiessnachweisEintrag> signierteEintraege = 
            eintragRepository.findBySchuetzeAndDatumBetweenAndStatus(
                schuetze, vorgestern, heute, EintragStatus.SIGNIERT);

        assertEquals(2, signierteEintraege.size());
        assertTrue(signierteEintraege.stream()
                .allMatch(e -> e.getStatus() == EintragStatus.SIGNIERT));
    }

    @Test
    void testFindBySchiesstand() {
        createAndPersistEintrag(LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(LocalDate.now().minusDays(1), 30, "290 Ringe", EintragStatus.SIGNIERT);

        List<SchiessnachweisEintrag> eintraege = eintragRepository.findBySchiesstand(schiesstand);

        assertEquals(2, eintraege.size());
        eintraege.forEach(e -> assertEquals(schiesstand.getId(), e.getSchiesstand().getId()));
    }

    @Test
    void testFindBySchiesstandAndStatus() {
        createAndPersistEintrag(LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(LocalDate.now().minusDays(1), 30, "290 Ringe", EintragStatus.SIGNIERT);
        createAndPersistEintrag(LocalDate.now().minusDays(2), 35, "330 Ringe", EintragStatus.OFFEN);

        List<SchiessnachweisEintrag> offeneEintraege = 
            eintragRepository.findBySchiesstandAndStatus(schiesstand, EintragStatus.OFFEN);

        assertEquals(2, offeneEintraege.size());
        assertTrue(offeneEintraege.stream()
                .allMatch(e -> e.getStatus() == EintragStatus.OFFEN));
    }

    @Test
    void testCountBySchuetzeAndStatus() {
        createAndPersistEintrag(LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);
        createAndPersistEintrag(LocalDate.now().minusDays(1), 30, "290 Ringe", EintragStatus.SIGNIERT);
        createAndPersistEintrag(LocalDate.now().minusDays(2), 35, "330 Ringe", EintragStatus.SIGNIERT);
        createAndPersistEintrag(LocalDate.now().minusDays(3), 25, "240 Ringe", EintragStatus.ABGELEHNT);

        long signierteCount = eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.SIGNIERT);
        long offeneCount = eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.OFFEN);
        long abgelehntCount = eintragRepository.countBySchuetzeAndStatus(schuetze, EintragStatus.ABGELEHNT);

        assertEquals(2, signierteCount);
        assertEquals(1, offeneCount);
        assertEquals(1, abgelehntCount);
    }

    @Test
    void testEntityGraphLoadsAllRelations() {
        SchiessnachweisEintrag eintrag = createAndPersistEintrag(
                LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);

        entityManager.clear(); // Cache leeren

        Optional<SchiessnachweisEintrag> found = eintragRepository.findById(eintrag.getId());

        assertTrue(found.isPresent());
        SchiessnachweisEintrag loaded = found.get();

        // Teste ob alle Beziehungen ohne LazyInitializationException zugreifbar sind
        assertNotNull(loaded.getSchuetze());
        assertNotNull(loaded.getSchuetze().getVorname());
        
        assertNotNull(loaded.getDisziplin());
        assertNotNull(loaded.getDisziplin().getKennziffer());
        
        assertNotNull(loaded.getSchiesstand());
        assertNotNull(loaded.getSchiesstand().getName());
        
        // Wichtig: Verein über Schießstand sollte auch geladen sein
        assertNotNull(loaded.getSchiesstand().getVerein());
        assertNotNull(loaded.getSchiesstand().getVerein().getName());
    }

    @Test
    void testSignierterEintragMitZertifikat() {
        // Erstelle Zertifikat
        DigitalesZertifikat zertifikat = DigitalesZertifikat.builder()
                .zertifikatsTyp("AUFSEHER")
                .seriennummer("ABC123")
                .subjectDN("CN=Hans Schmidt")
                .issuerDN("CN=Root CA")
                .zertifikatPEM("-----BEGIN CERTIFICATE-----")
                .privateKeyPEM("-----BEGIN PRIVATE KEY-----")
                .gueltigSeit(LocalDateTime.now())
                .widerrufen(false)
                .benutzer(aufseher)
                .verein(verein)
                .build();
        entityManager.persist(zertifikat);

        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .kaliber("4.5mm")
                .status(EintragStatus.SIGNIERT)
                .aufseher(aufseher)
                .signiertAm(LocalDateTime.now())
                .digitaleSignatur("SignaturHash123")
                .zertifikat(zertifikat)
                .build();

        SchiessnachweisEintrag saved = eintragRepository.save(eintrag);

        assertNotNull(saved.getZertifikat());
        assertEquals(zertifikat.getSeriennummer(), saved.getZertifikat().getSeriennummer());
        assertEquals(aufseher.getId(), saved.getAufseher().getId());
    }

    @Test
    void testAbgelehnterEintragMitGrund() {
        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .status(EintragStatus.ABGELEHNT)
                .ablehnungsgrund("Fehlende Dokumentation")
                .build();

        SchiessnachweisEintrag saved = eintragRepository.save(eintrag);

        assertEquals(EintragStatus.ABGELEHNT, saved.getStatus());
        assertEquals("Fehlende Dokumentation", saved.getAblehnungsgrund());
    }

    @Test
    void testUpdateEintrag() {
        SchiessnachweisEintrag eintrag = createAndPersistEintrag(
                LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);

        eintrag.setErgebnis("390 Ringe");
        eintrag.setAnzahlSchuesse(45);
        SchiessnachweisEintrag updated = eintragRepository.save(eintrag);

        assertEquals("390 Ringe", updated.getErgebnis());
        assertEquals(45, updated.getAnzahlSchuesse());
    }

    @Test
    void testDeleteEintrag() {
        SchiessnachweisEintrag eintrag = createAndPersistEintrag(
                LocalDate.now(), 40, "380 Ringe", EintragStatus.OFFEN);
        Long id = eintrag.getId();

        eintragRepository.delete(eintrag);
        entityManager.flush();

        assertFalse(eintragRepository.findById(id).isPresent());
    }

    @Test
    void testFindBySchuetzeEmptyResult() {
        Benutzer andererSchuetze = Benutzer.builder()
                .email("anderer@example.com")
                .vorname("Peter")
                .nachname("Pan")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();
        entityManager.persist(andererSchuetze);

        List<SchiessnachweisEintrag> eintraege = eintragRepository.findBySchuetze(andererSchuetze);

        assertTrue(eintraege.isEmpty());
    }

    // Hilfsmethode zum Erstellen und Persistieren von Einträgen
    private SchiessnachweisEintrag createAndPersistEintrag(
            LocalDate datum, Integer anzahlSchuesse, String ergebnis, EintragStatus status) {
        SchiessnachweisEintrag eintrag = SchiessnachweisEintrag.builder()
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(datum)
                .anzahlSchuesse(anzahlSchuesse)
                .ergebnis(ergebnis)
                .kaliber("4.5mm")
                .waffenart("Luftgewehr")
                .status(status)
                .build();
        
        entityManager.persist(eintrag);
        entityManager.flush();
        return eintrag;
    }
}
