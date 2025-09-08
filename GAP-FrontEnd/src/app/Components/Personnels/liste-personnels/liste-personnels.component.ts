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
  exportExel() {
    // Mettez à jour les données pour remplacer les IDs par les noms des fonctions et ateliers
    const transformedPosts = this.POSTS.map((employe: Iemploye) => {
      return {
        ...employe,
        ateliers: employe.ateliers ? employe.ateliers.designation : '',  // Remplacer l'ID de l'atelier par le nom
        fonction: employe.fonction ? employe.fonction.designation : '',  // Remplacer l'ID de la fonction par le nom
      };
    });

    // Créer la feuille Excel avec les données modifiées
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedPosts);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Liste Employés');

    // Générer et télécharger le fichier Excel
    XLSX.writeFile(wb, 'Liste_Employes.xlsx');
  }


  // Vérifie si l'utilisateur possède l'un des rôles spécifiés
  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }

  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
  protected readonly ROLES_ADMIN_RH = ROLES_ADMIN_RH;
}
