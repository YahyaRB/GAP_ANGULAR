package ma.gap.repository;

import ma.gap.entity.Role;
import ma.gap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // Change le retour en Optional<User>
    List<User> findAllByRolesIn(List<Role> roles);
    User findByUsernameAndSession(String username, String session);
    boolean existsByUsername(String s);


    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
}
