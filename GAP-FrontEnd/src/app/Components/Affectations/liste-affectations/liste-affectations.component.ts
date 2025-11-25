import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { Ideplacement } from "../../../services/Interfaces/ideplacement";
import { Iateliers } from "../../../services/Interfaces/iateliers";
import { Iprojet } from "../../../services/Interfaces/iprojet";
import { Iemploye } from "../../../services/Interfaces/iemploye";
import { FormBuilder, FormGroup } from "@angular/forms";
import { TokenStorageService } from "../../../Auth/services/token-storage.service";
import { DeplacementService } from "../../../services/deplacement.service";
import { ProjetService } from "../../../services/projet.service";
import { RoleService } from "../../../services/role.service";
import { SortService } from "../../../services/sort.service";
import { Ilivraison } from "../../../services/Interfaces/ilivraison";
import { AffectationService } from "../../../services/affectation.service";
import { Iarticle } from "../../../services/Interfaces/iarticle";
import { ArticleService } from "../../../services/article.service";
import { ROLES_ADMIN_AGENTSAISIE } from "../../../Roles";
import { Iaffectation } from "../../../services/Interfaces/iaffectation";
import * as XLSX from 'xlsx';
import { EmployeService } from "../../../services/employe.service";
@Component({
  selector: 'app-liste-affectations',
  templateUrl: './liste-affectations.component.html',
  styleUrls: ['./liste-affectations.component.css']
})
export class ListeAffectationsComponent implements OnInit, OnChanges {
  @ViewChild(ListeAffectationsComponent) ListeUtilisateurs: ListeAffectationsComponent;

  POSTS: Iaffectation[] = [];  // Tableau pour stocker les données des livraisons
  page: number = 1;  // Numéro de la page courante pour la pagination
  count: number = 0;  // Compteur pour le nombre total d'éléments
  tableSize: number = 10;  // Taille par défaut de la page
  tableSizes: any = [5, 10, 15, 20];  // Options pour la taille de la page
  pfiltre: string = '';  // Filtre de recherche
  sortDirection: { [key: string]: boolean } = {};  // Direction de tri pour chaque colonne
  listeAteliers: Iateliers[] = [];  // Liste des ateliers
  listeAffairesByAtelier: Iprojet[] = [];
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idatelier: number = 0;  // Définir l'ID de l'atelier
  idemploye: number = 0;  // Définir l'ID de l'emplye
  dateDebut: string = '';  // Date de début pour la recherche
  dateFin: string = '';    // Date de fin pour la recherche
  affectationSelected: Iaffectation;
  listeEmploye: Iemploye[] = [];
  myFormSearch: FormGroup;
  articles: Iarticle[] = [];



  constructor(private tokenstorage: TokenStorageService,
    private formBuilder: FormBuilder,
    private affectationService: AffectationService,
    private roleService: RoleService,
    private sortService: SortService,
    private projetService: ProjetService,
    private articleService: ArticleService,
    private employeService: EmployeService
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser = this.tokenstorage.getUser().id
    this.listeAteliers = this.tokenstorage.getUser().atelier;
    this.projetService.getAffairesByAtelier(this.tokenstorage.getUser().id).subscribe(x => this.listeAffairesByAtelier = x);
    this.articleService.getArticlesByAtelier(this.tokenstorage.getUser().id).subscribe(data => this.articles = data);
    this.employeService.getAll(this.tokenstorage.getUser().id).subscribe(data => this.listeEmploye = data);
  }

