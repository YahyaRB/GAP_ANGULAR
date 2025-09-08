package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.LivraisonsRepository;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
@AllArgsConstructor
@Service
public class LivraisonImpService implements LivraisonService{

    private LivraisonsRepository livraisonsRepository;
    private DetailLivraisonService detailLivraisonService;
    @Override
    public List<Livraisons> allLivraisons() {

        return livraisonsRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
    }
    @Override
    public  List<Livraisons> allLivraisonsByAtelierWithPagination(List<Ateliers> ateliers){
        return livraisonsRepository.findAllByAtelier(ateliers);
    }

    @Override
    public Optional<Livraisons> livraisonById(Long id) {
        return livraisonsRepository.findById(id);
    }

    @Override
    public Livraisons saveLivraison(Livraisons livraison) {
        return livraisonsRepository.save(livraison);
    }

    @Override
    public Livraisons editLivraison(Livraisons livraison, Long id) {

     livraison.setId(id);
     return livraisonsRepository.save(livraison);



        }
    @Override
    public void deleteLivraison(Long id) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {

        List<DetailLivraison> listeDetails=detailLivraisonService.allDetailsLivraisons(id);
        for (DetailLivraison str : listeDetails) {
            detailLivraisonService.deleteDetailLivraison(str.getId());
        }
        livraisonsRepository.deleteById(id);
    }
	@Override
	public List<Livraisons> allLiraisonsByAtelier(Ateliers ateliers) {
		// TODO Auto-generated method stub
		return null;
	}

}
