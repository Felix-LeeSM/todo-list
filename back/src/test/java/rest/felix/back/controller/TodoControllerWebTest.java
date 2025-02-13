package rest.felix.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import rest.felix.back.dto.request.CreateTodoRequestDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.entity.UserGroup;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.entity.enumerated.TodoStatus;
import rest.felix.back.security.JwtTokenProvider;
import rest.felix.back.utility.Pair;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TodoControllerWebTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    EntityManager em;
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

        Cookie cookie = userCookie(user.getUsername());

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                get(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", hasSize(4)));
        result.andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
        result.andExpect(jsonPath("$[*].authorId", everyItem(equalTo(user.getId().intValue()))));
        result.andExpect(jsonPath("$[*].groupId", everyItem(equalTo(group.getId().intValue()))));
        result.andExpect(jsonPath("$[*].title", containsInAnyOrder("todo 1", "todo 2", "todo 3", "todo 4")));
        result.andExpect(jsonPath("$[*].description", containsInAnyOrder("todo 1 description", "todo 2 description", "todo 3 description", "todo 4 description")));
        result.andExpect(jsonPath("$[*].todoStatus", containsInAnyOrder("PENDING", "ACTIVE", "IMMINENT", "DONE")));
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
                        .contentType(MediaType.APPLICATION_JSON)
        );

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
                        .contentType(MediaType.APPLICATION_JSON)
        );

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
                        .contentType(MediaType.APPLICATION_JSON)
        );

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
                        .contentType(MediaType.APPLICATION_JSON)
        );

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

        // Then

        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id", notNullValue()));
        result.andExpect(jsonPath("$.authorId", equalTo(user.getId().intValue())));
        result.andExpect(jsonPath("$.groupId", equalTo(group.getId().intValue())));
        result.andExpect(jsonPath("$.title", equalTo("todo title")));
        result.andExpect(jsonPath("$.description", equalTo("todo description")));
        result.andExpect(jsonPath("$.todoStatus", equalTo("PENDING")));

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

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

        CreateTodoRequestDTO createTodoRequestDTO = new CreateTodoRequestDTO("todo title", "todo description");

        String body = objectMapper.writeValueAsString(createTodoRequestDTO);

        String path = String.format("/api/v1/group/%d/todo", group.getId());

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );

        // Then

        result.andExpect(status().isForbidden());
        result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

    }
}
