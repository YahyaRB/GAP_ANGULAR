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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public Deplacement getById(long id) {
        Optional<Deplacement> optionalDeplacement = deplacementRepository.findById(id);
        if (!optionalDeplacement.isPresent()) {
            throw new EmptyResultDataAccessException("Déplacement non trouvé avec l'ID : " + id, 1);
        }
        return optionalDeplacement.get();
    }

    @Override
    public List<Deplacement> allDeplacement(long idUser) {
        User user = userImpService.findbyusername(idUser);
        List<Role> roles = user.getRoles();
        List<Ateliers> ateliers = user.getAteliers();
        List<Employee> employeeList = new ArrayList<>();
        for (Role role : roles) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atelier : ateliers) {
                    employeeList
                            .addAll(employeeRepository.findAllByAteliers(atelier, Sort.by(Sort.Direction.ASC, "nom")));
                }

                List<Deplacement> deplacementList = deplacementRepository
                        .findDistinctByEmployeeInOrderByIdDesc(employeeList);
                return deplacementList;
            } else {
                return deplacementRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            }
        }
        return null;
    }

    @Override
    public Page<Deplacement> allDeplacement(long idUser, Pageable pageable) {
        User user = userImpService.findbyusername(idUser);
        List<Role> roles = user.getRoles();
        List<Ateliers> ateliers = user.getAteliers();
        List<Employee> employeeList = new ArrayList<>();
        for (Role role : roles) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atelier : ateliers) {
                    employeeList
                            .addAll(employeeRepository.findAllByAteliers(atelier, Sort.by(Sort.Direction.ASC, "nom")));
                }

                return deplacementRepository.findDistinctByEmployeeIn(employeeList, pageable);
            } else {
                return deplacementRepository.findAll(pageable);
            }
        }
        return Page.empty();
    }

    @Override
    public Deplacement saveDeplacement(Deplacement deplacement) {
        deplacement.setFlag(StatutEntity.SAISI.valeur);
        return deplacementRepository.save(deplacement);
    }

    @Override
    public Deplacement editDeplacement(Deplacement deplacement, long id) {
        deplacement.setId(id);
        deplacement.setFlag(StatutEntity.SAISI.valeur);
        return deplacementRepository.save(deplacement);
    }

    @Override
    public boolean deleteDeplacement(Long id) throws IOException {
        Optional<Deplacement> dp = deplacementRepository.findById(id);
        if (dp.isPresent() && dp.get().getFlag() == StatutEntity.SAISI.valeur) {
            if (dp.get().getPieceJointe() != null) {
                filesStorageService.delete(dp.get().getPieceJointe());
            }
        }
        deplacementRepository.deleteById(id);
        return true;
    }

    @Override
    public ResponseEntity<byte[]> generateOm(Long id) throws JRException, FileNotFoundException, IOException,
            EmptyResultDataAccessException, OrdreMissionNotFoundException {

        // Vérification de l'existence du déplacement
        Optional<Deplacement> omOptional = deplacementRepository.findById(id);
        if (!omOptional.isPresent()) {
            throw new OrdreMissionNotFoundException("Déplacement non trouvé avec l'ID : " + id);
        }

        Deplacement om = omOptional.get();

        // Vérifications des données critiques
        if (om.getEmployee() == null || om.getEmployee().isEmpty()) {
            throw new OrdreMissionNotFoundException("Aucun employé associé au déplacement ID : " + id);
        }

        Employee firstEmployee = om.getEmployee().get(0);
        if (firstEmployee == null) {
            throw new OrdreMissionNotFoundException("Données employé manquantes pour le déplacement ID : " + id);
        }

        // Vérification du template JasperReports
        Resource resource = new ClassPathResource("files/OrdreMission.jrxml");
        if (!resource.exists()) {
            throw new FileNotFoundException(
                    "Le fichier template OrdreMission.jrxml est introuvable dans resources/files/");
        }

        try {
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(
                    Collections.singleton(om));

            // Compilation du rapport avec gestion d'erreur
            JasperReport compileReport;
            try {
                compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));
            } catch (Exception e) {
                throw new JRException("Erreur lors de la compilation du template JasperReports : " + e.getMessage(), e);
            }

            // Préparation des paramètres avec vérification des valeurs nulles
            HashMap<String, Object> map = new HashMap<>();
            map.put("creer_par", om.getCreatedBy() != null ? om.getCreatedBy() : "Non défini");

            String nomPrenom = "";
            if (firstEmployee.getNom() != null) {
                nomPrenom += firstEmployee.getNom();
            }
            if (firstEmployee.getPrenom() != null) {
                if (!nomPrenom.isEmpty())
                    nomPrenom += " ";
                nomPrenom += firstEmployee.getPrenom();
            }
            if (nomPrenom.isEmpty()) {
                nomPrenom = "Nom non défini";
            }
            map.put("nom_prenom", nomPrenom);

            String fonction = "";
            if (firstEmployee.getFonction() != null && firstEmployee.getFonction().getDesignation() != null) {
                fonction = firstEmployee.getFonction().getDesignation();
            } else {
                fonction = "Fonction non définie";
            }
            map.put("fonction", fonction);

            // Génération du rapport
            JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

            if (report.getPages() == null || report.getPages().isEmpty()) {
                throw new JRException("Le rapport généré est vide. Vérifiez les données et le template.");
            }

            byte[] data = JasperExportManager.exportReportToPdf(report);

            if (data == null || data.length == 0) {
                throw new JRException("Erreur lors de l'export PDF : données vides");
            }

            HttpHeaders headers = new HttpHeaders();
            String filename = String.format("Ordre_Mission_%d_%s.pdf",
                    om.getId(),
                    StringUtils.cleanPath(nomPrenom.replaceAll("[^a-zA-Z0-9\\s]", "_")));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + filename);

            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);

        } catch (JRException e) {
            throw new JRException("Erreur JasperReports lors de la génération du PDF : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif,
            String dateDebut, String dateFin) throws ParseException {
        return deplacementSearchDao.searchDeplacement(idUser, idemploye, idprojet, idatelier, motif, dateDebut,
                dateFin);
    }

    @Override
    public Page<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif,
            String dateDebut, String dateFin, Pageable pageable) throws ParseException {
        return deplacementSearchDao.searchDeplacementPaginated(idUser, idemploye, idprojet, idatelier, motif, dateDebut,
                dateFin, pageable);
    }

    /**
     * Génère un PDF d'ordre de mission pour chaque employé du déplacement
     * Si un seul employé : retourne un seul PDF
     * Si plusieurs employés : retourne un fichier ZIP contenant un PDF par employé
     */
    @Override
    public ResponseEntity<byte[]> generateOmForAllEmployees(Long id) throws JRException, FileNotFoundException,
            IOException, EmptyResultDataAccessException, OrdreMissionNotFoundException {

        Optional<Deplacement> deplacementOpt = deplacementRepository.findById(id);
        if (!deplacementOpt.isPresent()) {
            throw new OrdreMissionNotFoundException("Déplacement non trouvé avec l'ID: " + id);
        }

        Deplacement deplacement = deplacementOpt.get();
        List<Employee> employees = deplacement.getEmployee();

        // Si un seul employé, générer un seul PDF
        if (employees == null || employees.isEmpty()) {
            throw new OrdreMissionNotFoundException("Aucun employé trouvé pour ce déplacement");
        }

        if (employees.size() == 1) {
            return generateOm(id);
        }

        // Si plusieurs employés, créer un ZIP avec un PDF par employé
        Resource resource = new ClassPathResource("files/OrdreMission.jrxml");
        if (!resource.exists()) {
            throw new FileNotFoundException(
                    "Le fichier template OrdreMission.jrxml est introuvable dans resources/files/");
        }

        JasperReport compileReport;
        try {
            compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));
        } catch (Exception e) {
            throw new JRException("Erreur lors de la compilation du template : " + e.getMessage(), e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        try {
            for (int i = 0; i < employees.size(); i++) {
                Employee employee = employees.get(i);

                if (employee == null) {
                    continue; // Ignorer les employés null
                }

                // Créer une collection avec le déplacement pour JasperReports
                JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(
                        Collections.singleton(deplacement));

                // Préparer les paramètres pour ce PDF
                HashMap<String, Object> map = new HashMap<>();
                map.put("creer_par", deplacement.getCreatedBy() != null ? deplacement.getCreatedBy() : "Non défini");

                String nomPrenom = "";
                if (employee.getNom() != null) {
                    nomPrenom += employee.getNom();
                }
                if (employee.getPrenom() != null) {
                    if (!nomPrenom.isEmpty())
                        nomPrenom += " ";
                    nomPrenom += employee.getPrenom();
                }
                if (nomPrenom.isEmpty()) {
                    nomPrenom = "Employe_" + (i + 1);
                }
                map.put("nom_prenom", nomPrenom);

                String fonction = "";
                if (employee.getFonction() != null && employee.getFonction().getDesignation() != null) {
                    fonction = employee.getFonction().getDesignation();
                } else {
                    fonction = "Fonction non définie";
                }
                map.put("fonction", fonction);

                // Générer le PDF pour cet employé
                JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
                byte[] pdfData = JasperExportManager.exportReportToPdf(report);

                if (pdfData != null && pdfData.length > 0) {
                    // Ajouter le PDF au ZIP avec un nom unique
                    String fileName = String.format("Ordre_Mission_%d_%s.pdf",
                            deplacement.getId(),
                            StringUtils.cleanPath(nomPrenom.replaceAll("[^a-zA-Z0-9\\s]", "_")));

                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    zos.write(pdfData);
                    zos.closeEntry();
                }
            }
        } finally {
            zos.close();
        }

        // Préparer la réponse HTTP
        byte[] zipData = baos.toByteArray();

        if (zipData.length == 0) {
            throw new JRException("Aucun PDF généré avec succès");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=Ordres_Mission_" + deplacement.getId() + ".zip");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().headers(headers).body(zipData);
    }
}