package ma.gap.repository;

import lombok.RequiredArgsConstructor;
import ma.gap.entity.Ateliers;
import ma.gap.entity.Employee;
import ma.gap.entity.Role;
import ma.gap.entity.User;
import ma.gap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class PersonelSearchDao {
    private final EntityManager em;
    @Autowired
   private FonctionRepository fonctionRepository;
    @Autowired
   private AtelierRepository atelierRepository;
    @Autowired
    private UserService userImpService;

    public List<Employee> searchEmploye(long idUser, String matricule, String nom, String prenom, long atelier, long fonction) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> root = criteriaQuery.from(Employee.class);
        List<Predicate> predicates = new ArrayList<>();

        // Récupération de l'utilisateur
        User user = userImpService.findbyusername(idUser);

        // Gestion spécifique pour le rôle "agentSaisie"
        if (user != null) {
            for (Role role : user.getRoles()) {
                if ("agentSaisie".equals(role.getName())) {
                    for (Ateliers atel : user.getAteliers()) {
                        atelier = atel.getId();
                    }
                }
            }
        }

        // Ajout des conditions de recherche
        if (nom != null && !nom.trim().isEmpty()) {
            Predicate nomPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + nom.toLowerCase() + "%");
            predicates.add(nomPredicate);
        }

        if (prenom != null && !prenom.trim().isEmpty()) {
            Predicate prenomPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), "%" + prenom.toLowerCase() + "%");
            predicates.add(prenomPredicate);
        }

        if (matricule != null && !matricule.trim().isEmpty()) {
            try {
                Predicate matriculePredicate = criteriaBuilder.equal(root.get("matricule"), Integer.parseInt(matricule));
                predicates.add(matriculePredicate);
            } catch (NumberFormatException e) {
                System.out.println("Invalid matricule format: " + matricule);
            }
        }

        if (atelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("ateliers").get("id"), atelier);
            predicates.add(atelierPredicate);
        }

        if (fonction != 0) {
            Predicate fonctionPredicate = criteriaBuilder.equal(root.get("fonction").get("id"), fonction);
            predicates.add(fonctionPredicate);
        }

        // Construction de la requête finale
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        TypedQuery<Employee> query = em.createQuery(criteriaQuery);

        return query.getResultList();
    }

}

