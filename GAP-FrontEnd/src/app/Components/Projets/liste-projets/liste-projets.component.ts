import { Component, OnChanges, OnInit, ViewChild } from '@angular/core';
import { Iprojet } from "../../../services/Interfaces/iprojet";
import { TokenStorageService } from "../../../Auth/services/token-storage.service";
import { ProjetService } from "../../../services/projet.service";
import { RoleService } from "../../../services/role.service";
import { Iaffaire } from "../../../services/Interfaces/iaffaire";
import { ROLES, ROLES_ADMIN } from "../../../Roles";

@Component({
  selector: 'app-liste-projets',
  templateUrl: './liste-projets.component.html',
  styleUrls: ['./liste-projets.component.css']
})
export class ListeProjetsComponent implements OnInit, OnChanges {
  @ViewChild(ListeProjetsComponent) ListeUtilisateurs: ListeProjetsComponent;
  POSTS: Iprojet[];
  page: number = 1;
  count: number = 0;
  tableSize: number = 10;
  pfiltre: any;
  affaireSelected: any;
  affairesActives: Iprojet[] = [];
  sortDirection: { [key: string]: boolean } = {};
  constructor(private tokenstorage: TokenStorageService,
    private affaireService: ProjetService,
    private roleService: RoleService) { }
  postList(): void {
    this.affaireService.getAll().subscribe(data => {
      this.POSTS = data;
      this.count = data.length; // Optional, but good for info
    });
  }

  getAffairesActives() {
    this.affaireService.affairesByStatut("Actif").subscribe(data =>
      this.affairesActives = data
    );
  }

  ngOnChanges(): void {
    this.postList();

  }

  ngOnInit(): void {
    this.postList();
  }

  recupAffaire(affaire: Iprojet) {
    this.affaireSelected = affaire;
  }
  onTableDataChange(event: any) {
    this.page = event;
  }




  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string): boolean {
    return this.roleService.hasRole(roleToCheck);
  }


  onTableSizeChange(): void {
    this.page = 1;  // Réinitialiser à la première page lors du changement de taille de la table
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;


  // Méthode de tri des données par colonne
  sortColumn(column: string): void {
    if (!(column in this.sortDirection)) {
      this.sortDirection[column] = true;
    }

    const isAscending = this.sortDirection[column];

    this.POSTS.sort((a, b) => {
      let aValue: string | null = null;
      let bValue: string | null = null;

      if (column === 'affaireConcat') {
        aValue = `${a.code} - ${a.designation}`;
        bValue = `${b.code} - ${b.designation}`;
      } else {
        aValue = this.resolvePath(a, column);
        bValue = this.resolvePath(b, column);
      }

      if (aValue < bValue) return isAscending ? -1 : 1;
      if (aValue > bValue) return isAscending ? 1 : -1;
      return 0;
    });

    this.sortDirection[column] = !isAscending;
  }

  resolvePath(obj: any, path: string) {
    return path.split('.').reduce((acc, key) => acc && acc[key], obj);
  }

  protected readonly ROLES = ROLES;
}


