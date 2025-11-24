package ma.gap.service;

import ma.gap.dtos.AffectationPreviewDTO;
import ma.gap.dtos.DuplicationRequestDTO;
import ma.gap.entity.AffectationUpdate;
import ma.gap.entity.Ateliers;
import org.springframework.data.domain.Page;

import java.text.ParseException;
import java.util.List;

/**
 * Service interface pour la gestion des affectations
 */
public interface AffectationUpService {

    /**
     * Récupère toutes les affectations pour un utilisateur donné
     * @param idUser ID de l'utilisateur
     * @return Liste des affectations
     */
    List<AffectationUpdate> allAffectation(long idUser);

    /**
     * Sauvegarde une nouvelle affectation
     * @param affectation L'affectation à sauvegarder
     * @return Message de confirmation
     */
    String saveAffectation(AffectationUpdate affectation);

    /**
     * Supprime une affectation
     * @param id ID de l'affectation à supprimer
     * @return true si suppression réussie, false sinon
     */
    boolean deleteAffectation(Long id);

    /**
     * Trouve une affectation par son ID
     * @param id ID de l'affectation
     * @return L'affectation trouvée
     */
    AffectationUpdate findById(Long id);

    /**
     * Vérifie l'existence d'une affectation
     * @param affectation L'affectation à vérifier
     * @return L'affectation existante ou null
     */
    AffectationUpdate affVerif(AffectationUpdate affectation);

    /**
     * Récupère la dernière affectation créée
     * @return La dernière affectation
     */
    AffectationUpdate lastAff();

    /**
     * Envoie un email à l'agent de saisie
     * @param affectationUpdate L'affectation concernée
     * @param ateliers L'atelier concerné
     */
    void sendEmailAgentSaisi(AffectationUpdate affectationUpdate, Ateliers ateliers);

    /**
     * Envoie un email au consulteur
     * @param affectationUpdate L'affectation concernée
     * @param ateliers L'atelier concerné
     */
    void sendEmailConsulteur(AffectationUpdate affectationUpdate, Ateliers ateliers);

    /**
     * Sauvegarde les affectations dupliquées
     * @param affectations Liste des affectations à sauvegarder
     * @return Message de résultat
     */
    String saveDuplicatedAffectations(List<AffectationPreviewDTO> affectations);

    /**
     * Duplique des affectations selon les critères donnés
     * @param request Requête de duplication
     * @return Message de résultat
     */
    String duplicateAffectations(DuplicationRequestDTO request);

    /**
     * Prévisualise une duplication d'affectations
     * @param request Requête de duplication
     * @return Liste des affectations qui seront dupliquées
     */
    List<AffectationPreviewDTO> previewDuplication(DuplicationRequestDTO request);

    /**
     * Récupère les affectations filtrées selon des critères avec pagination
     * @param idUser ID de l'utilisateur
     * @param idprojet ID du projet (0 pour tous)
     * @param idemploye ID de l'employé (0 pour tous)
     * @param idarticle ID de l'article (0 pour tous)
     * @param idatelier ID de l'atelier (0 pour tous)
     * @param dateDebut Date de début (format dd/MM/yyyy)
     * @param dateFin Date de fin (format dd/MM/yyyy)
     * @param page Numéro de page (commence à 0)
     * @param size Taille de la page
     * @return Page des affectations filtrées
     * @throws ParseException Si les dates ne sont pas dans le bon format
     */
    Page<AffectationUpdate> affectationFiltredPaginated(long idUser, long idprojet, long idemploye,
                                                        long idarticle, long idatelier,
                                                        String dateDebut, String dateFin,
                                                        int page, int size) throws ParseException;

    /**
     * Récupère les affectations filtrées selon des critères
     * @param idUser ID de l'utilisateur
     * @param idprojet ID du projet (0 pour tous)
     * @param idemploye ID de l'employé (0 pour tous)
     * @param idarticle ID de l'article (0 pour tous)
     * @param idatelier ID de l'atelier (0 pour tous)
     * @param dateDebut Date de début (format dd/MM/yyyy)
     * @param dateFin Date de fin (format dd/MM/yyyy)
     * @return Liste des affectations filtrées
     * @throws ParseException Si les dates ne sont pas dans le bon format
     */
    List<AffectationUpdate> affectationFiltred(long idUser, long idprojet, long idemploye,
                                               long idarticle, long idatelier,
                                               String dateDebut, String dateFin) throws ParseException;
}