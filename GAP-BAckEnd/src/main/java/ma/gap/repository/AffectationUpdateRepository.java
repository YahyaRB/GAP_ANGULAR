package ma.gap.repository;

import ma.gap.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AffectationUpdateRepository extends JpaRepository<AffectationUpdate,Long> {

    List<AffectationUpdate> findAllByAteliersOrderByIdDesc(Ateliers ateliers);
    List<AffectationUpdate> findAllByArticleOrderByIdDesc(Article article);
   /* Integer countAllByEmployeesAndDateAndPeriode(Employee employee, Date date, String periode);*/
   int countAllByEmployeesAndDateAndPeriode(Employee employee, Date date, String periode);

    @Query("select coalesce(sum( case when a.periode = 'Matin' then 5 when " +
            "a.periode = 'Après-midi' then 4 else coalesce(a.nombreHeures, 0) end), 0) " +
            "from AffectationUpdate a where a.employees = :employee and a.date = :date ")
    int totalHeures(@Param("employee") Employee employee, @Param("date") Date date);

    AffectationUpdate findByEmployeesAndDateAndPeriode(Employee employee, Date date,String periode);

    @Query(value = "select projet_id,COUNT(periode) as periodeCount,periode,atelier_id,STRING_AGG(employe_id, ',') as listEmp from affectation_update where atelier_id=:atelier and MONTH(date)=:month and YEAR(date)=:year group by projet_id,periode,atelier_id ",nativeQuery = true)
    List<Object[]> listCalculPerProject(@Param("atelier") Long id,@Param("month") Integer month,@Param("year") Integer year);

    List<AffectationUpdate> findAllByProjets(Projet projet);
    List<AffectationUpdate> findAllByEmployees(Employee employee);

    @Query(value = "SELECT employe_id FROM affectation_update", nativeQuery = true)
    public List<Integer> listeEmployeAffecte();
    @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND a.date = :date")
    List<AffectationUpdate> findByAteliersIdAndDate(@Param("atelierId") Long atelierId, @Param("date") Date date);

    @Query("SELECT a FROM AffectationUpdate a WHERE a.ateliers.id = :atelierId AND a.date = :date AND a.periode IN :periodes")
    List<AffectationUpdate> findByAteliersIdAndDateAndPeriodeIn(
            @Param("atelierId") Long atelierId,
            @Param("date") Date date,
            @Param("periodes") List<String> periodes);

    boolean existsByEmployeesAndDateAndPeriode(Employee employee, Date date, String periode);


}
