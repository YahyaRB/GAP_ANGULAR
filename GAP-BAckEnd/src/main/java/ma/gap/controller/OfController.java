package ma.gap.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ma.gap.config.GlobalVariableConfig;
import ma.gap.entity.*;
import ma.gap.enums.StatutEntity;
import ma.gap.message.ResponseMessage;
import ma.gap.repository.DetailLivraisonRepository;
import ma.gap.repository.OfSearchDao;
import ma.gap.repository.*;
import ma.gap.service.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import ma.gap.dtos.EmployeeDTO;
import ma.gap.exceptions.OrdreFabricationNotFoundException;
import net.sf.jasperreports.engine.JRException;

import javax.servlet.http.HttpServletResponse;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ofs")
@AllArgsConstructor
public class OfController {
	private OrdreFabricationImpService fabricationImpService;
	private ProjetImpService projetService;
	private UserImpService userImpService;
	private OfSearchDao ofSearchDao;
	private OrdreFabricationService ordreFabricationService;
	private DetailLivraisonRepository detailLivraisonRepository;
	private GlobalVariableConfig globalVariableConfig;
	private FilesStorageService storageService;


	@GetMapping(value = "/getAll/{idUser}")
	public ResponseEntity<?> findAllLivraisons(@PathVariable("idUser") long idUser) {

		try {
			return ResponseEntity.ok( ordreFabricationService.findAllByAtelier(idUser));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des ordres de fabrication.");
		}
	}

	@PostMapping(value = "/add")
	public ResponseEntity<?> save(@RequestBody OrdreFabrication ordreFabricationn) {
		try {
			OrdreFabrication savedOrdreFabrication = fabricationImpService.saveOrdreFabrication(ordreFabricationn);

			// Récupérer l'ID de l'OrdreFabrication enregistré
			Long id = savedOrdreFabrication.getId();  // Si ton entité OrdreFabrication a un attribut 'id'

			// Retourner l'ID dans la réponse
			return ResponseEntity.status(HttpStatus.CREATED).body(id);  // Retourner l'ID dans la réponse
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
		}
	}


	@PutMapping(value = "/update/{id}")
	//@PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
	public ResponseEntity<?> edit(@PathVariable("id") long id, @RequestBody OrdreFabrication of) {
		try {
			Optional<OrdreFabrication> optionalOF = ordreFabricationService.findOFById(id);

			if (!optionalOF.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Odre de fabrication non trouvée. Édition impossible.");
			}

			OrdreFabrication updatedOF=fabricationImpService.editOf(of, id);
			return ResponseEntity.ok(updatedOF.getId());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
		}
	}

	@DeleteMapping(value = "/delete/{id}")
	// @PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
	public ResponseEntity<String> deleteLivraison(@PathVariable("id") long id) {
		try {

			fabricationImpService.deleteOrdreFabrication(id);
			return ResponseEntity.ok("Ordre de fabrication supprimé avec succès.");
		} catch (OrdreFabricationNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordre de fabrication non trouvée. Suppression impossible.");
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de l'ordre de fabrication.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur inattendue est survenue.");
		}
	}


	@GetMapping("/ImprimerOF/{id}")
	public ResponseEntity<byte[]> impressionLivraison(@PathVariable("id") Long id) throws JRException, IOException, OrdreFabricationNotFoundException {

		return fabricationImpService.generateOf(id);
	}
	@GetMapping("/ofs/Imprimerfiche/{id}")
	@PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
	public ResponseEntity<byte[]> generateFiche(@PathVariable("id") Long id) throws JRException, IOException {

		ResponseEntity<byte[]> OfEtat = null;
		try {
			OfEtat = fabricationImpService.generateOf(id);
		} catch (FileNotFoundException | EmptyResultDataAccessException | OrdreFabricationNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return OfEtat;
	}

	
	@GetMapping("/findOFByAtelierAndProjet")
	public ResponseEntity<?> findOFByAtelierAndProjet(@RequestParam("idAtelier") Long idAtelier , @RequestParam("idProjet") Long idProjet){
		try {
			return ResponseEntity.ok( ordreFabricationService.findOFByAtelierAndProjet(idAtelier,idProjet));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des ordres de fabrication.");
		}
	}
	
	@GetMapping("/ofs/PieceJointe/{id}")
	@PreAuthorize("hasAnyAuthority('admin','agentSaisie')")
	public ResponseEntity TelechargerPieceJointe(@PathVariable("id") Long id) throws JRException, IOException, EmptyResultDataAccessException, OrdreFabricationNotFoundException {
		OrdreFabrication of = fabricationImpService.findOFById(id).get();
		Path dossierPiecesJointes = Paths.get(globalVariableConfig.getGlobalVariable());
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + of.getPieceJointe() + ".pdf")
				.body(new UrlResource(Paths.get(dossierPiecesJointes + "/" + of.getPieceJointe() + ".pdf").toUri() + "/"));

	}

