package ma.gap.service;


import ma.gap.entity.Role;
import ma.gap.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class RoleImpService implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC,"name"));
    }



    @Override
    public Optional<Role> getRoleById(int id) {
        return roleRepository.findById( id);
    }





    @Override
    public Role getRoleByName(String name) {

        return roleRepository.findByName(name);
    }
}
