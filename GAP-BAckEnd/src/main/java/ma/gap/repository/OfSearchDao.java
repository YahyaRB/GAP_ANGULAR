package ma.gap.repository;

import lombok.RequiredArgsConstructor;
import ma.gap.entity.*;
import ma.gap.service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OfSearchDao {
    private final EntityManager em;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private AtelierRepository atelierRepository;
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PlanRepository planRepository;
    @Autowired
    UserImpService userImpService;

    public List<OrdreFabrication> searchOF(long idUser, String idOf, long idProjet, long idAtelier, long idArticle,
            String dateDebut, String dateFin) throws ParseException {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<OrdreFabrication> criteriaQuery = criteriaBuilder.createQuery(OrdreFabrication.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<OrdreFabrication> root = criteriaQuery.from(OrdreFabrication.class);

        User user = userImpService.findbyusername(idUser);
        if (idAtelier == 0) {
            Predicate atelierPredicate = criteriaBuilder.in(root.get("atelier")).value(user.getAteliers());
            predicates.add(atelierPredicate);
        }

        if (idOf != null && !idOf.isEmpty()) {
            Predicate prenomPredicate = criteriaBuilder.equal(root.get("numOF"), idOf);
            predicates.add(prenomPredicate);
        }

        if (idProjet != 0) {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"),
                    projetRepository.findById(idProjet).get());
            predicates.add(projetPredicate);
        }

        if (idAtelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("atelier"),
                    atelierRepository.findById(idAtelier).get());
            predicates.add(atelierPredicate);
        }
        if (idArticle != 0) {
            Predicate articlePredicate = criteriaBuilder.equal(root.get("article"),
                    articleRepository.findById(idArticle).get());
            predicates.add(articlePredicate);
        }
        if (dateDebut != null && !dateDebut.trim().isEmpty() && dateFin != null && !dateFin.trim().isEmpty()) {
            try {
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut.trim());
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin.trim());
                Predicate datePredicate = criteriaBuilder.between(root.get("date"), date1, date2);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing des dates: " + e.getMessage());
            }
        } else if (dateDebut != null && !dateDebut.trim().isEmpty()) {
            try {
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut.trim());
                Predicate datePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("date"), date1);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing de dateDebut: " + e.getMessage());
            }
        } else if (dateFin != null && !dateFin.trim().isEmpty()) {
            try {
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin.trim());
                Predicate datePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("date"), date2);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing de dateFin: " + e.getMessage());
            }
        }

        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);

        // Default sort by id desc
        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));

        TypedQuery<OrdreFabrication> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public org.springframework.data.domain.Page<OrdreFabrication> searchOFPaginated(long idUser, String idOf,
            long idProjet, long idAtelier, long idArticle, String dateDebut, String dateFin,
            org.springframework.data.domain.Pageable pageable) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        // 1. Count Query
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<OrdreFabrication> countRoot = countQuery.from(OrdreFabrication.class);

        Predicate[] countPredicates = getPredicates(criteriaBuilder, countRoot, idUser, idOf, idProjet, idAtelier,
                idArticle, dateDebut, dateFin);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicates);

        Long totalRows = em.createQuery(countQuery).getSingleResult();

        // 2. Data Query
        CriteriaQuery<OrdreFabrication> criteriaQuery = criteriaBuilder.createQuery(OrdreFabrication.class);
        Root<OrdreFabrication> root = criteriaQuery.from(OrdreFabrication.class);

        Predicate[] predicates = getPredicates(criteriaBuilder, root, idUser, idOf, idProjet, idAtelier, idArticle,
                dateDebut, dateFin);
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

        TypedQuery<OrdreFabrication> query = em.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<OrdreFabrication> result = query.getResultList();

        return new org.springframework.data.domain.PageImpl<>(result, pageable, totalRows);
    }

    private Predicate[] getPredicates(CriteriaBuilder criteriaBuilder, Root<OrdreFabrication> root, long idUser,
            String idOf, long idProjet, long idAtelier, long idArticle, String dateDebut, String dateFin) {
        List<Predicate> predicates = new ArrayList<>();
        User user = userImpService.findbyusername(idUser);

        if (idAtelier == 0) {
            CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(root.get("atelier").get("id"));
            if (user.getAteliers() != null && !user.getAteliers().isEmpty()) {
                for (Ateliers atelier : user.getAteliers()) {
                    inClause.value(atelier.getId());
                }
                predicates.add(inClause);
            }
        }

        if (idOf != null && !idOf.isEmpty()) {
            Predicate prenomPredicate = criteriaBuilder.equal(root.get("numOF"), idOf);
            predicates.add(prenomPredicate);
        }

        if (idProjet != 0) {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"),
                    projetRepository.findById(idProjet).get());
            predicates.add(projetPredicate);
        }

        if (idAtelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("atelier"),
                    atelierRepository.findById(idAtelier).get());
            predicates.add(atelierPredicate);
        }
        if (idArticle != 0) {
            Predicate articlePredicate = criteriaBuilder.equal(root.get("article"),
                    articleRepository.findById(idArticle).get());
            predicates.add(articlePredicate);
        }
        if (dateDebut != null && !dateDebut.trim().isEmpty() && dateFin != null && !dateFin.trim().isEmpty()) {
            try {
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut.trim());
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin.trim());
                Predicate datePredicate = criteriaBuilder.between(root.get("date"), date1, date2);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing des dates: " + e.getMessage());
            }
        } else if (dateDebut != null && !dateDebut.trim().isEmpty()) {
            try {
                Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut.trim());
                Predicate datePredicate = criteriaBuilder.greaterThanOrEqualTo(root.get("date"), date1);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing de dateDebut: " + e.getMessage());
            }
        } else if (dateFin != null && !dateFin.trim().isEmpty()) {
            try {
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin.trim());
                Predicate datePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("date"), date2);
                predicates.add(datePredicate);
            } catch (ParseException e) {
                System.err.println("Erreur de parsing de dateFin: " + e.getMessage());
            }
        }

        return predicates.toArray(new Predicate[0]);
    }

    public List<OrdreFabrication> searchOFByProjet(User user, long idprojet, long idof, long atelier, String libelle,
            String avancement) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<OrdreFabrication> criteriaQuery = criteriaBuilder.createQuery(OrdreFabrication.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<OrdreFabrication> root = criteriaQuery.from(OrdreFabrication.class);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atel : user.getAteliers()) {
                    atelier = atel.getId();
                }
            }
        }

        if (idprojet != 0) {
            Projet projet = projetRepository.findById(idprojet).get();

            if (idof != 0) {
                Predicate ofPredicate = criteriaBuilder.and(criteriaBuilder.equal(root.get("id"), idof),
                        (criteriaBuilder.equal(root.get("projet"), projet)));
                predicates.add(ofPredicate);

            }

            if (atelier != 0) {
                Predicate atelierPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("atelier"), atelierRepository.findById(atelier).get()),
                        (criteriaBuilder.equal(root.get("projet"), projet)));
                predicates.add(atelierPredicate);

            }
            if (!libelle.equals("")) {
                Predicate libellePredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("description"), libelle),
                        (criteriaBuilder.equal(root.get("projet"), projet)));
                predicates.add(libellePredicate);

            }
            if (!avancement.equals("")) {
                Date date = new Date();
                Predicate avancementPredicate;

                if (avancement.equals("Termine")) {
                    avancementPredicate = criteriaBuilder.and((criteriaBuilder.equal(root.get("projet"), projet)),
                            (criteriaBuilder.equal(root.get("avancement"), 100)));
                    predicates.add(avancementPredicate);
                } else if (avancement.equals("EnCours")) {
                    avancementPredicate = criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("dateFin"), date),
                            criteriaBuilder.lessThan(root.get("avancement"), 100),
                            (criteriaBuilder.equal(root.get("projet"), projet)));
                    predicates.add(avancementPredicate);
                } else if (avancement.equals("EnRetard")) {
                    avancementPredicate = criteriaBuilder.and(criteriaBuilder.lessThan(root.get("dateFin"), date),
                            criteriaBuilder.lessThan(root.get("avancement"), 100),
                            (criteriaBuilder.equal(root.get("projet"), projet)));
                    predicates.add(avancementPredicate);
                } else {
                    avancementPredicate = criteriaBuilder.equal(root.get("projet"), projet);
                    predicates.add(avancementPredicate);
                }
            }
        }
        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<OrdreFabrication> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

}
