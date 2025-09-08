package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.Role;
import ma.gap.service.RoleService;
import ma.gap.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@AllArgsConstructor
@RestController
@RequestMapping("/api/role")
public class RoleController {
    private RoleService roleService;
    private final UserService userService;


    @GetMapping("/ListeRoles")
    public List<Role> getAllRoles(){
        System.out.println(userService.getCurrentUsername());
        return roleService.getAllRoles();
    }

    @GetMapping("/roleById/{id}")
    /*  @PreAuthorize("hasAnyAuthority('admin')")*/
    public Optional<Role> getRoleById(@PathVariable int id) {
        return roleService.getRoleById(id);
    }


    @GetMapping("/roleByUserName/{name}")
    public Role getRoleByUserName(@PathVariable String name){
        return roleService.getRoleByName(name);
    }
}

