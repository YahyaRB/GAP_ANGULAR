package ma.gap.repository;

import ma.gap.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AffectationUpdateRepository extends JpaRepository<AffectationUpdate, Long> {

        List<AffectationUpdate> findAllByAteliersOrderByIdDesc(Ateliers ateliers);

        Page<AffectationUpdate> findAllByAteliersIn(List<Ateliers> ateliers, Pageable pageable);

        List<AffectationUpdate> findAllByArticleOrderByIdDesc(Article article);

        int countAllByEmployeesAndDateAndPeriode(Employee employee, Date date, String periode);

        // ✅ CORRIGÉ POUR SQL SERVER - Utilisation de CAST au lieu de DATE()
        @Query("select coalesce(sum( case when a.periode = 'Matin' then 5 when " +
                        "a.periode = 'Après-midi' then 4 else coalesce(a.nombreHeures, 0) end), 0) " +
                        "from AffectationUpdate a where a.employees = :employee and CAST(a.date as date) = CAST(:date as date)")
        int totalHeures(@Param("employee") Employee employee, @Param("date") Date date);

        AffectationUpdate findByEmployeesAndDateAndPeriode(Employee employee, Date date, String periode);

        @Query(value = "select projet_id, COUNT(periode) as periodeCount, periode, atelier_id, " +
                        "STUFF((SELECT ',' + CAST(employe_id AS VARCHAR) " +
                        "       FROM affectation_update au " +
                        "       WHERE au.atelier_id = a.atelier_id " +
                        "         AND au.projet_id = a.projet_id " +
                        "         AND au.periode = a.periode " +
                        "         AND MONTH(au.date) = :month " +
                        "         AND YEAR(au.date) = :year " +
                        "       FOR XML PATH('')), 1, 1, '') as listEmp " +
                        "from affectation_update a " +
                        "where atelier_id=:atelier and MONTH(date)=:month and YEAR(date)=:year " +
                        "group by projet_id, periode, atelier_id", nativeQuery = true)
        List<Object[]> listCalculPerProject(@Param("atelier") Long id, @Param("month") Integer month,
                        @Param("year") Integer year);

        List<AffectationUpdate> findAllByProjets(Projet projet);

        List<AffectationUpdate> findAllByEmployees(Employee employee);

        @Query(value = "SELECT employe_id FROM affectation_update", nativeQuery = true)
        List<Integer> listeEmployeAffecte();

        // =============== REQUÊTES CORRIGÉES POUR SQL SERVER ===============

        // ✅ Requêtes avec CAST au lieu de DATE() - Compatible SQL Server
        @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND CAST(a.date as date) = CAST(:date as date)")
        List<AffectationUpdate> findByAteliersIdAndDate(@Param("atelierId") Long atelierId, @Param("date") Date date);

        @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND CAST(a.date as date) = CAST(:date as date) AND a.periode IN :periodes")
        List<AffectationUpdate> findByAteliersIdAndDateAndPeriodeIn(
                        @Param("atelierId") Long atelierId,
                        @Param("date") Date date,
                        @Param("periodes") List<String> periodes);

        @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND CAST(a.date as date) = CAST(:date as date) AND a.periode = :periode")
        List<AffectationUpdate> findByAteliersIdAndDateAndPeriode(
                        @Param("atelierId") Long atelierId,
                        @Param("date") Date date,
                        @Param("periode") String periode);

        // ✅ Requêtes BETWEEN (celles qui fonctionnent déjà)
        @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND a.date BETWEEN :startDate AND :endDate")
        List<AffectationUpdate> findByAteliersIdAndDateRange(
                        @Param("atelierId") Long atelierId,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND a.date BETWEEN :startDate AND :endDate AND a.periode IN :periodes")
        List<AffectationUpdate> findByAteliersIdAndDateRangeAndPeriodeIn(
                        @Param("atelierId") Long atelierId,
                        @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate,
                        @Param("periodes") List<String> periodes);

        // ✅ Requêtes natives corrigées pour SQL Server
        @Query(value = "SELECT * FROM affectation_update WHERE atelier_id = :atelierId AND CAST(date as date) = CAST(:date as date)", nativeQuery = true)
        List<AffectationUpdate> findByAteliersIdAndDateNative(
                        @Param("atelierId") Long atelierId,
                        @Param("date") Date date);

        @Query(value = "SELECT * FROM affectation_update WHERE atelier_id = :atelierId AND CAST(date as date) = CAST(:date as date) AND periode IN (:periodes)", nativeQuery = true)
        List<AffectationUpdate> findByAteliersIdAndDateAndPeriodeInNative(
                        @Param("atelierId") Long atelierId,
                        @Param("date") Date date,
                        @Param("periodes") List<String> periodes);

        // =============== REQUÊTES DE VÉRIFICATION CORRIGÉES ===============

        @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AffectationUpdate a WHERE a.employees = :employee AND CAST(a.date as date) = CAST(:date as date) AND a.periode = :periode")
        boolean existsByEmployeesAndDateAndPeriode(@Param("employee") Employee employee, @Param("date") Date date,
                        @Param("periode") String periode);

        // Méthode pour compter les conflits
        @Query("SELECT COUNT(a) FROM AffectationUpdate a WHERE a.employees.id = :employeeId AND CAST(a.date as date) = CAST(:date as date) AND a.periode = :periode")
        long countByEmployeesIdAndDateAndPeriode(
                        @Param("employeeId") Long employeeId,
                        @Param("date") Date date,
                        @Param("periode") String periode);

        // =============== REQUÊTES DE DEBUG CORRIGÉES ===============

        // Pour vérifier toutes les périodes distinctes
        @Query("SELECT DISTINCT a.periode FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId")
        List<String> findDistinctPeriodesByAtelierId(@Param("atelierId") Long atelierId);

        // Pour compter les affectations par atelier et date
        @Query("SELECT COUNT(a) FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND CAST(a.date as date) = CAST(:date as date)")
        long countByAteliersIdAndDate(@Param("atelierId") Long atelierId, @Param("date") Date date);
}