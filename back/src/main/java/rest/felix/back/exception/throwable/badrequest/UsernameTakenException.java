package rest.felix.back.exception.throwable.badrequest;

import lombok.Getter;

@Getter
public class UsernameTakenException extends BadRequestException {
    public UsernameTakenException() {
        super("해당 username은 이미 사용 중입니다.");
    }
}
