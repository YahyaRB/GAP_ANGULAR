package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.repository.AffectationUpdateRepository;
import ma.gap.repository.EmployeeRepository;
import ma.gap.repository.PersonelSearchDao;
import ma.gap.service.AtelierService;
import ma.gap.service.EmployeeImpService;
import ma.gap.service.FonctionService;
import ma.gap.service.UserImpService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employe")
public class EmployeeController {

    private EmployeeImpService employeeImpService;
    private FonctionService fonctionService;
    private AtelierService atelierService;
    private PersonelSearchDao personelSearchDao;
    private EmployeeRepository employeeRepository;
    private AffectationUpdateRepository affectationUpdateRepository;
    private UserImpService userImpService;

    @GetMapping("/getAll/{idUser}")
    public ResponseEntity<List<Employee>> getEmpList(@PathVariable long idUser) {
        List<Employee> employees = employeeImpService.allEmployee(idUser);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/getAllByAtelier/{idAtelier}")
    public ResponseEntity<List<Employee>> getEmpByAtelierList(@PathVariable long idAtelier) {
        Ateliers ateliers=atelierService.getAtelierById(idAtelier);
        List<Employee> employees = employeeImpService.findAllByAteliers(ateliers);
        return ResponseEntity.ok(employees);
    }

    @PostMapping(value = "/add")
    public ResponseEntity<String> save(@RequestBody Employee employe) {
        try {
            Integer countMatricule = Integer.valueOf(employeeRepository.countMatricule(employe.getMatricule()));
            if (countMatricule == 0) {
                employeeImpService.saveEmploye(employe);
                return ResponseEntity.status(HttpStatus.CREATED).body("Employé enregistré avec succès.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Matricule déjà enregistré. Ajout impossible.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<String> edit(@PathVariable("id") long id, @RequestBody Employee employe) {
        try {
            Optional<Employee> employeUpdated = Optional.ofNullable(employeeImpService.findById(id));
            if (!employeUpdated.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employé non trouvé. Edition impossible.");
            } else {
                employeeImpService.updateEmploye(employe, id);
                return ResponseEntity.status(HttpStatus.OK).body("Employé édité avec succès.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") long id) {
        try {
            employeeImpService.deleteEmploye(id);
            return ResponseEntity.ok("Employé supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
        }
    }

    @GetMapping("Search")
    public ResponseEntity<List<Employee>> searchEmployes(
            @RequestParam("idUser") long idUser,
            @RequestParam("matricule") String matricule,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("atelier") long atelier,
            @RequestParam("fonction") long fonction) throws ParseException {

        List<Employee> employees = personelSearchDao.searchEmploye(idUser, matricule, nom, prenom, atelier, fonction);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('admin','EXPORT')")
    public ResponseEntity<Void> exportToExcel(
            HttpServletResponse response,
            @RequestParam("idUser") long idUser,
            @RequestParam("matricule") String matricule,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("atelier") long atelier,
            @RequestParam("fonction") long fonction) {

        try {
            User user = userImpService.findbyusername(idUser);
            for (Role role : user.getRoles()) {
                if (!role.getName().equals("admin") && !role.getName().equals("consulteur")) {
                    for (Ateliers ateli : user.getAteliers()) {
                        atelier = ateli.getId();
                    }
                }
            }

            List<Employee> employees = personelSearchDao.searchEmploye(idUser, matricule, nom, prenom, atelier, fonction);
            Collections.reverse(employees);

            // Créer un nouveau classeur Excel
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet("Feuille1");

            // Style des cellules
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setItalic(true);
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 12);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // En-tête
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Id");
            headerRow.createCell(1).setCellValue("Matricule");
            headerRow.createCell(2).setCellValue("Nom");
            headerRow.createCell(3).setCellValue("Prénom");
            headerRow.createCell(4).setCellValue("Atelier");
            headerRow.createCell(5).setCellValue("Fonction");

            // Remplissage des données
            int rowNum = 1;
            for (Employee rowData : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowData.getId());
                row.createCell(1).setCellValue(rowData.getMatricule());
                row.createCell(2).setCellValue(rowData.getNom());
                row.createCell(3).setCellValue(rowData.getPrenom());
                row.createCell(4).setCellValue(rowData.getAteliers().getDesignation());
                row.createCell(5).setCellValue(rowData.getFonction().getDesignation());
            }

            // Envoi du fichier Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=Employes.xls");

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/existeByNomComplet")
    public ResponseEntity<Boolean> existeByNomComplet(@RequestParam String nom, @RequestParam String prenom) {
        boolean exists = employeeImpService.existeByNomComplet(nom, prenom);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/existeByMatricule/{matricule}")
    public ResponseEntity<Boolean> existeByMatricule(@PathVariable String matricule) {
        boolean exists = employeeImpService.existeByMatricule(matricule);
        return ResponseEntity.ok(exists);
    }
}
