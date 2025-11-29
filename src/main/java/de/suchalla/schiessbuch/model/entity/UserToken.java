package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity-Klasse für Benutzer-Tokens (Passwort-Reset, E-Mail-Verifizierung).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Setter
@Getter
@Table(name = "user_token")
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Column(nullable = false)
    private LocalDateTime ablaufdatum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTokenTyp typ;

    @ManyToOne
    @JoinColumn(name = "benutzer_id", nullable = false)
    private Benutzer benutzer;

    public UserToken() {}

    public UserToken(String token, LocalDateTime ablaufdatum, UserTokenTyp typ, Benutzer benutzer) {
        this.token = token;
        this.ablaufdatum = ablaufdatum;
        this.typ = typ;
        this.benutzer = benutzer;
    }
}
