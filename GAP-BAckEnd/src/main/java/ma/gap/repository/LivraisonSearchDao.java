package ma.gap.repository;

import lombok.RequiredArgsConstructor;
import ma.gap.entity.Ateliers;
import ma.gap.entity.Livraisons;
import ma.gap.entity.Role;
import ma.gap.entity.User;
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
@Repository

@RequiredArgsConstructor
public class LivraisonSearchDao {
    private final EntityManager em;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private AtelierRepository atelierRepository;
    @Autowired ChauffeurRepository chauffeurRepository;
    @Autowired
    UserImpService userImpService;
    public List<Livraisons> searchLivraison(long idUser,long idchauffeur, long idprojet, long idatelier, String dateDebut,String dateFin) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Livraisons> criteriaQuery = criteriaBuilder.createQuery(Livraisons.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Livraisons> root = criteriaQuery.from(Livraisons.class);
        User user = userImpService.findbyusername(idUser);

        for (Role role : user.getRoles()) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atel : user.getAteliers()) {
                    idatelier = atel.getId();
                }
            }
        }


        if(idchauffeur!= 0)
        {
            Predicate chauffeurPredicate = criteriaBuilder.equal(root.get("chauffeur"),chauffeurRepository.findById(idchauffeur).get());
            predicates.add(chauffeurPredicate);
        }

        if(idprojet!=0)
        {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"),projetRepository.findById(idprojet).get());
            predicates.add(projetPredicate);
        }

        if(idatelier!=0){
            Predicate atelierPredicate=criteriaBuilder.equal(root.get("atelier"),atelierRepository.findById(idatelier).get());
            predicates.add(atelierPredicate);
        }

        if(dateDebut!= "" || dateFin!= "")
        {
            Predicate datePredicate;
            Date date1;
            Date date2;
            if(dateDebut== "")
              dateDebut=dateFin;
            else if(dateFin== "")
              dateFin=dateDebut;

            date1=new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            date2=new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
            datePredicate = criteriaBuilder.between(root.get("dateLivraison"), date1, date2);
            predicates.add(datePredicate);
        }



        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Livraisons> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }
    public List<Livraisons> searchLivraisonAffectation( long idprojet,long idatelier, String affectation, String dateDebut,String dateFin) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Livraisons> criteriaQuery = criteriaBuilder.createQuery(Livraisons.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Livraisons> root = criteriaQuery.from(Livraisons.class);


        if(affectation.equals("non")) {
            Predicate chauffeurPredicate = criteriaBuilder.isNull(root.get("chauffeur"));
            predicates.add(chauffeurPredicate);
        }else if (affectation.equals("oui")){
            Predicate chauffeurPredicate = criteriaBuilder.isNotNull(root.get("chauffeur"));
            predicates.add(chauffeurPredicate);
        }

        if(idprojet!=0)
        {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("projet"),projetRepository.findById(idprojet).get());
            predicates.add(projetPredicate);
        }
        if(idatelier!=0)
        {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("atelier"),atelierRepository.findById(idatelier).get());
            predicates.add(atelierPredicate);
        }


        if(dateDebut!= "" || dateFin!= "")
        {
            Predicate datePredicate;
            Date date1;
            Date date2;
            if(dateDebut== "")
                dateDebut=dateFin;
            else if(dateFin== "")
                dateFin=dateDebut;

            date1=new SimpleDateFormat("yyyy-MM-dd").parse(dateDebut);
            date2=new SimpleDateFormat("yyyy-MM-dd").parse(dateFin);
            datePredicate = criteriaBuilder.between(root.get("dateLivraison"), date1, date2);
            predicates.add(datePredicate);
        }



        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Livraisons> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

}
