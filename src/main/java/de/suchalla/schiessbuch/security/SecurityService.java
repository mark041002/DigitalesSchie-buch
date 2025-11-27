package de.suchalla.schiessbuch.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import org.springframework.stereotype.Component;
import com.vaadin.flow.component.UI;

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
     * @return Aktueller Benutzer oder null
     */
    public Benutzer getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> benutzerRepository.findByEmailWithMitgliedschaften(userDetails.getUsername()))
                .orElse(null);
    }

    /**
     * Meldet den aktuellen Benutzer ab.
     */
    public void logout() {
        authenticationContext.logout();
        try {
            UI ui = UI.getCurrent();
            if (ui != null) {
                ui.getPage().setLocation("/digschbuch/login");
            }
        } catch (Exception ignored) {
            // UI not available or navigation failed; ignore to avoid breaking logout
        }
    }

    /**
     * Aktualisiert die Authentication im SecurityContext mit den Daten des gegebenen Benutzers.
     * Dadurch bleibt der Benutzer eingeloggt, auch wenn sich z.B. die E-Mail bzw. der Username geändert hat.
     *
     * @param benutzer der aktualisierte Benutzer
     */
    public void refreshAuthentication(Benutzer benutzer) {
        if (benutzer == null) return;

        UserDetails userDetails = User.builder()
                .username(benutzer.getEmail())
                .password(benutzer.getPasswort())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + benutzer.getRolle().name())))
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
