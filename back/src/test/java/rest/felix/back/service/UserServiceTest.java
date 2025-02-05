package rest.felix.back.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import rest.felix.back.dto.internal.SignupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.entity.User;
import rest.felix.back.exception.throwable.badrequest.ConfirmPasswordMismatchException;
import rest.felix.back.exception.throwable.badrequest.UsernameTakenException;
import rest.felix.back.repository.UserRepository;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;

    @Test
    void signup_HappyPath() {
        // Given

        SignupDTO signupDTO = new SignupDTO("username", "nickname", "hashedPassword");

        // When

        UserDTO createdUserDTO = userService.signup(signupDTO);
        User createdUser = userRepository.getByUsername("username").get();

        // Then

        Assertions.assertEquals("hashedPassword", createdUserDTO.getHashedPassword());
        Assertions.assertEquals("nickname", createdUserDTO.getNickname());
        Assertions.assertEquals("username", createdUserDTO.getUsername());
        Assertions.assertEquals(createdUser.getId(), createdUserDTO.getId());
    }

    @Test
    void signup_Failure_UsernameTaken() {
        // Given

        SignupDTO signupDTO = new SignupDTO("username", "nickname", "hashedPassword");
        SignupDTO duplicatedUsernameSignupDTO = new SignupDTO("username", "nickname2", "hashedPassword2");

        // When

        userService.signup(signupDTO);
        em.flush();

        // Then

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            userService.signup(duplicatedUsernameSignupDTO);
            em.flush();
        });

    }

    @Test
    void validateSignupRequestDTO_HappyPath() {
        // Given

        // When

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username", "nickname", "password", "password");

        // Then

        Assertions.assertDoesNotThrow(() -> {
            userService.validateSignupRequestDTO(signupRequestDTO);
        });


    }

    @Test
    void validateSignupRequestDTO_UsernameTaken() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setHashedPassword("hashedPassword");
        user.setNickname("nickname");
        em.persist(user);

        // When

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username", "nickname", "password", "password");

        // Then

        Assertions.assertThrows(UsernameTakenException.class, () -> {
            userService.validateSignupRequestDTO(signupRequestDTO);
        });


    }

    @Test
    void validateSignupRequestDTO_PasswordMismatch() {
        // Given


        // When

        SignupRequestDTO signupRequestDTO = new SignupRequestDTO("username", "nickname", "password", "passwordMismatch");

        // Then

        Assertions.assertThrows(ConfirmPasswordMismatchException.class, () -> {
            userService.validateSignupRequestDTO(signupRequestDTO);
        });


    }
}