package ma.gap.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import ma.gap.entity.*;
import ma.gap.exceptions.ArticleNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import ma.gap.exceptions.OrdreFabricationNotFoundException;
import net.sf.jasperreports.engine.JRException;

public interface OrdreFabricationService {

	public List<OrdreFabrication> findAll();

	void deleteOrdreFabrication(Long id)
			throws OrdreFabricationNotFoundException, EmptyResultDataAccessException, IOException;

	Optional<OrdreFabrication> findOFById(Long id)
			throws OrdreFabricationNotFoundException, EmptyResultDataAccessException;

	public ResponseEntity<byte[]> generateOf(Long id) throws JRException, FileNotFoundException, IOException,
			EmptyResultDataAccessException, OrdreFabricationNotFoundException;

	OrdreFabrication saveOrdreFabrication(OrdreFabrication ofe)
			throws IOException, OrdreFabricationNotFoundException, ArticleNotFoundException;

	OrdreFabrication editOf(OrdreFabrication of, Long id)
			throws IOException, OrdreFabricationNotFoundException, EmptyResultDataAccessException,
			ArticleNotFoundException;

	public OrdreFabrication updateOf(OrdreFabrication of, Long id)
			throws IOException, OrdreFabricationNotFoundException, EmptyResultDataAccessException;

	public List<OrdreFabrication> ofByProject(Projet projet);

	public List<OrdreFabrication> ofByProjectAndAtelier(Projet projet, Ateliers atelier);

	public List<OrdreFabrication> findOFByAtelierAndProjet(long idAtelier, long idProjet);

	public List<OrdreFabrication> SearchofByProjectAndAtelier(User user, long idprojet, long idof, long atelier,
			String libelle, String avancement) throws ParseException;

	List<OrdreFabrication> findAllByAtelier(long idUser);

	Page<OrdreFabrication> findAllByAtelier(long idUser, Pageable pageable);

	List<OrdreFabrication> ofByPlan(Plan plan);
	// ResponseEntity<byte[]> generateFicheOf(Long id) throws JRException,
	// IOException, OrdreFabricationNotFoundException;
}
