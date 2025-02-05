package rest.felix.back.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.dto.response.SignupResponseDTO;
import rest.felix.back.entity.User;
import rest.felix.back.exception.throwable.badrequest.ConfirmPasswordMismatchException;
import rest.felix.back.exception.throwable.badrequest.UsernameTakenException;
import rest.felix.back.repository.UserRepository;

import java.util.Optional;

@SpringBootTest
@Transactional
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

        ResponseEntity<SignupResponseDTO> response = userController.signUp(signupRequestDTO);

        // Then

        SignupResponseDTO signupResponseDTO = response.getBody();

        Assertions.assertNotNull(signupResponseDTO.getId());
        Assertions.assertEquals("username", signupResponseDTO.getUsername());
        Assertions.assertEquals("nickname", signupResponseDTO.getNickname());

        User user = userRepository.getById(signupResponseDTO.getId()).get();
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


}