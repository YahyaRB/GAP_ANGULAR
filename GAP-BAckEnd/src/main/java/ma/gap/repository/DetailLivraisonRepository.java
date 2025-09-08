package ma.gap.repository;

import ma.gap.entity.*;
import ma.gap.dtos.OfProjectQteRestDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface DetailLivraisonRepository extends JpaRepository<DetailLivraison,Long> {
    List<DetailLivraison> findAllBylivraisonOrderByIdDesc(Livraisons livraisons);
    //List<DetailLivraison> findAllByOrdreFabrication(List<OrdreFabrication> listeordreFabrications);
    //@Query(value = "select o.id as id ,(o.quantite - sum(d.quantite)) as QteRest ,o.creer_le as creerLe,o.creer_par as creerPar,a.designation as designation,o.compteur as compteur from detail_livraison d,ordre_fabrication o,article a where a.id=o.article_id and o.projet_id=:idProjet and o.id=d.idof group by o.quantite,o.id,o.creer_le,o.creer_par,o.description,o.projet_id,o.id, o.creer_le, o.creer_par, a.designation, o.compteur", nativeQuery = true)
   /* @Query(value = "select o.id as id,o.avancement,o.quantite - (select COALESCE(sum(d.quantite), 0) from detail_livraison d,ordre_fabrication orf where d.idof=orf.id and d.idof=o.id) as QteRest,o.creer_le as creerLe,o.creer_par as creerPar,a.designation as designation,o.compteur as compteur from detail_livraison d,ordre_fabrication o,article a where o.projet_id=:idProjet and a.id=o.article_id group by o.quantite,o.id,o.description,o.creer_le, o.creer_par, a.designation, o.compteur,o.avancement", nativeQuery = true)*/
    @Query(value = "SELECT o.id as id, o.numof as numOF, a.designation as designation, o.qte_rest as qteRest FROM ordre_fabrication o INNER JOIN article a ON a.id = o.article_id WHERE o.projet_id = :idProjet and o.qte_rest>0 and o.qte_rest<100", nativeQuery = true)
    List<OfProjectQteRestDto> listeOfavecQteRestante(@Param("idProjet") Long idProjet);
    @Query(value = "select o.id as id ,(o.quantite-sum(d.quantite)) as QteRest from detail_livraison d,ordre_fabrication o where o.id=:idof and o.id=d.idof group by o.quantite,o.id", nativeQuery = true)
    OfProjectQteRestDto listeOfQteByIdOf(@Param("idof") long idof);



}
