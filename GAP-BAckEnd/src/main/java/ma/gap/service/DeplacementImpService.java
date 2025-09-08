package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.config.GlobalVariableConfig;
import ma.gap.entity.*;
import ma.gap.enums.StatutEntity;
import ma.gap.repository.DeplacementRepository;
import ma.gap.repository.DeplacementSearchDao;
import ma.gap.repository.EmployeeRepository;
import ma.gap.repository.ProjetRepository;
import ma.gap.exceptions.OrdreMissionNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DeplacementImpService implements DeplacementService {

    private DeplacementRepository deplacementRepository;
    private UserImpService userImpService;
    private EmployeeRepository employeeRepository;
    private ProjetRepository projetRepository;
    private DeplacementSearchDao deplacementSearchDao;
    private GlobalVariableConfig globalVariableConfig;
    private FilesStorageService filesStorageService;

    @Override
    public Deplacement getById(long id){
        return deplacementRepository.findById(id).get();
    }

    @Override
    public List<Deplacement> allDeplacement(long idUser) {
        User user = userImpService.findbyusername(idUser);
        List<Role> roles = user.getRoles();
        List<Ateliers> ateliers = user.getAteliers();
        List<Employee> employeeList = new ArrayList<>();
        for (Role role : roles){
            if (role.getName().equals("agentSaisie")){
            	for(Ateliers atelier: ateliers)
            	{
	            	employeeList.addAll(employeeRepository.findAllByAteliers(atelier, Sort.by(Sort.Direction.ASC, "nom")));
				}
                
                List<Deplacement> deplacementList = deplacementRepository.findDistinctByEmployeeInOrderByIdDesc(employeeList);
                return deplacementList;
            } 
            else 
            {
                return deplacementRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
            }
    }
        return null;
    }

    @Override
    public Deplacement saveDeplacement(Deplacement deplacement) {

        deplacement.setFlag(StatutEntity.SAISI.valeur);
        return deplacementRepository.save(deplacement);
    }

    @Override
    public Deplacement editDeplacement(Deplacement deplacement,long id) {
        deplacement.setId(id);
        deplacement.setFlag(StatutEntity.SAISI.valeur);

        return deplacementRepository.save(deplacement);
    }
    @Override
    public boolean deleteDeplacement(Long id) throws IOException {

        Optional<Deplacement> dp = deplacementRepository.findById(id);
        if (dp.get().getFlag() == StatutEntity.SAISI.valeur) {
            if (dp.get().getPieceJointe() != null) {
                filesStorageService.delete(dp.get().getPieceJointe());
            }

        }
        deplacementRepository.deleteById(id);
        return true;

    }
    @Override
	public ResponseEntity<byte[]> generateOm(Long id) throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException, OrdreMissionNotFoundException {	
		
		
		Optional<Deplacement> om = deplacementRepository.findById(id); 
		Resource resource = new ClassPathResource("files/OrdreMission.jrxml");
		
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(om.get()));
        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));

        HashMap<String, Object> map = new HashMap<>();
        map.put("creer_par",om.get().getCreatedBy());
        map.put("nom_prenom",om.get().getEmployee().get(0).getNom()+' '+om.get().getEmployee().get(0).getPrenom());
        map.put("fonction",om.get().getEmployee().get(0).getFonction().getDesignation());
        
        JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
       
        byte[] data = JasperExportManager.exportReportToPdf(report);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Ordre_Mission_"+om.get().getId()+".pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
	}

    @Override
    public List<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif, String dateDebut, String dateFin) throws ParseException {
        return deplacementSearchDao.searchDeplacement( idUser,  idemploye,  idprojet,  idatelier,  motif,  dateDebut,  dateFin);
    }
}
