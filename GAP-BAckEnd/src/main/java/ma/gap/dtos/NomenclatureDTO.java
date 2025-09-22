package ma.gap.dtos;

public class NomenclatureDTO {
    private Long id;
    private String type;
    private String designation;
    private String unite;
    private Double quantite;
    private Double quantiteRest;
    private Double quantiteLivre;
    private Long ordreFabricationId;

    // Constructeurs
    public NomenclatureDTO() {}

    public NomenclatureDTO(Long id, String type, String designation, String unite,
                           Double quantite, Double quantiteRest, Double quantiteLivre,
                           Long ordreFabricationId) {
        this.id = id;
        this.type = type;
        this.designation = designation;
        this.unite = unite;
        this.quantite = quantite;
        this.quantiteRest = quantiteRest;
        this.quantiteLivre = quantiteLivre;
        this.ordreFabricationId = ordreFabricationId;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }

    public Double getQuantite() { return quantite; }
    public void setQuantite(Double quantite) { this.quantite = quantite; }

    public Double getQuantiteRest() { return quantiteRest; }
    public void setQuantiteRest(Double quantiteRest) { this.quantiteRest = quantiteRest; }

    public Double getQuantiteLivre() { return quantiteLivre; }
    public void setQuantiteLivre(Double quantiteLivre) { this.quantiteLivre = quantiteLivre; }

    public Long getOrdreFabricationId() { return ordreFabricationId; }
    public void setOrdreFabricationId(Long ordreFabricationId) { this.ordreFabricationId = ordreFabricationId; }
}