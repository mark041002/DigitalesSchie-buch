package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    // Löscht alle Tokens für einen gegebenen Benutzer (Spring Data erstellt die Implementierung)
    void deleteAllByBenutzer(de.suchalla.schiessbuch.model.entity.Benutzer benutzer);
    // Löscht alle Tokens für einen Benutzer anhand der Benutzer-ID (vermeidet Laden der Entity)
    void deleteAllByBenutzerId(Long benutzerId);
}

