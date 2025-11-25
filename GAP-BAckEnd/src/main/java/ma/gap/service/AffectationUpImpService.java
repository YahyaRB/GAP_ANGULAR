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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(AffectationUpImpService.class);
    private static final int MAX_HEURES_NORMALES = 9;
    private static final int HEURES_MATIN = 5;
    private static final int HEURES_APRES_MIDI = 4;

    private final CustomAffectationRepository customAffectationRepository;
    private final AffectationUpdateRepository affectationRepository;
    private final UserImpService userImpService;
    private final JavaMailSender emailSender;
    private final EmployeeService employeeService;
    private final AtelierService atelierService;
    private final ProjetService projetService;
    private final ArticleService articleService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AffectationUpdate> allAffectation(long idUser) {
        try {
            User user = userImpService.findbyusername(idUser);
            if (user == null) {
                logger.warn("Utilisateur non trouvé avec l'ID : {}", idUser);
                return new ArrayList<>();
            }

            List<AffectationUpdate> affectationList = new ArrayList<>();
            List<Role> roles = user.getRoles();

            for (Role role : roles) {
                if ("agentSaisie".equals(role.getName())) {
                    List<Ateliers> ateliers = user.getAteliers();
                    for (Ateliers atelier : ateliers) {
                        affectationList.addAll(affectationRepository.findAllByAteliersOrderByIdDesc(atelier));
                    }
                    return affectationList;
                } else {
                    return affectationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
                }
            }
            return affectationList;
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des affectations pour l'utilisateur {}: {}", idUser,
                    e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String saveAffectation(AffectationUpdate affectation) {
        try {
            validateAffectation(affectation);

            final Employee employee = affectation.getEmployees();
            final Date date = affectation.getDate();
            String periode = normalizePeriode(affectation.getPeriode());

            boolean isOvertime = isOvertimePeriod(periode);
            if (isOvertime) {
                affectation.setPeriode("Heures_Sup");
            }

            // Calcul du total actuel des heures
            int totalActuel = calculateTotalHeuresSafe(employee, date);

            // Vérification de la limite pour les périodes normales
            if (totalActuel >= MAX_HEURES_NORMALES && !isOvertime) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "L'employé a déjà 9h planifiées ce jour.");
            }

            Integer heures = processPeriodesAndHours(affectation, employee, date, periode, isOvertime);

            // Vérification finale de la limite des 9h pour les périodes normales
            if (!isOvertime && totalActuel + heures > MAX_HEURES_NORMALES) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Dépassement : il reste " + Math.max(0, MAX_HEURES_NORMALES - totalActuel)
                                + "h possibles pour cette date.");
            }

            affectationRepository.save(affectation);
            int totalFinal = totalActuel + heures;

            String label = isOvertime ? "Affectation (Heures Sup)" : "Affectation";
            return label + " enregistrée (" + heures + "h). Total du jour : " + totalFinal + "h.";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde de l'affectation: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la sauvegarde de l'affectation");
        }
    }

    private void validateAffectation(AffectationUpdate affectation) {
        if (affectation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'affectation ne peut pas être nulle");
        }
        if (affectation.getEmployees() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'employé est obligatoire");
        }
        if (affectation.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date est obligatoire");
        }
    }

    private String normalizePeriode(String periode) {
        return (periode == null) ? null : periode.trim();
    }

    private boolean isOvertimePeriod(String periode) {
        if (periode == null)
            return false;
        String p = periode.replace('-', '_').replace(' ', '_');
        return p.equalsIgnoreCase("Heures_Sup") || p.equalsIgnoreCase("HeuresSup");
    }

    private Integer processPeriodesAndHours(AffectationUpdate affectation, Employee employee,
            Date date, String periode, boolean isOvertime) {
        Integer heures = affectation.getNombreHeures();

        if ("Matin".equalsIgnoreCase(periode)) {
            checkPeriodAvailability(employee, date, "Matin");
            affectation.setPeriode("Matin");
            heures = HEURES_MATIN;
            affectation.setNombreHeures(HEURES_MATIN);

        } else if ("Après-midi".equalsIgnoreCase(periode) || "Apres-midi".equalsIgnoreCase(periode)) {
            checkPeriodAvailability(employee, date, "Après-midi");
            affectation.setPeriode("Après-midi");
            heures = HEURES_APRES_MIDI;
            affectation.setNombreHeures(HEURES_APRES_MIDI);

        } else if (isOvertime) {
            validateOvertimeHours(heures);
            affectation.setPeriode("Heures_Sup");

        } else {
            validateNormalHours(heures);
            affectation.setPeriode("Heures");
        }

        return heures;
    }

    private void checkPeriodAvailability(Employee employee, Date date, String periode) {
        if (affectationRepository.countAllByEmployeesAndDateAndPeriode(employee, date, periode) > 0) {
            String message = "Matin".equals(periode) ? "Déjà affecté le matin pour cette date."
                    : "Déjà affecté l'après-midi pour cette date.";
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    private void validateOvertimeHours(Integer heures) {
        if (heures == null || heures <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Veuillez saisir un nombre d'heures > 0.");
        }
    }

    private void validateNormalHours(Integer heures) {
        if (heures == null || heures <= 0 || heures > MAX_HEURES_NORMALES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Heures invalides (1 à " + MAX_HEURES_NORMALES + ").");
        }
    }

    private int calculateTotalHeuresSafe(Employee employee, Date date) {
        try {
            return affectationRepository.totalHeures(employee, date);
        } catch (Exception e) {
            logger.warn("Échec de la méthode totalHeures, utilisation de la méthode alternative");
            return calculateTotalHoursManual(employee, date);
        }
    }

    private int calculateTotalHoursManual(Employee employee, Date date) {
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
        } catch (Exception e) {
            logger.error("Erreur calcul heures: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean deleteAffectation(Long id) {
        try {
            if (!affectationRepository.existsById(id)) {
                logger.warn("Tentative de suppression d'une affectation inexistante: {}", id);
                return false;
            }
            affectationRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'affectation {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public AffectationUpdate findById(Long id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Affectation non trouvée avec l'ID: " + id));
    }

    @Override
    public AffectationUpdate affVerif(AffectationUpdate affectation) {
        Employee employeeList = affectation.getEmployees();
        Date date = affectation.getDate();
        String periode = affectation.getPeriode();

        return affectationRepository.findByEmployeesAndDateAndPeriode(employeeList, date, periode);
    }

    @Override
    public AffectationUpdate lastAff() {
        List<AffectationUpdate> affectations = affectationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (affectations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune affectation trouvée");
        }
        return affectations.get(0);
    }

    @Override
    public void sendEmailAgentSaisi(AffectationUpdate affectationUpdate, Ateliers ateliers) {
        try {
            DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleMailMessage message = new SimpleMailMessage();

            if ("CH000002".equals(ateliers.getCode())) {
                message.setTo("kh.moustaghfir@groupearma.com");
                message.setFrom("sigrh@groupearma.com");
                message.setSubject("Rappel Affectation : " + sourceFormat.format(affectationUpdate.getDate()));

                String emailBody = String.format(
                        "Bonjour Mr MOUSTAGHFIR,\n\n" +
                                "L'employé(e) : %s %s avec le matricule : %s " +
                                "vient d'être affecté(e) le : %s en Période : %s\n\n" +
                                "Cordialement,\n" +
                                "Service RH",
                        affectationUpdate.getEmployees().getNom(),
                        affectationUpdate.getEmployees().getPrenom(),
                        affectationUpdate.getEmployees().getMatricule(),
                        sourceFormat.format(affectationUpdate.getDate()),
                        affectationUpdate.getPeriode());

                message.setText(emailBody);
                emailSender.send(message);
                logger.info("Email envoyé avec succès pour l'affectation de l'employé {}",
                        affectationUpdate.getEmployees().getMatricule());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        }
    }

    @Override
    public void sendEmailConsulteur(AffectationUpdate affectationUpdate, Ateliers ateliers) {
        try {
            DateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleMailMessage message = new SimpleMailMessage();

            // Configuration spécifique pour le consulteur selon l'atelier
            if ("CH000002".equals(ateliers.getCode())) {
                message.setTo("consulteur@groupearma.com"); // Adapter selon vos besoins
                message.setFrom("sigrh@groupearma.com");
                message.setSubject("Notification Affectation : " + sourceFormat.format(affectationUpdate.getDate()));

                String emailBody = String.format(
                        "Bonjour,\n\n" +
                                "Une nouvelle affectation a été créée :\n\n" +
                                "Employé(e) : %s %s\n" +
                                "Matricule : %s\n" +
                                "Date : %s\n" +
                                "Période : %s\n" +
                                "Nombre d'heures : %s\n" +
                                "Atelier : %s\n\n" +
                                "Cordialement,\n" +
                                "Service RH",
                        affectationUpdate.getEmployees().getNom(),
                        affectationUpdate.getEmployees().getPrenom(),
                        affectationUpdate.getEmployees().getMatricule(),
                        sourceFormat.format(affectationUpdate.getDate()),
                        affectationUpdate.getPeriode(),
                        affectationUpdate.getNombreHeures(),
                        ateliers.getDesignation());

                message.setText(emailBody);
                emailSender.send(message);
                logger.info("Email envoyé avec succès au consulteur pour l'affectation de l'employé {}",
                        affectationUpdate.getEmployees().getMatricule());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email au consulteur: {}", e.getMessage());
        }
    }

    @Override
    public Page<AffectationUpdate> affectationFiltredPaginated(long idUser, long idprojet, long idemploye,
            long idarticle, long idatelier,
            String dateDebut, String dateFin,
            int page, int size) throws ParseException {
        try {
            User user = userImpService.findbyusername(idUser);
            if (user == null) {
                logger.warn("Utilisateur non trouvé avec l'ID : {}", idUser);
                return new PageImpl<>(new ArrayList<>());
            }

            // Parsing des dates
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate = null;
            Date endDate = null;

            if (dateDebut != null && !dateDebut.trim().isEmpty()) {
                startDate = sdf.parse(dateDebut.trim());
                // Début de journée
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
            }

            if (dateFin != null && !dateFin.trim().isEmpty()) {
                endDate = sdf.parse(dateFin.trim());
                // Fin de journée
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                endDate = cal.getTime();
            }

            // Construction de la requête JPQL dynamique
            StringBuilder jpql = new StringBuilder("SELECT a FROM AffectationUpdate a WHERE 1=1 ");
            StringBuilder countJpql = new StringBuilder("SELECT COUNT(a) FROM AffectationUpdate a WHERE 1=1 ");
            Map<String, Object> parameters = new HashMap<>();

            // Construction des clauses WHERE communes
            String whereClause = buildWhereClauseForFilter(idUser, user, idatelier, idprojet,
                    idemploye, idarticle, startDate, endDate, parameters);

            jpql.append(whereClause);
            countJpql.append(whereClause);
            jpql.append(" ORDER BY a.date DESC, a.id DESC");

            // Requête pour compter le total
            javax.persistence.Query countQuery = entityManager.createQuery(countJpql.toString());
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                countQuery.setParameter(entry.getKey(), entry.getValue());
            }
            Long totalElements = (Long) countQuery.getSingleResult();

            // Requête pour récupérer les données paginées
            javax.persistence.Query query = entityManager.createQuery(jpql.toString());
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            // Application de la pagination
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            @SuppressWarnings("unchecked")
            List<AffectationUpdate> results = query.getResultList();

            // Création de l'objet Pageable
            Pageable pageable = PageRequest.of(page, size);

            logger.info("Recherche filtrée paginée retourne {} résultats sur {} total pour l'utilisateur {}",
                    results.size(), totalElements, idUser);

            return new PageImpl<>(results, pageable, totalElements);

        } catch (ParseException e) {
            logger.error("Erreur de parsing des dates: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors du filtrage paginé des affectations: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>());
        }
    }

    @Override
    public List<AffectationUpdate> affectationFiltred(long idUser, long idprojet, long idemploye,
            long idarticle, long idatelier,
            String dateDebut, String dateFin) throws ParseException {
        try {
            User user = userImpService.findbyusername(idUser);
            if (user == null) {
                logger.warn("Utilisateur non trouvé avec l'ID : {}", idUser);
                return new ArrayList<>();
            }

            // Parsing des dates
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate = null;
            Date endDate = null;

            if (dateDebut != null && !dateDebut.trim().isEmpty()) {
                startDate = sdf.parse(dateDebut.trim());
                // Début de journée
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();
            }

            if (dateFin != null && !dateFin.trim().isEmpty()) {
                endDate = sdf.parse(dateFin.trim());
                // Fin de journée
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                endDate = cal.getTime();
            }

            // Construction de la requête JPQL dynamique
            StringBuilder jpql = new StringBuilder("SELECT a FROM AffectationUpdate a WHERE 1=1 ");
            Map<String, Object> parameters = new HashMap<>();

            // Utilisation de la méthode utilitaire pour construire les clauses WHERE
            String whereClause = buildWhereClauseForFilter(idUser, user, idatelier, idprojet,
                    idemploye, idarticle, startDate, endDate, parameters);
            jpql.append(whereClause);
            jpql.append("ORDER BY a.date DESC, a.id DESC");

            // Exécution de la requête
            javax.persistence.Query query = entityManager.createQuery(jpql.toString());

            // Application des paramètres
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            @SuppressWarnings("unchecked")
            List<AffectationUpdate> results = query.getResultList();

            logger.info("Recherche filtrée retourne {} résultats pour l'utilisateur {}",
                    results.size(), idUser);

            return results;

        } catch (ParseException e) {
            logger.error("Erreur de parsing des dates: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors du filtrage des affectations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<AffectationPreviewDTO> previewDuplication(DuplicationRequestDTO request) {
        logger.info("============== DEBUT PREVIEW DUPLICATION ==============");
        logger.info("Paramètres reçus:");
        logger.info("   - Atelier ID: {}", request.getAtelierId());
        logger.info("   - Date source: {}", request.getSourceDate());
        logger.info("   - Date cible: {}", request.getTargetDate());
        logger.info("   - Périodes: {}", request.getPeriodes());
        logger.info("   - Période cible: {}", request.getTargetPeriod());

        try {
            List<AffectationUpdate> sourceAffectations = findAffectationsWithBetween(
                    request.getAtelierId(), request.getSourceDate(), request.getPeriodes());

            if (sourceAffectations.isEmpty()) {
                logger.info("Aucune affectation trouvée");
                return new ArrayList<>();
            }

            logger.info("{} affectation(s) trouvée(s)", sourceAffectations.size());

            List<AffectationPreviewDTO> previewList = new ArrayList<>();
            Date targetDate = normalizeDate(request.getTargetDate());

            for (AffectationUpdate source : sourceAffectations) {
                try {
                    AffectationPreviewDTO preview = createPreviewFromAffectation(source, targetDate,
                            request.getTargetPeriod());
                    previewList.add(preview);
                } catch (Exception e) {
                    logger.error("Erreur lors de la création du preview pour l'affectation ID {}: {}",
                            source.getId(), e.getMessage());
                }
            }

            logger.info("Preview final: {} éléments créés", previewList.size());
            logger.info("============== FIN PREVIEW DUPLICATION ==============");
            return previewList;

        } catch (Exception e) {
            logger.error("Erreur critique dans previewDuplication: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<AffectationUpdate> findAffectationsWithBetween(Long atelierId, Date sourceDate,
            List<String> periodes) {
        Date normalizedDate = normalizeDate(sourceDate);
        logger.info("Utilisation de la méthode BETWEEN");

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
                results = affectationRepository.findByAteliersIdAndDateRangeAndPeriodeIn(atelierId, startOfDay,
                        endOfDay, periodes);
            }

            logger.info("Résultats avec BETWEEN: {}", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Erreur avec BETWEEN: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private AffectationPreviewDTO createPreviewFromAffectation(AffectationUpdate source, Date targetDate,
            String targetPeriod) {
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

        String finalPeriod = source.getPeriode();
        Integer finalHeures = source.getNombreHeures();

        if (targetPeriod != null && !targetPeriod.isEmpty() && !"SAME".equals(targetPeriod)) {
            finalPeriod = targetPeriod;
            if ("Matin".equalsIgnoreCase(targetPeriod)) {
                finalHeures = HEURES_MATIN;
            } else if ("Après-midi".equalsIgnoreCase(targetPeriod) || "Apres-midi".equalsIgnoreCase(targetPeriod)) {
                finalHeures = HEURES_APRES_MIDI;
            } else if ("Heures".equalsIgnoreCase(targetPeriod) || "Heures_Sup".equalsIgnoreCase(targetPeriod)) {
                finalHeures = 4;
            }
        }

        preview.setPeriode(finalPeriod);
        preview.setNombreHeures(finalHeures);

        // Définir si les heures peuvent être modifiées
        preview.setCanModifyHours("Heures".equals(finalPeriod) || "Heures_Sup".equals(finalPeriod));

        // Vérifier les conflits
        checkConflictsSafe(preview, source, targetDate, finalPeriod, finalHeures);

        return preview;
    }

    private void checkConflictsSafe(AffectationPreviewDTO preview, AffectationUpdate source, Date targetDate,
            String periode, Integer heures) {
        try {
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

            // Vérifier l'existence d'une affectation similaire
            String jpql = "SELECT COUNT(a) FROM AffectationUpdate a WHERE a.employees = :employee " +
                    "AND a.date BETWEEN :startDate AND :endDate AND a.periode = :periode";

            Number count = (Number) entityManager.createQuery(jpql)
                    .setParameter("employee", source.getEmployees())
                    .setParameter("startDate", startOfDay)
                    .setParameter("endDate", endOfDay)
                    .setParameter("periode", periode)
                    .getSingleResult();

            boolean exists = count.intValue() > 0;

            if (exists) {
                preview.setHasConflict(true);
                preview.setConflictMessage("Affectation déjà existante pour cette période");
                return;
            }

            // Vérifier la limite des 9h pour les périodes normales
            if (!"Heures_Sup".equals(periode)) {
                int totalActuel = calculateTotalHeuresSafe(source.getEmployees(), targetDate);
                int nouvellesHeures = heures != null ? heures : 0;

                if (totalActuel + nouvellesHeures > MAX_HEURES_NORMALES) {
                    preview.setHasConflict(true);
                    preview.setConflictMessage(
                            "Dépassement de la limite de " + MAX_HEURES_NORMALES + "h journalières. Reste: " +
                                    Math.max(0, MAX_HEURES_NORMALES - totalActuel) + "h");
                    return;
                }
            }

            // Pas de conflit
            preview.setHasConflict(false);
            preview.setConflictMessage("");

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des conflits: {}", e.getMessage());
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
                // Récupérer les entités nécessaires avec vérification
                Employee employee = employeeService.findById(preview.getEmployeeId());
                if (employee == null) {
                    errors.add("Employé non trouvé pour " + preview.getEmployeeName());
                    continue;
                }

                Ateliers atelier = atelierService.getAtelierById(preview.getAtelierId());
                if (atelier == null) {
                    errors.add("Atelier non trouvé pour " + preview.getEmployeeName());
                    continue;
                }

                Optional<Projet> projetOpt = projetService.findById(preview.getProjetId());
                if (!projetOpt.isPresent()) {
                    errors.add("Projet non trouvé pour " + preview.getEmployeeName());
                    continue;
                }

                Optional<Article> articleOpt = articleService.findById(preview.getArticleId());
                if (!articleOpt.isPresent()) {
                    errors.add("Article non trouvé pour " + preview.getEmployeeName());
                    continue;
                }

                // Créer la nouvelle affectation
                AffectationUpdate newAffectation = new AffectationUpdate();
                newAffectation.setEmployees(employee);
                newAffectation.setAteliers(atelier);
                newAffectation.setProjets(projetOpt.get());
                newAffectation.setArticle(articleOpt.get());
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
                    if (!isAffectationExists(source.getEmployees(), targetDate, source.getPeriode())) {
                        // Créer la nouvelle affectation
                        AffectationUpdate newAffectation = createNewAffectation(source, targetDate);

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

    private boolean isAffectationExists(Employee employee, Date targetDate, String periode) {
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
                .setParameter("employee", employee)
                .setParameter("startDate", startOfDay)
                .setParameter("endDate", endOfDay)
                .setParameter("periode", periode)
                .getSingleResult();

        return count.intValue() > 0;
    }

    private AffectationUpdate createNewAffectation(AffectationUpdate source, Date targetDate) {
        AffectationUpdate newAffectation = new AffectationUpdate();
        newAffectation.setDate(targetDate);
        newAffectation.setAteliers(source.getAteliers());
        newAffectation.setEmployees(source.getEmployees());
        newAffectation.setPeriode(source.getPeriode());
        newAffectation.setNombreHeures(source.getNombreHeures());
        newAffectation.setProjets(source.getProjets());
        newAffectation.setArticle(source.getArticle());
        return newAffectation;
    }

    private String buildWhereClauseForFilter(long idUser, User user, long idatelier, long idprojet,
            long idemploye, long idarticle, Date startDate, Date endDate,
            Map<String, Object> parameters) {
        StringBuilder whereClause = new StringBuilder();

        // Filtrage par utilisateur et ses ateliers (pour agentSaisie)
        List<Role> roles = user.getRoles();
        boolean isAgentSaisie = roles.stream().anyMatch(role -> "agentSaisie".equals(role.getName()));

        if (isAgentSaisie) {
            List<Ateliers> userAteliers = user.getAteliers();
            if (!userAteliers.isEmpty()) {
                whereClause.append("AND a.ateliers IN :userAteliers ");
                parameters.put("userAteliers", userAteliers);
            }
        }

        // Filtrage par atelier
        if (idatelier > 0) {
            whereClause.append("AND a.ateliers.id = :atelierId ");
            parameters.put("atelierId", idatelier);
        }

        // Filtrage par projet
        if (idprojet > 0) {
            whereClause.append("AND a.projets.id = :projetId ");
            parameters.put("projetId", idprojet);
        }

        // Filtrage par employé
        if (idemploye > 0) {
            whereClause.append("AND a.employees.id = :employeId ");
            parameters.put("employeId", idemploye);
        }

        // Filtrage par article
        if (idarticle > 0) {
            whereClause.append("AND a.article.id = :articleId ");
            parameters.put("articleId", idarticle);
        }

        // Filtrage par dates
        if (startDate != null) {
            whereClause.append("AND a.date >= :startDate ");
            parameters.put("startDate", startDate);
        }

        if (endDate != null) {
            whereClause.append("AND a.date <= :endDate ");
            parameters.put("endDate", endDate);
        }

        return whereClause.toString();
    }

    private Date normalizeDate(Date date) {
        if (date == null)
            return null;

        try {
            LocalDate localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return date;
        }
    }
}