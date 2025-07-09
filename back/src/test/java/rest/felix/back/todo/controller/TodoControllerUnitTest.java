package rest.felix.back.todo.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import rest.felix.back.todo.dto.TodoDTO;
import rest.felix.back.todo.dto.CreateTodoRequestDTO;
import rest.felix.back.todo.dto.UpdateTodoRequestDTO;
import rest.felix.back.todo.dto.TodoResponseDTO;
import rest.felix.back.group.entity.Group;
import rest.felix.back.todo.entity.Todo;
import rest.felix.back.user.entity.User;
import rest.felix.back.group.entity.UserGroup;
import rest.felix.back.group.entity.enumerated.GroupRole;
import rest.felix.back.todo.entity.enumerated.TodoStatus;
import rest.felix.back.common.exception.throwable.forbidden.UserAccessDeniedException;
import rest.felix.back.common.exception.throwable.notfound.ResourceNotFoundException;
import rest.felix.back.common.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.common.util.Pair;

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
        new Pair<>(TodoStatus.TO_DO, 1),
        new Pair<>(TodoStatus.IN_PROGRESS, 2),
        new Pair<>(TodoStatus.DONE, 3),
        new Pair<>(TodoStatus.ON_HOLD, 4));

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

    ResponseEntity<List<TodoResponseDTO>> responseEntity = todoController.getTodos(principal,
        group.getId());

    // Then

    Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    List<TodoResponseDTO> todoResponseDTOs = responseEntity.getBody();
    Assertions.assertEquals(4, todoResponseDTOs.size());
    Assertions.assertTrue(
        todoResponseDTOs
            .stream()
            .map(TodoResponseDTO::authorId)
            .map(authorId -> authorId.equals(user.getId()))
            .reduce(true, (one, another) -> one && another));

    Assertions.assertTrue(
        todoResponseDTOs
            .stream()
            .map(TodoResponseDTO::groupId)
            .map(groupId -> groupId.equals(group.getId()))
            .reduce(true, (one, another) -> one && another));

    Assertions.assertTrue(
        todoResponseDTOs
            .stream()
            .map(TodoResponseDTO::title)
            .toList()
            .containsAll(
                List.of("todo 1", "todo 2", "todo 3", "todo 4")));

    Assertions.assertTrue(
        todoResponseDTOs
            .stream()
            .map(TodoResponseDTO::description)
            .toList()
            .containsAll(
                List.of("todo 1 description", "todo 2 description", "todo 3 description",
                    "todo 4 description")));

    Assertions.assertTrue(
        todoResponseDTOs
            .stream()
            .map(TodoResponseDTO::status)
            .toList()
            .containsAll(
                List.of(TodoStatus.TO_DO, TodoStatus.IN_PROGRESS, TodoStatus.DONE,
                    TodoStatus.ON_HOLD)));

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

    ResponseEntity<List<TodoResponseDTO>> responseEntity = todoController.getTodos(principal,
        group.getId());

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

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    // When

    ResponseEntity<TodoResponseDTO> responseEntity = todoController.createTodo(principal,
        group.getId(), createTodoRequestDTO);

    // Then

    Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    TodoResponseDTO todoResponseDTO = responseEntity.getBody();

    Assertions.assertEquals("todo title", todoResponseDTO.title());
    Assertions.assertEquals("todo description", todoResponseDTO.description());
    Assertions.assertEquals(TodoStatus.TO_DO, todoResponseDTO.status());
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

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    // When

    Runnable lambda = () -> todoController.createTodo(principal, group.getId(),
        createTodoRequestDTO);

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

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    // When

    Runnable lambda = () -> todoController.createTodo(principal, group.getId(),
        createTodoRequestDTO);

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

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    // When

    Runnable lambda = () -> todoController.createTodo(principal, group.getId(),
        createTodoRequestDTO);

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

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    // When

    Runnable lambda = () -> todoController.createTodo(principal, group.getId(),
        createTodoRequestDTO);

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void deleteTodo_HappyPath() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Principal principal = user::getUsername;

    // When

    ResponseEntity<Void> responseEntity = todoController.deleteTodo(principal, group.getId(),
        todo.getId());

    // Then

    Assertions.assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());

    Assertions.assertTrue(
        em.createQuery("""
            SELECT
              t
            FROM
              Todo t
            WHERE
              t.id = :todoId
            """, Todo.class)
            .setParameter("todoId", todo.getId())
            .getResultStream()
            .findFirst()
            .isEmpty());
  }

  @Test
  void deleteTodo_Failure_NoUser() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(userGroup);
    em.remove(user);

    em.flush();

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(NoMatchingUserException.class, lambda::run);

  }

  @Test
  void deleteTodo_Failure_NoGroupUser() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(userGroup);

    em.flush();

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void deleteTodo_Failure_ImproperGroupRole1() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void deleteTodo_Failure_ImproperGroupRole2() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    User author = new User();
    author.setUsername("username_author");
    author.setNickname("nickname_author");
    author.setHashedPassword("hashedPassword");

    em.persist(author);

    Group group = new Group();
    group.setName("group");
    group.setDescription("description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setUser(user);
    userGroup.setGroup(group);
    userGroup.setGroupRole(GroupRole.MEMBER);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(author);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Principal principal = author::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void deleteTodo_Failure_NoGroup() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(group);
    em.remove(userGroup);

    em.flush();

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void deleteTodo_Failure_NoTodo() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);

    em.flush();

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.deleteTodo(principal, group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);
  }

  @Test
  void updateTodo_HappyPath() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    ResponseEntity<TodoDTO> responseEntity = todoController.updateTodo(principal, group.getId(),
        todo.getId(), updateTodoRequestDTO);

    // Then

    Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    TodoDTO todoDTO = responseEntity.getBody();

    Assertions.assertEquals(user.getId(), todoDTO.getAuthorId());
    Assertions.assertEquals(group.getId(), todoDTO.getGroupId());
    Assertions.assertEquals("updated todo title", todoDTO.getTitle());
    Assertions.assertEquals("updated todo description", todoDTO.getDescription());
    Assertions.assertEquals(TodoStatus.DONE, todoDTO.getStatus());

    Todo updatedTodo = em
        .createQuery("""
            SELECT
              t
            FROM
              Todo t
            WHERE
              t.id = :todoId
            """, Todo.class)
        .setParameter("todoId", todo.getId())
        .getSingleResult();

    Assertions.assertEquals(updatedTodo.getId(), updatedTodo.getId());
    Assertions.assertEquals(user.getId(), updatedTodo.getAuthor().getId());
    Assertions.assertEquals(group.getId(), updatedTodo.getGroup().getId());
    Assertions.assertEquals("updated todo title", updatedTodo.getTitle());
    Assertions.assertEquals("updated todo description", updatedTodo.getDescription());
    Assertions.assertEquals(TodoStatus.DONE, updatedTodo.getTodoStatus());
  }

  @Test
  void updateTodo_Failure_NoUser() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(userGroup);
    em.remove(user);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(NoMatchingUserException.class, lambda::run);

  }

  @Test
  void updateTodo_Failure_NoUserGroup() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(userGroup);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void updateTodo_Failure_ImproperGroupRole1() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void updateTodo_Failure_ImproperGroupRole2() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    User author = new User();
    author.setUsername("username_author");
    author.setNickname("nickname_author");
    author.setHashedPassword("hashedPassword");

    em.persist(author);

    Group group = new Group();
    group.setName("group");
    group.setDescription("description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setUser(user);
    userGroup.setGroup(group);
    userGroup.setGroupRole(GroupRole.MEMBER);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(author);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void updateTodo_Failure_NoGroup() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(userGroup);
    em.remove(group);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(UserAccessDeniedException.class, lambda::run);
  }

  @Test
  void updateTodo_Failure_NoTodo() {
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

    Todo todo = new Todo();
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.DONE,
        "someOrder"
    );

    Principal principal = user::getUsername;

    // When

    Runnable lambda = () -> todoController.updateTodo(principal, group.getId(), todo.getId(),
        updateTodoRequestDTO);

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);
  }
}
