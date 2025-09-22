package ma.gap.controller;

import ma.gap.dtos.NomenclatureDTO;
import ma.gap.entity.Nomenclature;
import ma.gap.entity.OrdreFabrication;
import ma.gap.service.NomenclatureService;
import ma.gap.service.OrdreFabricationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nomenclatures")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NomenclatureController {

    @Autowired
    private NomenclatureService nomenclatureService;

    @Autowired
    private OrdreFabricationService ordreFabricationService;

    @PostMapping("/save/{ofId}")
    public ResponseEntity<String> ajouterNomenclature(@RequestBody Map<String, Object> requestData, @PathVariable Long ofId) {
        try {
            System.out.println("=== AJOUT NOMENCLATURE ===");
            System.out.println("Données reçues: " + requestData);

            // Créer une nouvelle nomenclature
            Nomenclature nomenclature = new Nomenclature();
            nomenclature.setType(ma.gap.enums.TypeNomenclature.valueOf((String) requestData.get("type")));
            nomenclature.setDesignation((String) requestData.get("designation"));
            nomenclature.setUnite((String) requestData.get("unite"));

            // CORRECTION : Mapper correctement quantite -> quantiteTot
            Double quantite = ((Number) requestData.get("quantite")).doubleValue();
            nomenclature.setQuantiteTot(quantite);

            nomenclature.setQuantiteLivre(0.0);
            nomenclature.setQuantiteRest(quantite);

            System.out.println("Nomenclature à sauvegarder - quantiteTot: " + quantite);

            nomenclatureService.save(nomenclature, ofId);
            return new ResponseEntity<>("Nomenclature ajoutée avec succès", HttpStatus.CREATED);

        } catch (Exception e) {
            System.err.println("=== ERREUR AJOUT ===");
            e.printStackTrace();
            return new ResponseEntity<>("Erreur lors de l'ajout: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * NOUVELLE MÉTHODE PUT pour mettre à jour une nomenclature
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNomenclature(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("=== MISE À JOUR NOMENCLATURE ===");
            System.out.println("ID: " + id);
            System.out.println("Données reçues: " + requestData);

            // Vérifier que la nomenclature existe
            Optional<Nomenclature> nomenclatureOptional = nomenclatureService.findById(id);
            if (!nomenclatureOptional.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Nomenclature non trouvée");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Nomenclature existingNomenclature = nomenclatureOptional.get();

            // Extraire et valider les données
            Double nouvelleQuantiteTotale = ((Number) requestData.get("quantite")).doubleValue();

            if (nouvelleQuantiteTotale <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "La quantité totale doit être positive");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Vérifier la cohérence avec les livraisons existantes
            double quantiteLivree = existingNomenclature.getQuantiteLivre();
            if (nouvelleQuantiteTotale < quantiteLivree) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", String.format("La quantité totale (%.2f) ne peut pas être inférieure à la quantité déjà livrée (%.2f)",
                        nouvelleQuantiteTotale, quantiteLivree));
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            // Mettre à jour les champs
            existingNomenclature.setType(ma.gap.enums.TypeNomenclature.valueOf((String) requestData.get("type")));
            existingNomenclature.setDesignation((String) requestData.get("designation"));
            existingNomenclature.setUnite((String) requestData.get("unite"));
            existingNomenclature.setQuantiteTot(nouvelleQuantiteTotale);

            // Recalculer la quantité restante
            existingNomenclature.setQuantiteRest(nouvelleQuantiteTotale - quantiteLivree);

            // SOLUTION TEMPORAIRE : Définir manuellement les champs d'audit pour éviter la NullPointerException
            try {
                existingNomenclature.setLastModifiedBy("system");
                existingNomenclature.setLastModifiedDate(new java.util.Date());
            } catch (Exception auditException) {
                System.err.println("Erreur d'audit ignorée: " + auditException.getMessage());
            }

            System.out.println("=== AVANT SAUVEGARDE ===");
            System.out.println("quantiteTot: " + existingNomenclature.getQuantiteTot());
            System.out.println("quantiteLivre: " + existingNomenclature.getQuantiteLivre());
            System.out.println("quantiteRest: " + existingNomenclature.getQuantiteRest());

            // Sauvegarder avec gestion d'erreur spécifique
            try {
                nomenclatureService.save(existingNomenclature, existingNomenclature.getOrdreFabrication().getId());

                // CORRECTION : Retourner du JSON au lieu d'un String
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Nomenclature mise à jour avec succès");
                successResponse.put("data", existingNomenclature.getId());

                return new ResponseEntity<>(successResponse, HttpStatus.OK);

            } catch (org.springframework.transaction.TransactionSystemException ex) {
                System.err.println("Erreur de transaction (probablement audit): " + ex.getMessage());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Erreur d'audit lors de la sauvegarde. Contactez l'administrateur.");
                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("=== ERREUR MISE À JOUR ===");
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Récupère toutes les nomenclatures d'un ordre de fabrication spécifique
     */
    @GetMapping("/by-of/{ofId}")
    public ResponseEntity<List<NomenclatureDTO>> getNomenclaturesByOF(@PathVariable Long ofId) {
        try {
            List<NomenclatureDTO> nomenclatures = nomenclatureService.getNomenclaturesByOF(ofId);

            if (nomenclatures.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(nomenclatures);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nomenclatures pour OF " + ofId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une nomenclature par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NomenclatureDTO> getNomenclatureById(@PathVariable Long id) {
        try {
            NomenclatureDTO nomenclature = nomenclatureService.getNomenclatureById(id);

            if (nomenclature == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(nomenclature);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la nomenclature " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une nomenclature
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNomenclature(@PathVariable Long id) {
        try {
            System.out.println("=== SUPPRESSION NOMENCLATURE ===");
            System.out.println("ID à supprimer: " + id);

            // Vérifier que la nomenclature existe
            Optional<Nomenclature> nomenclatureOptional = nomenclatureService.findById(id);
            if (!nomenclatureOptional.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Nomenclature non trouvée");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Nomenclature nomenclature = nomenclatureOptional.get();

            // Vérifier qu'aucune quantité n'a été livrée
            if (nomenclature.getQuantiteLivre() > 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Impossible de supprimer une nomenclature qui a déjà des livraisons");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            System.out.println("Suppression de la nomenclature: " + nomenclature.getDesignation());
            nomenclatureService.deleteById(id);

            // CORRECTION : Retourner du JSON au lieu d'un String
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Nomenclature supprimée avec succès");
            successResponse.put("deletedId", id);

            return new ResponseEntity<>(successResponse, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("=== ERREUR SUPPRESSION ===");
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Met à jour une livraison pour une nomenclature
     */
    @PatchMapping("/{id}/livrer")
    public ResponseEntity<String> livrerNomenclature(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        try {
            Double quantiteLivre = request.get("quantiteLivre");

            // Valider les paramètres
            if (quantiteLivre == null || quantiteLivre <= 0) {
                return new ResponseEntity<>("La quantité à livrer doit être positive", HttpStatus.BAD_REQUEST);
            }

            // Vérifier que la nomenclature existe
            Optional<Nomenclature> nomenclatureOptional = nomenclatureService.findById(id);
            if (!nomenclatureOptional.isPresent()) {
                return new ResponseEntity<>("Nomenclature non trouvée", HttpStatus.NOT_FOUND);
            }

            Nomenclature nomenclature = nomenclatureOptional.get();

            // Vérifier qu'il y a assez de quantité disponible
            if (quantiteLivre > nomenclature.getQuantiteRest()) {
                return new ResponseEntity<>(
                        String.format("Quantité insuffisante. Disponible: %.2f, Demandée: %.2f",
                                nomenclature.getQuantiteRest(), quantiteLivre),
                        HttpStatus.BAD_REQUEST);
            }

            // Mettre à jour les quantités
            nomenclatureService.updateQuantitiesAfterDelivery(id, quantiteLivre);

            return new ResponseEntity<>("Livraison enregistrée avec succès", HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("Erreur lors de la livraison de la nomenclature " + id + ": " + e.getMessage());
            return new ResponseEntity<>("Erreur lors de la livraison: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}