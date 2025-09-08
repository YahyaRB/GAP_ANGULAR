package ma.gap.service;

import ma.gap.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ma.gap.repository.ArticleAchRepository;
import ma.gap.repository.NomenclatureArticleAchRepository;
import ma.gap.repository.NomenclatureRepository;
import ma.gap.exceptions.NomenclatureNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import ma.gap.entity.ArticleAch;
import ma.gap.entity.Nomenclature;
import ma.gap.enums.TypeNomenclature;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.persistence.*;
import javax.transaction.Transactional;

@Service
public class NomenclatureImpService implements NomenclatureService{
	//	private static final Logger logger = LoggerFactory.getLogger(NomenclatureImpService.class);
	@Autowired
	private NomenclatureRepository nomenclatureRepository;

	@Autowired
	private ArticleAchRepository articleAchRepository;

	@Autowired
	private NomenclatureArticleAchRepository nomenclatureArticleAchRepository;

	@Autowired
	private NomenclatureArticleAchService nomenclatureArticleAchService;



	@Override
	public List<ArticleAch> getAllArticleAchs() {
		return articleAchRepository.findAll();
	}

	@Override
	public Nomenclature createNomenclature(Nomenclature nomenclature, List<NomenclatureArticleAch> articleAchList) {
		Nomenclature savedNomenclature = nomenclatureRepository.save(nomenclature);
		for (NomenclatureArticleAch articleAch : articleAchList) {
			articleAch.setNomenclature(savedNomenclature);
			nomenclatureArticleAchRepository.save(articleAch);
		}
		return savedNomenclature;
	}



	@Override
	public Nomenclature createNomenclatureIdPlan(Nomenclature nomenclature, List<NomenclatureArticleAch> articleAchList) {
		Nomenclature savedNomenclature = nomenclatureRepository.save(nomenclature);
		for (NomenclatureArticleAch articleAch : articleAchList) {
			articleAch.setNomenclature(savedNomenclature);
			nomenclatureArticleAchRepository.save(articleAch);
		}
		return savedNomenclature;
	}


	@Override
	public List<Nomenclature> getAllNomenclatures() {
		return nomenclatureRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	}

	@Override
	public Nomenclature getNomenclatureById(Long id) {
		return nomenclatureRepository.findById(id).orElse(null);
	}





	@Override
	public void updateNomenclature(Nomenclature nomenclature,long id){
		nomenclature.setId(id);
		nomenclatureRepository.save(nomenclature);

	}








	@Override
	public Optional<Nomenclature> findNomenclatureById(Long id) {
		return nomenclatureRepository.findById(id);
	}


	@Transactional
	@Override
	public void deleteNomenclature(Long id) {
		Optional<Nomenclature> nomenclatureOpt = nomenclatureRepository.findById(id);
		if (nomenclatureOpt.isPresent()) {
			Nomenclature nomenclature = nomenclatureOpt.get();
			List<NomenclatureArticleAch> articleAchs = nomenclatureArticleAchRepository.findByNomenclature(nomenclature);
			nomenclatureArticleAchRepository.deleteAll(articleAchs);
			nomenclatureRepository.delete(nomenclature);
		} else {
			throw new EntityNotFoundException("Nomenclature not found with id: " + id);
		}
	}



	@Override
	public ResponseEntity<byte[]> generateReport(Long id) throws JRException, IOException, NomenclatureNotFoundException {
		Optional<Nomenclature> nomenclatureOptional = findNomenclatureById(id);
		List<NomenclatureArticleAch> nomenclatureArticleAch=nomenclatureArticleAchRepository.findByNomenclature(nomenclatureOptional.get());
		if (!nomenclatureOptional.isPresent()) {
			throw new NomenclatureNotFoundException("OrdreFabrication with id " + id + " not found");
		}

		Nomenclature nomenclature = nomenclatureOptional.get();

		String jrxmlFileName;
		Resource resource;
		if (nomenclature.getType() == TypeNomenclature.Bois) {
			jrxmlFileName = "files/Nomenclature_Bois.jrxml";
			resource = new ClassPathResource("files/Nomenclature_Bois.jrxml");
		} else if (nomenclature.getType() == TypeNomenclature.Quincaillerie) {
			jrxmlFileName = "files/Nomenclature_Quincallerie.jrxml";
			resource = new ClassPathResource("files/Nomenclature_Quincallerie.jrxml");
		} else {
			throw new IllegalArgumentException("Unknown Nomenclature type: " + nomenclature.getType());
		}

		//Resource resource = new ClassPathResource(jrxmlFileName);
		if (!resource.exists()) {
			throw new FileNotFoundException("File " + resource.getFilename() + " not found in classpath");
		}


		JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));


		JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(nomenclatureArticleAch);

		HashMap<String, Object> parameters = new HashMap<>();
		if (nomenclature.getCreatedBy() != null) {
			parameters.put("creer_par", nomenclature.getCreatedBy());
		}
		parameters.put("numeroPlan",nomenclatureOptional.get().getId());



		JasperPrint report = JasperFillManager.fillReport(compileReport, parameters, beanCollectionDataSource);

		byte[] data = JasperExportManager.exportReportToPdf(report);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Ordre_Fabrication_" + nomenclature.getId() + ".pdf");

		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_PDF)
				.body(data);

	}

	@Override
	public List<NomenclatureArticleAch> findArticlesByNomenclature(Nomenclature nomenclature) {
		return nomenclatureArticleAchRepository.findByNomenclature(nomenclature);
	}


	@Override
	public Optional<Nomenclature> findById(Long id) {

		return nomenclatureRepository.findById(id) ;
	}


}

