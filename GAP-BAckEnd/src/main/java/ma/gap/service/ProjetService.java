package ma.gap.service;

import ma.gap.entity.Article;
import ma.gap.entity.OrdreFabrication;
import ma.gap.entity.Projet;
import ma.gap.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Interface pour la gestion des projets et des articles associés.
 */
public interface ProjetService {

    public List<Projet> allProjet();

    public Page<Projet> searchProjets(String keyword, Pageable pageable);

    public List<Projet> getAllProjetsByStatus(int status);

    public Projet saveProjet(Projet projet);

    public Projet updateProjet(Projet projet, long id);

    public void deleteProjet(long id);

    public Optional<Projet> findById(long id);

    public List<Projet> getAffairesByAtelier(Long atelierId);

    public List<Projet> ProjetFiltred(String code, String affaire, String article, String atelier);

    public Page<OrdreFabrication> ofProjetFiltred(Pageable pageable, User user, long idprojet, long idof, long atelier,
            String libelle, String avancement) throws ParseException;

    public List<Projet> getAffairesByAtelierAndOfDeliverable(Long atelierId);

    public List<Projet> findAffairesByAtelierAndQteArticle_Sup_QteENPROD(long atelierId);
}
