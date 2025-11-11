# 🎨 CSS-Struktur und Verwendungsanleitung

## Übersicht der Dateien

```
frontend/themes/
├── variables.css          # CSS-Variablen, Farben, Gradienten
├── layout.css            # Layouts: View, Content, Form, Grid Container
├── cards.css             # Karten, Stats, Details, Empty-State
├── notifications.css     # Info-, Warning-, Error-, Success-Boxen
├── dialogs.css           # Dialog-Styling, Responsive Dialog-Größen
├── buttons.css           # Button-Styling, Hover-Effekte
├── animations.css        # Animationen, Effekte, Status-Indikatoren
└── responsive.css        # Media-Queries, Print-Optimierungen
```

---

## 📋 Quick-Reference Checkliste

### Für neue View-Dateien

- [ ] `addClassName("view-container")` auf VerticalLayout
- [ ] `addClassName("content-wrapper")` für zentrierte Inhalte
- [ ] `addClassName("gradient-header")` für Header-Bereiche
- [ ] `addClassName("info-box")` für Informations-Boxen
- [ ] `addClassName("form-container")` für Formulare
- [ ] `addClassName("grid-container")` für Grids/Tabellen
- [ ] `addClassName("stat-card")` für Dashboard-Statistiken
- [ ] `addClassName("empty-state")` für leere Zustände

### Für Buttons

- [ ] `.neuer-eintrag-btn` für grüne Success-Buttons
- [ ] `ButtonVariant.LUMO_PRIMARY` für Haupt-Buttons
- [ ] `ButtonVariant.LUMO_ERROR` für Löschen-Buttons
- [ ] `theme~="error"` für dunklere Fehler-Buttons

### Für Dialog-Elemente

- [ ] Responsive Breakpoints beachten (1400px, 1200px, 992px, 768px, 480px)
- [ ] `vaadin-dialog-overlay` Selektoren verwenden
- [ ] Padding und Gap-Einstellungen aus dialogs.css verwenden

---

## 🎯 Beispiele für häufige Fälle

### 1. Einfache Admin-View erstellen

```java
@Route(value = "admin/meine-views", layout = MainLayout.class)
public class MeineViewVerwaltungView extends VerticalLayout {
    
    public MeineViewVerwaltungView() {
        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");
        
        // Content-Wrapper
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");
        
        // Header
        Div header = new Div();
        header.addClassName("gradient-header");
        H2 title = new H2("Meine Verwaltung");
        title.getStyle().set("margin", "0");
        header.add(title);
        contentWrapper.add(header);
        
        // Info-Box
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        Icon icon = VaadinIcon.INFO_CIRCLE.create();
        Paragraph text = new Paragraph("Hier können Sie Ihre Daten verwalten");
        infoBox.add(icon, text);
        contentWrapper.add(infoBox);
        
        // Form-Container
        Div formContainer = new Div();
        formContainer.addClassName("form-container");
        FormLayout form = new FormLayout();
        // ... Formular-Felder hinzufügen
        formContainer.add(form);
        contentWrapper.add(formContainer);
        
        add(contentWrapper);
    }
}
```

### 2. Dashboard mit Stats erstellen

```java
private Div createStatsGrid() {
    Div grid = new Div();
    grid.addClassName("stats-grid");
    
    // Stat-Karten
    Div card1 = new Div();
    card1.addClassName("stat-card");
    card1.getStyle().set("border-left", "4px solid var(--lumo-primary-color)");
    H3 label1 = new H3("Statistik 1");
    H4 value1 = new H4("123");
    card1.add(label1, value1);
    
    Div card2 = new Div();
    card2.addClassName("stat-card");
    card2.getStyle().set("border-left", "4px solid var(--lumo-success-color)");
    H3 label2 = new H3("Statistik 2");
    H4 value2 = new H4("456");
    card2.add(label2, value2);
    
    grid.add(card1, card2);
    return grid;
}
```

### 3. Grid mit Action-Buttons

```java
Grid<MyEntity> grid = new Grid<>(MyEntity.class, false);
grid.addClassName("rounded-grid");

grid.addColumn(MyEntity::getName).setHeader("Name");
grid.addColumn(MyEntity::getDescription).setHeader("Beschreibung");

// Action-Buttons
grid.addComponentColumn(entity -> {
    HorizontalLayout actions = new HorizontalLayout();
    
    Button editBtn = new Button("Bearbeiten", VaadinIcon.EDIT.create());
    editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
    
    Button deleteBtn = new Button("Löschen", VaadinIcon.TRASH.create());
    deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
    
    actions.add(editBtn, deleteBtn);
    return actions;
}).setHeader("Aktionen");
```

### 4. Info-Box mit verschiedenen Typen

```java
// Info-Box (blau)
Div infoBox = new Div();
infoBox.addClassName("info-box");
infoBox.add(VaadinIcon.INFO_CIRCLE.create(), new Paragraph("Information"));

// Warning-Box (gelb)
Div warningBox = new Div();
warningBox.addClassName("warning-box");
warningBox.add(VaadinIcon.WARNING.create(), new Paragraph("Warnung"));

// Error-Box (rot)
Div errorBox = new Div();
errorBox.addClassName("error-box");
errorBox.add(new H3("Fehler"), new Paragraph("Es ist ein Fehler aufgetreten"));

// Success-Box (grün)
Div successBox = new Div();
successBox.addClassName("success-box");
successBox.add(new H3("Erfolg"), new Paragraph("Erfolgreich gespeichert"));
```

