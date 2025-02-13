package rest.felix.back.exception.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import rest.felix.back.dto.response.ErrorResponseDTO;
import rest.felix.back.exception.throwable.badrequest.BadRequestException;
import rest.felix.back.exception.throwable.forbidden.UserAccessDeniedException;
import rest.felix.back.exception.throwable.unauthorized.UnauthorizedException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ErrorResponseDTO(exception.getMessage()));

    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ErrorResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UserAccessDeniedException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new ErrorResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("Bad Request, please try again later."));
    }

}
