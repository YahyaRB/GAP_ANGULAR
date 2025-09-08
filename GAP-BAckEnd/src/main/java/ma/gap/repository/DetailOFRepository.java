package ma.gap.repository;

import ma.gap.entity.DetailOF;
import ma.gap.entity.OrdreFabrication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailOFRepository extends JpaRepository<DetailOF, Long> {

    // Récupérer le plus grand compteur pour un OF donné
    @Query("SELECT COALESCE(MAX(d.compteur), 0) FROM DetailOF d WHERE d.ordreFabrication = :ordreFabrication")
    int findMaxCompteurByOrdreFabrication(@Param("ordreFabrication") OrdreFabrication ordreFabrication);
}
