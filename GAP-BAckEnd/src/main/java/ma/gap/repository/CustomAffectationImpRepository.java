package ma.gap.repository;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.service.UserImpService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;

@AllArgsConstructor
@Repository
public class CustomAffectationImpRepository implements CustomAffectationRepository {

    private ArticleRepository articleRepository;
    private AtelierRepository atelierRepository;
    private EmployeeRepository employeRepository;
    private ProjetRepository projetRepository;
    private EntityManager entityManager;
    UserImpService userImpService;

    public List<AffectationUpdate> affectationFiltred(long idUser, long idprojet, long idemploye, long idarticle,
            long idatelier, String dateDebut, String dateFin) throws ParseException {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AffectationUpdate> criteriaQuery = criteriaBuilder.createQuery(AffectationUpdate.class);
        Root<AffectationUpdate> affectationUpdateRoot = criteriaQuery.from(AffectationUpdate.class);
        List<Predicate> predicates = new ArrayList<>();

        User user = userImpService.findbyusername(idUser);
        List<Ateliers> listeAteliers = user.getAteliers();
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atel : listeAteliers) {
                    idatelier = atel.getId();
                }
            }
        }
        if (idprojet != 0) {
            Optional<Projet> projet = projetRepository.findById(idprojet);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("projets"), projet.get()));
        }
        if (idarticle != 0) {
            Optional<Article> article1 = articleRepository.findById(idarticle);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("article"), article1.get()));
        }
        if (idemploye != 0) {
            Optional<Employee> employee = employeRepository.findById(idemploye);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("employees"), employee.get()));
        }
        if (idatelier != 0) {
            Optional<Ateliers> ateliers = atelierRepository.findById(idatelier);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("ateliers"), ateliers.get()));
        }
        if (!dateDebut.equals("")) {
            Date datedebut = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(affectationUpdateRoot.get("date"), datedebut));
        }
        if (!dateFin.equals("")) {
            Date datefin = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(affectationUpdateRoot.get("date"), datefin));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public Page<AffectationUpdate> affectationFiltredPaginated(long idUser, long idprojet, long idemploye,
            long idarticle, long idatelier, String dateDebut, String dateFin, Pageable pageable) throws ParseException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AffectationUpdate> criteriaQuery = criteriaBuilder.createQuery(AffectationUpdate.class);
        Root<AffectationUpdate> affectationUpdateRoot = criteriaQuery.from(AffectationUpdate.class);
        List<Predicate> predicates = new ArrayList<>();

        User user = userImpService.findbyusername(idUser);
        List<Ateliers> listeAteliers = user.getAteliers();
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atel : listeAteliers) {
                    idatelier = atel.getId();
                }
            }
        }
        if (idprojet != 0) {
            Optional<Projet> projet = projetRepository.findById(idprojet);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("projets"), projet.get()));
        }
        if (idarticle != 0) {
            Optional<Article> article1 = articleRepository.findById(idarticle);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("article"), article1.get()));
        }
        if (idemploye != 0) {
            Optional<Employee> employee = employeRepository.findById(idemploye);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("employees"), employee.get()));
        }
        if (idatelier != 0) {
            Optional<Ateliers> ateliers = atelierRepository.findById(idatelier);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("ateliers"), ateliers.get()));
        }
        if (!dateDebut.equals("")) {
            Date datedebut = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(affectationUpdateRoot.get("date"), datedebut));
        }
        if (!dateFin.equals("")) {
            Date datefin = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(affectationUpdateRoot.get("date"), datefin));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        // Appliquer le tri
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    criteriaQuery.orderBy(criteriaBuilder.asc(affectationUpdateRoot.get(order.getProperty())));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.desc(affectationUpdateRoot.get(order.getProperty())));
                }
            });
        }

        // Créer la requête
        TypedQuery<AffectationUpdate> query = entityManager.createQuery(criteriaQuery);

        // Compter le total
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<AffectationUpdate> countRoot = countQuery.from(AffectationUpdate.class);
        countQuery.select(criteriaBuilder.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // Appliquer la pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AffectationUpdate> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
