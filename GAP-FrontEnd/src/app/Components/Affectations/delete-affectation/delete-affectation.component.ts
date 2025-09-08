import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {ArticleService} from "../../../services/article.service";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Iaffectation} from "../../../services/Interfaces/iaffectation";
import {AffectationService} from "../../../services/affectation.service";
import {ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
@Component({
  selector: 'app-delete-affectation',
  templateUrl: './delete-affectation.component.html',
  styleUrls: ['./delete-affectation.component.css']
})
export class DeleteAffectationComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public affectation:Iaffectation;
  constructor(private affectationService:AffectationService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {

    this.affectationService
      .delete(this.affectation.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

          this.notifyService.showSuccess(data, "Suppression Affectation"); // Notification avec réponse texte
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


