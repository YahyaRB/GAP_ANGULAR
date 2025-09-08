package ma.gap.service;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Role;
import ma.gap.entity.User;
import ma.gap.payload.request.SignupRequest;
import ma.gap.repository.AtelierRepository;
import ma.gap.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserImpService implements UserService{


    private UserRepository userRepository;
    private AtelierRepository atelierRepository;

   // private RoleRepository roleRepository;

    @Override
    public User saveUser(SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return null;
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return null;
        }
        User user = new User();
        String password = signUpRequest.getPassword();
        password = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(password);
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setNom(signUpRequest.getNom());
        user.setPrenom(signUpRequest.getPrenom());
        user.setSession(signUpRequest.getSession());
        user.setMatricule(signUpRequest.getMatricule());
        user.setAteliers(signUpRequest.getAteliers());
        return  userRepository.save(user) ;
    }

    @Override
    public User saveUsers(User user) {
        String password = user.getPassword();
        password = BCrypt.hashpw(password, BCrypt.gensalt());

        user.setPassword(password);

        return userRepository.save(user);

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



    @Override
    public User finduserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.get();
    }

    @Override
    public Boolean deleteUser(Long id) {
        userRepository.deleteById(id);

        return true;
    }

    @Override
    public User updateUser(User user, long id) {
        User user1=userRepository.findById(id).get();
        user1.setId(id);
        user1.setMatricule(user.getMatricule());
        user1.setNom(user.getNom());
        user1.setPrenom(user.getPrenom());
        user1.setEmail(user.getEmail());
        user1.setRoles(user.getRoles());
        user1.setAteliers(user.getAteliers());
        user1.setUsername(user.getUsername());
        user1.setSession(user.getSession());
        return userRepository.save(user1);
    }
    @Override
    public User findbyusername(long idUser) {

        User user = userRepository.findById(idUser).get();
        return user;
    }

    @Override
    public List<User> finuserbyrole(List<Role> roles) {
        return userRepository.findAllByRolesIn(roles);
    }


    @Override
	public boolean findAtelierById(long atelier,long idUser) {
		Optional<Ateliers> atelier1 = atelierRepository.findById(atelier);
		return findbyusername(idUser).getAteliers().contains(atelier1.get());
	}

    @Override
    public List<User> getAllLogins() {
        List<User> logins = userRepository.findAll();
        List<User> sortedLogins = logins.stream()
                .sorted(Comparator.comparing(User::getNom)
                        .thenComparing(User::getPrenom))
                .collect(Collectors.toList());

        return sortedLogins;
    }
    @Override
    public void sessionUser(long id) {
        User user=userRepository.findById(id).get();
        if(user.getSession().equals("Actif"))
            user.setSession("Inactif");
        else{
            user.setSession("Actif");
        }
        updateUser(user,id);
    }
    @Override
    public boolean findUsernameExiste(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean findEmailExiste(String email) {
        return userRepository.existsByEmail(email);
    }
    @Override
    public void updatePassword(long id,String newPassword) {
        User user=userRepository.findById(id).get();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
      
        userRepository.save(user);
    }
    @Override
    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean isValidPassword(long id, String password) {
        User user=userRepository.findById(id).get();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public String roleUserConnected(User user) {
        String roleUser="";
        if(user.getRoles().stream().anyMatch(e -> e.getName().equals("admin"))) {
            roleUser="admin";
        } else if(user.getRoles().stream().anyMatch(e -> e.getName().equals("comptable"))){
            roleUser="comptable";
        }
        else if(user.getRoles().stream().anyMatch(e -> e.getName().equals("consulteur"))){
            roleUser="consulteur";
        }
        else if(user.getRoles().stream().anyMatch(e -> e.getName().equals("Administration"))){
            roleUser="Administration";
        }
        else if(user.getRoles().stream().anyMatch(e -> e.getName().equals("Chef Projet"))) {
            roleUser = "Chef Projet";
        }
        else {
            roleUser="caissier";

        }

        return roleUser;
    }
    @Override
    public Optional <User> findUserByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }
    @Override
    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    @Override
    public boolean isRoleAutorize(long idUser){
    User user = finduserById(idUser);
    return  user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur"));
}
}