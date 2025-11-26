package de.suchalla.schiessbuch.testutil;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Zentrale Factory für wiederverwendbare Testdaten.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    public static Benutzer createBenutzer(Long id, String email) {
        return Benutzer.builder()
                .id(id)
                .email(email)
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("pw")
                .rolle(BenutzerRolle.SCHUETZE)
                .emailNotificationsEnabled(true)
                .emailVerifiziert(false)
                .build();
    }

    public static Verein createVerein(Long id, String name) {
        return Verein.builder()
                .id(id)
                .name(name)
                .adresse("Adresse " + name)
                .beschreibung("Beschreibung " + name)
                .build();
    }

    public static Verband createVerband(Long id, String name) {
        return Verband.builder()
                .id(id)
                .name(name)
                .beschreibung("Beschreibung " + name)
                .build();
    }

    public static Schiesstand createSchiesstand(Long id, String name, Verein verein) {
        return Schiesstand.builder()
                .id(id)
                .name(name)
                .typ(SchiesstandTyp.VEREINSGEBUNDEN)
                .verein(verein)
                .adresse("Adresse " + name)
                .beschreibung("Beschreibung " + name)
                .build();
    }

    public static Disziplin createDisziplin(Long id, String name, Verband verband) {
        return Disziplin.builder()
                .id(id)
                .kennziffer(name)
                .programm("Beschreibung " + name)
                .verband(verband)
                .waffeKlasse(null)
                .build();
    }

    public static DigitalesZertifikat createZertifikat(Long id, String typ, Benutzer benutzer) {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        return DigitalesZertifikat.builder()
                .id(id)
                .zertifikatsTyp(typ)
                .seriennummer(UUID.randomUUID().toString().replace("-",""))
                .subjectDN("CN=" + benutzer.getEmail())
                .issuerDN("CN=Issuer")
                .zertifikatPEM("PEM")
                .privateKeyPEM("PRIVATE_KEY")
                .gueltigSeit(now)
                .gueltigBis(null)
                .widerrufen(false)
                .benutzer(benutzer)
                .build();
    }

    public static Vereinsmitgliedschaft createMitgliedschaft(Long id, Benutzer benutzer, Verein verein, MitgliedschaftsStatus status) {
        return Vereinsmitgliedschaft.builder()
                .id(id)
                .benutzer(benutzer)
                .verein(verein)
                .status(status)
                .beitrittDatum(LocalDate.now().minusDays(10))
                .aktiv(status == MitgliedschaftsStatus.AKTIV)
                .istAufseher(false)
                .istVereinschef(false)
                .build();
    }

    public static SchiessnachweisEintrag createEintrag(Long id, Benutzer schuetze, Disziplin disziplin, Schiesstand schiesstand, LocalDate datum) {
        return SchiessnachweisEintrag.builder()
                .id(id)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(datum)
                .anzahlSchuesse(30)
                .ergebnis("250")
                .kaliber("9mm")
                .waffenart("Pistole")
                .status(EintragStatus.UNSIGNIERT)
                .build();
    }
}
