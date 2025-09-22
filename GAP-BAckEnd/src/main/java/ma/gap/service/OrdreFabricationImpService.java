package ma.gap.service;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import ma.gap.config.GlobalVariableConfig;
import ma.gap.dtos.OfProjectQteRestDto;
import ma.gap.entity.*;
import ma.gap.enums.StatutEntity;
import ma.gap.exceptions.ArticleNotFoundException;
import ma.gap.repository.OfSearchDao;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;

import ma.gap.repository.OrdreFabricationRepository;
import ma.gap.repository.PlanRepository;
import ma.gap.enums.StatutPlan;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import static ma.gap.controller.OfController.formatDate;

@Service
@AllArgsConstructor
public class OrdreFabricationImpService implements OrdreFabricationService{
	OrdreFabricationRepository ofRepository;
	CountUPService countUPService;
	OfSearchDao ofSearchDao;
	ArticleService articleService;
	UserImpService userImpService;
	private GlobalVariableConfig globalVariableConfig;
	private PlanRepository planRepository ;
	

	@Override
	public OrdreFabrication saveOrdreFabrication(OrdreFabrication ofe) throws IOException, OrdreFabricationNotFoundException, ArticleNotFoundException {
        int compteur=countUPService.saveCountUP(ofe.getDate(), ofe.getAtelier());
		Article article=ofe.getArticle();
		ofe.setCompteur(compteur);
		ofe.setFlag(StatutEntity.SAISI.valeur);
        ofe.setQteRest(ofe.getQuantite());
        
		OrdreFabrication saveOf = ofRepository.save(ofe);
		String charAt2 = Optional.ofNullable(saveOf.getCreatedBy()).map(s -> s.length() > 2 ? String.valueOf(s.charAt(2)) : "").orElse("");
		String month = "";
		if (saveOf.getCreatedDate() != null) {

			month = formatDate(saveOf.getCreatedDate());
		}

		saveOf.setNumOF("OF" + saveOf.getCompteur() + "-" + month + " " + charAt2);
        updateOf(saveOf,saveOf.getId());
		article.setQuantiteEnProd((float) ofe.getQuantite()+article.getQuantiteEnProd());
		long idArticle=article.getId();
		articleService.editArticle(article,idArticle);
		Plan plan =saveOf.getPlan();
		 if (plan != null && plan.getStatut() == StatutPlan.VALIDÉ) {
	            plan.setStatut(StatutPlan.LANCER);
	            planRepository.save(plan);
	        }

	        return saveOf;
		
	}

	@Override
	public List<OrdreFabrication> findAll(){
		return ofRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
	}


	@Override
	public OrdreFabrication editOf(OrdreFabrication of, Long id) throws  EmptyResultDataAccessException, ArticleNotFoundException {

		OrdreFabrication ofOld = ofRepository.findById(id).get();
		Article article=ofOld.getArticle();
		if(ofOld.getFlag()== StatutEntity.SAISI.valeur)
		{
			float newQte= (float) (of.getQuantite()-ofOld.getQuantite());
			System.out.println(of.getQuantite()+"-"+ofOld.getQuantite()+"="+newQte);
			float qteEnProd=article.getQuantiteEnProd();
			float qteLivre= (float) of.getQteLivre();

			article.setQuantiteEnProd(qteEnProd+newQte);
			System.out.println("Article : "+qteEnProd+"+"+newQte+"="+(qteEnProd+newQte));
			long articleId=article.getId();
			articleService.editArticle(article,articleId);
			ofOld.setQteRest(of.getQuantite()+qteLivre);
			ofOld.setArticle(of.getArticle());
			ofOld.setAtelier(of.getAtelier());
			ofOld.setAvancement(of.getAvancement());
			ofOld.setDate(of.getDate());
			ofOld.setDateFin(of.getDateFin());
			ofOld.setDescription(of.getDescription());
			ofOld.setPieceJointe(of.getPieceJointe());
			ofOld.setProjet(of.getProjet());
			ofOld.setTempsPrevu(of.getTempsPrevu());
			ofOld.setStatut(of.getStatut());
			ofOld.setQuantite(of.getQuantite());
			ofOld.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
			ofOld.setLastModifiedDate(new Date());
			ofOld.setFlag(ofOld.getFlag());
			return ofRepository.save(ofOld);
		}
		else
			return null;
	}
	@Override
	public OrdreFabrication updateOf(OrdreFabrication of, Long id) throws IOException,OrdreFabricationNotFoundException,EmptyResultDataAccessException{

            of.setId(id);
			return ofRepository.save(of);
	}
	@Override
	public void deleteOrdreFabrication(Long id) throws IOException,OrdreFabricationNotFoundException,EmptyResultDataAccessException{

		Optional<OrdreFabrication> of = ofRepository.findById(id);

		if(of.get().getFlag()== StatutEntity.SAISI.valeur) 
		{
			Article article=of.get().getArticle();
			long articleId=article.getId();
			article.setQuantiteEnProd((float) (article.getQuantiteEnProd()-of.get().getQuantite()));
			Path file = Paths.get(globalVariableConfig.getGlobalVariable()).resolve(of.get().getPieceJointe()+".pdf");

		    Files.deleteIfExists(file);
			ofRepository.deleteById(id);
		}
	}


