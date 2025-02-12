package rest.felix.back.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.felix.back.dto.internal.CreateGroupDTO;
import rest.felix.back.dto.internal.GroupDTO;
import rest.felix.back.dto.internal.UserDTO;
import rest.felix.back.dto.request.CreateGroupRequestDTO;
import rest.felix.back.dto.response.GroupResponseDTO;
import rest.felix.back.exception.throwable.unauthorized.NoMatchingUserException;
import rest.felix.back.service.GroupService;
import rest.felix.back.service.UserService;

import java.security.Principal;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<GroupResponseDTO>> getUserGroups(
            Principal principal
    ) {
        String username = principal.getName();
        UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
        long userId = userDTO.getId();

        List<GroupResponseDTO> groupResponseDTOS =
                groupService.getGroupsByUserId(userId)
                        .stream()
                        .map(groupDTO -> new GroupResponseDTO(groupDTO.getId(), groupDTO.getName()))
                        .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(groupResponseDTOS);

    }

}
