import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {EmployeService} from "../../../services/employe.service";
import {Ifonction} from "../../../services/Interfaces/ifonction";
import {Iateliers} from "../../../services/Interfaces/iateliers";

@Component({
  selector: 'app-add-personnel',
  templateUrl: './add-personnel.component.html',
  styleUrls: ['./add-personnel.component.css']
})
export class AddPersonnelComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  Fonctions:Ifonction[];
  @Input()
  Ateliers:Iateliers[];

  nomCompletExist:boolean=false;
  matriculeExist:boolean=false;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private employeService: EmployeService,
              private roleService: RoleService

  ) {
  }
  onMaterialGroupChange(event) {}
  onAdd() {

        this.employeService
            .add(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
            .subscribe({
              next: (data: any) => {

                  this.notifyService.showSuccess(data, "Ajout Employé"); // Notification avec réponse texte
                  this.initmyForm(); // Réinitialisation du formulaire
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

      matricule: ['', Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      fonction: ['', Validators.required],
      ateliers: ['', Validators.required],

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


