import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Ifonction} from "../../../services/Interfaces/ifonction";
import {FonctionService} from "../../../services/fonction.service";
import {ListeFonctionsComponent} from "../liste-fonctions/liste-fonctions.component";
import {ROLES_ADMIN_LOGISTIQUE} from "../../../Roles";

@Component({
  selector: 'app-delete-fonction',
  templateUrl: './delete-fonction.component.html',
  styleUrls: ['./delete-fonction.component.css']
})
export class DeleteFonctionComponent {
  @Output() refreshTable = new EventEmitter<void>();

  @ViewChild('closebutton') closebutton;
  @Input()
  public fonction:Ifonction;
  constructor(private fonctionService:FonctionService,
              private fonctionC:ListeFonctionsComponent,
              private notifyService:NotificationService,
              private roleService: RoleService) {}


  onDelete() {
    this.fonctionService
      .delete(this.fonction.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Suppression Fonction"); // Notification avec réponse texte
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit(); // Rafraîchissement des données

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


  protected readonly ROLES_ADMIN_LOGISTIQUE = ROLES_ADMIN_LOGISTIQUE;
}

