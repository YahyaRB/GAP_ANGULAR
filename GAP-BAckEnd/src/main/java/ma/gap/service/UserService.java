package ma.gap.service;

import ma.gap.entity.User;
import ma.gap.entity.Role;
import ma.gap.payload.request.SignupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public User saveUser(SignupRequest signUpRequest);
    public User saveUsers(User user);
    public List<User> getAllUsers();
     User finduserById(Long id);
     Boolean deleteUser(Long id);
     User updateUser(User user,long id);
     User findbyusername(long idUser);
     List<User> finuserbyrole(List<Role> roles);

     void sessionUser(long id);
    List<User> getAllLogins();
    boolean findUsernameExiste(String username);
     boolean findEmailExiste(String email);
    Optional<User> findUserByResetToken(String resetToken);
    String roleUserConnected(User user);
    boolean isValidPassword(long id, String password);
    Optional<User> findUserByEmail(String email);
    void updatePassword(long id,String newPassword);
    String getCurrentUsername();
    boolean isRoleAutorize(long idIser);
    boolean findAtelierById(long atelier,long idUser);
}
