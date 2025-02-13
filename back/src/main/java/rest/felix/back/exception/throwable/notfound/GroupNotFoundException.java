package rest.felix.back.exception.throwable.notfound;

public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException() {
        super("Group Not Found.");
    }
}
