package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.BenachrichtigungsTyp;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity-Klasse für Benachrichtigungen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Entity
@Table(name = "benachrichtigung")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benachrichtigung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empfänger darf nicht leer sein")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empfaenger_id", nullable = false)
    private Benutzer empfaenger;

    @NotBlank(message = "Titel darf nicht leer sein")
    @Column(nullable = false)
    private String titel;

    @NotBlank(message = "Nachricht darf nicht leer sein")
    @Column(length = 2000, nullable = false)
    private String nachricht;

    @NotNull(message = "Typ darf nicht leer sein")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenachrichtigungsTyp typ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eintrag_id")
    private SchiessnachweisEintrag eintrag;

    @Column(nullable = false)
    @Builder.Default
    private Boolean gelesen = false;

    @Column(name = "gelesen_am")
    private LocalDateTime gelesenAm;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @PrePersist
    protected void onCreate() {
        erstelltAm = LocalDateTime.now();
    }

    /**
     * Markiert die Benachrichtigung als gelesen.
     */
    public void markiereAlsGelesen() {
        this.gelesen = true;
        this.gelesenAm = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Benachrichtigung{" +
                "id=" + id +
                ", titel='" + titel + '\'' +
                ", typ=" + typ +
                ", gelesen=" + gelesen +
                ", erstelltAm=" + erstelltAm +
                '}';
    }
}

