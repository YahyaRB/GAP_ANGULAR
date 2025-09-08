package ma.gap.repository;

import lombok.RequiredArgsConstructor;
import ma.gap.entity.Chauffeur;
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
@Repository
@RequiredArgsConstructor
public class ChauffeurSearchDao {
    private final EntityManager em;

    public List<Chauffeur> searchChauffeur(String nom, String prenom,String matricule) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Chauffeur> criteriaQuery = criteriaBuilder.createQuery(Chauffeur.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Chauffeur> root = criteriaQuery.from(Chauffeur.class);


        if(nom!= "")
        {
            Predicate nomPredicate = criteriaBuilder.like(root.get("nom"),nom);
            predicates.add(nomPredicate);
        }
        if(prenom!="")
        {
            Predicate prenomPredicate = criteriaBuilder.like(root.get("prenom"),prenom);
            predicates.add(prenomPredicate);
        }
        if(matricule!="")
        { int a=Integer.parseInt(matricule);
            System.out.println(a);
            Predicate matriculePredicate = criteriaBuilder.equal(root.get("matricule"),Integer.parseInt(matricule));
            predicates.add(matriculePredicate);
        }



        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Chauffeur> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

}
