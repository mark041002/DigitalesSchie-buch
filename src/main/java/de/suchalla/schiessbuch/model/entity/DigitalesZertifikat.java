package de.suchalla.schiessbuch.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity-Klasse für digitale PKI-Zertifikate.
 * Hierarchie: Root -> Verein -> Aufseher
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "digitales_zertifikat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DigitalesZertifikat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Typ: ROOT, VEREIN, AUFSEHER
     */
    @NotBlank(message = "Zertifikatstyp darf nicht leer sein")
    @Column(name = "zertifikats_typ", nullable = false)
    private String zertifikatsTyp;

    /**
     * Seriennummer des Zertifikats (eindeutig)
     */
    @NotBlank(message = "Seriennummer darf nicht leer sein")
    @Column(nullable = false, unique = true)
    private String seriennummer;

    /**
     * Subject Distinguished Name
     */
    @NotBlank(message = "Subject DN darf nicht leer sein")
    @Column(name = "subject_dn", nullable = false, length = 500)
    private String subjectDN;

    /**
     * Issuer Distinguished Name
     */
    @NotBlank(message = "Issuer DN darf nicht leer sein")
    @Column(name = "issuer_dn", nullable = false, length = 500)
    private String issuerDN;

    /**
     * Zertifikat im PEM-Format
     */
    @NotBlank(message = "Zertifikat PEM darf nicht leer sein")
    @Column(name = "zertifikat_pem", nullable = false, columnDefinition = "TEXT")
    private String zertifikatPEM;

    /**
     * Private Key im PEM-Format (verschlüsselt gespeichert)
     */
    @Column(name = "private_key_pem", columnDefinition = "TEXT")
    private String privateKeyPEM;

    /**
     * Gültig ab
     */
    @NotNull(message = "Gültig ab darf nicht leer sein")
    @Column(name = "gueltig_ab", nullable = false)
    private LocalDateTime gueltigAb;

    /**
     * Gültig bis
     */
    @NotNull(message = "Gültig bis darf nicht leer sein")
    @Column(name = "gueltig_bis", nullable = false)
    private LocalDateTime gueltigBis;

    /**
     * Wurde das Zertifikat widerrufen?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean widerrufen = false;

    /**
     * Zeitpunkt des Widerrufs
     */
    @Column(name = "widerrufen_am")
    private LocalDateTime widerrufenAm;

    /**
     * Grund für Widerruf
     */
    @Column(name = "widerrufs_grund", length = 1000)
    private String widerrufsGrund;

    /**
     * Referenz zum Benutzer (für Aufseher-Zertifikate)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benutzer_id")
    private Benutzer benutzer;

    /**
     * Referenz zum Verein (für Vereins- und Aufseher-Zertifikate)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verein_id")
    private Verein verein;

    /**
     * Parent-Zertifikat in der Hierarchie
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_zertifikat_id")
    private DigitalesZertifikat parentZertifikat;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    @PrePersist
    protected void onCreate() {
        erstelltAm = LocalDateTime.now();
        aktualisiertAm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        aktualisiertAm = LocalDateTime.now();
    }

    /**
     * Prüft, ob das Zertifikat aktuell gültig ist
     */
    public boolean istGueltig() {
        if (widerrufen) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(gueltigAb) && now.isBefore(gueltigBis);
    }

    /**
     * Widerruft das Zertifikat
     */
    public void widerrufen(String grund) {
        this.widerrufen = true;
        this.widerrufenAm = LocalDateTime.now();
        this.widerrufsGrund = grund;
    }

    @Override
    public String toString() {
        return "DigitalesZertifikat{" +
                "id=" + id +
                ", typ='" + zertifikatsTyp + '\'' +
                ", seriennummer='" + seriennummer + '\'' +
                ", gueltig=" + istGueltig() +
                '}';
    }
}

