package de.suchalla.schiessbuch.model.enums;

import lombok.Getter;

/**
 * Enum für Schießstand-Typen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Getter
public enum SchiesstandTyp {
    VEREINSGEBUNDEN("Vereinsgebunden"),
    GEWERBLICH("Gewerblich"),
    SONSTIGES("Sonstiges");

    private final String bezeichnung;

    SchiesstandTyp(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
