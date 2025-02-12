package rest.felix.back.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import rest.felix.back.dto.request.CreateGroupRequestDTO;
import rest.felix.back.dto.response.GroupResponseDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.User;
import rest.felix.back.entity.UserGroup;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class GroupControllerUnitTest {

    @Autowired
    private GroupController groupController;
    @Autowired
    private EntityManager em;

    @Test
    public void createGroup_HappyPath() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");
        em.persist(user);
        em.flush();

        Principal principal = user::getUsername;

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");

        // When

        ResponseEntity<GroupResponseDTO> responseEntity = groupController.createGroup(principal, createGroupRequestDTO);

        // Then

        Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        GroupResponseDTO groupResponseDTO = responseEntity.getBody();

        Group createdGroup = em.createQuery("""
                        SELECT
                            g
                        FROM
                            Group g
                        WHERE
                            g.id = :groupId
                        
                        """, Group.class)
                .setParameter("groupId", groupResponseDTO.id())
                .getSingleResult();

        Assertions.assertEquals("groupName", createdGroup.getName());

        UserGroup userGroup = em.createQuery("""
                        SELECT
                            ug
                        FROM
                            UserGroup ug
                        WHERE
                            ug.group.id = :groupId AND
                            ug.user.id = :userId
                        """, UserGroup.class)
                .setParameter("groupId", groupResponseDTO.id())
                .setParameter("userId", user.getId())
                .getSingleResult();

        Assertions.assertEquals(GroupRole.OWNER, userGroup.getGroupRole());


    }

    @Test
    public void createGroup_Failure_NoSuchUser() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");
        em.persist(user);
        em.flush();
        em.remove(user);
        em.flush();

        Principal principal = user::getUsername;

        CreateGroupRequestDTO createGroupRequestDTO = new CreateGroupRequestDTO("groupName");

        // When

        Runnable lambda = () -> groupController.createGroup(principal, createGroupRequestDTO);

        // Then

        Assertions.assertThrows(NoMatchingUserException.class, lambda::run);


    }


    @Test
    public void getUserGroups_HappyPath() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");
        em.persist(user);
        em.flush();

        Principal principal = user::getUsername;

        Arrays.stream(new int[]{1, 2, 3})
                .forEach(idx -> {
                    String groupName = String.format("group %d", idx);
                    groupController.createGroup(user::getUsername, new CreateGroupRequestDTO(groupName));
                });

        // When

        ResponseEntity<List<GroupResponseDTO>> responseEntity =
                groupController.getUserGroups(principal);

        // Then

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<GroupResponseDTO> groupResponseDTOS = responseEntity.getBody();

        Assertions.assertEquals(3, groupResponseDTOS.size());

        Assertions.assertTrue(
                groupResponseDTOS
                        .stream()
                        .map(GroupResponseDTO::name)
                        .toList()
                        .containsAll(
                                List.of("group 1", "group 2", "group 3")
                        )

        );


    }

    @Test
    public void getUserGroups_HappyPath_NoGroup() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");
        em.persist(user);
        em.flush();

        Principal principal = user::getUsername;


        // When

        ResponseEntity<List<GroupResponseDTO>> responseEntity =
                groupController.getUserGroups(principal);

        // Then

        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<GroupResponseDTO> groupResponseDTOS = responseEntity.getBody();

        Assertions.assertEquals(0, groupResponseDTOS.size());


    }

    @Test
    public void getUserGroups_Failure_NoSuchUser() {
        // Given

        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
        user.setHashedPassword("hashedPassword");
        em.persist(user);
        em.flush();
        em.remove(user);
        em.flush();

        Principal principal = user::getUsername;

        // When

        Runnable lambda = () -> groupController.getUserGroups(principal);

        // Then

        Assertions.assertThrows(NoMatchingUserException.class, lambda::run);

    }


}
