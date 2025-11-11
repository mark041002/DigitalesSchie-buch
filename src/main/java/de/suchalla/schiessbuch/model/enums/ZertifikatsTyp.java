package de.suchalla.schiessbuch.model.enums;

/**
 * Enum für Zertifikatstypen in der PKI-Hierarchie.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public enum ZertifikatsTyp {
    /**
     * Root-Zertifikat (Root CA) - oberste Instanz der PKI-Hierarchie.
     */
    ROOT("Root CA"),

    /**
     * Vereinszertifikat (Intermediate CA) - abgeleitet vom Root-Zertifikat.
     */
    VEREIN("Vereinszertifikat"),

    /**
     * Aufseher-Zertifikat (End Entity) - für Vereinsmitglieder/Aufseher,
     * abgeleitet vom Vereinszertifikat.
     */
    AUFSEHER("Aufseher-Zertifikat"),

    /**
     * Schießstandaufseher-Zertifikat (End Entity) - für Aufseher von gewerblichen Schießständen,
     * direkt vom Root-Zertifikat abgeleitet.
     */
    SCHIESSTANDAUFSEHER("Schießstandaufseher-Zertifikat");

    private final String bezeichnung;

    ZertifikatsTyp(String bezeichnung) {
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

