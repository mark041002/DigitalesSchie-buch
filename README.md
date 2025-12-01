# Digitales Schießbuch

## Projektbeschreibung

Das Digitale Schießbuch ist eine webbasierte Anwendung zur Verwaltung und Dokumentation von Schießnachweisen für Sportschützen. Die Anwendung ermöglicht die digitale Erfassung, Signierung und Verwaltung von Schießübungen gemäß den Anforderungen deutscher Schießsportverbände.

## Autor

Markus Suchalla

## Technologie-Stack

### Backend
- **Java 23** - Programmiersprache
- **Spring Boot 3.x** - Application Framework
- **Spring Data JPA** - Datenzugriff und Persistenz
- **Spring Security** - Authentifizierung und Autorisierung
- **PostgreSQL/H2** - Relationale Datenbank
- **Maven** - Build-Management und Dependency Management

### Frontend
- **Vaadin 24.3** - Java-basiertes Web-Framework
- **Responsive CSS** - Mobile-first Design
- **Lumo Theme** - UI-Komponenten-Theme

### Sicherheit & Kryptographie
- **Public Key Infrastructure (PKI)** - Digitale Signaturen
- **X.509 Zertifikate** - Zertifikatsverwaltung
- **Java Security API** - Kryptographische Operationen

### Dokumenten-Generierung
- **Apache PDFBox** - PDF-Export von Schießnachweisen
- **HTML Templates** - E-Mail-Benachrichtigungen

## Funktionsumfang

### Benutzerverwaltung
- Registrierung und E-Mail-Verifizierung
- Passwort-Reset-Funktionalität
- Rollenbasierte Berechtigungen (Schütze, Aufseher, Vereinschef, Administrator)
- Profilverwaltung

### Vereinsverwaltung
- Verwaltung von Vereinen und Verbänden
- Mitgliedschaftsverwaltung mit Genehmigungsworkflow
- Schießstandverwaltung
- Aufseherzuweisung

### Schießnachweis-Verwaltung
- Erfassung von Schießübungen
- Disziplinverwaltung
- Digitale Signierung durch Aufseher
- Statusverfolgung (Unsigniert, Signiert, Abgelehnt)

### Digitale Signaturen
- PKI-basierte Zertifikatsverwaltung
- X.509 Zertifikate für Aufseher
- Digitale Signierung von Schießnachweisen
- Zertifikatsverifizierung
- Öffentlich zugängliche Verifikationsseite

### Dokumenten-Export
- PDF-Export von Schießnachweisen
- Zeitraumbasierte Filterung
- Inklusive digitaler Signaturinformationen

### E-Mail-Benachrichtigungen
- Template-basiertes E-Mail-System
- Benachrichtigungen für:
  - Beitrittsanfragen
  - Signaturanfragen
  - Status-Updates
  - E-Mail-Verifizierung
  - Passwort-Reset

## Installation und Setup

### Voraussetzungen

- Java Development Kit (JDK) 23 oder höher
- Apache Maven 3.8 oder höher
- PostgreSQL 16 oder höher (optional, H2 für Entwicklung)
- Einen SMTP-Server für E-Mail-Benachrichtigungen (optional, für Tests kann ein lokaler SMTP-Server verwendet werden wie smpt4dev)

### Datenbank konfigurieren

Bearbeiten Sie `src/main/resources/application.properties`:

```properties
# PostgreSQL Konfiguration
spring.datasource.url=jdbc:postgresql://localhost:5432/schiessbuch
spring.datasource.username=<username>
spring.datasource.password=<password>

# E-Mail Konfiguration
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=<email>
spring.mail.password=<password>
```

### Anwendung kompilieren

```bash
mvn clean install
```

### Anwendung starten

```bash
mvn spring-boot:run
```

Die Anwendung ist dann verfügbar unter: `http://localhost:8000`

### Docker Deployment

Für Docker-Deployment existiert eine separate Konfiguration:
Diese ist dafür ausgelegt, auf dem FH Server zu laufen, da sie den SMTP Server der FH verwendet.


```bash
docker-compose up -d
```

## Testing

Um die Tests seperat auszuführen, verwenden Sie den folgenden Befehl:
```bash
# Unit Tests ausführen
mvn test

```
