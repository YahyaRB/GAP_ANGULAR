package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.DetailLivraisonRepository;
import ma.gap.repository.LivraisonsRepository;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
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

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class DetailLivraisonImpService implements DetailLivraisonService{

    private DetailLivraisonRepository detailLivraisonRepository;
    private LivraisonsRepository livraisonsRepository;
    private OrdreFabricationService ordreFabricationService;
    private ArticleService articleService;

    @Override
    public List<DetailLivraison> allDetailsLivraisons(Long id) {

        Livraisons liv=livraisonsRepository.findById(id).get();
        return detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(liv);
    }

    @Override
    public Optional<DetailLivraison> detailLivraisonById(Long id) {
        return detailLivraisonRepository.findById(id);
    }

    @Override
    public DetailLivraison saveDetailLivraison(DetailLivraison detaillivraison,Long id) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {
        int avancement,qteArticlesLivre,qteProd,qteEnProd;

        OrdreFabrication ordreFabrication=ordreFabricationService.findOFById(detaillivraison.getOrdreFabrication().getId()).get();

        double qte_reste=ordreFabrication.getQteRest();
        double qte_livreOF=ordreFabrication.getQteLivre();
        Article article=ordreFabrication.getArticle();
        qteArticlesLivre= (int) (article.getQuantiteLivre()+detaillivraison.getQuantite());
        article.setQuantiteLivre(qteArticlesLivre);
        ordreFabrication.setQteRest(qte_reste-detaillivraison.getQuantite());
        ordreFabrication.setQteLivre(qte_livreOF+detaillivraison.getQuantite());
        avancement= (int) ((((qte_livreOF+detaillivraison.getQuantite())/ordreFabrication.getQuantite())*100));
        System.out.println(avancement);
        ordreFabrication.setAvancement(avancement);
        qteProd= (int) (article.getQuantiteProd()+detaillivraison.getQuantite());
        article.setQuantiteProd(qteProd);
        qteEnProd=(int) (article.getQuantiteEnProd()-detaillivraison.getQuantite());
        article.setQuantiteEnProd(qteEnProd);
        articleService.editArticle(article,article.getId());
        ordreFabricationService.updateOf(ordreFabrication,ordreFabrication.getId());
        Livraisons liv=livraisonsRepository.findById(id).get();
        detaillivraison.setLivraison(liv);
        return detailLivraisonRepository.save(detaillivraison);
    }

    @Override
    public DetailLivraison editDetailLivraison(DetailLivraison detaillivraisonSaisie) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {
        int avancement,qteArticlesLivre,qteProd,qteEnProd;
        DetailLivraison detaillivraison=detailLivraisonRepository.findById(detaillivraisonSaisie.getId()).get();
       detaillivraisonSaisie.setId(detaillivraison.getId());
        OrdreFabrication ordreFabrication=detaillivraison.getOrdreFabrication();


        Article article=ordreFabrication.getArticle();

        if(detaillivraison.getQuantite()-detaillivraisonSaisie.getQuantite()!=0){


            qteArticlesLivre= (int) (article.getQuantiteLivre()+(detaillivraisonSaisie.getQuantite()-detaillivraison.getQuantite()));
            article.setQuantiteLivre(qteArticlesLivre);
            qteProd= (int) (article.getQuantiteProd()+(detaillivraisonSaisie.getQuantite()-detaillivraison.getQuantite()));
            article.setQuantiteProd(qteProd);
            qteEnProd= (int) (article.getQuantiteEnProd()-(detaillivraisonSaisie.getQuantite()-detaillivraison.getQuantite()));
            article.setQuantiteEnProd(qteEnProd);
            article.setQuantiteProd(qteProd);
            articleService.editArticle(article,article.getId());
            float QteRest=detaillivraisonSaisie.getQuantite()-detaillivraison.getQuantite();

            ordreFabrication.setQteRest(ordreFabrication.getQteRest()-QteRest);
            System.out.println(ordreFabrication.getQuantite()+"-"+QteRest);
            ordreFabrication.setQteLivre(ordreFabrication.getQteLivre()+QteRest);
            avancement= (int) (((ordreFabrication.getQteLivre()/ordreFabrication.getQuantite())*100));
            ordreFabrication.setAvancement(avancement);
            ordreFabricationService.updateOf(ordreFabrication,ordreFabrication.getId());

        }
       detaillivraisonSaisie.setLivraison(detaillivraison.getLivraison());
        return detailLivraisonRepository.save(detaillivraisonSaisie);
    }


    @Override
    @Transactional
    public void deleteDetailLivraison(Long id) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {
        try {
            System.out.println("Attempting to delete DetailLivraison with ID: " + id);

            // Check if the DetailLivraison exists
            Optional<DetailLivraison> optionalDetail = detailLivraisonRepository.findById(id);
            if (!optionalDetail.isPresent()) {
                throw new RuntimeException("DetailLivraison not found with id: " + id);
            }

            DetailLivraison detaillivraison = optionalDetail.get();
            System.out.println("Found DetailLivraison: " + detaillivraison);

            OrdreFabrication ordreFabrication = detaillivraison.getOrdreFabrication();
            if (ordreFabrication == null) {
                throw new RuntimeException("OrdreFabrication is null for DetailLivraison id: " + id);
            }

            // Rest of your logic...
            ordreFabrication.setQteRest(ordreFabrication.getQteRest()+detaillivraison.getQuantite());
            ordreFabrication.setQteLivre(ordreFabrication.getQteLivre()-detaillivraison.getQuantite());
            int avancement= (int) (((ordreFabrication.getQteLivre()/ordreFabrication.getQuantite())*100));
            System.out.println("Calculated avancement: " + avancement);

            Article article = ordreFabrication.getArticle();
            if (article == null) {
                throw new RuntimeException("Article is null for OrdreFabrication");
            }

            // Update quantities with validation
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

            // Update order fabrication

            ordreFabrication.setAvancement(avancement);

            System.out.println("Updating article: " + article);
            articleService.editArticle(article, article.getId());

            System.out.println("Updating ordre fabrication: " + ordreFabrication);
            ordreFabricationService.updateOf(ordreFabrication, ordreFabrication.getId());

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


//    @Override
//    public ResponseEntity<byte[]> impLivraison(Long id) throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException, OrdreFabricationNotFoundException {
//
//        Livraisons liv = livraisonsRepository.findById(id).get();
//       List<DetailLivraison> detailLivraisons=detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(liv);
//        Resource resource = new ClassPathResource("files/BL.jrxml");
//
//        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(detailLivraisons);
//        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));
//
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("chauffeur",liv.getChauffeur().getNom()+ " "+liv.getChauffeur().getPrenom());
//        JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
//
//        byte[] data = JasperExportManager.exportReportToPdf(report);
//        HttpHeaders headers = new HttpHeaders();
//        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Bon_Livraison_"+liv.getId()+".pdf");
//
//
//        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
//    }







    @Override
    public ResponseEntity<byte[]> impLivraison(Long id) throws JRException, IOException, OrdreFabricationNotFoundException {

        // Récupérer l'alimentation via l'ID
        Livraisons liv = livraisonsRepository.findById(id)
                .orElseThrow(() -> new OrdreFabricationNotFoundException("Livraison not found"));
        List<DetailLivraison> detailLivraisons = detailLivraisonRepository.findAllBylivraisonOrderByIdDesc(liv);


        // Charger le fichier Jasper (via le flux de ressources, plus sûr)
        Resource resource = new ClassPathResource("files/BL.jrxml");
        InputStream inputStream = resource.getInputStream();

        // Compiler le rapport Jasper à partir du flux d'entrée
        JasperReport compileReport = JasperCompileManager.compileReport(inputStream);

        // Créer la source de données à partir de l'objet AlimentationCaisse
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(detailLivraisons);

        // Créer une map pour les paramètres du rapport (si nécessaire)
        HashMap<String, Object> params = new HashMap<>();

        // Remplir le rapport avec les données et les paramètres
        JasperPrint report = JasperFillManager.fillReport(compileReport, params, beanCollectionDataSource);

        // Exporter le rapport en PDF
        byte[] data = JasperExportManager.exportReportToPdf(report);

        // Configurer les en-têtes HTTP pour la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Bon_Livraison_" + liv.getId() + ".pdf");

        // Retourner le PDF en tant que tableau de bytes
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
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Detail_"+id+".pdf");


        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
    }

}
