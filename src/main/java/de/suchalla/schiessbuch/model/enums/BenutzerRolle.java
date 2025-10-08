package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Benutzerrollen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum BenutzerRolle {
    /**
     * Normaler Schütze.
     */
    SCHUETZE("Schütze"),

    /**
     * Aufseher/Trainer im Verein.
     */
    AUFSEHER("Aufseher"),

    /**
     * Vereinschef mit Verwaltungsrechten für einen Verein.
     */
    VEREINS_CHEF("Vereinschef"),

    /**
     * Vereinsadministrator.
     */
    VEREINS_ADMIN("Vereinsadministrator"),

    /**
     * Administrator mit vollen Rechten.
     */
    ADMIN("Administrator"),

    /**
     * Software-Administrator mit vollen Rechten.
     */
    SOFTWARE_ADMIN("Software-Administrator");

    private final String bezeichnung;

    BenutzerRolle(String bezeichnung) {
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
