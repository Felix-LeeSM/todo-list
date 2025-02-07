package rest.felix.back.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.felix.back.dto.internal.SignupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.SignInRequestDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.dto.response.UserResponseDTO;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.security.JwtTokenProvider;
import rest.felix.back.service.PasswordService;
import rest.felix.back.service.UserService;

import java.time.Duration;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService,
                          PasswordService passwordService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {

        userService.validateSignupRequestDTO(signupRequestDTO);
        String hashedPassword = passwordService.hashPassword(signupRequestDTO.getPassword());

        SignupDTO signupDTO = new SignupDTO(
                signupRequestDTO.getUsername(),
                signupRequestDTO.getNickname(),
                hashedPassword);

        UserDTO createdUserDTO = userService.signup(signupDTO);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                createdUserDTO.getId(),
                createdUserDTO.getUsername(),
                createdUserDTO.getNickname());

        return ResponseEntity
                .status(201)
                .body(userResponseDTO);
    }

    @PostMapping("/token/access-token")
    public ResponseEntity<UserResponseDTO> createAccessToken(@RequestBody @Valid SignInRequestDTO signInRequestDTO) {

        String givenUsername = signInRequestDTO.getUsername();
        String givenPassword = signInRequestDTO.getPassword();

        UserDTO userDTO = userService
                .getByUsername(givenUsername)
                .filter(DTO -> passwordService.verifyPassword(givenPassword, DTO.getHashedPassword()))
                .orElseThrow(NoMatchingUserException::new);

        String token = jwtTokenProvider.generateToken(userDTO.getUsername());

        ResponseCookie authCookie = ResponseCookie
                .from("accessToken", token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.ofHours(24))
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .status(201)
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .body(new UserResponseDTO(userDTO.getId(), userDTO.getUsername(), userDTO.getNickname()));
    }

    @DeleteMapping("/token")
    public ResponseEntity logOutUser() {
        ResponseCookie emptyCookie = ResponseCookie
                .from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity
                .status(204)
                .header(HttpHeaders.SET_COOKIE, emptyCookie.toString())
                .build();


    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUserInfo(@CookieValue(name="accessToken", required = false) String accessToken) {

        return ResponseEntity.ok().body(Optional.ofNullable(accessToken)
                .map(jwtTokenProvider::getUsernameFromToken)
                .flatMap(userService::getByUsername)
                .map(userDTO -> new UserResponseDTO(userDTO.getId(), userDTO.getUsername(), userDTO.getNickname()))
                .orElseThrow(NoMatchingUserException::new));
    }


}
