package app.web;


import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    void getRequestToLogoutEndpointWithErrorParameter_shouldReturnLoginPageAndErrorMessageAttribute() throws Exception {

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
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterPage() throws Exception {

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

}
