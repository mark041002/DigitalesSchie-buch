package de.suchalla.schiessbuch.ui.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login-View für die Anwendung.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@Route("login")
@PageTitle("Anmeldung | Digitales Schießbuch")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Deutsche Übersetzungen für das Login-Formular
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Anmeldung");
        i18nForm.setUsername("Benutzername");
        i18nForm.setPassword("Passwort");
        i18nForm.setSubmit("Anmelden");
        i18nForm.setForgotPassword("Passwort vergessen?");
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Anmeldung fehlgeschlagen");
        i18nErrorMessage.setMessage("Bitte überprüfen Sie Ihren Benutzernamen und Ihr Passwort und versuchen Sie es erneut.");
        i18n.setErrorMessage(i18nErrorMessage);

        loginForm.setI18n(i18n);
        loginForm.setAction("login");
        loginForm.addForgotPasswordListener(e ->
            getUI().ifPresent(ui -> ui.navigate("passwort-vergessen"))
        );

        H1 title = new H1("Digitales Schießbuch");

        RouterLink registerLink = new RouterLink("Noch kein Konto? Jetzt registrieren", RegisterView.class);

        add(title, loginForm, registerLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
