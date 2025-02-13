package rest.felix.back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.CreateTodoRequestDTO;
import rest.felix.back.dto.response.TodoResponseDTO;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.exception.throwable.forbidden.UserAccessDeniedException;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.service.GroupService;
import rest.felix.back.service.TodoService;
import rest.felix.back.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TodoController {

    private final GroupService groupService;
    private final TodoService todoService;
    private final UserService userService;

    @Autowired
    public TodoController(GroupService groupService, TodoService todoService, UserService userService) {
        this.groupService = groupService;
        this.todoService = todoService;
        this.userService = userService;
    }

    @GetMapping("/group/{groupId}/todo")
    public ResponseEntity<List<TodoResponseDTO>> getTodos(
            Principal principal,
            @PathVariable(name = "groupId") long groupId) {
        String username = principal.getName();
        UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
        long userId = userDTO.getId();

        groupService.getUserRoleInGroup(userId, groupId);

        List<TodoResponseDTO> todoResponseDTOs = todoService
                .getTodosInGroup(groupId)
                .stream()
                .map(todoDTO -> new TodoResponseDTO(todoDTO.getId(), todoDTO.getTitle(), todoDTO.getDescription(), todoDTO.getStatus(), todoDTO.getAuthorId(), todoDTO.getGroupId()))
                .toList();


        return ResponseEntity.ok().body(todoResponseDTOs);
    }

    @PostMapping("/group/{groupId}/todo")
    public ResponseEntity<TodoResponseDTO> createTodo(
            Principal principal,
            @PathVariable(name = "groupId") long groupId,
            @RequestBody CreateTodoRequestDTO createTodoRequestDTO) {
        String username = principal.getName();
        UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
        long userId = userDTO.getId();

        GroupRole groupRole = groupService.getUserRoleInGroup(userId, groupId);
        if (groupRole == GroupRole.VIEWER) {
            throw new UserAccessDeniedException();
        }

        CreateTodoDTO createTodoDTO = new CreateTodoDTO(createTodoRequestDTO.getTitle(), createTodoRequestDTO.getDescription(), userId, groupId);

        TodoDTO todoDTO = todoService.createTodo(createTodoDTO);

        TodoResponseDTO todoResponseDTO = new TodoResponseDTO(todoDTO.getId(), todoDTO.getTitle(), todoDTO.getDescription(), todoDTO.getStatus(), todoDTO.getAuthorId(), todoDTO.getGroupId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(todoResponseDTO);
    }
}
