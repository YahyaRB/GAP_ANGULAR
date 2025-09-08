package ma.gap.repository;

import ma.gap.entity.Ateliers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtelierRepository extends JpaRepository<Ateliers,Long> {
   Ateliers findByCode(String code);
}