### 5. Dialog mit responsiven Formularen

```java
Dialog dialog = new Dialog();

VerticalLayout dialogContent = new VerticalLayout();
dialogContent.setPadding(true);
dialogContent.setSpacing(true);

TextField nameField = new TextField("Name");
nameField.setWidthFull();

TextArea descriptionField = new TextArea("Beschreibung");
descriptionField.setWidthFull();
descriptionField.setMinHeight("100px");

HorizontalLayout buttons = new HorizontalLayout();
Button saveBtn = new Button("Speichern");
Button cancelBtn = new Button("Abbrechen", e -> dialog.close());
buttons.add(saveBtn, cancelBtn);

dialogContent.add(nameField, descriptionField, buttons);
dialog.add(dialogContent);

// Die responsive Anpassung erfolgt automatisch durch die CSS!
dialog.open();
```

---

## 🔍 CSS-Klassen nach Kategorie

### Layout
- `.view-container` - Haupt-View Container
- `.content-wrapper` - Zentrierter Inhalts-Wrapper
- `.form-container` - Formular-Container
- `.grid-container` - Grid/Table-Container
- `.rounded-grid` - Abgerundetes Grid
- `.button-container` - Button-Layout

### Komponenten
- `.gradient-header` - Header mit Gradient
- `.info-box`, `.warning-box`, `.error-box`, `.success-box` - Benachrichtigungs-Boxen
- `.stat-card` - Statistik-Karten
- `.card` - Allgemeine Karten
- `.empty-state` - Leere Zustand-Anzeige

### Interaktiv
- `.neuer-eintrag-btn` - Grüner Success-Button
- `.card-hover` - Hover-Effekt für Karten
- `.icon-rotate-hover` - Icon Rotation bei Hover

### Effekte
- `.badge-pulse` - Pulsierungs-Animation
- `.skeleton` - Loading-Animation
- `.shake` - Schüttel-Animation
- `.fade-in` - Einblend-Animation
- `.gradient-text` - Gradient-Text
- `.glass-effect` - Glassmorphism-Effekt
- `.shadow-soft`, `.shadow-medium`, `.shadow-hard` - Schatten-Varianten
- `.status-indicator` - Status-Indikatoren

### Status
- `.badge-success`, `.badge-warning`, `.badge-error` - Badge-Farben
- `.status-indicator.active`, `.status-indicator.pending`, `.status-indicator.inactive` - Status-Punkte

### Utilities
- `.align-right` - Rechtsbündiger Text
- `.divider` - Trennlinie
- `.striped-layout` - Zebra-Streifen

---

## 📱 Responsive Breakpoints

```css
Desktop (1400px+)
├── Dialoge: max-width 1200px
└── Full-Width Layouts

Große Bildschirme (1200px - 1399px)
├── Dialoge: max-width 1000px
└── Optimierte Spalten

Mittlere Bildschirme (992px - 1199px)
├── Dialoge: max-width 900px
└── 2-Spalten Layouts

Tablets (768px - 991px)
├── Dialoge: Full-Width
├── 1-Spalten Layouts
└── Buttons untereinander

Smartphones (480px - 767px)
├── Dialoge: Full-Width mit Padding
├── Volle Breite für alle Elemente
└── Buttons volle Breite

Sehr kleine Bildschirme (<480px)
├── Reduziertes Padding
├── Vergrößerte Touch-Targets
└── Vereinfachtes Layout
```

---

## 🎓 Best Practices

### DO ✅
- Verwenden Sie die vordefinierten CSS-Klassen statt inline Styles
- Beachten Sie responsive Breakpoints in neuen Komponenten
- Nutzen Sie `addClassName()` für mehrere Klassen
- Konsistent mit Spacing-Variablen arbeiten (`var(--lumo-space-*)`)

### DON'T ❌
- Keine inline Styles für wiederkehrende Muster
- Nicht gegen die CSS-Struktur arbeiten
- Keine neuen CSS-Klassen hinzufügen, ohne sie in die entsprechende Datei zu schreiben
- Nicht beide alte und neue CSS-Dateien mischen

---

## 🐛 Häufige Probleme und Lösungen

### Problem: Dialog ist zu klein
**Lösung:** `dialogs.css` hat responsive Breiten, aber verwenden Sie nicht `max-width` im Java-Code

### Problem: Grid wird nicht responsive
**Lösung:** Stellen Sie sicher, dass `grid-container` Klasse auf dem Parent-Div ist, nicht auf dem Grid selbst

### Problem: Mobile-Ansicht sieht komisch aus
**Lösung:** Überprüfen Sie `responsive.css` und beachten Sie die Breakpoints

### Problem: Button-Styling wird nicht angewendet
**Lösung:** Verwenden Sie `addThemeVariants()` oder `addClassName()` mit korrektem Klassennamen

---

## 📚 Weitere Ressourcen

- Lumo Theme Dokumentation: https://vaadin.com/docs/latest/styling
- CSS-Variablen Referenz: `variables.css`
- MainLayout.java - Beispiel für CSS-Importe

