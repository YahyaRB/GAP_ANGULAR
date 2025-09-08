package ma.gap.service;

import ma.gap.entity.Historique;
import ma.gap.repository.HistoriqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoriqueImpService implements HistoriqueService{

    @Autowired
    private HistoriqueRepository HistoriqueRepository;


    @Override
    public Historique createHistorique(Historique historique) {
        return HistoriqueRepository.save(historique);
    }

    @Override
    public List<Historique> getAllHistorique() {
        return HistoriqueRepository.findAll();
    }

    @Override
    public Optional<Historique> findHistoriqueById(long id) {
        return  HistoriqueRepository.findById(id);
    }

    @Override
    public Historique saveHistorique(Historique Historique) {
        return HistoriqueRepository.save(Historique);
    }

    @Override
    public void deleteHistorique(long id) {
        HistoriqueRepository.deleteById(id);
    }

    @Override
    public Historique updateHistorique(Historique Historique, long id) {
        Optional<Historique> optionalHistorique = HistoriqueRepository.findById(id);
        if (optionalHistorique.isPresent()) {
            Historique existingHistorique = optionalHistorique.get();
            existingHistorique.setIndice(Historique.getIndice());
            existingHistorique.setDescription(Historique.getDescription());
            existingHistorique.setDate(Historique.getDate());
            existingHistorique.setFaitPar(Historique.getFaitPar());
            existingHistorique.setValidePar(Historique.getValidePar());
            existingHistorique.setNumeroPlan(Historique.getNumeroPlan());
            return HistoriqueRepository.save(existingHistorique);
        }
        else {
            return null;
        }
    }
    
    
    @Autowired
    private HistoriqueRepository historiqueRepository;

    public List<Historique> getHistoriqueByPlanId(Long planId) {
        return historiqueRepository.findByNumeroPlanIdOrderByDate(planId);
    }
    
    
    
}
