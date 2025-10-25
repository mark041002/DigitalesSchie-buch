package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.EintragStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity-Klasse für Schießnachweis-Einträge.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "schiessnachweise_eintrag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SchiessnachweisEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Schütze darf nicht leer sein")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schuetze_id", nullable = false)
    private Benutzer schuetze;

    @NotNull(message = "Disziplin darf nicht leer sein")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disziplin_id", nullable = false)
    private Disziplin disziplin;

    @NotNull(message = "Schießstand darf nicht leer sein")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schiesstand_id", nullable = false)
    private Schiesstand schiesstand;

    @NotNull(message = "Datum darf nicht leer sein")
    @Column(nullable = false)
    private LocalDate datum;

    @Column(name = "anzahl_schuesse")
    private Integer anzahlSchuesse;

    @Column(name = "ergebnis")
    private String ergebnis;

    @Column(length = 100)
    private String kaliber;

    @Column(length = 100)
    private String waffenart;

    @Column(length = 1000)
    private String bemerkung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aufseher_id")
    private Benutzer aufseher;

    @Column(name = "signiert_am")
    private LocalDateTime signiertAm;

    @Column(name = "ist_signiert", nullable = false)
    @Builder.Default
    private Boolean istSigniert = false;

    @NotNull(message = "Status darf nicht leer sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EintragStatus status = EintragStatus.OFFEN;

    @Column(name = "digitale_signatur", length = 500)
    private String digitaleSignatur;

    @Column(name = "ablehnungsgrund", length = 1000)
    private String ablehnungsgrund;

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
     * Signiert den Eintrag durch einen Aufseher.
     *
     * @param aufseher Der signierende Aufseher
     */
    public void signieren(Benutzer aufseher) {
        this.aufseher = aufseher;
        this.istSigniert = true;
        this.status = EintragStatus.SIGNIERT;
        this.signiertAm = LocalDateTime.now();
    }

    /**
     * Prüft, ob der Eintrag bearbeitet werden kann.
     *
     * @return true wenn bearbeitbar
     */
    public boolean kannBearbeitetWerden() {
        return status == EintragStatus.OFFEN || status == EintragStatus.UNSIGNIERT;
    }

    /**
     * Prüft, ob der Eintrag gelöscht werden kann.
     *
     * @return true wenn löschbar
     */
    public boolean kannGeloeschtWerden() {
        return status == EintragStatus.OFFEN || status == EintragStatus.UNSIGNIERT;
    }
}
