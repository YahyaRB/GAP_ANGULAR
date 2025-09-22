package ma.gap.dtos;

import ma.gap.enums.TypeNomenclature;

public class NomenclatureQteRestDto {
    private Long id;
    private String designation;
    private Double quantiteTot;
    private Double quantiteLivre;
    private Double quantiteRest;
    private String unite;
    private TypeNomenclature type;
    private Long ordreFabricationId;
    private String numOF;
    private String articleDesignation;

    // Constructeurs
    public NomenclatureQteRestDto() {}

    public NomenclatureQteRestDto(Long id, String designation, Double quantiteTot,
                                  Double quantiteLivre, Double quantiteRest, String unite,
                                  TypeNomenclature type, Long ordreFabricationId,
                                  String numOF, String articleDesignation) {
        this.id = id;
        this.designation = designation;
        this.quantiteTot = quantiteTot;
        this.quantiteLivre = quantiteLivre;
        this.quantiteRest = quantiteRest;
        this.unite = unite;
        this.type = type;
        this.ordreFabricationId = ordreFabricationId;
        this.numOF = numOF;
        this.articleDesignation = articleDesignation;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Double getQuantiteTot() {
        return quantiteTot;
    }

    public void setQuantiteTot(Double quantiteTot) {
        this.quantiteTot = quantiteTot;
    }

    public Double getQuantiteLivre() {
        return quantiteLivre;
    }

    public void setQuantiteLivre(Double quantiteLivre) {
        this.quantiteLivre = quantiteLivre;
    }

    public Double getQuantiteRest() {
        return quantiteRest;
    }

    public void setQuantiteRest(Double quantiteRest) {
        this.quantiteRest = quantiteRest;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public TypeNomenclature getType() {
        return type;
    }

    public void setType(TypeNomenclature type) {
        this.type = type;
    }

    public Long getOrdreFabricationId() {
        return ordreFabricationId;
    }

    public void setOrdreFabricationId(Long ordreFabricationId) {
        this.ordreFabricationId = ordreFabricationId;
    }

    public String getNumOF() {
        return numOF;
    }

    public void setNumOF(String numOF) {
        this.numOF = numOF;
    }

    public String getArticleDesignation() {
        return articleDesignation;
    }

    public void setArticleDesignation(String articleDesignation) {
        this.articleDesignation = articleDesignation;
    }

    @Override
    public String toString() {
        return "NomenclatureQteRestDto{" +
                "id=" + id +
                ", designation='" + designation + '\'' +
                ", quantiteTot=" + quantiteTot +
                ", quantiteLivre=" + quantiteLivre +
                ", quantiteRest=" + quantiteRest +
                ", unite='" + unite + '\'' +
                ", type=" + type +
                ", ordreFabricationId=" + ordreFabricationId +
                ", numOF='" + numOF + '\'' +
                ", articleDesignation='" + articleDesignation + '\'' +
                '}';
    }
}