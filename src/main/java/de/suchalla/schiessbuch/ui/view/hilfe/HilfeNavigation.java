package de.suchalla.schiessbuch.ui.view.hilfe;

import java.util.*;

/**
 * Verwaltet die Navigation zwischen Hilfe-Seiten für verschiedene Rollen.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
public final class HilfeNavigation {

    private HilfeNavigation() {
        // Utility-Klasse
    }

    /**
     * Hilfe-Seiten für Schützen in der Reihenfolge der Sidebar.
     */
    public static final List<String> SCHUETZE_PAGES = List.of(
            "dashboard",
            "meine-eintraege",
            "neuer-eintrag",
            "meine-vereine",
            "profil"
    );

    /**
     * Hilfe-Seiten für Aufseher in der Reihenfolge der Sidebar.
     */
    public static final List<String> AUFSEHER_PAGES = List.of(
            "eintragsverwaltung",
            "zertifikate"
    );


    /**
     * Hilfe-Seiten für Vereinschefs in der Reihenfolge der Sidebar.
     */
    public static final List<String> VEREINSCHEF_PAGES = List.of(
            "eintragsverwaltung",    
            "vereinsdetails",
            "mitgliedsverwaltung",
            "zertifikate",
            "verbaende"
    );

    /**
     * Hilfe-Seiten für Administratoren in der Reihenfolge der Sidebar.
     */
    public static final List<String> ADMIN_PAGES = List.of(
            "verbaende-verwaltung",
            "vereine-verwaltung",
            "schiesstaende-verwaltung",
            "mitglieder-verwaltung",
            "zertifikate"
    );

    /**
     * Map aller Rollen zu ihren Hilfe-Seiten.
     */
    private static final Map<String, List<String>> ROLE_PAGES = Map.of(
            "schuetze", SCHUETZE_PAGES,
            "aufseher", AUFSEHER_PAGES,
            "vereinschef", VEREINSCHEF_PAGES,
            "admin", ADMIN_PAGES
    );

    /**
     * Gibt die Hilfe-Seiten für eine bestimmte Rolle zurück.
     */
    public static List<String> getPagesForRole(String role) {
        return ROLE_PAGES.getOrDefault(role.toLowerCase(), Collections.emptyList());
    }

    /**
     * Gibt den Index der aktuellen Seite zurück.
     */
    public static int getCurrentIndex(String role, String currentPage) {
        List<String> pages = getPagesForRole(role);
        return pages.indexOf(currentPage);
    }

    /**
     * Gibt die vorherige Seite zurück oder null, wenn es keine gibt.
     */
    public static String getPreviousPage(String role, String currentPage) {
        List<String> pages = getPagesForRole(role);
        int currentIndex = pages.indexOf(currentPage);
        if (currentIndex > 0) {
            return pages.get(currentIndex - 1);
        }
        return null;
    }

    /**
     * Gibt die nächste Seite zurück oder null, wenn es keine gibt.
     */
    public static String getNextPage(String role, String currentPage) {
        List<String> pages = getPagesForRole(role);
        int currentIndex = pages.indexOf(currentPage);
        if (currentIndex >= 0 && currentIndex < pages.size() - 1) {
            return pages.get(currentIndex + 1);
        }
        return null;
    }

    /**
     * Gibt den Titel für eine Hilfe-Seite zurück.
     */
    public static String getPageTitle(String page) {
        return switch (page) {
            case "dashboard" -> "Dashboard";
            case "meine-eintraege" -> "Meine Einträge";
            case "neuer-eintrag" -> "Neuer Eintrag";
            case "meine-vereine" -> "Meine Vereine";
            case "profil" -> "Profil";
            case "eintragsverwaltung" -> "Eintragsverwaltung";
            case "zertifikate" -> "Zertifikate";
            case "schiesstand-details" -> "Schießstanddetails";
            case "vereinsdetails" -> "Vereinsdetails";
            case "mitgliedsverwaltung" -> "Mitgliedsverwaltung";
            case "verbaende" -> "Verbände";
            case "verbaende-verwaltung" -> "Verbänderverwaltung";
            case "vereine-verwaltung" -> "Vereinsverwaltung";
            case "schiesstaende-verwaltung" -> "Schießständeverwaltung";
            case "mitglieder-verwaltung" -> "Mitgliederverwaltung";
            default -> "Hilfe";
        };
    }

    /**
     * Gibt die Route für eine Hilfe-Seite zurück.
     */
    public static String getPageRoute(String role, String page) {
        return "hilfe/" + role + "/" + page;
    }
}
