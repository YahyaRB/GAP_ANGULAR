package ma.gap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.gap.entity.ArticleAch;

@Repository
public interface ArticleAchRepository extends JpaRepository<ArticleAch, Long> {

	List<ArticleAch> findByDesignationContaining(String ref);
}
