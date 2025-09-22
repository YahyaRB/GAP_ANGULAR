package ma.gap.service;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Employee;
import ma.gap.entity.Role;
import ma.gap.entity.User;
import ma.gap.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeImpService implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserImpService userImpService;

    @Override
    public List<Employee> allEmployee(long idUser) {

        User user = userImpService.findbyusername(idUser);
        List<Role> roles = user.getRoles();

        for (Role role : roles){
            if (role.getName().equals("agentSaisie")){
                List<Employee> employeeList = employeeRepository.findAllByAteliers(user.getAteliers().get(0), Sort.by(Sort.Direction.ASC,"nom"));
                return employeeList;
            } else {
                return employeeRepository.findAll(Sort.by(Sort.Direction.ASC,"id"));
            }
        }

        return null;
    }

    @Override
    public List<Employee> findAllByAteliers(Ateliers ateliers) {
        return employeeRepository.findAllByAteliers(ateliers,Sort.by(Sort.Direction.ASC,"id"));
    }

    @Override
    public Employee saveEmploye(Employee employee) {
        System.out.println(employee.getAteliers());
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmploye(Employee employee, long id) {

        employee.setId(id);
        employee.setMatricule(String.valueOf(employee.getMatricule()));

        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmploye(long id) {
        employeeRepository.deleteById(id);

    }

    @Override
    public boolean existeByNomComplet(String nom, String prenom) {
        return employeeRepository.existsByNomAndPrenom(nom, prenom);
    }
    @Override
    public boolean existeByMatricule(String matricule){
        return employeeRepository.existsByMatricule(matricule);
    }

    @Override
    public Employee findById(Long id) {

        Optional<Employee> employee = employeeRepository.findById(id);

        return employee.get();
    }

}