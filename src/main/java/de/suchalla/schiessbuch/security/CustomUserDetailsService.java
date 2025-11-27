package de.suchalla.schiessbuch.security;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * UserDetailsService-Implementierung für Spring Security.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final BenutzerRepository benutzerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Benutzer benutzer = benutzerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));

        // E-Mail-Verifizierung prüfen
        if (!benutzer.isEmailVerifiziert()) {
            throw new UsernameNotFoundException("E-Mail-Adresse nicht verifiziert. Bitte bestätigen Sie Ihre E-Mail.");
        }

        return User.builder()
                .username(benutzer.getEmail())  // E-Mail als Username verwenden
                .password(benutzer.getPasswort())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + benutzer.getRolle().name())))
                .build();
    }
}
