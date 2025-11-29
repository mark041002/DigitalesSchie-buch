package de.suchalla.schiessbuch.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.suchalla.schiessbuch.model.enums.SchiesstandTyp;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity-Klasse für Schießstände.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "schiesstand")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schiesstand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Name darf nicht leer sein")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Typ darf nicht leer sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchiesstandTyp typ;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verein_id")
    @JsonIgnore
    private Verein verein;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aufseher_id")
    @JsonIgnore
    private Benutzer aufseher;

    @Column(length = 500)
    private String adresse;

    @Column(length = 1000)
    private String beschreibung;

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
}
