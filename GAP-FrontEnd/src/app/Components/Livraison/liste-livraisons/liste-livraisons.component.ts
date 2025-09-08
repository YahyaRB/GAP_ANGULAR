import {AfterViewInit, Component, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Ilivraison} from "../../../services/Interfaces/ilivraison";  // Importation de l'interface Ilivraison
import {TokenStorageService} from "../../../Auth/services/token-storage.service";  // Service de stockage du token
import {FormBuilder, FormGroup} from "@angular/forms";  // Importation du service FormBuilder pour gérer les formulaires
import {UtilisateurService} from "../../../services/utilisateur.service";  // Service utilisateur
import {LivraisonService} from "../../../services/livraison.service";  // Service pour les livraisons
import {RoleService} from "../../../services/role.service";  // Service de gestion des rôles
import {NotificationService} from "../../../services/notification.service";  // Service de notifications
import {ROLES, ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";  // Constante des rôles d'administration
import {Iateliers} from "../../../services/Interfaces/iateliers";  // Interface pour les ateliers
import {Iprojet} from "../../../services/Interfaces/iprojet";  // Interface pour les projets
import {ChauffeurService} from "../../../services/chauffeur.service";  // Service pour les chauffeurs
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";  // Interface pour les chauffeurs
import {ProjetService} from "../../../services/projet.service";
import {Iuser} from "../../../services/Interfaces/iuser";
import {DetailLivraisonService} from "../../../services/detail-livraison.service";
import {AtelierService} from "../../../services/atelier.service";  // Service pour les projets
declare var $: any;  // Déclaration de jQuery pour l'utilisation des plugins JS

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
  listeAffairesByAtelier:Iprojet[]=[];
  listeChauffeurs: Ichauffeur[] = [];  // Liste des chaffeurs
  livraisonSelected:Ilivraison; // Livraison selectionné
  ateliersUpdate:Iateliers[]; // Pour stocker l'atelier du livraison selectionné
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  idprojet: number = 0;  // Définir l'ID du projet
  idchauffeur: number = 0;  // Définir l'ID du chauffeur
  idatelier: number = 0;  // Définir l'ID de l'atelier
  dateDebut: string = '';  // Date de début pour la recherche
  dateFin: string = '';    // Date de fin pour la recherche
  myFormSearch: FormGroup;


  constructor(private tokenstorage: TokenStorageService,
              private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private detailService:DetailLivraisonService,
              private chauffeurService:ChauffeurService,
              private projetService: ProjetService,
              private livraisonservice: LivraisonService,
              private roleService: RoleService,
  ) {
    // Récupération d'id d'utilisateur connecté
    this.idUser=this.tokenstorage.getUser().id
    // Récupération des ateliers associés à l'utilisateur connecté depuis le service TokenStorage
    this.listeAteliers = this.tokenstorage.getUser().atelier;
    // Récupération des projets via le service ProjetService et stockage dans la variable listeaffaires
    this.projetService.getAll().subscribe(data => this.listeaffaires = data);
    this.chauffeurService.getAll().subscribe(data => this.listeChauffeurs = data);
    this.projetService.getAffairesByAtelier(this.tokenstorage.getUser().id).subscribe(x=>this.listeAffairesByAtelier = x);
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

    this.livraisonservice.getAll( this.idUser).subscribe(
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
      this.myFormSearch.value.idprojet?? 0,
      this.myFormSearch.value.idchauffeur?? 0,
      this.myFormSearch.value.idatelier?? 0,
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
    this.livraisonSelected=livraison;
    this.listeaffaires=[];
    this.listeaffaires.push(livraison.projet) ;
    this.ateliersUpdate=[];
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

  exportExel() {

  }

  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
  protected readonly ROLES = ROLES;


}
