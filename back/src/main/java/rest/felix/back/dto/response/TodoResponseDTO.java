package rest.felix.back.dto.response;

import rest.felix.back.entity.enumerated.TodoStatus;

public record TodoResponseDTO(long id, String title, String description, TodoStatus status, long authorId, long groupId) {}
