package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTodoDTO {

  private final String title;
  private final String description;
  private final long authorId;
  private final long groupId;
}
