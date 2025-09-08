package ma.gap.service;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    public List<Employee> allEmployee(long idUser);
    public List<Employee> findAllByAteliers(Ateliers ateliers);
    public Employee findById(Long id);
    public Employee saveEmploye(Employee employee);

    public Employee updateEmploye(Employee employee, long id);
    public void deleteEmploye(long id) ;
    boolean existeByNomComplet(String nom, String prenom);
    boolean existeByMatricule(String matricule);
}
