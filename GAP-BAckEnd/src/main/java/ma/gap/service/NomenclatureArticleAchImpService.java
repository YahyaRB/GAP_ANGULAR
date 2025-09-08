package ma.gap.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;
import ma.gap.repository.NomenclatureArticleAchRepository;

@Service
public class NomenclatureArticleAchImpService implements NomenclatureArticleAchService {

    @Autowired
    private NomenclatureArticleAchRepository nomenclatureArticleAchRepository;

    @Override
    public List<NomenclatureArticleAch> getAllNomenclatureArticleAchs() {
        return nomenclatureArticleAchRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
    }

    @Override
    public List<NomenclatureArticleAch> findByNomenclature(Nomenclature nomenclature) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public NomenclatureArticleAch updateNomenclature(NomenclatureArticleAch nomenclatureArticleAch , long id ){
        nomenclatureArticleAch.setId(id);
        return nomenclatureArticleAchRepository.save(nomenclatureArticleAch);

    }
}
