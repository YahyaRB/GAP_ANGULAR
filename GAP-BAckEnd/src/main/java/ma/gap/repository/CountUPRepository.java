package ma.gap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.gap.entity.CountUP;

@Repository
public interface CountUPRepository extends JpaRepository<CountUP,Long> {

    CountUP findByAnneeAndAtelier(int annee,int atelier);
}
