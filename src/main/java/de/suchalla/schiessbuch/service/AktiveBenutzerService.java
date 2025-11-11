package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import java.util.List;

/**
 * Service zur Ermittlung aktuell eingeloggter Benutzer.
 */
public interface AktiveBenutzerService {
    /**
     * Gibt alle aktuell eingeloggten Benutzer zurück.
     * @return Liste eingeloggter Benutzer
     */
    List<Benutzer> getEingeloggteBenutzer();
}

