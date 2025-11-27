import { AfterViewInit, Component, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { Ilivraison } from "../../../services/Interfaces/ilivraison";  // Importation de l'interface Ilivraison
import { TokenStorageService } from "../../../Auth/services/token-storage.service";  // Service de stockage du token
import { FormBuilder, FormGroup } from "@angular/forms";  // Importation du service FormBuilder pour gérer les formulaires
import { UtilisateurService } from "../../../services/utilisateur.service";  // Service utilisateur
import { LivraisonService } from "../../../services/livraison.service";  // Service pour les livraisons
import { RoleService } from "../../../services/role.service";  // Service de gestion des rôles
import { NotificationService } from "../../../services/notification.service";  // Service de notifications
import { ROLES, ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE } from "../../../Roles";  // Constante des rôles d'administration
import { Iateliers } from "../../../services/Interfaces/iateliers";  // Interface pour les ateliers
import { Iprojet } from "../../../services/Interfaces/iprojet";  // Interface pour les projets
import { ChauffeurService } from "../../../services/chauffeur.service";  // Service pour les chauffeurs
import { Ichauffeur } from "../../../services/Interfaces/ichauffeur";  // Interface pour les chauffeurs
import { ProjetService } from "../../../services/projet.service";
import { Iuser } from "../../../services/Interfaces/iuser";
import { DetailLivraisonService } from "../../../services/detail-livraison.service";
import { AtelierService } from "../../../services/atelier.service";  // Service pour les projets
declare var $: any;  // Déclaration de jQuery pour l'utilisation des plugins JS
import * as XLSX from 'xlsx';
import { IdetailLivraison } from "../../../services/Interfaces/idetail-livraison";
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-liste-livraisons',  // Définition du sélecteur pour ce composant
  templateUrl: './liste-livraisons.component.html',  // Lien vers le fichier de template HTML
  styleUrls: ['./liste-livraisons.component.css']  // Lien vers le fichier CSS pour le style
})
export class ListeLivraisonsComponent implements OnInit, OnChanges, AfterViewInit {
  @ViewChild(ListeLivraisonsComponent) ListeUtilisateurs: ListeLivraisonsComponent;
  POSTS: any[] = [];  // Tableau pour stocker les données des livraisons
  page: number = 1;  // Numéro de la page courante pour la pagination
  count: number = 0;  // Compteur pour le nombre total d'éléments
  tableSize: number = 10;  // Taille par défaut de la page
  tableSizes: any = [5, 10, 15, 20];  // Options pour la taille de la page
  pfiltre: string = '';  // Filtre de recherche
  sortDirection: { [key: string]: boolean } = {};  // Direction de tri pour chaque colonne
  livraisons: Ilivraison[] = [];  // Liste des livraisons
  listeAteliers: Iateliers[] = [];  // Liste des ateliers
  listeaffaires: Iprojet[] = [];  // Liste des projets
  listeAffairesByAtelier: Iprojet[] = [];
  listeChauffeurs: Ichauffeur[] = [];  // Liste des chaffeurs
  livraisonSelected: Ilivraison; // Livraison selectionné
  ateliersUpdate: Iateliers[]; // Pour stocker l'atelier du livraison selectionné
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idchauffeur: number = 0;  // Définir l'ID du chauffeur
  idatelier: number = 0;  // Définir l'ID de l'atelier
  dateDebut: string = '';  // Date de début pour la recherche
  dateFin: string = '';    // Date de fin pour la recherche
  myFormSearch: FormGroup;

  // Propriétés pour le modal de progression
  showExportModal: boolean = false;
  exportProgress: number = 0;
  exportCurrentStep: string = '';


  constructor(private tokenstorage: TokenStorageService,
    private notifyService: NotificationService,
    private formBuilder: FormBuilder,
    private detailService: DetailLivraisonService,
    private chauffeurService: ChauffeurService,
    private projetService: ProjetService,
    private livraisonservice: LivraisonService,
    private roleService: RoleService,
    private toastr: ToastrService
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser = this.tokenstorage.getUser().id
    // Récupération des ateliers associés à l'utilisateur connecté depuis le service TokenStorage
    this.listeAteliers = this.tokenstorage.getUser().atelier;
    // Récupération des projets via le service ProjetService et stockage dans la variable listeaffaires
    this.projetService.getAll().subscribe(data => this.listeaffaires = data);
    this.chauffeurService.getAll().subscribe(data => this.listeChauffeurs = data);
    this.projetService.getAffairesByAtelier(this.tokenstorage.getUser().id).subscribe(x => this.listeAffairesByAtelier = x);
  }

