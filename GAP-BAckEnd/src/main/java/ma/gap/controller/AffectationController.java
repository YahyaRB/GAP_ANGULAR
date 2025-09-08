package ma.gap.controller;


import lombok.AllArgsConstructor;
import ma.gap.dtos.AffectationPreviewDTO;
import ma.gap.dtos.AffectationRequestDTO;
import ma.gap.dtos.DuplicationRequestDTO;
import ma.gap.entity.*;
import ma.gap.repository.CustomAffectationRepository;
import ma.gap.dtos.EmployeeDTO;
import ma.gap.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/Affectation")
public class AffectationController {
    private AffectationUpImpService affectationImpService;
    private EmployeeImpService employeeImpService;
    private ProjetImpService projetImpService;
    private AtelierImpService atelierImpService;
    private ArticleImpService articleImpService;
    private CustomAffectationRepository customAffectationRepository;
    @GetMapping("/Liste/{idUser}")
    public List<AffectationUpdate> getlistAffectation(@PathVariable long idUser) {
        List<AffectationUpdate> listeAffectations = affectationImpService.allAffectation(idUser);
        return listeAffectations;
    }

    @PostMapping(value = "/add", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> saveAffectation(@RequestBody AffectationRequestDTO request) {
        String message = null;
            List<Employee> empIds = request.getEmployees();
         

            for (Employee emp : empIds) {
                AffectationUpdate af = new AffectationUpdate();


                af.setDate(request.getDate());
                af.setAteliers(request.getAteliers());
                af.setEmployees(emp);
                af.setPeriode(request.getPeriode());
                af.setNombreHeures(request.getNombreHeures());
                af.setProjets(request.getProjets());
                af.setArticle(request.getArticle());


                message = affectationImpService.saveAffectation(af);
                }

     return ResponseEntity.status(HttpStatus.CREATED).body(message);


    }

    @DeleteMapping("/delete/{id}")
   // @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public  ResponseEntity<String> deleteAffectation(@PathVariable("id") Long id) {
        try {

            affectationImpService.deleteAffectation(id);

            return ResponseEntity.status(HttpStatus.CREATED).body("Affectation supprimée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }

    }

    @PostMapping("/Editer/{id}")
    @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
    public String editAffectation(@PathVariable("id") Long id, AffectationUpdate aff) {
        affectationImpService.saveAffectation(aff);
        return "redirect:/Affectations/Liste";
    }

    @GetMapping("/Search")
        public List<AffectationUpdate> SearchAffec(@RequestParam("idUser") long idUser,@RequestParam("idprojet") long idprojet, @RequestParam("idemploye") long idemploye,@RequestParam("idatelier") long idatelier, @RequestParam("idarticle") long idarticle,
        @RequestParam("dateDebut") String dateDebut, @RequestParam("dateFin") String dateFin) throws ParseException {
        List<AffectationUpdate> affectationUpdates =affectationImpService.affectationFiltred( idUser, idprojet,  idemploye,  idarticle, idatelier,  dateDebut,  dateFin);

        return affectationUpdates;
    }
    @PostMapping("/duplicate")
   /* @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")*/
    public ResponseEntity<String> duplicateAffectations(@RequestBody DuplicationRequestDTO request) {
        try {
            String result = affectationImpService.duplicateAffectations(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la duplication: " + e.getMessage());
        }
    }
    @PostMapping("/duplicate/preview")
    /* @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")*/
    public List<AffectationPreviewDTO>  previewduplicateAffectations(@RequestBody DuplicationRequestDTO request) {

            return  affectationImpService.previewDuplication(request);

    }
    @PostMapping("/duplicate/save")
    /* @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")*/
    public ResponseEntity<String> saveduplicateAffectations(@RequestBody List<AffectationPreviewDTO> request) {
        try {
            String result = affectationImpService.saveDuplicatedAffectations(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur lors de la duplication: " + e.getMessage());
        }
    }
   /* @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin','EXPORT')")
    public void exportToExcel(HttpServletResponse response,@RequestParam("idUser") long idUser,@RequestParam("idprojet") long idprojet, @RequestParam("idemploye") long idemploye,@RequestParam("idatelier") long idatelier, @RequestParam("idarticle") long idarticle,
                              @RequestParam("dateDebut") String dateDebut, @RequestParam("dateFin") String dateFin){

        List<AffectationUpdate> affectations=customAffectationRepository.affectationFiltred( idUser, idprojet,  idemploye,  idarticle, idatelier,  dateDebut,  dateFin);
        Collections.reverse(affectations);
        // Créer un nouveau classeur Excel
        Workbook workbook = new HSSFWorkbook();

        // Créer une feuille dans le classeur
        Sheet sheet = workbook.createSheet("Affectations");

        // Créer un style pour la mise en page personnalisée
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        //font.setBold(true);
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
        cell1.setCellValue("Code");
        cell1.setCellStyle(style);
        Cell cell2 = headerRow.createCell(2);
        cell2.setCellValue("Projet");
        cell2.setCellStyle(style);
        Cell cell3 = headerRow.createCell(3);
        cell3.setCellValue("Atelier");
        cell3.setCellStyle(style);
        Cell cell4 = headerRow.createCell(4);
        cell4.setCellValue("Article");
        cell4.setCellStyle(style);
        Cell cell5 = headerRow.createCell(5);
        cell5.setCellValue("Date");
        cell5.setCellStyle(style);
        Cell cell6 = headerRow.createCell(6);
        cell6.setCellValue("Période");
        cell6.setCellStyle(style);
        Cell cell7 = headerRow.createCell(7);
        cell7.setCellValue("Nombre Heures");
        cell7.setCellStyle(style);
        Cell cell8 = headerRow.createCell(8);
        cell8.setCellValue("Matricule");
        cell8.setCellStyle(style);
        Cell cell9 = headerRow.createCell(9);
        cell9.setCellValue("Employé");
        cell9.setCellStyle(style);


        // Remplir les données dans les lignes suivantes
        int rowNum = 1;
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        for (AffectationUpdate rowData : affectations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData.getId());
            row.createCell(1).setCellValue(rowData.getProjets().getCode());
            row.createCell(2).setCellValue(rowData.getProjets().getDesignation());
            row.createCell(3).setCellValue(rowData.getAteliers().getDesignation());
            row.createCell(4).setCellValue(rowData.getArticle().getDesignation());
            row.createCell(5).setCellValue(dateFormatter.format(rowData.getDate()));
            row.createCell(6).setCellValue(rowData.getPeriode());
            row.createCell(7).setCellValue(rowData.getNombreHeures());
            row.createCell(8).setCellValue(rowData.getEmployees().getMatricule());
            row.createCell(9).setCellValue(rowData.getEmployees().getNom()+" "+rowData.getEmployees().getPrenom());
           // row.createCell(8).setCellFormula("CONCATENATE(A"+rowNum+",B"+rowNum+")");
            // Ajouter d'autres colonnes si nécessaire
        }

        // Définir le type de contenu de la réponse HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Affectations.xls");

        // Écrire le classeur Excel dans la réponse HTTP
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();


    }
*/

}

