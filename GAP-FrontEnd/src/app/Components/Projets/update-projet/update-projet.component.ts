import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {LivraisonService} from "../../../services/livraison.service";
import {ListeLivraisonsComponent} from "../../Livraison/liste-livraisons/liste-livraisons.component";
import {NotificationService} from "../../../services/notification.service";
import {DatePipe} from "@angular/common";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {ListeProjetsComponent} from "../liste-projets/liste-projets.component";

@Component({
  selector: 'app-update-projet',
  templateUrl: './update-projet.component.html',
  styleUrls: ['./update-projet.component.css']
})
export class UpdateProjetComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public projet:Iprojet;
  myFormUpdate:FormGroup;
  @Input()
  projets:Iprojet[]=[];
  constructor(private projetService: ProjetService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private roleService:RoleService) {}

  onUpdate() {
    this.projetService
      .updateProjet(this.myFormUpdate.value, this.projet.id,{ responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Modification Projet"); // Notification avec réponse texte
            this.initmyUpdateForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit(); // Rafraîchissement des données

        },
        error: (error) => {

          console.error("Erreur lors de la modification :", error);
          this.notifyService.showError("Échec de la modification", "Erreur");
        },
      });
  }
  ngOnChanges(changes: SimpleChanges): void {
    if(this.projet){
      this.affectUsertoForm(this.projet.id);
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      designation: ['', Validators.required],
      status: ['', Validators.required],
      code: ['', Validators.required],
    });

  }
  private affectUsertoForm(id:number){
    this.myFormUpdate.setValue({
      designation:this.projet.designation,
      status: this.projet.status,
      code: this.projet.code,

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }
}





