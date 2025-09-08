package ma.gap.payload.request;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ma.gap.entity.Ateliers;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private List<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @Size(min = 1, max = 20)
    private String nom;

    @NotBlank
    @Size(min = 1, max = 20)
    private String prenom;

    @NotBlank
    @Size(min = 1, max = 10)
    private String session;

    @NotBlank
    @Size(min = 1, max = 10)
    private String sexe;

    @NotBlank
    @Size(min = 1, max = 10)
    private String matricule;

    private List<Ateliers> ateliers;
}
