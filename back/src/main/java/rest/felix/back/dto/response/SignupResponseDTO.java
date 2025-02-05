package rest.felix.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDTO {
    private final Long id;
    private final String username;
    private final String nickname;

}
