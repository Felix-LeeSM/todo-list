package rest.felix.back.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.entity.Group;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepository {

    private final EntityManager em;

    @Autowired
    public GroupRepository(EntityManager em) {
        this.em = em;
    }

    public GroupDTO createGroup(CreateGroupDTO createGroupDTO) {
        long userId = createGroupDTO.getUserId();
        String groupName = createGroupDTO.getGroupName();

        Group group = new Group();
        group.setName(groupName);
        group.setDescription(createGroupDTO.getDescription());

        em.persist(group);

        return new GroupDTO(group.getId(), group.getName(), group.getDescription());
    }

    public List<GroupDTO> getGroupsByUserId(long userId) {
        String query = """
                SELECT
                    g
                FROM
                    UserGroup ug
                JOIN
                    ug.group g
                WHERE
                    ug.user.id = :userId
                """;

        return em
                .createQuery(query, Group.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(group -> new GroupDTO(group.getId(), group.getName(), group.getDescription()))
                .toList();
    }


    public Optional<GroupDTO> getById(long groupId) {
        try {
            String query = """
                    SELECT
                        g
                    FROM
                        Group g
                    WHERE
                        g.id = :groupId
                    """;

            return
                    Optional.of(
                                    em
                                            .createQuery(query, Group.class)
                                            .setParameter("groupId", groupId)
                                            .getSingleResult())
                            .map(group -> new GroupDTO(group.getId(), group.getName(), group.getDescription()));
        } catch (NoResultException e) {
            return Optional.empty();
        }

    }
}
