package de.suchalla.schiessbuch.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalisierungs-Provider für die Anwendung.
 * Unterstützt Deutsch und Englisch.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Component
public class CustomI18NProvider implements I18NProvider {

    private static final Locale LOCALE_DE = new Locale("de", "DE");
    private static final Locale LOCALE_EN = new Locale("en", "GB");

    @Override
    public List<Locale> getProvidedLocales() {
        return Arrays.asList(LOCALE_DE, LOCALE_EN);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            String value = bundle.getString(key);

            // Parameter ersetzen wenn vorhanden
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    value = value.replace("{" + i + "}", params[i].toString());
                }
            }

            return value;
        } catch (Exception e) {
            // Fallback: Gebe den Key zurück wenn Übersetzung nicht gefunden
            return key;
        }
    }
}

