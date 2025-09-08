package ma.gap.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.gap.entity.Ateliers;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String nom;
    private String prenom;
    private String session;
    private List<Ateliers> atelier;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username,String nom,String prenom,String session,List<Ateliers> ateliers,List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.nom=nom;
        this.prenom=prenom;
        this.session=session;
        this.atelier=ateliers;
        this.roles = roles;
    }

    public JwtResponse(String jwt, Long id, String username, String email, String password, List<String> roles) {
    }
}
