package ma.gap.repository;

import ma.gap.entity.Article;
import ma.gap.entity.Projet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Repository
public class CustomProjetImpRepository implements CustomProjetRepository{

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Projet> projetList(String code,String affaire, String article, String atelier) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Projet> criteriaQuery = criteriaBuilder.createQuery(Projet.class);
        Root<Projet> projetRoot = criteriaQuery.from(Projet.class);
        List<Predicate> predicates = new ArrayList<>();

        if (!affaire.equals("all")){
            predicates.add(criteriaBuilder.equal(projetRoot.get("id"),Long.parseLong(affaire)));
        }
        if (!article.equals("all")){
            Optional<Article> article1 = articleRepository.findById(Long.parseLong(article));

            predicates.add(criteriaBuilder.equal(projetRoot.get("id"),article1.get().getProjet().getId()));
        }
        if (!code.equals("all")){
            predicates.add(criteriaBuilder.like(projetRoot.get("code"),code));
        }
       /* if (!atelier.equals("all")){

            List<Article> articlesatelier = articleRepository.findAllByAteliersOrderByIdDesc(Long.parseLong(atelier));

            predicates.add(criteriaBuilder.equal(projetRoot.get("id"),articlesatelier.get().getAteliers().getId()));
        }*/

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();

    }
}
