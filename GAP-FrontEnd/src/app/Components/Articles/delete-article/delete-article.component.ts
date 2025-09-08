import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ArticleService} from "../../../services/article.service";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";

@Component({
  selector: 'app-delete-article',
  templateUrl: './delete-article.component.html',
  styleUrls: ['./delete-article.component.css']
})
export class DeleteArticleComponent {

  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public article:Iarticle;
  constructor(private articleService:ArticleService,
              private notifyService:NotificationService,
              private roleService: RoleService) {}

  onDelete() {

    this.articleService
      .delete(this.article.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Suppression Article"); // Notification avec réponse texte
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


