package de.suchalla.schiessbuch.service;

import de.suchalla.schiessbuch.mapper.BenutzerMapper;
import de.suchalla.schiessbuch.model.dto.BenutzerDTO;
import de.suchalla.schiessbuch.model.entity.Benutzer;
import de.suchalla.schiessbuch.model.entity.UserToken;
import de.suchalla.schiessbuch.model.enums.BenutzerRolle;
import de.suchalla.schiessbuch.model.enums.UserTokenTyp;
import de.suchalla.schiessbuch.repository.BenutzerRepository;
import de.suchalla.schiessbuch.repository.UserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für BenutzerService mit Mockito.
 *
 * @author Markus Suchalla
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class BenutzerServiceTest {

    @Mock
    private BenutzerRepository benutzerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private BenutzerMapper benutzerMapper;

    @Mock
    private de.suchalla.schiessbuch.repository.DigitalesZertifikatRepository digitalesZertifikatRepository;

    @InjectMocks
    private BenutzerService benutzerService;

    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        testBenutzer = Benutzer.builder()
                .id(1L)
                .email("test@example.com")
                .vorname("Max")
                .nachname("Mustermann")
                .passwort("plainPassword")
                .rolle(BenutzerRolle.SCHUETZE)
                .emailVerifiziert(false)
                .build();
    }

    @Test
    void testRegistriereBenutzer_Success() {
        when(benutzerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(benutzerRepository.save(any(Benutzer.class))).thenReturn(testBenutzer);

        benutzerService.registriereBenutzer(testBenutzer);

        verify(benutzerRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("plainPassword");
        verify(benutzerRepository).save(testBenutzer);
        assertEquals(BenutzerRolle.SCHUETZE, testBenutzer.getRolle());
    }

    @Test
    void testRegistriereBenutzer_EmailAlreadyExists() {
        when(benutzerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            benutzerService.registriereBenutzer(testBenutzer);
        });

        verify(benutzerRepository).existsByEmail("test@example.com");
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    void testFindAlleBenutzer() {
        List<Benutzer> benutzerList = Arrays.asList(testBenutzer);
        List<BenutzerDTO> dtoList = Arrays.asList(BenutzerDTO.builder()
                .id(1L)
                .email("test@example.com")
                .build());

        when(benutzerRepository.findAll()).thenReturn(benutzerList);
        when(benutzerMapper.toDTOList(benutzerList)).thenReturn(dtoList);

        List<BenutzerDTO> result = benutzerService.findAlleBenutzer();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(benutzerRepository).findAll();
        verify(benutzerMapper).toDTOList(benutzerList);
    }

    @Test
    void testFindAlleBenutzerEntities() {
        List<Benutzer> benutzerList = Arrays.asList(testBenutzer);
        when(benutzerRepository.findAll()).thenReturn(benutzerList);

        List<Benutzer> result = benutzerService.findAlleBenutzerEntities();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBenutzer, result.get(0));
        verify(benutzerRepository).findAll();
    }

    @Test
    void testAktualisiereBenutzer() {
        when(benutzerRepository.save(any(Benutzer.class))).thenReturn(testBenutzer);

        benutzerService.aktualisiereBenutzer(testBenutzer);

        verify(benutzerRepository).save(testBenutzer);
    }

    @Test
    void testAenderePasswortOhneAltes_Success() {
        when(benutzerRepository.findById(1L)).thenReturn(Optional.of(testBenutzer));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(benutzerRepository.save(any(Benutzer.class))).thenReturn(testBenutzer);

        benutzerService.aenderePasswortOhneAltes(1L, "newPassword");

        verify(benutzerRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword");
        verify(benutzerRepository).save(testBenutzer);
    }

    @Test
    void testAenderePasswortOhneAltes_BenutzerNotFound() {
        when(benutzerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            benutzerService.aenderePasswortOhneAltes(999L, "newPassword");
        });

        verify(benutzerRepository).findById(999L);
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    void testLoescheBenutzer() {
        when(benutzerRepository.findById(testBenutzer.getId())).thenReturn(Optional.of(testBenutzer));
        doNothing().when(digitalesZertifikatRepository).deleteAllByBenutzerId(testBenutzer.getId());
        doNothing().when(userTokenRepository).deleteAllByBenutzer(testBenutzer);
        doNothing().when(benutzerRepository).delete(any(Benutzer.class));

        benutzerService.loescheBenutzer(testBenutzer);

        verify(benutzerRepository).delete(testBenutzer);
    }

    @Test
    void testErstelleVerifizierungsToken() {
        UserToken userToken = new UserToken();
        userToken.setToken("test-token");
        userToken.setTyp(UserTokenTyp.VERIFICATION);

        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        String token = benutzerService.erstelleVerifizierungsToken(testBenutzer);

        assertNotNull(token);
        verify(userTokenRepository).save(any(UserToken.class));
    }

    @Test
    void testBestaetigeEmail_Success() {
        String token = "valid-token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setTyp(UserTokenTyp.VERIFICATION);
        userToken.setAblaufdatum(LocalDateTime.now().plusDays(1));
        userToken.setBenutzer(testBenutzer);

        when(userTokenRepository.findByToken(token)).thenReturn(Optional.of(userToken));
        when(benutzerRepository.save(any(Benutzer.class))).thenReturn(testBenutzer);
        doNothing().when(userTokenRepository).delete(any(UserToken.class));

        boolean result = benutzerService.bestaetigeEmail(token);

        assertTrue(result);
        assertTrue(testBenutzer.isEmailVerifiziert());
        verify(userTokenRepository).findByToken(token);
        verify(benutzerRepository).save(testBenutzer);
        verify(userTokenRepository).delete(userToken);
    }

    @Test
    void testBestaetigeEmail_InvalidToken() {
        String token = "invalid-token";
        when(userTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean result = benutzerService.bestaetigeEmail(token);

        assertFalse(result);
        verify(userTokenRepository).findByToken(token);
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    void testBestaetigeEmail_ExpiredToken() {
        String token = "expired-token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setTyp(UserTokenTyp.VERIFICATION);
        userToken.setAblaufdatum(LocalDateTime.now().minusDays(1)); // Abgelaufen
        userToken.setBenutzer(testBenutzer);

        when(userTokenRepository.findByToken(token)).thenReturn(Optional.of(userToken));
        doNothing().when(userTokenRepository).delete(any(UserToken.class));

        boolean result = benutzerService.bestaetigeEmail(token);

        assertFalse(result);
        verify(userTokenRepository).findByToken(token);
        verify(userTokenRepository).delete(userToken);
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    void testErstellePasswortResetToken() {
        UserToken userToken = new UserToken();
        userToken.setToken("reset-token");
        userToken.setTyp(UserTokenTyp.PASSWORD_RESET);

        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        String token = benutzerService.erstellePasswortResetToken(testBenutzer);

        assertNotNull(token);
        verify(userTokenRepository).save(any(UserToken.class));
    }

    @Test
    void testResetPasswortMitToken_Success() {
        String token = "reset-token";
        String neuesPasswort = "newPassword123";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setTyp(UserTokenTyp.PASSWORD_RESET);
        userToken.setAblaufdatum(LocalDateTime.now().plusHours(2));
        userToken.setBenutzer(testBenutzer);

        when(userTokenRepository.findByToken(token)).thenReturn(Optional.of(userToken));
        when(passwordEncoder.encode(neuesPasswort)).thenReturn("encodedNewPassword");
        when(benutzerRepository.save(any(Benutzer.class))).thenReturn(testBenutzer);
        doNothing().when(userTokenRepository).delete(any(UserToken.class));

        boolean result = benutzerService.resetPasswortMitToken(token, neuesPasswort);

        assertTrue(result);
        verify(userTokenRepository).findByToken(token);
        verify(passwordEncoder).encode(neuesPasswort);
        verify(benutzerRepository).save(testBenutzer);
        verify(userTokenRepository).delete(userToken);
    }

    @Test
    void testResetPasswortMitToken_InvalidToken() {
        String token = "invalid-token";
        when(userTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        boolean result = benutzerService.resetPasswortMitToken(token, "newPassword");

        assertFalse(result);
        verify(userTokenRepository).findByToken(token);
        verify(benutzerRepository, never()).save(any());
    }

    @Test
    void testResetPasswortMitToken_WrongTokenType() {
        String token = "wrong-type-token";
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setTyp(UserTokenTyp.VERIFICATION); // Falscher Typ
        userToken.setAblaufdatum(LocalDateTime.now().plusHours(1));
        userToken.setBenutzer(testBenutzer);

        when(userTokenRepository.findByToken(token)).thenReturn(Optional.of(userToken));

        boolean result = benutzerService.resetPasswortMitToken(token, "newPassword");

        assertFalse(result);
        verify(userTokenRepository).findByToken(token);
        verify(benutzerRepository, never()).save(any());
    }
}

