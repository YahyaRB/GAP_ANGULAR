import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";
import {ChauffeurService} from "../../../services/chauffeur.service";
import {ListeChauffeursComponent} from "../../Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {LivraisonService} from "../../../services/livraison.service";
import {ListeLivraisonsComponent} from "../liste-livraisons/liste-livraisons.component";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {ROLES_ADMIN_LOGISTIQUE} from "../../../Roles";

@Component({
  selector: 'app-delete-livraison',
  templateUrl: './delete-livraison.component.html',
  styleUrls: ['./delete-livraison.component.css']
})
export class DeleteLivraisonComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public livraison:Ilivraison;
  constructor(private livraisonService:LivraisonService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {
        this.livraisonService.deleteLivraison(this.livraison.id).subscribe(data=> {


          this.notifyService.showSuccess(data, "Suppression Livraison");
          this.refreshTable.emit();
          this.closebutton.nativeElement.click();
        });

  }

  hasRole(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }


  protected readonly ROLES_ADMIN_LOGISTIQUE = ROLES_ADMIN_LOGISTIQUE;
}

