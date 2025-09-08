package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.DetailOF;
import ma.gap.service.DetailOFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/detail-of")
public class DetailOFController {

    @Autowired
    private DetailOFService detailOFService;

    @PostMapping("/add/{ofId}")
    public ResponseEntity<DetailOF> createDetailOF(
            @PathVariable Long ofId,
            @RequestBody DetailOF detailOF) {
        DetailOF createdDetailOF = detailOFService.createDetailOF(ofId, detailOF);
        return ResponseEntity.ok(createdDetailOF);
    }
   /* @GetMapping("/of/{ofId}")
    public ResponseEntity<List<DetailOF>> getDetailsByOfId(@PathVariable Long ofId) {
        List<DetailOF> details = detailOFService.getDetailsByOfId(ofId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/deliver")
    public ResponseEntity<String> deliverOfOrSubOf(@RequestBody DeliveryRequest deliveryRequest) {
        detailOFService.deliverOfOrSubOf(deliveryRequest);
        return ResponseEntity.ok("Livraison effectuée avec succès");
    }*/
}
