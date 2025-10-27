package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.DigitalesZertifikat;
import de.suchalla.schiessbuch.model.entity.Verein;
import de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository;
import de.suchalla.schiessbuch.repository.VereinRepository;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service für PKI-Zertifikatsverwaltung.
 * Hierarchie: Root CA -> Verein CA -> Aufseher
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PkiService {

    private final DigitalesZertifikatRepository zertifikatRepository;
    private final VereinRepository vereinRepository;
    private final BenutzerRepository benutzerRepository;

    static {
        // Bouncy Castle Provider registrieren
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Initialisiert Root-Zertifikat beim Start
     */
    @PostConstruct
    @Transactional
    public void initializeRootCertificate() {
        if (zertifikatRepository.findByZertifikatsTyp("ROOT").isEmpty()) {
            try {
                log.info("Erstelle Root-Zertifikat...");

                // RSA Key Pair generieren
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
                keyGen.initialize(4096, new SecureRandom());
                KeyPair rootKeyPair = keyGen.generateKeyPair();

                // Root CA Distinguished Name
                X500Name rootDN = new X500Name("CN=Digitales Schiessbuch Root CA, O=Digitales Schiessbuch, C=DE");

                // Zertifikat erstellen
                BigInteger serialNumber = new BigInteger(128, new SecureRandom());
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime validUntil = now.plusYears(20);

                Date notBefore = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
                Date notAfter = Date.from(validUntil.atZone(ZoneId.systemDefault()).toInstant());

                X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                        rootDN,
                        serialNumber,
                        notBefore,
                        notAfter,
                        rootDN,
                        rootKeyPair.getPublic()
                );

                // Extensions für Root CA
                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
                certBuilder.addExtension(Extension.keyUsage, true,
                        new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

                ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                        .setProvider("BC")
                        .build(rootKeyPair.getPrivate());

                X509CertificateHolder certHolder = certBuilder.build(signer);
                X509Certificate rootCert = new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(certHolder);

                // In Datenbank speichern
                DigitalesZertifikat rootZertifikat = DigitalesZertifikat.builder()
                        .zertifikatsTyp("ROOT")
                        .seriennummer(serialNumber.toString(16))
                        .subjectDN(rootDN.toString())
                        .issuerDN(rootDN.toString())
                        .zertifikatPEM(convertToPEM(rootCert))
                        .privateKeyPEM(convertPrivateKeyToPEM(rootKeyPair.getPrivate()))
                        .gueltigAb(now)
                        .gueltigBis(validUntil)
                        .widerrufen(false)
                        .build();

                zertifikatRepository.save(rootZertifikat);
                log.info("Root-Zertifikat erfolgreich erstellt mit Seriennummer: {}", serialNumber.toString(16));

            } catch (Exception e) {
                log.error("Fehler beim Erstellen des Root-Zertifikats", e);
                throw new RuntimeException("Root-Zertifikat konnte nicht erstellt werden", e);
            }
        } else {
            log.info("Root-Zertifikat existiert bereits");
        }
    }

    /**
     * Erstellt ein Vereinszertifikat, signiert vom Root-Zertifikat.
     */
    @Transactional
    public DigitalesZertifikat createVereinCertificate(Verein verein) {
        try {
            // Verein aus DB laden, um LazyInitializationException zu vermeiden
            Verein managedVerein = vereinRepository.findById(verein.getId())
                    .orElseThrow(() -> new RuntimeException("Verein nicht gefunden"));

            log.info("Erstelle Vereinszertifikat für: {}", managedVerein.getName());

            // Prüfen ob bereits vorhanden
            if (zertifikatRepository.existsByVereinAndZertifikatsTyp(managedVerein, "VEREIN")) {
                return zertifikatRepository.findByVereinAndZertifikatsTyp(managedVerein, "VEREIN")
                        .orElseThrow();
            }

            // Root-Zertifikat laden
            DigitalesZertifikat rootZertifikat = zertifikatRepository.findByZertifikatsTyp("ROOT")
                    .orElseThrow(() -> new RuntimeException("Root-Zertifikat nicht gefunden"));

            // Key Pair für Verein generieren
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048, new SecureRandom());
            KeyPair vereinKeyPair = keyGen.generateKeyPair();

            // Vereinszertifikat erstellen
            X500Name issuerDN = new X500Name(rootZertifikat.getSubjectDN());
            X500Name subjectDN = new X500Name(String.format(
                    "CN=%s, O=Digitales Schiessbuch, OU=Verein, C=DE",
                    managedVerein.getName()
            ));

            BigInteger serialNumber = new BigInteger(128, new SecureRandom());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime validUntil = now.plusYears(5);

            Date notBefore = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date notAfter = Date.from(validUntil.atZone(ZoneId.systemDefault()).toInstant());

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuerDN,
                    serialNumber,
                    notBefore,
                    notAfter,
                    subjectDN,
                    vereinKeyPair.getPublic()
            );

            // Extensions für Intermediate CA (Verein)
            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(1));
            certBuilder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature));

            // Mit Root-Private-Key signieren
            PrivateKey rootPrivateKey = loadPrivateKeyFromPEM(rootZertifikat.getPrivateKeyPEM());
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                    .setProvider("BC")
                    .build(rootPrivateKey);

            X509CertificateHolder certHolder = certBuilder.build(signer);
            X509Certificate vereinCert = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certHolder);

            // In Datenbank speichern
            DigitalesZertifikat vereinZertifikat = DigitalesZertifikat.builder()
                    .zertifikatsTyp("VEREIN")
                    .seriennummer(serialNumber.toString(16))
                    .subjectDN(subjectDN.toString())
                    .issuerDN(issuerDN.toString())
                    .zertifikatPEM(convertToPEM(vereinCert))
                    .privateKeyPEM(convertPrivateKeyToPEM(vereinKeyPair.getPrivate()))
                    .gueltigAb(now)
                    .gueltigBis(validUntil)
                    .widerrufen(false)
                    .verein(managedVerein)
                    .parentZertifikat(rootZertifikat)
                    .build();

            zertifikatRepository.save(vereinZertifikat);
            log.info("Vereinszertifikat erstellt für: {}", managedVerein.getName());

            return vereinZertifikat;

        } catch (Exception e) {
            log.error("Fehler beim Erstellen des Vereinszertifikats", e);
            throw new RuntimeException("Vereinszertifikat konnte nicht erstellt werden", e);
        }
    }

    /**
     * Erstellt ein Aufseher-Zertifikat, signiert vom Vereinszertifikat.
     */
    @Transactional
    public DigitalesZertifikat createAufseherCertificate(Benutzer benutzer, Verein verein) {
        try {
            // Benutzer und Verein aus DB laden, um LazyInitializationException zu vermeiden
            Benutzer managedBenutzer = benutzerRepository.findById(benutzer.getId())
                    .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
            Verein managedVerein = vereinRepository.findById(verein.getId())
                    .orElseThrow(() -> new RuntimeException("Verein nicht gefunden"));

            log.info("Erstelle Aufseher-Zertifikat für: {} im Verein: {}",
                    managedBenutzer.getVollstaendigerName(), managedVerein.getName());

            // Prüfen ob bereits vorhanden
            if (zertifikatRepository.existsByBenutzer(managedBenutzer)) {
                return zertifikatRepository.findByBenutzer(managedBenutzer)
                        .orElseThrow();
            }

            // Vereinszertifikat laden oder erstellen
            DigitalesZertifikat vereinZertifikat = zertifikatRepository
                    .findByVereinAndZertifikatsTyp(managedVerein, "VEREIN")
                    .orElseGet(() -> createVereinCertificate(managedVerein));

            // Key Pair für Aufseher generieren
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
            keyGen.initialize(2048, new SecureRandom());
            KeyPair aufseherKeyPair = keyGen.generateKeyPair();

            // Aufseher-Zertifikat erstellen
            X500Name issuerDN = new X500Name(vereinZertifikat.getSubjectDN());
            X500Name subjectDN = new X500Name(String.format(
                    "CN=%s, O=Digitales Schiessbuch, OU=Aufseher %s, C=DE",
                    managedBenutzer.getVollstaendigerName(),
                    managedVerein.getName()
            ));

            BigInteger serialNumber = new BigInteger(128, new SecureRandom());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime validUntil = now.plusYears(3);

            Date notBefore = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date notAfter = Date.from(validUntil.atZone(ZoneId.systemDefault()).toInstant());

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    issuerDN,
                    serialNumber,
                    notBefore,
                    notAfter,
                    subjectDN,
                    aufseherKeyPair.getPublic()
            );

            // Extensions für End Entity (Aufseher)
            certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            certBuilder.addExtension(Extension.keyUsage, true,
                    new KeyUsage(KeyUsage.digitalSignature | KeyUsage.nonRepudiation));

            // Mit Vereins-Private-Key signieren
            PrivateKey vereinPrivateKey = loadPrivateKeyFromPEM(vereinZertifikat.getPrivateKeyPEM());
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                    .setProvider("BC")
                    .build(vereinPrivateKey);

            X509CertificateHolder certHolder = certBuilder.build(signer);
            X509Certificate aufseherCert = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certHolder);

            // In Datenbank speichern
            DigitalesZertifikat aufseherZertifikat = DigitalesZertifikat.builder()
                    .zertifikatsTyp("AUFSEHER")
                    .seriennummer(serialNumber.toString(16))
                    .subjectDN(subjectDN.toString())
                    .issuerDN(issuerDN.toString())
                    .zertifikatPEM(convertToPEM(aufseherCert))
                    .privateKeyPEM(convertPrivateKeyToPEM(aufseherKeyPair.getPrivate()))
                    .gueltigAb(now)
                    .gueltigBis(validUntil)
                    .widerrufen(false)
                    .benutzer(managedBenutzer)
                    .verein(managedVerein)
                    .parentZertifikat(vereinZertifikat)
                    .build();

            zertifikatRepository.save(aufseherZertifikat);
            log.info("Aufseher-Zertifikat erstellt für: {}", managedBenutzer.getVollstaendigerName());

            return aufseherZertifikat;

        } catch (Exception e) {
            log.error("Fehler beim Erstellen des Aufseher-Zertifikats", e);
            throw new RuntimeException("Aufseher-Zertifikat konnte nicht erstellt werden", e);
        }
    }

    /**
     * Signiert Daten mit dem Zertifikat eines Aufsehers
     */
    public String signData(String data, DigitalesZertifikat zertifikat) {
        try {
            PrivateKey privateKey = loadPrivateKeyFromPEM(zertifikat.getPrivateKeyPEM());
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            byte[] signatureBytes = signature.sign();
            return java.util.Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("Fehler beim Signieren der Daten", e);
            throw new RuntimeException("Daten konnten nicht signiert werden", e);
        }
    }

    /**
     * Verifiziert eine Signatur
     */
    public boolean verifySignature(String data, String signatureBase64, DigitalesZertifikat zertifikat) {
        try {
            X509Certificate cert = loadCertificateFromPEM(zertifikat.getZertifikatPEM());
            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initVerify(cert.getPublicKey());
            signature.update(data.getBytes());
            byte[] signatureBytes = java.util.Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Fehler beim Verifizieren der Signatur", e);
            return false;
        }
    }

    /**
     * Konvertiert X509Certificate zu PEM-Format
     */
    private String convertToPEM(X509Certificate certificate) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(certificate);
        }
        return stringWriter.toString();
    }

    /**
     * Konvertiert PrivateKey zu PEM-Format
     */
    private String convertPrivateKeyToPEM(PrivateKey privateKey) throws Exception {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(privateKey);
        }
        return stringWriter.toString();
    }

    /**
     * Lädt PrivateKey aus PEM-Format
     */
    private PrivateKey loadPrivateKeyFromPEM(String pem) throws Exception {
        try (PemReader pemReader = new PemReader(new StringReader(pem))) {
            PemObject pemObject = pemReader.readPemObject();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            return keyFactory.generatePrivate(keySpec);
        }
    }

    /**
     * Lädt X509Certificate aus PEM-Format
     */
    private X509Certificate loadCertificateFromPEM(String pem) throws Exception {
        try (PemReader pemReader = new PemReader(new StringReader(pem))) {
            PemObject pemObject = pemReader.readPemObject();
            return (X509Certificate) java.security.cert.CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new java.io.ByteArrayInputStream(pemObject.getContent()));
        }
    }
}
