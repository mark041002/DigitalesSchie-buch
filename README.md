
## Standard-Benutzer

Bei der ersten Initialisierung werden automatisch Testbenutzer erstellt:

| Benutzername        | Passwort | Rolle                  |
|---------------------|----------|------------------------|
| admin@schiessbuch.de| admin123 | Software-Administrator |
| schuetze@test.de    | test123 | Schütze                |
| aufseher@test.de    | test123 | Vereins-Aufseher       |
| vereinschef@test.de | test123 | Vereinschef            |
| standaufseher@test.de | test123 | Standaufseher         |


# Alle Container bauen und starten
docker-compose up --build

# Logs anschauen
docker-compose logs -f

# Stoppen
docker-compose down

# Alles löschen (inkl. Datenbank!)
docker-compose down -v