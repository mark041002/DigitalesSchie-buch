package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Zertifikatstypen in der PKI-Hierarchie.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum ZertifikatsTyp {
    ROOT("Root CA"),
    VEREIN("Vereinszertifikat"),
    AUFSEHER("Aufseher-Zertifikat"),
    SCHIESSTANDAUFSEHER("Schießstandaufseher-Zertifikat");

    private final String bezeichnung;

    ZertifikatsTyp(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}

