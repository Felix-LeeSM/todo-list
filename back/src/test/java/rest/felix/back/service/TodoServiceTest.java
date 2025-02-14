package rest.felix.back.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.dto.internal.UpdateTodoDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.entity.enumerated.TodoStatus;
import rest.felix.back.exception.throwable.notfound.ResourceNotFoundException;
import rest.felix.back.utility.Pair;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TodoServiceTest {

  @Autowired
  private EntityManager em;
  @Autowired
  private TodoService todoService;

  @Test
  void getTodosInGroup_HappyPath() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Arrays.stream(new int[]{1, 2, 3}).forEach(idx -> {
      Todo todo = new Todo();
      todo.setGroup(group);
      todo.setAuthor(user);
      todo.setTitle(String.format("todo %d", idx));
      todo.setDescription(String.format("todo %d description", idx));
      em.persist(todo);
    });

    em.flush();

    // When

    List<TodoDTO> todoDTOs = todoService.getTodosInGroup(group.getId());

    // Then

    Assertions.assertEquals(3, todoDTOs.size());

    Assertions.assertTrue(todoDTOs
        .stream()
        .map(TodoDTO::getStatus)
        .map(status -> status == TodoStatus.TO_DO)
        .reduce(true, (one, another) -> one && another));

    Assertions.assertTrue(todoDTOs
        .stream()
        .map(TodoDTO::getGroupId)
        .map(groupId -> groupId.equals(group.getId()))
        .reduce(true, (one, another) -> one && another));

    Assertions.assertTrue(todoDTOs
        .stream()
        .map(TodoDTO::getAuthorId)
        .map(authorId -> authorId.equals(user.getId()))
        .reduce(true, (one, another) -> one && another));

    Assertions.assertTrue(todoDTOs
        .stream()
        .map(TodoDTO::getTitle)
        .toList()
        .containsAll(
            List.of("todo 1", "todo 2", "todo 3")));

    Assertions.assertTrue(todoDTOs
        .stream()
        .map(TodoDTO::getDescription)
        .toList()
        .containsAll(
            List.of("todo 1 description", "todo 2 description", "todo 3 description")));

  }

  @Test
  void getTodosInGroup_HappyPath_NoTodo() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    em.flush();

    // When

    List<TodoDTO> todoDTOs = todoService.getTodosInGroup(group.getId());

    // Then

    Assertions.assertEquals(0, todoDTOs.size());
  }

  @Test
  void getTodosInGroup_HappyPath_NoGroup() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    em.remove(group);

    em.flush();

    // When

    List<TodoDTO> todoDTOs = todoService.getTodosInGroup(group.getId());

    // Then

    Assertions.assertEquals(0, todoDTOs.size());
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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    em.flush();

    CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(),
        group.getId());

    // When

    TodoDTO todoDTO = todoService.createTodo(createTodoDTO);

    // Then

    Assertions.assertEquals("todo title", todoDTO.getTitle());
    Assertions.assertEquals("todo description", todoDTO.getDescription());
    Assertions.assertEquals(TodoStatus.TO_DO, todoDTO.getStatus());
    Assertions.assertEquals(user.getId(), todoDTO.getAuthorId());
    Assertions.assertEquals(group.getId(), todoDTO.getGroupId());

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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    em.flush();

    em.remove(user);

    em.flush();

    CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(),
        group.getId());

    // When

    Runnable lambda = () -> todoService.createTodo(createTodoDTO);

    // Then

    Assertions.assertThrows(DataIntegrityViolationException.class, lambda::run);

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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    em.flush();

    em.remove(group);

    em.flush();

    CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(),
        group.getId());

    // When

    Runnable lambda = () -> todoService.createTodo(createTodoDTO);

    // Then

    Assertions.assertThrows(DataIntegrityViolationException.class, lambda::run);

  }

  @Test
  void getTodoInGroup_HappyPath() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    List<Todo> todos = Stream.of(
            new Pair<>(TodoStatus.TO_DO, 1),
            new Pair<>(TodoStatus.IN_PROGRESS, 2),
            new Pair<>(TodoStatus.DONE, 3),
            new Pair<>(TodoStatus.ON_HOLD, 4))
        .map(pair -> {
          TodoStatus todoStatus = pair.first();
          int idx = pair.second();

          Todo todo = new Todo();
          todo.setTitle(String.format("todo %d", idx));
          todo.setDescription(String.format("todo %d description", idx));
          todo.setTodoStatus(todoStatus);
          todo.setAuthor(user);
          todo.setGroup(group);
          em.persist(todo);
          return todo;

        })
        .toList();

    em.flush();

    todos
        .forEach(todo -> {

          // When

          TodoDTO todoDTO = todoService.getTodoInGroup(group.getId(), todo.getId());

          // Then

          Assertions.assertEquals(todo.getId(), todoDTO.getId());
          Assertions.assertEquals(todo.getTodoStatus(), todoDTO.getStatus());
          Assertions.assertEquals(todo.getDescription(), todoDTO.getDescription());
          Assertions.assertEquals(todo.getTitle(), todoDTO.getTitle());
          Assertions.assertEquals(todo.getGroup().getId(), todoDTO.getGroupId());
          Assertions.assertEquals(todo.getAuthor().getId(), todoDTO.getAuthorId());

        });

  }

  @Test
  void getTodoInGroup_Failure_NoTodo() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);
    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.flush();

    // When

    Runnable lambda = () -> todoService.getTodoInGroup(group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);
  }

  @Test
  void getTodoInGroup_Failure_NoGroup() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);
    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(group);
    em.flush();

    // When

    Runnable lambda = () -> todoService.getTodoInGroup(group.getId(), todo.getId());

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);
  }

  @Test
  void getTodoInGroup_Failure_WrongGroup() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group1 = new Group();
    group1.setName("group1 name");
    group1.setDescription("group1 description");

    Group group2 = new Group();
    group2.setName("group2 name");
    group2.setDescription("group2 description");

    em.persist(group1);
    em.persist(group2);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group1);
    em.persist(todo);

    em.flush();

    // When

    Runnable lambda = () -> todoService.getTodoInGroup(group2.getId(), todo.getId());

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);
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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);
    em.persist(todo);

    em.flush();

    // When

    todoService.deleteTodo(todo.getId());

    // Then

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
            .getResultList()
            .stream()
            .findFirst()
            .isEmpty()
    );
  }

  @Test
  void deleteTodo_HappyPath_NoTodo() {
    // Given

    User user = new User();
    user.setUsername("username");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.flush();

    // When

    Runnable lambda = () -> todoService.deleteTodo(todo.getId());

    // Then

    Assertions.assertDoesNotThrow(lambda::run);


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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoDTO updateTodoDTO = new UpdateTodoDTO(
        todo.getId(),
        "todo updated title",
        "todo updated description",
        TodoStatus.DONE);

    // When

    TodoDTO todoDTO = todoService.updateTodo(updateTodoDTO);

    // Then

    Assertions.assertEquals(todo.getId(), todoDTO.getId());
    Assertions.assertEquals("todo updated title", todoDTO.getTitle());
    Assertions.assertEquals("todo updated description", todoDTO.getDescription());
    Assertions.assertEquals(TodoStatus.DONE, todoDTO.getStatus());

    Todo updatedTodo = em.createQuery("""
            SELECT
              t
            FROM
              Todo t
            WHERE
              t.id = :todoId
            """, Todo.class)
        .setParameter("todoId", todo.getId())
        .getSingleResult();

    Assertions.assertEquals("todo updated title", updatedTodo.getTitle());
    Assertions.assertEquals("todo updated description", updatedTodo.getDescription());
    Assertions.assertEquals(TodoStatus.DONE, updatedTodo.getTodoStatus());

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
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);

    em.flush();

    UpdateTodoDTO updateTodoDTO = new UpdateTodoDTO(
        todo.getId(),
        "todo updated title",
        "todo updated description",
        TodoStatus.DONE);

    // When

    Runnable lambda = () -> todoService.updateTodo(updateTodoDTO);

    // Then

    Assertions.assertThrows(ResourceNotFoundException.class, lambda::run);

  }

}