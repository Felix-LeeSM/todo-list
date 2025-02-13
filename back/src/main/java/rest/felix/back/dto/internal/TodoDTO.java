package rest.felix.back.dto.internal;

import lombok.Getter;
import lombok.AllArgsConstructor;
import rest.felix.back.entity.enumerated.TodoStatus;

@Getter
@AllArgsConstructor
public class TodoDTO {
    private final long id;
    private final String title;
    private final String description;
    private final TodoStatus todoStatus;
    private final long authorId;
    private final long groupId;

}
