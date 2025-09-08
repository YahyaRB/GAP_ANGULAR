package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.entity.Chauffeur;
import ma.gap.repository.ChauffeurRepository;
import ma.gap.repository.LivraisonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class ChauffeurImpService implements ChauffeurService {

    private ChauffeurRepository chauffeurRepository;
    private LivraisonsRepository livraisonsRepository;
    @Override
    public List<Chauffeur> allChauffeurs() {
        return chauffeurRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
    }

    @Override
    public Optional<Chauffeur> chauffeurById(long id) {
        return chauffeurRepository.findById(id);
    }

    @Override
    public Chauffeur saveChauffeur(Chauffeur chauffeur) {
        return chauffeurRepository.save(chauffeur);
    }

    @Override
    public Chauffeur updateChauffeur(Chauffeur chauffeur, long id) {
        chauffeur.setId(id);
        return chauffeurRepository.save(chauffeur);
    }

    @Override
    public void deleteChauffeur(long id) {
        chauffeurRepository.deleteById(id);

    }
@Override
public boolean existeByNomComplet(String nom, String prenom) {
    return chauffeurRepository.existsByNomAndPrenom(nom, prenom);
}
@Override
    public boolean existeByMatricule(int matricule){
        return chauffeurRepository.existsByMatricule(matricule);
}
    public boolean hasDeliveries(Long chauffeurId) {
        return livraisonsRepository.existsByChauffeurId(chauffeurId);
    }

}
