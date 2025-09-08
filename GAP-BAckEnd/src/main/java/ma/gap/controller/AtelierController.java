package ma.gap.controller;

import lombok.AllArgsConstructor;
import ma.gap.entity.Ateliers;
import ma.gap.entity.User;
import ma.gap.service.AtelierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/atelier")
@AllArgsConstructor
public class AtelierController {
    private AtelierService atelierService;

    @GetMapping("/getAll/{idUser}")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public List<Ateliers> getAllAteliers(@PathVariable long idUser) {

        return atelierService.allAteliers(idUser);
    }
    @GetMapping("/searchById/{id}")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public Ateliers getAtelierById(@PathVariable long id){

        return atelierService.getAtelierById(id);
    }
    /*@GetMapping("/atelierByUser/{id}")
    //@PreAuthorize("hasAnyAuthority('admin')")
    public Ateliers getAtelierById(@PathVariable long id){

        return atelierService.;
    }*/
}


