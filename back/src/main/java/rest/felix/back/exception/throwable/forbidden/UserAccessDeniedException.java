package rest.felix.back.exception.throwable.forbidden;

public class UserAccessDeniedException extends ForbiddenException {

  public UserAccessDeniedException() {
    super("No permission to perform this action.");
  }
}
