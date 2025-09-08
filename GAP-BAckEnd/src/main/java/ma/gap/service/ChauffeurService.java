package ma.gap.service;

import ma.gap.entity.Chauffeur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;


public interface ChauffeurService {
    public List<Chauffeur> allChauffeurs();
    public Optional<Chauffeur> chauffeurById(long id);
    public Chauffeur saveChauffeur(Chauffeur chauffeur);
    public Chauffeur updateChauffeur(Chauffeur chauffeur,long id);
    public void deleteChauffeur(long id);
    boolean existeByNomComplet(String nom, String prenom);
    boolean existeByMatricule(int matricule);
     boolean hasDeliveries(Long chauffeurId) ;

}
