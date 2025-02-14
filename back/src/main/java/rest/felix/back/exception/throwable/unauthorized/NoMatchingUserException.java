package rest.felix.back.exception.throwable.unauthorized;


public class NoMatchingUserException extends UnauthorizedException {

  public NoMatchingUserException() {
    super("There is no user with given conditions.");
  }
}
