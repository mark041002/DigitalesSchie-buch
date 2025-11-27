package de.suchalla.schiessbuch.model.enums;

import lombok.Getter;

/**
 * Enum für Waffenarten.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Getter
public enum Waffenart {
    KURZWAFFE("Kurzwaffe"),
    LANGWAFFE("Langwaffe");

    private final String anzeigeText;

    Waffenart(String anzeigeText) {
        this.anzeigeText = anzeigeText;
    }

    @Override
    public String toString() {
        return anzeigeText;
    }
}

