package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für den Status einer Vereinsmitgliedschaft.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum MitgliedschaftsStatus {
    BEANTRAGT("Beantragt"),
    AKTIV("Aktiv"),
    ABGELEHNT("Abgelehnt"),
    BEENDET("Beendet"),
    VERLASSEN("Verlassen");

    private final String bezeichnung;

    MitgliedschaftsStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
