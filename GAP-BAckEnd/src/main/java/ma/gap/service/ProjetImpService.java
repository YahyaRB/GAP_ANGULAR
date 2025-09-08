package ma.gap.service;

import ma.gap.entity.*;
import ma.gap.repository.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.ProjetNotFoundException;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProjetImpService implements ProjetService {

    private CustomProjetRepository customProjetRepository;
    private ProjetRepository projetRepository;
    private OfSearchDao ofSearchDao;

    @Override
    public List<Projet> allProjet() {

        return projetRepository.findAll(Sort.by(Sort.Direction.DESC,"code"));
    }

    @Override
    public List<Projet> getAllProjetsByStatus(int status) {
        return projetRepository.findAllByStatus(status);
    }

    @Override
    public Projet saveProjet(Projet projet) {
        return projetRepository.save(projet);
    }

    @Override
    public Projet updateProjet(Projet projet, long id) {
        projet.setId(id);
        return projetRepository.save(projet);
    }

    @Override
    public void deleteProjet(long id) {
     projetRepository.deleteById(id);
    }



    @Override
    public Optional<Projet> findById(long id) {
        return projetRepository.findById(id);
    }





    @Override
    public List<Projet> ProjetFiltred(String code, String affaire, String article, String atelier) {

        List<Projet> projetFiltred = customProjetRepository.projetList(code,affaire,article,atelier);

        return projetFiltred;
    }


    @Override
    public Page<OrdreFabrication> ofProjetFiltred(Pageable pageable,User user, long idprojet, long idof,long atelier,  String libelle, String avancement) throws ParseException {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<OrdreFabrication> listCons;

        List<OrdreFabrication> projetFiltred = ofSearchDao.searchOFByProjet(user,idprojet, idof,atelier,libelle,avancement);

        if (projetFiltred.size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, projetFiltred.size());
            listCons = projetFiltred.subList(startItem, toIndex);
        }
        Page<OrdreFabrication> referencePage = new PageImpl<OrdreFabrication>(listCons, PageRequest.of(currentPage, pageSize),projetFiltred.size());

        return referencePage;
    }
    @Override
    public List<Projet> getAffairesByAtelier(Long atelierId) {
        return projetRepository.findAffairesByAtelierAndOF(atelierId);
    }
    @Override
    public List<Projet> findAffairesByAtelierAndQteArticle_Sup_QteENPROD(long atelierId) {
System.out.println("sdfdsfds");
System.out.println(projetRepository.findAffairesByAtelierAndQteArticle_Sup_QteEnProd(atelierId));

        return projetRepository.findAffairesByAtelierAndQteArticle_Sup_QteEnProd(atelierId);
    }
}
