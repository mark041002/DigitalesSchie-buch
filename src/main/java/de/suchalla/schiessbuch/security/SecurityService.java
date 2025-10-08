package de.suchalla.schiessbuch.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Service für Security-Utilities.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationContext authenticationContext;
    private final BenutzerRepository benutzerRepository;

    /**
     * Gibt den aktuell angemeldeten Benutzer zurück.
     *
     * @return Optional mit aktuellem Benutzer
     */
    public Optional<Benutzer> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> benutzerRepository.findByEmailWithMitgliedschaften(userDetails.getUsername()));
    }

    /**
     * Meldet den aktuellen Benutzer ab.
     */
    public void logout() {
        authenticationContext.logout();
    }
}
