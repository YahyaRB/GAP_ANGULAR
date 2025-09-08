package ma.gap.service;

import ma.gap.entity.Ateliers;
import ma.gap.entity.Livraisons;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface LivraisonService {

    public List<Livraisons> allLivraisons();
    public Optional<Livraisons> livraisonById(Long id);
    public Livraisons saveLivraison(Livraisons livraison);
    public Livraisons editLivraison(Livraisons livraison,Long id);
    public void deleteLivraison(Long id) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException;
    public List<Livraisons> allLiraisonsByAtelier(Ateliers ateliers);
	//List<Livraisons> allLiraisonsByAtelier(List<Ateliers> ateliers);
    List<Livraisons> allLivraisonsByAtelierWithPagination(List<Ateliers> ateliers);



}
