package ma.gap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;
import ma.gap.repository.NomenclatureArticleAchRepository;

import java.util.List;
import java.util.Optional;

@Service
public interface NomenclatureArticleAchService {

	List<NomenclatureArticleAch> getAllNomenclatureArticleAchs();

	List<NomenclatureArticleAch> findByNomenclature(Nomenclature nomenclature);

	NomenclatureArticleAch updateNomenclature(NomenclatureArticleAch nomenclatureArticleAch , long id );
}
