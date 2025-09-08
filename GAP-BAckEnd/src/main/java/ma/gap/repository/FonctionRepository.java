package ma.gap.repository;

import ma.gap.entity.Fonction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FonctionRepository extends JpaRepository<Fonction,Long> {
    List<Fonction> findAllByOrderByIdDesc();
}
