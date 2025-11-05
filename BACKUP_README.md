# Datenbank Backup-System

## Übersicht

Das System erstellt automatisch täglich um 2:00 Uhr ein Backup der PostgreSQL-Datenbank.
Das Backup wird immer überschrieben, sodass bei einem Systemabsturz ein aktuelles Backup vorhanden ist.

## Voraussetzungen

**PostgreSQL-Tools müssen installiert sein:**
- `pg_dump` für Backup-Erstellung
- `psql` für Wiederherstellung

### Installation unter Windows:
PostgreSQL-Tools sind normalerweise mit PostgreSQL installiert.
Stellen Sie sicher, dass der PostgreSQL-bin-Ordner in der PATH-Umgebungsvariable ist:
```
C:\Program Files\PostgreSQL\<version>\bin
```

### Installation unter Linux:
```bash
sudo apt-get install postgresql-client
```

## Konfiguration

In `application.properties`:

```properties
# Backup-System aktivieren/deaktivieren
backup.enabled=true

# Backup-Verzeichnis (relativ oder absolut)
backup.directory=./backups
```

## Backup-Dateien

### Tägliches Backup (wird überschrieben):
- `./backups/schiessbuch_backup_daily.sql` - Aktuelles tägliches Backup

### Archivierte Backups (mit Zeitstempel):
- `./backups/archive/schiessbuch_backup_YYYYMMDD_HHmmss.sql`
- Werden automatisch nach 7 Tagen gelöscht

## Manuelles Backup erstellen

### Über die Web-Oberfläche:
1. Als Admin einloggen
2. Zu "Admin > Backup" navigieren
3. Button "Backup jetzt erstellen" klicken

### Über die Kommandozeile:
```bash
pg_dump -h localhost -p 5432 -U postgres -F p -f backup.sql schiessbuch
```

## Backup wiederherstellen

### Über die Web-Oberfläche:
1. Als Admin einloggen
2. Zu "Admin > Backup" navigieren
3. "Tägliches Backup wiederherstellen" klicken ODER eigene Backup-Datei hochladen
4. Bestätigen
5. **Anwendung neu starten**

### Über die Kommandozeile:
```bash
# WARNUNG: Überschreibt alle aktuellen Daten!
psql -h localhost -p 5432 -U postgres -d schiessbuch -f backup.sql
```

## Zeitplan

Das automatische Backup läuft täglich um **2:00 Uhr nachts**.

Um die Zeit zu ändern, passen Sie die Cron-Expression in `DatabaseBackupService.java` an:
```java
@Scheduled(cron = "0 0 2 * * ?")  // Sekunde Minute Stunde Tag Monat Wochentag
```

Beispiele:
- `"0 0 3 * * ?"` - Täglich um 3:00 Uhr
- `"0 30 1 * * ?"` - Täglich um 1:30 Uhr
- `"0 0 */6 * * ?"` - Alle 6 Stunden

## Backup-Strategie

1. **Tägliches Backup**: Wird jeden Tag erstellt und überschreibt das vorherige
2. **Archiv**: Die letzten 7 Tagesbackups werden mit Zeitstempel aufbewahrt
3. **Automatische Bereinigung**: Backups älter als 7 Tage werden automatisch gelöscht

## Sicherheit

- Backups enthalten **alle Daten** inklusive Passwörter (gehasht)
- Bewahren Sie Backups sicher auf
- Schützen Sie das Backup-Verzeichnis vor unbefugtem Zugriff

## Troubleshooting

### "pg_dump: command not found"
PostgreSQL-Tools sind nicht im PATH. Fügen Sie den PostgreSQL-bin-Ordner zur PATH-Variable hinzu.

### "Backup fehlgeschlagen mit Exit-Code: 1"
- Prüfen Sie Datenbankverbindung
- Prüfen Sie Benutzer und Passwort in `application.properties`
- Prüfen Sie PostgreSQL-Log-Dateien

### Backup-Verzeichnis wird nicht erstellt
- Prüfen Sie Schreibrechte im Anwendungsverzeichnis
- Verwenden Sie einen absoluten Pfad in der Konfiguration

## Logs

Backup-Aktivitäten werden geloggt:
```
2025-01-28 02:00:00 INFO  - Starte tägliches Datenbank-Backup...
2025-01-28 02:00:05 INFO  - Backup erfolgreich erstellt: ./backups/schiessbuch_backup_daily.sql
```

Bei Fehlern:
```
2025-01-28 02:00:00 ERROR - Fehler beim Erstellen des Backups
```

