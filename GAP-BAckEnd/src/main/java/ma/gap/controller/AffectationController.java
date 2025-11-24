package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.dtos.AffectationPreviewDTO;
import ma.gap.dtos.AffectationRequestDTO;
import ma.gap.dtos.DuplicationRequestDTO;
import ma.gap.entity.*;
import ma.gap.repository.AffectationUpdateRepository;
import ma.gap.repository.CustomAffectationRepository;
import ma.gap.dtos.EmployeeDTO;
import ma.gap.service.*;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private AffectationUpdateRepository affectationUpdateRepository;
    private CustomAffectationRepository customAffectationRepository;
    private AffectationDebugService debugService; // Nouveau service de debug

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
    public ResponseEntity<String> deleteAffectation(@PathVariable("id") Long id) {
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
    public ResponseEntity<Map<String, Object>> SearchAffec(
            @RequestParam("idUser") long idUser,
            @RequestParam("idprojet") long idprojet,
            @RequestParam("idemploye") long idemploye,
            @RequestParam("idatelier") long idatelier,
            @RequestParam("idarticle") long idarticle,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ParseException {

        Page<AffectationUpdate> affectationPage = affectationImpService.affectationFiltredPaginated(
                idUser, idprojet, idemploye, idarticle, idatelier, dateDebut, dateFin, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", affectationPage.getContent());
        response.put("currentPage", affectationPage.getNumber());
        response.put("totalItems", affectationPage.getTotalElements());
        response.put("totalPages", affectationPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/duplicate")
    public ResponseEntity<String> duplicateAffectations(@RequestBody DuplicationRequestDTO request) {
        try {
            String result = affectationImpService.duplicateAffectations(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors de la duplication: " + e.getMessage());
        }
    }

    @PostMapping("/duplicate/preview")
    public ResponseEntity<List<AffectationPreviewDTO>> previewDuplication(@RequestBody DuplicationRequestDTO request) {
        try {
            List<AffectationPreviewDTO> preview = affectationImpService.previewDuplication(request);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
    }

    @PostMapping("/duplicate/save")
    public ResponseEntity<String> saveduplicateAffectations(@RequestBody List<AffectationPreviewDTO> request) {
        try {
            String result = affectationImpService.saveDuplicatedAffectations(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors de la duplication: " + e.getMessage());
        }
    }

    // ================== ENDPOINTS DE DEBUG ==================

    /**
     * Endpoint pour diagnostiquer les problèmes de duplication
     */
    @GetMapping("/debug/duplication")
    public ResponseEntity<String> debugDuplication(
            @RequestParam Long atelierId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date sourceDate,
            @RequestParam(required = false, defaultValue = "Après-midi") String periodes) {

        try {
            List<String> periodesList = Arrays.asList(periodes.split(","));
            String diagnostic = debugService.debugDuplicationProblem(atelierId, sourceDate, periodesList);
            return ResponseEntity.ok(diagnostic);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du diagnostic: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour tester différentes approches de recherche
     */
    @GetMapping("/debug/search-test/{atelierId}")
    public ResponseEntity<Map<String, Object>> testSearchApproaches(
            @PathVariable Long atelierId,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date date) {

        Map<String, Object> results = new HashMap<>();

        try {
            // Test 1: Méthode actuelle
            List<AffectationUpdate> method1 = affectationUpdateRepository.findByAteliersIdAndDate(atelierId, date);
            results.put("method1_findByAteliersIdAndDate", method1.size());

            // Test 2: Recherche par période spécifique
            List<AffectationUpdate> method2 = affectationUpdateRepository.findByAteliersIdAndDateAndPeriodeIn(
                    atelierId, date, Arrays.asList("Après-midi", "Matin", "Heures", "Heures_Sup"));
            results.put("method2_withAllPeriodes", method2.size());

            // Test 3: Recherche avec période "Après-midi" uniquement
            List<AffectationUpdate> method3 = affectationUpdateRepository.findByAteliersIdAndDateAndPeriode(
                    atelierId, date, "Après-midi");
            results.put("method3_apresMiddiOnly", method3.size());

            // Détails des résultats
            if (!method3.isEmpty()) {
                List<Map<String, Object>> details = new ArrayList<>();
                for (AffectationUpdate aff : method3) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", aff.getId());
                    detail.put("periode", aff.getPeriode());
                    detail.put("date", new SimpleDateFormat("dd/MM/yyyy").format(aff.getDate()));
                    detail.put("employee", aff.getEmployees().getNom());
                    detail.put("heures", aff.getNombreHeures());
                    details.add(detail);
                }
                results.put("details_apresMiddi", details);
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            results.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(results);
        }
    }

    @GetMapping("/debug/affectations-by-atelier/{atelierId}")
    public ResponseEntity<String> debugAffectationsByAtelier(
            @PathVariable long atelierId,
            @RequestParam(required = false) String date) {

        try {
            String debugInfo = String.format(
                    "Atelier ID: %d%nDate: %s%nNombre d'affectations trouvées: [À implémenter]",
                    atelierId, date);

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }
}