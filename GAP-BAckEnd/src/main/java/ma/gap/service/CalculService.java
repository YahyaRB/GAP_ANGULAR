package ma.gap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ma.gap.entity.BiotimeInfo;
import ma.gap.entity.CalculPerProjet;

import java.text.ParseException;
import java.util.List;

public interface CalculService {

    public List<CalculPerProjet> getAffectationPerProject(Long id, Integer month,Integer year);
    public List<BiotimeInfo> bioTimeListEmp() throws JsonProcessingException, ParseException;
}
