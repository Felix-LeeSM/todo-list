package rest.felix.back.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import rest.felix.back.dto.internal.CreateTodoDTO;
import rest.felix.back.dto.internal.TodoDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.entity.enumerated.TodoStatus;

import java.util.Arrays;
import java.util.List;


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
                .map(status -> status == TodoStatus.PENDING)
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
                        List.of("todo 1", "todo 2", "todo 3")
                )
        );

        Assertions.assertTrue(todoDTOs
                .stream()
                .map(TodoDTO::getDescription)
                .toList()
                .containsAll(
                        List.of("todo 1 description", "todo 2 description", "todo 3 description")
                )
        );


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

        CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(), group.getId());

        // When

        TodoDTO todoDTO = todoService.createTodo(createTodoDTO);

        // Then

        Assertions.assertEquals("todo title", todoDTO.getTitle());
        Assertions.assertEquals("todo description", todoDTO.getDescription());
        Assertions.assertEquals(TodoStatus.PENDING, todoDTO.getStatus());
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

        CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(), group.getId());

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

        CreateTodoDTO createTodoDTO = new CreateTodoDTO("todo title", "todo description", user.getId(), group.getId());

        // When

        Runnable lambda = () -> todoService.createTodo(createTodoDTO);

        // Then

        Assertions.assertThrows(DataIntegrityViolationException.class, lambda::run);

    }

}