package rest.felix.back.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import rest.felix.back.dto.internal.SignupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.SignupRequestDTO;
import rest.felix.back.entity.User;
import rest.felix.back.exception.throwable.badrequest.ConfirmPasswordMismatchException;
import rest.felix.back.exception.throwable.badrequest.UsernameTakenException;
import rest.felix.back.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO signup(SignupDTO signupDTO) {
        User user = userRepository.createUser(signupDTO);

        return new UserDTO(user.getId(), user.getNickname(), user.getUsername(), user.getHashedPassword());
    }

    public void validateSignupRequestDTO(SignupRequestDTO signupRequestDTO)
            throws ConfirmPasswordMismatchException, UsernameTakenException {

        if (!signupRequestDTO.getPassword().equals(signupRequestDTO.getConfirmPassword())) {
            throw new ConfirmPasswordMismatchException();
        }

        if (userRepository.getByUsername(signupRequestDTO.getUsername()).isPresent()) {
            throw new UsernameTakenException();
        }

    }

}