	@Override
	public Optional<OrdreFabrication> findOFById(Long id) throws OrdreFabricationNotFoundException,EmptyResultDataAccessException{
		ofRepository.findById(id);
		return ofRepository.findById(id);
	}
	
	

//	@Override
//	public ResponseEntity<byte[]> generateOf(Long id) throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException, OrdreFabricationNotFoundException {
//		
//		Optional<OrdreFabrication> of = findOFById(id);
//		if(of.get().getFlag()== StatutEntity.SAISI.valeur) 
//		{
//			OrdreFabrication OrdreF =of.get();
//			OrdreF.setFlag(StatutEntity.LANCE.valeur);
//			ofRepository.save(OrdreF);
//		}
//		Resource resource = new ClassPathResource("files/OrdreFabrication.jrxml");
//
//        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(of.get()));
//        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));
//
//        HashMap<String, Object> map = new HashMap<>();
//		if(of.get().getCreatedBy() !=null)
//        map.put("creer_par",of.get().getCreatedBy());
//        
//        JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);
//
//        byte[] data = JasperExportManager.exportReportToPdf(report);
//        HttpHeaders headers = new HttpHeaders();
//        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Ordre_Fabrication_"+of.get().getId()+".pdf");
//           
//        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
//	}



	@Override
	public ResponseEntity<byte[]> generateOf(Long id) throws JRException, IOException, OrdreFabricationNotFoundException {
	    Optional<OrdreFabrication> ofOptional = findOFById(id);
	    if (!ofOptional.isPresent()) {
	        throw new OrdreFabricationNotFoundException("OrdreFabrication with id " + id + " not found");
	    }

	    OrdreFabrication ordreFabrication = ofOptional.get();
	    if (ordreFabrication.getFlag() == StatutEntity.SAISI.valeur) {
	        ordreFabrication.setFlag(StatutEntity.LANCE.valeur);
	        ofRepository.save(ordreFabrication);
	    }

	    Resource resource1 = new ClassPathResource("files/OrdreFabrication.jrxml");
	    if (!resource1.exists()) {
	        throw new FileNotFoundException("File " + resource1.getFilename() + " not found in classpath");
	    }
	    Resource resource2 = new ClassPathResource("files/Fiche d’autocontrôle.jrxml");
	    if (!resource2.exists()) {
	        throw new FileNotFoundException("File " + resource2.getFilename() + " not found in classpath");
	    }

	    List<JasperPrint> jasperPrintList = new ArrayList<>();

	    try (InputStream inputStream1 = resource1.getInputStream(); InputStream inputStream2 = resource2.getInputStream()) {

	        JasperReport compileReport1 = JasperCompileManager.compileReport(inputStream1);
	        JRBeanCollectionDataSource beanCollectionDataSource1 = new JRBeanCollectionDataSource(Collections.singleton(ordreFabrication));

	        HashMap<String, Object> parameters = new HashMap<>();
	        if (ordreFabrication.getCreatedBy() != null) {
	            parameters.put("creer_par", ordreFabrication.getCreatedBy());
	        }

	        JasperPrint report1 = JasperFillManager.fillReport(compileReport1, parameters, beanCollectionDataSource1);
	        jasperPrintList.add(report1);

	        JasperReport compileReport2 = JasperCompileManager.compileReport(inputStream2);
	        JRBeanCollectionDataSource beanCollectionDataSource2 = new JRBeanCollectionDataSource(Collections.singleton(ordreFabrication));

	        JasperPrint report2 = JasperFillManager.fillReport(compileReport2, parameters, beanCollectionDataSource2);
	        jasperPrintList.add(report2);

	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        JRPdfExporter exporter = new JRPdfExporter();
	        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
	        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
	        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
	        exporter.setConfiguration(configuration);
	        exporter.exportReport();

	        byte[] data = outputStream.toByteArray();

	        HttpHeaders headers = new HttpHeaders();
	        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Ordre_Fabrication_" + ordreFabrication.getId() + ".pdf");

	        return ResponseEntity.ok()
	                .headers(headers)
	                .contentType(MediaType.APPLICATION_PDF)
	                .body(data);
	    }
	}



//	@Override
//	public ResponseEntity<byte[]> generateFicheOf(Long id) throws JRException, IOException, OrdreFabricationNotFoundException {
//	    Optional<OrdreFabrication> ofOptional = findOFById(id);
//	    if (!ofOptional.isPresent()) {
//	        throw new OrdreFabricationNotFoundException("OrdreFabrication with id " + id + " not found");
//	    }
//
//	    OrdreFabrication ordreFabrication = ofOptional.get();
//	    if (ordreFabrication.getFlag() == StatutEntity.SAISI.valeur) {
//	        ordreFabrication.setFlag(StatutEntity.LANCE.valeur);
//	        ofRepository.save(ordreFabrication);
//	    }
//
//	    Resource resource = new ClassPathResource("files/Fiche d’autocontrôle.jrxml");
//	    if (!resource.exists()) {
//	        throw new FileNotFoundException("File " + resource.getFilename() + " not found in classpath");
//	    }
//
//	    try (InputStream inputStream = resource.getInputStream()) {
//	        JasperReport compileReport = JasperCompileManager.compileReport(inputStream);
//	        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(ordreFabrication));
//
//	        HashMap<String, Object> parameters = new HashMap<>();
//	        if (ordreFabrication.getCreatedBy() != null) {
//	            parameters.put("creer_par", ordreFabrication.getCreatedBy());
//	        }
//
//	        JasperPrint report = JasperFillManager.fillReport(compileReport, parameters, beanCollectionDataSource);
//	        byte[] data = JasperExportManager.exportReportToPdf(report);
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Ordre_Fabrication_" + ordreFabrication.getId() + ".pdf");
//
//	        return ResponseEntity.ok()
//	                .headers(headers)
//	                .contentType(MediaType.APPLICATION_PDF)
//	                .body(data);
//	    }
//	}
//	
	
	
	
