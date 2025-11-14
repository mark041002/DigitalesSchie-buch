package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.UserToken;
import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    List<UserToken> findByBenutzerIdAndTyp(Long benutzerId, UserTokenTyp typ);
}

