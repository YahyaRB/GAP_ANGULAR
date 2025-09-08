import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ListeLivraisonsComponent} from "../../Livraison/liste-livraisons/liste-livraisons.component";
import {LivraisonService} from "../../../services/livraison.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {ListeProjetsComponent} from "../liste-projets/liste-projets.component";

@Component({
  selector: 'app-add-projet',
  templateUrl: './add-projet.component.html',
  styleUrls: ['./add-projet.component.css']
})
export class AddProjetComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  projets:Iprojet[]=[];
  @Input()
  projet: Iprojet;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private projetService: ProjetService,
              private roleService: RoleService) {}
  onMaterialGroupChange(event) {}

  onAdd() {
    this.projetService
      .addProjet(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Ajout Projet"); // Notification avec réponse texte
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
      designation: ['', Validators.required],
      status: ['Actif', Validators.required],
      code: ['', Validators.required],
    });

  }
  ngOnInit(): void {
    this.initmyForm();
  }
  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }
}


