package rest.felix.back.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateTodoRequestDTO {

  @NotEmpty
  @Size(max = 50)
  private String title;

  @NotNull
  @Size(max = 200)
  private String description;

}
