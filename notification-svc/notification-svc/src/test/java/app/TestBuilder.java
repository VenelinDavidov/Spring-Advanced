package app;

import app.model.NotificationPreference;
import app.model.NotificationType;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {



       public static NotificationPreference aRandomNotificationPreference () {
               return NotificationPreference.builder ()
                       .id (UUID.randomUUID ())
                       .userId (UUID.randomUUID ())
                       .enabled (true)
                       .contactInfo ("contactInfo")
                       .type (NotificationType.EMAIL)
                       .createdOn (LocalDate.from (LocalDateTime.now ()))
                       .updatedOn (LocalDate.from (LocalDateTime.now ()))
                       .build ();
       }
}
