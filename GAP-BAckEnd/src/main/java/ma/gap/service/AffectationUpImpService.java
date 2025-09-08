package ma.gap.service;


import lombok.AllArgsConstructor;
import ma.gap.dtos.AffectationPreviewDTO;
import ma.gap.dtos.DuplicationRequestDTO;
import ma.gap.entity.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ma.gap.repository.*;
import ma.gap.repository.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class AffectationUpImpService implements AffectationUpService {

    private CustomAffectationRepository customAffectationRepository;
    private AffectationUpdateRepository affectationRepository;
    private UserImpService userImpService;
    public JavaMailSender emailSender;
    private EmployeeService employeeService;
    private AtelierService atelierService;
    private ProjetService projetService;
   private ArticleService articleService;

    @Override
    public List<AffectationUpdate> allAffectation(long idUser) {
        User user = userImpService.findbyusername(idUser);
        List<Ateliers> ateliers = user.getAteliers();
        List<AffectationUpdate> affectationList = new ArrayList<>();
        List<Role> roles = user.getRoles();
        for (Role role : roles){
            if (role.getName().equals("agentSaisie")){
            	for(Ateliers atelier: ateliers)
            	{
	            	
	            	affectationList.addAll(affectationRepository.findAllByAteliersOrderByIdDesc(atelier));
				}
                
                return affectationList;
            }else {
                List<AffectationUpdate> affectations = affectationRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
                return affectations;
            }
        }
        return null;

    }

    @Override
    public String saveAffectation(AffectationUpdate affectation) {
        final Employee employee = affectation.getEmployees();
        final Date date = affectation.getDate();

        String periode = affectation.getPeriode();
        periode = (periode == null) ? null : periode.trim();

        // normaliser "Heures_Sup" (gère variantes saisies)
        boolean isOvertime = false;
        if (periode != null) {
            String p = periode.replace('-', '_').replace(' ', '_');
            isOvertime = p.equalsIgnoreCase("Heures_Sup") || p.equalsIgnoreCase("HeuresSup");
            if (isOvertime) {
                affectation.setPeriode("Heures_Sup");
            }
        }

        int totalActuel = affectationRepository.totalHeures(employee, date);

        // ❗ On bloque à 9h SEULEMENT pour les périodes normales (pas pour Heures_Sup)
        if (totalActuel >= 9 && !isOvertime) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L’employé a déjà 9h planifiées ce jour.");
        }

        Integer heures = affectation.getNombreHeures();

        if ("Matin".equalsIgnoreCase(periode)) {
            if (affectationRepository.countAllByEmployeesAndDateAndPeriode(employee, date, "Matin") > 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà affecté le matin pour cette date.");
            affectation.setPeriode("Matin");
            heures = 5; affectation.setNombreHeures(5);

        } else if ("Après-midi".equalsIgnoreCase(periode) || "Apres-midi".equalsIgnoreCase(periode)) {
            if (affectationRepository.countAllByEmployeesAndDateAndPeriode(employee, date, "Après-midi") > 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà affecté l’après-midi pour cette date.");
            affectation.setPeriode("Après-midi");
            heures = 4; affectation.setNombreHeures(4);

        } else if (isOvertime) {
            // Heures Sup : autorisées > 9h, mais on valide quand même la saisie
            if (heures == null || heures <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veuillez saisir un nombre d'heures > 0.");
            // pas de plafond ici
            affectation.setPeriode("Heures_Sup");

        } else {
            // Saisie libre "normale" (sans période) : plafonnée à 9h et soumise à la limite journalière
            if (heures == null || heures <= 0 || heures > 9)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Heures invalides (1 à 9).");
            affectation.setPeriode("Heures");
        }

        // ❗ Ne vérifier la limite des 9h que pour le "normal", pas pour Heures_Sup
        if (!isOvertime && totalActuel + heures > 9) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Dépassement : il reste " + Math.max(0, 9 - totalActuel) + "h possibles pour cette date."
            );
        }

        affectationRepository.save(affectation);
        int totalFinal = totalActuel + heures;

        // Message différent si Heures_Sup (optionnel)
        String label = isOvertime ? "Affectation (Heures Sup)" : "Affectation";
        return label + " enregistrée (" + heures + "h). Total du jour : " + totalFinal + "h.";
    }

    /*public AffectationUpdate saveAffectation(AffectationUpdate affectation) {
        Employee employee = affectation.getEmployees();
        Date date = affectation.getDate();
        String periode = affectation.getPeriode();


        Integer verificationCount = affectationRepository.countAllByEmployeesAndDateAndPeriode(employee,date,periode);

        if (verificationCount==0){
            return affectationRepository.save(affectation);
        }else
            return null;
    }*/

    @Override
    public boolean deleteAffectation(Long id) {
        affectationRepository.deleteById(id);
        return true;
    }

    @Override
    public AffectationUpdate findById(Long id) {
        Optional<AffectationUpdate> aff = affectationRepository.findById(id);
        return aff.get();
    }
    @Override
    public AffectationUpdate affVerif(AffectationUpdate affectation) {
        Employee employeeList = affectation.getEmployees();
        Date date = affectation.getDate();
        String periode = affectation.getPeriode();

        AffectationUpdate affectationVerif = affectationRepository.findByEmployeesAndDateAndPeriode(employeeList,date,periode);

        return affectationVerif;
    }


    @Override
    public AffectationUpdate lastAff() {
        List<AffectationUpdate> affectations = affectationRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        return affectations.get(0);
    }

    @Override
    public void sendEmailAgentSaisi(AffectationUpdate affectationUpdate,Ateliers ateliers) {
        DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
//        Role role = roleRepository.findByType("agentSaisie");
//        List<Role> roles = new ArrayList<>();
//        roles.add(role);
//        List<Login> users = userImpService.finuserbyrole(roles);
//        for (Login user : users) {
            SimpleMailMessage message = new SimpleMailMessage();
            if (ateliers.getCode().equals("CH000002")){
                message.setTo("r.hicham@richebois.ma");
            } else if (ateliers.getCode().equals("CH000003")){
                message.setTo("b.lahcen@richebois.ma","f.abderrahim@richebois.ma");
            } else if (ateliers.getCode().equals("CH000004")){
                message.setTo("e.abderrahmane@richebois.ma");
            } else if (ateliers.getCode().equals("CH000036")){
                message.setTo("e.abderrahmane@richebois.ma");
            }
            message.setTo("e.abderrahmane@richebois.ma");
            message.setSubject("Gestion des affectations des Ateliers");
            message.setText("Bonjour \n \n " +
                    "       aucun affectation ajouté depuis deux jours. la dernier affectation était ajoutée depuis le : "+sourceFormat.format(affectationUpdate.getCreatedDate())+"\n \n"
                    +"Sincéres salutations");
            this.emailSender.send(message);
//        }

    }

    @Override
    public void sendEmailConsulteur(AffectationUpdate affectationUpdate,Ateliers ateliers) {
        DateFormat sourceFormat1 = new SimpleDateFormat("dd-MM-yyyy");
//        Role role = roleRepository.findByType("agentSaisie");
//        List<Role> roles = new ArrayList<>();
//        roles.add(role);
//        List<Login> users = userImpService.finuserbyrole(roles);
//        for (Login user : users) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("e.abderrahmane@richebois.ma");
        message.setSubject("Gestion des affectations des Ateliers");
        message.setText("Bonjour \n \n " +
                "       aucun affectation ajouté depuis deux jours. la dernier affectation était ajoutée depuis le : "+sourceFormat1.format(affectationUpdate.getCreatedDate())+"\n \n"
                +"Sincéres salutations");
        this.emailSender.send(message);
//        }
    }

    @Override
    public List<AffectationUpdate> affectationFiltred(long idUser,long idprojet, long idemploye, long idarticle,long idatelier, String dateDebut, String dateFin) throws ParseException {
        List<AffectationUpdate> affectationFiltred = customAffectationRepository.affectationFiltred( idUser, idprojet,  idemploye,  idarticle, idatelier,  dateDebut,  dateFin);
        Collections.reverse(affectationFiltred);
        return affectationFiltred;
    }
    @Override
    public List<AffectationPreviewDTO> previewDuplication(DuplicationRequestDTO request) {
        List<AffectationUpdate> sourceAffectations;

        // Récupérer les affectations source selon les critères
        if (request.getPeriodes().contains("ALL")) {
            sourceAffectations = affectationRepository.findByAteliersIdAndDate(
                    request.getAtelierId(), request.getSourceDate());
        } else {
            sourceAffectations = affectationRepository.findByAteliersIdAndDateAndPeriodeIn(
                    request.getAtelierId(), request.getSourceDate(), request.getPeriodes());
        }

        List<AffectationPreviewDTO> previewList = new ArrayList<>();

        for (AffectationUpdate source : sourceAffectations) {
            AffectationPreviewDTO preview = new AffectationPreviewDTO();

            // Générer un ID temporaire unique
            preview.setTempId("temp_" + System.currentTimeMillis() + "_" + source.getId());

            // Informations employé
            preview.setEmployeeId(source.getEmployees().getId());
            preview.setEmployeeName(source.getEmployees().getNom() + " " + source.getEmployees().getPrenom());
            preview.setEmployeeMatricule(source.getEmployees().getMatricule());

            // Informations atelier
            preview.setAtelierId(source.getAteliers().getId());
            preview.setAtelierDesignation(source.getAteliers().getDesignation());

            // Informations projet
            preview.setProjetId(source.getProjets().getId());
            preview.setProjetCode(source.getProjets().getCode());
            preview.setProjetDesignation(source.getProjets().getDesignation());

            // Informations article
            preview.setArticleId(source.getArticle().getId());
            preview.setArticleNumPrix(source.getArticle().getNumPrix());
            preview.setArticleDesignation(source.getArticle().getDesignation());

            // Informations affectation
            preview.setDate(request.getTargetDate());
            preview.setPeriode(source.getPeriode());
            preview.setNombreHeures(source.getNombreHeures());

            // Définir si les heures peuvent être modifiées
            preview.setCanModifyHours("Heures".equals(source.getPeriode()) || "Heures_Sup".equals(source.getPeriode()));

            // Vérifier les conflits
            boolean exists = affectationRepository.existsByEmployeesAndDateAndPeriode(
                    source.getEmployees(), request.getTargetDate(), source.getPeriode());

            if (exists) {
                preview.setHasConflict(true);
                preview.setConflictMessage("Affectation déjà existante pour cette période");
            } else {
                // Vérifier la limite des 9h pour les périodes normales
                if (!"Heures_Sup".equals(source.getPeriode())) {
                    int totalActuel = affectationRepository.totalHeures(source.getEmployees(), request.getTargetDate());
                    if (totalActuel + source.getNombreHeures() > 9) {
                        preview.setHasConflict(true);
                        preview.setConflictMessage("Dépassement de la limite de 9h journalières. Reste: " +
                                Math.max(0, 9 - totalActuel) + "h");
                    }
                }
            }

            previewList.add(preview);
        }

        return previewList;
    }
    @Override
    public String saveDuplicatedAffectations(List<AffectationPreviewDTO> affectations) {
        int savedCount = 0;
        List<String> errors = new ArrayList<>();

        for (AffectationPreviewDTO preview : affectations) {
            try {
                // Récupérer les entités nécessaires
                Employee employee = employeeService.findById(preview.getEmployeeId());

                Ateliers atelier = atelierService.getAtelierById(preview.getAtelierId());
                Projet projet = projetService.findById(preview.getProjetId()).get();
                Article article = articleService.findById(preview.getArticleId()).get();





                // Créer la nouvelle affectation
                AffectationUpdate newAffectation = new AffectationUpdate();
                newAffectation.setEmployees(employee);
                newAffectation.setAteliers(atelier);
                newAffectation.setProjets(projet);
                newAffectation.setArticle(article);
                newAffectation.setDate(preview.getDate());
                newAffectation.setPeriode(preview.getPeriode());
                newAffectation.setNombreHeures(preview.getNombreHeures());

                // Utiliser la méthode existante pour valider et sauvegarder
                saveAffectation(newAffectation);
                savedCount++;

            } catch (Exception e) {
                errors.add("Erreur pour " + preview.getEmployeeName() + " (" + preview.getPeriode() + "): " +
                        e.getMessage());
            }
        }

        String result = savedCount + " affectations enregistrées avec succès.";
        if (!errors.isEmpty()) {
            result += "\nErreurs rencontrées:\n" + String.join("\n", errors);
        }

        return result;
    }
    @Override
    public String duplicateAffectations(DuplicationRequestDTO request) {
        List<AffectationUpdate> sourceAffectations;

        // Récupérer les affectations source selon les critères
        if (request.getPeriodes().contains("ALL")) {
            sourceAffectations = affectationRepository.findByAteliersIdAndDate(
                    request.getAtelierId(), request.getSourceDate());
        } else {
            sourceAffectations = affectationRepository.findByAteliersIdAndDateAndPeriodeIn(
                    request.getAtelierId(), request.getSourceDate(), request.getPeriodes());
        }

        if (sourceAffectations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Aucune affectation trouvée pour les critères spécifiés");
        }

        int duplicatedCount = 0;
        List<String> errors = new ArrayList<>();

        for (AffectationUpdate source : sourceAffectations) {
            try {
                // Vérifier si l'affectation existe déjà pour la date cible
                boolean exists = affectationRepository.existsByEmployeesAndDateAndPeriode(
                        source.getEmployees(), request.getTargetDate(), source.getPeriode());

                if (!exists) {
                    // Créer la nouvelle affectation
                    AffectationUpdate newAffectation = new AffectationUpdate();
                    newAffectation.setDate(request.getTargetDate());
                    newAffectation.setAteliers(source.getAteliers());
                    newAffectation.setEmployees(source.getEmployees());
                    newAffectation.setPeriode(source.getPeriode());
                    newAffectation.setNombreHeures(source.getNombreHeures());
                    newAffectation.setProjets(source.getProjets());
                    newAffectation.setArticle(source.getArticle());

                    // Utiliser la méthode existante pour valider et sauvegarder
                    saveAffectation(newAffectation);
                    duplicatedCount++;
                }
            } catch (Exception e) {
                errors.add("Erreur pour " + source.getEmployees().getNom() + " " +
                        source.getEmployees().getPrenom() + " (" + source.getPeriode() + "): " +
                        e.getMessage());
            }
        }

        String result = duplicatedCount + " affectations dupliquées avec succès.";
        if (!errors.isEmpty()) {
            result += "\nErreurs rencontrées:\n" + String.join("\n", errors);
        }

        return result;
    }
}
