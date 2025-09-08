import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Ichauffeur} from "../../../services/Interfaces/ichauffeur";
import {NotificationService} from "../../../services/notification.service";
import {ListeChauffeursComponent} from "../../Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {ChauffeurService} from "../../../services/chauffeur.service";
import {RoleService} from "../../../services/role.service";
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {ListeLivraisonsComponent} from "../liste-livraisons/liste-livraisons.component";
import {LivraisonService} from "../../../services/livraison.service";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {IdetailLivraison} from "../../../services/Interfaces/idetail-livraison";
import {ProjetService} from "../../../services/projet.service";

@Component({
  selector: 'app-add-livraison',
  templateUrl: './add-livraison.component.html',
  styleUrls: ['./add-livraison.component.css'],
})
export class AddLivraisonComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  livraisons:Ilivraison[]=[];
  @Input()
  ateliers:Iateliers[]=[];
  projets:Iprojet[]=[];
  livraison: Ilivraison;
  today:string;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private livraisonService: LivraisonService,
              private projetService:ProjetService,
              private roleService: RoleService

  ) {
  }
  onMaterialGroupChange(event) {}

  onAdd() {
    this.livraisonService
      .addLivraison(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Ajout Livraison"); // Notification avec réponse texte
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
      dateLivraison: ['', Validators.required],
      atelier: [[], Validators.required],
      projet: [[], Validators.required],
      chauffeur: [null],
      detailLivraison: []
    });

  }
  ngOnInit(): void {
    const currentDate = new Date();
    this.today = currentDate.toISOString().split('T')[0]; // Formate la date en YYYY-MM-DD
    this.initmyForm();
  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  onValueChangeAtelier(newValue) {
    this.projetService.getAffairesByAtelier(this.myFormAdd.value.atelier.id).subscribe(data=>
        this.projets = data

    );
  }
}

