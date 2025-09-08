package ma.gap.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.ProjetNotFoundException;
import ma.gap.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ArticleImpService implements ArticleService{
	ArticleRepository articleRepository;
	UserService userImpService;
	ProjetService projetService;
	AtelierService atelierService;
	AffectationUpdateRepository affectationUpdateRepository;
	AtelierRepository atelierRepository;
	ArticleSearchDao articleSearchDao;
	
	@Override
	public List<Article> findAll(Sort id) {
		
		return articleRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
	}

	@Override
	public Optional<Article> findById(long id) {
		// TODO Auto-generated method stub
		return articleRepository.findById(id);
	}

	@Override
	public Article saveArticle(Article article,long projet) {
		//Optional<Projet> projet1 = projetRepository.findById(projet);
		//article.setProjet(projet1.get());
		return articleRepository.save(article);
	}
	@Override
	public List<Article> allArticleByProject(long idUser, long projet) {
		List<Ateliers> ateliers = userImpService.findbyusername(idUser).getAteliers();
		Optional<Projet> projet1 = projetService.findById(projet);

		return articleRepository.findAllByProjetAndAteliersInOrderByIdDesc(projet1.get(),ateliers);
	}

	@Override
	public List<Article> allArticleByAtelierAndProjet(long atelier,long projet) {
		List<Ateliers> ateliersList = new ArrayList<>();
		Optional<Ateliers> ateliers = atelierRepository.findById(atelier);
		Optional<Projet> projet1 = projetService.findById(projet);
		ateliersList.add(ateliers.get());

		List<Article> articleList = articleRepository.findAllByProjetAndAteliersInOrderByIdDesc(projet1.get(),ateliersList);

		return articleList;
	}
	@Override
	public List<Article> allArticleByAtelier(long atelier) {
		return articleRepository.findAllByAteliersOrderByIdDesc(atelier);
	}
	@Override
	public Article editArticle(Article article, long id) throws ArticleNotFoundException {
		article.setId(id);

		return articleRepository.save(article);
	}

	@Override
	public boolean deleteArticle(long id) {
		Optional<Article> article = articleRepository.findById(id);
		List<AffectationUpdate> affectationUpdates = affectationUpdateRepository.findAllByArticleOrderByIdDesc(article.get());

		if (affectationUpdates.isEmpty()){
			articleRepository.deleteById(id);
			return true;
		}
		else
			return false;
	}
	@Override
	public List<Article> allArticleByProjectAndAtelier(long idUser,long projet,long atelier) throws ProjetNotFoundException {
		projetService.findById(projet);
		if(userImpService.findAtelierById(idUser,atelier))

			return articleRepository.findAllByProjetAndAteliersOrderByIdDesc(projet,atelier);
		else
		{
			return null;
		}
	}

	@Override
	public List<Article> searchArticle(long idUser, String numPrix, String designation, long idProjet, long idAtelier, long idArticle) throws ParseException {
		return articleSearchDao.searchArticle( idUser,  numPrix,  designation,  idProjet,  idAtelier,  idArticle);
	}

	@Override
	public List<Article> findArticles_QteSup_QteOF(long projetId,long atelierId) {

		return articleRepository.findArticlesByProjetIdAndAtelierId(projetId,atelierId);
	}
}
