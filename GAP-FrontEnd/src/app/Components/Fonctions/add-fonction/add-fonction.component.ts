import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Ifonction} from "../../../services/Interfaces/ifonction";
import {ListeFonctionsComponent} from "../liste-fonctions/liste-fonctions.component";
import {FonctionService} from "../../../services/fonction.service";

@Component({
  selector: 'app-add-fonction',
  templateUrl: './add-fonction.component.html',
  styleUrls: ['./add-fonction.component.css']
})
export class AddFonctionComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  Fonctions:Ifonction[]=[];
  fonction: Ifonction;
  constructor(private notifyService: NotificationService,
              private fonctionC:ListeFonctionsComponent,
              private formBuilder: FormBuilder,
              private fonctionservice: FonctionService,
              private roleService: RoleService

  ) {
  }
  onMaterialGroupChange(event) {}
  onAdd() {

        this.fonctionservice
          .addFonction(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
          .subscribe({
            next: (data: any) => {

                this.notifyService.showSuccess(data, "Ajout Fonction"); // Notification avec réponse texte
                this.closebutton.nativeElement.click(); // Fermeture de la modale
                this.refreshTable.emit(); // Rafraîchissement des données

            },
            error: (error) => {
              console.error("Erreur lors de l'ajout :", error);
              this.notifyService.showError("Échec de l'ajout", "Erreur");
            },
          });


  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      codeFonction: ['', Validators.required],
      designation: ['', Validators.required],
      typeCalcul: [[], Validators.required],
    });

  }
  ngOnInit(): void {
    this.initmyForm();
  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }


}

