package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String nickname;
    private String username;
    private String hashedPassword;

}
