package rest.felix.back.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import rest.felix.back.dto.request.SignInRequestDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.dto.response.UserResponseDTO;
import rest.felix.back.entity.User;
import rest.felix.back.exception.throwable.badrequest.ConfirmPasswordMismatchException;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.exception.throwable.badrequest.UsernameTakenException;
import rest.felix.back.repository.UserRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserControllerUnitTest {

    @Autowired
    private UserController userController;
    @Autowired
    private UserRepository userRepository;

    @Test
    void signUp_HappyPath() {
        // Given

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "username",
                "nickname",
                "password",
                "password");

        // When

        ResponseEntity<UserResponseDTO> response = userController.signUp(signupRequestDTO);

        // Then

        UserResponseDTO signupResponseDTO = response.getBody();

        Assertions.assertNotNull(signupResponseDTO.id());
        Assertions.assertEquals("username", signupResponseDTO.username());
        Assertions.assertEquals("nickname", signupResponseDTO.nickname());

        User user = userRepository.getById(signupResponseDTO.id()).get();
        Assertions.assertEquals("username", user.getUsername());
        Assertions.assertEquals("nickname", user.getNickname());


    }

    @Test
    void signUp_Failure_UsernameDuplicated() {

        // Given

        User existingUser = new User();
        existingUser.setHashedPassword("hashed_password");
        existingUser.setNickname("nickname1");
        existingUser.setUsername("duplicateUsername");


        userRepository.save(existingUser);

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "duplicateUsername",
                "nickname2",
                "password",
                "password");


        // When

        Assertions.assertThrows(UsernameTakenException.class, () -> {
            userController.signUp(signupRequestDTO);
        });


    }

    @Test
    void signUp_Failure_ConfirmPasswordMismatch() {
        // Given

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "duplicateUsername",
                "nickname2",
                "password",
                "mismatchPassword");

        // When

        Assertions.assertThrows(ConfirmPasswordMismatchException.class, () -> {
            userController.signUp(signupRequestDTO);
        });
    }

    @Test
    void createAccessToken_HappyPath() {
        // Given

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "username",
                "nickname",
                "password",
                "password");

        UserResponseDTO user = userController.signUp(signupRequestDTO).getBody();

        SignInRequestDTO signInRequestDTO = new SignInRequestDTO("username", "password");

        // When

        ResponseEntity<UserResponseDTO> response = userController.createAccessToken(signInRequestDTO);

        // Then

        UserResponseDTO userResponseDTO = response.getBody();

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals(user.id(), userResponseDTO.id());
        Assertions.assertEquals(user.username(), userResponseDTO.username());
        Assertions.assertEquals(user.nickname(), userResponseDTO.nickname());

    }

    @Test
    void createAccessToken_Failure_NoSuchUsername() {
        // Given

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "username",
                "nickname",
                "password",
                "password");

        UserResponseDTO user = userController.signUp(signupRequestDTO).getBody();

        // When

        SignInRequestDTO signInRequestDTO = new SignInRequestDTO("wrong_username", "password");

        // Then

        Assertions.assertThrows(NoMatchingUserException.class, () -> {
            userController.createAccessToken(signInRequestDTO);
        });

    }

    @Test
    void createAccessToken_Failure_WrongPassword() {
        // Given

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "username",
                "nickname",
                "password",
                "password");

        UserResponseDTO user = userController.signUp(signupRequestDTO).getBody();

        // When

        SignInRequestDTO signInRequestDTO = new SignInRequestDTO("username", "wrong_password");

        // Then

        Assertions.assertThrows(NoMatchingUserException.class, () -> {
            userController.createAccessToken(signInRequestDTO);
        });

    }
}