package app.web;


import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMetadata;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {


    // Важно: Когато тествам API контролери, трябва да мокна  всички депендънсита на този контролер c @MockitoBean
    @MockitoBean
    private UserService  userService;

    // using MockMvc to the send requests
    @Autowired
    private MockMvc mockMvc;

    //Result -> view name -> index
    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexPage() throws Exception {

        //1.build request
        MockHttpServletRequestBuilder requestBuilder = get("/");

        //.andExpect -> check result
        //MockMvcResultMatchers.status - check status
        //2.Send request
        mockMvc.perform(requestBuilder)
                .andExpect (status().is (200))
                .andExpect (view().name ("index"));


    }

    @Test
    void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

        // 1. Build Request
        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Vik123")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());


        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExist"));

        verify(userService, times(1)).register(any());
    }



    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterPage() throws Exception {

        //1.build request
        MockHttpServletRequestBuilder requestBuilder = get("/register");

        //.andExpect -> check result
        //MockMvcResultMatchers.status - check status
        //2.Send request
        mockMvc.perform(requestBuilder)
                .andExpect (status().is (200))
                .andExpect (view().name ("register"))
                .andExpect (model().attributeExists ("registerRequest"));

    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginPage() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/login");

        mockMvc.perform(requestBuilder)
                .andExpect (status().is (200))
                .andExpect (view().name ("login"))
                .andExpect (model().attributeExists ("loginRequest"));

    }



    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginPageAndErrorMessageAttribute() throws Exception {

        MockHttpServletRequestBuilder requestBuilder =
                get("/login")
                .param("error", "");

        mockMvc.perform(requestBuilder)
                .andExpect (status().is (200))
                .andExpect (view().name ("login"))
                .andExpect (model ().attributeExists ("loginRequest", "errorMessage"));
    }



    @Test
    void postRequestToRegisterEndpoint_shouldReturnRegisterPage_HappyPath() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = post("/register")
                .formField ("username", "Ven123")
                .formField ("password", "123123")
                .formField ("country", "GERMANY")
                .with (csrf ());


        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times (1)).register (any ());
    }




    @Test
    void postRequestToRegisterEndPointWithInvalidData_returnRegisterPage() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = post("/register")
                .formField ("username", "")
                .formField ("password", "")
                .formField ("country", "GERMANY")
                .with (csrf ());

        mockMvc.perform(requestBuilder)
                .andExpect (status ().is (200))
                .andExpect (view ().name ("register"));
        verify (userService, never ()).register (any ());
    }


    @Test
    void getRequestAuthenticateToHome_shouldReturnHomeView() throws Exception {

        when (userService.getById (any ())).thenReturn (aRandomUser ());

        UUID userId = UUID.randomUUID ();
        AuthenticationMetadata principal = new AuthenticationMetadata
                (
                           userId,
                "Ven123",
                "123123",
                     UserRole.USER,
                      true
                );

        MockHttpServletRequestBuilder requestBuilder = get("/home")
                .with (user(principal));

        mockMvc.perform(requestBuilder)
                .andExpect (status().is (200))
                .andExpect (view().name ("home"))
                .andExpect (model().attributeExists ("user"));

        verify (userService, times (1)).getById (userId);
    }




    @Test
    void getRequestUnAuthenticateToHome_redirectToLogin() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/home");

        mockMvc.perform(requestBuilder)
                .andExpect (status().is3xxRedirection());
        verify (userService, never ()).getById (any ());

    }

}
