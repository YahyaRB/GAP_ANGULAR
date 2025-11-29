import { AfterViewInit, Component, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { Iateliers } from "../../../services/Interfaces/iateliers";
import { Iprojet } from "../../../services/Interfaces/iprojet";
import { Iarticle } from "../../../services/Interfaces/iarticle";
import { FormBuilder, FormGroup } from "@angular/forms";
import { TokenStorageService } from "../../../Auth/services/token-storage.service";
import { OfService } from "../../../services/of.service";
import { ArticleService } from "../../../services/article.service";
import { ProjetService } from "../../../services/projet.service";
import { RoleService } from "../../../services/role.service";
import { Ilivraison } from "../../../services/Interfaces/ilivraison";
import { ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE } from "../../../Roles";
import { SortService } from "../../../services/sort.service";
import * as XLSX from 'xlsx';
@Component({
  selector: 'app-liste-articles',
  templateUrl: './liste-articles.component.html',
  styleUrls: ['./liste-articles.component.css']
})
export class ListeArticlesComponent implements OnInit, OnChanges {
  @ViewChild(ListeArticlesComponent) ListeUtilisateurs: ListeArticlesComponent;
  POSTS: any[] = [];  // Tableau pour stocker les données des livraisons
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
  idarticle: number = 0;  // Définir l'ID de l'article
  numPrix: string;
  designation: string;
  articleSelected: Iarticle;
  myFormSearch: FormGroup;


  constructor(private tokenstorage: TokenStorageService,
    private formBuilder: FormBuilder,
    private articleService: ArticleService,
    private projetService: ProjetService,
    private roleService: RoleService,
    private sortService: SortService
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser = this.tokenstorage.getUser().id
    this.listeAteliers = this.tokenstorage.getUser().atelier;
  }

  ngOnInit(): void {

    this.initmyForm();  // Initialisation du formulaire
    this.searchOF();  // Chargement des livraisons au démarrage du composant
  }


  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({
      numPrix: [],             // Valeur par défaut : 0
      designation: [],
      idprojet: [],        // Valeur par défaut : 0
      idarticle: [],       // Valeur par défaut : 0
      idatelier: [],       // Valeur par défaut : 0
    });
  }
  // Méthode de tri des données par colonne


  // Méthode appelée lors du changement de page dans la pagination
  onTableDataChange(event: any) {
    this.page = event;  // Met à jour le numéro de page courant
    this.searchOF();  // Recharge les données en fonction de la nouvelle page
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
    this.searchOF();
  }
  sortColumn(column: string) {
    this.sortService.sortColumn(this.POSTS, column);
  }
  ngOnChanges(changes: SimpleChanges): void {
    // Méthode appelée lors des changements de propriétés ou d'input, ici on recharge les données des livraisons
    this.searchOF();
  }
  ClearSearch() {
    this.initmyForm();
    this.onSearchSubmit();
  }

  onSearchSubmit(): void {
    this.page = 1;
    this.searchOF();
  }

  searchOF(): void {
    const formValues = this.myFormSearch.value;
    const isFilterEmpty = !formValues.numPrix && !formValues.designation && !formValues.idprojet && !formValues.idatelier && !formValues.idarticle;

    if (isFilterEmpty) {
      this.articleService.getAll(this.idUser, this.page - 1, this.tableSize).subscribe({
        next: (data) => {
          this.POSTS = data.content;
          this.count = data.totalElements;
          // extractUniqueTables is disabled for server-side pagination
        },
        error: (error) => {
          console.error('Erreur lors du chargement des articles:', error);
        }
      });
    } else {
      this.articleService.searchArticle(
        this.idUser,
        formValues.numPrix || '',
        formValues.designation || '',
        formValues.idprojet || 0,
        formValues.idatelier || 0,
        formValues.idarticle || 0,
        this.page - 1,
        this.tableSize
      ).subscribe({
        next: (data) => {
          this.POSTS = data.content;
          this.count = data.totalElements;
          // extractUniqueTables is disabled for server-side pagination
        },
        error: (error) => {
          console.error('Erreur lors de la recherche des articles:', error);
        }
      });
    }
  }







  recupItem(article: Iarticle) {
    this.articleSelected = article;
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

  exportExel() {

  }

  extractUniqueTables() {
    const uniqueProjectsMap = new Map<number, Iprojet>();
    const uniqueAteliersMap = new Map<number, Iateliers>();

    this.POSTS.forEach(article => {
      if (!uniqueProjectsMap.has(article.projet.id)) {
        uniqueProjectsMap.set(article.projet.id, article.projet);
      }
      if (!uniqueAteliersMap.has(article.ateliers.id)) {
        uniqueAteliersMap.set(article.ateliers.id, article.ateliers);
      }
    });

    this.listeAffairesByAtelier = Array.from(uniqueProjectsMap.values());
    this.listeAteliers = Array.from(uniqueAteliersMap.values());
  }
  exportExcel(): void {
    try {
      // Appliquer le filtre sur les données (même logique que votre pipe search)
      const filteredData = this.applyFilter(this.POSTS, this.pfiltre);

      if (filteredData.length === 0) {
        alert('Aucune donnée à exporter');
        return;
      }

      // Préparer les données pour l'export
      const exportData = filteredData.map((article, index) => ({
        'N°': index + 1,
        'N° Prix': article.numPrix || '',
        'Désignation': article.designation || '',
        'Code Affaire': article.projet?.code || '',
        'Désignation Affaire': article.projet?.designation || '',
        'Unité': article.unite || '',
        'Quantité Totale': article.quantiteTot || 0,
        'Quantité Produite': article.quantiteProd || 0,
        'Quantité En Production': article.quantiteEnProd || 0,
        'Quantité Livrée': article.quantiteLivre || 0,
        'Quantité Posée': article.quantitePose || 0,
        'Atelier': article.ateliers?.designation || ''
      }));

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes
      const colWidths = [
        { wch: 5 },   // N°
        { wch: 15 },  // N° Prix
        { wch: 30 },  // Désignation
        { wch: 15 },  // Code Affaire
        { wch: 30 },  // Désignation Affaire
        { wch: 10 },  // Unité
        { wch: 15 },  // Quantité Totale
        { wch: 15 },  // Quantité Produite
        { wch: 18 },  // Quantité En Production
        { wch: 15 },  // Quantité Livrée
        { wch: 15 },  // Quantité Posée
        { wch: 20 }   // Atelier
      ];
      ws['!cols'] = colWidths;

      // Styliser les en-têtes (optionnel)
      const headerStyle = {
        font: { bold: true },
        fill: { fgColor: { rgb: "E3F2FD" } },
        alignment: { horizontal: "center" }
      };

      // Appliquer le style aux en-têtes (ligne 1)
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1', 'F1', 'G1', 'H1', 'I1', 'J1', 'K1', 'L1'];
      headerCells.forEach(cell => {
        if (ws[cell]) {
          ws[cell].s = headerStyle;
        }
      });

      // Ajouter la worksheet au workbook
      XLSX.utils.book_append_sheet(wb, ws, 'Liste Articles');

      // Générer le nom du fichier avec la date
      const currentDate = new Date();
      const dateStr = currentDate.toISOString().split('T')[0]; // Format YYYY-MM-DD
      const fileName = `Liste_Articles_${dateStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      // Afficher un message de succès
      console.log(`Export réussi: ${exportData.length} articles exportés`);

      // Optionnel: Afficher une notification toast si vous utilisez un service de notification
      // this.toastr.success(`${exportData.length} articles exportés avec succès`, 'Export Excel');

    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      alert('Erreur lors de l\'export Excel. Veuillez réessayer.');
    }
  }


  private applyFilter(data: Iarticle[], filter: string): Iarticle[] {
    if (!filter || filter.trim() === '') {
      return data;
    }

    const searchTerm = filter.toLowerCase().trim();

    return data.filter(article => {
      return (
        article.numPrix?.toLowerCase().includes(searchTerm) ||
        article.designation?.toLowerCase().includes(searchTerm) ||
        article.projet?.code?.toLowerCase().includes(searchTerm) ||
        article.projet?.designation?.toLowerCase().includes(searchTerm) ||
        article.unite?.toLowerCase().includes(searchTerm) ||
        article.ateliers?.designation?.toLowerCase().includes(searchTerm) ||
        article.quantiteTot?.toString().includes(searchTerm) ||
        article.quantiteProd?.toString().includes(searchTerm) ||
        article.quantiteEnProd?.toString().includes(searchTerm) ||
        article.quantiteLivre?.toString().includes(searchTerm) ||
        article.quantitePose?.toString().includes(searchTerm)
      );
    });
  }




  protected readonly Date = Date;
  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}
