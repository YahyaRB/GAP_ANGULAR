package ma.gap.repository;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import ma.gap.entity.Article;
import ma.gap.entity.Ateliers;
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

    public List<Article> searchArticle(long idUser, String numPrix, String designation, long idProjet, long idAtelier,
            long idArticle) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Article> criteriaQuery = criteriaBuilder.createQuery(Article.class);
        Root<Article> root = criteriaQuery.from(Article.class);

        Predicate[] predicates = getPredicates(criteriaBuilder, root, idUser, numPrix, designation, idProjet, idAtelier,
                idArticle);

        criteriaQuery.where(predicates);
        TypedQuery<Article> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public org.springframework.data.domain.Page<Article> searchArticlePaginated(long idUser, String numPrix,
            String designation, long idProjet, long idAtelier, long idArticle,
            org.springframework.data.domain.Pageable pageable) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        // 1. Count Query
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Article> countRoot = countQuery.from(Article.class);

        Predicate[] countPredicates = getPredicates(criteriaBuilder, countRoot, idUser, numPrix, designation, idProjet,
                idAtelier, idArticle);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicates);

        Long totalRows = em.createQuery(countQuery).getSingleResult();

        // 2. Data Query
        CriteriaQuery<Article> criteriaQuery = criteriaBuilder.createQuery(Article.class);
        Root<Article> root = criteriaQuery.from(Article.class);

        Predicate[] predicates = getPredicates(criteriaBuilder, root, idUser, numPrix, designation, idProjet, idAtelier,
                idArticle);
        criteriaQuery.where(predicates);

        // Sorting
        if (pageable.getSort().isSorted()) {
            List<javax.persistence.criteria.Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(criteriaBuilder.asc(root.get(order.getProperty())));
                } else {
                    orders.add(criteriaBuilder.desc(root.get(order.getProperty())));
                }
            });
            criteriaQuery.orderBy(orders);
        } else {
            // Default sort by id desc
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));
        }

        TypedQuery<Article> query = em.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Article> result = query.getResultList();

        return new org.springframework.data.domain.PageImpl<>(result, pageable, totalRows);
    }

    private Predicate[] getPredicates(CriteriaBuilder criteriaBuilder, Root<Article> root, long idUser, String numPrix,
            String designation, long idProjet, long idAtelier, long idArticle) {
        List<Predicate> predicates = new ArrayList<>();
        User user = userImpService.finduserById(idUser);

        if (idAtelier == 0) {
            CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(root.get("ateliers").get("id"));
            if (user.getAteliers() != null && !user.getAteliers().isEmpty()) {
                for (Ateliers atelier : user.getAteliers()) {
                    inClause.value(atelier.getId());
                }
                predicates.add(inClause);
            }
        }

        if (numPrix != null && !numPrix.isEmpty()) {
            Predicate prenomPredicate = criteriaBuilder.like(root.get("numPrix"), numPrix);
            predicates.add(prenomPredicate);
        }
        if (designation != null && !designation.isEmpty()) {
            Predicate prenomPredicate = criteriaBuilder.like(root.get("designation"), designation);
            predicates.add(prenomPredicate);
        }

        if (idProjet != 0) {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"),
                    projetRepository.findById(idProjet).get());
            predicates.add(projetPredicate);
        }

        if (idAtelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("ateliers"),
                    atelierRepository.findById(idAtelier).get());
            predicates.add(atelierPredicate);
        }

        return predicates.toArray(new Predicate[0]);
    }

}
