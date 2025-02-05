package rest.felix.back.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @Test
    void verifyPassword_HappyPath() {

        // Given

        String rawPassword = "password";
        String hashedPassword = passwordService.hashPassword("password");

        // When

        Assertions.assertTrue(passwordService.verifyPassword(rawPassword, hashedPassword));
        Assertions.assertFalse(passwordService.verifyPassword("wrongPassword", hashedPassword));

    }
}