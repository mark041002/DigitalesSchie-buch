package de.suchalla.schiessbuch.service.email;

import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class EmailTemplateLoader {
    public static String loadTemplate(String templateName, Map<String, Object> variables) {
        try (InputStream is = EmailTemplateLoader.class.getResourceAsStream("/templates/" + templateName)) {
            if (is == null) {
                log.error("Template nicht gefunden: /templates/{}", templateName);
                return "";
            }
            String template = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            log.debug("Template {} geladen", templateName);
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
            }
            return template;
        } catch (Exception e) {
            log.error("Fehler beim Laden des Templates {}: {}", templateName, e.getMessage(), e);
            return "";
        }
    }
}
