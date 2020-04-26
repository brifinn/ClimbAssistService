package com.climbassist.api.user.authorization;

import com.climbassist.api.user.UserData;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.AccessTokenExpiredException;
import com.climbassist.api.user.authentication.UserSessionData;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministratorAuthorizationHandlerTest {

    private static final String USERNAME = "captain-america";
    private static final String EMAIL = "cap@shield.com";
    private static final UserData USER_DATA_ADMINISTRATOR = UserData.builder()
            .username(USERNAME)
            .email(EMAIL)
            .isEmailVerified(true)
            .isAdministrator(true)
            .build();
    private static final UserData USER_DATA_NOT_ADMINISTRATOR = UserData.builder()
            .username(USERNAME)
            .email(EMAIL)
            .isEmailVerified(true)
            .isAdministrator(false)
            .build();
    private static final UserSessionData USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("access token")
            .refreshToken("refresh token")
            .build();
    private static final UserSessionData NEW_USER_SESSION_DATA = UserSessionData.builder()
            .accessToken("new access token")
            .refreshToken("refresh token")
            .build();

    @Mock
    private UserManager mockUserManager;

    private AdministratorAuthorizationHandler administratorAuthorizationHandler;

    @BeforeEach
    void setUp() {
        administratorAuthorizationHandler = AdministratorAuthorizationHandler.builder()
                .userManager(mockUserManager)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.setDefault(UserSessionData.class, USER_SESSION_DATA);
        nullPointerTester.testInstanceMethods(administratorAuthorizationHandler, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void checkAuthorization_returnsOriginalSessionData_whenUserIsSignedInAndIsAdministrator()
            throws AuthorizationException {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_ADMINISTRATOR);
        assertThat(administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA),
                is(equalTo(USER_SESSION_DATA)));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_refreshesTokenAndReturnsNewSessionData_whenUserIsSignedInAndAdministratorButAccessTokenIsExpired()
            throws AuthorizationException {
        doThrow(new AccessTokenExpiredException(null)).when(mockUserManager)
                .isSignedIn(USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.refreshAccessToken(any())).thenReturn(NEW_USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_ADMINISTRATOR);
        doReturn(true).when(mockUserManager)
                .isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());

        assertThat(administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA),
                is(equalTo(NEW_USER_SESSION_DATA)));

        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
        verify(mockUserManager).refreshAccessToken(USER_SESSION_DATA.getRefreshToken());
        verify(mockUserManager).isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsSignedInAndIsNotAdministrator() {
        when(mockUserManager.isSignedIn(any())).thenReturn(true);
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_NOT_ADMINISTRATOR);
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_refreshesTokenAndThrowsAuthorizationException_whenUserIsSignedInAndAdministratorButAccessTokenIsExpired()
            throws SessionExpiredException {
        doThrow(new AccessTokenExpiredException(null)).when(mockUserManager)
                .isSignedIn(USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.refreshAccessToken(any())).thenReturn(NEW_USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.getUserData(any())).thenReturn(USER_DATA_NOT_ADMINISTRATOR);
        doReturn(true).when(mockUserManager)
                .isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());

        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));

        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
        verify(mockUserManager).refreshAccessToken(USER_SESSION_DATA.getRefreshToken());
        verify(mockUserManager).isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedIn() {
        when(mockUserManager.isSignedIn(any())).thenReturn(false);
        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));
        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
    }

    @Test
    void checkAuthorization_throwsAuthorizationException_whenUserIsNotSignedInAfterRefreshingToken()
            throws SessionExpiredException {
        doThrow(new AccessTokenExpiredException(null)).when(mockUserManager)
                .isSignedIn(USER_SESSION_DATA.getAccessToken());
        when(mockUserManager.refreshAccessToken(any())).thenReturn(NEW_USER_SESSION_DATA.getAccessToken());
        doReturn(false).when(mockUserManager)
                .isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());

        assertThrows(AuthorizationException.class,
                () -> administratorAuthorizationHandler.checkAuthorization(USER_SESSION_DATA));

        verify(mockUserManager).isSignedIn(USER_SESSION_DATA.getAccessToken());
        verify(mockUserManager).refreshAccessToken(USER_SESSION_DATA.getRefreshToken());
        verify(mockUserManager).isSignedIn(NEW_USER_SESSION_DATA.getAccessToken());
    }
}
