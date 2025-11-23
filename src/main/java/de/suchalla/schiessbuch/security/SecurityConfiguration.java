package de.suchalla.schiessbuch.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import de.suchalla.schiessbuch.ui.view.persoenlich.LoginView;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// AntPathRequestMatcher is deprecated in recent Spring Security versions.
// Use the varargs `requestMatchers(String...)` overload instead.

/**
 * Security-Konfiguration für Spring Security und Vaadin.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends VaadinWebSecurity {

    /**
     * Konfiguriert HTTP-Security.
     *
     * @param http HttpSecurity-Objekt
     * @throws Exception bei Konfigurationsfehlern
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
            auth.requestMatchers(
                "/images/**",
                "/icons/**",
                "/line-awesome/**",
                "/register",
                "/passwort-vergessen",
                "/passwort-zuruecksetzen",
                "/zertifikat-verifizieren"
            ).permitAll()
        );

        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    /**
     * Bean für Password-Encoder.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

