import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ChauffeurService} from "../../../services/chauffeur.service";
import {ListeChauffeursComponent} from "../../Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import {Ifonction} from "../../../services/Interfaces/ifonction";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {EmployeService} from "../../../services/employe.service";
import {ListePersonnelsComponent} from "../liste-personnels/liste-personnels.component";

@Component({
  selector: 'app-update-personnel',
  templateUrl: './update-personnel.component.html',
  styleUrls: ['./update-personnel.component.css']
})
export class UpdatePersonnelComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public employe:Iemploye;
  myFormUpdate:FormGroup;
  @Input()
  listeFonctions:Ifonction[]=[];
  @Input()
  listeAteliers:Iateliers[]=[];
  nomCompletExist:boolean=false;
  matriculeExist:boolean=false;
  constructor(private employeService: EmployeService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private roleService:RoleService) {}

  onUpdate() {
    this.employeService.existeByMatricule(this.myFormUpdate.value.matricule).subscribe(matricule=>{

          if(matricule && this.myFormUpdate.value.matricule!=this.employe.matricule){
            this.matriculeExist=true;
            this.notifyService.showError("Ce matricule existe déjà !!", "Erreur Matricule");
          }
          else {
            this.matriculeExist=false;
          }
        }
    );

      if (!this.matriculeExist) {
       this.myFormUpdate.value.ateliers=this.listeAteliers.find(atelier=>atelier.id===this.myFormUpdate.value.ateliers);
       this.myFormUpdate.value.fonction=this.listeFonctions.find(fonction=>fonction.id===this.myFormUpdate.value.fonction);
        this.employeService
            .update(this.myFormUpdate.value, this.employe.id,{ responseType: 'text' }) // On force le texte comme réponse
            .subscribe({
              next: (data: any) => {

                  this.notifyService.showSuccess(data, "Modification Employé"); // Notification avec réponse texte
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
    if(this.employe){
      this.affectUsertoForm(this.employe.id);
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
      fonction: [[], Validators.required],
      ateliers: [[], Validators.required],

    });

  }
  private affectUsertoForm(id:number){
    this.myFormUpdate.setValue({
      matricule:this.employe.matricule,
      nom: this.employe.nom,
      prenom: this.employe.prenom,
      fonction: this.employe.fonction.id,
      ateliers: this.employe.ateliers.id,

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

}


