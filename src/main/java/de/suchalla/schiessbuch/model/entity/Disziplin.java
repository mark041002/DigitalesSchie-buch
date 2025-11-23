package de.suchalla.schiessbuch.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity-Klasse für Schießdisziplinen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "disziplin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Disziplin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Kennziffer darf nicht leer sein")
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String kennziffer;

    @Column(length = 1000)
    private String programm;

    @Column(name = "waffe_klasse")
    private String waffeKlasse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verband_id")
    private Verband verband;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    @OneToMany(mappedBy = "disziplin", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<SchiessnachweisEintrag> eintraege = new HashSet<>();

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
