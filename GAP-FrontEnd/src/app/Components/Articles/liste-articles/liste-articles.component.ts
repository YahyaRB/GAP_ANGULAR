import {AfterViewInit, Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {OfService} from "../../../services/of.service";
import {ArticleService} from "../../../services/article.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
import {SortService} from "../../../services/sort.service";

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
  listeAffairesByAtelier:Iprojet[]=[];
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idatelier: number = 0;  // Définir l'ID de l'atelier
  idarticle: number = 0;  // Définir l'ID de l'article
  numPrix:string;
  designation:string;
  articleSelected:Iarticle;
  myFormSearch: FormGroup;


  constructor(private tokenstorage: TokenStorageService,
              private formBuilder: FormBuilder,
              private articleService:ArticleService,
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
      numPrix:[],             // Valeur par défaut : 0
      designation:[],
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
this.searchOF();
  }
  searchOF(): void {
    this.articleService.searchArticle(
      this.idUser,
      this.myFormSearch.value.numPrix?? '',
      this.myFormSearch.value.designation?? '',
      this.myFormSearch.value.idprojet?? 0,
      this.myFormSearch.value.idatelier?? 0,
      this.myFormSearch.value.idarticle?? 0,
    ).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les livraisons retournées
      },
      (error) => {
        console.error('Erreur lors de la recherche des livraisons:', error);
      }
    );
    setTimeout(() => {
      this.extractUniqueTables();
    }, 1000);

  }







  recupItem(article: Iarticle) {
        this.articleSelected=article;
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

  protected readonly Date = Date;
  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}
