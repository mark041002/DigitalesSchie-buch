package de.suchalla.schiessbuch.config;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import de.suchalla.schiessbuch.repository.*;
import de.suchalla.schiessbuch.service.PkiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initialisiert die Datenbank mit Testdaten beim Anwendungsstart.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final BenutzerRepository benutzerRepository;
    private final VerbandRepository verbandRepository;
    private final VereinRepository vereinRepository;
    private final DisziplinRepository disziplinRepository;
    private final SchiesstandRepository schiesstandRepository;
    private final VereinsmitgliedschaftRepository mitgliedschaftRepository;
    private final PasswordEncoder passwordEncoder;
    private final PkiService pkiService;
    private final SchiessnachweisEintragRepository eintragRepository;

    @Override
    public void run(String... args) {
        // Prüfen ob bereits Daten vorhanden sind
        if (benutzerRepository.count() > 0) {
            log.info("Datenbank enthält bereits Daten. Initialisierung übersprungen.");
            return;
        }

        log.info("Initialisiere Datenbank mit Testdaten...");

        try {
            // Admin-Benutzer erstellen
            Benutzer admin = Benutzer.builder()
                    .email("admin@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("System")
                    .nachname("Administrator")
                    .rolle(BenutzerRolle.ADMIN)
                    .emailVerifiziert(true)
                    .emailNotificationsEnabled(true)
                    .build();
            benutzerRepository.save(admin);
            log.info("Admin-Benutzer erstellt: admin@test.de / test123");

            // Testbenutzer erstellen
            Benutzer schuetze = Benutzer.builder()
                    .email("schuetze@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Max")
                    .nachname("Mustermann")
                    .rolle(BenutzerRolle.SCHUETZE)
                    .emailVerifiziert(true)
                    .emailNotificationsEnabled(true)
                    .build();
            benutzerRepository.save(schuetze);
            log.info("Test-Schütze erstellt: schuetze@test.de / test123");

            Benutzer aufseher = Benutzer.builder()
                    .email("aufseher@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Klaus")
                    .nachname("Aufseher")
                    .rolle(BenutzerRolle.AUFSEHER)
                    .emailVerifiziert(true)
                    .emailNotificationsEnabled(true)
                    .build();
            benutzerRepository.save(aufseher);

            log.info("Test-Aufseher erstellt: aufseher@test.de / test123");

            Benutzer schiesstandAufseher = Benutzer.builder()
                    .email("standaufseher@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Peter")
                    .nachname("Standaufseher")
                    .rolle(BenutzerRolle.SCHIESSSTAND_AUFSEHER)
                    .emailVerifiziert(true)
                    .emailNotificationsEnabled(true)
                    .build();
            benutzerRepository.save(schiesstandAufseher);
            log.info("Test-Schießstandaufseher erstellt: standaufseher@test.de / test123");

            Benutzer vereinschef = Benutzer.builder()
                    .email("vereinschef@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Werner")
                    .nachname("Vereinschef")
                    .rolle(BenutzerRolle.VEREINS_CHEF)
                    .emailVerifiziert(true)
                    .emailNotificationsEnabled(true)
                    .build();
            benutzerRepository.save(vereinschef);
            log.info("Test-Vereinschef erstellt: vereinschef@test.de / test123");

            // Verband erstellen
            Verband dsb = Verband.builder()
                    .name("Deutscher Schützenbund")
                    .beschreibung("Dachverband der deutschen Sportvereine")
                    .build();
            verbandRepository.save(dsb);
            log.info("Verband erstellt: {}", dsb.getName());

            // Verbands-Disziplinen erstellen
            String[] verbandsDisziplinen = {
                    "Luftgewehr 10m",
                    "Luftpistole 10m",
                    "Kleinkaliber 50m liegend",
                    "Sportpistole 25m",
                    "Großkaliber 100m",
                    "KK-Gewehr 50m Dreistellungskampf"
            };

            int idx = 1;
            for (String name : verbandsDisziplinen) {
                Disziplin disziplin = Disziplin.builder()
                        .kennziffer("K-" + idx)
                        .programm(name)
                        .verband(dsb)
                        .waffeKlasse(null)
                        .build();
                disziplinRepository.save(disziplin);
                idx++;
            }
            log.info("{} Verbands-Disziplinen erstellt", verbandsDisziplinen.length);

            // Verein erstellen
                Verein verein = Verein.builder()
                    .name("SV Teststadt")
                    .verbaende(java.util.Collections.singleton(dsb))
                    .adresse("Musterstraße 1, 12345 Teststadt")
                    .build();
            vereinRepository.save(verein);
            log.info("Verein erstellt: {}", verein.getName());
                log.info("Vereins-ID: {}", verein.getId());

            // Verbands-Schießstand erstellen (ohne Vereinszuordnung)
            Schiesstand verbandsSchiesstand = Schiesstand.builder()
                    .name("DSB Bundesleistungszentrum")
                    .typ(SchiesstandTyp.GEWERBLICH)
                    .adresse("Olympiastraße 10, 80809 München")
                    .aufseher(schiesstandAufseher)
                    .build();
            schiesstandRepository.save(verbandsSchiesstand);
            log.info("Verbands-Schießstand erstellt: {}", verbandsSchiesstand.getName());

            // Schießstand erstellen
            Schiesstand schiesstand = Schiesstand.builder()
                    .name("Vereinsanlage Teststadt")
                    .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                    .verein(verein)
                    .adresse("Am Schießstand 10, 12345 Teststadt")
                    .aufseher(vereinschef)
                    .build();
            schiesstandRepository.save(schiesstand);
            log.info("Schießstand erstellt: {}", schiesstand.getName());

            // Gewerblicher Schießstand
            Schiesstand gewerblich = Schiesstand.builder()
                    .name("Schießsportzentrum Musterstadt")
                    .typ(SchiesstandTyp.GEWERBLICH)
                    .adresse("Industriestraße 50, 54321 Musterstadt")
                    .build();
            schiesstandRepository.save(gewerblich);
            log.info("Gewerblicher Schießstand erstellt: {}", gewerblich.getName());

            // Vereinsmitgliedschaften erstellen
            // Vereinschef-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftChef = Vereinsmitgliedschaft.builder()
                    .benutzer(vereinschef)
                    .verein(verein)
                    .status(MitgliedschaftsStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(2))
                    .istVereinschef(true)
                    .istAufseher(false)
                    .aktiv(true)
                    .build();
                mitgliedschaftRepository.save(mitgliedschaftChef);
                // Bi-direktionale Zuordnung pflegen, damit Benutzersammlung und Vereinsammlung gefüllt sind
                vereinschef.getVereinsmitgliedschaften().add(mitgliedschaftChef);
                verein.getMitgliedschaften().add(mitgliedschaftChef);
                benutzerRepository.save(vereinschef);
                vereinRepository.save(verein);
            log.info("Vereinschef-Mitgliedschaft erstellt für: {}", vereinschef.getVollstaendigerName());

            // Aufseher-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftAufseher = Vereinsmitgliedschaft.builder()
                    .benutzer(aufseher)
                    .verein(verein)
                    .status(MitgliedschaftsStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(3))
                    .istVereinschef(false)
                    .istAufseher(true)
                    .aktiv(true)
                    .build();
                mitgliedschaftRepository.save(mitgliedschaftAufseher);
                aufseher.getVereinsmitgliedschaften().add(mitgliedschaftAufseher);
                verein.getMitgliedschaften().add(mitgliedschaftAufseher);
                benutzerRepository.save(aufseher);
                vereinRepository.save(verein);
            log.info("Aufseher-Mitgliedschaft erstellt für: {}", aufseher.getVollstaendigerName());

            // Schießstandaufseher-Mitgliedschaft (nicht als Vereinsaufseher, nur als Schießstandaufseher!)
            Vereinsmitgliedschaft mitgliedschaftSchiesstandAufseher = Vereinsmitgliedschaft.builder()
                    .benutzer(schiesstandAufseher)
                    .verein(verein)
                    .status(MitgliedschaftsStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(2))
                    .istVereinschef(false)
                    .istAufseher(false)
                    .aktiv(true)
                    .build();
                mitgliedschaftRepository.save(mitgliedschaftSchiesstandAufseher);
                schiesstandAufseher.getVereinsmitgliedschaften().add(mitgliedschaftSchiesstandAufseher);
                verein.getMitgliedschaften().add(mitgliedschaftSchiesstandAufseher);
                benutzerRepository.save(schiesstandAufseher);
                vereinRepository.save(verein);
            log.info("Schießstandaufseher-Mitgliedschaft erstellt für: {}", schiesstandAufseher.getVollstaendigerName());

            // Schützen-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftSchuetze = Vereinsmitgliedschaft.builder()
                    .benutzer(schuetze)
                    .verein(verein)
                    .status(MitgliedschaftsStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(1))
                    .istVereinschef(false)
                    .istAufseher(false)
                    .aktiv(true)
                    .build();
                mitgliedschaftRepository.save(mitgliedschaftSchuetze);
                schuetze.getVereinsmitgliedschaften().add(mitgliedschaftSchuetze);
                verein.getMitgliedschaften().add(mitgliedschaftSchuetze);
                benutzerRepository.save(schuetze);
                vereinRepository.save(verein);
            log.info("Schützen-Mitgliedschaft erstellt für: {}", schuetze.getVollstaendigerName());

            // PKI-Zertifikate erstellen
            log.info("Erstelle PKI-Zertifikate...");

            // Variablen hier deklarieren, damit wir sie später den Einträgen zuweisen können
            DigitalesZertifikat vereinsZertifikat = null;
            DigitalesZertifikat aufseherZertifikat = null;
            DigitalesZertifikat vereinschefZertifikat = null;
            DigitalesZertifikat schiesstandAufseherZertifikat = null;

            // Vereinszertifikat erstellen
            try {
                vereinsZertifikat = pkiService.createVereinCertificate(verein);
                log.info("Vereinszertifikat erstellt für: {} (SN: {})", verein.getName(), vereinsZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Vereinszertifikats", e);
            }

            // Aufseher-Zertifikat erstellen
            try {
                aufseherZertifikat = pkiService.createAufseherCertificate(aufseher, verein);
                log.info("Aufseher-Zertifikat erstellt für: {} (SN: {})", aufseher.getVollstaendigerName(), aufseherZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Aufseher-Zertifikats", e);
            }

            // Vereinschef-Zertifikat erstellen (da Vereinschefs auch Aufseher sind)
            try {
                vereinschefZertifikat = pkiService.createAufseherCertificate(vereinschef, verein);
                log.info("Vereinschef-Zertifikat erstellt für: {} (SN: {})", vereinschef.getVollstaendigerName(), vereinschefZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Vereinschef-Zertifikats", e);
            }

            // Schießstandaufseher-Zertifikat erstellen (für Verbands-Schießstand)
            try {
                schiesstandAufseherZertifikat = pkiService.createSchiesstandaufseheCertificate(schiesstandAufseher, verbandsSchiesstand);
                log.info("Schießstandaufseher-Zertifikat erstellt für: {} (SN: {})", schiesstandAufseher.getVollstaendigerName(), schiesstandAufseherZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Schießstandaufseher-Zertifikats", e);
            }

            log.info("Datenbank-Initialisierung abgeschlossen!");
            log.info("===============================================");
            log.info("Login-Daten:");
            log.info("  Admin:               admin@test.de / test123");
            log.info("  Schütze:             schuetze@test.de / test123");
            log.info("  Aufseher:            aufseher@test.de / test123");
            log.info("  Schießstandaufseher: standaufseher@test.de / test123");
            log.info("  Vereinschef:         vereinschef@test.de / test123");
            log.info("===============================================");
            log.info("PKI-Zertifikate wurden erstellt");
            log.info("===============================================");

            // Zwei signierte Einträge für den Schützen anlegen
            Disziplin disziplin1 = disziplinRepository.findAll().get(0);
            Disziplin disziplin2 = disziplinRepository.findAll().get(1);

            SchiessnachweisEintrag eintragVomAufseher = SchiessnachweisEintrag.builder()
                    .schuetze(schuetze)
                    .disziplin(disziplin1)
                    .schiesstand(schiesstand)
                    .datum(LocalDate.now().minusDays(10))
                    .kaliber(".22lr")
                    .waffenart("Gewehr")
                    .anzahlSchuesse(30)
                    .ergebnis("285 Ringe")
                    .status(de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)
                    .aufseher(aufseher)
                    .signiertAm(java.time.LocalDateTime.now().minusDays(9))
                    .build();
            schiesstandRepository.save(schiesstand); // falls nicht persistiert
            // Eintrag signieren und Zertifikat zuweisen, falls Aufseher-Zertifikat vorhanden
            if (aufseherZertifikat != null) {
                String dataToSign = buildSigningPayload(eintragVomAufseher);
                String signature = pkiService.signData(dataToSign, aufseherZertifikat);
                eintragVomAufseher.setZertifikat(aufseherZertifikat);
                eintragVomAufseher.setDigitaleSignatur(signature);
                eintragVomAufseher.setIstSigniert(true);
            }
            // Eintrag speichern
            eintragRepository.save(eintragVomAufseher);

            SchiessnachweisEintrag eintragVomVereinschef = SchiessnachweisEintrag.builder()
                    .schuetze(schuetze)
                    .disziplin(disziplin2)
                    .schiesstand(schiesstand)
                    .datum(LocalDate.now().minusDays(5))
                    .kaliber("9mm")
                    .waffenart("Pistole")
                    .anzahlSchuesse(20)
                    .ergebnis("180 Ringe")
                    .status(de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)
                    .aufseher(vereinschef)
                    .signiertAm(java.time.LocalDateTime.now().minusDays(4))
                    .build();
            if (vereinschefZertifikat != null) {
                String dataToSign = buildSigningPayload(eintragVomVereinschef);
                String signature = pkiService.signData(dataToSign, vereinschefZertifikat);
                eintragVomVereinschef.setZertifikat(vereinschefZertifikat);
                eintragVomVereinschef.setDigitaleSignatur(signature);
                eintragVomVereinschef.setIstSigniert(true);
            }
            eintragRepository.save(eintragVomVereinschef);
            log.info("Zwei signierte Einträge für Schütze wurden erstellt.");
            log.info("===============================================");

            // 20 signierte Einträge für den Schützen anlegen (10 vom Aufseher, 10 vom Vereinschef)
            java.util.List<Disziplin> disziplinen = disziplinRepository.findAll();
            for (int i = 0; i < 10; i++) {
                SchiessnachweisEintrag eintragVomAufseherLoop = SchiessnachweisEintrag.builder()
                        .schuetze(schuetze)
                        .disziplin(disziplinen.get(i % disziplinen.size()))
                        .schiesstand(schiesstand)
                        .datum(LocalDate.now().minusDays(20 - i))
                        .kaliber(".22lr")
                        .waffenart("Gewehr")
                        .anzahlSchuesse(30 + i)
                        .ergebnis((280 + i) + " Ringe")
                        .status(de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)
                        .aufseher(aufseher)
                        .signiertAm(java.time.LocalDateTime.now().minusDays(19 - i))
                        .build();
                if (aufseherZertifikat != null) {
                    String dataToSign = buildSigningPayload(eintragVomAufseherLoop);
                    String signature = pkiService.signData(dataToSign, aufseherZertifikat);
                    eintragVomAufseherLoop.setZertifikat(aufseherZertifikat);
                    eintragVomAufseherLoop.setDigitaleSignatur(signature);
                    eintragVomAufseherLoop.setIstSigniert(true);
                }
                eintragRepository.save(eintragVomAufseherLoop);
            }
            for (int i = 0; i < 10; i++) {
                SchiessnachweisEintrag eintragVomVereinschefLoop = SchiessnachweisEintrag.builder()
                        .schuetze(schuetze)
                        .disziplin(disziplinen.get((i + 1) % disziplinen.size()))
                        .schiesstand(schiesstand)
                        .datum(LocalDate.now().minusDays(10 - i))
                        .kaliber("9mm")
                        .waffenart("Pistole")
                        .anzahlSchuesse(20 + i)
                        .ergebnis((170 + i) + " Ringe")
                        .status(de.suchalla.schiessbuch.model.enums.EintragStatus.SIGNIERT)
                        .aufseher(vereinschef)
                        .signiertAm(java.time.LocalDateTime.now().minusDays(9 - i))
                        .build();
                if (vereinschefZertifikat != null) {
                    String dataToSign = buildSigningPayload(eintragVomVereinschefLoop);
                    String signature = pkiService.signData(dataToSign, vereinschefZertifikat);
                    eintragVomVereinschefLoop.setZertifikat(vereinschefZertifikat);
                    eintragVomVereinschefLoop.setDigitaleSignatur(signature);
                    eintragVomVereinschefLoop.setIstSigniert(true);
                }
                eintragRepository.save(eintragVomVereinschefLoop);
            }
            log.info("20 signierte Einträge für Schütze wurden erstellt.");
            log.info("===============================================");

        
        } catch (Exception e) {
            log.error("Fehler bei der Datenbank-Initialisierung", e);
        }
    }

    // Hilfsmethode zum Erzeugen einer konsistenten Signier-Payload aus einem Eintrag
    private String buildSigningPayload(SchiessnachweisEintrag eintrag) {
        StringBuilder sb = new StringBuilder();
        sb.append(eintrag.getSchuetze() != null ? eintrag.getSchuetze().getEmail() : "")
            .append('|')
            .append(eintrag.getDatum() != null ? eintrag.getDatum().toString() : "")
            .append('|')
            .append(eintrag.getDisziplin() != null ? eintrag.getDisziplin().getKennziffer() : "")
            .append('|')
            .append(eintrag.getSchiesstand() != null ? eintrag.getSchiesstand().getName() : "")
            .append('|')
            .append(eintrag.getErgebnis() != null ? eintrag.getErgebnis() : "")
            .append('|')
            .append(eintrag.getAufseher() != null ? eintrag.getAufseher().getEmail() : "");
        return sb.toString();
    }

}
