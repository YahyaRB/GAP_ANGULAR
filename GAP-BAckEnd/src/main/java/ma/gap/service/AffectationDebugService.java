package ma.gap.service;

import lombok.AllArgsConstructor;
import ma.gap.entity.*;
import ma.gap.repository.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@AllArgsConstructor
public class AffectationDebugService {

    private AffectationUpdateRepository affectationRepository;
    private AtelierService atelierService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Méthode pour diagnostiquer pourquoi les affectations ne sont pas trouvées
     */
    public String debugDuplicationProblem(Long atelierId, Date sourceDate, List<String> periodes) {
        StringBuilder debug = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        debug.append("=== DIAGNOSTIC DUPLICATION ===\n");
        debug.append("Atelier ID: ").append(atelierId).append("\n");
        debug.append("Date source: ").append(sdf.format(sourceDate)).append("\n");
        debug.append("Périodes recherchées: ").append(periodes).append("\n\n");

        try {
            // 1. Vérifier si l'atelier existe
            Ateliers atelier = atelierService.getAtelierById(atelierId);
            debug.append("1. ATELIER TROUVÉ: ").append(atelier.getDesignation()).append(" (").append(atelier.getCode()).append(")\n\n");

            // 2. Rechercher toutes les affectations pour cet atelier (sans filtre de date)
            List<AffectationUpdate> allAffectations = affectationRepository.findAllByAteliersOrderByIdDesc(atelier);
            debug.append("2. TOTAL AFFECTATIONS POUR CET ATELIER: ").append(allAffectations.size()).append("\n");

            if (!allAffectations.isEmpty()) {
                debug.append("   Quelques exemples:\n");
                for (int i = 0; i < Math.min(5, allAffectations.size()); i++) {
                    AffectationUpdate aff = allAffectations.get(i);
                    debug.append("   - Date: ").append(sdf.format(aff.getDate()))
                            .append(", Période: '").append(aff.getPeriode()).append("'")
                            .append(", Employé: ").append(aff.getEmployees().getNom()).append("\n");
                }
            }
            debug.append("\n");

            // 3. Rechercher les affectations pour la date exacte (requête native)
            String sqlNative = "SELECT * FROM affectation_update WHERE atelier_id = ? AND DATE(date) = DATE(?)";
            List<Object[]> nativeResults = entityManager.createNativeQuery(sqlNative)
                    .setParameter(1, atelierId)
                    .setParameter(2, sourceDate)
                    .getResultList();

            debug.append("3. AFFECTATIONS POUR LA DATE (requête native): ").append(nativeResults.size()).append("\n");

            // 4. Rechercher avec comparaison de date Java
            Calendar cal = Calendar.getInstance();
            cal.setTime(sourceDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endOfDay = cal.getTime();

            String jpqlRange = "SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND a.date BETWEEN :startDate AND :endDate";
            List<AffectationUpdate> rangeResults = entityManager.createQuery(jpqlRange, AffectationUpdate.class)
                    .setParameter("atelierId", atelierId)
                    .setParameter("startDate", startOfDay)
                    .setParameter("endDate", endOfDay)
                    .getResultList();

            debug.append("4. AFFECTATIONS AVEC RANGE DE DATE: ").append(rangeResults.size()).append("\n");

            for (AffectationUpdate aff : rangeResults) {
                debug.append("   - Période: '").append(aff.getPeriode()).append("'")
                        .append(", Heures: ").append(aff.getNombreHeures())
                        .append(", Employé: ").append(aff.getEmployees().getNom()).append("\n");
            }
            debug.append("\n");

            // 5. Vérifier les périodes exactes dans la DB
            debug.append("5. TOUTES LES PÉRIODES DISTINCTES POUR CET ATELIER:\n");
            String periodesQuery = "SELECT DISTINCT a.periode FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId";
            List<String> distinctPeriodes = entityManager.createQuery(periodesQuery, String.class)
                    .setParameter("atelierId", atelierId)
                    .getResultList();

            for (String periode : distinctPeriodes) {
                debug.append("   - '").append(periode).append("'\n");
            }
            debug.append("\n");

            // 6. Test avec la requête actuelle du repository
            debug.append("6. TEST REQUÊTE REPOSITORY:\n");
            List<AffectationUpdate> repoResults = affectationRepository.findByAteliersIdAndDate(atelierId, sourceDate);
            debug.append("   Résultats findByAteliersIdAndDate: ").append(repoResults.size()).append("\n");

            if (!periodes.isEmpty()) {
                List<AffectationUpdate> repoResultsWithPeriodes = affectationRepository.findByAteliersIdAndDateAndPeriodeIn(atelierId, sourceDate, periodes);
                debug.append("   Résultats avec périodes ").append(periodes).append(": ").append(repoResultsWithPeriodes.size()).append("\n");
            }

        } catch (Exception e) {
            debug.append("ERREUR: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        debug.append("\n=== FIN DIAGNOSTIC ===");
        return debug.toString();
    }
}