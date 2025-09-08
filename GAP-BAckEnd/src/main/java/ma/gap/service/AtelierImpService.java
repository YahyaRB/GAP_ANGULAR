package ma.gap.service;

import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.exceptions.ProjetNotFoundException;
import ma.gap.repository.AtelierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AtelierImpService implements AtelierService {

    @Autowired
    private AtelierRepository atelierRepository;

    @Autowired
    private UserImpService userImpService;

    @Override
    public List<Ateliers> allAteliers(long idUser) {

        User user = userImpService.findbyusername(idUser);

        List<Role> roles = user.getRoles();

        for (Role role : roles){
            if (role.getName().equals("agentSaisie")){
                List<Ateliers> ateliers = user.getAteliers();
                return ateliers;
            }else {
                return atelierRepository.findAll();
            }
        }
        return null;
    }

    @Override
    public Ateliers getAtelierById(Long id) {

        Optional<Ateliers> atelier = atelierRepository.findById(id);
        return atelier.get();
    }

    @Override
    public Ateliers getAtelierByCode(String code) {

        return atelierRepository.findByCode(code);
    }


}
