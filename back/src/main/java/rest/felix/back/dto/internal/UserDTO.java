package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {

    private final Long id;
    private final String nickname;
    private final String username;
    private final String hashedPassword;

}
