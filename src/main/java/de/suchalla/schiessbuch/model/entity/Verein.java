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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Verein {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Vereinsnummer darf nicht leer sein")
    @Column(name = "vereins_nummer", nullable = false)
    private String vereinsNummer;

    @Column(length = 500)
    private String adresse;

    @Column(length = 1000)
    private String beschreibung;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "verein_verband",
            joinColumns = @JoinColumn(name = "verein_id"),
            inverseJoinColumns = @JoinColumn(name = "verband_id")
    )
    @Builder.Default
    private Set<Verband> verbaende = new HashSet<>();

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    @OneToMany(mappedBy = "verein", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Vereinsmitgliedschaft> mitgliedschaften = new HashSet<>();

    @OneToOne(mappedBy = "verein", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Schiesstand schiesstand;

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
        return "Verein{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", vereinsNummer='" + vereinsNummer + '\'' +
                '}';
    }
}
