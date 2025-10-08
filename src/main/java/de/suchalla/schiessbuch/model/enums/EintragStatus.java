package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für den Status von Schießnachweis-Einträgen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum EintragStatus {
    /**
     * Eintrag wurde erstellt und wartet auf Signierung.
     */
    OFFEN("Offen"),

    /**
     * Eintrag wurde erstellt und wartet auf Signierung (Alias für OFFEN).
     */
    UNSIGNIERT("Unsigniert"),

    /**
     * Eintrag wurde signiert.
     */
    SIGNIERT("Signiert"),

    /**
     * Eintrag wurde abgelehnt.
     */
    ABGELEHNT("Abgelehnt");

    private final String bezeichnung;

    EintragStatus(String bezeichnung) {
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
