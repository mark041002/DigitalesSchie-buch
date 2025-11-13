package de.suchalla.schiessbuch.ui.view.administrativ;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.service.BenutzerService;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * View für Benutzerverwaltung - zeigt alle Benutzer im System.
 *
 * @author Markus Suchalla
 * @version 1.1.0
 */
@Route(value = "admin/benutzer", layout = MainLayout.class)
@PageTitle("Benutzerverwaltung | Digitales Schießbuch")
@RolesAllowed({"ADMIN"})
@Slf4j
public class MitgliederVerwaltungView extends VerticalLayout {

    private final BenutzerService benutzerService;
    private final Grid<Benutzer> grid = new Grid<>(Benutzer.class, false);
    private Div emptyStateMessage;

    public MitgliederVerwaltungView(BenutzerService benutzerService) {
        this.benutzerService = benutzerService;

        setSpacing(false);
        setPadding(false);
        setSizeFull();
        addClassName("view-container");

        createContent();
        updateGrid();
    }

    private void createContent() {
        // Content-Wrapper für zentrierte Inhalte
        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setSpacing(false);
        contentWrapper.setPadding(false);
        contentWrapper.addClassName("content-wrapper");

        // Header-Bereich
        Div header = new Div();
        header.addClassName("gradient-header");
        header.setWidthFull();

        H2 title = new H2("Benutzerverwaltung");
        title.getStyle().set("margin", "0");

        header.add(title);
        contentWrapper.add(header);

        // Info-Box mit modernem Styling
        Div infoBox = new Div();
        infoBox.addClassName("info-box");
        infoBox.setWidthFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("20px");

        Paragraph beschreibung = new Paragraph(
                "Verwalten Sie alle Benutzer im System. Sie können Passwörter ändern und Benutzer löschen."
        );
        beschreibung.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("margin", "0");

        infoBox.add(infoIcon, beschreibung);
        contentWrapper.add(infoBox);

        // Grid-Container mit weißem Hintergrund
        Div gridContainer = new Div();
        gridContainer.addClassName("grid-container");
        gridContainer.setWidthFull();
        gridContainer.getStyle()
                .set("flex", "1 1 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("min-height", "0")
                .set("overflow-x", "auto")
                .set("overflow-y", "auto");

        // Empty State Message
        emptyStateMessage = new Div();
        emptyStateMessage.addClassName("empty-state");
        emptyStateMessage.setWidthFull();
        emptyStateMessage.getStyle()
                .set("text-align", "center")
                .set("padding", "var(--lumo-space-xl)")
                .set("color", "var(--lumo-secondary-text-color)");

        Icon emptyIcon = VaadinIcon.USERS.create();
        emptyIcon.setSize("48px");
        emptyIcon.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Paragraph emptyText = new Paragraph("Keine Benutzer vorhanden.");
        emptyText.getStyle().set("margin", "0");

        emptyStateMessage.add(emptyIcon, emptyText);
        emptyStateMessage.setVisible(false);

        // Grid Setup
        setupGrid();

        gridContainer.add(emptyStateMessage, grid);
        contentWrapper.add(gridContainer);
        add(contentWrapper);
    }

    private void setupGrid() {
        grid.setHeight("100%");
        grid.setWidthFull();
        grid.getStyle()
                .set("min-height", "400px");
        grid.addClassName("rounded-grid");
        grid.setColumnReorderingAllowed(true);

        grid.addColumn(Benutzer::getId)
                .setHeader("ID")
                .setWidth("80px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true)
                .setClassNameGenerator(item -> "align-right");

        grid.addColumn(Benutzer::getVollstaendigerName)
                .setHeader("Name")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(Benutzer::getEmail)
                .setHeader("E-Mail")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        grid.addColumn(b -> b.getRolle() != null ? b.getRolle().name() : "-")
                .setHeader("Rolle")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true);

        grid.addColumn(b -> b.getErstelltAm() != null ? b.getErstelltAm().toLocalDate().toString() : "-")
                .setHeader("Erstellt")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(true);

        // Aktionen-Spalte
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Aktionen")
                .setWidth("240px")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setResizable(false);

        grid.getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = '.align-right { text-align: right; }';" +
                        "document.head.appendChild(style);"
        );
    }

    private HorizontalLayout createActionButtons(Benutzer benutzer) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.getStyle().set("flex-wrap", "nowrap");

        // Entferne Passwort-Ändern-Button
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        loeschenButton.addClickListener(e -> bestaetigeLoesch(benutzer));

        layout.add(loeschenButton);
        return layout;
    }

    private void bestaetigeLoesch(Benutzer benutzer) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Benutzer löschen?");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Paragraph("Möchten Sie den Benutzer " + benutzer.getVollstaendigerName() + " wirklich löschen?"));
        layout.add(new Paragraph("Die Daten werden anonymisiert."));

        Button loeschenButton = new Button("Ja, löschen", e -> {
            try {
                benutzerService.loescheBenutzer(benutzer);
                Notification.show("Benutzer gelöscht")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                updateGrid();
            } catch (Exception ex) {
                log.error("Fehler beim Löschen", ex);
                Notification.show("Fehler: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        loeschenButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button abbrechenButton = new Button("Abbrechen", e -> confirmDialog.close());

        confirmDialog.add(layout);
        confirmDialog.getFooter().add(abbrechenButton, loeschenButton);
        confirmDialog.open();
    }

    private void updateGrid() {
        List<Benutzer> alleBenutzzer = benutzerService.findAlleBenutzer();
        grid.setItems(alleBenutzzer);
        grid.getDataProvider().refreshAll();

        // Zeige/Verstecke Empty State Message
        boolean isEmpty = alleBenutzzer.isEmpty();
        grid.setVisible(!isEmpty);
        emptyStateMessage.setVisible(isEmpty);
    }
}
