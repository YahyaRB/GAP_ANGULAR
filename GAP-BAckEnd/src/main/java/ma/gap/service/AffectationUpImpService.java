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
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AffectationUpdate> allAffectation(long idUser) {
        User user = userImpService.findbyusername(idUser);
        List<Ateliers> ateliers = user.getAteliers();
        List<AffectationUpdate> affectationList = new ArrayList<>();
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            if (role.getName().equals("agentSaisie")) {
                for (Ateliers atelier : ateliers) {
                    affectationList.addAll(affectationRepository.findAllByAteliersOrderByIdDesc(atelier));
                }
                return affectationList;
            } else {
                List<AffectationUpdate> affectations = affectationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
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

        // ✅ CORRIGÉ : Utiliser une méthode alternative pour totalHeures
        int totalActuel = calculateTotalHeuresSafe(employee, date);

        // ❗ On bloque à 9h SEULEMENT pour les périodes normales (pas pour Heures_Sup)
        if (totalActuel >= 9 && !isOvertime) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'employé a déjà 9h planifiées ce jour.");
        }

        Integer heures = affectation.getNombreHeures();

        if ("Matin".equalsIgnoreCase(periode)) {
            if (affectationRepository.countAllByEmployeesAndDateAndPeriode(employee, date, "Matin") > 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà affecté le matin pour cette date.");
            affectation.setPeriode("Matin");
            heures = 5;
            affectation.setNombreHeures(5);

        } else if ("Après-midi".equalsIgnoreCase(periode) || "Apres-midi".equalsIgnoreCase(periode)) {
            if (affectationRepository.countAllByEmployeesAndDateAndPeriode(employee, date, "Après-midi") > 0)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà affecté l'après-midi pour cette date.");
            affectation.setPeriode("Après-midi");
            heures = 4;
            affectation.setNombreHeures(4);

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

    /**
     * ✅ Méthode alternative pour calculer les heures totales sans utiliser DATE()
     */
    private int calculateTotalHeuresSafe(Employee employee, Date date) {
        try {
            // Utiliser la méthode du repository qui fonctionne
            return affectationRepository.totalHeures(employee, date);
        } catch (Exception e) {
            // Si ça échoue, utiliser une méthode manuelle avec BETWEEN
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date startOfDay = cal.getTime();

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                Date endOfDay = cal.getTime();

                String jpql = "SELECT SUM(CASE WHEN a.periode = 'Matin' THEN 5 " +
                        "WHEN a.periode = 'Après-midi' THEN 4 " +
                        "ELSE COALESCE(a.nombreHeures, 0) END) " +
                        "FROM AffectationUpdate a WHERE a.employees = :employee " +
                        "AND a.date BETWEEN :startDate AND :endDate";

                Number result = (Number) entityManager.createQuery(jpql)
                        .setParameter("employee", employee)
                        .setParameter("startDate", startOfDay)
                        .setParameter("endDate", endOfDay)
                        .getSingleResult();

                return result != null ? result.intValue() : 0;
            } catch (Exception e2) {
                System.err.println("Erreur calcul heures: " + e2.getMessage());
                return 0;
            }
        }
    }

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

        AffectationUpdate affectationVerif = affectationRepository.findByEmployeesAndDateAndPeriode(employeeList, date, periode);

        return affectationVerif;
    }

    @Override
    public AffectationUpdate lastAff() {
        List<AffectationUpdate> affectations = affectationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return affectations.get(0);
    }

    @Override
    public void sendEmailAgentSaisi(AffectationUpdate affectationUpdate, Ateliers ateliers) {
        DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleMailMessage message = new SimpleMailMessage();
        if (ateliers.getCode().equals("CH000002")) {
            message.setTo("kh.moustaghfir@groupearma.com");
            message.setFrom("sigrh@groupearma.com");
            message.setSubject("Rappel Affectation :" + sourceFormat.format(affectationUpdate.getDate()));
            message.setText("Bonjour Mr MOUSTAGHFIR," +
                    "\nL'employé(e) : " + affectationUpdate.getEmployees().getNom() + " " + affectationUpdate.getEmployees().getPrenom() + " avec le matricule :" + affectationUpdate.getEmployees().getMatricule()
                    + " vient d'etre affecter le :" + sourceFormat.format(affectationUpdate.getDate()) + " en Période : " + affectationUpdate.getPeriode() +
                    "\n Dans l'atelier : " + affectationUpdate.getAteliers().getDesignation() + " sur le projet :" + affectationUpdate.getProjets().getDesignation() + " sur l'article :" + affectationUpdate.getArticle().getDesignation() + "\n\nCordialement.");
            emailSender.send(message);
        }
    }

    @Override
    public void sendEmailConsulteur(AffectationUpdate affectationUpdate, Ateliers ateliers) {
        // Implementation selon besoins
    }

    @Override
    public List<AffectationUpdate> affectationFiltred(long idUser, long idprojet, long idemploye, long idarticle, long idatelier, String dateDebut, String dateFin) throws ParseException {
        List<AffectationUpdate> affectationFiltred = customAffectationRepository.affectationFiltred(idUser, idprojet, idemploye, idarticle, idatelier, dateDebut, dateFin);
        Collections.reverse(affectationFiltred);
        return affectationFiltred;
    }

    @Override
    public List<AffectationPreviewDTO> previewDuplication(DuplicationRequestDTO request) {
        System.out.println("\n============== DÉBUT PREVIEW DUPLICATION ==============");
        System.out.println("🔍 Paramètres reçus:");
        System.out.println("   - Atelier ID: " + request.getAtelierId());
        System.out.println("   - Date source: " + request.getSourceDate());
        System.out.println("   - Date cible: " + request.getTargetDate());
        System.out.println("   - Périodes: " + request.getPeriodes());

        try {
            // ✅ CORRIGÉ : Utiliser uniquement les méthodes qui fonctionnent (BETWEEN)
            List<AffectationUpdate> sourceAffectations = findAffectationsWithBetween(
                    request.getAtelierId(), request.getSourceDate(), request.getPeriodes());

            if (sourceAffectations.isEmpty()) {
                System.out.println("❌ AUCUNE affectation trouvée");
                return new ArrayList<>();
            }

            System.out.println("✅ " + sourceAffectations.size() + " affectation(s) trouvée(s)");

            List<AffectationPreviewDTO> previewList = new ArrayList<>();
            Date targetDate = normalizeDate(request.getTargetDate());

            for (AffectationUpdate source : sourceAffectations) {
                try {
                    AffectationPreviewDTO preview = createPreviewFromAffectation(source, targetDate);
                    previewList.add(preview);
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la création du preview pour l'affectation ID " +
                            source.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("📋 Preview final: " + previewList.size() + " éléments créés");
            System.out.println("============== FIN PREVIEW DUPLICATION ==============\n");
            return previewList;

        } catch (Exception e) {
            System.err.println("💥 ERREUR CRITIQUE dans previewDuplication: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * ✅ Méthode simplifiée qui utilise uniquement les requêtes BETWEEN (qui fonctionnent)
     */
    private List<AffectationUpdate> findAffectationsWithBetween(Long atelierId, Date sourceDate, List<String> periodes) {
        Date normalizedDate = normalizeDate(sourceDate);

        System.out.println("🔍 Utilisation de la méthode BETWEEN qui fonctionne");

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(normalizedDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endOfDay = cal.getTime();

            List<AffectationUpdate> results;
            if (periodes.contains("ALL")) {
                results = affectationRepository.findByAteliersIdAndDateRange(atelierId, startOfDay, endOfDay);
            } else {
                results = affectationRepository.findByAteliersIdAndDateRangeAndPeriodeIn(atelierId, startOfDay, endOfDay, periodes);
            }

            System.out.println("   Résultats avec BETWEEN: " + results.size());
            return results;

        } catch (Exception e) {
            System.err.println("   Erreur avec BETWEEN: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Crée un DTO de preview à partir d'une affectation source
     */
    private AffectationPreviewDTO createPreviewFromAffectation(AffectationUpdate source, Date targetDate) {
        AffectationPreviewDTO preview = new AffectationPreviewDTO();

        // ID temporaire unique
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
        preview.setDate(targetDate);
        preview.setPeriode(source.getPeriode());
        preview.setNombreHeures(source.getNombreHeures());

        // Définir si les heures peuvent être modifiées
        preview.setCanModifyHours("Heures".equals(source.getPeriode()) || "Heures_Sup".equals(source.getPeriode()));

        // ✅ CORRIGÉ : Vérifier les conflits sans utiliser les requêtes problématiques
        checkConflictsSafe(preview, source, targetDate);

        return preview;
    }

    /**
     * ✅ Vérification des conflits sans utiliser les requêtes DATE() problématiques
     */
    private void checkConflictsSafe(AffectationPreviewDTO preview, AffectationUpdate source, Date targetDate) {
        try {
            // Vérifier avec BETWEEN au lieu de DATE()
            Calendar cal = Calendar.getInstance();
            cal.setTime(targetDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endOfDay = cal.getTime();

            // Requête manuelle pour vérifier l'existence
            String jpql = "SELECT COUNT(a) FROM AffectationUpdate a WHERE a.employees = :employee " +
                    "AND a.date BETWEEN :startDate AND :endDate AND a.periode = :periode";

            Number count = (Number) entityManager.createQuery(jpql)
                    .setParameter("employee", source.getEmployees())
                    .setParameter("startDate", startOfDay)
                    .setParameter("endDate", endOfDay)
                    .setParameter("periode", source.getPeriode())
                    .getSingleResult();

            boolean exists = count.intValue() > 0;

            if (exists) {
                preview.setHasConflict(true);
                preview.setConflictMessage("Affectation déjà existante pour cette période");
                return;
            }

            // Vérifier la limite des 9h pour les périodes normales
            if (!"Heures_Sup".equals(source.getPeriode())) {
                int totalActuel = calculateTotalHeuresSafe(source.getEmployees(), targetDate);
                int nouvellesHeures = source.getNombreHeures() != null ? source.getNombreHeures() : 0;

                if (totalActuel + nouvellesHeures > 9) {
                    preview.setHasConflict(true);
                    preview.setConflictMessage("Dépassement de la limite de 9h journalières. Reste: " +
                            Math.max(0, 9 - totalActuel) + "h");
                    return;
                }
            }

            // Pas de conflit
            preview.setHasConflict(false);
            preview.setConflictMessage("");

        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des conflits: " + e.getMessage());
            preview.setHasConflict(false);
            preview.setConflictMessage("");
        }
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
        try {
            List<AffectationUpdate> sourceAffectations = findAffectationsWithBetween(
                    request.getAtelierId(), request.getSourceDate(), request.getPeriodes());

            if (sourceAffectations.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucune affectation trouvée pour les critères spécifiés");
            }

            Date targetDate = normalizeDate(request.getTargetDate());
            int duplicatedCount = 0;
            List<String> errors = new ArrayList<>();

            for (AffectationUpdate source : sourceAffectations) {
                try {
                    // Vérification d'existence simplifiée
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(targetDate);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date startOfDay = cal.getTime();

                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    Date endOfDay = cal.getTime();

                    String jpql = "SELECT COUNT(a) FROM AffectationUpdate a WHERE a.employees = :employee " +
                            "AND a.date BETWEEN :startDate AND :endDate AND a.periode = :periode";

                    Number count = (Number) entityManager.createQuery(jpql)
                            .setParameter("employee", source.getEmployees())
                            .setParameter("startDate", startOfDay)
                            .setParameter("endDate", endOfDay)
                            .setParameter("periode", source.getPeriode())
                            .getSingleResult();

                    boolean exists = count.intValue() > 0;

                    if (!exists) {
                        // Créer la nouvelle affectation
                        AffectationUpdate newAffectation = new AffectationUpdate();
                        newAffectation.setDate(targetDate);
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
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la duplication: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour normaliser les dates
    private Date normalizeDate(Date date) {
        if (date == null) return null;

        try {
            // Convertir en LocalDate puis retour en Date pour éliminer les heures/minutes/secondes
            LocalDate localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            System.err.println("Erreur normalisation date: " + e.getMessage());
            return date;
        }
    }
}