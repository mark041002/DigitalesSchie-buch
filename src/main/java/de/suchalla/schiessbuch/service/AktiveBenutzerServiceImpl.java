package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

/**
 * Dummy-Implementierung: Gibt eine leere Liste zurück.
 * Hier sollte die echte Session-Logik implementiert werden.
 */
@Service
public class AktiveBenutzerServiceImpl implements AktiveBenutzerService {
    @Override
    public List<Benutzer> getEingeloggteBenutzer() {
        // TODO: Session-Tracking implementieren
        return Collections.emptyList();
    }
}

