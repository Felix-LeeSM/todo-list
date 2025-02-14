package rest.felix.back.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

@RestController
@RequestMapping("/api/v1/group")
@AllArgsConstructor
public class GroupController {

  private final UserService userService;
  private final GroupService groupService;

  @PostMapping
  public ResponseEntity<GroupResponseDTO> createGroup(
      Principal principal,
      @RequestBody @Valid CreateGroupRequestDTO createGroupRequestDTO) {
    String username = principal.getName();
    UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);

    CreateGroupDTO createGroupDTO = new CreateGroupDTO(
        userDTO.getId(),
        createGroupRequestDTO.getName(),
        createGroupRequestDTO.getDescription());

    GroupDTO groupDTO = groupService.createGroup(createGroupDTO);

    GroupResponseDTO groupResponseDTO = new GroupResponseDTO(groupDTO.getId(), groupDTO.getName(),
        groupDTO.getDescription());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(groupResponseDTO);
  }

  @GetMapping
  public ResponseEntity<List<GroupResponseDTO>> getUserGroups(
      Principal principal) {
    String username = principal.getName();
    UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
    long userId = userDTO.getId();

    List<GroupResponseDTO> groupResponseDTOS = groupService.getGroupsByUserId(userId)
        .stream()
        .map(groupDTO -> new GroupResponseDTO(groupDTO.getId(), groupDTO.getName(),
            groupDTO.getDescription()))
        .toList();

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(groupResponseDTOS);

  }

  @GetMapping("/{groupId}")
  public ResponseEntity<GroupResponseDTO> getUserGroup(
      Principal principal,
      @PathVariable(name = "groupId") long groupId) {
    String username = principal.getName();
    UserDTO userDTO = userService.getByUsername(username).orElseThrow(NoMatchingUserException::new);
    long userId = userDTO.getId();

    GroupDTO groupDTO = groupService.getGroupById(groupId);
    groupService.getUserRoleInGroup(userId, groupDTO.getId());
    GroupResponseDTO groupResponseDTO = new GroupResponseDTO(groupDTO.getId(), groupDTO.getName(),
        groupDTO.getDescription());

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(groupResponseDTO);

  }

}
