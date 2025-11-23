package de.suchalla.schiessbuch.model.entity;

import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTokenTest {

    private Benutzer benutzer;
    private UserToken token;

    @BeforeEach
    void setUp() {
        benutzer = TestDataFactory.createBenutzer(1L, "user@example.com");
        token = new UserToken("abc123", LocalDateTime.now().plusHours(1), UserTokenTyp.VERIFICATION, benutzer);
    }

    @Test
    void testConstructor() {
        assertEquals("abc123", token.getToken());
        assertEquals(UserTokenTyp.VERIFICATION, token.getTyp());
        assertEquals(benutzer, token.getBenutzer());
        assertTrue(token.getAblaufdatum().isAfter(LocalDateTime.now()));
    }

    @Test
    void testSetters() {
        token.setToken("xyz789");
        token.setTyp(UserTokenTyp.PASSWORD_RESET);
        token.setAblaufdatum(LocalDateTime.now().plusMinutes(30));
        assertEquals("xyz789", token.getToken());
        assertEquals(UserTokenTyp.PASSWORD_RESET, token.getTyp());
    }

    @Test
    void testExpiredTokenScenario() {
        token.setAblaufdatum(LocalDateTime.now().minusMinutes(1));
        assertTrue(token.getAblaufdatum().isBefore(LocalDateTime.now()));
    }

    @Test
    void testEqualsHashCodeIdentity() {
        UserToken t1 = new UserToken("tok1", LocalDateTime.now().plusHours(1), UserTokenTyp.VERIFICATION, benutzer);
        UserToken t2 = new UserToken("tok1", LocalDateTime.now().plusHours(2), UserTokenTyp.VERIFICATION, benutzer);
        // Ohne Überschriebenes equals -> unterschiedliche Instanzen
        assertNotEquals(t1, t2);
    }
}

