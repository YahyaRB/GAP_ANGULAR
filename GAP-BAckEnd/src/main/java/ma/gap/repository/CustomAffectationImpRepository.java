package ma.gap.repository;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.service.UserImpService;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
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
public class CustomAffectationImpRepository implements CustomAffectationRepository{

    private ArticleRepository articleRepository;
    private AtelierRepository atelierRepository;
    private EmployeeRepository employeRepository;
    private ProjetRepository projetRepository;
    private EntityManager entityManager;
    UserImpService userImpService;
    public List<AffectationUpdate> affectationFiltred(long idUser,long idprojet ,long idemploye, long idarticle,long idatelier, String dateDebut, String dateFin) throws ParseException {

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
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("projets"),projet.get()));
        }
        if (idarticle != 0){
            Optional<Article> article1 = articleRepository.findById(idarticle);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("article"),article1.get()));
        }
        if (idemploye != 0){
            Optional<Employee> employee = employeRepository.findById(idemploye);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("employees"),employee.get()));
        }
        if (idatelier != 0){
            Optional<Ateliers> ateliers = atelierRepository.findById(idatelier);
            predicates.add(criteriaBuilder.equal(affectationUpdateRoot.get("ateliers"),ateliers.get()));
        }
        if (!dateDebut.equals("")){
            Date datedebut = new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(affectationUpdateRoot.get("date"),datedebut));
        }
        if (!dateFin.equals("")){
            Date datefin = new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(affectationUpdateRoot.get("date"),datefin));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
