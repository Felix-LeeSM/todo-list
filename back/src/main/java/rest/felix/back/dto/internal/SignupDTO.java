package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupDTO {

    private final String username;
    private final String nickname;
    private final String hashedPassword;

}
