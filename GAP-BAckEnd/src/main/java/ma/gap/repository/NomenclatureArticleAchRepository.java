package ma.gap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ma.gap.entity.Nomenclature;
import ma.gap.entity.NomenclatureArticleAch;

import org.springframework.stereotype.Repository;

@Repository
public interface NomenclatureArticleAchRepository extends JpaRepository<NomenclatureArticleAch, Long> {

	void deleteByNomenclature(Nomenclature nomenclature);

	NomenclatureArticleAch save(Nomenclature nomenclature);

	List<NomenclatureArticleAch> findByNomenclature(Nomenclature nomenclature);

}
