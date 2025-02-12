package rest.felix.back.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.entity.enumerated.GroupRole;
import rest.felix.back.repository.GroupRepository;

import java.util.List;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupDTO createGroup(CreateGroupDTO createGroupDTO) {

        GroupDTO groupDTO = groupRepository.createGroup(createGroupDTO);

        groupRepository.registerUserToGroup(createGroupDTO.getUserId(), groupDTO.getId(), GroupRole.OWNER);

        return groupDTO;
    }

    public List<GroupDTO>getGroupsByUserId(long userId) {

        return groupRepository.getGroupsByUserId(userId);
    }

}
