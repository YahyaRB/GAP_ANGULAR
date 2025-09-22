package ma.gap.dtos;


public class OfProjectQteRestDtoImpl implements OfProjectQteRestDto {
    private Long id;
    private String numOF;
    private String designation;
    private Double qteRest;

    public OfProjectQteRestDtoImpl(Long id, String numOF, String designation, Double qteRest) {
        this.id = id;
        this.numOF = numOF;
        this.designation = designation;
        this.qteRest = qteRest;
    }

    @Override
    public Long getId() { return id; }

    @Override
    public String getNumOF() { return numOF; }

    @Override
    public String getDesignation() { return designation; }

    @Override
    public Double getQteRest() { return qteRest; }
}