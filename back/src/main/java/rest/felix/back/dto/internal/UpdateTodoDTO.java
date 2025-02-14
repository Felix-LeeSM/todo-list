package rest.felix.back.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rest.felix.back.entity.enumerated.TodoStatus;

@Getter
@AllArgsConstructor
public class UpdateTodoDTO {

  private final long id;
  private final String title;
  private final String description;
  private final TodoStatus status;

}
