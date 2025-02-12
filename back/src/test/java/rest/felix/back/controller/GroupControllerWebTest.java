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
import rest.felix.back.dto.request.CreateGroupRequestDTO;
import rest.felix.back.entity.User;
import rest.felix.back.security.JwtTokenProvider;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GroupControllerWebTest {

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

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");
        String body = objectMapper.writeValueAsString(createGroupRequestDTO);

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then

        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").isNotEmpty());
        result.andExpect(jsonPath("$.name").value("groupName"));


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

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");
        String body = objectMapper.writeValueAsString(createGroupRequestDTO);

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then

        result.andExpect(status().isUnauthorized());

    }

    @Test
    public void createGroup_Failure_NoCookie() throws Exception {

        // Given

        String path = "/api/v1/group";

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");
        String body = objectMapper.writeValueAsString(createGroupRequestDTO);

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

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

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO(null);
        String body = objectMapper.writeValueAsString(createGroupRequestDTO);

        // When

        ResultActions result = mvc.perform(
                post(path)
                        .cookie(cookie)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then

        result.andExpect(status().isBadRequest());

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
            CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");
            String body = objectMapper.writeValueAsString(createGroupRequestDTO);
            mvc.perform(
                            post("/api/v1/group")
                                    .content(body)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .cookie(cookie)
                    )
                    .andExpect(status().isCreated());
        }

        String path = "/api/v1/group";

        // When

        ResultActions result = mvc.perform(
                get(path)
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then

        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$", hasSize(3)));
        result.andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
        result.andExpect(jsonPath("$[*].name", everyItem(equalTo("groupName"))));

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
                        .accept(MediaType.APPLICATION_JSON)
        );

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
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then


        result.andExpect(status().isForbidden());

    }

}
