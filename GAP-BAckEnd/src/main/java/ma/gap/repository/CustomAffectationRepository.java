package ma.gap.repository;

import ma.gap.entity.AffectationUpdate;

import java.text.ParseException;
import java.util.List;

public interface CustomAffectationRepository {


    List<AffectationUpdate> affectationFiltred(long idUser,long idprojet, long idemploye, long idarticle,long idatelier, String dateDebut, String dateFin) throws ParseException;
}
