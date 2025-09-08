package ma.gap.service;

import ma.gap.dtos.AffectationPreviewDTO;
import ma.gap.dtos.DuplicationRequestDTO;
import ma.gap.entity.AffectationUpdate;
import ma.gap.entity.Ateliers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.List;

public interface AffectationUpService {

    public List<AffectationUpdate> allAffectation(long idUser);
    public String saveAffectation(AffectationUpdate affectation);
    public boolean deleteAffectation(Long id);
    public AffectationUpdate findById(Long id);
    AffectationUpdate affVerif(AffectationUpdate affectation);
    AffectationUpdate lastAff();
    public void sendEmailAgentSaisi(AffectationUpdate affectationUpdate, Ateliers ateliers);
    public void sendEmailConsulteur(AffectationUpdate affectationUpdate,Ateliers ateliers);
    public String saveDuplicatedAffectations(List<AffectationPreviewDTO> affectations);
    public String duplicateAffectations(DuplicationRequestDTO request);
    public List<AffectationPreviewDTO> previewDuplication(DuplicationRequestDTO request);
    public List<AffectationUpdate> affectationFiltred(long idUser,long idprojet, long idemploye, long idarticle,long idatelier, String dateDebut, String dateFin) throws ParseException;
}
