package ma.gap.repository;

import ma.gap.entity.Deplacement;
import ma.gap.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DeplacementRepository extends JpaRepository<Deplacement, Long> {

    List<Deplacement> findDistinctByEmployeeInOrderByIdDesc(List<Employee> emp);

    Page<Deplacement> findDistinctByEmployeeIn(List<Employee> emp, Pageable pageable);

}
