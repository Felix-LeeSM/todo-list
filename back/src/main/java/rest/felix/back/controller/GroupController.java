package rest.felix.back.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.CreateGroupRequestDTO;
import rest.felix.back.dto.response.GroupResponseDTO;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.service.GroupService;
import rest.felix.back.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/group")
public class GroupController {

    private final UserService userService;
    private final GroupService groupService;

    @Autowired
    GroupController(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<GroupResponseDTO> createGroup(
            Principal principal,
            @RequestBody @Valid CreateGroupRequestDTO createGroupRequestDTO) {
        String username = principal.getName();
        UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
        String groupName = createGroupRequestDTO.getName();

        CreateGroupDTO createGroupDTO = new CreateGroupDTO(userDTO.getId(), groupName);

        GroupDTO groupDTO = groupService.createGroup(createGroupDTO);

        GroupResponseDTO groupResponseDTO = new GroupResponseDTO(groupDTO.getId(), groupDTO.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupResponseDTO);
    }

}
