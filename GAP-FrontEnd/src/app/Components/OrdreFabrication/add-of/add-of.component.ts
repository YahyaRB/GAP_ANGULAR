import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {ListeOFComponent} from "../liste-of/liste-of.component";
import {OfService} from "../../../services/of.service";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {ArticleService} from "../../../services/article.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-add-of',
  templateUrl: './add-of.component.html',
  styleUrls: ['./add-of.component.css']
})
export class AddOFComponent {

  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  ordresFabrications:IordreFabrication[]=[];
  @Input()
  ateliers:Iateliers[]=[];
  listeArticles:Iarticle[]=[];
  projets:Iprojet[]=[];
  today:string;
  fileSelected:any;
  minDate: string = '';
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private ofService: OfService,
              private articleService:ArticleService,
              private projetService:ProjetService,
              private roleService: RoleService,
              private tokenStorage:TokenStorageService,
              private snackBar: MatSnackBar

  ) {
  }
  onMaterialGroupChange(event) {}

  onAdd() {
    const qteArcticleAutorise=this.myFormAdd.value.article.quantiteTot-this.myFormAdd.value.article.quantiteEnProd
    if(this.myFormAdd.value.quantite<=qteArcticleAutorise){
      this.ofService
        .add(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
        .subscribe({
          next: (data: any) => {

              if(this.fileSelected){
                this.saveFile(data);
              }
              this.notifyService.showSuccess("Ordre de fabrication ajouté avec succées", "Ajout Ordre de fabrication"); // Notification avec réponse texte
              this.initmyForm(); // Réinitialisation du formulaire
              this.closebutton.nativeElement.click(); // Fermeture de la modale
              this.refreshTable.emit(); // Rafraîchissement des données
              this.fileSelected=[];

          },
          error: (error) => {
            console.error("Erreur lors de l'ajout :", error);
            this.notifyService.showError("Échec de l'ajout", "Erreur");
          },
        });
    }else{
      this.notifyService.showError("Vous avez dépassé la quantité d'article", "Erreur")
    }

  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      atelier: [null, Validators.required],
      projet: [null, Validators.required],
      date: ['', Validators.required],
      article: [null, Validators.required],
      quantite: ['', Validators.required],
      dateFin:['', Validators.required],
      description:['', Validators.required],
      pieceJointe:['', Validators.required],
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

    this.projetService.findAffairesByAtelierAndQteArticle_Sup_QteOF(this.myFormAdd.value.atelier.id).subscribe(data=>
      this.projets = data

    );
  }
  onValueChangeAffaire(newValue) {

    this.articleService.atriclesByProjet(this.myFormAdd.value.projet.id,this.myFormAdd.value.atelier.id).subscribe(data=>
      this.listeArticles = data

    );
  }
// Méthode pour sélectionner un fichier
  // Méthode pour sélectionner un fichier
  selectFile(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.fileSelected = file;
    } else {
      console.error('Aucun fichier sélectionné');
    }
  }

// Méthode pour enregistrer le fichier avec l'ID
  saveFileByIdSuivi(id: number, file: File) {
    if (file) {
      this.ofService.saveFileByIdOF(id, file).subscribe(
        (response) => {
          this.snackBar.open('Fichier enregistré avec succès!', 'Fermer', {duration: 3000});
        },
        (error) => {
          this.snackBar.open('Erreur lors de l\'enregistrement du fichier.', 'Fermer', {duration: 3000});
        }
      );
    }
  }
// Méthode pour gérer l'envoi du fichier
  saveFile(id: number): void {
    // Vérifier si un fichier a été sélectionné
    if (this.fileSelected) {
      // Appeler la méthode pour enregistrer le fichier
      this.saveFileByIdSuivi(id, this.fileSelected);
    } else {
      console.error('Aucun fichier sélectionné pour l\'envoi');
    }

    // Fermer le bouton (si nécessaire)
    if (this.closebutton && this.closebutton.nativeElement) {
      this.closebutton.nativeElement.click();
    }
  }
  // Cette méthode sera appelée à chaque changement de la date de début (date)
  onDateChange() {
    const selectedDate = this.myFormAdd.get('date')?.value;
    if (selectedDate) {
      this.minDate = selectedDate; // Si la date de début est définie, on la prend comme min
    } else {
      this.minDate = this.today; // Sinon, on met la date du jour comme min
    }
  }
}