	public void EnregistrerPieceJointe(OrdreFabrication ofe,MultipartFile file) throws IOException
	{
		Path dossierPiecesJointes = Paths.get(globalVariableConfig.getGlobalVariable());
        if (Files.notExists(dossierPiecesJointes)) {
            Files.createDirectory(dossierPiecesJointes);
        }
        String pieceJointe = null;
        if(file.getSize()!=0)
		{
			pieceJointe = UUID.randomUUID().toString();
			ofe.setPieceJointe(pieceJointe);
			Files.copy(file.getInputStream(), dossierPiecesJointes.resolve(pieceJointe+ ".pdf"));
		}
        else 
        {
        	if(ofe.getId() != null)  // Check IF IS UPDATE OPERATION OR INSERT
        	{
        		ofe.setPieceJointe(ofRepository.findById(ofe.getId()).get().getPieceJointe());
        	}
        }
        
        
        
	}
@Override
	public List<OrdreFabrication> ofByProject(Projet projet){


	List<OrdreFabrication> listeOfs=ofRepository.findAllByProjet(projet);


	return listeOfs;
}

@Override
public List<OrdreFabrication> ofByPlan(Plan plan){
List<OrdreFabrication> listeOfs=ofRepository.findAllByPlan(plan);

return listeOfs;
}

	@Override
	public List<OrdreFabrication> ofByProjectAndAtelier(Projet projet, Ateliers atelier){


		List<OrdreFabrication> listeOfs=ofRepository.findAllByProjetAndAtelierOrderByIdAsc(projet,atelier);



		return listeOfs;
	}

	@Override
	public List<OrdreFabrication> findOFByAtelierAndProjet(long idAtelier, long idProjet) {
		return ofRepository.findOFByAtelierAndProjet(idAtelier,idProjet);
	}

	@Override
	public List<OrdreFabrication> SearchofByProjectAndAtelier( User user, long idprojet, long idof, long atelier, String libelle, String avancement) throws ParseException {



		List<OrdreFabrication> listeOfs=ofSearchDao.searchOFByProjet(user,idprojet,idof,atelier,libelle,avancement);


		return listeOfs;
	}

	@Override
	public List<OrdreFabrication> findAllByAtelier(long idUser) {



			List<OrdreFabrication> listeOfs = new ArrayList<>();
			List<Ateliers> at = new ArrayList<Ateliers>();

			User user=userImpService.findbyusername(idUser);
            boolean isAuth=userImpService.isRoleAutorize(user.getId());
							if(isAuth){
					listeOfs = findAll();
				}
				else{
					for (Ateliers atelier:user.getAteliers()){
						at.add(atelier);
					}
					listeOfs=ofRepository.findAllByAtelierInOrderByIdDesc(at);
				}

			return listeOfs;
		}





}





