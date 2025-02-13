package rest.felix.back.exception.throwable.forbidden;

import lombok.Getter;
import rest.felix.back.exception.throwable.RequestExceptionInterface;

@Getter
public class ForbiddenException extends RuntimeException implements RequestExceptionInterface {

    private final int statusCode = 403;
    private String message = "Forbidden Request.";

    public ForbiddenException(String message) {
        this.message = message;
    }

}