	@GetMapping("/Search")
	public List<OrdreFabrication> SearchOF(@RequestParam(value = "idUser") long idUser, @RequestParam("idof") String idOf, @RequestParam("idprojet") long idProjet, @RequestParam("idatelier") long idAtelier, @RequestParam("idarticle") long idArticle, @RequestParam("dateDebut") String dateDebut,@RequestParam("dateFin") String dateFin ) throws ParseException, OrdreFabricationNotFoundException, IOException {
	/*	List<OrdreFabrication> listeO=ordreFabricationService.findAll();
		for (OrdreFabrication ordreFabrication : listeO) {
			String charAt2 = Optional.ofNullable(ordreFabrication.getCreatedBy()).map(s -> s.length() > 2 ? String.valueOf(s.charAt(2)) : "").orElse("");
			String month = "";
			if (ordreFabrication.getCreatedDate() != null) {

				month = formatDate(ordreFabrication.getCreatedDate());
			}
			ordreFabrication.setNumOF("OF" + ordreFabrication.getCompteur() + "-" + month + " " + charAt2);
			ordreFabricationService.updateOf(ordreFabrication,ordreFabrication.getId());
			System.out.println(ordreFabrication);
		}*/
		List<OrdreFabrication> listeOfSearch = ofSearchDao.searchOF(idUser,idOf, idProjet, idAtelier, idArticle, dateDebut, dateFin);
		Collections.reverse(listeOfSearch);
		return listeOfSearch;
	}

