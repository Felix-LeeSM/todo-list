package rest.felix.back.todo.dto;

import rest.felix.back.todo.entity.enumerated.TodoStatus;

public record TodoResponseDTO(long id, String title, String description, TodoStatus status,
                long authorId, long groupId) {

}
