package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.DetailLivraisonRepository;
import ma.gap.repository.OrdreFabricationRepository;
import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import ma.gap.service.DetailLivraisonService;
import ma.gap.service.LivraisonService;
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
public class DetailLivraisonController{
        private DetailLivraisonService detailLivraisonService;
        private LivraisonService livraisonService;
        private OrdreFabricationRepository ordreFabricationRepository;


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
            return new ResponseEntity<>("Erreur lors de l'ajout du détail: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*    @GetMapping(value = "/DetailLivraisons/Tous")
        @PreAuthorize("hasAnyAuthority('admin','agentSaisie','consulteur')")
        public String findAllLivraisons(Model model, @RequestParam(value = "page") Optional<Integer> page,
                                        @RequestParam(value = "size" ) Optional<Integer> size,@PathVariable Long id){

           model.addAttribute("detail",detailLivraisonService.allDetailsLivraisons(id));
            return "Livraisons/ListeLivraisons";
        }*/
   /*     @GetMapping(value = "/DetailLivraisons/Tous/{id}")
        @PreAuthorize("hasAnyAuthority('admin','agentSaisie','consulteur')")
        public String findLivraison(Model model,@RequestParam(value = "page") Optional<Integer> page,
                                    @RequestParam(value = "size" ) Optional<Integer> size){

            // A developper
            return "redirect:/DetailLivraisons/Tous";
        }*/




    @GetMapping("Livraison/ListeDetailByBL/{idLivraison}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie','consulteur')")
    public List<DetailLivraison> listeDetailsByBL(@PathVariable Long idLivraison){


Livraisons livraisons=livraisonService.livraisonById(idLivraison).get();

        return detailLivraisonService.findAllBylivraison(livraisons);
    }
/*       @PostMapping(value = "DetailLivraisons/Editer/{id}")
        @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
        public String editLivraison(Model model,@PathVariable("id") long id, DetailLivraison detailLivraison) throws OrdreFabricationNotFoundException, IOException, ArticleNotFoundException {
	       DetailLivraison dl=detailLivraisonService.detailLivraisonById(id).get();
           detailLivraison.setImprime(dl.getImprime());
	        detailLivraison.setId(id);
	        detailLivraisonService.editDetailLivraison(detailLivraison);
	        return addDetailLivraison(model,detailLivraison.getLivraison().getId());
        }*/

    @PutMapping(value = "/Editer/{id}")
    public ResponseEntity<?> updateDetail(@PathVariable Long id, @Valid @RequestBody DetailLivraison detail) {
        try {
            // Vérification de cohérence des IDs
            if (!id.equals(detail.getId())) {
                return ResponseEntity.badRequest().body("L'ID dans l'URL ne correspond pas à l'ID du détail");
            }

            DetailLivraison updatedDetail = detailLivraisonService.editDetailLivraison( detail);
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






/* @PostMapping(value = "/of/miseajour/")
    public String miseajourof(@RequestParam("idof") String idof ) throws OrdreFabricationNotFoundException, IOException {
    List<String> columnData = new ArrayList<>();

            FileInputStream fileInputStream = new FileInputStream("C:\\Users\\richebois\\Desktop\\amigosservices\\ListeOFCopie.xls");
            Workbook workbook = new HSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cell = row.getCell(0);
                String cellValue = "";

                if (cell != null) {
                    switch (cell.getCellType()) {
                        case STRING:
                            cellValue = cell.getStringCellValue();
                            break;
                        default :

                            break;
                        // Ajoutez d'autres types de cellules si nécessaire (BOOLEAN, FORMULA, etc.)
                    }
                }

                columnData.add(cellValue);
            }

            workbook.close();
            fileInputStream.close();



        return "redirect:/of/saisiemiseajour";
}*/



}




