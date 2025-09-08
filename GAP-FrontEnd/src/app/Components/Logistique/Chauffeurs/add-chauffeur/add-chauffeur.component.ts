import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../../services/Interfaces/iateliers";
import {Irole} from "../../../../services/Interfaces/irole";
import {Iuser} from "../../../../services/Interfaces/iuser";
import {NotificationService} from "../../../../services/notification.service";
import {ListeUtilisateursComponent} from "../../../Utilisateurs/liste-utilisateurs/liste-utilisateurs.component";
import {UtilisateurService} from "../../../../services/utilisateur.service";
import {RoleService} from "../../../../services/role.service";
import {Ichauffeur} from "../../../../services/Interfaces/ichauffeur";
import {ListeChauffeursComponent} from "../liste-chauffeurs/liste-chauffeurs.component";
import {ChauffeurService} from "../../../../services/chauffeur.service";

@Component({
  selector: 'app-add-chauffeur',
  templateUrl: './add-chauffeur.component.html',
  styleUrls: ['./add-chauffeur.component.css']
})
export class AddChauffeurComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  Chauffeurs:Ichauffeur[]=[];
  chauffeur: Ichauffeur;
  nomCompletExist:boolean=false;
  matriculeExist:boolean=false;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private chauffeurservice: ChauffeurService,
              private roleService: RoleService

  ) {
  }
  onMaterialGroupChange(event) {}
  onAdd() {
    this.chauffeurservice.existeByMatricule(this.myFormAdd.value.matricule).subscribe(matricule => {
        this.matriculeExist = matricule;
        if (this.matriculeExist) {
          this.notifyService.showError("Ce matricule existe déjà !!", "Erreur Matricule");
        }
      }
    );
    this.chauffeurservice.existeByNomComplet(this.myFormAdd.value.nom,this.myFormAdd.value.prenom).subscribe(data => {
        this.nomCompletExist = data;
        if (this.nomCompletExist) {
          this.notifyService.showError("Cet Nom existe déjà !!", "Erreur Nom");
        }
      }
    );

    if (!this.nomCompletExist && !this.matriculeExist) {

      this.chauffeurservice
        .addChauffeur(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
        .subscribe({
          next: (data: any) => {

              this.notifyService.showSuccess(data, "Ajout Chauffeur"); // Notification avec réponse texte
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

  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({

      matricule: ['', Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
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

