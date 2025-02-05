package rest.felix.back.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import rest.felix.back.dto.internal.SignupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.dto.response.SignupResponseDTO;
import rest.felix.back.service.PasswordService;
import rest.felix.back.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final PasswordService passwordService;

    public UserController(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @PostMapping
    public ResponseEntity<SignupResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {

        userService.validateSignupRequestDTO(signupRequestDTO);
        String hashedPassword = passwordService.hashPassword(signupRequestDTO.getPassword());

        SignupDTO signupDTO = new SignupDTO(
                signupRequestDTO.getUsername(),
                signupRequestDTO.getNickname(),
                hashedPassword);

        UserDTO createdUserDTO = userService.signup(signupDTO);

        SignupResponseDTO signupResponseDTO = new SignupResponseDTO(
                createdUserDTO.getId(),
                createdUserDTO.getUsername(),
                createdUserDTO.getNickname());

        return ResponseEntity.status(201).body(signupResponseDTO);
    }

}
