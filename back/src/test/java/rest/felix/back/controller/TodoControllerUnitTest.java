package rest.felix.back.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rest.felix.back.dto.request.CreateTodoRequestDTO;
import rest.felix.back.dto.response.TodoResponseDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.entity.UserGroup;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.entity.enumerated.TodoStatus;
import rest.felix.back.exception.throwable.forbidden.UserAccessDeniedException;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.utility.Pair;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
public class TodoControllerUnitTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private TodoController todoController;

    @Test
    void getTodos_HappyPath() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);


        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        List<Pair<TodoStatus, Integer>> list = Arrays.asList(
                new Pair<>(TodoStatus.PENDING, 1),
                new Pair<>(TodoStatus.ACTIVE, 2),
                new Pair<>(TodoStatus.IMMINENT, 3),
                new Pair<>(TodoStatus.DONE, 4)
        );

        list.forEach(pair -> {
            TodoStatus todoStatus = pair.first();
            int idx = pair.second();

            Todo todo = new Todo();
            todo.setTitle(String.format("todo %d", idx));
            todo.setDescription(String.format("todo %d description", idx));
            todo.setTodoStatus(todoStatus);
            todo.setAuthor(user);
            todo.setGroup(group);
            em.persist(todo);
        });

        em.flush();

        Principal principal = user::getUsername;

        // When

        ResponseEntity<List<TodoResponseDTO>> responseEntity = todoController.getTodos(principal, group.getId());

        // Then

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<TodoResponseDTO> todoResponseDTOs = responseEntity.getBody();
        Assertions.assertEquals(4, todoResponseDTOs.size());
        Assertions.assertTrue(
                todoResponseDTOs
                        .stream()
                        .map(TodoResponseDTO::authorId)
                        .map(authorId -> authorId.equals(user.getId()))
                        .reduce(true, (one, another) -> one && another)
        );

        Assertions.assertTrue(
                todoResponseDTOs
                        .stream()
                        .map(TodoResponseDTO::groupId)
                        .map(groupId -> groupId.equals(group.getId()))
                        .reduce(true, (one, another) -> one && another)
        );

        Assertions.assertTrue(
                todoResponseDTOs
                .stream()
                .map(TodoResponseDTO::title)
                .toList()
                .containsAll(
                        List.of("todo 1", "todo 2", "todo 3", "todo 4")
                )
        );

        Assertions.assertTrue(
                todoResponseDTOs
                        .stream()
                        .map(TodoResponseDTO::description)
                        .toList()
                        .containsAll(
                                List.of("todo 1 description", "todo 2 description", "todo 3 description", "todo 4 description")
                        )
        );

        Assertions.assertTrue(
                todoResponseDTOs
                        .stream()
                        .map(TodoResponseDTO::todoStatus)
                        .toList()
                        .containsAll(
                                List.of(TodoStatus.PENDING, TodoStatus.ACTIVE, TodoStatus.IMMINENT, TodoStatus.DONE)
                        )
        );

    }

    @Test
    void getTodos_HappyPath_NoTodo() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);


        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        Principal principal = user::getUsername;

        // When

        ResponseEntity<List<TodoResponseDTO>> responseEntity = todoController.getTodos(principal, group.getId());

        // Then

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<TodoResponseDTO> todoResponseDTOs = responseEntity.getBody();
        Assertions.assertEquals(0, todoResponseDTOs.size());

    }

    @Test
    void getTodos_Failure_NoUser() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);


        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        em.flush();

        em.remove(user);
        
        em.flush();

        Principal principal = user::getUsername;

        // When

        Runnable lambda = () -> todoController.getTodos(principal, group.getId());

        // Then

        Assertions.assertThrows(NoMatchingUserException.class, lambda::run);

    }

    @Test
    void getTodos_Failure_Group() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);


        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        em.remove(userGroup);
        em.remove(group);
        em.flush();

        Principal principal = user::getUsername;

        // When

        Runnable lambda = () -> todoController.getTodos(principal, group.getId());

        // Then

        Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);

    }

    @Test
    void getTodos_Failure_NoUserGroup() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        em.remove(userGroup);
        em.flush();

        Principal principal = user::getUsername;

        // When

        Runnable lambda = () -> todoController.getTodos(principal, group.getId());

        // Then

        Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);

    }


    @Test
    void createTodo_HappyPath() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        Principal principal = user::getUsername;

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        // When

        ResponseEntity<TodoResponseDTO> responseEntity = todoController.createTodo(principal, group.getId(), createTodoRequestDTO);

        // Then

        Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        TodoResponseDTO todoResponseDTO = responseEntity.getBody();

        Assertions.assertEquals("todo title", todoResponseDTO.title());
        Assertions.assertEquals("todo description", todoResponseDTO.description());
        Assertions.assertEquals(TodoStatus.PENDING, todoResponseDTO.todoStatus());
        Assertions.assertEquals(user.getId(), todoResponseDTO.authorId());
        Assertions.assertEquals(group.getId(), todoResponseDTO.groupId());
    }

    @Test
    void createTodo_Failure_NoAuthority() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.VIEWER);

        em.persist(userGroup);

        em.flush();

        Principal principal = user::getUsername;

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        // When

        Runnable lambda = () -> todoController.createTodo(principal, group.getId(), createTodoRequestDTO);

        // Then


        Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
    }

    @Test
    void createTodo_Failure_NoUser() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        em.remove(userGroup);
        em.remove(user);
        em.flush();

        Principal principal = user::getUsername;

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        // When

        Runnable lambda = () -> todoController.createTodo(principal, group.getId(), createTodoRequestDTO);

        // Then


        Assertions.assertThrows(NoMatchingUserException.class, lambda::run);
    }

    @Test
    void createTodo_Failure_NoGroup() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        em.remove(userGroup);
        em.remove(group);
        em.flush();

        Principal principal = user::getUsername;

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        // When

        Runnable lambda = () -> todoController.createTodo(principal, group.getId(), createTodoRequestDTO);

        // Then


        Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
    }

    @Test
    void createTodo_Failure_NoUserGroup() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        Group group = new Group();
        group.setName("group");
        group.setDescription("description");

        em.persist(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setGroupRole(GroupRole.OWNER);

        em.persist(userGroup);

        em.flush();

        em.remove(userGroup);
        em.flush();

        Principal principal = user::getUsername;

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        // When

        Runnable lambda = () -> todoController.createTodo(principal, group.getId(), createTodoRequestDTO);

        // Then


        Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
    }

}
