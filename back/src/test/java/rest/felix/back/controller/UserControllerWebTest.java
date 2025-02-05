package rest.felix.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.repository.UserRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class UserControllerWebTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser
    void signUp_HappyPath() throws Exception {

        // Given

        String path = "/api/v1/user";

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "LongEnoughUsername",
                "nickname",
                "LongEnoughPassword",
                "LongEnoughPassword");

        String requestBody = objectMapper.writeValueAsString(signupRequestDTO);


        // When

        ResultActions result = mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );


        // Then

        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").isNotEmpty());
        result.andExpect(jsonPath("$.username").value("LongEnoughUsername"));
        result.andExpect(jsonPath("$.nickname").value("nickname"));


    }

    @Test
    @WithMockUser
    void signUp_Failure_UsernameTooShort() throws Exception {

        // Given

        String path = "/api/v1/user";

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "username",
                "nickname",
                "LongEnoughPassword",
                "LongEnoughPassword");

        String requestBody = objectMapper.writeValueAsString(signupRequestDTO);


        // When

        ResultActions result = mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );


        // Then

        result.andExpect(status().isBadRequest());


    }

    @Test
    @WithMockUser
    void signUp_Failure_PasswordTooShort() throws Exception {

        // Given

        String path = "/api/v1/user";

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "LongEnoughUsername",
                "nickname",
                "password",
                "password");

        String requestBody = objectMapper.writeValueAsString(signupRequestDTO);


        // When

        ResultActions result = mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );


        // Then

        result.andExpect(status().isBadRequest());


    }

    @Test
    @WithMockUser
    void signUp_Failure_PasswordMisMatch() throws Exception {

        // Given

        String path = "/api/v1/user";

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "LongEnoughUsername",
                "nickname",
                "LongEnoughPassword1",
                "LongEnoughPassword2");

        String requestBody = objectMapper.writeValueAsString(signupRequestDTO);


        // When

        ResultActions result = mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );


        // Then

        result.andExpect(status().isBadRequest());
        result.andExpect(jsonPath("$.message").value("password and confirm Password do not match."));





    }

    @Test
    @WithMockUser
    void signUp_Failure_UsernameTaken() throws Exception {

        // Given

        String path = "/api/v1/user";

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "LongEnoughUsername",
                "nickname",
                "LongEnoughPassword",
                "LongEnoughPassword");

        String requestBody = objectMapper.writeValueAsString(signupRequestDTO);


        // When

        mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );

        ResultActions result = mvc.perform(
                post(path)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)

        );


        // Then



        result.andExpect(status().isBadRequest());
        result.andExpect(jsonPath("$.message").value("해당 username은 이미 사용 중입니다."));


    }


}
