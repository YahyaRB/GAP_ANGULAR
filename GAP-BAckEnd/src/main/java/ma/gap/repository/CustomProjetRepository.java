package ma.gap.repository;

import ma.gap.entity.Projet;

import java.util.List;

public interface CustomProjetRepository {

    List<Projet> projetList(String code,String affaire, String article, String atelier);
}
