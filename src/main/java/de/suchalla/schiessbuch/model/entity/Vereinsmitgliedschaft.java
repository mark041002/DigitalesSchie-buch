package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.MitgliedschaftsStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity-Klasse für Vereinsmitgliedschaften.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "vereinsmitgliedschaft")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"benutzer", "verein"})
public class Vereinsmitgliedschaft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Benutzer darf nicht leer sein")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "benutzer_id", nullable = false)
    private Benutzer benutzer;

    @NotNull(message = "Verein darf nicht leer sein")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verein_id", nullable = false)
    private Verein verein;

    @NotNull(message = "Status darf nicht leer sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MitgliedschaftsStatus status = MitgliedschaftsStatus.BEANTRAGT;

    @Column(name = "beitritt_datum", nullable = false)
    private LocalDate beitrittDatum;

    @Column(name = "austritt_datum")
    private LocalDate austrittDatum;

    @Column(name = "ist_aufseher", nullable = false)
    @Builder.Default
    private Boolean istAufseher = false;

    @Column(name = "ist_vereinschef", nullable = false)
    @Builder.Default
    private Boolean istVereinschef = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aktiv = true;

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
        if (beitrittDatum == null) {
            beitrittDatum = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        aktualisiertAm = LocalDateTime.now();
    }

}
