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

  exportExel() {

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
