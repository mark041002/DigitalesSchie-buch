package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für den Status einer Vereinsmitgliedschaft.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum MitgliedschaftStatus {
    BEANTRAGT("Beantragt"),
    AKTIV("Aktiv"),
    ABGELEHNT("Abgelehnt"),
    BEENDET("Beendet"),
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
