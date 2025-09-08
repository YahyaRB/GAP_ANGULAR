package ma.gap.service;

import ma.gap.entity.Fonction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FonctionService {

    List<Fonction> findAllFonctions();

     Optional<Fonction> findFonctionById(long id);

     Fonction saveFonction(Fonction fonction) ;

     Fonction updateFonction(Fonction fonction, long id) ;

     void deleteFonction(long id) ;
}

