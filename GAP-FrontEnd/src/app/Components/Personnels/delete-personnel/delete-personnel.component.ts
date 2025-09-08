import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";
import {ChauffeurService} from "../../../services/chauffeur.service";
import {ListeChauffeursComponent} from "../../Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES_ADMIN_RH} from "../../../Roles";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import {EmployeService} from "../../../services/employe.service";
import {ListePersonnelsComponent} from "../liste-personnels/liste-personnels.component";

@Component({
  selector: 'app-delete-personnel',
  templateUrl: './delete-personnel.component.html',
  styleUrls: ['./delete-personnel.component.css']
})
export class DeletePersonnelComponent {

  @Output() refreshTable = new EventEmitter<void>();

  @ViewChild('closebutton') closebutton;
  @Input()
  public employe:Iemploye;
  constructor(private employeService:EmployeService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}


  onDelete() {
    this.employeService
        .delete(this.employe.id) // On force le texte comme réponse
        .subscribe({
          next: (data: any) => {
              this.notifyService.showSuccess(data, "Suppression Employé"); // Notification avec réponse texte
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

  protected readonly ROLES_ADMIN_RH = ROLES_ADMIN_RH;
}

