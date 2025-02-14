package rest.felix.back.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Service;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.dto.internal.UserGroupDTO;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.exception.throwable.forbidden.UserAccessDeniedException;
import rest.felix.back.exception.throwable.notfound.ResourceNotFoundException;
import rest.felix.back.repository.GroupRepository;
import rest.felix.back.repository.UserGroupRepository;

@Service
@Transactional
@AllArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final UserGroupRepository userGroupRepository;

  public GroupDTO createGroup(CreateGroupDTO createGroupDTO) {

    GroupDTO groupDTO = groupRepository.createGroup(createGroupDTO);

    userGroupRepository.registerUserToGroup(createGroupDTO.getUserId(), groupDTO.getId(),
        GroupRole.OWNER);

    return groupDTO;
  }

  public List<GroupDTO> getGroupsByUserId(long userId) {

    return groupRepository.getGroupsByUserId(userId);
  }

  public GroupDTO getGroupById(long groupId) {

    return groupRepository.getById(groupId).orElseThrow(ResourceNotFoundException::new);
  }

  public GroupRole getUserRoleInGroup(long userId, long groupId) {
    return userGroupRepository
        .getByUserIdAndGroupId(userId, groupId)
        .map(UserGroupDTO::getGroupRole)
        .orElseThrow(UserAccessDeniedException::new);

  }

}
