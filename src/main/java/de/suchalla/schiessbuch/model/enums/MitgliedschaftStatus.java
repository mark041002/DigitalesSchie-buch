package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für den Status einer Vereinsmitgliedschaft.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum MitgliedschaftStatus {
    /**
     * Beitrittsanfrage wurde gestellt, wartet auf Genehmigung.
     */
    BEANTRAGT("Beantragt"),

    /**
     * Mitgliedschaft wurde genehmigt und ist aktiv.
     */
    AKTIV("Aktiv"),

    /**
     * Beitrittsanfrage wurde abgelehnt.
     */
    ABGELEHNT("Abgelehnt"),

    /**
     * Mitgliedschaft wurde beendet.
     */
    BEENDET("Beendet"),

    /**
     * Benutzer hat den Verein verlassen.
     */
    VERLASSEN("Verlassen");

    private final String bezeichnung;

    MitgliedschaftStatus(String bezeichnung) {
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
