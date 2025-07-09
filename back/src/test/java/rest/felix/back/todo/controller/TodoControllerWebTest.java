package rest.felix.back.todo.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rest.felix.back.todo.dto.CreateTodoRequestDTO;
import rest.felix.back.todo.dto.UpdateTodoRequestDTO;
import rest.felix.back.group.entity.Group;
import rest.felix.back.todo.entity.Todo;
import rest.felix.back.user.entity.User;
import rest.felix.back.group.entity.UserGroup;
import rest.felix.back.group.entity.enumerated.GroupRole;
import rest.felix.back.todo.entity.enumerated.TodoStatus;
import rest.felix.back.common.security.JwtTokenProvider;
import rest.felix.back.common.util.Pair;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TodoControllerWebTest {

  @Autowired
  EntityManager em;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  private Cookie userCookie(String username) {
    return new Cookie("accessToken", jwtTokenProvider.generateToken("username123"));
  }

  @Test
  void getTodos_HappyPath() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

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

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$", hasSize(4)));
    result.andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
    result.andExpect(jsonPath("$[*].authorId", everyItem(equalTo(user.getId().intValue()))));
    result.andExpect(jsonPath("$[*].groupId", everyItem(equalTo(group.getId().intValue()))));
    result.andExpect(
        jsonPath("$[*].title", containsInAnyOrder("todo 1", "todo 2", "todo 3", "todo 4")));
    result.andExpect(jsonPath("$[*].description",
        containsInAnyOrder("todo 1 description", "todo 2 description", "todo 3 description",
            "todo 4 description")));
    result.andExpect(
        jsonPath("$[*].status", containsInAnyOrder("TO_DO", "IN_PROGRESS", "DONE", "ON_HOLD")));

  }

  @Test
  void getTodos_HappyPath_NoTodo() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void getTodos_Failure_NoUser() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.remove(user);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));
  }

  @Test
  void getTodos_Failure_NoGroup() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.remove(group);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void getTodos_Failure_NoUserGroup() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  void createTodo_HappyPath() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isCreated());
    result.andExpect(jsonPath("$.id", notNullValue()));
    result.andExpect(jsonPath("$.authorId", equalTo(user.getId().intValue())));
    result.andExpect(jsonPath("$.groupId", equalTo(group.getId().intValue())));
    result.andExpect(jsonPath("$.title", equalTo("todo title")));
    result.andExpect(jsonPath("$.description", equalTo("todo description")));
    result.andExpect(jsonPath("$.status", equalTo("TO_DO")));

  }

  @Test
  void createTodo_Failure_NoCookie() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
  }

  @Test
  void createTodo_Failure_NoUser() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.remove(user);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));

  }

  @Test
  void createTodo_Failure_NoGroup() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.remove(group);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  void createTodo_Failure_NoUserGroup() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.OWNER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  void createTodo_Failure_GroupRoleViewer() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    em.flush();

    em.remove(userGroup);
    em.remove(group);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title",
        "todo description");

    String body = objectMapper.writeValueAsString(createTodoRequestDTO);

    String path = String.format("/api/v1/group/%d/todo", group.getId());

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  void deleteTodo_HappyPath() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MANAGER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isNoContent());

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
  void deleteTodo_Failure_NoUser() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MANAGER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(userGroup);
    em.remove(user);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));
  }

  @Test
  void deleteTodo_Failure_NoGroupUser() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MANAGER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(userGroup);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void deleteTodo_Failure_ImproperGroupRole1() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(userGroup);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void deleteTodo_Failure_ImproperGroupRole2() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    User author = new User();
    author.setUsername("username_author");
    author.setNickname("nickname_author");
    author.setHashedPassword("hashedPassword");

    em.persist(author);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MEMBER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    UserGroup authorUserGroup = new UserGroup();
    authorUserGroup.setGroupRole(GroupRole.MEMBER);
    authorUserGroup.setUser(author);
    authorUserGroup.setGroup(group);

    em.persist(authorUserGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(author);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void deleteTodo_Failure_NoGroup() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(todo);
    em.remove(userGroup);
    em.remove(group);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void deleteTodo_Failure_NoTodo() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

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

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void updateTodo_HappyPath() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MEMBER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id", equalTo(todo.getId().intValue())));
    result.andExpect(jsonPath("$.title", equalTo("updated todo title")));
    result.andExpect(jsonPath("$.description", equalTo("updated todo description")));
    result.andExpect(jsonPath("$.status", equalTo("ON_HOLD")));
    result.andExpect(jsonPath("$.authorId", equalTo(user.getId().intValue())));
    result.andExpect(jsonPath("$.groupId", equalTo(group.getId().intValue())));

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

    Assertions.assertEquals(todo.getId(), updatedTodo.getId());
    Assertions.assertEquals("updated todo title", updatedTodo.getTitle());
    Assertions.assertEquals("updated todo description", updatedTodo.getDescription());
    Assertions.assertEquals(TodoStatus.ON_HOLD, updatedTodo.getTodoStatus());
    Assertions.assertEquals(user.getId(), updatedTodo.getAuthor().getId());
    Assertions.assertEquals(group.getId(), updatedTodo.getGroup().getId());
  }

  @Test
  void updateTodo_Failure_NoUser() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MEMBER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
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
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));
  }

  @Test
  void updateTodo_Failure_NoUserGroup() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MANAGER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    em.remove(userGroup);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void updateTodo_Failure_ImproperGroupRole1() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void updateTodo_Failure_ImproperGroupRole2() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    User author = new User();
    author.setUsername("username123_author");
    author.setNickname("nickname_author");
    author.setHashedPassword("hashedPassword");

    em.persist(author);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.MEMBER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    UserGroup authorUserGroup = new UserGroup();
    authorUserGroup.setGroupRole(GroupRole.MEMBER);
    authorUserGroup.setUser(author);
    authorUserGroup.setGroup(group);

    em.persist(authorUserGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(author);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void updateTodo_Failure_NoGroup() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
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
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

  @Test
  void updateTodo_Failure_NoTodo() throws Exception {
    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");

    em.persist(user);

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");

    em.persist(group);

    UserGroup userGroup = new UserGroup();
    userGroup.setGroupRole(GroupRole.VIEWER);
    userGroup.setUser(user);
    userGroup.setGroup(group);

    em.persist(userGroup);

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

    UpdateTodoRequestDTO updateTodoRequestDTO = new UpdateTodoRequestDTO(
        "updated todo title",
        "updated todo description",
        TodoStatus.ON_HOLD);

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d/todo/%d", group.getId(), todo.getId());

    String body = objectMapper.writeValueAsString(updateTodoRequestDTO);

    // When

    ResultActions result = mvc.perform(
        put(path)
            .cookie(cookie)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
  }

}
