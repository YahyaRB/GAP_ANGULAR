package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.entity.Fonction;
import ma.gap.repository.FonctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FonctionImpService implements FonctionService{
    private final FonctionRepository fonctionRepository;

    public List<Fonction> findAllFonctions() {
        return fonctionRepository.findAllByOrderByIdDesc();
    }

    public Optional<Fonction> findFonctionById(long id) {
        return fonctionRepository.findById(id);
    }

    public Fonction saveFonction(Fonction fonction) {
        return fonctionRepository.save(fonction);
    }

    public Fonction updateFonction(Fonction fonction, long id) {
        fonction.setId(id);
        return fonctionRepository.save(fonction);
    }

    public void deleteFonction(long id) {
        fonctionRepository.deleteById(id);
    }
}