package ma.gap.repository;

import java.util.List;

import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.entity.Ateliers;
import org.springframework.data.jpa.repository.JpaRepository;
import ma.gap.entity.OrdreFabrication;
import ma.gap.entity.Plan;
import ma.gap.entity.Projet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdreFabricationRepository extends JpaRepository<OrdreFabrication, Long> {
	List<OrdreFabrication> findAllByProjet(Projet projet);

	List<OrdreFabrication> findAllByPlan(Plan plan);

	List<OrdreFabrication> findAllByAtelierOrderByIdDesc(Ateliers ateliers);

	List<OrdreFabrication> findAllByAtelierInOrderByIdDesc(List<Ateliers> ateliers);

	Page<OrdreFabrication> findAllByAtelierInOrderByIdDesc(List<Ateliers> ateliers, Pageable pageable);

	List<OrdreFabrication> findAllByProjetAndAtelierOrderByIdAsc(Projet projet, Ateliers atelier);

	/*
	 * @Query(value =
	 * "SELECT id,avancement,quantite FROM ordre_fabrication where avancement<100 and projet_id= :id"
	 * , nativeQuery = true)
	 * public List<String> avancementProjet(@Param("id") long id);
	 */
	@Query(value = "select o.id as id ,(o.quantite - sum(d.quantite)) as QteRest from detail_livraison d,ordre_fabrication o where o.projet_id=:id and o.avancement<100 and o.id=d.idof group by o.quantite,o.id", nativeQuery = true)
	public List<String> avancementProjet(@Param("id") long id);

	@Query(value = "select o.* from ordre_fabrication o, projet p where p.id=o.projet_id and o.atelier_id=:idAtelier and o.projet_id=:idProjet and p.status=2 and o.qte_rest>0", nativeQuery = true)
	public List<OrdreFabrication> findOFByAtelierAndProjet(@Param("idAtelier") long idAtelier,
			@Param("idProjet") long idProjet);

	@Query("SELECT o.id as id, o.numOF as numOF, o.article.designation as designation, o.qteRest as qteRest " +
			"FROM OrdreFabrication o WHERE o.projet.id = :projetId AND o.qteRest > 0")
	List<OfProjectQteRestDto> findOfProjectQteRestByProjetId(@Param("projetId") Long projetId);

	/*
	 * @Query(value =
	 * "SELECT * FROM ordre_fabrication where avancement<100 and projet_id= :idProjet and atelier_id= :idAtelier"
	 * , nativeQuery = true)
	 * public List<OrdreFabrication> findAllAvancementaActif(@Param("idProjet") long
	 * idProjet,@Param("idAtelier") long idAtelier);
	 */
}