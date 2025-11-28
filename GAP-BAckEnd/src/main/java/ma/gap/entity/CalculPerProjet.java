package ma.gap.entity;

import java.util.List;

public class CalculPerProjet {

    private Projet projet;
    private Integer count;
    private String periode;
    private Ateliers atelier;
    private Integer heureTrav;
    private float pourcHeur;
    private Integer totalHeur;
    private List<EmployeeCalcul> employesCalculs;
    private Integer empHeurTrav;
    private float pourcHeurEmp;

    public float getPourcHeurEmp() {
        return pourcHeurEmp;
    }

    public void setPourcHeurEmp(float pourcHeurEmp) {
        this.pourcHeurEmp = pourcHeurEmp;
    }

    public Integer getEmpHeurTrav() {
        return empHeurTrav;
    }

    public void setEmpHeurTrav(Integer empHeurTrav) {
        this.empHeurTrav = empHeurTrav;
    }

    public List<EmployeeCalcul> getEmployesCalculs() {
        return employesCalculs;
    }

    public void setEmployesCalculs(List<EmployeeCalcul> employesCalculs) {
        this.employesCalculs = employesCalculs;
    }

    public Integer getTotalHeur() {
        return totalHeur;
    }

    public void setTotalHeur(Integer totalHeur) {
        this.totalHeur = totalHeur;
    }

    public float getPourcHeur() {
        return pourcHeur;
    }

    public void setPourcHeur(float pourcHeur) {
        this.pourcHeur = pourcHeur;
    }

    public Integer getHeureTrav() {
        return heureTrav;
    }

    public void setHeureTrav(Integer heureTrav) {
        this.heureTrav = heureTrav;
    }

    public Projet getProjet() {
        return projet;
    }

    public void setProjet(Projet projet) {
        this.projet = projet;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public Ateliers getAtelier() {
        return atelier;
    }

    public void setAtelier(Ateliers atelier) {
        this.atelier = atelier;
    }
}
