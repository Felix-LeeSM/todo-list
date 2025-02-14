package rest.felix.back.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
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
import rest.felix.back.dto.request.CreateGroupRequestDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.Todo;
import rest.felix.back.entity.User;
import rest.felix.back.entity.UserGroup;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.entity.enumerated.TodoStatus;
import rest.felix.back.security.JwtTokenProvider;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupControllerWebTest {

  @Autowired
  private EntityManager em;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  private Cookie userCookie(String username) {
    return new Cookie("accessToken", jwtTokenProvider.generateToken(username));
  }

  @Test
  public void createGroup_HappyPath() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");
    em.persist(user);
    em.flush();
    Cookie cookie = userCookie(user.getUsername());

    String path = "/api/v1/group";

    CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName",
        "group description");
    String body = objectMapper.writeValueAsString(createGroupRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isCreated());
    result.andExpect(jsonPath("$.id").isNotEmpty());
    result.andExpect(jsonPath("$.name").value("groupName"));
    result.andExpect(jsonPath("$.description").value("group description"));

  }

  @Test
  public void createGroup_Failure_NoSuchUser() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");
    em.persist(user);
    em.flush();
    em.remove(user);
    em.flush();
    Cookie cookie = userCookie(user.getUsername());

    String path = "/api/v1/group";

    CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName",
        "description");
    String body = objectMapper.writeValueAsString(createGroupRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .cookie(cookie)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isUnauthorized());

  }

  @Test
  public void createGroup_Failure_NoCookie() throws Exception {

    // Given

    String path = "/api/v1/group";

    CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName",
        "group description");
    String body = objectMapper.writeValueAsString(createGroupRequestDTO);

    // When

    ResultActions result = mvc.perform(
        post(path)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());

  }

  @Test
  public void createGroup_Failure_InvalidArgument() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");
    em.persist(user);
    em.flush();
    Cookie cookie = userCookie(user.getUsername());

    String path = "/api/v1/group";

    for (String[] row : new String[][]{{"groupName", null}, {null, "group description"}}) {
      CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO(row[0], row[1]);
      String body = objectMapper.writeValueAsString(createGroupRequestDTO);

      // When

      ResultActions result = mvc.perform(
          post(path)
              .cookie(cookie)
              .content(body)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON));

      // Then

      result.andExpect(status().isBadRequest());

    }

  }

  @Test
  public void getUserGroups_HappyPath() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");
    em.persist(user);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    for (int idx : new int[]{1, 2, 3}) {
      CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName",
          "group description");
      String body = objectMapper.writeValueAsString(createGroupRequestDTO);
      mvc.perform(
              post("/api/v1/group")
                  .content(body)
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON)
                  .cookie(cookie))
          .andExpect(status().isCreated());
    }

    String path = "/api/v1/group";

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$", hasSize(3)));
    result.andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
    result.andExpect(jsonPath("$[*].name", everyItem(equalTo("groupName"))));
    result.andExpect(jsonPath("$[*].description", everyItem(equalTo("group description"))));

  }

  @Test
  public void getUserGroups_HappyPath_NoGroup() throws Exception {

    // Given

    User user = new User();
    user.setUsername("username123");
    user.setNickname("nickname");
    user.setHashedPassword("hashedPassword");
    em.persist(user);
    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = "/api/v1/group";

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$", hasSize(0)));

  }

  @Test
  public void getUserGroups_Failure_NoSuchUser() throws Exception {

    // Given

    String path = "/api/v1/group";

    // When

    ResultActions result = mvc.perform(
        get(path)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());

  }

  @Test
  public void getUserGroup_HappyPath() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isOk());
    result.andExpect(jsonPath("$.id", notNullValue()));
    result.andExpect(jsonPath("$.name", equalTo("group name")));
    result.andExpect(jsonPath("$.description", equalTo("group description")));

  }

  @Test
  public void getUserGroup_Failure_NoCookie() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());

  }

  @Test
  public void getUserGroup_Failure_NoUser() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));

  }

  @Test
  public void getUserGroup_Failure_NoGroup() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  public void getUserGroup_Failure_NoUserGroup() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        get(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  public void deleteGroup_HappyPath() throws Exception {

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

    Todo todo = new Todo();
    todo.setTitle("todo title");
    todo.setDescription("todo description");
    todo.setTodoStatus(TodoStatus.IN_PROGRESS);
    todo.setAuthor(user);
    todo.setGroup(group);

    em.persist(todo);

    em.flush();

    Cookie cookie = userCookie(user.getUsername());

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isNoContent());

    Assertions.assertTrue(
        em.createQuery("""
                SELECT
                  g
                FROM
                  Group g
                WHERE
                  g.id = :groupId
                """, Group.class)
            .setParameter("groupId", group.getId())
            .getResultStream()
            .findFirst()
            .isEmpty()
    );

    Assertions.assertTrue(
        em.createQuery("""
                SELECT
                  t
                FROM
                  Todo t
                WHERE
                  t.group.id = :groupId
                """, Todo.class)
            .setParameter("groupId", group.getId())
            .getResultStream()
            .findFirst()
            .isEmpty()
    );

    Assertions.assertTrue(
        em.createQuery("""
                SELECT
                  ug
                FROM
                  UserGroup ug
                WHERE
                  ug.group.id = :groupId
                """, UserGroup.class)
            .setParameter("groupId", group.getId())
            .getResultStream()
            .findFirst()
            .isEmpty()
    );

  }

  @Test
  public void deleteGroup_Failure_NoUser() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.message", equalTo("There is no user with given conditions.")));
  }

  @Test
  public void deleteGroup_Failure_NoUserGroup() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }

  @Test
  public void deleteGroup_Failure_ImproperGroupRole() throws Exception {

    // Given

    Group group = new Group();
    group.setName("group name");
    group.setDescription("group description");
    em.persist(group);

    for (GroupRole role : List.of(GroupRole.MANAGER, GroupRole.MEMBER, GroupRole.VIEWER)) {

      User user = new User();
      user.setUsername("username123" + role);
      user.setNickname("nickname");
      user.setHashedPassword("hashedPassword");
      em.persist(user);

      UserGroup userGroup = new UserGroup();
      userGroup.setGroupRole(role);
      userGroup.setUser(user);
      userGroup.setGroup(group);
      em.persist(userGroup);

      em.flush();

      Cookie cookie = userCookie(user.getUsername());

      String path = String.format("/api/v1/group/%d", group.getId());

      // When

      ResultActions result = mvc.perform(
          delete(path)
              .cookie(cookie)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON));

      // Then

      result.andExpect(status().isForbidden());
      result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));
    }

  }

  @Test
  public void deleteGroup_Failure_NoGroup() throws Exception {

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

    String path = String.format("/api/v1/group/%d", group.getId());

    // When

    ResultActions result = mvc.perform(
        delete(path)
            .cookie(cookie)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

    // Then

    result.andExpect(status().isForbidden());
    result.andExpect(jsonPath("$.message", equalTo("No permission to perform this action.")));

  }
}
