import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Iuser} from "../../../../services/Interfaces/iuser";
import {UtilisateurService} from "../../../../services/utilisateur.service";
import {ListeUtilisateursComponent} from "../../../Utilisateurs/liste-utilisateurs/liste-utilisateurs.component";
import {NotificationService} from "../../../../services/notification.service";
import {RoleService} from "../../../../services/role.service";
import {Ichauffeur} from "../../../../services/Interfaces/ichauffeur";
import {ChauffeurService} from "../../../../services/chauffeur.service";
import {ROLES_ADMIN_LOGISTIQUE} from "../../../../Roles";
import {ListeChauffeursComponent} from "../liste-chauffeurs/liste-chauffeurs.component";

@Component({
  selector: 'app-delete-chauffeur',
  templateUrl: './delete-chauffeur.component.html',
  styleUrls: ['./delete-chauffeur.component.css']
})
export class DeleteChauffeurComponent {

  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public chauffeur:Ichauffeur;
  constructor(private chauffeurService:ChauffeurService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}


  onDelete() {
    this.chauffeurService
      .deleteChauffeur(this.chauffeur.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Suppression Chauffeur"); // Notification avec réponse texte
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

