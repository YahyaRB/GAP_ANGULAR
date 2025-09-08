import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {ROLES_ADMIN} from "../../../Roles";
import {UtilisateurService} from "../../../services/utilisateur.service";
import {ListeUtilisateursComponent} from "../liste-utilisateurs/liste-utilisateurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Iuser} from "../../../services/Interfaces/iuser";

@Component({
  selector: 'app-delete-utilisateur',
  templateUrl: './delete-utilisateur.component.html',
  styleUrls: ['./delete-utilisateur.component.css']
})
export class DeleteUtilisateurComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public user:Iuser;
  constructor(private userService: UtilisateurService,
              private userC:ListeUtilisateursComponent,
              private notifyService : NotificationService,
              private roleService: RoleService) {}
  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  deleteUser() {
    this.userService.deleteUser(this.user.id).subscribe(data=>
      this.notifyService.showSuccess("Utilisateur supprimé avec succés !!", "Suppression Utilisateur")
    );

      this.refreshTable.emit();
      this.closebutton.nativeElement.click();

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }
}
