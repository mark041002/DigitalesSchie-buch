package de.suchalla.schiessbuch.model.enums;

import lombok.Getter;

/**
 * Enum für Benutzerrollen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Getter
public enum BenutzerRolle {
    SCHUETZE("Schütze"),

    AUFSEHER("Aufseher"),
    SCHIESSSTAND_AUFSEHER("Schießstandaufseher"),

    VEREINS_CHEF("Vereinschef"),

    ADMIN("Administrator");

    private final String bezeichnung;

    BenutzerRolle(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
