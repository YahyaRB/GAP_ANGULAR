package ma.gap.repository;

import ma.gap.entity.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChauffeurRepository extends JpaRepository<Chauffeur,Long> {
    @Query(value = "SELECT matricule FROM chauffeur", nativeQuery = true)
    public Iterable<String> listeMatricules();
    @Query(value = "SELECT count(*) FROM chauffeur where matricule= :mat", nativeQuery = true)
    public Integer countMatricule(@Param("mat") String mat);
    boolean existsByNomAndPrenom(String nom, String prenom);
    boolean existsByMatricule(int matricule);
}
