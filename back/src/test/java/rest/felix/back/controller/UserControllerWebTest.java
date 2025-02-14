package rest.felix.back.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rest.felix.back.dto.request.SignInRequestDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.repository.UserRepository;
import rest.felix.back.security.JwtTokenProvider;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerWebTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;


  @Test
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
  void signUp_Failure_InvalidArguments() throws Exception {

    // Given

    String path = "/api/v1/user";

    List<String> requestBodies = new ArrayList<>();
    ObjectMapper objectMapper1 = objectMapper;
    for (String[] row : new String[][]{
        {null, "nickname", "LongEnoughPassword", "LongEnoughPassword"},
        {"LongEnoughUsername", null, "LongEnoughPassword", "LongEnoughPassword"},
        {"LongEnoughUsername", "nickname", null, "LongEnoughPassword"},
        {"LongEnoughUsername", "nickname", "LongEnoughPassword", null},
    }) {
      SignupRequestDTO signupRequestDTO = new SignupRequestDTO(row[0], row[1], row[2], row[3]);
      String s = objectMapper1.writeValueAsString(signupRequestDTO);
      requestBodies.add(s);
    }

    // When

    for (String requestBody : requestBodies) {
      ResultActions result = mvc.perform(
          post(path)
              .content(requestBody)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
      );

      // Then

      result.andExpect(status().isBadRequest());
    }


  }


  @Test
  void signUp_Failure_UsernameTooShort() throws Exception {

    // Given

    String path = "/api/v1/user";

    SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
        "user",
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

  @Test
  void createAccessToken_HappyPath() throws Exception {
    // Given

    SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username123", "nickname",
        "password123412341234", "password123412341234");

    mvc.perform(
        post("/api/v1/user")
            .content(objectMapper.writeValueAsString(signupRequestDTO))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    String path = "/api/v1/user/token/access-token";

    SignInRequestDTO signInRequestDTO = new SignInRequestDTO("username123", "password123412341234");
    String requestBody = objectMapper.writeValueAsString(signInRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    // Then

    result.andExpect(status().isCreated());
    result.andExpect(cookie().exists("accessToken"));

    MockHttpServletResponse response = result.andReturn().getResponse();
    Cookie accessTokenCookie = response.getCookie("accessToken");

    Assertions.assertNotNull(accessTokenCookie);
    String token = accessTokenCookie.getValue();
    Assertions.assertDoesNotThrow(() -> {
      jwtTokenProvider.validateToken(token);
    });
    Assertions.assertEquals("username123", jwtTokenProvider.getUsernameFromToken(token));

  }

  @Test
  void createAccessToken_Failure_NoUserWithUsername() throws Exception {
    // Given

    String path = "/api/v1/user/token/access-token";

    SignInRequestDTO signInRequestDTO = new SignInRequestDTO("username123", "password123412341234");
    String requestBody = objectMapper.writeValueAsString(signInRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message").value("There is no user with given conditions."));
  }

  @Test
  void createAccessToken_Failure_WrongPassword() throws Exception {
    // Given

    SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username123", "nickname",
        "password123412341234", "password123412341234");

    mvc.perform(
        post("/api/v1/user")
            .content(objectMapper.writeValueAsString(signupRequestDTO))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    String path = "/api/v1/user/token/access-token";

    SignInRequestDTO signInRequestDTO = new SignInRequestDTO("username123", "wrongPassword");
    String requestBody = objectMapper.writeValueAsString(signInRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message").value("There is no user with given conditions."));
  }

  @Test
  void logOutUser_HappyPath() throws Exception {
    // Given

    SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username123", "nickname",
        "password123412341234", "password123412341234");

    mvc.perform(
        post("/api/v1/user")
            .content(objectMapper.writeValueAsString(signupRequestDTO))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    Cookie cookie = new Cookie("accessToken", jwtTokenProvider.generateToken("username123"));
    String path = "/api/v1/user/token";

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
    );

    // Then

    result.andExpect(cookie().exists("accessToken"));
    result.andExpect(mvcResult -> {
          Cookie accessTokenCookie = mvcResult.getResponse().getCookie("accessToken");
          Assertions.assertNotNull(accessTokenCookie);
          Assertions.assertEquals(0, accessTokenCookie.getMaxAge());
        }
    );
  }

  @Test
  void logOutUser_Failure_NotSignedIn() throws Exception {
    // Given

    String path = "/api/v1/user/token";

    // When

    ResultActions result = mvc.perform(
        delete(path)
    );

    // Then

    result.andExpect(status().isForbidden());

  }

  @Test
  void currentUserInfo_HappyPath() throws Exception {
    // Given

    SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username123", "nickname",
        "password123412341234", "password123412341234");

    mvc.perform(
        post("/api/v1/user")
            .content(objectMapper.writeValueAsString(signupRequestDTO))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    Cookie cookie = new Cookie("accessToken", jwtTokenProvider.generateToken("username123"));

    String path = "/api/v1/user/me";

    // When

    ResultActions result = mvc.perform(
        get(path)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .cookie(cookie)
    );

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id").isNotEmpty());
    result.andExpect(jsonPath("$.username").value("username123"));
    result.andExpect(jsonPath("$.nickname").value("nickname"));

  }

  @Test
  void currentUserInfo_Failure_NotSingedIn() throws Exception {
    // Given

    String path = "/api/v1/user/me";

    // When

    ResultActions result = mvc.perform(
        get(path)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
    );

    // Then

    result.andExpect(status().isUnauthorized());

  }

}
