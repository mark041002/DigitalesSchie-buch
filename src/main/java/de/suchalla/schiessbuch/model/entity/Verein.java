package de.suchalla.schiessbuch.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity-Klasse für Vereine.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "verein")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verein {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Column(nullable = false)
    private String name;

    @Column(name = "vereins_nummer")
    private String vereinsNummer;

    @Column(length = 500)
    private String adresse;

    @Column(length = 1000)
    private String beschreibung;

    @NotNull(message = "Verband darf nicht leer sein")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verband_id", nullable = false)
    private Verband verband;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    @OneToMany(mappedBy = "verein", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Vereinsmitgliedschaft> mitgliedschaften = new HashSet<>();

    @OneToMany(mappedBy = "verein", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<Schiesstand> schiesstaende = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        erstelltAm = LocalDateTime.now();
        aktualisiertAm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        aktualisiertAm = LocalDateTime.now();
    }
}
