package ma.gap.repository;

import ma.gap.entity.Deplacement;
import ma.gap.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeplacementRepository extends JpaRepository<Deplacement,Long> {

    List<Deplacement> findDistinctByEmployeeInOrderByIdDesc(List<Employee> emp);

}
