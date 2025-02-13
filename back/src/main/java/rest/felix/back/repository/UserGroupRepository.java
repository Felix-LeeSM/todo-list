package rest.felix.back.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rest.felix.back.dto.internal.UserGroupDTO;
import rest.felix.back.entity.Group;
import rest.felix.back.entity.User;
import rest.felix.back.entity.UserGroup;
import rest.felix.back.entity.enumerated.GroupRole;

import java.util.Optional;

@Repository
public class UserGroupRepository {
    private final EntityManager em;

    @Autowired
    public UserGroupRepository(EntityManager em) {
        this.em = em;
    }

    public Optional<UserGroupDTO> getByUserIdAndGroupId(long userId, long groupId) {
        try {
            return Optional.of(
                            em.createQuery("""
                                            SELECT
                                                ug
                                            FROM
                                                UserGroup ug
                                            WHERE
                                                ug.user.id = :userId AND
                                                ug.group.id = :groupId
                                            """, UserGroup.class)
                                    .setParameter("userId", userId)
                                    .setParameter("groupId", groupId)
                                    .getSingleResult())
                    .map(userGroup -> new UserGroupDTO(userGroup.getGroupRole(), userId, groupId));

        } catch (NoResultException e) {
            return Optional.empty();
        }


    }

    public void registerUserToGroup(long userId, long groupId, GroupRole role) {
        User userRef = em.getReference(User.class, userId);
        Group groupRef = em.getReference(Group.class, groupId);

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(userRef);
        userGroup.setGroup(groupRef);
        userGroup.setGroupRole(role);

        em.persist(userGroup);

    }
}
