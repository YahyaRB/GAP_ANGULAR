package ma.gap.repository;

import ma.gap.entity.MotifDeplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotifDepRepository extends JpaRepository<MotifDeplacement,Long> {


}
