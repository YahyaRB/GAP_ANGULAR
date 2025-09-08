package ma.gap.repository;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Employee;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {

    List<Employee> findAllByAteliers(Ateliers ateliers, Sort nom);
    List<Employee> findAllByIdIn(List<Long> emplId);
    @Query(value = "SELECT matricule FROM employee", nativeQuery = true)
    public Iterable<String> listeMatricules();
    @Query(value = "SELECT count(*) FROM employee where matricule= :mat", nativeQuery = true)
    public String countMatricule(@Param("mat") String mat);
    boolean existsByNomAndPrenom(String nom, String prenom);
    boolean existsByMatricule(String matricule);

}
