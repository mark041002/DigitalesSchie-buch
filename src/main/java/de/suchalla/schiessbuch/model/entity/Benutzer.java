package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity-Klasse für Benutzer.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "benutzer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Column(nullable = false)
    private String passwort;

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Column(nullable = false)
    private String vorname;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Column(nullable = false)
    private String nachname;

    @Email(message = "E-Mail-Adresse ist ungültig")
    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Rolle darf nicht leer sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BenutzerRolle rolle = BenutzerRolle.SCHUETZE;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @Column(name = "aktualisiert_am")
    private LocalDateTime aktualisiertAm;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aktiv = true;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_ablauf")
    private LocalDateTime resetTokenAblauf;

    @OneToMany(mappedBy = "benutzer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<Vereinsmitgliedschaft> vereinsmitgliedschaften = new HashSet<>();

    @OneToMany(mappedBy = "schuetze", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<SchiessnachweisEintrag> schiessnachweise = new HashSet<>();

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
     * Gibt den vollständigen Namen zurück.
     *
     * @return Vollständiger Name
     */
    public String getVollstaendigerName() {
        return vorname + " " + nachname;
    }

    /**
     * Prüft, ob der Benutzer ein Aufseher ist.
     *
     * @return true wenn Aufseher
     */
    public boolean istAufseher() {
        return rolle == BenutzerRolle.AUFSEHER;
    }

    /**
     * Prüft, ob der Benutzer ein Admin ist.
     *
     * @return true wenn Admin
     */
    public boolean istAdmin() {
        return rolle == BenutzerRolle.ADMIN ||
               rolle == BenutzerRolle.SOFTWARE_ADMIN ||
               rolle == BenutzerRolle.VEREINS_ADMIN;
    }
}
