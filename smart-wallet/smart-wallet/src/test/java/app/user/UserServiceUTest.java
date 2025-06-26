package app.user;

import app.email.service.NotificationService;
import app.exception.DomainException;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.property.UserProperties;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;


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


}
