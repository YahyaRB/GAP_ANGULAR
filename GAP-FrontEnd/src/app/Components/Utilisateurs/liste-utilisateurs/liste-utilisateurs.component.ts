import {AfterViewInit, Component, OnInit, ViewChild} from "@angular/core";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {UtilisateurService} from "../../../services/utilisateur.service";
import {MatTableDataSource} from "@angular/material/table";
import {Iuser} from "../../../services/Interfaces/iuser";
import {Irole} from "../../../services/Interfaces/irole";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {NotificationService} from "../../../services/notification.service";
import {FormBuilder} from "@angular/forms";
import {RoleService} from "../../../services/role.service";
import {AtelierService} from "../../../services/atelier.service";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {ROLES_ADMIN} from "../../../Roles";

@Component({
  selector: 'app-liste-utilisateurs',
  templateUrl: './liste-utilisateurs.component.html',
  styleUrls: ['./liste-utilisateurs.component.css']
})
export class ListeUtilisateursComponent implements AfterViewInit,OnInit {
    @ViewChild(ListeUtilisateursComponent) ListeUtilisateurs: ListeUtilisateursComponent;
  roles: Irole[] = [];
  users: Iuser[] = [];
  ateliers: Iateliers[] = [];
  dataSource: MatTableDataSource<Iuser>;
  userSelected:Iuser=null;

  constructor(
    private userService: UtilisateurService,
    private roleService: RoleService,
    private atelierService:AtelierService,
    private tokenStorageService:TokenStorageService

  ) {
    // Récupérer les utilisateurs et initialiser la dataSource après réception des données
    this.userService.getAllUsers().subscribe(data => {
      this.users = data;
      // Initialiser MatTableDataSource après avoir récupéré les utilisateurs
      this.dataSource = new MatTableDataSource(this.users);
      // Assurez-vous que paginator et sort sont bien liés après l'initialisation des données
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }


  displayedColumns: string[] = ['id', 'username', 'nom', 'prenom', 'email', 'matricule', 'session', 'roles', 'ateliers', 'action'];


  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  getAllRoles() {
    this.roleService.getAllRoles().subscribe(data =>{
        this.roles = data

    }

    );
  }
 getAllAteliers(){
    this.atelierService.getAll(this.tokenStorageService.getUser().id).subscribe(data=>{
        this.ateliers=data;
    }

    );

  }
  ngAfterViewInit(): void {


  }
  ngOnInit(): void {
    this.getAllRoles();
    this.getAllAteliers();
    this.userService.getAllUsers().subscribe(data => {
      this.users = data;
      this.dataSource = new MatTableDataSource(this.users);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      this.dataSource.filterPredicate = (data: Iuser, filter: string) => {
        const ateliersString = data.atelier.map(atelier => atelier.designation).join(' ').toLowerCase();
        const userInfoString = `${data.username} ${data.nom} ${data.prenom} ${data.email}`.toLowerCase();
        const roleString = data.roles.map(role => role.name).join(' ').toLowerCase();
        return ateliersString.includes(filter) || userInfoString.includes(filter) || roleString.includes(filter);
      };
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;

  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  recupUser(user: Iuser) {
    this.userSelected=user;
  }
}
