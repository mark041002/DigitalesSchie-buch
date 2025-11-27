package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.*;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für PkiService.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PkiServiceTest {

    @Mock
    private DigitalesZertifikatRepository zertifikatRepository;

    @Mock
    private VereinRepository vereinRepository;

    @Mock
    private BenutzerRepository benutzerRepository;

    @Mock
    private SchiesstandRepository schiesstandRepository;

    @InjectMocks
    private PkiService pkiService;

    private Benutzer testBenutzer;
    private Verein testVerein;
    private Schiesstand testSchiesstand;
    private DigitalesZertifikat rootZertifikat;
    private DigitalesZertifikat vereinZertifikat;

    /**
     * Generiert einen validen PKCS#8 Private Key im PEM-Format für Tests
     */
    private String generateTestPrivateKeyPEM() throws Exception {
        // Füge BouncyCastle Provider hinzu falls noch nicht registriert
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        
        byte[] encoded = privateKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        
        // Formatiere Base64 in 64-Zeichen-Zeilen
        StringBuilder pem = new StringBuilder("-----BEGIN PRIVATE KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length()));
            pem.append("\n");
        }
        pem.append("-----END PRIVATE KEY-----");
        
        return pem.toString();
    }

    @BeforeEach
    void setUp() throws Exception {
        testBenutzer = Benutzer.builder()
                .id(1L)
                .email("aufseher@example.com")
                .vorname("Hans")
                .nachname("Schmidt")
                .passwort("password")
                .rolle(BenutzerRolle.AUFSEHER)
                .build();

        testVerein = Verein.builder()
                .id(1L)
                .name("Testverein")
                .build();

        testSchiesstand = Schiesstand.builder()
                .id(1L)
                .name("Stand 1")
                .verein(testVerein)
                .build();

        // Generiere valide Testschlüssel
        String rootPrivateKeyPEM = generateTestPrivateKeyPEM();
        String vereinPrivateKeyPEM = generateTestPrivateKeyPEM();

        // Simuliertes Root-Zertifikat
        rootZertifikat = DigitalesZertifikat.builder()
                .id(1L)
                .zertifikatsTyp("ROOT")
                .seriennummer("ROOT123")
                .subjectDN("CN=Digitales Schiessbuch Root CA, O=Digitales Schiessbuch, C=DE")
                .issuerDN("CN=Digitales Schiessbuch Root CA, O=Digitales Schiessbuch, C=DE")
                .zertifikatPEM("-----BEGIN CERTIFICATE-----\nMIIDXTCCAkWgAwIBAgIJAKZn\n-----END CERTIFICATE-----")
                .privateKeyPEM(rootPrivateKeyPEM)
                .widerrufen(false)
                .build();

        // Simuliertes Vereins-Zertifikat
        vereinZertifikat = DigitalesZertifikat.builder()
                .id(2L)
                .zertifikatsTyp("VEREIN")
                .seriennummer("VEREIN123")
                .subjectDN("CN=Testverein, O=Digitales Schiessbuch, OU=Verein, C=DE")
                .issuerDN(rootZertifikat.getSubjectDN())
                .zertifikatPEM("-----BEGIN CERTIFICATE-----\nMIIDXTCCAkWgAwIBAgIJAKZn\n-----END CERTIFICATE-----")
                .privateKeyPEM(vereinPrivateKeyPEM)
                .verein(testVerein)
                .parentZertifikat(rootZertifikat)
                .widerrufen(false)
                .build();
    }

    @Test
    void testInitializeRootCertificate_CreatesRootWhenNotExists() {
        when(zertifikatRepository.findByZertifikatsTyp("ROOT")).thenReturn(Optional.empty());
        when(zertifikatRepository.save(any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pkiService.initializeRootCertificate();

        verify(zertifikatRepository, times(1)).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testInitializeRootCertificate_SkipsWhenRootExists() {
        when(zertifikatRepository.findByZertifikatsTyp("ROOT"))
                .thenReturn(Optional.of(rootZertifikat));

        pkiService.initializeRootCertificate();

        verify(zertifikatRepository, never()).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateVereinCertificate_CreatesNewCertificate() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(false);
        when(zertifikatRepository.findByZertifikatsTyp("ROOT"))
                .thenReturn(Optional.of(rootZertifikat));
        when(zertifikatRepository.save(any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> {
                    DigitalesZertifikat zert = invocation.getArgument(0);
                    zert.setId(2L);
                    return zert;
                });

        DigitalesZertifikat result = pkiService.createVereinCertificate(testVerein);

        assertNotNull(result);
        assertEquals("VEREIN", result.getZertifikatsTyp());
        assertEquals(testVerein, result.getVerein());
        assertNotNull(result.getSeriennummer());
        assertNotNull(result.getZertifikatPEM());
        assertNotNull(result.getPrivateKeyPEM());
        
        verify(zertifikatRepository, times(1)).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateVereinCertificate_ReturnsExistingCertificate() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(true);
        when(zertifikatRepository.findByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(Optional.of(vereinZertifikat));

        DigitalesZertifikat result = pkiService.createVereinCertificate(testVerein);

        assertNotNull(result);
        assertEquals(vereinZertifikat.getId(), result.getId());
        verify(zertifikatRepository, never()).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateVereinCertificate_ThrowsExceptionWhenVereinNotFound() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            pkiService.createVereinCertificate(testVerein));
    }

    @Test
    void testCreateVereinCertificate_ThrowsExceptionWhenRootNotFound() {
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(false);
        when(zertifikatRepository.findByZertifikatsTyp("ROOT"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            pkiService.createVereinCertificate(testVerein));
    }

    @Test
    void testCreateAufseherCertificate_CreatesNewCertificate() {
        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByBenutzer(testBenutzer)).thenReturn(false);
        when(zertifikatRepository.findByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(Optional.of(vereinZertifikat));
        when(zertifikatRepository.save(any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> {
                    DigitalesZertifikat zert = invocation.getArgument(0);
                    zert.setId(3L);
                    return zert;
                });

        DigitalesZertifikat result = pkiService.createAufseherCertificate(testBenutzer, testVerein);

        assertNotNull(result);
        assertEquals("AUFSEHER", result.getZertifikatsTyp());
        assertEquals(testBenutzer, result.getBenutzer());
        assertEquals(testVerein, result.getVerein());
        assertNotNull(result.getSeriennummer());
        
        verify(zertifikatRepository, times(1)).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateAufseherCertificate_ReturnsExistingCertificate() {
        DigitalesZertifikat existingZert = DigitalesZertifikat.builder()
                .id(3L)
                .zertifikatsTyp("AUFSEHER")
                .benutzer(testBenutzer)
                .build();

        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByBenutzer(testBenutzer)).thenReturn(true);
        when(zertifikatRepository.findByBenutzer(testBenutzer))
                .thenReturn(Optional.of(existingZert));

        DigitalesZertifikat result = pkiService.createAufseherCertificate(testBenutzer, testVerein);

        assertNotNull(result);
        assertEquals(existingZert.getId(), result.getId());
        verify(zertifikatRepository, never()).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateAufseherCertificate_ThrowsExceptionWhenBenutzerNotFound() {
        when(benutzerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            pkiService.createAufseherCertificate(testBenutzer, testVerein));
    }

    @Test
    void testCreateSchiesstandaufseheCertificate_CreatesNewCertificate() {
        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(schiesstandRepository.findById(1L)).thenReturn(Optional.of(testSchiesstand));
        when(zertifikatRepository.existsByBenutzerAndSchiesstand(testBenutzer, testSchiesstand))
                .thenReturn(false);
        when(zertifikatRepository.findByZertifikatsTyp("ROOT"))
                .thenReturn(Optional.of(rootZertifikat));
        when(zertifikatRepository.save(any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> {
                    DigitalesZertifikat zert = invocation.getArgument(0);
                    zert.setId(4L);
                    return zert;
                });

        DigitalesZertifikat result = pkiService.createSchiesstandaufseheCertificate(
                testBenutzer, testSchiesstand);

        assertNotNull(result);
        assertEquals("SCHIESSTANDAUFSEHER", result.getZertifikatsTyp());
        assertEquals(testBenutzer, result.getBenutzer());
        assertEquals(testSchiesstand, result.getSchiesstand());
        assertNotNull(result.getSeriennummer());
        
        verify(zertifikatRepository, times(1)).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testCreateSchiesstandaufseheCertificate_ReturnsExistingCertificate() {
        DigitalesZertifikat existingZert = DigitalesZertifikat.builder()
                .id(4L)
                .zertifikatsTyp("SCHIESSTANDAUFSEHER")
                .benutzer(testBenutzer)
                .schiesstand(testSchiesstand)
                .build();

        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(schiesstandRepository.findById(1L)).thenReturn(Optional.of(testSchiesstand));
        when(zertifikatRepository.existsByBenutzerAndSchiesstand(testBenutzer, testSchiesstand))
                .thenReturn(true);
        when(zertifikatRepository.findByBenutzerAndSchiesstand(testBenutzer, testSchiesstand))
                .thenReturn(Optional.of(existingZert));

        DigitalesZertifikat result = pkiService.createSchiesstandaufseheCertificate(
                testBenutzer, testSchiesstand);

        assertNotNull(result);
        assertEquals(existingZert.getId(), result.getId());
        verify(zertifikatRepository, never()).save(any(DigitalesZertifikat.class));
    }

    @Test
    void testSignData_ThrowsExceptionWithInvalidKey() {
        DigitalesZertifikat invalidZert = DigitalesZertifikat.builder()
                .privateKeyPEM("INVALID_KEY")
                .build();

        assertThrows(RuntimeException.class, () -> 
            pkiService.signData("test data", invalidZert));
    }

    @Test
    void testZertifikatHierarchie() {
        // Root -> Verein -> Aufseher
        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(vereinRepository.findById(1L)).thenReturn(Optional.of(testVerein));
        when(zertifikatRepository.existsByBenutzer(testBenutzer)).thenReturn(false);
        when(zertifikatRepository.findByVereinAndZertifikatsTyp(testVerein, "VEREIN"))
                .thenReturn(Optional.of(vereinZertifikat));
        when(zertifikatRepository.save(any(DigitalesZertifikat.class)))
                .thenAnswer(invocation -> {
                    DigitalesZertifikat zert = invocation.getArgument(0);
                    zert.setId(3L);
                    return zert;
                });

        DigitalesZertifikat aufseherZert = pkiService.createAufseherCertificate(
                testBenutzer, testVerein);

        assertNotNull(aufseherZert);
        assertEquals(vereinZertifikat, aufseherZert.getParentZertifikat());
        assertEquals(rootZertifikat, vereinZertifikat.getParentZertifikat());
    }
}
