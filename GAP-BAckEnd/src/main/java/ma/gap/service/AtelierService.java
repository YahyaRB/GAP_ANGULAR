package ma.gap.service;

import ma.gap.entity.Ateliers;

import java.util.List;

public interface AtelierService {

    public List<Ateliers> allAteliers(long idUser);
    public Ateliers getAtelierById(Long id);
    public Ateliers getAtelierByCode(String code);
}
