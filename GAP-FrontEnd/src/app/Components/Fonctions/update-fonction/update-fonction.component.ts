import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Ifonction} from "../../../services/Interfaces/ifonction";
import {FonctionService} from "../../../services/fonction.service";
import {ListeFonctionsComponent} from "../liste-fonctions/liste-fonctions.component";

@Component({
  selector: 'app-update-fonction',
  templateUrl: './update-fonction.component.html',
  styleUrls: ['./update-fonction.component.css']
})
export class UpdateFonctionComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public fonction:Ifonction;
  myFormUpdate:FormGroup;
  @Input()
  Fonctions:Ifonction[]=[];


  constructor(private fonctionService: FonctionService,
              private formBuilder: FormBuilder,
              private fonctionC:ListeFonctionsComponent,
              private notifyService : NotificationService,
              private roleService:RoleService) {}

  onUpdate() {
   /* this.fonctionService.existeByMatricule(this.myFormUpdate.value.matricule).subscribe(matricule=>{

        if(matricule && this.myFormUpdate.value.matricule!=this.chauffeur.matricule){
          this.matriculeExist=true;
          this.notifyService.showError("Ce matricule existe déjà !!", "Erreur Matricule");
        }
        else {
          this.matriculeExist=false;
        }
      }
    );*/



        this.fonctionService
          .update(this.myFormUpdate.value, this.fonction.id,{ responseType: 'text' }) // On force le texte comme réponse
          .subscribe({
            next: (data: any) => {

                this.notifyService.showSuccess(data, "Modification Fonction"); // Notification avec réponse texte
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
    if(this.fonction){
      this.affectUsertoForm(this.fonction.id);
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      codeFonction: ['', Validators.required],
      designation: ['',Validators.required],
      typeCalcul: ['',Validators.required],

    });

  }
  private affectUsertoForm(id:number){
    this.myFormUpdate.setValue({
      codeFonction:this.fonction.codeFonction,
      designation: this.fonction.designation,
      typeCalcul: this.fonction.typeCalcul,

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

}



