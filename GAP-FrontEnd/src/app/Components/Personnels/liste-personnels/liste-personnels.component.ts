import {AfterViewInit, Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import { Ilivraison } from "../../../services/Interfaces/ilivraison";
import { Iateliers } from "../../../services/Interfaces/iateliers";
import { Ichauffeur } from "../../../services/Interfaces/ichauffeur";
import { FormBuilder, FormGroup } from "@angular/forms";
import { TokenStorageService } from "../../../Auth/services/token-storage.service";
import { NotificationService } from "../../../services/notification.service";
import { RoleService } from "../../../services/role.service";
import { Ifonction } from "../../../services/Interfaces/ifonction";
import { Iemploye } from "../../../services/Interfaces/iemploye";
import { FonctionService } from "../../../services/fonction.service";
import { EmployeService } from "../../../services/employe.service";
import { ROLES_ADMIN_AGENTSAISIE, ROLES_ADMIN_RH } from "../../../Roles";
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-liste-personnels',
  templateUrl: './liste-personnels.component.html',
  styleUrls: ['./liste-personnels.component.css']
})
export class ListePersonnelsComponent implements OnInit, OnChanges {
  @ViewChild(ListePersonnelsComponent) ListeUtilisateurs: ListePersonnelsComponent;
  POSTS: Iemploye[] = [];  // Tableau pour stocker les données
  page: number = 1;  // Numéro de la page courante pour la pagination
  count: number = 0;  // Compteur pour le nombre total d'éléments
  tableSize: number = 10;  // Taille par défaut de la page
  pfiltre: string = '';  // Filtre de recherche
  sortDirection: { [key: string]: boolean } = {};  // Direction de tri pour chaque colonne
  listeAteliers: Iateliers[] = [];  // Liste des ateliers
  listeFonctions: Ifonction[] = [];  // Liste des fonctions
  employeSelected: Iemploye; // Element selectionné
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  myFormSearch: FormGroup;

  constructor(private tokenstorage: TokenStorageService,
              private formBuilder: FormBuilder,
              private fonctionService: FonctionService,
              private employeService: EmployeService,
              private roleService: RoleService) {
    // Récupération de l'utilisateur connecté
    this.idUser = this.tokenstorage.getUser().id;
    this.listeAteliers = this.tokenstorage.getUser().atelier;
    this.fonctionService.getAllFonctions().subscribe(data => this.listeFonctions = data);
  }

  ngOnInit(): void {
    this.initmyForm();  // Initialisation du formulaire
    this.postList();  // Chargement des employés au démarrage du composant
  }

