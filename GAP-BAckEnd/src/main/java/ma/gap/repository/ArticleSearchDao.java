package ma.gap.repository;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import ma.gap.entity.Article;
import ma.gap.entity.User;
import ma.gap.service.UserImpService;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@AllArgsConstructor
public class ArticleSearchDao {
    private final EntityManager em;
    private ArticleRepository articleRepository;
    private UserImpService userImpService;
    private ProjetRepository projetRepository;
    private AtelierRepository atelierRepository;

    public List<Article> searchArticle(long idUser, String numPrix,String designation, long idProjet, long idAtelier, long idArticle) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Article> criteriaQuery = criteriaBuilder.createQuery(Article.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Article> root = criteriaQuery.from(Article.class);

        User user = userImpService.finduserById(idUser);

        if(idAtelier == 0)
        {

            Predicate atelierPredicate = criteriaBuilder.in(root.get("ateliers")).value(user.getAteliers());
            predicates.add(atelierPredicate);
        }

        if (!numPrix.isEmpty()) {

            Predicate prenomPredicate = criteriaBuilder.like(root.get("numPrix"),numPrix);
            predicates.add(prenomPredicate);
        }
        if (!designation.isEmpty()) {

            Predicate prenomPredicate = criteriaBuilder.like(root.get("designation"),designation);
            predicates.add(prenomPredicate);
        }

        if (idProjet != 0) {

            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"), projetRepository.findById(idProjet).get());
            predicates.add(projetPredicate);
        }

        if (idAtelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("ateliers"), atelierRepository.findById(idAtelier).get());
            predicates.add(atelierPredicate);
        }

        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Article> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

}
