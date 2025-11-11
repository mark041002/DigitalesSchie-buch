package de.suchalla.schiessbuch.model.enums;

import lombok.Getter;

/**
 * Enum für den Status von Schießnachweis-Einträgen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Getter
public enum EintragStatus {
    OFFEN("Offen"),
    UNSIGNIERT("Unsigniert"),
    SIGNIERT("Signiert"),

    ABGELEHNT("Abgelehnt");

    private final String bezeichnung;

    EintragStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