  ngOnInit(): void {

    this.initmyForm();  // Initialisation du formulaire
    this.postList();  // Chargement des livraisons au démarrage du composant
  }

  postList(): void {
    this.searchLivraisons();
  }
  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({
      idprojet: [],        // Valeur par défaut : 0
      idchauffeur: [],     // Valeur par défaut : 0
      idatelier: [],       // Valeur par défaut : 0
      dateDebut: [''],      // Valeur par défaut : chaîne vide
      dateFin: ['']         // Valeur par défaut : chaîne vide
    });
  }
  // Méthode de tri des données par colonne
  sortColumn(column: string) {
    // Initialiser la direction de tri si elle n'est pas définie
    if (!(column in this.sortDirection)) {
      this.sortDirection[column] = true;  // Tri croissant par défaut
    }

    const isAscending = this.sortDirection[column];

    // Tri des données en fonction de la direction et de la colonne choisie
    this.POSTS.sort((a, b) => {
      const aValue = this.resolvePath(a, column);
      const bValue = this.resolvePath(b, column);

      if (aValue < bValue) return isAscending ? -1 : 1;
      if (aValue > bValue) return isAscending ? 1 : -1;
      return 0;
    });

    // Alterner le sens du tri pour la prochaine fois
    this.sortDirection[column] = !isAscending;
  }

  // Fonction utilitaire pour accéder aux propriétés imbriquées des objets (par exemple : 'user.name')
  resolvePath(obj: any, path: string) {
    return path.split('.').reduce((acc, key) => acc && acc[key], obj);
  }

  // Méthode appelée lors du changement de page dans la pagination
  onTableDataChange(event: any) {
    this.page = event;  // Met à jour le numéro de page courant
    this.postList();  // Recharge les données en fonction de la nouvelle page
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

  ngOnChanges(changes: SimpleChanges): void {
    // Méthode appelée lors des changements de propriétés ou d'input, ici on recharge les données des livraisons
    this.postList();
  }
  ClearSearch() {

    this.livraisonservice.getAll(this.idUser).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les livraisons retournées
        this.initmyForm();
      },
      (error) => {
        console.error('Erreur lors de la recherche des livraisons:', error);
      }
    );
  }
  searchLivraisons(): void {

    this.livraisonservice.searchLivraisons(
      this.idUser,
      this.myFormSearch.value.idprojet ?? 0,
      this.myFormSearch.value.idchauffeur ?? 0,
      this.myFormSearch.value.idatelier ?? 0,
      this.myFormSearch.value.dateDebut,
      this.myFormSearch.value.dateFin
    ).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les livraisons retournées
      },
      (error) => {
        console.error('Erreur lors de la recherche des livraisons:', error);
      }
    );
  }
  ngAfterViewInit(): void {

  }

  onDateDebutChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateDebut = input.value; // Met à jour la valeur de dateDebut
  }

  // Méthode appelée lorsque la date de fin change
  onDateFinChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateFin = input.value; // Met à jour la valeur de dateFin
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;

  recupItem(livraison: Ilivraison) {
    this.livraisonSelected = livraison;
    this.listeaffaires = [];
    this.listeaffaires.push(livraison.projet);
    this.ateliersUpdate = [];
    this.ateliersUpdate.push(livraison.atelier);


  }
  ImprimeLivraison(livraison: Ilivraison) {
    this.livraisonservice.impressionLivraison(livraison.id).subscribe(
      (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Bon_Livraison_${livraison.id}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove(); // Nettoyage après téléchargement
      },
      error => {
        console.error('Error downloading the file', error);
      }
    );
  }

  async exportExcel(): Promise<void> {
    try {
      // Afficher le modal de progression
      this.showExportModal = true;
      this.exportProgress = 0;
      this.exportCurrentStep = 'fetching';

      // Étape 1: Récupération des données (0-10%)
      // Commencer avec toutes les données des livraisons
      let filteredData = [...this.POSTS];

      // Appliquer le filtre de recherche textuel si présent
      if (this.pfiltre && this.pfiltre.trim() !== '') {
        const searchTerm = this.pfiltre.toLowerCase().trim();
        filteredData = filteredData.filter(livraison =>
          (livraison.id?.toString() || '').includes(searchTerm) ||
          (livraison.projet?.code?.toLowerCase() || '').includes(searchTerm) ||
          (livraison.projet?.designation?.toLowerCase() || '').includes(searchTerm) ||
          (livraison.atelier?.designation?.toLowerCase() || '').includes(searchTerm) ||
          (livraison.chauffeur?.nom?.toLowerCase() || '').includes(searchTerm) ||
          (livraison.chauffeur?.prenom?.toLowerCase() || '').includes(searchTerm)
        );
      }

      // Appliquer les filtres du formulaire de recherche si présents
      if (this.myFormSearch && this.myFormSearch.value) {
        const formValues = this.myFormSearch.value;

        if (formValues.idprojet) {
          filteredData = filteredData.filter(livraison => livraison.projet?.id === formValues.idprojet);
        }

        if (formValues.idatelier) {
          filteredData = filteredData.filter(livraison => livraison.atelier?.id === formValues.idatelier);
        }

        if (formValues.idchauffeur) {
          filteredData = filteredData.filter(livraison => livraison.chauffeur?.id === formValues.idchauffeur);
        }

        if (formValues.dateDebut) {
          const dateDebut = new Date(formValues.dateDebut);
          filteredData = filteredData.filter(livraison =>
            new Date(livraison.dateLivraison) >= dateDebut
          );
        }

        if (formValues.dateFin) {
          const dateFin = new Date(formValues.dateFin);
          filteredData = filteredData.filter(livraison =>
            new Date(livraison.dateLivraison) <= dateFin
          );
        }
      }

      if (filteredData.length === 0) {
        this.showExportModal = false;
        this.toastr.warning('Aucune donnée correspondant aux filtres à exporter', 'Export Excel');
        return;
      }

      // Étape 2: Chargement des détails (10-60%)
      this.exportProgress = 10;
      this.exportCurrentStep = 'processing';

      console.log('Chargement des détails pour', filteredData.length, 'livraisons...');

      const livraisonsAvecDetails: any[] = [];
      const totalLivraisons = filteredData.length;
      let processedCount = 0;

      // Utiliser Promise.all pour paralléliser les requêtes par lots pour ne pas surcharger le navigateur/serveur
      // Ou traiter séquentiellement pour mettre à jour la barre de progression

      for (const livraison of filteredData) {
        try {
          const details = await this.detailService.getListeDetailByLivraison(livraison.id).toPromise();
          livraisonsAvecDetails.push({
            ...livraison,
            detailLivraison: details
          });
        } catch (error) {
          console.error(`Erreur lors du chargement des détails pour la livraison ${livraison.id}:`, error);
          // Ajouter la livraison sans détails
          livraisonsAvecDetails.push({
            ...livraison,
            detailLivraison: []
          });
        }

        // Mettre à jour la progression
        processedCount++;
        // La phase de traitement va de 10% à 60% (donc 50% de la barre totale)
        const processingPercentage = Math.round((processedCount / totalLivraisons) * 50);
        this.exportProgress = 10 + processingPercentage;
      }

      // Étape 3: Génération du fichier Excel (60-80%)
      this.exportCurrentStep = 'generating';
      this.exportProgress = 60;

      // Simuler un petit délai pour que l'utilisateur voie l'étape
      await new Promise(resolve => setTimeout(resolve, 300));

      // Préparer les données pour l'export - PARCOURIR LES DETAILS DE LIVRAISON
      const exportData: any[] = [];

      livraisonsAvecDetails.forEach(livraison => {
        if (livraison.detailLivraison && livraison.detailLivraison.length > 0) {
          // Pour chaque détail de cette livraison, créer une ligne
          livraison.detailLivraison.forEach(detail => {
            exportData.push({
              'BL': `BL-${livraison.id}`,
              'Date Livraison': livraison.dateLivraison,
              'Ordre de Fabrication': detail.ordreFabrication?.numOF || '',
              'Description OF': detail.ordreFabrication?.description || '',
              'Designation Article': detail.ordreFabrication?.article?.designation || '',
              'Date OF': detail.ordreFabrication?.date || '',
              'Quantité OF': detail.ordreFabrication?.quantite || 0,
              'Quantité Livré': detail.quantite || 0,
              'Chauffeur': `${livraison.chauffeur?.nom || ''} ${livraison.chauffeur?.prenom || ''}`.trim(),
              'Atelier': livraison.atelier?.designation || '',
              'Affaire': `${livraison.projet?.code || ''}-${livraison.projet?.designation || ''}`.replace(/^-|-$/g, '')
            });
          });
        } else {
          // Si pas de détails, créer une ligne pour la livraison sans détails
          exportData.push({
            'BL': `BL-${livraison.id}`,
            'Date Livraison': livraison.dateLivraison,
            'Ordre de Fabrication': 'Aucun détail',
            'Description OF': '',
            'Designation Article': '',
            'Date OF': '',
            'Quantité OF': 0,
            'Quantité Livré': 0,
            'Chauffeur': `${livraison.chauffeur?.nom || ''} ${livraison.chauffeur?.prenom || ''}`.trim(),
            'Atelier': livraison.atelier?.designation || '',
            'Affaire': `${livraison.projet?.code || ''}-${livraison.projet?.designation || ''}`.replace(/^-|-$/g, '')
          });
        }
      });

      this.exportProgress = 80;

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes
      const colWidths = [
        { wch: 12 },  // BL
        { wch: 15 },  // Date Livraison
        { wch: 20 },  // Ordre de Fabrication
        { wch: 50 },  // Description OF
        { wch: 40 },  // Designation Article
        { wch: 12 },  // Date OF
        { wch: 12 },  // Quantité OF
        { wch: 15 },  // Quantité Livré
        { wch: 25 },  // Chauffeur
        { wch: 15 },  // Atelier
        { wch: 60 }   // Affaire
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

      // Appliquer le style aux en-têtes
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1', 'F1', 'G1', 'H1', 'I1', 'J1', 'K1'];
      headerCells.forEach(cell => {
        if (ws[cell]) {
          ws[cell].s = headerStyle;
        }
      });

      // Styles pour les données
      const dataStyle = {
        alignment: { vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "CCCCCC" } },
          bottom: { style: "thin", color: { rgb: "CCCCCC" } },
          left: { style: "thin", color: { rgb: "CCCCCC" } },
          right: { style: "thin", color: { rgb: "CCCCCC" } }
        }
      };

      // Appliquer le style aux données
      for (let row = 2; row <= exportData.length + 1; row++) {
        headerCells.forEach((_, colIndex) => {
          const cellAddress = XLSX.utils.encode_cell({ r: row - 1, c: colIndex });
          if (ws[cellAddress]) {
            // Formatage spécial pour les dates
            if (colIndex === 1 || colIndex === 5) {
              ws[cellAddress].s = {
                ...dataStyle,
                numFmt: "dd/mm/yyyy",
                fill: row % 2 === 0 ?
                  { fgColor: { rgb: "F8F9FA" } } :
                  { fgColor: { rgb: "FFFFFF" } }
              };
            } else {
              ws[cellAddress].s = {
                ...dataStyle,
                fill: row % 2 === 0 ?
                  { fgColor: { rgb: "F8F9FA" } } :
                  { fgColor: { rgb: "FFFFFF" } }
              };
            }
          }
        });
      }

      // Ajouter la worksheet au workbook
      XLSX.utils.book_append_sheet(wb, ws, 'Liste Livraisons');

      // Ajouter une feuille de résumé
      const totalDetails = exportData.filter(row => row['Ordre de Fabrication'] !== 'Aucun détail').length;
      const summaryData = [
        { 'Information': 'Nombre total de lignes', 'Valeur': exportData.length },
        { 'Information': 'Lignes avec détails', 'Valeur': totalDetails },
        { 'Information': 'Livraisons sans détails', 'Valeur': exportData.length - totalDetails },
        { 'Information': 'Livraisons différentes', 'Valeur': livraisonsAvecDetails.length },
        { 'Information': 'Chauffeurs différents', 'Valeur': new Set(filteredData.map(l => l.chauffeur?.id).filter(id => id)).size },
        { 'Information': 'Ateliers différents', 'Valeur': new Set(filteredData.map(l => l.atelier?.id).filter(id => id)).size },
        { 'Information': 'Affaires différentes', 'Valeur': new Set(filteredData.map(l => l.projet?.id).filter(id => id)).size },
        { 'Information': 'Date d\'export', 'Valeur': new Date().toLocaleString('fr-FR') },
        { 'Information': 'Filtre de recherche', 'Valeur': this.pfiltre?.trim() || 'Aucun filtre' }
      ];

      const summaryWs: XLSX.WorkSheet = XLSX.utils.json_to_sheet(summaryData);
      summaryWs['!cols'] = [{ wch: 50 }, { wch: 30 }];

      // Styliser la feuille de résumé
      const summaryHeaderCells = ['A1', 'B1'];
      summaryHeaderCells.forEach(cell => {
        if (summaryWs[cell]) {
          summaryWs[cell].s = headerStyle;
        }
      });

      XLSX.utils.book_append_sheet(wb, summaryWs, 'Résumé');

      // Étape 4: Enregistrement du fichier (80-100%)
      this.exportCurrentStep = 'saving';
      this.exportProgress = 90;

      await new Promise(resolve => setTimeout(resolve, 500));

      // Générer le nom du fichier
      const currentDate = new Date();
      const dateStr = currentDate.toISOString().split('T')[0];
      const timeStr = currentDate.toTimeString().split(' ')[0].replace(/:/g, '-');
      const fileName = `Livraisons_Details_${dateStr}_${timeStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      this.exportProgress = 100;

      // Fermer le modal et afficher le succès
      setTimeout(() => {
        this.showExportModal = false;
        this.toastr.success(
          `${exportData.length} lignes exportées avec succès`,
          'Export Excel réussi',
          {
            timeOut: 5000,
            progressBar: true,
            closeButton: true
          }
        );
        console.log(`Export réussi: ${exportData.length} lignes exportées avec ${totalDetails} détails`);
      }, 500);

    } catch (error) {
      this.showExportModal = false;
      console.error('Erreur lors de l\'export Excel:', error);
      this.toastr.error(
        'Une erreur est survenue lors de l\'export. Veuillez réessayer.',
        'Erreur d\'export',
        {
          timeOut: 5000,
          progressBar: true,
          closeButton: true
        }
      );
    }
  }

  // Fonction utilitaire pour les statistiques par atelier depuis les détails
  private getAtelierStatisticsFromDetails(details: IdetailLivraison[]): { atelier: string, count: number }[] {
    const atelierCount = new Map<string, number>();

    details.forEach(detail => {
      const atelierName = detail.livraison?.atelier?.designation || 'Non défini';
      atelierCount.set(atelierName, (atelierCount.get(atelierName) || 0) + 1);
    });

    return Array.from(atelierCount.entries())
      .map(([atelier, count]) => ({ atelier, count }))
      .sort((a, b) => b.count - a.count);
  }
  // Fonction utilitaire pour les statistiques par atelier


  // **MÉTHODES AUXILIAIRES pour reproduire le formatage SQL**

  private formatOrdreFabrication(of: any): string {
    if (!of) return '';
    // Reproduction de : 'OF ' + CAST(ofs.compteur AS VARCHAR(10)) + ' -' +RIGHT('0' + CAST(MONTH(ofs.date) AS VARCHAR(2)), 2) + ' ' + REPLACE(SUBSTRING(ofs.creer_par, 2, 2), '.', '')
    const compteur = of.compteur || '';
    const mois = of.date ? String(new Date(of.date).getMonth() + 1).padStart(2, '0') : '00';
    const creerPar = of.creerPar ? of.creerPar.substring(1, 3).replace('.', '') : '';
    return `OF ${compteur} -${mois} ${creerPar}`;
  }

  private formatChauffeur(chauffeur: any): string {
    if (!chauffeur) return '';
    return `${chauffeur.nom || ''} ${chauffeur.prenom || ''}`.trim();
  }

  private formatAffaire(projet: any): string {
    if (!projet) return '';
    return `${projet.code || ''}-${projet.designation || ''}`;
  }

  private formatDateForExport(date: Date): string {
    if (!date) return '';
    // Format YYYY-MM-DD comme dans CONVERT(VARCHAR(10), date, 120)
    return new Date(date).toISOString().split('T')[0];
  }

  private formatDateForSearch(date: Date): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('fr-FR').toLowerCase();
  }

  private getDateRangeFromData(data: any[]): string {
    if (!data.length) return 'Aucune donnée';

    const dates = data.map(d => new Date(d.dateLivraison)).filter(d => !isNaN(d.getTime()));
    if (!dates.length) return 'Dates invalides';

    const minDate = new Date(Math.min(...dates.map(d => d.getTime())));
    const maxDate = new Date(Math.max(...dates.map(d => d.getTime())));

    return `Du ${minDate.toLocaleDateString('fr-FR')} au ${maxDate.toLocaleDateString('fr-FR')}`;
  }


  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;


}
