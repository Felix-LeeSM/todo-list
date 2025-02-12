package rest.felix.back.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.User;

import java.util.Arrays;
import java.util.List;

@Transactional
@SpringBootTest
class GroupServiceTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private GroupService groupService;

    @Test
    void createGroup_HappyPath() {
        // Given

        User user = new User();
        user.setNickname("nickname");
        user.setUsername("username");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        CreateGroupDTO createGroupDTO = new CreateGroupDTO(user.getId(), "groupName", "group description");

        em.flush();

        // When

        GroupDTO groupDTO = groupService.createGroup(createGroupDTO);

        // Then

        Assertions.assertEquals("groupName", groupDTO.getName());

        Group createdGroup = em
                .createQuery("SELECT g FROM Group g WHERE g.name = :groupName", Group.class)
                .setParameter("groupName", "groupName")
                .getSingleResult();

        Assertions.assertEquals(createdGroup.getId(), groupDTO.getId());
        Assertions.assertEquals("group description", createdGroup.getDescription());

    }

    @Test
    void createGroup_Failure_NoSuchUser() {
        // Given

        User user = new User();
        user.setNickname("nickname");
        user.setUsername("username");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        CreateGroupDTO createGroupDTO = new CreateGroupDTO(user.getId(), "groupName", "group description");

        em.remove(user);
        em.flush();

        // When

        Runnable lambda = () -> groupService.createGroup(createGroupDTO);

        // Then

        Assertions.assertThrows(DataIntegrityViolationException.class, lambda::run);

//        DataIntegrityViolationException
    }

    @Test
    void getGroupsByUserId_HappyPath() {

        // Given

        User user1 = new User();
        user1.setNickname("nickname1");
        user1.setUsername("username1");
        user1.setHashedPassword("hashedPassword");

        User user2 = new User();
        user2.setNickname("nickname2");
        user2.setUsername("username2");
        user2.setHashedPassword("hashedPassword");

        em.persist(user1);
        em.persist(user2);

        Arrays.stream(new int[]{1, 2, 3}).forEach(idx -> {
            groupService.createGroup(
                    new CreateGroupDTO(
                            user1.getId(),
                            String.format("user1 group%d", idx),
                            String.format("user1 group%d description", idx)));
            groupService.createGroup(
                    new CreateGroupDTO(
                            user2.getId(),
                            String.format("user2 group%d", idx),
                            String.format("user2 group%d description", idx)));
        });

        em.flush();

        // When

        List<GroupDTO> user1GroupDTOs = groupService.getGroupsByUserId(user1.getId());
        List<GroupDTO> user2GroupDTOs = groupService.getGroupsByUserId(user2.getId());

        // Then

        Assertions.assertEquals(3, user1GroupDTOs.size());
        Assertions.assertEquals(3, user2GroupDTOs.size());


        Assertions.assertTrue(user1GroupDTOs.stream()
                .map(GroupDTO::getName)
                .toList()
                .containsAll(
                        List.of("user1 group1", "user1 group2", "user1 group3")
                )
        );

        Assertions.assertTrue(user1GroupDTOs.stream()
                .map(GroupDTO::getDescription)
                .toList()
                .containsAll(
                        List.of("user1 group1 description", "user1 group2 description", "user1 group3 description")
                )
        );


        Assertions.assertTrue(user2GroupDTOs.stream()
                .map(GroupDTO::getName)
                .toList()
                .containsAll(
                        List.of("user2 group1", "user2 group2", "user2 group3")
                )
        );

        Assertions.assertTrue(user2GroupDTOs.stream()
                .map(GroupDTO::getDescription)
                .toList()
                .containsAll(
                        List.of("user2 group1 description", "user2 group2 description", "user2 group3 description")
                )
        );


    }

    @Test
    void getGroupsByUserId_HappyPath_NoUser() {

        // Given

        User user = new User();
        user.setNickname("nickname1");
        user.setUsername("username1");
        user.setHashedPassword("hashedPassword");

        em.persist(user);
        em.remove(user);
        em.flush();


        // When

        List<GroupDTO> userGroupDTOs = groupService.getGroupsByUserId(user.getId());

        // Then

        Assertions.assertEquals(0, userGroupDTOs.size());


    }

    @Test
    void getGroupsByUserId_HappyPath_NoGroup() {

        // Given

        User user = new User();
        user.setNickname("nickname1");
        user.setUsername("username1");
        user.setHashedPassword("hashedPassword");

        em.persist(user);

        em.flush();


        // When

        List<GroupDTO> userGroupDTOs = groupService.getGroupsByUserId(user.getId());

        // Then

        Assertions.assertEquals(0, userGroupDTOs.size());


    }
}