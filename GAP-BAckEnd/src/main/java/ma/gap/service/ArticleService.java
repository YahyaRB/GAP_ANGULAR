package ma.gap.service;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import ma.gap.entity.Article;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.ProjetNotFoundException;
import org.springframework.data.domain.Sort;

public interface ArticleService {

	// --------------------- GESTION DES ARTICLES ---------------------
	public List<Article> findAll(Sort id);
	public Optional<Article> findById(long i);
	public List<Article> allArticleByProject(long idUser, long projet);

	public List<Article> allArticleByAtelierAndProjet(long atelier, long projet);

	public Article saveArticle(Article article, long projet);

	public Article editArticle(Article article, long id) throws ArticleNotFoundException;

	public boolean deleteArticle(long id);
	public List<Article> allArticleByAtelier(long atelier);
	public List<Article> allArticleByProjectAndAtelier(long idUser,long projet,long atelier) throws ProjetNotFoundException;
	//public List<Article> allArticleByProjectAndAtelier(long idUser, long projet, long atelier) throws ProjetNotFoundException;

    List<Article> searchArticle(long idUser, String numPrix,String designation, long idProjet, long idAtelier, long idArticle) throws ParseException;

	public List<Article> findArticles_QteSup_QteOF(long projetId,long atelierId);

}
