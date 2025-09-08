import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';

import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES_ADMIN_AGENTSAISIE} from 'src/app/Roles';
import {OfService} from "../../../services/of.service";
import {ListeOFComponent} from "../liste-of/liste-of.component";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";

@Component({
  selector: 'app-delete-of',
  templateUrl: './delete-of.component.html',
  styleUrls: ['./delete-of.component.css']
})
export class DeleteOFComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public ordreFabrication:IordreFabrication;
  constructor(private ofService:OfService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {

    this.ofService
        .delete(this.ordreFabrication.id, { responseType: 'text' }) // On force le texte comme réponse
        .subscribe({
          next: (data: any) => {

              this.notifyService.showSuccess(data, "Suppression Ordre de fabrication"); // Notification avec réponse texte
              this.closebutton.nativeElement.click(); // Fermeture de la modale

          },
          error: (error) => {
            console.error("Erreur lors de la suppression :", error);
            this.notifyService.showError("Échec de suppression", "Erreur");
          },
        });

    }


  hasRole(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }


  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}