  postList(): void {
    this.searchEmploye();  // Recherche des employés
  }

  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({
      idfonction: [],  // Valeur par défaut : 0
      idatelier: [],   // Valeur par défaut : 0
      matricule: [''], // Valeur par défaut : chaîne vide
      nom: [''],       // Valeur par défaut : chaîne vide
      prenom: ['']     // Valeur par défaut : chaîne vide
    });
  }

  // Méthode de tri des données par colonne
  sortColumn(column: string) {
    if (!(column in this.sortDirection)) {
      this.sortDirection[column] = true;  // Tri croissant par défaut
    }

    const isAscending = this.sortDirection[column];

    this.POSTS.sort((a, b) => {
      const aValue = this.resolvePath(a, column);
      const bValue = this.resolvePath(b, column);

      if (aValue < bValue) return isAscending ? -1 : 1;
      if (aValue > bValue) return isAscending ? 1 : -1;
      return 0;
    });

    this.sortDirection[column] = !isAscending;
  }

  resolvePath(obj: any, path: string) {
    return path.split('.').reduce((acc, key) => acc && acc[key], obj);
  }

  // Méthode appelée lors du changement de page dans la pagination
  onTableDataChange(event: any) {
    this.page = event;
    this.postList();  // Recharge les données en fonction de la nouvelle page
  }

  onTableSizeChange(): void {
    this.page = 1;  // Réinitialiser à la première page lors du changement de taille de la table
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.postList();  // Recharge les données des employés en cas de changement de propriétés
  }

  ClearSearch() {
    this.employeService.getAll(this.idUser).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les employés retournées
        this.initmyForm();  // Réinitialise le formulaire
      },
      (error) => {
        console.error('Erreur lors de la recherche des employés:', error);
      }
    );
  }

  searchEmploye(): void {
    this.employeService.searchEmployes(
      this.idUser,
      this.myFormSearch.value.matricule,
      this.myFormSearch.value.nom,
      this.myFormSearch.value.prenom,
      this.myFormSearch.value.idatelier ?? 0,
      this.myFormSearch.value.idfonction ?? 0
    ).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les employés retournées
      },
      (error) => {
        console.error('Erreur lors de la recherche des employés:', error);
      }
    );
  }

  recupItem(employe: Iemploye) {
    this.employeSelected = employe;
  }

  // Exportation Excel



  // Vérifie si l'utilisateur possède l'un des rôles spécifiés
  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }
  exportExel(): void {
    try {
      // Commencer avec toutes les données
      let filteredData = [...this.POSTS];

      // Appliquer le filtre de recherche textuel si présent
      if (this.pfiltre && this.pfiltre.trim() !== '') {
        const searchTerm = this.pfiltre.toLowerCase().trim();
        filteredData = filteredData.filter(employe =>
          (employe.matricule?.toLowerCase() || '').includes(searchTerm) ||
          (employe.nom?.toLowerCase() || '').includes(searchTerm) ||
          (employe.prenom?.toLowerCase() || '').includes(searchTerm) ||
          (employe.ateliers?.designation?.toLowerCase() || '').includes(searchTerm) ||
          (employe.fonction?.designation?.toLowerCase() || '').includes(searchTerm)
        );
      }

      // Appliquer les filtres avancés du formulaire
      if (this.myFormSearch) {
        const formValues = this.myFormSearch.value;

        if (formValues.matricule && formValues.matricule.trim() !== '') {
          filteredData = filteredData.filter(employe =>
            employe.matricule?.toLowerCase().includes(formValues.matricule.toLowerCase())
          );
        }

        if (formValues.nom && formValues.nom.trim() !== '') {
          filteredData = filteredData.filter(employe =>
            employe.nom?.toLowerCase().includes(formValues.nom.toLowerCase())
          );
        }

        if (formValues.prenom && formValues.prenom.trim() !== '') {
          filteredData = filteredData.filter(employe =>
            employe.prenom?.toLowerCase().includes(formValues.prenom.toLowerCase())
          );
        }

        if (formValues.idatelier && formValues.idatelier !== 0) {
          filteredData = filteredData.filter(employe =>
            employe.ateliers?.id === formValues.idatelier
          );
        }

        if (formValues.idfonction && formValues.idfonction !== 0) {
          filteredData = filteredData.filter(employe =>
            employe.fonction?.id === formValues.idfonction
          );
        }
      }

      if (filteredData.length === 0) {
        alert('Aucune donnée correspondant aux filtres à exporter');
        return;
      }

      // Préparer les données pour l'export
      const exportData = filteredData.map((employe, index) => ({
        'N°': index + 1,
        'ID': employe.id || '',
        'Matricule': employe.matricule || '',
        'Nom': employe.nom || '',
        'Prénom': employe.prenom || '',
        'Atelier': employe.ateliers?.designation || '',
        'Fonction': employe.fonction?.designation || '',
        'Nom Complet': `${employe.nom || ''} ${employe.prenom || ''}`.trim()
      }));

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes
      const colWidths = [
        { wch: 5 },   // N°
        { wch: 8 },   // ID
        { wch: 15 },  // Matricule
        { wch: 20 },  // Nom
        { wch: 20 },  // Prénom
        { wch: 25 },  // Atelier
        { wch: 25 },  // Fonction
        { wch: 35 }   // Nom Complet
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
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1', 'F1', 'G1', 'H1'];
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
      XLSX.utils.book_append_sheet(wb, ws, 'Liste Employés');

      // Ajouter une feuille de résumé
      const summaryData = [
        { 'Information': 'Nombre total d\'employés', 'Valeur': filteredData.length },
        { 'Information': 'Nombre d\'ateliers représentés', 'Valeur': new Set(filteredData.map(emp => emp.ateliers?.id).filter(id => id)).size },
        { 'Information': 'Nombre de fonctions différentes', 'Valeur': new Set(filteredData.map(emp => emp.fonction?.id).filter(id => id)).size },
        { 'Information': 'Employés avec matricule', 'Valeur': filteredData.filter(emp => emp.matricule && emp.matricule.trim() !== '').length },
        { 'Information': 'Date d\'export', 'Valeur': new Date().toLocaleString('fr-FR') },
        { 'Information': 'Filtres appliqués', 'Valeur': this.getAppliedFiltersDescription() }
      ];

      const summaryWs: XLSX.WorkSheet = XLSX.utils.json_to_sheet(summaryData);
      summaryWs['!cols'] = [{ wch: 30 }, { wch: 25 }];

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
      const fileName = `Employes_${dateStr}_${timeStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      // Message de succès
      console.log(`Export réussi: ${exportData.length} employés exportés`);

    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      alert('Erreur lors de l\'export Excel. Veuillez réessayer.');
    }
  }

// Méthode auxiliaire pour décrire les filtres appliqués
  private getAppliedFiltersDescription(): string {
    if (!this.myFormSearch) return 'Aucun filtre';

    const formValues = this.myFormSearch.value;
    const filters: string[] = [];

    if (formValues.matricule?.trim()) filters.push(`Matricule: ${formValues.matricule}`);
    if (formValues.nom?.trim()) filters.push(`Nom: ${formValues.nom}`);
    if (formValues.prenom?.trim()) filters.push(`Prénom: ${formValues.prenom}`);
    if (formValues.idatelier && formValues.idatelier !== 0) {
      const atelier = this.listeAteliers?.find(a => a.id === formValues.idatelier);
      filters.push(`Atelier: ${atelier?.designation || formValues.idatelier}`);
    }
    if (formValues.idfonction && formValues.idfonction !== 0) {
      const fonction = this.listeFonctions?.find(f => f.id === formValues.idfonction);
      filters.push(`Fonction: ${fonction?.designation || formValues.idfonction}`);
    }
    if (this.pfiltre?.trim()) filters.push(`Recherche: ${this.pfiltre}`);

    return filters.length > 0 ? filters.join(', ') : 'Aucun filtre';
  }


  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
  protected readonly ROLES_ADMIN_RH = ROLES_ADMIN_RH;
}
