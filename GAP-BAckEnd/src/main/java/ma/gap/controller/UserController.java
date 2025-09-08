package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.config.MyConstants;
import ma.gap.entity.Ateliers;
import ma.gap.entity.Role;
import ma.gap.entity.User;
import ma.gap.message.ResponseMessage;
import ma.gap.payload.request.SignupRequest;
import ma.gap.service.AtelierService;
import ma.gap.service.RoleService;
import ma.gap.service.UserImpService;
import ma.gap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private static final long EXPIRE_TOKEN_AFTER_MINUTES = 30;
    private UserService userService;
    private RoleService roleService;
    private AtelierService atelierService;
    public JavaMailSender emailSender;




    @Autowired
    private UserImpService userImpService;

    @GetMapping("/Liste")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public List<User> getAllUsers() {

        return userImpService.getAllUsers();
    }

    @PostMapping("/addUser")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public User AddUser(@RequestBody SignupRequest user){
        return userService.saveUser(user);
    }


    @DeleteMapping("/Supprimer/{id}")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public void deleteUser(@PathVariable("id") Long id){
        userService.deleteUser(id);

    }

    @PutMapping("/updateUser/{id}")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public User updateUser(@PathVariable("id") Long id,@RequestBody User user){

        return userService.updateUser(user,id);
    }
    @GetMapping("/userById/{id}")
    public User getUserById(@PathVariable long id){
        return userImpService.finduserById(id);
    }




    /*@PreAuthorize("hasAnyAuthority('admin')")*/
    @PostMapping("/addRoles/{id}")
    public void AddRolesUser(@PathVariable long id,@RequestBody String role){

        User user=userService.finduserById(id);
        Role roleUser=roleService.getRoleByName(role);
        List<Role> roles = user.getRoles();
        roles.add(roleUser);
        user.setRoles(roles);
        userService.updateUser(user,id);
    }

        //@PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping("/addAteliers/{id}")
    public void addAtelierUser(@PathVariable long id,@RequestBody String codeAtelier){
        User user=userService.finduserById(id);
        Ateliers atelierUser=atelierService.getAtelierByCode(codeAtelier);
        List<Ateliers> ateliers = user.getAteliers();
        ateliers.add(atelierUser);
        user.setAteliers(ateliers);
        userService.updateUser(user,id);
    }


    @GetMapping("/sessionUser/{id}")
    public ResponseEntity<ResponseMessage> updateSessionUser(@PathVariable long id){
        String message = "";
        try {
            userService.sessionUser(id);
            message = "La session est modifié avec succès: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Impossible de modifier cette session . Erreur: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }
    @GetMapping("/existeByUsername/{username}")
    public boolean existeByUsername(@PathVariable String username){
        System.out.println("xxxxxxx");
        return userService.findUsernameExiste(username);
    }


    @GetMapping("/existeByEmail/{email}")
    public boolean existeByEmail(@PathVariable String email){
        return userService.findEmailExiste(email);
    }

    /*    @PreAuthorize("hasAnyAuthority('admin')")*/
    @PutMapping("/changePassword/{id}")
    public ResponseEntity<ResponseMessage> updatePasswordUser(@PathVariable long id,@RequestBody String newPassword){
        String message = "";
        try {
            userService.updatePassword(id,newPassword);
            message = "La mot de passe est modifié avec succès: ";
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Impossible de modifier le mot de passe . Erreur: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }


    @PostMapping("/verificationPassword/{id}")
    public boolean passwordIsValid(@PathVariable long id,@RequestBody String password){
        return userService.isValidPassword(id,password);
    }


    @GetMapping("/forgetPassword/{email}")
    public String forgetPassword(@PathVariable String email, HttpServletRequest request) {

        Optional<User> user =userService.findUserByEmail(email);


        if (user.isPresent()) {
            User userr = user.get();
            userr.setDateToken(LocalDateTime.now());
            userr.setResetToken(UUID.randomUUID().toString());
            userService.updateUser(userr,userr.getId());
            String appUrl = request.getScheme() + "://" + request.getServerName()+":4200";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(MyConstants.MY_EMAIL);
            message.setTo(userr.getEmail());
            message.setSubject("Demande de réinitialisation du mot de passe");
            message.setText("Pour changer votre Mot de passe , cliquer sur ce Lien :\n" + appUrl
                    + "/#/ResetPassword?token=" + userr.getResetToken());

            this.emailSender.send(message);

            return "Nous avons envoyé lien sur votre adresse email pour changer le mot de passe";
        } else {

            return "Aucun utilisateur avec cette adresse email";


        }
    }

    @GetMapping("/resetPwd/{resetToken}/{password}")
    public String findUserByResetToken (@PathVariable String resetToken,@PathVariable String password) {

        Optional<User> user = userService.findUserByResetToken(resetToken);
        if (!user.isPresent()) {
            return "0";
        } else {
            User userr = user.get();
            LocalDateTime tokenCreationDate = userr.getDateToken();

            if (isTokenExpired(tokenCreationDate)) {
                return "1";
            }
            userService.updatePassword(userr.getId(),password.trim());
            userr.setResetToken(null);
            userr.setDateToken(null);
            userService.updateUser(userr,userr.getId());
            return "2";
        }
    }
    /**
     * Check whether the created token expired or not.
     *
     * @param tokenCreationDate
     * @return true or false
     */
    private boolean isTokenExpired(final LocalDateTime tokenCreationDate) {

        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(tokenCreationDate, now);

        return diff.toMinutes() >= EXPIRE_TOKEN_AFTER_MINUTES;
    }
}
