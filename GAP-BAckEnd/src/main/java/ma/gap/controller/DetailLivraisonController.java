package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.dtos.DetailLivraisonRequest;
import ma.gap.dtos.NomenclatureQteRestDto;
import ma.gap.entity.*;
import ma.gap.enums.TypeDetail;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.DetailLivraisonRepository;
import ma.gap.repository.OrdreFabricationRepository;
import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import ma.gap.service.DetailLivraisonService;
import ma.gap.service.LivraisonService;
import ma.gap.service.NomenclatureService;
import ma.gap.service.OrdreFabricationService;
import net.sf.jasperreports.engine.JRException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/DetailLivraison")
public class DetailLivraisonController {
    private DetailLivraisonService detailLivraisonService;
    private LivraisonService livraisonService;
    private OrdreFabricationRepository ordreFabricationRepository;
    private NomenclatureService nomenclatureService;
    private OrdreFabricationService ofService;

    @PostMapping("/Ajouter/{idLivraison}")
    public ResponseEntity<String> ajouterDetail(
            @PathVariable Long idLivraison,
            @RequestBody DetailLivraison detail) {
        try {

            DetailLivraison nouveauDetail = detailLivraisonService.saveDetailLivraison(detail, idLivraison);
            return new ResponseEntity<>("Détail de livraison ajouté avec succès", HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("=== ERREUR DANS LE CONTRÔLEUR ===");
            e.printStackTrace();
            return new ResponseEntity<>("Erreur lors de l'ajout du détail: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("Livraison/ListeDetailByBL/{idLivraison}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie','consulteur')")
    public List<DetailLivraison> listeDetailsByBL(@PathVariable Long idLivraison) {

        Livraisons livraisons = livraisonService.livraisonById(idLivraison).get();

        return detailLivraisonService.findAllBylivraison(livraisons);
    }

    @PutMapping(value = "/Editer/{id}")
    public ResponseEntity<?> updateDetail(@PathVariable Long id, @Valid @RequestBody DetailLivraison detail) {
        try {
            // Vérification de cohérence des IDs
            if (!id.equals(detail.getId())) {
                return ResponseEntity.badRequest().body("L'ID dans l'URL ne correspond pas à l'ID du détail");
            }

            DetailLivraison updatedDetail = detailLivraisonService.editDetailLivraison(detail);
            return ResponseEntity.ok(updatedDetail);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur interne du serveur");
        }
    }

    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<?> deleteDetailLivraison(@PathVariable Long id) {
        try {
            detailLivraisonService.deleteDetailLivraison(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.err.println("Runtime error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/ArticleOf/Imprimer/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<byte[]> impressionArticle(Model model, @PathVariable("id") Long id)
            throws JRException, IOException, OrdreFabricationNotFoundException, ArticleNotFoundException {

        DetailLivraison dl = detailLivraisonService.detailLivraisonById(id).get();
        dl.setImprime(true);
        dl.setId(id);
        detailLivraisonService.editDetailLivraison(dl);
        return detailLivraisonService.impArticle(id);
    }

    /**
     * Récupère les nomenclatures disponibles pour une livraison donnée
     */
    @GetMapping("/getNomenclaturesByLivraison/{idLivraison}")
    public ResponseEntity<List<NomenclatureQteRestDto>> getNomenclaturesByLivraison(@PathVariable Long idLivraison) {
        try {
            // Récupérer la livraison pour obtenir le projet
            Livraisons livraison = livraisonService.livraisonById(idLivraison)
                    .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée avec l'ID: " + idLivraison));

            Long projetId = livraison.getProjet().getId();

            // Récupérer toutes les nomenclatures disponibles pour ce projet
            List<NomenclatureQteRestDto> nomenclatures = nomenclatureService
                    .getNomenclaturesByProjetWithQuantities(projetId);

            return ResponseEntity.ok(nomenclatures);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nomenclatures: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Récupère les nomenclatures disponibles pour un OF spécifique
     */
    @GetMapping("/getNomenclaturesByOF/{idOF}")
    public ResponseEntity<List<NomenclatureQteRestDto>> getNomenclaturesByOF(@PathVariable Long idOF) {
        try {
            List<NomenclatureQteRestDto> nomenclatures = nomenclatureService.getNomenclaturesByOFWithQuantities(idOF);
            return ResponseEntity.ok(nomenclatures);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nomenclatures pour l'OF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Ajoute un détail de livraison avec support pour OF complet ou nomenclature
     */
    @PostMapping("/AjouterAvecType/{idLivraison}")
    public ResponseEntity<String> ajouterDetailAvecType(
            @PathVariable Long idLivraison,
            @RequestBody DetailLivraisonRequest request) {
        try {

            // Validation des données
            if (request.getType() == null || request.getType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le type de détail est requis");
            }

            if (request.getQuantite() <= 0) {
                return ResponseEntity.badRequest().body("La quantité doit être supérieure à 0");
            }

            // Récupérer la livraison
            Livraisons livraison = livraisonService.livraisonById(idLivraison)
                    .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée"));

            // Créer le détail selon le type
            DetailLivraison detail = new DetailLivraison();
            detail.setQuantite(request.getQuantite());
            detail.setEmplacement(request.getEmplacement());
            detail.setObservation(request.getObservation());
            detail.setLivraison(livraison);

            if ("OF_COMPLET".equals(request.getType())) {
                // Traitement pour OF complet
                if (request.getOrdreFabricationId() == null) {
                    return ResponseEntity.badRequest()
                            .body("L'ID de l'ordre de fabrication est requis pour un OF complet");
                }

                OrdreFabrication of = ordreFabricationRepository.findById(request.getOrdreFabricationId())
                        .orElseThrow(() -> new EntityNotFoundException("Ordre de fabrication non trouvé"));

                // Vérifier les quantités disponibles
                if (request.getQuantite() > of.getQteRest()) {
                    return ResponseEntity.badRequest().body("Quantité demandée supérieure à la quantité restante");
                }

                detail.setOrdreFabrication(of);
                detail.setTypeDetail(TypeDetail.OF_COMPLET);

                // Sauvegarder le détail
                DetailLivraison savedDetail = detailLivraisonService.saveDetailLivraisonWithType(detail, idLivraison);

                // Mettre à jour les quantités de l'OF
                of.setQteLivre(of.getQteLivre() + request.getQuantite());
                of.setQteRest(of.getQuantite() - of.getQteLivre());
                ordreFabricationRepository.save(of);

            } else if ("NOMENCLATURE".equals(request.getType())) {
                // Traitement pour nomenclature
                if (request.getNomenclatureId() == null) {
                    return ResponseEntity.badRequest().body("L'ID de la nomenclature est requis");
                }

                Nomenclature nomenclature = nomenclatureService.findById(request.getNomenclatureId())
                        .orElseThrow(() -> new EntityNotFoundException("Nomenclature non trouvée"));

                // Vérifier les quantités disponibles
                if (request.getQuantite() > nomenclature.getQuantiteRest()) {
                    return ResponseEntity.badRequest()
                            .body("Quantité demandée supérieure à la quantité restante de la nomenclature");
                }

                detail.setNomenclature(nomenclature);
                detail.setTypeDetail(TypeDetail.NOMENCLATURE);

                // Populate OF and Article for reference
                if (nomenclature.getOrdreFabrication() != null) {
                    detail.setOrdreFabrication(nomenclature.getOrdreFabrication());
                    if (nomenclature.getOrdreFabrication().getArticle() != null) {
                        detail.setArticle(nomenclature.getOrdreFabrication().getArticle());
                    }
                }

                // Sauvegarder le détail
                DetailLivraison savedDetail = detailLivraisonService.saveDetailLivraisonWithType(detail, idLivraison);

                // Mettre à jour les quantités de la nomenclature
                nomenclatureService.updateQuantitiesAfterDelivery(request.getNomenclatureId(), request.getQuantite());

                // Mettre à jour les quantités de l'OF et de l'Article
                detailLivraisonService.updateOfAndArticleFromNomenclature(nomenclature, request.getQuantite());

            } else {
                return ResponseEntity.badRequest().body("Type de détail non reconnu: " + request.getType());
            }

            return ResponseEntity.ok("Détail de livraison ajouté avec succès");

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entité non trouvée: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("=== ERREUR DANS LE CONTRÔLEUR (AjouterAvecType) ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout du détail: " + e.getMessage());
        }
    }

    /**
     * Vérifie la disponibilité d'une nomenclature pour une quantité donnée
     */
    @GetMapping("/checkNomenclatureAvailability/{nomenclatureId}/{quantite}")
    public ResponseEntity<Boolean> checkNomenclatureAvailability(
            @PathVariable Long nomenclatureId,
            @PathVariable Double quantite) {
        try {
            Optional<Nomenclature> nomenclatureOpt = nomenclatureService.findById(nomenclatureId);
            if (nomenclatureOpt.isPresent()) {
                Nomenclature nomenclature = nomenclatureOpt.get();
                boolean available = nomenclature.getQuantiteRest() >= quantite;
                return ResponseEntity.ok(available);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de disponibilité: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/getAllOFByLivraison/{idLivraison}")
    public ResponseEntity<List<OfProjectQteRestDto>> getAllOFByLivraison(@PathVariable Long idLivraison) {
        try {
            Livraisons livraison = livraisonService.livraisonById(idLivraison)
                    .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée"));

            // Utiliser la projection du repository
            List<OfProjectQteRestDto> ordresFabrication = ordreFabricationRepository
                    .findOfProjectQteRestByProjetId(livraison.getProjet().getId());

            return ResponseEntity.ok(ordresFabrication);

        } catch (Exception e) {
            System.err.println("Erreur getAllOFByLivraison: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }
}