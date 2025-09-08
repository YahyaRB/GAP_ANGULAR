package ma.gap.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;

import ma.gap.entity.ArticleAch;
import ma.gap.entity.Plan;
import ma.gap.exceptions.ArticleAchNotFoundException;
import ma.gap.exceptions.NomenclatureNotFoundException;

public interface ArticleAchService {
	ArticleAch createArticleAch(ArticleAch articleAch ) throws IOException;
	 
	 List<ArticleAch> getAllArticleAchs();
	 
	 public ArticleAch editArticleAch(ArticleAch articleAch,Long id);

	 void deleteArticleAch(long id) throws IOException, EmptyResultDataAccessException, ArticleAchNotFoundException;
	 
	 Optional<ArticleAch> findArticleAchById(long id);

	List<ArticleAch> findByDesignationContaining(String designation);








}
