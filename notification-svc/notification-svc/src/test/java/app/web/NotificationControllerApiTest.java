package app.web;

import app.service.NotificationService;
import app.web.dto.NotificationTypeRequest;
import app.web.dto.UpsertNotificationPreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.aRandomNotificationPreference;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private  NotificationService notificationService;


    @Autowired
    private MockMvc mockMvc;


    @Test
    void  getRequestNotificationPreference_happyPath() throws Exception {


        when (notificationService.getPreferenceByUserId (any (UUID.class))).thenReturn (aRandomNotificationPreference  ());

        MockHttpServletRequestBuilder response = get("/api/v1/notifications/preferences").param("userId", UUID.randomUUID().toString());;

        mockMvc
                .perform (response)
                .andExpect (status ().is (200))
                .andExpect (jsonPath ("id").isNotEmpty ())
                .andExpect (jsonPath ("userId").isNotEmpty ())
                .andExpect (jsonPath ("type").isNotEmpty ())
                .andExpect (jsonPath ("enabled").isNotEmpty ())
                .andExpect (jsonPath ("contactInfo").isNotEmpty ());
    }


    @Test
    void postRequestWithBodyToCreateNotificationPreference_returns201AndCorrectStructure() throws Exception {

        UpsertNotificationPreference requestDto = UpsertNotificationPreference.builder ()
                .userId (UUID.randomUUID ())
                .type (NotificationTypeRequest.EMAIL)
                .contactInfo ("contactInfo")
                .notificationEnabled (true)
                .build ();

        when (notificationService.upsertPreference (any ())).thenReturn (aRandomNotificationPreference  ());

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/preferences")
                .contentType (MediaType.APPLICATION_JSON) // json
                .content (new ObjectMapper().writeValueAsBytes (requestDto)); // content in this request, build a new json object in the body

        mockMvc
                .perform (request)
                .andExpect (status ().isCreated ()) // 201
                .andExpect (jsonPath ("id").isNotEmpty ())
                .andExpect (jsonPath ("userId").isNotEmpty ())
                .andExpect (jsonPath ("type").isNotEmpty ())
                .andExpect (jsonPath ("enabled").isNotEmpty ())
                .andExpect (jsonPath ("contactInfo").isNotEmpty ());

    }

}
