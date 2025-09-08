import {Component, OnInit, ViewChild} from '@angular/core';
import { Ichauffeur } from "../../../../services/Interfaces/ichauffeur";
import { MatTableDataSource } from "@angular/material/table";
import { ChauffeurService } from "../../../../services/chauffeur.service";
import { RoleService } from "../../../../services/role.service";
import * as XLSX from 'xlsx';
import { ROLES, ROLES_ADMIN_LOGISTIQUE } from "../../../../Roles";
import {Iemploye} from "../../../../services/Interfaces/iemploye";

@Component({
  selector: 'app-liste-chauffeurs',
  templateUrl: './liste-chauffeurs.component.html',
  styleUrls: ['./liste-chauffeurs.component.css']
})
export class ListeChauffeursComponent implements OnInit {
  @ViewChild(ListeChauffeursComponent) ListeUtilisateurs: ListeChauffeursComponent;
  POSTS: Ichauffeur[] = [];
  page: number = 1;
  count: number = 0;
  tableSize: number = 10;
  pfiltre: any;
  sortDirection: { [key: string]: boolean } = {};
  chauffeurs: Ichauffeur[] = [];
  dataSource: MatTableDataSource<Ichauffeur>;
  chauffeurSelected: Ichauffeur = null;

  constructor(
    private chauffeurService: ChauffeurService,
    private roleService: RoleService
  ) {}

  ngOnInit(): void {
    this.postList();
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onTableSizeChange(): void {
    this.page = 1;
  }

  recupSelected(chauffeur: Ichauffeur) {
    this.chauffeurSelected = chauffeur;
  }

  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }

  protected readonly ROLES = ROLES;
  protected readonly ROLES_ADMIN_LOGISTIQUE = ROLES_ADMIN_LOGISTIQUE;

  sortColumn(column: string) {
    if (!(column in this.sortDirection)) {
      this.sortDirection[column] = true;
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

  onTableDataChange(event: any) {
    this.page = event;
    this.postList();
  }

  postList(): void {
    this.chauffeurService.getAll().subscribe(data => {
      this.POSTS = data;
      this.dataSource = new MatTableDataSource(this.POSTS);
    });
  }

  exportExel() {
    // Mettez à jour les données pour remplacer les IDs par les noms des fonctions et ateliers
    const transformedPosts = this.POSTS.map((chauffeur: Ichauffeur) => {
      return {
        ...chauffeur,
      };
    });

    // Créer la feuille Excel avec les données modifiées
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedPosts);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Liste Chauffeurs');

    // Générer et télécharger le fichier Excel
    XLSX.writeFile(wb, 'Liste_Chauffeurs.xlsx');
  }
}
