package app.user;

import app.email.service.NotificationService;
import app.exception.DomainException;
import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.Country;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1. Create the test class
// 2. Annotate the class with @ExtendWith(MockitoExtension.class)
// 3. Get the class you want to test
// 4. Get all dependencies of that class and annotate them with @Mock
// 5. Inject all those dependencies to the class we test with annotation @InjectMocks

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private  UserRepository userRepository;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  SubscriptionService subscriptionService;

    @Mock
    private  WalletService walletService;

    @Mock
    private  NotificationService notificationService;



    @InjectMocks
    private UserService userService;



    @Test
    void  givenMissingUserFromDatabase_whenGetEditUserDetails_thenThrowException() {

        //Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder().build();
        when(userRepository.findById (any())).thenReturn (Optional.empty());

        //Then & When
        assertThrows(DomainException.class, () -> userService.editUserDetails(userId, dto));

    }


    @Test
    void  givenExistingUser_whenEditTheirProfileWithActualEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        //Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder ()
                .firstName ("Moc")
                .lastName ("Doe")
                .email ("jdoe@me.com")
                .profilePicture ("www.image.com")
                .build();

        User user = User.builder ().build ();
        when (userRepository.findById (userId)).thenReturn (Optional.of (user));

        //When
        userService.editUserDetails (userId, dto);

        //Then
        assertEquals("Moc", user.getFirstName ());
        assertEquals("Doe", user.getLastName ());
        assertEquals("jdoe@me.com", user.getEmail ());
        assertEquals("www.image.com", user.getProfilePicture ());
        verify (notificationService, times (1)).saveNotificationPreference (userId, true, dto.getEmail ());
        verify (userRepository, times (1)).save (user);
    }



    @Test
    void  givenExistingUser_whenEditTheirProfileWithEmptyEmail_thenChangeTheirDetailsSaveNotificationPreferenceAndSaveToDatabase() {

        //Given
        UUID userId = UUID.randomUUID();
        UserEditRequest dto = UserEditRequest.builder ()
                .firstName ("Moc")
                .lastName ("Doe")
                .email ("")
                .profilePicture ("www.image.com")
                .build();

        User user = User.builder ().build ();
        when (userRepository.findById (userId)).thenReturn (Optional.of (user));

        //When
        userService.editUserDetails (userId, dto);

        //Then
        assertEquals("Moc", user.getFirstName ());
        assertEquals("Doe", user.getLastName ());
        assertEquals("", user.getEmail ());
        assertEquals("www.image.com", user.getProfilePicture ());
        verify (notificationService, times (1)).saveNotificationPreference (userId, false, null);
        verify (userRepository, times (1)).save (user);
    }


    @Test
    void givenMissingUserFromDatabase_whenLoadUserByUsername_thenThrowException() {

        //Given
        String username = "Venko123";
        when(userRepository.findByUsername (username)).thenReturn (Optional.empty ());

        //When and Then

        assertThrows(DomainException.class, () -> userService.loadUserByUsername (username));
    }


    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnAuthenticationMetadata() {

        //Given
        String username = "Venko123";
        User user = User.builder ()
                .id (UUID.randomUUID ())
                .isActive (true)
                .password ("123123")
                .role (UserRole.ADMIN)
                .build ();
        when(userRepository.findByUsername (username)).thenReturn (Optional.of (user));

        //When
        UserDetails  authenticationMetadata = userService.loadUserByUsername(username);

        //Then
        assertInstanceOf(AuthenticationMetadata.class, authenticationMetadata);
        AuthenticationMetadata result = (AuthenticationMetadata) authenticationMetadata;

        assertEquals(user.getId (), result.getUserId ());
        assertEquals(user.getUsername (), result.getUsername ());
        assertEquals(user.getPassword (), result.getPassword ());
        assertEquals(user.getRole (), result.getRole ());
        assertEquals(user.isActive (), result.isActive ());
        assertThat(result.getAuthorities ()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities ().iterator ().next ().getAuthority ());

    }



    @Test
    void givenExistingUsername_whenRegister_thenExceptionIsThrown() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder ()
                .username ("Venko123")
                .password ("123123")
                .country (Country.BULGARIA)
                .build ();

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        //When and Then

        assertThrows(UsernameAlreadyExistException.class, () -> userService.register (registerRequest));

        verify(userRepository, never()).save(any());
        verify(subscriptionService, never()).createDefaultSubscription(any());
        verify(walletService, never()).initializeFirstWallet(any());
//        verify(notificationService, never()).saveNotificationPreference(any(), any(), any());
    }



    @Test
    void givenUsernameHappyPath_whenRegister_thenUserIsCreated() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder ()
                .username ("Venko123")
                .password ("123123")
                .country (Country.BULGARIA)
                .build ();
        User user = User.builder ()
                .id (UUID.randomUUID ())
                .build ();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(subscriptionService.createDefaultSubscription(user)).thenReturn(new Subscription());
        when(walletService.initializeFirstWallet (user)).thenReturn(new Wallet());

        //When
        User registerUser = userService.register(registerRequest);

        //Then
        assertThat(registerUser.getSubscriptions()).hasSize(1);
        assertThat(registerUser.getWallets()).hasSize(1);
//        verify(notificationService, times(1)).saveNotificationPreference(user.getId(),false, null);
    }


    @Test
    void givenUserWithStatusActive_whenSwitchStatus_thenUserStatusBecomesInactive() {

        //Given
        User user = User.builder ()
                .id (UUID.randomUUID ())
                .isActive (true)
                .build ();

        when(userRepository.findById (user.getId ())).thenReturn(Optional.of (user));

        //When
        userService.switchStatus (user.getId ());

        //Then
        assertFalse( user.isActive ());
        verify(userRepository, times (1)).save (user);
    }


    @Test
    void givenUserWithStatusInActive_whenSwitchStatus_thenUserStatusBecomesActive() {

        //Given
        User user = User.builder ()
                .id (UUID.randomUUID ())
                .isActive (false)
                .build ();

        when(userRepository.findById (user.getId ())).thenReturn(Optional.of (user));

        //When
        userService.switchStatus (user.getId ());

        //Then
        assertTrue( user.isActive ());
        verify(userRepository, times (1)).save (user);
    }


    @Test
    void givenExistingUserInDatabase_whenGetAllUsers_thenReturnListOfUsers() {

        //Given
        List<User> userList = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(userList);

        //When
        List<User> allUsers = userService.getAllUsers();

        //Then
        assertThat(allUsers).hasSize(2);
    }



    // if user is ADMIN -> expected role is USER
    // if user is USER -> expected role is ADMIN
    @ParameterizedTest
    @MethodSource("userRolesArguments")
    void givenChangeRole_whenSwitchRole_thenRoleIsChanged(UserRole currentRole, UserRole expectedRole) {

        //Given
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .role(currentRole) //if role is ADMIN -> expected role is USER
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.switchRole(userId);

        //Then
        assertEquals(expectedRole, user.getRole());
    }


    private static Stream<Arguments> userRolesArguments() {
        return Stream.of(
                Arguments.of(UserRole.ADMIN, UserRole.USER),
                Arguments.of(UserRole.USER, UserRole.ADMIN)
        );
    }

}
