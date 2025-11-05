package de.suchalla.schiessbuch.config;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.MitgliedschaftStatus;
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
                    .email("admin@schiessbuch.de")
                    .passwort(passwordEncoder.encode("admin123"))
                    .vorname("System")
                    .nachname("Administrator")
                    .rolle(BenutzerRolle.ADMIN)
                    .aktiv(true)
                    .build();
            benutzerRepository.save(admin);
            log.info("Admin-Benutzer erstellt: admin@schiessbuch.de / admin123");

            // Testbenutzer erstellen
            Benutzer schuetze = Benutzer.builder()
                    .email("schuetze@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Max")
                    .nachname("Mustermann")
                    .rolle(BenutzerRolle.SCHUETZE)
                    .aktiv(true)
                    .build();
            benutzerRepository.save(schuetze);
            log.info("Test-Schütze erstellt: schuetze@test.de / test123");

            Benutzer aufseher = Benutzer.builder()
                    .email("aufseher@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Klaus")
                    .nachname("Aufseher")
                    .rolle(BenutzerRolle.AUFSEHER)
                    .aktiv(true)
                    .build();
            benutzerRepository.save(aufseher);
            log.info("Test-Aufseher erstellt: aufseher@test.de / test123");

            Benutzer vereinschef = Benutzer.builder()
                    .email("vereinschef@test.de")
                    .passwort(passwordEncoder.encode("test123"))
                    .vorname("Werner")
                    .nachname("Vereinschef")
                    .rolle(BenutzerRolle.VEREINS_CHEF)
                    .aktiv(true)
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

            for (String name : verbandsDisziplinen) {
                Disziplin disziplin = Disziplin.builder()
                        .name(name)
                        .verband(dsb)
                        .beschreibung("Offizielle Disziplin des " + dsb.getName())
                        .build();
                disziplinRepository.save(disziplin);
            }
            log.info("{} Verbands-Disziplinen erstellt", verbandsDisziplinen.length);

            // Verein erstellen
            Verein verein = Verein.builder()
                    .name("SV Teststadt")
                    .verband(dsb)
                    .vereinsNummer("DSB-12345")
                    .adresse("Musterstraße 1, 12345 Teststadt")
                    .build();
            vereinRepository.save(verein);
            log.info("Verein erstellt: {}", verein.getName());
            log.info("Vereins-ID: {} | Vereinsnummer: {}", verein.getId(), verein.getVereinsNummer());

            // Verbands-Schießstand erstellen (ohne Vereinszuordnung)
            Schiesstand verbandsSchiesstand = Schiesstand.builder()
                    .name("DSB Bundesleistungszentrum")
                    .typ(SchiesstandTyp.GEWERBLICH)
                    .adresse("Olympiastraße 10, 80809 München")
                    .build();
            schiesstandRepository.save(verbandsSchiesstand);
            log.info("Verbands-Schießstand erstellt: {}", verbandsSchiesstand.getName());

            // Schießstand erstellen
            Schiesstand schiesstand = Schiesstand.builder()
                    .name("Vereinsanlage Teststadt")
                    .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                    .verein(verein)
                    .adresse("Am Schießstand 10, 12345 Teststadt")
                    .build();
            schiesstandRepository.save(schiesstand);
            log.info("Schießstand erstellt: {}", schiesstand.getName());

            // Gewerblicher Schießstand
            Schiesstand gewerblich = Schiesstand.builder()
                    .name("Schießsportzentrum Musterstadt")
                    .typ(SchiesstandTyp.GEWERBLICH)
                    .verein(verein)
                    .adresse("Industriestraße 50, 54321 Musterstadt")
                    .build();
            schiesstandRepository.save(gewerblich);
            log.info("Gewerblicher Schießstand erstellt: {}", gewerblich.getName());

            // Vereinsmitgliedschaften erstellen
            // Vereinschef-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftChef = Vereinsmitgliedschaft.builder()
                    .benutzer(vereinschef)
                    .verein(verein)
                    .status(MitgliedschaftStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(2))
                    .istVereinschef(true)
                    .istAufseher(false)
                    .aktiv(true)
                    .build();
            mitgliedschaftRepository.save(mitgliedschaftChef);
            log.info("Vereinschef-Mitgliedschaft erstellt für: {}", vereinschef.getVollstaendigerName());

            // Aufseher-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftAufseher = Vereinsmitgliedschaft.builder()
                    .benutzer(aufseher)
                    .verein(verein)
                    .status(MitgliedschaftStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(3))
                    .istVereinschef(false)
                    .istAufseher(true)
                    .aktiv(true)
                    .build();
            mitgliedschaftRepository.save(mitgliedschaftAufseher);
            log.info("Aufseher-Mitgliedschaft erstellt für: {}", aufseher.getVollstaendigerName());

            // Schützen-Mitgliedschaft
            Vereinsmitgliedschaft mitgliedschaftSchuetze = Vereinsmitgliedschaft.builder()
                    .benutzer(schuetze)
                    .verein(verein)
                    .status(MitgliedschaftStatus.AKTIV)
                    .beitrittDatum(LocalDate.now().minusYears(1))
                    .istVereinschef(false)
                    .istAufseher(false)
                    .aktiv(true)
                    .build();
            mitgliedschaftRepository.save(mitgliedschaftSchuetze);
            log.info("Schützen-Mitgliedschaft erstellt für: {}", schuetze.getVollstaendigerName());

            // PKI-Zertifikate erstellen
            log.info("Erstelle PKI-Zertifikate...");

            // Vereinszertifikat erstellen
            try {
                DigitalesZertifikat vereinsZertifikat = pkiService.createVereinCertificate(verein);
                log.info("Vereinszertifikat erstellt für: {} (SN: {})", verein.getName(), vereinsZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Vereinszertifikats", e);
            }

            // Aufseher-Zertifikat erstellen
            try {
                DigitalesZertifikat aufseherZertifikat = pkiService.createAufseherCertificate(aufseher, verein);
                log.info("Aufseher-Zertifikat erstellt für: {} (SN: {})", aufseher.getVollstaendigerName(), aufseherZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Aufseher-Zertifikats", e);
            }

            // Vereinschef-Zertifikat erstellen (da Vereinschefs auch Aufseher sind)
            try {
                DigitalesZertifikat vereinschefZertifikat = pkiService.createAufseherCertificate(vereinschef, verein);
                log.info("Vereinschef-Zertifikat erstellt für: {} (SN: {})", vereinschef.getVollstaendigerName(), vereinschefZertifikat.getSeriennummer());
            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Vereinschef-Zertifikats", e);
            }

            log.info("Datenbank-Initialisierung abgeschlossen!");
            log.info("===============================================");
            log.info("Login-Daten:");
            log.info("  Admin:    admin@schiessbuch.de / admin123");
            log.info("  Schütze:  schuetze@test.de / test123");
            log.info("  Aufseher: aufseher@test.de / test123");
            log.info("  Vereinschef: vereinschef@test.de / test123");
            log.info("===============================================");
            log.info("PKI-Zertifikate wurden erstellt");
            log.info("===============================================");

        } catch (Exception e) {
            log.error("Fehler bei der Datenbank-Initialisierung", e);
        }
    }
}
