import {Ifonction} from "../../../services/Interfaces/ifonction";
import {Component, OnInit, ViewChild} from "@angular/core";
import {FonctionService} from "../../../services/fonction.service";
import {RoleService} from "../../../services/role.service";
import * as XLSX from 'xlsx';
import {ROLES_ADMIN_RH} from "../../../Roles";

@Component({
  selector: 'app-liste-fonctions',
  templateUrl: './liste-fonctions.component.html',
  styleUrls: ['./liste-fonctions.component.css']
})
export class ListeFonctionsComponent implements OnInit {
  @ViewChild(ListeFonctionsComponent) ListeUtilisateurs: ListeFonctionsComponent;
  POSTS: Ifonction[] = [];
  page: number = 1;
  count: number = 0;
  tableSize: number = 10;
  pfiltre: any;
  sortDirection: { [key: string]: boolean } = {};
  fonctions: Ifonction[] = [];

  fonctionSelected: Ifonction = null;

  constructor(
    private fonctionService: FonctionService,
    private roleService: RoleService
  ) {}

  ngOnInit(): void {
    this.postList();
  }



  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onTableSizeChange(): void {
    this.page = 1;
  }

  recupSelected(fonction: Ifonction) {
    this.fonctionSelected = fonction;
  }

  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }


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
    this.fonctionService.getAllFonctions().subscribe(data => {
      this.POSTS = data;

    });
  }

  exportExel() {
    const transformedPosts = this.POSTS.map((fonction: Ifonction) => {
      const { id, codeFonction, designation, typeCalcul } = fonction;
      return { id, codeFonction, designation, typeCalcul };
    });

    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedPosts);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Liste Fonctions');
    XLSX.writeFile(wb, 'Liste_Fonctions.xlsx');
  }

  protected readonly ROLES_ADMIN_RH = ROLES_ADMIN_RH;
}
