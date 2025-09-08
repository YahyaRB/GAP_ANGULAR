package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.LivraisonSearchDao;
import ma.gap.repository.LivraisonsRepository;
import ma.gap.repository.*;
import ma.gap.dtos.EmployeeDTO;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import ma.gap.service.*;
import net.sf.jasperreports.engine.JRException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@AllArgsConstructor@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/livraison")

public class LivraisonController {
    private LivraisonService livraisonService;
    private AtelierService atelierService;
    private LivraisonsRepository livraisonsRepository;
    private ProjetService projetService;
    private LivraisonSearchDao livraisonSearchDao;
    private ChauffeurService chauffeurService;
    private UserImpService userImpService;
    private DetailLivraisonRepository detailLivraisonRepository;
    private DetailLivraisonService detailLivraisonService;
    @GetMapping(value = "/getAll/{idUser}")
    public ResponseEntity<?> findAllLivraisons(@PathVariable("idUser") long idUser) {
        try {
            User user = userImpService.finduserById(idUser);
            boolean isAdminOrLogistique = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur"));

            if (isAdminOrLogistique) {
                return ResponseEntity.ok(livraisonsRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
            } else {
                List<Livraisons> livraisons = livraisonService.allLivraisonsByAtelierWithPagination(user.getAteliers());
                return ResponseEntity.ok(livraisons);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des livraisons.");
        }
    }


    @PostMapping(value = "/add")
    //@PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<?> saveLivraison(@RequestBody Livraisons livraison) {
        try {
            livraison.setChauffeur(null);
            Livraisons savedLivraison = livraisonService.saveLivraison(livraison);
            return ResponseEntity.status(HttpStatus.CREATED).body("Livraison enregistrée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }


    @PutMapping(value = "/update/{id}")
    //@PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<String> editLivraison(@PathVariable("id") long id, @RequestBody Livraisons livraison) {
        try {
            Optional<Livraisons> optionalLivraison = livraisonService.livraisonById(id);
            if (!optionalLivraison.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livraison non trouvée. Édition impossible.");
            }

            Livraisons existingLivraison = optionalLivraison.get();
            livraison.setProjet(existingLivraison.getProjet());
            livraison.setAtelier(existingLivraison.getAtelier());
            livraison.setChauffeur(existingLivraison.getChauffeur());

            livraisonService.editLivraison(livraison, id);
            return ResponseEntity.ok("Livraison éditée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping(value = "/delete/{id}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<String> deleteLivraison(@PathVariable("id") long id) {
        try {
            livraisonService.deleteLivraison(id);
            return ResponseEntity.ok("Livraison supprimée avec succès.");
        } catch (OrdreFabricationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livraison non trouvée. Suppression impossible.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de la livraison.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping("/Search")
    public List<Livraisons> SearchLivraisons(@RequestParam("idUser") long idUser, @RequestParam("idprojet") long idprojet, @RequestParam("chauffeur") long idchauffeur, @RequestParam("atelier") long idatelier, @RequestParam("dateDebut") String dateDebut, @RequestParam("dateFin") String dateFin) throws ParseException {

        List<Livraisons> listeLivraisons = livraisonSearchDao.searchLivraison(idUser, idchauffeur, idprojet, idatelier, dateDebut, dateFin);
        // Pour le tri (Order by desc)
        Collections.reverse(listeLivraisons);

        return listeLivraisons;
    }

    @GetMapping("AffectationChauffeur/Search")
    public List<Livraisons> SearchAffectationChauffeur(Model model, @RequestParam(value = "page") Optional<Integer> page,
                                                       @RequestParam(value = "size") Optional<Integer> size, @RequestParam("idprojet") long idprojet, @RequestParam("idatelier") long idatelier, @RequestParam("affectation") String affectation, @RequestParam("dateDebut") String dateDebut, @RequestParam("dateFin") String dateFin) throws ParseException {
        List<Livraisons> livraisons = livraisonSearchDao.searchLivraisonAffectation(idprojet, idatelier, affectation, dateDebut, dateFin);
        // Pour le tri (Order by desc)
        Collections.reverse(livraisons);


        return livraisons;
    }

    @GetMapping("/byAtelier/{atelierId}")
    public ResponseEntity<List<Projet>> getAffairesByAtelier(@PathVariable Long atelierId) {
        // Récupérer la liste des affaires associées à un atelier donné
        List<Projet> affaires = livraisonsRepository.findAffairesWithLivraisonByAtelier(atelierId);

        if (affaires.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retourner une réponse vide si aucune affaire n'est trouvée
        }

        return ResponseEntity.ok(affaires); // Retourner les affaires trouvées
    }

    @PutMapping(value = "/AffectationChauffeur/{id}")
    //@PreAuthorize("hasAnyAuthority('admin','logistique')")
    public ResponseEntity<String> AffectationLivraison(@PathVariable("id") long id, @RequestBody Livraisons livraison) {
        try {
            if (!Objects.isNull(livraison)) {
                Livraisons liv = livraisonService.livraisonById(id).get();
                liv.setChauffeur(livraison.getChauffeur());
                livraisonService.editLivraison(liv, id);
                return ResponseEntity.ok("Chauffeur affecté avec succès.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livraison non trouvée. Édition impossible.");

            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping("/imprimer/{id}")
    //@PreAuthorize("hasAnyAuthority('admin','agentSaisie','logistique')")
    public ResponseEntity<byte[]> impressionLivraison(@PathVariable("id") Long id) throws JRException, IOException, OrdreFabricationNotFoundException {
        return detailLivraisonService.impLivraison(id);
    }
    @GetMapping("/getAllOFByLivraison/{idLivraison}")
    // @PreAuthorize("hasAnyAuthority('admin','agentSaisie','consulteur')")
    public List<OfProjectQteRestDto> getDetailLivraisonByProjet(@PathVariable Long idLivraison) {

        try {
            // Récupérer la livraison une seule fois
            Livraisons livraison = livraisonService.livraisonById(idLivraison)
                    .orElseThrow(() -> new EntityNotFoundException("Livraison non trouvée avec l'ID: " + idLivraison));

            Long projetId = livraison.getProjet().getId();

            return detailLivraisonRepository.listeOfavecQteRestante(projetId);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    @GetMapping("/ArticleOf/Imprimer/{id}")
    @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public ResponseEntity<byte[]> impressionArticle(Model model,@PathVariable("id") Long id) throws JRException, IOException, OrdreFabricationNotFoundException, ArticleNotFoundException {
        DetailLivraison dl=detailLivraisonService.detailLivraisonById(id).get();
        dl.setImprime(true);
        dl.setId(id);
        detailLivraisonService.editDetailLivraison(dl);
        return detailLivraisonService.impArticle(id);
    }
    /*    @GetMapping("/AffectationLivraison/export")
    @PreAuthorize("hasAnyAuthority('admin')")
    public void exportAffectationToExcel(HttpServletResponse response, @RequestParam(value = "idprojet",defaultValue = "0") long idprojet,
                              @RequestParam(value = "idatelier",defaultValue = "0") long idatelier,@RequestParam("affectation") String affectation,
                              @RequestParam(value = "dateDebut",defaultValue = "1900-01-01") String dateDebut,@RequestParam(value = "dateFin",defaultValue = "2900-01-01") String dateFin) throws IOException, ParseException {

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        List<Livraisons> livraisons=livraisonSearchDao.searchLivraisonAffectation(idprojet,idatelier,affectation,dateDebut,dateFin);
        Collections.reverse(livraisons);
        // Créer un nouveau classeur Excel
        Workbook workbook = new HSSFWorkbook();

        // Créer une feuille dans le classeur
        Sheet sheet = workbook.createSheet("Feuille1");

        //font & Style
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short)12);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);




        // Créer une ligne pour les en-têtes de colonne
        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        cell0.setCellValue("Id");
        cell0.setCellStyle(style);
        Cell cell1 = headerRow.createCell(1);
        cell1.setCellValue("Date Livraison");
        cell1.setCellStyle(style);
        Cell cell2 = headerRow.createCell(2);
        cell2.setCellValue("Projet");
        cell2.setCellStyle(style);
        Cell cell3 = headerRow.createCell(3);
        cell3.setCellValue("Atelier");
        cell3.setCellStyle(style);
        Cell cell4 = headerRow.createCell(4);
        cell4.setCellValue("Chauffeur");
        cell4.setCellStyle(style);



        // Remplir les données dans les lignes suivantes
        int rowNum = 1;
        for (Livraisons rowData : livraisons) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData.getId());
            row.createCell(1).setCellValue(dateFormatter.format(rowData.getDateLivraison()));
            row.createCell(2).setCellValue(rowData.getProjet().getCode()+" - "+rowData.getProjet().getDesignation());
            row.createCell(3).setCellValue(rowData.getAtelier().getDesignation());
            if(rowData.getChauffeur()!=null)
            row.createCell(4).setCellValue(rowData.getChauffeur().getNom()+" "+rowData.getChauffeur().getPrenom());
            else
            row.createCell(4).setCellValue("");

            // Ajouter d'autres colonnes si nécessaire
        }

        // Définir le type de contenu de la réponse HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=listeLivraisons.xls");

        // Écrire le classeur Excel dans la réponse HTTP
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

    }*/
/*
    @GetMapping("/Livraison/export")
    @PreAuthorize("hasAnyAuthority('admin','EXPORT')")
    public void exportToExcel(HttpServletResponse response, @RequestParam(value="idUser") long idUser, @RequestParam(value="idprojet",defaultValue = "0") long idprojet, @RequestParam(value="chauff",defaultValue = "0") long idchauffeur,
                              @RequestParam(value="idatelier",defaultValue = "0") long idatelier, @RequestParam(value="dateDebut",defaultValue = "1900-01-01") String dateDebut,
                              @RequestParam(value="dateFin",defaultValue = "2900-01-01") String dateFin) throws IOException, ParseException {


        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        List<Livraisons> livraisons=livraisonSearchDao.searchLivraison(idUser,idchauffeur,idprojet,idatelier,dateDebut,dateFin);
        Collections.reverse(livraisons);
        // Créer un nouveau classeur Excel
        Workbook workbook = new HSSFWorkbook();

        // Créer une feuille dans le classeur
        Sheet sheet = workbook.createSheet("Feuille1");

        //font & Style
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short)12);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);




        // Créer une ligne pour les en-têtes de colonne
        Row headerRow = sheet.createRow(0);
        Cell cell0 = headerRow.createCell(0);
        cell0.setCellValue("Id");
        cell0.setCellStyle(style);
        Cell cell1 = headerRow.createCell(1);
        cell1.setCellValue("Date Livraison");
        cell1.setCellStyle(style);
        Cell cell2 = headerRow.createCell(2);
        cell2.setCellValue("Projet");
        cell2.setCellStyle(style);
        Cell cell3 = headerRow.createCell(3);
        cell3.setCellValue("Atelier");
        cell3.setCellStyle(style);
        Cell cell4 = headerRow.createCell(4);
        cell4.setCellValue("Chauffeur");
        cell4.setCellStyle(style);



        // Remplir les données dans les lignes suivantes
        int rowNum = 1;
        for (Livraisons rowData : livraisons) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData.getId());
            row.createCell(1).setCellValue(dateFormatter.format(rowData.getDateLivraison()));
            row.createCell(2).setCellValue(rowData.getProjet().getCode()+" - "+rowData.getProjet().getDesignation());
            row.createCell(3).setCellValue(rowData.getAtelier().getDesignation());
            if(rowData.getChauffeur()!=null)
                row.createCell(4).setCellValue(rowData.getChauffeur().getNom()+" "+rowData.getChauffeur().getPrenom());
            else
                row.createCell(4).setCellValue("");

            // Ajouter d'autres colonnes si nécessaire
        }

        // Définir le type de contenu de la réponse HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=listeLivraisons.xls");

        // Écrire le classeur Excel dans la réponse HTTP
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();

    }
}
*/

}

