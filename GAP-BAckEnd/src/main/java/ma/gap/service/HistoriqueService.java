package ma.gap.service;

import ma.gap.entity.Historique;

import java.util.List;
import java.util.Optional;

public interface HistoriqueService {
    Historique createHistorique(Historique historique);

    List<Historique> getAllHistorique();

    Optional<Historique> findHistoriqueById(long id);

    Historique saveHistorique(Historique Historique);

    void deleteHistorique(long id);

    Historique updateHistorique(Historique Historique ,long id);
}
