# Digitales Schießbuch

Webbasierte Schießnachweisverwaltung für Sportschützen und Vereine.

## 📋 Projektbeschreibung

Das Digitale Schießbuch ist eine moderne Webanwendung zur Verwaltung von Schießnachweisen. Sie digitalisiert den bisher papierbasierten Prozess und bietet eine zentrale Plattform für Schützen, Vereine und gewerbliche Schießstände.

### Hauptfunktionen

- ✅ **Schießnachweisverwaltung**: Digitale Erfassung und Verwaltung von Schießaktivitäten
- ✅ **Digitale Signatur**: Aufseher bestätigen Einträge mit fälschungssicherer Signatur
- ✅ **Rollenbasiertes System**: 6 verschiedene Benutzerrollen (Software-Admin, Admin, Vereins-Admin, Gewerblicher Aufseher, Vereins-Aufseher, Schütze)
- ✅ **PDF-Export**: Generierung von Nachweisen für behördliche Prüfungen
- ✅ **Vereinsverwaltung**: Verwaltung von Mitgliedschaften und Aufsehern
- ✅ **Benachrichtigungssystem**: In-App-Benachrichtigungen bei wichtigen Ereignissen
- ✅ **Mehrfachmitgliedschaft**: Schützen können mehreren Vereinen angehören

## 🛠️ Technologie-Stack

- **Backend**: Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend**: Vaadin 24.3.0
- **Datenbank**: PostgreSQL
- **PDF-Export**: Apache PDFBox + Boxable
- **E-Mail**: Spring Mail
- **Build-Tool**: Maven

## 📦 Voraussetzungen

- Java 17 oder höher
- PostgreSQL 12 oder höher
- Maven 3.6 oder höher

## 🚀 Installation und Start

### 1. PostgreSQL-Datenbank einrichten

```sql
CREATE DATABASE schiessbuch;
```

Falls Sie andere Zugangsdaten verwenden, passen Sie diese in `src/main/resources/application.properties` an.

### 2. Projekt kompilieren

```bash
mvn clean install
```

### 3. Anwendung starten

```bash
mvn spring-boot:run
```

Die Anwendung ist dann unter http://localhost:8000 erreichbar.

## 👥 Standard-Benutzer

Bei der ersten Initialisierung werden automatisch Testbenutzer erstellt:

| Benutzername        | Passwort | Rolle                  |
|---------------------|----------|------------------------|
| admin@test.de       | admin123 | Software-Administrator |
| schuetze@test.de    | test123 | Schütze                |
| aufseher@test.de    | test123 | Vereins-Aufseher       |
| vereinschef@test.de | test123 | Vereinschef            |

## 📊 Datenbankschema

Die Anwendung verwendet folgende Hauptentitäten:

- **Benutzer**: Systembenutzer mit Rollen
- **Verband**: Schützenverbände (z.B. DSB)
- **Verein**: Sportvereine
- **Vereinsmitgliedschaft**: Zuordnung Benutzer ↔ Verein
- **Disziplin**: Schießdisziplinen
- **Schiesstand**: Schießstände (vereinsgebunden oder gewerblich)
- **SchiessnachweisEintrag**: Einzelne Schießnachweise
- **Benachrichtigung**: System-Benachrichtigungen

## 🔐 Sicherheit

- Passwörter werden mit BCrypt verschlüsselt
- Spring Security für Authentifizierung und Autorisierung
- Rollenbasierte Zugriffskontrolle
- Digitale Signaturen (SHA-256) für Einträge
- CSRF-Schutz durch Spring Security

## 📱 Benutzerrollen und Berechtigungen

### Software-Admin / Admin
- Verwaltung von Verbänden, Vereinen, Schießständen und Disziplinen
- Volle Systemrechte

### Vereins-Admin
- Verwaltung der Vereinsmitglieder
- Ernennung von Aufsehern
- Verwaltung vereinseigener Schießstände

### Gewerblicher Aufseher
- Signierung von Einträgen an gewerblichen Schießständen
- Verwaltung des Schießstands

### Vereins-Aufseher
- Signierung von Einträgen der Vereinsmitglieder
- Einsicht in Vereinsaktivitäten

### Schütze
- Erfassung eigener Schießnachweise
- Vereinsbeitritt
- PDF-Export der Nachweise
- Profilverwaltung

## 📄 PDF-Export

Schützen können ihre signierten Einträge als PDF exportieren:
- Filterung nach Zeitraum
- Nur signierte Einträge werden exportiert
- Fälschungssichere Darstellung mit Aufseher-Informationen

## 📧 E-Mail-Konfiguration

Für die Passwort-Reset-Funktion muss ein SMTP-Server konfiguriert werden. 
Die Standardkonfiguration nutzt einen lokalen Test-SMTP-Server (Port 1025).

Für Produktion passen Sie die Einstellungen in `application.properties` an:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ihre-email@gmail.com
spring.mail.password=ihr-passwort
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🎨 Benutzeroberfläche

Die Anwendung verwendet Vaadin mit dem Lumo-Theme und bietet:
- Responsives Design
- Intuitive Navigation
- Filterbare Grids
- Formulare mit Validierung
- Benachrichtigungssystem

## 📝 Entwicklung

### Projektstruktur

```
src/main/java/de/suchalla/schiessbuch/
├── config/              # Konfigurationsklassen
├── model/
│   ├── entity/         # JPA-Entitäten
│   └── enums/          # Enumerationen
├── repository/          # Spring Data Repositories
├── security/            # Security-Konfiguration
├── service/             # Business-Logik
└── ui/
    └── view/           # Vaadin-Views
```

### Code-Konventionen

- Alle Klassen sind vollständig in Deutsch dokumentiert (JavaDoc)
- Objektorientierte Architektur
- Service-Layer für Geschäftslogik
- Repository-Pattern für Datenzugriff
- DTO-Pattern wo sinnvoll

## 🐛 Bekannte Einschränkungen

- E-Mail-Versand erfordert SMTP-Konfiguration
- PDF-Export nutzt einfache Tabellendarstellung
- Keine digitale PKI-Zertifikate (vereinfachte SHA-256-Signatur)

## 📚 Weitere Entwicklung

Mögliche Erweiterungen:
- [ ] Mobile App (Progressive Web App)
- [ ] Echte PKI-Zertifikate für Signaturen
- [ ] Statistiken und Auswertungen
- [ ] Import/Export von Daten
- [ ] Multi-Mandanten-Fähigkeit
- [ ] REST-API für Drittanbieter

## 📄 Lizenz

Dieses Projekt wurde im Rahmen einer Bachelorarbeit entwickelt.

## 👨‍💻 Autor

**Markus Suchalla**  
Bachelorarbeit - Digitales Schießbuch  
Version 1.0.0  
Datum: Oktober 2025

---

Bei Fragen oder Problemen erstellen Sie bitte ein Issue im Repository.
