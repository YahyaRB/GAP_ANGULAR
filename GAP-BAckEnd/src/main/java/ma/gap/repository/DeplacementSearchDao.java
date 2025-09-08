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
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DeplacementSearchDao {
    private final EntityManager em;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private AtelierRepository atelierRepository;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    UserImpService userImpService;
    public List<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif, String dateDebut, String dateFin) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Deplacement> criteriaQuery = criteriaBuilder.createQuery(Deplacement.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Deplacement> root = criteriaQuery.from(Deplacement.class);

        User user = userImpService.findbyusername(idUser);

        // Gestion des rôles
        for (Role role : user.getRoles()) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atel : user.getAteliers()) {
                    idatelier = atel.getId(); // Assurez-vous que cela correspond à votre logique
                }
            }
        }

        // Ajoutez les prédicats
        if (idemploye != 0) {
            Predicate employePredicate = root.join("employee").in(employeeRepository.findById(idemploye).get());
            predicates.add(employePredicate);
        }

        if (idprojet != 0) {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"), projetRepository.findById(idprojet).get());
            predicates.add(projetPredicate);
        }

        if (idatelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.join("employee").get("ateliers"), atelierRepository.findById(idatelier).get());
            predicates.add(atelierPredicate);
        }

        if (!motif.isEmpty()) {
            Predicate motifPredicate = criteriaBuilder.equal(root.get("motif"), motif);
            predicates.add(motifPredicate);
        }

        if (!dateDebut.isEmpty() || !dateFin.isEmpty()) {
            Date date1;
            Date date2;

            if (dateDebut.isEmpty()) {
                dateDebut = dateFin;
            } else if (dateFin.isEmpty()) {
                dateFin = dateDebut;
            }

            date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            date2 = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);

            Predicate datePredicate = criteriaBuilder.between(root.get("date"), date1, date2);
            predicates.add(datePredicate);
        }

        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Deplacement> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }
}
