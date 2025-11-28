package ma.gap.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import ma.gap.entity.CalculPerProjet;
import ma.gap.service.CalculService;
import ma.gap.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class CalculController {

    @Autowired
    private CalculService calculService;

    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/Calcul/CalculerParProjet")
    public ResponseEntity<List<CalculPerProjet>> doCalculPerProject(
            @RequestParam(name = "idUser", required = false) Long idUser,
            @RequestParam(name = "atelier") Long id,
            @RequestParam(name = "month") Integer month,
            @RequestParam(name = "year") Integer year) throws JsonProcessingException, ParseException {

        List<CalculPerProjet> list = calculService.getAffectationPerProject(id, month, year);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/Calcul/export/calculs")
    public ResponseEntity<InputStreamResource> exportCalculs(
            @RequestParam(name = "atelier") Long id,
            @RequestParam(name = "month") Integer month,
            @RequestParam(name = "year") Integer year) {

        List<CalculPerProjet> list = calculService.getAffectationPerProject(id, month, year);
        ByteArrayInputStream in = excelExportService.exportCalculsToExcel(list);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=calculs.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }

    @PostMapping("/Calcul/export/details")
    public ResponseEntity<InputStreamResource> exportDetails(@RequestBody CalculPerProjet calcul) {
        ByteArrayInputStream in = excelExportService.exportEmployeeDetailsToExcel(calcul);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=details_employes.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
