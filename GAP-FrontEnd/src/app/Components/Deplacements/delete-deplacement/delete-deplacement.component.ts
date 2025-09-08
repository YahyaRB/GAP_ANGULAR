import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
import {Ideplacement} from "../../../services/Interfaces/ideplacement";
import {DeplacementService} from "../../../services/deplacement.service";


@Component({
  selector: 'app-delete-deplacement',
  templateUrl: './delete-deplacement.component.html',
  styleUrls: ['./delete-deplacement.component.css']
})
export class DeleteDeplacementComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public dpl:Ideplacement;
  constructor(private deplacementService:DeplacementService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {

    this.deplacementService
      .delete(this.dpl.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Suppression Déplacement"); // Notification avec réponse texte
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();

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


