package lk.ijse.dep11.todo.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskTO implements Serializable {
    // We should use wrapper
    @Null(message = "Id should be empty")
    private Integer id;
    @NotBlank(message = "Description should not be empty")
    private String description;
    @Null(message = "Status should be empty", groups = Create.class)  // CREATE
    @NotNull(message = "Status should not be empty", groups = Update.class)  // UPDATE
    private Boolean status;

    @NotEmpty(message = "Email can't be empty")
    @Email
    private String email;

   public interface Update extends Default {} // if we send Patch request it should contain status & description
   public interface Create extends Default {} // if we send Patch request it should contain only description
}
