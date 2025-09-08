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
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {ListeOFComponent} from "../liste-of/liste-of.component";
import {OfService} from "../../../services/of.service";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {ArticleService} from "../../../services/article.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {elementAt} from "rxjs";

@Component({
  selector: 'app-update-of',
  templateUrl: './update-of.component.html',
  styleUrls: ['./update-of.component.css'],
  providers:[DatePipe]
})
export class UpdateOFComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public ordreFabrication:IordreFabrication;
  myFormUpdate:FormGroup;
  @Input()
  ordresFabrications:IordreFabrication[]=[];
  @Input()
  ateliers:Iateliers[]=[];
  articles:Iarticle[]=[];
  projets:Iprojet[]=[];
  today:string;
  fileSelected:any;

  constructor(private ofService: OfService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private datePipe: DatePipe,
              private projetService:ProjetService,
              private articleService:ArticleService,
              private roleService:RoleService,
              private snackBar: MatSnackBar) {}

  onUpdate() {
    const qteArcticleAutorise = this.ordreFabrication.article.quantiteTot - this.ordreFabrication.article.quantiteEnProd+this.ordreFabrication.quantite
    if (this.myFormUpdate.value.quantite <= qteArcticleAutorise) {
      this.myFormUpdate.value.projet = this.projets.find(
        (projet) => (projet.id === this.myFormUpdate.value.projet));
      this.myFormUpdate.value.atelier = this.ateliers.find(
        (atelier) => (atelier.id === this.myFormUpdate.value.atelier));
      this.myFormUpdate.value.article = this.articles.find(
        (article) => (article.id === this.myFormUpdate.value.article));
      this.ofService
        .update(this.myFormUpdate.value, this.ordreFabrication.id, {responseType: 'text'}) // On force le texte comme réponse
        .subscribe({
          next: (data: any) => {

              if (this.fileSelected) {
                this.saveFile(data);
              }

              this.notifyService.showSuccess("Ordre de fabrication edité avec succés", "Modification Ordre de fabrication"); // Notification avec réponse texte
              this.initmyUpdateForm(); // Réinitialisation du formulaire
              this.closebutton.nativeElement.click(); // Fermeture de la modale
              this.refreshTable.emit(); // Rafraîchissement des données
              this.fileSelected = [];

          },
          error: (error) => {
            console.error("Erreur lors de la modification :", error);
            this.notifyService.showError("Échec de la modification", "Erreur");
          },
        });
    }else{
      this.notifyService.showError("Vous avez dépassé la quantité d'article", "Erreur")
    }
  }


  ngOnChanges(changes: SimpleChanges): void {
    if(this.ordreFabrication){
      this.articles=[];
      this.projets=[];
      this.affectToForm(this.ordreFabrication.id);
      this.articles.push(this.ordreFabrication.article)
      this.projets.push(this.ordreFabrication.projet)
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      atelier: [null, Validators.required],
      projet: [null, Validators.required],
      date: ['', Validators.required],
      article: [null, Validators.required],
      quantite: ['', Validators.required],
      dateFin:['', Validators.required],
      description:['', Validators.required],
      pieceJointe:[''],
    });

  }
  private affectToForm(id:number){
    this.myFormUpdate.setValue({

      atelier:this.ordreFabrication.atelier.id ,
      projet: this.ordreFabrication.projet.id,
      article: this.ordreFabrication.article.id,
      date: this.datePipe.transform(this.ordreFabrication.date, 'yyyy-MM-dd'),
      quantite: this.ordreFabrication.quantite,
      dateFin:this.datePipe.transform(this.ordreFabrication.dateFin, 'yyyy-MM-dd'),
      description:this.ordreFabrication.description,
      pieceJointe:""

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onValueChangeAtelier(newValue) {
    this.projetService.findAffairesByAtelierAndQteArticle_Sup_QteOF(this.myFormUpdate.value.atelier.id).subscribe(data=>
      this.projets = data

    );
  }
  onValueChangeAffaire(newValue) {
    this.articleService.atriclesByProjet(this.myFormUpdate.value.projet.id,this.myFormUpdate.value.atelier.id).subscribe(data=>
      this.articles = data

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
}




