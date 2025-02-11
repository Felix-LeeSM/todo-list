package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGroupDTO {
    private final long userId;
    private final String groupName;
}
