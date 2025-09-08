import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {LivraisonService} from "../../../services/livraison.service";
import {ListeLivraisonsComponent} from "../../Livraison/liste-livraisons/liste-livraisons.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES, ROLES_ADMIN_LOGISTIQUE} from "../../../Roles";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {ProjetService} from "../../../services/projet.service";
import {ListeProjetsComponent} from "../liste-projets/liste-projets.component";

@Component({
  selector: 'app-delete-projet',
  templateUrl: './delete-projet.component.html',
  styleUrls: ['./delete-projet.component.css']
})
export class DeleteProjetComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public projet:Iprojet;
  constructor(private projetService:ProjetService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {


    this.projetService
      .deleteProjet(this.projet.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Suppression Projet"); // Notification avec réponse texte
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit(); // Rafraîchissement des données

        },
        error: (error) => {
          console.error("Erreur lors de l'ajout :", error);
          this.notifyService.showError("Échec de suppression", "Erreur");
        },
      });

  }

  hasRole(roleToCheck:string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  protected readonly ROLES = ROLES;
}

