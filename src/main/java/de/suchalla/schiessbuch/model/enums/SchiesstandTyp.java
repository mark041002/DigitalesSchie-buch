package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Schießstand-Typen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum SchiesstandTyp {
    /**
     * 10m Luftgewehr/Luftpistole.
     */
    LUFTGEWEHR_10M("10m Luftgewehr/Luftpistole"),

    /**
     * 25m Pistole.
     */
    PISTOLE_25M("25m Pistole"),

    /**
     * 50m Gewehr.
     */
    GEWEHR_50M("50m Gewehr"),

    /**
     * 100m Gewehr.
     */
    GEWEHR_100M("100m Gewehr"),

    /**
     * Trap/Skeet.
     */
    TRAP_SKEET("Trap/Skeet"),

    /**
     * Vereinsgebundener Schießstand.
     */
    VEREINSGEBUNDEN("Vereinsgebunden"),

    /**
     * Gewerblicher Schießstand.
     */
    GEWERBLICH("Gewerblich"),

    /**
     * Sonstiger Schießstand.
     */
    SONSTIGES("Sonstiges");

    private final String bezeichnung;

    SchiesstandTyp(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