  ngOnInit(): void {

    this.initmyForm();  // Initialisation du formulaire
    this.searchAffectation();  // Chargement des livraisons au démarrage du composant
  }


  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({
      idprojet: [],       // Valeur par défaut : vide
      idemploye: [],      // Valeur par défaut : vide
      idarticle: [],      // Valeur par défaut : vide
      idatelier: [],      // Valeur par défaut : vide
      dateDebut: [''],    // Pas de date par défaut
      dateFin: [''],      // Pas de date par défaut
    });

  }

  // @RequestParam("idUser") long idUser,@RequestParam("idemploye") long idemploye,@RequestParam("idprojet") long idprojet , @RequestParam("atelier") long idatelier,@RequestParam("motif") String motif,
  // @RequestParam("dateDebut") String dateDebut,
  // @RequestParam("dateFin") String dateFin) throws ParseException {
  // Méthode de tri des données par colonne


  // Méthode appelée lors du changement de page dans la pagination
  onTableDataChange(event: any) {
    this.page = event;  // Met à jour le numéro de page courant
    this.searchAffectation();  // Recharge les données en fonction de la nouvelle page
  }

  // Vérifie si l'utilisateur possède l'un des rôles spécifiés
  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }
  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }
  onMaterialGroupChange(event) {
    // Méthode vide, peut être utilisée pour gérer des changements de groupe de matériel si nécessaire
  }

  // Méthode appelée lors du changement de taille de la table
  onTableSizeChange(): void {
    this.page = 1;  // Réinitialiser à la première page lors du changement de taille de la table
  }
  sortColumn(column: string) {
    this.sortService.sortColumn(this.POSTS, column);
  }
  ngOnChanges(changes: SimpleChanges): void {
    // Méthode appelée lors des changements de propriétés ou d'input, ici on recharge les données des livraisons
    this.searchAffectation();
  }
  ClearSearch() {
    this.initmyForm();
    this.page = 1;  // Réinitialiser à la première page
    this.searchAffectation();
  }
  searchAffectation(): void {

    this.affectationService.searchAffectation(
      this.idUser,
      this.myFormSearch.value.idemploye ?? 0,
      this.myFormSearch.value.idprojet ?? 0,
      this.myFormSearch.value.idarticle ?? 0,
      this.myFormSearch.value.idatelier ?? 0,
      this.myFormSearch.value.dateDebut || '',
      this.myFormSearch.value.dateFin || '',
      this.page - 1,  // Spring Data Page commence à 0
      this.tableSize
    ).subscribe(
      (response) => {
        this.POSTS = response.content;  // Les données paginées
        this.count = response.totalItems;  // Total d'éléments

        setTimeout(() => {
          // this.extractUniqueTables();
        }, 1000);

      },
      (error) => {
        console.error('Erreur lors de la recherche des affectations:', error);
      }
    );

  }

  recupItem(affectation: Iaffectation) {
    this.affectationSelected = affectation;
  }
  ImprimeLivraison(livraison: Ilivraison) {
    /*   this.detailService.impressionLivraison(livraison.id).subscribe(
         (response: Blob) => {
           const url = window.URL.createObjectURL(response);
           const a = document.createElement('a');
           a.href = url;
           a.download = `BL_${livraison.id}.pdf`;
           document.body.appendChild(a);
           a.click();
           window.URL.revokeObjectURL(url);
           a.remove(); // Nettoyage après téléchargement
         },
         error => {
           console.error('Error downloading the file', error);
         }
       );*/
  }

  exportExel(): void {
    try {
      // Appliquer le filtre de recherche textuel si présent
      let filteredData = this.applyFilter(this.POSTS, this.pfiltre);

      // Appliquer les filtres avancés du formulaire
      if (this.myFormSearch) {
        const formValues = this.myFormSearch.value;

        if (formValues.idprojet && formValues.idprojet !== 0) {
          filteredData = filteredData.filter(affectation =>
            affectation.projets?.id === formValues.idprojet
          );
        }

        if (formValues.idemploye && formValues.idemploye !== 0) {
          filteredData = filteredData.filter(affectation =>
            affectation.employees?.id === formValues.idemploye
          );
        }

        if (formValues.idarticle && formValues.idarticle !== 0) {
          filteredData = filteredData.filter(affectation =>
            affectation.article?.id === formValues.idarticle
          );
        }

        if (formValues.idatelier && formValues.idatelier !== 0) {
          filteredData = filteredData.filter(affectation =>
            affectation.ateliers?.id === formValues.idatelier
          );
        }

        // Filtrer par dates si spécifiées
        if (formValues.dateDebut) {
          const dateDebut = new Date(formValues.dateDebut);
          filteredData = filteredData.filter(affectation => {
            const affectationDate = new Date(affectation.date);
            return affectationDate >= dateDebut;
          });
        }

        if (formValues.dateFin) {
          const dateFin = new Date(formValues.dateFin);
          filteredData = filteredData.filter(affectation => {
            const affectationDate = new Date(affectation.date);
            return affectationDate <= dateFin;
          });
        }
      }

      if (filteredData.length === 0) {
        alert('Aucune donnée correspondant aux filtres à exporter');
        return;
      }

      // Préparer les données pour l'export
      const exportData = filteredData.map((affectation, index) => ({
        'N°': index + 1,
        'Date': this.formatDateForExport(affectation.date),
        'Période': affectation.periode || '',
        'Nombre d\'Heures': affectation.nombreHeures || 0,
        'Code Projet': affectation.projets?.code || '',
        'Désignation Projet': affectation.projets?.designation || '',
        'Atelier': affectation.ateliers?.designation || '',
        'Article N° Prix': affectation.article?.numPrix || '',
        'Article Désignation': affectation.article?.designation || '',
        'Article Quantité': affectation.article?.quantiteTot || 0,
        'Article Unité': affectation.article?.unite || '',
        'Employé': this.formatEmployeeForExport(affectation.employees),
        'Matricule Employé': affectation.employees?.matricule || ''
      }));

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes
      const colWidths = [
        { wch: 5 },   // N°
        { wch: 12 },  // Date
        { wch: 15 },  // Période
        { wch: 15 },  // Nombre d'Heures
        { wch: 15 },  // Code Projet
        { wch: 30 },  // Désignation Projet
        { wch: 20 },  // Atelier
        { wch: 15 },  // Article N° Prix
        { wch: 35 },  // Article Désignation
        { wch: 15 },  // Article Quantité
        { wch: 10 },  // Article Unité
        { wch: 40 },  // Employé
        { wch: 15 }   // Matricule Employé
      ];
      ws['!cols'] = colWidths;

      // Styliser les en-têtes
      const headerStyle = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4472C4" } },
        alignment: { horizontal: "center", vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "000000" } },
          bottom: { style: "thin", color: { rgb: "000000" } },
          left: { style: "thin", color: { rgb: "000000" } },
          right: { style: "thin", color: { rgb: "000000" } }
        }
      };

      // Appliquer le style aux en-têtes (ligne 1)
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1', 'F1', 'G1', 'H1', 'I1', 'J1', 'K1', 'L1', 'M1'];
      headerCells.forEach(cell => {
        if (ws[cell]) {
          ws[cell].s = headerStyle;
        }
      });

      // Ajouter des styles alternés pour les lignes de données
      const dataStyle = {
        alignment: { vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "CCCCCC" } },
          bottom: { style: "thin", color: { rgb: "CCCCCC" } },
          left: { style: "thin", color: { rgb: "CCCCCC" } },
          right: { style: "thin", color: { rgb: "CCCCCC" } }
        }
      };

      // Appliquer le style aux données (à partir de la ligne 2)
      for (let row = 2; row <= exportData.length + 1; row++) {
        headerCells.forEach((_, colIndex) => {
          const cellAddress = XLSX.utils.encode_cell({ r: row - 1, c: colIndex });
          if (ws[cellAddress]) {
            ws[cellAddress].s = {
              ...dataStyle,
              fill: row % 2 === 0 ?
                { fgColor: { rgb: "F8F9FA" } } :
                { fgColor: { rgb: "FFFFFF" } }
            };
          }
        });
      }

      // Ajouter la worksheet au workbook
      XLSX.utils.book_append_sheet(wb, ws, 'Liste Affectations');

      // Ajouter une feuille de résumé
      const summaryData = [
        { 'Information': 'Nombre total d\'affectations', 'Valeur': filteredData.length },
        { 'Information': 'Total heures affectées', 'Valeur': filteredData.reduce((sum, aff) => sum + (aff.nombreHeures || 0), 0) },
        { 'Information': 'Nombre de projets distincts', 'Valeur': new Set(filteredData.map(aff => aff.projets?.id)).size },
        { 'Information': 'Nombre d\'ateliers distincts', 'Valeur': new Set(filteredData.map(aff => aff.ateliers?.id)).size },
        { 'Information': 'Nombre d\'employés distincts', 'Valeur': new Set(filteredData.map(aff => aff.employees?.id).filter(id => id)).size },
        { 'Information': 'Date d\'export', 'Valeur': new Date().toLocaleString('fr-FR') },
        { 'Information': 'Période couverte', 'Valeur': this.getDateRangeFromForm() }
      ];

      const summaryWs: XLSX.WorkSheet = XLSX.utils.json_to_sheet(summaryData);
      summaryWs['!cols'] = [{ wch: 30 }, { wch: 20 }];

      // Styliser la feuille de résumé
      const summaryHeaderCells = ['A1', 'B1'];
      summaryHeaderCells.forEach(cell => {
        if (summaryWs[cell]) {
          summaryWs[cell].s = headerStyle;
        }
      });

      XLSX.utils.book_append_sheet(wb, summaryWs, 'Résumé');

      // Générer le nom du fichier avec la date
      const currentDate = new Date();
      const dateStr = currentDate.toISOString().split('T')[0];
      const timeStr = currentDate.toTimeString().split(' ')[0].replace(/:/g, '-');
      const fileName = `Affectations_${dateStr}_${timeStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      // Message de succès
      console.log(`Export réussi: ${exportData.length} affectations exportées`);

      // Si vous avez un service de toast/notification
      // this.toastr.success(`${exportData.length} affectations exportées avec succès`, 'Export Excel');

    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      alert('Erreur lors de l\'export Excel. Veuillez réessayer.');
    }
  }

  /**
   * Applique le filtre de recherche textuel
   * @param data - Tableau des affectations
   * @param filter - Terme de recherche
   * @returns Tableau filtré
   */
  private applyFilter(data: Iaffectation[], filter: string): Iaffectation[] {
    if (!filter || filter.trim() === '') {
      return data;
    }

    const searchTerm = filter.toLowerCase().trim();

    return data.filter(affectation => {
      return (
        affectation.periode?.toLowerCase().includes(searchTerm) ||
        affectation.projets?.code?.toLowerCase().includes(searchTerm) ||
        affectation.projets?.designation?.toLowerCase().includes(searchTerm) ||
        affectation.ateliers?.designation?.toLowerCase().includes(searchTerm) ||
        affectation.article?.numPrix?.toLowerCase().includes(searchTerm) ||
        affectation.article?.designation?.toLowerCase().includes(searchTerm) ||
        affectation.article?.unite?.toLowerCase().includes(searchTerm) ||
        affectation.nombreHeures?.toString().includes(searchTerm) ||
        this.formatDateForExport(affectation.date).includes(searchTerm) ||
        this.formatEmployeeForExport(affectation.employees).toLowerCase().includes(searchTerm)
      );
    });
  }

  /**
   * Formate la date pour l'affichage dans Excel
   * @param date - Date à formater
   * @returns Date formatée en string
   */
  private formatDateForExport(date: Date | string): string {
    if (!date) return '';

    const dateObj = typeof date === 'string' ? new Date(date) : date;

    if (isNaN(dateObj.getTime())) return '';

    return dateObj.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  /**
   * Formate l'employé pour l'export
   * @param employee - L'employé
   * @returns String formatée de l'employé
   */
  private formatEmployeeForExport(employee: Iemploye): string {
    if (!employee) return '';

    const nom = employee.nom || '';
    const prenom = employee.prenom || '';
    const fullName = `${prenom} ${nom}`.trim();
    return fullName || 'Employé inconnu';
  }

  /**
   * Récupère la plage de dates du formulaire pour le résumé
   * @returns String de la plage de dates
   */
  private getDateRangeFromForm(): string {
    if (!this.myFormSearch) return 'Non définie';

    const formValues = this.myFormSearch.value;
    const dateDebut = formValues.dateDebut;
    const dateFin = formValues.dateFin;

    if (!dateDebut && !dateFin) return 'Non définie';

    const formatDate = (date: string) => {
      if (!date) return '';
      return new Date(date).toLocaleDateString('fr-FR');
    };

    return `Du ${formatDate(dateDebut)} au ${formatDate(dateFin)}`;
  }

  extractUniqueTables() {
    const uniqueProjectsMap = new Map<number, Iprojet>();
    const uniqueEmployeMap = new Map<number, Iemploye>();
    const uniqueAtelierMap = new Map<number, Iateliers>();
    this.POSTS.forEach(element => {
      if (!uniqueProjectsMap.has(element.projets.id)) {
        uniqueProjectsMap.set(element.projets.id, element.projets);
      }
      // Vérification pour les employés
      if (element.employees) {
        // Si `employee` est un tableau d'employés
        if (Array.isArray(element.employees)) {
          element.employees.forEach(emp => {
            if (emp.id && !uniqueEmployeMap.has(emp.id)) {
              uniqueEmployeMap.set(emp.id, emp);
            }
            if (emp.ateliers.id && !uniqueAtelierMap.has(emp.ateliers.id)) {
              uniqueAtelierMap.set(emp.ateliers.id, emp.ateliers);
            }
          });

        }
      }
    });

    this.listeAffairesByAtelier = Array.from(uniqueProjectsMap.values());
    this.listeAteliers = Array.from(uniqueAtelierMap.values());
    this.listeEmploye = Array.from(uniqueEmployeMap.values());
  }
  private getCurrentYearDates() {
    const currentYear = new Date().getFullYear();
    const startOfYear = new Date(currentYear, 0, 1); // 1er janvier
    const endOfYear = new Date(currentYear, 11, 31); // 31 décembre

    return {
      startDate: this.formatDate(startOfYear),
      endDate: this.formatDate(endOfYear)
    };
  }
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  protected readonly Date = Date;


  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;

}
