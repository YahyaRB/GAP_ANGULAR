import {Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {ArticleService} from "../../../services/article.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {SortService} from "../../../services/sort.service";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {DeplacementService} from "../../../services/deplacement.service";
import {ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
import {Ideplacement} from "../../../services/Interfaces/ideplacement";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import * as XLSX from 'xlsx';
@Component({
  selector: 'app-liste-deplacements',
  templateUrl: './liste-deplacements.component.html',
  styleUrls: ['./liste-deplacements.component.css']
})
export class ListeDeplacementsComponent  implements OnInit, OnChanges {
  @ViewChild(ListeDeplacementsComponent) ListeUtilisateurs: ListeDeplacementsComponent;
  POSTS: Ideplacement[] = [];  // Tableau pour stocker les données des livraisons
  page: number = 1;  // Numéro de la page courante pour la pagination
  count: number = 0;  // Compteur pour le nombre total d'éléments
  tableSize: number = 10;  // Taille par défaut de la page
  pfiltre: string = '';  // Filtre de recherche
  listeAteliers: Iateliers[] = [];  // Liste des ateliers
  listeAffairesByAtelier:Iprojet[]=[];
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idatelier: number = 0;  // Définir l'ID de l'atelier
  idemploye: number = 0;  // Définir l'ID de l'emplye
  motif:string;
  dateDebut: string = '';  // Date de début pour la recherche
  dateFin: string = '';    // Date de fin pour la recherche
  deplacementSelected:Ideplacement;
  listeEmploye:Iemploye[]=[];
  myFormSearch: FormGroup;


  constructor(private tokenstorage: TokenStorageService,
              private formBuilder: FormBuilder,
              private deplacementService:DeplacementService,
              private projetService: ProjetService,
              private roleService: RoleService,
              private sortService: SortService
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser = this.tokenstorage.getUser().id
    this.listeAteliers = this.tokenstorage.getUser().atelier;
    this.projetService.getAffairesByAtelier(this.tokenstorage.getUser().id).subscribe(x=>this.listeAffairesByAtelier = x);
  }

  ngOnInit(): void {

    this.initmyForm();  // Initialisation du formulaire
    this.searchDeplacement();  // Chargement des livraisons au démarrage du composant
  }


  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({       // Valeur par défaut : 0
      motif:[''],
      idprojet: [],        // Valeur par défaut : 0
      idemploye: [],       // Valeur par défaut : 0
      idatelier: [],       // Valeur par défaut : 0
      dateDebut: [''],
      dateFin: [''],

    });
  }
  // @RequestParam("idUser") long idUser,@RequestParam("idemploye") long idemploye,@RequestParam("idprojet") long idprojet , @RequestParam("atelier") long idatelier,@RequestParam("motif") String motif,
  // @RequestParam("dateDebut") String dateDebut,
  // @RequestParam("dateFin") String dateFin) throws ParseException {
  // Méthode de tri des données par colonne


  // Méthode appelée lors du changement de page dans la pagination
  onTableDataChange(event: any) {
    this.page = event;  // Met à jour le numéro de page courant
    this.searchDeplacement();  // Recharge les données en fonction de la nouvelle page
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
    this.searchDeplacement();
  }
  ClearSearch() {
    this.initmyForm();
    this.searchDeplacement();
  }
  searchDeplacement(): void {
    this.deplacementService.searchDeplacement(

      this.idUser,
      this.myFormSearch.value.idemploye?? 0,
      this.myFormSearch.value.idprojet?? 0,
      this.myFormSearch.value.idatelier?? 0,
      this.myFormSearch.value.motif?? '',
        this.myFormSearch.value.dateDebut,
        this.myFormSearch.value.dateFin
    ).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les livraisons retournées
        setTimeout(() => {
          this.extractUniqueTables();
        }, 1000);

      },
      (error) => {
        console.error('Erreur lors de la recherche des livraisons:', error);
      }
    );

  }

  recupItem(deplacement: Ideplacement) {
    this.deplacementSelected=deplacement;
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
      // Commencer avec toutes les données
      let filteredData = [...this.POSTS];

      // Appliquer le filtre de recherche textuel si présent
      if (this.pfiltre && this.pfiltre.trim() !== '') {
        const searchTerm = this.pfiltre.toLowerCase().trim();
        filteredData = filteredData.filter(deplacement =>
          (deplacement.id?.toString() || '').includes(searchTerm) ||
          (deplacement.motif?.toLowerCase() || '').includes(searchTerm) ||
          (deplacement.nmbJours?.toString() || '').includes(searchTerm) ||
          (deplacement.projet?.code?.toLowerCase() || '').includes(searchTerm) ||
          (deplacement.projet?.designation?.toLowerCase() || '').includes(searchTerm) ||
          (deplacement.employee?.some(emp =>
            (emp.nom?.toLowerCase() || '').includes(searchTerm) ||
            (emp.prenom?.toLowerCase() || '').includes(searchTerm) ||
            (emp.ateliers?.designation?.toLowerCase() || '').includes(searchTerm)
          )) ||
          this.formatDateForSearch(deplacement.date).includes(searchTerm)
        );
      }

      // Appliquer les filtres avancés du formulaire (si vous en avez)
      if (this.myFormSearch) {
        const formValues = this.myFormSearch.value;

        if (formValues.idprojet && formValues.idprojet !== 0) {
          filteredData = filteredData.filter(deplacement =>
            deplacement.projet?.id === formValues.idprojet
          );
        }

        if (formValues.idemploye && formValues.idemploye !== 0) {
          filteredData = filteredData.filter(deplacement =>
            deplacement.employee?.some(emp => emp.id === formValues.idemploye)
          );
        }

        if (formValues.dateDebut) {
          const dateDebut = new Date(formValues.dateDebut);
          filteredData = filteredData.filter(deplacement => {
            const deplacementDate = new Date(deplacement.date);
            return deplacementDate >= dateDebut;
          });
        }

        if (formValues.dateFin) {
          const dateFin = new Date(formValues.dateFin);
          filteredData = filteredData.filter(deplacement => {
            const deplacementDate = new Date(deplacement.date);
            return deplacementDate <= dateFin;
          });
        }
      }

      if (filteredData.length === 0) {
        alert('Aucune donnée correspondant aux filtres à exporter');
        return;
      }

      // **MODIFICATION PRINCIPALE : Créer une ligne par employé**
      const exportData: any[] = [];
      let rowNumber = 1;

      filteredData.forEach(deplacement => {
        if (deplacement.employee && deplacement.employee.length > 0) {
          // Créer une ligne pour chaque employé
          deplacement.employee.forEach(employe => {
            exportData.push({
              'N°': rowNumber++,
              'ID Déplacement': deplacement.id || '',
              'Date Déplacement': this.formatDateForExport(deplacement.date),
              'Durée (jours)': deplacement.nmbJours || 0,
              'Motif': deplacement.motif || '',
              'Nom Employé': employe.nom || '',
              'Prénom Employé': employe.prenom || '',
              'Nom Complet': `${employe.nom || ''} ${employe.prenom || ''}`.trim(),
              'Matricule': employe.matricule || '',
              'Atelier': employe.ateliers?.designation || '',
              'Code Affaire': deplacement.projet?.code || '',
              'Désignation Affaire': deplacement.projet?.designation || '',
              'Affaire Complète': deplacement.projet ? `${deplacement.projet.code} - ${deplacement.projet.designation}` : '',
              'Pièce Jointe': deplacement.pieceJointe ? 'Oui' : 'Non',
              'Nb Employés Total': deplacement.employee?.length || 0
            });
          });
        } else {
          // Si pas d'employé, créer quand même une ligne
          exportData.push({
            'N°': rowNumber++,
            'ID Déplacement': deplacement.id || '',
            'Date Déplacement': this.formatDateForExport(deplacement.date),
            'Durée (jours)': deplacement.nmbJours || 0,
            'Motif': deplacement.motif || '',
            'Nom Employé': '',
            'Prénom Employé': '',
            'Nom Complet': '',
            'Matricule': '',
            'Atelier': '',
            'Code Affaire': deplacement.projet?.code || '',
            'Désignation Affaire': deplacement.projet?.designation || '',
            'Affaire Complète': deplacement.projet ? `${deplacement.projet.code} - ${deplacement.projet.designation}` : '',
            'Pièce Jointe': deplacement.pieceJointe ? 'Oui' : 'Non',
            'Nb Employés Total': 0
          });
        }
      });

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes (ajustée pour les nouvelles colonnes)
      const colWidths = [
        { wch: 5 },   // N°
        { wch: 12 },  // ID Déplacement
        { wch: 15 },  // Date Déplacement
        { wch: 12 },  // Durée (jours)
        { wch: 30 },  // Motif
        { wch: 20 },  // Nom Employé
        { wch: 20 },  // Prénom Employé
        { wch: 35 },  // Nom Complet
        { wch: 15 },  // Matricule
        { wch: 20 },  // Atelier
        { wch: 15 },  // Code Affaire
        { wch: 35 },  // Désignation Affaire
        { wch: 45 },  // Affaire Complète
        { wch: 12 },  // Pièce Jointe
        { wch: 12 }   // Nb Employés Total
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
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1', 'F1', 'G1', 'H1', 'I1', 'J1', 'K1', 'L1', 'M1', 'N1', 'O1'];
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

      // Appliquer le style aux données avec couleurs alternées par déplacement
      let currentDeplacementId = null;
      let colorToggle = false;

      for (let row = 2; row <= exportData.length + 1; row++) {
        const dataRow = exportData[row - 2];

        // Changer la couleur quand on change de déplacement
        if (dataRow['ID Déplacement'] !== currentDeplacementId) {
          currentDeplacementId = dataRow['ID Déplacement'];
          colorToggle = !colorToggle;
        }

        headerCells.forEach((_, colIndex) => {
          const cellAddress = XLSX.utils.encode_cell({ r: row - 1, c: colIndex });
          if (ws[cellAddress]) {
            ws[cellAddress].s = {
              ...dataStyle,
              fill: colorToggle ?
                { fgColor: { rgb: "F8F9FA" } } :
                { fgColor: { rgb: "FFFFFF" } }
            };
          }
        });
      }

      // Ajouter la worksheet au workbook
      XLSX.utils.book_append_sheet(wb, ws, 'Déplacements par Employé');

      // Ajouter une feuille de résumé mise à jour
      const totalEmployeesInDeplacements = exportData.length;
      const uniqueDeplacements = filteredData.length;
      const uniqueEmployees = new Set(exportData.map(row => row['Nom Complet']).filter(name => name)).size;

      const summaryData = [
        { 'Information': 'Nombre de déplacements', 'Valeur': uniqueDeplacements },
        { 'Information': 'Nombre total de lignes (employé-déplacement)', 'Valeur': totalEmployeesInDeplacements },
        { 'Information': 'Nombre d\'employés distincts', 'Valeur': uniqueEmployees },
        { 'Information': 'Total jours de déplacement', 'Valeur': filteredData.reduce((sum, dep) => sum + (dep.nmbJours || 0), 0) },
        { 'Information': 'Nombre de projets distincts', 'Valeur': new Set(filteredData.map(dep => dep.projet?.id).filter(id => id)).size },
        { 'Information': 'Déplacements avec pièce jointe', 'Valeur': filteredData.filter(dep => dep.pieceJointe).length },
        { 'Information': 'Durée moyenne par déplacement (jours)', 'Valeur': Math.round((filteredData.reduce((sum, dep) => sum + (dep.nmbJours || 0), 0) / filteredData.length) * 100) / 100 },
        { 'Information': 'Employés moyens par déplacement', 'Valeur': Math.round((totalEmployeesInDeplacements / uniqueDeplacements) * 100) / 100 },
        { 'Information': 'Date d\'export', 'Valeur': new Date().toLocaleString('fr-FR') },
        { 'Information': 'Période couverte', 'Valeur': this.getDateRangeFromData(filteredData) }
      ];

      const summaryWs: XLSX.WorkSheet = XLSX.utils.json_to_sheet(summaryData);
      summaryWs['!cols'] = [{ wch: 35 }, { wch: 20 }];

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
      const fileName = `Deplacements_ParEmploye_${dateStr}_${timeStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      // Message de succès
      console.log(`Export réussi: ${exportData.length} lignes exportées pour ${uniqueDeplacements} déplacements`);

    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      alert('Erreur lors de l\'export Excel. Veuillez réessayer.');
    }
  }

// Méthodes auxiliaires (inchangées)
  private formatDateForExport(date: Date): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR');
  }

  private formatDateForSearch(date: Date): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR').toLowerCase();
  }

  private getDateRangeFromData(data: any[]): string {
    if (!data.length) return 'Aucune donnée';

    const dates = data.map(d => new Date(d.date)).filter(d => !isNaN(d.getTime()));
    if (!dates.length) return 'Dates invalides';

    const minDate = new Date(Math.min(...dates.map(d => d.getTime())));
    const maxDate = new Date(Math.max(...dates.map(d => d.getTime())));

    return `Du ${minDate.toLocaleDateString('fr-FR')} au ${maxDate.toLocaleDateString('fr-FR')}`;
  }


// Méthodes auxiliaires
  private formatEmployeesForExport(employees: any[]): string {
    if (!employees || employees.length === 0) return '';
    return employees.map(emp => `${emp.nom || ''} ${emp.prenom || ''}`.trim()).join(', ');
  }







  extractUniqueTables() {
    const uniqueProjectsMap = new Map<number, Iprojet>();
    const uniqueEmployeMap = new Map<number, Iemploye>();
    const uniqueAtelierMap = new Map<number, Iateliers>();
    this.POSTS.forEach(element => {
      if (!uniqueProjectsMap.has(element.projet.id)) {
        uniqueProjectsMap.set(element.projet.id, element.projet);
      }
      // Vérification pour les employés
      if (element.employee) {
        // Si `employee` est un tableau d'employés
        if (Array.isArray(element.employee)) {
          element.employee.forEach(emp => {
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
    this.listeEmploye=Array.from(uniqueEmployeMap.values());
  }

  protected readonly Date = Date;

  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;


}
