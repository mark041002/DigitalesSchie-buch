package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.EintragStatus;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.SchiessnachweisEintragRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für SignaturService.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SignaturServiceTest {

    @Mock
    private PkiService pkiService;

    @Mock
    private DigitalesZertifikatRepository zertifikatRepository;

    @Mock
    private SchiessnachweisEintragRepository eintragRepository;

    @Mock
    private SchiessnachweisService schiessnachweisService;

    @Mock
    private ZertifikatVerifizierungsService zertifikatVerifizierungsService;
    
        @Mock
        private EmailService notificationService;

    @InjectMocks
    private SignaturService signaturService;

    private Benutzer schuetze;
    private Benutzer aufseher;
    private Verein verein;
    private Disziplin disziplin;
    private Schiesstand schiesstand;
    private SchiessnachweisEintrag eintrag;
    private DigitalesZertifikat aufseherZertifikat;

    @BeforeEach
    void setUp() {
        schuetze = Benutzer.builder()
                .id(1L)
                .email("schuetze@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("password")
                .rolle(BenutzerRolle.SCHUETZE)
                .build();

        aufseher = Benutzer.builder()
                .id(2L)
                .email("aufseher@example.com")
                .vorname("Hans")
                .nachname("Schmidt")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        verein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();

        disziplin = Disziplin.builder()
                .id(1L)
                .kennziffer("LG-10m")
                .programm("Luftgewehr 10m")
                .build();

        schiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .verein(verein)
                .build();

        eintrag = SchiessnachweisEintrag.builder()
                .id(1L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(40)
                .ergebnis("380 Ringe")
                .kaliber("4.5mm")
                .waffenart("Luftgewehr")
                .status(EintragStatus.OFFEN)
                .build();

        aufseherZertifikat = DigitalesZertifikat.builder()
                .id(1L)
                .zertifikatsTyp("AUFSEHER")
                .seriennummer("ZERT123")
                .subjectDN("CN=Hans Schmidt")
                .issuerDN("CN=Testverein")
                .zertifikatPEM("-----BEGIN CERTIFICATE-----\nCERT\n-----END CERTIFICATE-----")
                .privateKeyPEM("-----BEGIN PRIVATE KEY-----\nKEY\n-----END PRIVATE KEY-----")
                .gueltigSeit(LocalDateTime.now().minusYears(1))
                .gueltigBis(null)
                .widerrufen(false)
                .benutzer(aufseher)
                .verein(verein)
                .build();
    }


    @Test
    void testSignEintrag_UsesExistingZertifikat() {
        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenReturn("MOCK_SIGNATURE_123");
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        signaturService.signEintrag(eintrag, aufseher);

        verify(pkiService, times(1)).signData(any(String.class), eq(aufseherZertifikat));
    }

    @Test
    void testSignEintrag_ThrowsExceptionWhenZertifikatInvalid() {
        DigitalesZertifikat invalidZertifikat = DigitalesZertifikat.builder()
                .id(1L)
                .zertifikatsTyp("AUFSEHER")
                .widerrufen(true) // Widerrufenes Zertifikat
                .build();

        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(invalidZertifikat));

        assertThrows(RuntimeException.class, () ->
            signaturService.signEintrag(eintrag, aufseher));

        verify(pkiService, never()).signData(any(), any());
        verify(schiessnachweisService, never()).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());
    }

    @Test
    void testSignEintrag_SetsCorrectFieldsOnEintrag() {
        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenReturn("MOCK_SIGNATURE_123");
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        signaturService.signEintrag(eintrag, aufseher);

        assertEquals(aufseher, eintrag.getAufseher());
        assertEquals("MOCK_SIGNATURE_123", eintrag.getDigitaleSignatur());
        assertEquals(aufseherZertifikat, eintrag.getZertifikat());
    }

    @Test
    void testBuildSignatureData_ContainsAllRelevantFields() {
        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> {
                    String dataToSign = invocation.getArgument(0);

                    // Prüfe ob alle wichtigen Felder in den signierten Daten enthalten sind
                    assertTrue(dataToSign.contains("ID:" + eintrag.getId()));
                    assertTrue(dataToSign.contains("Schuetze:" + schuetze.getEmail()));
                    assertTrue(dataToSign.contains("Disziplin:" + disziplin.getKennziffer()));
                    assertTrue(dataToSign.contains("Schiesstand:" + schiesstand.getName()));
                    assertTrue(dataToSign.contains("AnzahlSchuesse:40"));
                    assertTrue(dataToSign.contains("Ergebnis:380 Ringe"));
                    assertTrue(dataToSign.contains("Kaliber:4.5mm"));
                    assertTrue(dataToSign.contains("Waffenart:Luftgewehr"));

                    return "MOCK_SIGNATURE";
                });
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        signaturService.signEintrag(eintrag, aufseher);

        verify(pkiService, times(1)).signData(any(String.class), any(DigitalesZertifikat.class));
    }

    @Test
    void testSignEintrag_HandlesNullOptionalFields() {
        SchiessnachweisEintrag eintragMitNulls = SchiessnachweisEintrag.builder()
                .id(2L)
                .schuetze(schuetze)
                .disziplin(disziplin)
                .schiesstand(schiesstand)
                .datum(LocalDate.now())
                .anzahlSchuesse(null)
                .ergebnis(null)
                .kaliber(null)
                .waffenart(null)
                .status(EintragStatus.OFFEN)
                .build();

        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenReturn("MOCK_SIGNATURE_123");
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        assertDoesNotThrow(() ->
            signaturService.signEintrag(eintragMitNulls, aufseher));
    }

    @Test
    void testSignEintrag_ThrowsRuntimeExceptionOnPkiError() {
        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenThrow(new RuntimeException("PKI Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            signaturService.signEintrag(eintrag, aufseher));

        assertTrue(exception.getMessage().contains("Eintrag konnte nicht signiert werden"));
        verify(schiessnachweisService, never()).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());
    }

    @Test
    void testSignEintrag_CallsSchiessnachweisServiceWithCorrectParameters() {
        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenReturn("SIGNATURE_XYZ");
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        signaturService.signEintrag(eintrag, aufseher);

        verify(schiessnachweisService, times(1))
                .signiereEintrag(eq(eintrag), eq(aufseher), eq("SIGNATURE_XYZ"));
    }

    @Test
    void testSignEintrag_WithAlreadySignedEintrag() {
        DigitalesZertifikat oldZertifikat = DigitalesZertifikat.builder()
                .id(999L)
                .seriennummer("OLD_ZERT")
                .build();

        eintrag.setZertifikat(oldZertifikat);
        eintrag.setDigitaleSignatur("OLD_SIGNATURE");
        eintrag.setAufseher(aufseher);

        when(zertifikatRepository.findByBenutzer(aufseher))
                .thenReturn(Optional.of(aufseherZertifikat));
        when(pkiService.signData(any(String.class), any(DigitalesZertifikat.class)))
                .thenReturn("NEW_SIGNATURE");
        doNothing().when(schiessnachweisService).signiereEintrag(any(SchiessnachweisEintrag.class), any(), any());

        signaturService.signEintrag(eintrag, aufseher);

        // Sollte neue Signatur und neues Zertifikat setzen
        assertEquals("NEW_SIGNATURE", eintrag.getDigitaleSignatur());
        assertEquals(aufseherZertifikat, eintrag.getZertifikat());
    }
}