	@GetMapping("/Projet/{idUser}/{id}")
	public List<OrdreFabrication> SearchOF(@PathVariable long idUser,@PathVariable String id) throws ParseException, OrdreFabricationNotFoundException, IOException {
		Ateliers at = null;
		List<OrdreFabrication> entete = null;
		Projet projet = projetService.findById(Long.valueOf(id)).get();
		User user = userImpService.findbyusername(idUser);

		for (Role role : user.getRoles()) {
			if (role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur")) {
				entete = fabricationImpService.ofByProject(projet);
			} else {
				for (Ateliers atelier : user.getAteliers()) {
					at = atelier;
				}
				entete = ordreFabricationService.ofByProjectAndAtelier( projet, at);
			}
		}



		return entete;
	}
	@PostMapping("/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.save(file);
			message = "Le fichier a été téléchargé avec succès: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Impossible de télécharger le fichier: " + file.getOriginalFilename() + ". Erreur: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}
	@PostMapping("savePjOFById/{id}")
	ResponseEntity<ResponseMessage> savePjSuiviCaisse(@PathVariable("id") long id,
													  @RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.updatePjOFById(id,file);
			message = "Le fichier a été téléchargé avec succès: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Impossible de télécharger le fichier: " + file.getOriginalFilename() + ". Erreur: " + e.getMessage();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}
	
	
	@GetMapping(value = "ordreFabrication/Projet/{id}/Search")
	public List<OrdreFabrication> getOfProjectFiltred(@PathVariable long idUser,@PathVariable String id, @RequestParam("idof") long idof, @RequestParam("atelier") long atelier, @RequestParam("libelle") String libelle,
									  @RequestParam("avancement") String avancement) throws ParseException {
		Ateliers at = null;
		List<OrdreFabrication> ofprojet = null;
		Projet projet = projetService.findById(Long.parseLong(id)).get();
		User user = userImpService.findbyusername(idUser);

		for (Role role : user.getRoles()) {
			if (role.getName().equals("admin") || role.getName().equals("logistique") || role.getName().equals("consulteur")) {
				ofprojet=fabricationImpService.ofByProject(projet);
			} else {
				for (Ateliers ateli : user.getAteliers()) {
					at = ateli;
					ofprojet=ordreFabricationService.ofByProjectAndAtelier( projet, at);
				}

			}
		}

		return ofprojet;
	}

	/*@GetMapping("/OF/export")
	public void exportToExcel(HttpServletResponse response, @RequestParam(value = "idof",defaultValue = "0") long idOf, @RequestParam(value = "idprojet",defaultValue = "0") long idProjet,
							  @RequestParam(value = "idatelier",defaultValue = "0") long idAtelier, @RequestParam(value = "idarticle",defaultValue = "0") long idArticle) throws ParseException, IOException {

		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		List<OrdreFabrication> listeOfSearch = ofSearchDao.searchOF(idOf, idProjet, idAtelier, idArticle);
		Collections.reverse(listeOfSearch);
		// Créer un nouveau classeur Excel
		Workbook workbook = new HSSFWorkbook();

		// Créer une feuille dans le classeur
		Sheet sheet = workbook.createSheet("Feuille1");

		// Créer un style pour la mise en page personnalisée
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		//font.setBold(true);
		font.setItalic(true);
		font.setFontName("Calibri");
		font.setFontHeightInPoints((short)12);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);




		// Créer une ligne pour les en-têtes de colonne

		Row headerRow = sheet.createRow(0);
		Cell cell0 = headerRow.createCell(0);
		cell0.setCellValue("id of");
		cell0.setCellStyle(style);
		Cell cell1 = headerRow.createCell(1);
		cell1.setCellValue("N° OF");
		cell1.setCellStyle(style);
		Cell cell2 = headerRow.createCell(2);
		cell2.setCellValue("Projet");
		cell2.setCellStyle(style);
		Cell cell3 = headerRow.createCell(3);
		cell3.setCellValue("Date OF");
		cell3.setCellStyle(style);
		Cell cell4 = headerRow.createCell(4);
		cell4.setCellValue("Date Fin Prévu");
		cell4.setCellStyle(style);
		Cell cell5 = headerRow.createCell(5);
		cell5.setCellValue("Atelier");
		cell5.setCellStyle(style);
		Cell cell6 = headerRow.createCell(6);
		cell6.setCellValue("Article");
		cell6.setCellStyle(style);
		Cell cell7 = headerRow.createCell(7);
		cell7.setCellValue("Qte Total");
		cell7.setCellStyle(style);




		// Remplir les données dans les lignes suivantes
		int rowNum = 1;

		for (OrdreFabrication rowData : listeOfSearch) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(rowData.getId());
			row.createCell(1).setCellValue("OF "+rowData.getCompteur()+
					" -"+String.format("%02d", rowData.getDate().getMonth())+" "+ rowData.getCreatedBy().charAt(2));
			row.createCell(2).setCellValue(rowData.getProjet().getCode()+" - "+rowData.getProjet().getDesignation());
			row.createCell(3).setCellValue(dateFormatter.format(rowData.getDate()));
			row.createCell(4).setCellValue(dateFormatter.format(rowData.getDateFin()));
			row.createCell(5).setCellValue(rowData.getAtelier().getDesignation());
			row.createCell(6).setCellValue(rowData.getArticle().getDesignation());
			row.createCell(7).setCellValue(rowData.getQuantite());

			// Ajouter d'autres colonnes si nécessaire
		}

		// Définir le type de contenu de la réponse HTTP
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=ListeOF.xls");

		// Écrire le classeur Excel dans la réponse HTTP
		OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();


	}*/
	@GetMapping("/OF/export")
	@PreAuthorize("hasAnyAuthority('admin','EXPORT')")
	public void exportToExcel(HttpServletResponse response,@RequestParam(value = "idUser") long idUser, @RequestParam(value = "idof",defaultValue = "") String idOf, @RequestParam(value = "idprojet",defaultValue = "0") long idProjet,
							  @RequestParam(value = "idatelier",defaultValue = "0") long idAtelier, @RequestParam(value = "idarticle",defaultValue = "0") long idArticle , @RequestParam("dateDebut") String dateDebut,@RequestParam("dateFin") String dateFin) throws ParseException, IOException {

		LocalDate.now();
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		userImpService.findbyusername(idUser);
		List<OrdreFabrication> listeOF = ofSearchDao.searchOF(idUser,idOf, idProjet, idAtelier, idArticle, dateDebut,dateFin);


		Collections.reverse(listeOF);
		// Créer un nouveau classeur Excel
		Workbook workbook = new HSSFWorkbook();

		// Créer une feuille dans le classeur
		Sheet sheet = workbook.createSheet("Feuille1");

		//font & Style
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setItalic(true);
		font.setFontName("Calibri");
		font.setFontHeightInPoints((short)12);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		//Font Encours & Style Encours
		Font fontEncours = workbook.createFont();
		CellStyle styleEnCours = workbook.createCellStyle();
		fontEncours.setFontName("Playbill");
		fontEncours.setFontHeightInPoints((short)11);
		fontEncours.setColor(IndexedColors.BLUE.getIndex());
		styleEnCours.setFont(fontEncours);

		//Font Encours & Style Encours
		Font fontEnRetard = workbook.createFont();
		CellStyle styleEnRetard = workbook.createCellStyle();
		fontEnRetard.setFontName("Playbill");
		fontEnRetard.setFontHeightInPoints((short)11);
		fontEnRetard.setColor(IndexedColors.RED.getIndex());
		styleEnRetard.setFont(fontEnRetard);

		//Font Terminé & Style Terminé
		Font fontTermine = workbook.createFont();
		CellStyle styleTermine = workbook.createCellStyle();
		fontTermine.setFontName("Playbill");
		fontTermine.setFontHeightInPoints((short)11);
		fontTermine.setColor(IndexedColors.GREEN.getIndex());
		styleTermine.setFont(fontTermine);


		// Créer une ligne pour les en-têtes de colonne
		Row headerRow = sheet.createRow(0);
		Cell cell0 = headerRow.createCell(0);
		cell0.setCellValue("Id");
		cell0.setCellStyle(style);
		Cell cell1 = headerRow.createCell(1);
		cell1.setCellValue("N° OF");
		cell1.setCellStyle(style);
		Cell cell2 = headerRow.createCell(2);
		cell2.setCellValue("Affaire");
		cell2.setCellStyle(style);
		Cell cell3 = headerRow.createCell(3);
		cell3.setCellValue("Libelle");
		cell3.setCellStyle(style);
		Cell cell4 = headerRow.createCell(4);
		cell4.setCellValue("Date OF");
		cell4.setCellStyle(style);
		Cell cell5 = headerRow.createCell(5);
		cell5.setCellValue("Date Fin Prévu");
		cell5.setCellStyle(style);
		Cell cell6 = headerRow.createCell(6);
		cell6.setCellValue("Nb Jours");
		cell6.setCellStyle(style);
		Cell cell7 = headerRow.createCell(7);
		cell7.setCellValue("Atelier");
		cell7.setCellStyle(style);
		Cell cell8 = headerRow.createCell(8);
		cell8.setCellValue("Qte Total");
		cell8.setCellStyle(style);
		Cell cell9 = headerRow.createCell(9);
		cell9.setCellValue("Qte Livré");
		cell9.setCellStyle(style);
		Cell cell10= headerRow.createCell(10);
		cell10.setCellValue("Qte à Livré");
		cell10.setCellStyle(style);
		Cell cell11 = headerRow.createCell(11);
		cell11.setCellValue("Avancement");
		cell11.setCellStyle(style);
		Cell cell12 = headerRow.createCell(12);
		cell12.setCellValue("N° Prix");
		cell12.setCellStyle(style);


		// Remplir les données dans les lignes suivantes
		int rowNum = 1;
		long idof;
		for (OrdreFabrication rowData : listeOF) {
			Row row = sheet.createRow(rowNum++);
			idof=rowData.getId();


			row.createCell(0).setCellValue(idof);
			row.createCell(1).setCellValue("OF "+rowData.getCompteur()+
					" -"+String.format("%02d", rowData.getDate().getMonth())+" "+ rowData.getCreatedBy().charAt(2));
			row.createCell(2).setCellValue(rowData.getProjet().getCode() +" - "+rowData.getProjet().getDesignation());
			row.createCell(3).setCellValue(rowData.getArticle().getDesignation());
			row.createCell(4).setCellValue(dateFormatter.format(rowData.getDate()));
			row.createCell(5).setCellValue(dateFormatter.format(rowData.getDateFin()));
			row.createCell(6).setCellValue(rowData.getTempsPrevu());
			row.createCell(7).setCellValue(rowData.getAtelier().getDesignation());
			row.createCell(8).setCellValue(rowData.getQuantite());
			//row.createCell(8).setCellValue((int) ((rowData.getAvancement()/100)*rowData.getQuantite()));
			if(rowData.getAvancement()<100) {
				if (detailLivraisonRepository.listeOfQteByIdOf(idof) != null) {
					row.createCell(9).setCellValue(rowData.getQuantite() - detailLivraisonRepository.listeOfQteByIdOf(idof).getQteRest());
					row.createCell(10).setCellValue(detailLivraisonRepository.listeOfQteByIdOf(idof).getQteRest());
				} else {
					if(rowData.getAvancement()==0) {
						row.createCell(9).setCellValue(0);
						row.createCell(10).setCellValue(rowData.getQuantite());
					}
					else{
						row.createCell(9).setCellValue(" - ");
						row.createCell(10).setCellValue(" - ");
					}
				}
			}else
				{
				row.createCell(9).setCellValue(rowData.getQuantite());
				row.createCell(10).setCellValue(0);
			}
			row.createCell(11).setCellValue(rowData.getAvancement()+"%");
			row.createCell(12).setCellValue(rowData.getArticle().getNumPrix());


			// Ajouter d'autres colonnes si nécessaire
		}

		// Définir le type de contenu de la réponse HTTP
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=ListeOfs.xls");

		// Écrire le classeur Excel dans la réponse HTTP
		OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();

	}



	@GetMapping("/DetailOF/export")
	@PreAuthorize("hasAnyAuthority('admin','EXPORT')")
	public void exportToExcelDetail(HttpServletResponse response,  @RequestParam("idUser") long idUser,@RequestParam("id") long id, @RequestParam(value = "idof",defaultValue = "0") long idof, @RequestParam(value = "atelier",defaultValue = "0") long atelier, @RequestParam("libelle") String libelle,
									@RequestParam(value = "avancement",defaultValue = "All") String avancement) throws IOException, ParseException {
		LocalDate todaysDate = LocalDate.now();
		String datefin;
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		Projet projet=projetService.findById(id).get();
		User user = userImpService.findbyusername(idUser);
		List<OrdreFabrication> listeOF = ofSearchDao.searchOFByProjet(user,id,idof,atelier,libelle,avancement);


		Collections.reverse(listeOF);
		// Créer un nouveau classeur Excel
		Workbook workbook = new HSSFWorkbook();

		// Créer une feuille dans le classeur
		Sheet sheet = workbook.createSheet("Feuille1");

		//font & Style
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setItalic(true);
		font.setFontName("Calibri");
		font.setFontHeightInPoints((short)12);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		//Font Encours & Style Encours
		Font fontEncours = workbook.createFont();
		CellStyle styleEnCours = workbook.createCellStyle();
		fontEncours.setFontName("Playbill");
		fontEncours.setFontHeightInPoints((short)11);
		fontEncours.setColor(IndexedColors.BLUE.getIndex());
		styleEnCours.setFont(fontEncours);

		//Font Encours & Style Encours
		Font fontEnRetard = workbook.createFont();
		CellStyle styleEnRetard = workbook.createCellStyle();
		fontEnRetard.setFontName("Playbill");
		fontEnRetard.setFontHeightInPoints((short)11);
		fontEnRetard.setColor(IndexedColors.RED.getIndex());
		styleEnRetard.setFont(fontEnRetard);

		//Font Terminé & Style Terminé
		Font fontTermine = workbook.createFont();
		CellStyle styleTermine = workbook.createCellStyle();
		fontTermine.setFontName("Playbill");
		fontTermine.setFontHeightInPoints((short)11);
		fontTermine.setColor(IndexedColors.GREEN.getIndex());
		styleTermine.setFont(fontTermine);


		// Créer une ligne pour les en-têtes de colonne
		Row headerRow = sheet.createRow(0);
		Cell cell0 = headerRow.createCell(0);
		cell0.setCellValue("N° OF");
		cell0.setCellStyle(style);
		Cell cell1 = headerRow.createCell(1);
		cell1.setCellValue("Libelle");
		cell1.setCellStyle(style);
		Cell cell2 = headerRow.createCell(2);
		cell2.setCellValue("Date OF");
		cell2.setCellStyle(style);
		Cell cell3 = headerRow.createCell(3);
		cell3.setCellValue("Date Fin Prévu");
		cell3.setCellStyle(style);
		Cell cell4 = headerRow.createCell(4);
		cell4.setCellValue("Nb Jours");
		cell4.setCellStyle(style);
		Cell cell5 = headerRow.createCell(5);
		cell5.setCellValue("Atelier");
		cell5.setCellStyle(style);
		Cell cell6 = headerRow.createCell(6);
		cell6.setCellValue("Qte Total");
		cell6.setCellStyle(style);
		Cell cell7 = headerRow.createCell(7);
		cell7.setCellValue("Qte Livré");
		cell7.setCellStyle(style);
		Cell cell8 = headerRow.createCell(8);
		cell8.setCellValue("Reste à Livrer");
		cell8.setCellStyle(style);
		Cell cell9 = headerRow.createCell(9);
		cell9.setCellValue("Avancement");
		cell9.setCellStyle(style);
		Cell cell10 = headerRow.createCell(10);
		cell10.setCellValue("Etat Avancement");
		cell10.setCellStyle(style);

		// Remplir les données dans les lignes suivantes
		int rowNum = 1;
		for (OrdreFabrication rowData : listeOF) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue("OF "+rowData.getCompteur()+
					" -"+String.format("%02d", rowData.getDate().getMonth())+" "+ rowData.getCreatedBy().charAt(2));
			row.createCell(1).setCellValue(rowData.getDescription());
			row.createCell(2).setCellValue(dateFormatter.format(rowData.getDate()));
			row.createCell(3).setCellValue(dateFormatter.format(rowData.getDateFin()));
			row.createCell(4).setCellValue(rowData.getTempsPrevu());
			row.createCell(5).setCellValue(rowData.getAtelier().getDesignation());
			row.createCell(6).setCellValue(rowData.getQuantite());
			row.createCell(7).setCellValue((int) ((rowData.getAvancement()/100)*rowData.getQuantite()));
			row.createCell(8).setCellValue((int) (((100-rowData.getAvancement())/100)*rowData.getQuantite()));
			row.createCell(9).setCellValue(rowData.getAvancement()+"%");
			row.createCell(10).setCellFormula("REPT(\"|\",J"+rowNum+"*100)");

			Cell cell = sheet.getRow(rowNum-1).getCell(10);
			if(rowData.getAvancement()==100){
				cell.setCellStyle(styleTermine);
			}else {
				datefin=dateFormatter.format(rowData.getDateFin());
				if(datefin.compareTo(String.valueOf(todaysDate))>0)
				cell.setCellStyle(styleEnCours);
				else cell.setCellStyle(styleEnRetard);

			}




			// Ajouter d'autres colonnes si nécessaire
		}

		// Définir le type de contenu de la réponse HTTP
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=DetailOf"+projet.getDesignation()+".xls");

		// Écrire le classeur Excel dans la réponse HTTP
		OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();

	}
	public static String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MM"); // Format du mois
		return sdf.format(date);
	}
}

