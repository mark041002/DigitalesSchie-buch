# Digitales Schießbuch

## Projektbeschreibung

Das Digitale Schießbuch ist eine webbasierte Anwendung zur Verwaltung und Dokumentation von Schießnachweisen für Sportschützen. Die Anwendung ermöglicht die digitale Erfassung, Signierung und Verwaltung von Schießübungen gemäß den Anforderungen deutscher Schießsportverbände.

## Autor

Markus Suchalla

## Technologie-Stack

### Backend
- **Java 21** - Programmiersprache
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

## Architektur

Die Anwendung folgt einer mehrschichtigen Architektur (Layered Architecture):

```
┌─────────────────────────────────────┐
│     Presentation Layer (Views)      │  Vaadin UI-Komponenten
├─────────────────────────────────────┤
│      Service Layer (Business)       │  Geschäftslogik
├─────────────────────────────────────┤
│     Repository Layer (Data Access)  │  JPA Repositories
├─────────────────────────────────────┤
│       Domain Layer (Entities)       │  JPA Entities & DTOs
└─────────────────────────────────────┘
```

### Komponenten

**Domain Layer:**
- Entities: JPA-Entitäten für Datenbankzugriff
- DTOs: Data Transfer Objects für sichere Datenübertragung
- Mapper: Konvertierung zwischen Entities und DTOs
- Enums: Typsichere Aufzählungen

**Repository Layer:**
- JPA Repositories mit @EntityGraph für optimierte Queries
- Custom Queries für komplexe Abfragen

**Service Layer:**
- Geschäftslogik und Validierung
- Transaktionsverwaltung
- E-Mail-Benachrichtigungen
- PDF-Export
- PKI/Signatur-Services

**Presentation Layer:**
- Vaadin Views mit MVC-Pattern
- Responsive UI-Komponenten
- Formular-Validierung
- Rollenbasierte Zugriffskontrolle

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

```bash
docker-compose up -d
```

## Sicherheitskonzept

### Authentifizierung
- Spring Security mit formularbasiertem Login
- Passwort-Hashing mit BCrypt
- Session-basierte Authentifizierung
- E-Mail-Verifizierung erforderlich

### Autorisierung
- Rollenbasierte Zugriffskontrolle
- Method-Level Security mit @RolesAllowed
- View-Level Security in Vaadin Routes

### Digitale Signaturen
- RSA 2048-bit Schlüsselpaare
- SHA-256 Hash-Algorithmus
- Öffentliche Schlüssel für Verifikation verfügbar

### Datenschutz
- Passwörter werden nie im Klartext gespeichert
- DTOs vermeiden Exposition sensibler Daten
- Transaktionale Datenbankoperationen
- HTTPS-Unterstützung (Produktionsumgebung)

## Code-Qualität

### Design Patterns
- Repository Pattern (Datenzugriff)
- DTO Pattern (Datenübertragung)
- Factory Pattern (UI-Komponenten)
- Service Layer Pattern (Geschäftslogik)
- Mapper Pattern (Entity-DTO Konvertierung)

### Best Practices
- Clean Code Prinzipien
- SOLID Principles
- @EntityGraph für optimierte JPA Queries
- Generische Interfaces zur Code-Wiederverwendung

### Dokumentation
- Inline-Kommentare für komplexe Logik
- Strukturierte Package-Organisation
- README-Dokumentation

## Testing

```bash
# Unit Tests ausführen
mvn test

```
