import {Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {FormBuilder, FormGroup} from "@angular/forms";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {OfService} from "../../../services/of.service";
import {ArticleService} from "../../../services/article.service";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
import {SortService} from "../../../services/sort.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";

@Component({
  selector: 'app-liste-of',
  templateUrl: './liste-of.component.html',
  styleUrls: ['./liste-of.component.css']
})
export class ListeOFComponent implements OnInit, OnChanges{
  @ViewChild(ListeOFComponent) ListeUtilisateurs: ListeOFComponent;
  POSTS: any[] = [];  // Tableau pour stocker les données des livraisons
  page: number = 1;  // Numéro de la page courante pour la pagination
  count: number = 0;  // Compteur pour le nombre total d'éléments
  tableSize: number = 10;  // Taille par défaut de la page
  tableSizes: any = [5, 10, 15, 20];  // Options pour la taille de la page
  pfiltre: string = '';  // Filtre de recherche
  listeAteliers: Iateliers[] = [];  // Liste des ateliers
  listeAffairesByAtelier:Iprojet[]=[];
  listeArticles: Iarticle[] = [];  // Liste des articles
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idof: string ;  // Définir l'ID OF
  idatelier: number = 0;  // Définir l'ID de l'atelier
  idarticle: number = 0;  // Définir l'ID de l'article
  dateDebut: string = '';  // Date de début pour la recherche
  dateFin: string = '';    // Date de fin pour la recherche
  myFormSearch: FormGroup;
  ofSelected:IordreFabrication; // OF selectionné


  constructor(private tokenstorage: TokenStorageService,
              private formBuilder: FormBuilder,
              private ofService:OfService,
              private articleService:ArticleService,
              private projetService: ProjetService,
              private roleService: RoleService,
              private sortService: SortService
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser=this.tokenstorage.getUser().id
  }

  ngOnInit(): void {

    this.initmyForm();  // Initialisation du formulaire
    this.searchOF();  // Chargement des livraisons au démarrage du composant
  }

  postList(): void {
    this.searchOF();
  }
  private initmyForm() {
    this.myFormSearch = this.formBuilder.group({
      numOF:[''],             // Valeur par défaut : 0
      idprojet: [],        // Valeur par défaut : 0
      idarticle: [],       // Valeur par défaut : 0
      idatelier: [],       // Valeur par défaut : 0
      dateDebut: [''],     // Valeur par défaut : chaîne vide
      dateFin: ['']        // Valeur par défaut : chaîne vide
    });
  }
  // Méthode de tri des données par colonne
  sortColumn(column: string) {
    this.sortService.sortColumn(this.POSTS, column);
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
    this.initmyForm();
    this.searchOF();
 /*   this.ofService.getAll(this.idUser).subscribe(
      (data) => {
        this.POSTS = data;  // Stocke les livraisons retournées
        this.initmyForm();
      },
      (error) => {
        console.error('Erreur lors de la recherche des livraisons:', error);
      }
    );*/
  }
  searchOF(): void {
    this.ofService.searchOF(
      this.idUser,
      this.myFormSearch.value.numOF?? '',
      this.myFormSearch.value.idprojet?? 0,
      this.myFormSearch.value.idatelier?? 0,
      this.myFormSearch.value.idarticle?? 0,
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


  onDateDebutChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateDebut = input.value; // Met à jour la valeur de dateDebut
  }

  // Méthode appelée lorsque la date de fin change
  onDateFinChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateFin = input.value; // Met à jour la valeur de dateFin
  }

  recupItem(ordrefabrication: IordreFabrication) {
    this.ofSelected=ordrefabrication;
    this.listeAteliers = this.tokenstorage.getUser().atelier
  }
  recupItemAdd() {
    this.listeAteliers = this.tokenstorage.getUser().atelier;
  }
  ImprimeOF(of: IordreFabrication) {
    this.ofService.generateOf(of.id).subscribe(
      (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = of.numOF+'.pdf';
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

  exportExel() {

  }
  extractUniqueTables() {
    const uniqueProjectsMap = new Map<number, Iprojet>();
    const uniqueAteliersMap = new Map<number, Iateliers>();
    const uniqueArticlesMap = new Map<number, Iarticle>();

    this.POSTS.forEach(element => {
      if (!uniqueProjectsMap.has(element.projet.id)) {
        uniqueProjectsMap.set(element.projet.id, element.projet);
      }
      if (!uniqueAteliersMap.has(element.atelier.id)) {
        uniqueAteliersMap.set(element.atelier.id, element.atelier);
      }
      if (!uniqueArticlesMap.has(element.article.id)) {
        uniqueArticlesMap.set(element.article.id, element.article);
      }
    });

    this.listeAffairesByAtelier = Array.from(uniqueProjectsMap.values());
    this.listeAteliers = Array.from(uniqueAteliersMap.values());
    this.listeArticles=Array.from(uniqueArticlesMap.values());
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;

  protected readonly Date = Date;


}
