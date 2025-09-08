import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Iuser} from "../../../../services/Interfaces/iuser";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../../services/Interfaces/iateliers";
import {Irole} from "../../../../services/Interfaces/irole";
import {UtilisateurService} from "../../../../services/utilisateur.service";
import {ListeUtilisateursComponent} from "../../../Utilisateurs/liste-utilisateurs/liste-utilisateurs.component";
import {NotificationService} from "../../../../services/notification.service";
import {RoleService} from "../../../../services/role.service";
import {Ichauffeur} from "../../../../services/Interfaces/ichauffeur";
import {ChauffeurService} from "../../../../services/chauffeur.service";
import {ListeChauffeursComponent} from "../liste-chauffeurs/liste-chauffeurs.component";

@Component({
  selector: 'app-update-chauffeur',
  templateUrl: './update-chauffeur.component.html',
  styleUrls: ['./update-chauffeur.component.css']
})
export class UpdateChauffeurComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public chauffeur:Ichauffeur;
  myFormUpdate:FormGroup;
  @Input()
  Chauffeurs:Ichauffeur[]=[];

  nomCompletExist:boolean=false;
  matriculeExist:boolean=false;
  constructor(private chauffeurService: ChauffeurService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private roleService:RoleService) {}

  onUpdate() {
    this.chauffeurService.existeByMatricule(this.myFormUpdate.value.matricule).subscribe(matricule=>{

      if(matricule && this.myFormUpdate.value.matricule!=this.chauffeur.matricule){
          this.matriculeExist=true;
          this.notifyService.showError("Ce matricule existe déjà !!", "Erreur Matricule");
        }
        else {
          this.matriculeExist=false;
        }
      }
    );
    this.chauffeurService.existeByNomComplet(this.myFormUpdate.value.nom,this.myFormUpdate.value.prenom).subscribe(x=>{
        if(x && (this.myFormUpdate.value.nom!=this.chauffeur.nom || this.myFormUpdate.value.prenom!=this.chauffeur.prenom)){
          this.nomCompletExist=true;
          this.notifyService.showError("Cet Nom existe déjà !!", "Erreur Nom");
        }else{
          this.nomCompletExist=false;
        }
      }
    );

      if (!this.matriculeExist && !this.nomCompletExist) {

          this.chauffeurService
            .updateChauffeur(this.myFormUpdate.value, this.chauffeur.id,{ responseType: 'text' }) // On force le texte comme réponse
            .subscribe({
              next: (data: any) => {

                  this.notifyService.showSuccess(data, "Modification Chauffeur"); // Notification avec réponse texte
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



  }
  ngOnChanges(changes: SimpleChanges): void {
    if(this.chauffeur){
      this.affectUsertoForm(this.chauffeur.id);
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      matricule: ['', Validators.required],
      nom: ['',Validators.required],
      prenom: ['',Validators.required],

    });

  }
  private affectUsertoForm(id:number){
      this.myFormUpdate.setValue({
        matricule:this.chauffeur.matricule,
        nom: this.chauffeur.nom,
        prenom: this.chauffeur.prenom,

      });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

}


