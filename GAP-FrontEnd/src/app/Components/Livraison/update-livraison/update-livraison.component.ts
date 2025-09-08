import {Component, EventEmitter, Input, Output, Pipe, SimpleChanges, ViewChild} from '@angular/core';
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ChauffeurService} from "../../../services/chauffeur.service";
import {ListeChauffeursComponent} from "../../Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {LivraisonService} from "../../../services/livraison.service";
import {ListeLivraisonsComponent} from "../liste-livraisons/liste-livraisons.component";
import {ProjetService} from "../../../services/projet.service";
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-update-livraison',
  templateUrl: './update-livraison.component.html',
  styleUrls: ['./update-livraison.component.css'],
  providers: [DatePipe]
})
export class UpdateLivraisonComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public livraison:Ilivraison;
  myFormUpdate:FormGroup;
  @Input()
  livraisons:Ilivraison[]=[];
  @Input()
  ateliers:Iateliers[]=[];
  @Input()
  projets:Iprojet[]=[];
  today:string;
  constructor(private livraisonService: LivraisonService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private datePipe: DatePipe,
              private projetService:ProjetService,
              private roleService:RoleService) {}

  onUpdate() {
    this.livraisonService
      .updateLivraison(this.myFormUpdate.value, this.livraison.id,{ responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Modification Livraison"); // Notification avec réponse texte
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
    if(this.livraison){
      this.affectUsertoForm(this.livraison.id);
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      dateLivraison: ['', Validators.required],
      atelier: ['', Validators.required],
      projet: ['', Validators.required],
    });

  }
  private affectUsertoForm(id:number){
    this.myFormUpdate.setValue({
      dateLivraison:this.datePipe.transform(this.livraison.dateLivraison, 'yyyy-MM-dd'),
      atelier: this.livraison.atelier,
      projet: this.livraison.projet,

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onValueChangeAtelier(newValue) {
    this.projetService.getAffairesByAtelier(this.myFormUpdate.value.atelier.id).subscribe(data=>
      this.projets = data

    );
  }
}




