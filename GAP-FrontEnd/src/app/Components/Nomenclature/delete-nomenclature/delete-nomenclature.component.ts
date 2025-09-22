import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES_ADMIN_AGENTSAISIE} from 'src/app/Roles';
import {INomenclature} from "../../../services/Interfaces/inomenclature";
import {NomenclatureService} from "../../../services/nomenclature.service";

@Component({
  selector: 'app-delete-nomenclature',
  templateUrl: './delete-nomenclature.component.html',
  styleUrls: ['./delete-nomenclature.component.css']
})
export class DeleteNomenclatureComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input() nomenclature: INomenclature;

  constructor(
    private nomenclatureService: NomenclatureService,
    private notifyService: NotificationService,
    private roleService: RoleService
  ) {}

  onDelete() {
    if (this.nomenclature) {
      this.nomenclatureService.deleteNomenclature(this.nomenclature.id)
        .subscribe({
          next: (data: any) => {
            this.notifyService.showSuccess("Nomenclature supprimée avec succès", "Suppression Nomenclature");
            this.closebutton.nativeElement.click();
            this.refreshTable.emit();
          },
          error: (error) => {
            console.error("Erreur lors de la suppression :", error);
            this.notifyService.showError("Échec de suppression", "Erreur");
          },
        });
    }
  }

  hasRole(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}
