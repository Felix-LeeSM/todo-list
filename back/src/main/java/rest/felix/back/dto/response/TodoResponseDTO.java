package rest.felix.back.dto.response;

import rest.felix.back.entity.enumerated.TodoStatus;

public record TodoResponseDTO(long id, String title, String description, TodoStatus todoStatus, long authorId, long groupId) {}
