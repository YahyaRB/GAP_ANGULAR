import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

import {Iateliers} from "../../../services/Interfaces/iateliers";
import {ProjetService} from "../../../services/projet.service";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {AtelierService} from "../../../services/atelier.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {ArticleService} from "../../../services/article.service";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import {EmployeService} from "../../../services/employe.service";
import {NotificationService} from "../../../services/notification.service";
import {AffectationService} from "../../../services/affectation.service";
import {ListeAffectationsComponent} from "../liste-affectations/liste-affectations.component";
import {OfService} from "../../../services/of.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {HttpErrorResponse} from "@angular/common/http";


@Component({
  selector: 'app-add-affectation',
  templateUrl: './add-affectation.component.html',
  styleUrls: ['./add-affectation.component.css']
})
export class AddAffectationComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  projets:Iprojet[]=[];
  @Input()
  ateliers:Iateliers[]=[];
  @Input()
  listeEmploye: Iemploye[]=[];
  listeOFS:IordreFabrication[]=[];
  today:string;
  minDate: string = '';
  showNombreHeures: boolean;

  constructor(private formBuilder: FormBuilder,
              private ofService:OfService,
              private projetService:ProjetService,
              private affectatioService:AffectationService,
              private employeServie:EmployeService,
              private articleService:ArticleService,
              private notifyService: NotificationService,




  ) {

  }
  onAdd() {
    function getBackendMessage(err: HttpErrorResponse): string {
      if (err.status === 0) return 'Impossible de joindre le serveur.';
      if (typeof err.error === 'string' && err.error.trim()) {
        try { const j = JSON.parse(err.error); return j.message || j.error || err.error; }
        catch { return err.error; }
      }
      if (err.error && typeof err.error === 'object') {
        if (err.error.message) return err.error.message;   // Spring Boot 2 après include-message=always
        if (Array.isArray(err.error.errors) && err.error.errors.length) {
          return err.error.errors[0]?.defaultMessage || String(err.error.errors[0]);
        }
      }
      return err.statusText || 'Erreur inconnue';
    }



    function tryParse(s: string): any | null {
      try { return JSON.parse(s); } catch { return null; }
    }
    this.affectatioService.add(this.myFormAdd.value).subscribe({
      next: (message: string) => {
        this.notifyService.showSuccess(message, 'Ajout Affectation');
        this.initmyForm();
        this.closebutton?.nativeElement?.click();
        this.refreshTable.emit();
      },
      error: (err: HttpErrorResponse) => {
        const msg = getBackendMessage(err);
        this.notifyService.showError(getBackendMessage(err), 'Erreur');
        console.error('Erreur lors de l’ajout :', err);
      },
    });
  }





  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      ateliers: [null, Validators.required],
      projets: [null, Validators.required],
      date: ['', Validators.required],
      employees:[null, Validators.required],
      article: [null, Validators.required],
      periode:[null, Validators.required],
      nombreHeures:['', Validators.required],
    });

  }
  ngOnInit(): void {
    const currentDate = new Date();
    this.today = currentDate.toISOString().split('T')[0]; // Formate la date en YYYY-MM-DD
    this.initmyForm();
  }
  onValueChangeAtelier(newValue) {
    this.myFormAdd.patchValue({
      projet: null,  // Réinitialise l'affaire
      article: null, // Réinitialise l'article
      employees:null// Réinitialise les employes
    });
    this.employeServie.getAllByAtelier(this.myFormAdd.value.ateliers.id).subscribe(data=>
    this.listeEmploye=data
    )
    this.projetService.findAffairesByAtelierAndQteArticle_Sup_QteOF(this.myFormAdd.value.ateliers.id).subscribe(data=>
      this.projets = data

    );
  }
  onValueChangeAffaire(newValue) {
    this.myFormAdd.patchValue({
      article: null  // Réinitialise l'article
    });
    this.ofService.findOFByAtelierAndProjet(this.myFormAdd.value.ateliers.id,this.myFormAdd.value.projets.id).subscribe(data=>
      this.listeOFS = data

    );
  }
  onMaterialGroupChange(event) {}
  onDateChange() {
    const selectedDate = this.myFormAdd.get('date')?.value;
    if (selectedDate) {
      this.minDate = selectedDate; // Si la date de début est définie, on la prend comme min
    } else {
      this.minDate = this.today; // Sinon, on met la date du jour comme min
    }
  }
  onValueChangePeriode($event: any) {
    const periode = this.myFormAdd.value.periode;

    this.showNombreHeures = periode === 'Heures' || periode === 'Heures_Sup';

    if (periode === 'Matin') {
      this.myFormAdd.patchValue({ nombreHeures: 5 });
      this.myFormAdd.get('nombreHeures')?.clearValidators(); // Supprime la validation
    }else if(periode === 'Après-midi') {
      this.myFormAdd.patchValue({ nombreHeures: 4 });
      this.myFormAdd.get('nombreHeures')?.clearValidators(); // Supprime la validation
    } else {
      this.myFormAdd.patchValue({ nombreHeures: '' });
      this.myFormAdd.get('nombreHeures')?.setValidators(Validators.required); // Réactive la validation
    }

    this.myFormAdd.get('nombreHeures')?.updateValueAndValidity(); // Appliquer les modifications
  }



}

/*
import {Component, Input, ViewChild} from '@angular/core';

import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ListeOFComponent} from "../../OrdreFabrication/liste-of/liste-of.component";
import {OfService} from "../../../services/of.service";
import {ArticleService} from "../../../services/article.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-add-affectation',
  templateUrl: './add-affectation.component.html',
  styleUrls: ['./add-affectation.component.css']
})
export class AddAffectationComponent {

  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  ordresFabrications:IordreFabrication[]=[];




  fileSelected:any;

  constructor(private notifyService: NotificationService,
              private ofC:ListeOFComponent,
              private formBuilder: FormBuilder,
              private ofService: OfService,

              private projetService:ProjetService,
              private roleService: RoleService,
              private tokenStorage:TokenStorageService,
              private snackBar: MatSnackBar

  ) {
  }


  onAdd() {
    this.ofService
      .add(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {
          setTimeout(() => {
            if(this.fileSelected){
              this.saveFile(data);
            }
            this.notifyService.showSuccess("Ordre de fabrication ajouté avec succées", "Ajout Ordre de fabrication"); // Notification avec réponse texte
            this.initmyForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.ofC.ngOnInit(); // Rafraîchissement des données
            this.fileSelected=[];
          }, 400);
        },
        error: (error) => {
          console.error("Erreur lors de l'ajout :", error);
          this.notifyService.showError("Échec de l'ajout", "Erreur");
        },
      });
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

  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
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




}

*/
