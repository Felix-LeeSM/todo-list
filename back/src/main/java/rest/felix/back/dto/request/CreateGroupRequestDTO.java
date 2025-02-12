package rest.felix.back.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateGroupRequestDTO {
    @NotNull
    @Size(min = 5, max = 50)
    private String name;

    @NotNull
    @Size(max = 200)

    private String description;

}
