package rest.felix.back.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateGroupRequestDTO {
    @NotNull
    @Size(min = 10, max = 50)
    private String name;

}
