package de.suchalla.schiessbuch.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity-Klasse für digitale PKI-Zertifikate.
 * Hierarchie:
 * - Root -> Verein -> Aufseher (für Vereinsmitglieder)
 * - Root -> Schießstandaufseher (für gewerbliche Schießstände)
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

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Zertifikatstyp darf nicht leer sein")
    @Column(name = "zertifikats_typ", nullable = false)
    private String zertifikatsTyp;

    @NotBlank(message = "Seriennummer darf nicht leer sein")
    @Column(nullable = false, unique = true)
    private String seriennummer;

    @NotBlank(message = "Subject DN darf nicht leer sein")
    @Column(name = "subject_dn", nullable = false, length = 500)
    private String subjectDN;

    @NotBlank(message = "Issuer DN darf nicht leer sein")
    @Column(name = "issuer_dn", nullable = false, length = 500)
    private String issuerDN;

    @NotBlank(message = "Zertifikat PEM darf nicht leer sein")
    @Column(name = "zertifikat_pem", nullable = false)
    private String zertifikatPEM;

    @Column(name = "private_key_pem")
    private String privateKeyPEM;

    @NotNull(message = "Gültig ab darf nicht leer sein")
    @Column(name = "gueltig_ab", nullable = false)
    private LocalDateTime gueltigSeit;

    //Gültig bis (null = unbegrenzt gültig)
    @Column(name = "gueltig_bis")
    private LocalDateTime gueltigBis;

    @Column(nullable = false)
    @Builder.Default
    private Boolean widerrufen = false;

    @Column(name = "widerrufen_am")
    private LocalDateTime widerrufenAm;

    @Column(name = "widerrufs_grund", length = 1000)
    private String widerrufsGrund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benutzer_id")
    private Benutzer benutzer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verein_id")
    private Verein verein;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schiesstand_id")
    private Schiesstand schiesstand;

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


    @Override
    public String toString() {
        return "DigitalesZertifikat{" +
                "id=" + id +
                ", typ='" + zertifikatsTyp + '\'' +
                ", seriennummer='" + seriennummer + '\'' +
                '}';
    }

    public boolean istGueltig() {
        return !widerrufen &&
                (gueltigSeit.isBefore(LocalDateTime.now())) &&
                (gueltigBis == null || gueltigBis.isAfter(LocalDateTime.now()));
    }
}
