package de.suchalla.schiessbuch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse der Digitales Schießbuch Anwendung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@SpringBootApplication
public class DigitalesSchiessbuchApplication {

    /**
     * Startet die Spring Boot Anwendung.
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        SpringApplication.run(DigitalesSchiessbuchApplication.class, args);
    }
}

