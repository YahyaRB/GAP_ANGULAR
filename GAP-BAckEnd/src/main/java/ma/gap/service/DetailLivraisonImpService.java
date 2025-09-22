package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.DetailLivraisonRepository;
import ma.gap.repository.LivraisonsRepository;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import ma.gap.repository.NomenclatureRepository;
import ma.gap.repository.OrdreFabricationRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DetailLivraisonImpService implements DetailLivraisonService {

    private DetailLivraisonRepository detailLivraisonRepository;
    private LivraisonsRepository livraisonsRepository;
    private OrdreFabricationService ordreFabricationService;
    private ArticleService articleService;
    private OrdreFabricationRepository ordreFabricationRepository;
    private NomenclatureRepository nomenclatureRepository;

    @Override
    public List<DetailLivraison> allDetailsLivraisons(Long id) {
        Livraisons liv = livraisonsRepository.findById(id).get();
        List<DetailLivraison> details = detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(liv);

        // DEBUG - Afficher les informations des détails
        for (DetailLivraison detail : details) {
            System.out.println("=== DETAIL LIVRAISON DEBUG ===");
            System.out.println("ID: " + detail.getId());
            System.out.println("Type Detail: " + detail.getTypeDetail());
            System.out.println("Quantite: " + detail.getQuantite());
            System.out.println("OF présent: " + (detail.getOrdreFabrication() != null));
            System.out.println("Nomenclature présente: " + (detail.getNomenclature() != null));

            if (detail.getOrdreFabrication() != null) {
                System.out.println("OF NumOF: " + detail.getOrdreFabrication().getNumOF());
                if (detail.getOrdreFabrication().getArticle() != null) {
                    System.out.println("OF Article: " + detail.getOrdreFabrication().getArticle().getDesignation());
                }
            }

            if (detail.getNomenclature() != null) {
                System.out.println("Nomenclature Type: " + detail.getNomenclature().getType());
                System.out.println("Nomenclature Unite: " + detail.getNomenclature().getUnite());
                if (detail.getNomenclature().getOrdreFabrication() != null) {
                    System.out.println("Nomenclature OF: " + detail.getNomenclature().getOrdreFabrication().getNumOF());
                }
            }
            System.out.println("==============================");
        }

        return details;
    }

    @Override
    public Optional<DetailLivraison> detailLivraisonById(Long id) {
        return detailLivraisonRepository.findById(id);
    }

    @Override
    @Transactional
    public DetailLivraison saveDetailLivraison(DetailLivraison dl, Long idLivraison)
            throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {

        try {
            // 1) Charger la livraison et lier
            Livraisons livraison = livraisonsRepository.findById(idLivraison)
                    .orElseThrow(() -> new EntityNotFoundException("Livraison introuvable: " + idLivraison));
            dl.setLivraison(livraison);

            // 2) Validations génériques
            if (dl.getTypeDetail() == null) {
                throw new IllegalArgumentException("Type de détail manquant (OF_COMPLET ou NOMENCLATURE).");
            }

            Float qteObj = dl.getQuantite();
            float qte = (qteObj == null) ? 0f : qteObj;
            if (qte <= 0f) {
                throw new IllegalArgumentException("Quantité invalide.");
            }

            // Récupérer les IDs depuis la requête avant de nettoyer
            Long ofId = (dl.getOrdreFabrication() != null) ? dl.getOrdreFabrication().getId() : null;
            Long nomId = (dl.getNomenclature() != null) ? dl.getNomenclature().getId() : null;

            // IMPORTANT: Nettoyer les références avant de commencer
            dl.setOrdreFabrication(null);
            dl.setNomenclature(null);

            switch (dl.getTypeDetail()) {
                case OF_COMPLET: {
                    // Vérification de l'OF fourni dans la requête
                    if (ofId == null) {
                        throw new IllegalArgumentException("ordreFabrication.id est requis pour OF_COMPLET.");
                    }

                    OrdreFabrication of = ordreFabricationRepository.findById(ofId)
                            .orElseThrow(() -> new EntityNotFoundException("Ordre de Fabrication introuvable: " + ofId));

                    // Logique OF_COMPLET...
                    double ofQteRest = of.getQteRest();
                    double ofQteLivre = of.getQteLivre();
                    double ofQuantite = of.getQuantite();

                    if (ofQuantite <= 0) {
                        throw new IllegalStateException("Quantité totale de l'OF invalide.");
                    }
                    if (qte > ofQteRest) {
                        throw new IllegalArgumentException(
                                "Quantité demandée (" + qte + ") > quantité restante de l'OF (" + ofQteRest + ").");
                    }

                    // MAJ OF
                    of.setQteRest(ofQteRest - qte);
                    of.setQteLivre(ofQteLivre + qte);
                    int avancement = (int) Math.round((of.getQteLivre() / ofQuantite) * 100);
                    of.setAvancement(avancement);

                    // MAJ article si nécessaire
                    Article article = of.getArticle();
                    if (article != null) {
                        article.setQuantiteLivre(article.getQuantiteLivre() + qte);
                        articleService.editArticle(article, article.getId());
                    }

                    ordreFabricationService.updateOf(of, of.getId());

                    // Définir SEULEMENT l'OF
                    dl.setOrdreFabrication(of);

                    System.out.println("DEBUG OF_COMPLET - OF ID: " + of.getId() + ", Nomenclature: null");
                    break;
                }

                case NOMENCLATURE: {
                    // Vérification de la nomenclature fournie dans la requête
                    if (nomId == null) {
                        throw new IllegalArgumentException("nomenclature.id est requis pour NOMENCLATURE.");
                    }

                    Nomenclature nomenclature = nomenclatureRepository.findById(nomId)
                            .orElseThrow(() -> new EntityNotFoundException("Nomenclature introuvable: " + nomId));

                    double nomQteRest = nomenclature.getQuantiteRest();

                    if (qte > nomQteRest) {
                        throw new IllegalArgumentException(
                                "Quantité demandée (" + qte + ") > quantité restante nomenclature (" + nomQteRest + ").");
                    }

                    // MAJ nomenclature
                    nomenclature.setQuantiteRest(nomQteRest - qte);
                    nomenclature.setQuantiteLivre(nomenclature.getQuantiteLivre() + qte);

                    nomenclatureRepository.save(nomenclature);

                    // Définir SEULEMENT la nomenclature
                    dl.setNomenclature(nomenclature);

                    System.out.println("DEBUG NOMENCLATURE - Nomenclature ID: " + nomenclature.getId() + ", OF: null");
                    break;
                }

                default:
                    throw new IllegalArgumentException("Type de détail inconnu: " + dl.getTypeDetail());
            }

            // Debug avant sauvegarde
            System.out.println("Avant save - Type: " + dl.getTypeDetail() +
                    ", OF: " + (dl.getOrdreFabrication() != null ? dl.getOrdreFabrication().getId() : "null") +
                    ", Nomenclature: " + (dl.getNomenclature() != null ? dl.getNomenclature().getId() : "null"));

            return detailLivraisonRepository.save(dl);

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde DetailLivraison: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public DetailLivraison editDetailLivraison(DetailLivraison detaillivraisonSaisie)
            throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {

        DetailLivraison detaillivraison = detailLivraisonRepository.findById(detaillivraisonSaisie.getId()).get();
        detaillivraisonSaisie.setId(detaillivraison.getId());

        // Gérer selon le type de détail
        if (detaillivraison.getTypeDetail() == ma.gap.enums.TypeDetail.OF_COMPLET) {
            // Logique pour OF_COMPLET (votre code existant)
            OrdreFabrication ordreFabrication = detaillivraison.getOrdreFabrication();
            Article article = ordreFabrication.getArticle();

            if (detaillivraison.getQuantite() - detaillivraisonSaisie.getQuantite() != 0) {
                int qteArticlesLivre = (int) (article.getQuantiteLivre() + (detaillivraisonSaisie.getQuantite() - detaillivraison.getQuantite()));
                article.setQuantiteLivre(qteArticlesLivre);

                int qteProd = (int) (article.getQuantiteProd() + (detaillivraisonSaisie.getQuantite() - detaillivraison.getQuantite()));
                article.setQuantiteProd(qteProd);

                int qteEnProd = (int) (article.getQuantiteEnProd() - (detaillivraisonSaisie.getQuantite() - detaillivraison.getQuantite()));
                article.setQuantiteEnProd(qteEnProd);

                articleService.editArticle(article, article.getId());

                float QteRest = detaillivraisonSaisie.getQuantite() - detaillivraison.getQuantite();
                ordreFabrication.setQteRest(ordreFabrication.getQteRest() - QteRest);
                ordreFabrication.setQteLivre(ordreFabrication.getQteLivre() + QteRest);

                int avancement = (int) (((ordreFabrication.getQteLivre() / ordreFabrication.getQuantite()) * 100));
                ordreFabrication.setAvancement(avancement);

                ordreFabricationService.updateOf(ordreFabrication, ordreFabrication.getId());
            }
        } else if (detaillivraison.getTypeDetail() == ma.gap.enums.TypeDetail.NOMENCLATURE) {
            // Logique pour NOMENCLATURE
            Nomenclature nomenclature = detaillivraison.getNomenclature();

            if (detaillivraison.getQuantite() - detaillivraisonSaisie.getQuantite() != 0) {
                float qteDiff = detaillivraisonSaisie.getQuantite() - detaillivraison.getQuantite();

                nomenclature.setQuantiteRest(nomenclature.getQuantiteRest() - qteDiff);
                nomenclature.setQuantiteLivre(nomenclature.getQuantiteLivre() + qteDiff);

                nomenclatureRepository.save(nomenclature);
            }
        }

        detaillivraisonSaisie.setLivraison(detaillivraison.getLivraison());
        return detailLivraisonRepository.save(detaillivraisonSaisie);
    }

    @Override
    @Transactional
    public void deleteDetailLivraison(Long id) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {
        try {
            System.out.println("Attempting to delete DetailLivraison with ID: " + id);

            Optional<DetailLivraison> optionalDetail = detailLivraisonRepository.findById(id);
            if (!optionalDetail.isPresent()) {
                throw new RuntimeException("DetailLivraison not found with id: " + id);
            }

            DetailLivraison detaillivraison = optionalDetail.get();
            System.out.println("Found DetailLivraison: " + detaillivraison);

            // Gérer selon le type de détail
            if (detaillivraison.getTypeDetail() == ma.gap.enums.TypeDetail.OF_COMPLET) {
                OrdreFabrication ordreFabrication = detaillivraison.getOrdreFabrication();
                if (ordreFabrication == null) {
                    throw new RuntimeException("OrdreFabrication is null for DetailLivraison id: " + id);
                }

                // Restaurer les quantités de l'OF
                ordreFabrication.setQteRest(ordreFabrication.getQteRest() + detaillivraison.getQuantite());
                ordreFabrication.setQteLivre(ordreFabrication.getQteLivre() - detaillivraison.getQuantite());

                int avancement = (int) (((ordreFabrication.getQteLivre() / ordreFabrication.getQuantite()) * 100));
                ordreFabrication.setAvancement(avancement);

                Article article = ordreFabrication.getArticle();
                if (article != null) {
                    float qteArticlesLivre = article.getQuantiteLivre() - detaillivraison.getQuantite();
                    if (qteArticlesLivre < 0) {
                        throw new RuntimeException("Quantity delivered would become negative: " + qteArticlesLivre);
                    }
                    article.setQuantiteLivre(qteArticlesLivre);

                    float qteProd = article.getQuantiteProd() - detaillivraison.getQuantite();
                    if (qteProd < 0) {
                        throw new RuntimeException("Quantity produced would become negative: " + qteProd);
                    }
                    article.setQuantiteProd(qteProd);

                    float qteEnProd = article.getQuantiteEnProd() + detaillivraison.getQuantite();
                    article.setQuantiteEnProd(qteEnProd);

                    articleService.editArticle(article, article.getId());
                }

                ordreFabricationService.updateOf(ordreFabrication, ordreFabrication.getId());

            } else if (detaillivraison.getTypeDetail() == ma.gap.enums.TypeDetail.NOMENCLATURE) {
                Nomenclature nomenclature = detaillivraison.getNomenclature();
                if (nomenclature != null) {
                    // Restaurer les quantités de la nomenclature
                    nomenclature.setQuantiteRest(nomenclature.getQuantiteRest() + detaillivraison.getQuantite());
                    nomenclature.setQuantiteLivre(nomenclature.getQuantiteLivre() - detaillivraison.getQuantite());

                    nomenclatureRepository.save(nomenclature);
                }
            }

            System.out.println("Deleting DetailLivraison with id: " + id);
            detailLivraisonRepository.deleteById(id);

            System.out.println("Successfully deleted DetailLivraison with id: " + id);

        } catch (Exception e) {
            System.err.println("Error deleting DetailLivraison with id " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<DetailLivraison> findAllBylivraison(Livraisons livraisons) {
        return detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(livraisons);
    }

    @Override
    public ResponseEntity<byte[]> impLivraison(Long id) throws JRException, IOException, OrdreFabricationNotFoundException {
        // Récupérer l'alimentation via l'ID
        Livraisons liv = livraisonsRepository.findById(id)
                .orElseThrow(() -> new OrdreFabricationNotFoundException("Livraison not found"));
        List<DetailLivraison> detailLivraisons = detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(liv);

        // Charger le fichier Jasper
        Resource resource = new ClassPathResource("files/BL.jrxml");
        InputStream inputStream = resource.getInputStream();

        // Compiler le rapport Jasper
        JasperReport compileReport = JasperCompileManager.compileReport(inputStream);

        // Créer la source de données
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(detailLivraisons);

        // Créer une map pour les paramètres du rapport
        HashMap<String, Object> params = new HashMap<>();

        // Remplir le rapport avec les données et les paramètres
        JasperPrint report = JasperFillManager.fillReport(compileReport, params, beanCollectionDataSource);

        // Exporter le rapport en PDF
        byte[] data = JasperExportManager.exportReportToPdf(report);

        // Configurer les en-têtes HTTP pour la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Bon_Livraison_" + liv.getId() + ".pdf");

        // Retourner le PDF
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @Override
    public ResponseEntity<byte[]> impArticle(Long id) throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException, OrdreFabricationNotFoundException {
        DetailLivraison detailLivraison = detailLivraisonRepository.findById(id).get();

        Resource resource = new ClassPathResource("files/ArticleOf.jrxml");

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(detailLivraison));
        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));

        HashMap<String, Object> map = new HashMap<>();

        JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

        byte[] data = JasperExportManager.exportReportToPdf(report);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Detail_" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
    }

    @Override
    public List<OfProjectQteRestDto> getAvailableOFByProjet(Long projetId) {
        // À implémenter selon votre logique métier
        return null;
    }

    @Override
    public OfProjectQteRestDto mapToOfProjectQteRestDto(OrdreFabrication of) {
        // À implémenter selon votre logique métier
        return null;
    }

    @Override
    public DetailLivraison saveDetailLivraisonWithType(DetailLivraison detaillivraison, Long idLivraison) {
        // Récupérer la livraison
        Livraisons liv = livraisonsRepository.findById(idLivraison).get();
        detaillivraison.setLivraison(liv);

        // Sauvegarder le détail
        return detailLivraisonRepository.save(detaillivraison);
    }
}