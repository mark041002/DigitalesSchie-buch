package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Waffenarten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum Waffenart {
    KURZWAFFE("Kurzwaffe"),
    LANGWAFFE("Langwaffe");

    private final String anzeigeText;

    Waffenart(String anzeigeText) {
        this.anzeigeText = anzeigeText;
    }

    public String getAnzeigeText() {
        return anzeigeText;
    }

    @Override
    public String toString() {
        return anzeigeText;
    }
}

