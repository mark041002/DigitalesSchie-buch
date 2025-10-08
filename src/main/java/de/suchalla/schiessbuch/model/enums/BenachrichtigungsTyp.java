package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Benachrichtigungstypen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum BenachrichtigungsTyp {
    /**
     * Eintrag wurde signiert.
     */
    EINTRAG_SIGNIERT("Eintrag signiert"),

    /**
     * Eintrag wurde abgelehnt.
     */
    EINTRAG_ABGELEHNT("Eintrag abgelehnt"),

    /**
     * Neuer Eintrag wartet auf Signierung.
     */
    NEUER_EINTRAG("Neuer Eintrag"),

    /**
     * Eintrag wurde gelöscht.
     */
    EINTRAG_GELOESCHT("Eintrag gelöscht"),

    /**
     * Mitgliedschaft wurde bestätigt.
     */
    MITGLIEDSCHAFT_BESTAETIGT("Mitgliedschaft bestätigt"),

    /**
     * Mitgliedschaft wurde beendet.
     */
    MITGLIEDSCHAFT_BEENDET("Mitgliedschaft beendet"),

    /**
     * Benutzer wurde zum Aufseher ernannt.
     */
    AUFSEHER_ERNANNT("Aufseher ernannt"),

    /**
     * Allgemeine Information.
     */
    INFO("Information"),

    /**
     * Warnung.
     */
    WARNUNG("Warnung");

    private final String bezeichnung;

    BenachrichtigungsTyp(String bezeichnung) {
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
