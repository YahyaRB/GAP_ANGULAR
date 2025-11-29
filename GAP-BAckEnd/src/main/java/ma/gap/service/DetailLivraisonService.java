package ma.gap.service;

import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import net.sf.jasperreports.engine.JRException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DetailLivraisonService {

    List<DetailLivraison> allDetailsLivraisons(Long id);

    public Optional<DetailLivraison> detailLivraisonById(Long id);

    public DetailLivraison saveDetailLivraison(DetailLivraison detaillivraison, Long id)
            throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException;

    void updateOfAndArticleFromNomenclature(Nomenclature nomenclature, float quantity);

    public DetailLivraison editDetailLivraison(DetailLivraison detaillivraison)
            throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException;

    public void deleteDetailLivraison(Long id)
            throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException;

    List<DetailLivraison> findAllBylivraison(Livraisons livraisons);

    ResponseEntity<byte[]> impLivraison(Long id) throws JRException, FileNotFoundException, IOException,
            EmptyResultDataAccessException, OrdreFabricationNotFoundException;

    ResponseEntity<byte[]> impArticle(Long id) throws JRException, FileNotFoundException, IOException,
            EmptyResultDataAccessException, OrdreFabricationNotFoundException;

    List<OfProjectQteRestDto> getAvailableOFByProjet(Long projetId);

    OfProjectQteRestDto mapToOfProjectQteRestDto(OrdreFabrication of);

    DetailLivraison saveDetailLivraisonWithType(DetailLivraison detaillivraison, Long idLivraison);

}
