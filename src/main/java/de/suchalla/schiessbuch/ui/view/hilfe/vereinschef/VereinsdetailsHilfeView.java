package de.suchalla.schiessbuch.ui.view.hilfe.vereinschef;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.suchalla.schiessbuch.ui.view.MainLayout;
import de.suchalla.schiessbuch.ui.view.hilfe.BaseHilfeView;
import jakarta.annotation.security.PermitAll;

/**
 * Hilfe-Seite für Vereinsdetails (Vereinschef).
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route(value = "hilfe/vereinschef/vereinsdetails", layout = MainLayout.class)
@PageTitle("Hilfe: Vereinsdetails")
@PermitAll
public class VereinsdetailsHilfeView extends BaseHilfeView {

    public VereinsdetailsHilfeView() {
        super("vereinschef", "vereinsdetails", "var(--lumo-warning-color)");
    }

    @Override
    protected void createContent() {
        Div section = createSection("Vereinsdetails verwalten", VaadinIcon.COG);

        Paragraph intro = new Paragraph(
                "Als Vereinschef können Sie die Details Ihres Vereins verwalten und aktualisieren. " +
                "Halten Sie diese Informationen aktuell."
        );


        Div tippBox = createTippBox(
            "Tipp: Mitglieder finden Ihren Verein über Namen und Adresse. Stellen Sie sicher, dass diese Angaben korrekt sind."
        );

      

        section.add(intro, tippBox);
        addToContent(section);
    }
}
