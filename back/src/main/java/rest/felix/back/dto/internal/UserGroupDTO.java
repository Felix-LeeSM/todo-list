package rest.felix.back.dto.internal;


import lombok.AllArgsConstructor;
import lombok.Getter;
import rest.felix.back.entity.enumerated.GroupRole;

@Getter
@AllArgsConstructor
public class UserGroupDTO {
    private final GroupRole groupRole;
    private final long userId;
    private final long groupId;
}
