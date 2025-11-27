package de.suchalla.schiessbuch.repository;

import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.UserToken;
import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import de.suchalla.schiessbuch.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserTokenRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserTokenRepository repository;

    private Benutzer benutzer;

    @BeforeEach
    void init() {
        benutzer = TestDataFactory.createBenutzer(null, "user@example.com");
        em.persist(benutzer);
        em.flush();
    }

    @Test
    void testSaveAndFindByToken() {
        UserToken token = new UserToken("abc123", LocalDateTime.now().plusHours(1), UserTokenTyp.VERIFICATION, benutzer);
        em.persist(token);
        em.flush();

        Optional<UserToken> found = repository.findByToken("abc123");
        assertTrue(found.isPresent());
        assertEquals("abc123", found.get().getToken());
        assertEquals(UserTokenTyp.VERIFICATION, found.get().getTyp());
        assertEquals(benutzer.getId(), found.get().getBenutzer().getId());
    }

    @Test
    void testFindByTokenNotFound() {
        Optional<UserToken> found = repository.findByToken("notexist");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteToken() {
        UserToken token = new UserToken("delete123", LocalDateTime.now().plusHours(1), UserTokenTyp.PASSWORD_RESET, benutzer);
        em.persist(token);
        em.flush();
        Long id = token.getId();

        repository.delete(token);
        em.flush();

        assertNull(em.find(UserToken.class, id));
    }

    @Test
    void testExpiredToken() {
        UserToken token = new UserToken("expired", LocalDateTime.now().minusHours(1), UserTokenTyp.VERIFICATION, benutzer);
        em.persist(token);
        em.flush();

        Optional<UserToken> found = repository.findByToken("expired");
        assertTrue(found.isPresent());
        assertTrue(found.get().getAblaufdatum().isBefore(LocalDateTime.now()));
    }
}

