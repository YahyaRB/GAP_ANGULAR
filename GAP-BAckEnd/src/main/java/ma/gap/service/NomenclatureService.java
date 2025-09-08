package ma.gap.service;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

import ma.gap.exceptions.NomenclatureNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ma.gap.entity.*;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ResponseEntity;


public interface NomenclatureService {
	public List<Nomenclature> getAllNomenclatures();

	List<ArticleAch> getAllArticleAchs();

	Nomenclature createNomenclature(Nomenclature nomenclature, List<NomenclatureArticleAch> articleAchList);

	Nomenclature createNomenclatureIdPlan(Nomenclature nomenclature, List<NomenclatureArticleAch> articleAchList);

	Nomenclature getNomenclatureById(Long id);


	public Optional<Nomenclature> findNomenclatureById(Long id);

	void deleteNomenclature(Long id);

	public Optional<Nomenclature> findById(Long id);

	ResponseEntity<byte[]> generateReport(Long nomenclatureId) throws JRException, IOException, NomenclatureNotFoundException;

	List<NomenclatureArticleAch> findArticlesByNomenclature(Nomenclature nomenclature);

	void updateNomenclature(Nomenclature nomenclature,long id);
}