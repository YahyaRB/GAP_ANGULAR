package ma.gap.dtos;

public  class DetailLivraisonRequest {
    private String type; // "OF_COMPLET" ou "NOMENCLATURE"
    private Long livraisonId;
    private Long ordreFabricationId; // Pour OF complet
    private Long nomenclatureId; // Pour nomenclature
    private double quantite;
    private String emplacement;
    private String observation;

    // Constructeurs, getters et setters
    public DetailLivraisonRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getLivraisonId() { return livraisonId; }
    public void setLivraisonId(Long livraisonId) { this.livraisonId = livraisonId; }

    public Long getOrdreFabricationId() { return ordreFabricationId; }
    public void setOrdreFabricationId(Long ordreFabricationId) { this.ordreFabricationId = ordreFabricationId; }

    public Long getNomenclatureId() { return nomenclatureId; }
    public void setNomenclatureId(Long nomenclatureId) { this.nomenclatureId = nomenclatureId; }

    public float getQuantite() { return (float) quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public String getEmplacement() { return emplacement; }
    public void setEmplacement(String emplacement) { this.emplacement = emplacement; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}