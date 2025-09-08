package ma.gap.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import ma.gap.entity.ArticleAch;
import ma.gap.repository.ArticleAchRepository;
import ma.gap.exceptions.ArticleAchNotFoundException;

@Service
@AllArgsConstructor
public class ArticleAchImpService implements ArticleAchService {

	private ArticleAchRepository articleAchRepository;
	
	@Override
	public ArticleAch createArticleAch(ArticleAch articleAch) throws IOException {
		ArticleAch saveArticleAch = articleAchRepository.save(articleAch);
		return saveArticleAch;
	}

	@Override
	public List<ArticleAch> getAllArticleAchs() {
		
		return articleAchRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
	}

	
	@Override
	public ArticleAch editArticleAch(ArticleAch articleAch, Long id) {
		articleAch.setId(id);
		ArticleAch updateArticleAch =articleAchRepository.save(articleAch);
		return updateArticleAch ;
	}

	@Override
	public void deleteArticleAch(long id)
			throws IOException, ArticleAchNotFoundException, EmptyResultDataAccessException {
		Optional<ArticleAch> articleAch =articleAchRepository.findById(id);
		articleAchRepository.deleteById(id);
	}

	@Override
	public Optional<ArticleAch> findArticleAchById(long id) {
		
		return articleAchRepository.findById(id);
	}
	
	
	

	@Override
    public List<ArticleAch> findByDesignationContaining(String designation ) {
        return articleAchRepository.findByDesignationContaining(designation); 
    }

	

}
