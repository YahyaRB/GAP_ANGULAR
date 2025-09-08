package ma.gap.repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import ma.gap.entity.*;
import ma.gap.service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ma.gap.enums.StatutPlan;
@Repository
@RequiredArgsConstructor
public class PlanSearchDoa {
	private final EntityManager em ;
    @Autowired
    private ProjetRepository projetRepository;
    @Autowired
    private AtelierRepository atelierRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    UserImpService userImpService;
    
    
    public List<Plan> searchPlan(long idUser,long idProjet, long idAtelier, long idArticle, String statutPlan, long idPlan) throws ParseException {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Plan> criteriaQuery = criteriaBuilder.createQuery(Plan.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Plan> root = criteriaQuery.from(Plan.class);

        User user = userImpService.findbyusername(idUser);
        if(idAtelier == 0) 
        {
            Predicate atelierPredicate = criteriaBuilder.in(root.get("atelier")).value(user.getAteliers());
            predicates.add(atelierPredicate);
        }

        if (idProjet != 0) {
            Predicate projetPredicate = criteriaBuilder.equal(root.get("affaire"), projetRepository.findById(idProjet).get());
            predicates.add(projetPredicate);
        }

        if (idAtelier != 0) {
            Predicate atelierPredicate = criteriaBuilder.equal(root.get("atelier"), atelierRepository.findById(idAtelier).get());
            predicates.add(atelierPredicate);
        }

        if (idArticle != 0) {
            Predicate articlePredicate = criteriaBuilder.equal(root.get("article"), articleRepository.findById(idArticle).get());
            predicates.add(articlePredicate);
        }

        if (!statutPlan.isEmpty()) {
            Predicate statutPredicate = criteriaBuilder.equal(root.get("statut"), StatutPlan.valueOf(statutPlan));
            predicates.add(statutPredicate);
        }

        if (idPlan != 0) {
            Predicate planIdPredicate = criteriaBuilder.equal(root.get("id"), idPlan);
            predicates.add(planIdPredicate);
        }

        Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.where(queryPredicate);
        TypedQuery<Plan> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

	
	
	 public List<Plan> searchPlanByProjet(User user, long idprojet,  long atelier) throws ParseException {

	        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
	        CriteriaQuery<Plan> criteriaQuery = criteriaBuilder.createQuery(Plan.class);
	        List<Predicate> predicates = new ArrayList<>();
	        Root<Plan> root = criteriaQuery.from(Plan.class);




	        for (Role role : user.getRoles()) {
	            if (role.getName().equals("agentSaisie")) {
	                for (Ateliers atel : user.getAteliers()) {
	                    atelier = atel.getId();
	                }
	            }
	        }

	        if (idprojet != 0) {
	            Projet projet = projetRepository.findById(idprojet).get();

	         

	            if (atelier != 0) {
	                    Predicate atelierPredicate = criteriaBuilder.and(criteriaBuilder.equal(root.get("atelier"), atelierRepository.findById(atelier).get()), (criteriaBuilder.equal(root.get("projet"), projet)));
	                    predicates.add(atelierPredicate);

	            }
	   
	            
	        }
	            Predicate queryPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[0]));
	            criteriaQuery.where(queryPredicate);
	            TypedQuery<Plan> query = em.createQuery(criteriaQuery);
	            return query.getResultList();
	        }
	
}
