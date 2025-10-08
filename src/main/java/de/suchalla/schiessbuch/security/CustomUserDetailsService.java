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
        // Login erfolgt jetzt über E-Mail statt Benutzername
        Benutzer benutzer = benutzerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));

        if (!benutzer.getAktiv()) {
            throw new UsernameNotFoundException("Benutzer ist deaktiviert: " + email);
        }

        return User.builder()
                .username(benutzer.getEmail())  // E-Mail als Username verwenden
                .password(benutzer.getPasswort())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + benutzer.getRolle().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!benutzer.getAktiv())
                .build();
    }
}
