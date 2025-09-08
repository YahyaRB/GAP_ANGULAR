package ma.gap.service;


import ma.gap.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    public List<Role> getAllRoles();
    public Optional<Role> getRoleById(int id);
    public Role getRoleByName(String name);

}
