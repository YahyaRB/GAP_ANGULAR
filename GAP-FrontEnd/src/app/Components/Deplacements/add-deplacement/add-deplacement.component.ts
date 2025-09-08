import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DeplacementService} from "../../../services/deplacement.service";
import {EmployeService} from "../../../services/employe.service";
import {Iemploye} from "../../../services/Interfaces/iemploye";

@Component({
  selector: 'app-add-deplacement',
  templateUrl: './add-deplacement.component.html',
  styleUrls: ['./add-deplacement.component.css']
})
export class AddDeplacementComponent {

  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  listeAteliers:Iateliers[]=[];
  employes:Iemploye[]=[];
  projets:Iprojet[]=[];
  fileSelected:any;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private deplacementService: DeplacementService,
              private employeService:EmployeService,
              private projetService:ProjetService,
              private roleService: RoleService,
              private tokenStorage:TokenStorageService,
              private snackBar: MatSnackBar

  ) {
    this.listeAteliers=this.tokenStorage.getUser().atelier;
    this.projetService.getAll().subscribe(data=>this.projets=data);
  }
  onMaterialGroupChange(event) {}

  onAdd() {
    this.deplacementService
      .add(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            if(this.fileSelected){
              this.saveFile(data);
            }
            this.notifyService.showSuccess("Déplacement ajouté avec succées", "Ajout Déplacement"); // Notification avec réponse texte
            this.initmyForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();
            this.fileSelected=[];

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
      employee: [null, Validators.required],
      nmbJours: ['', Validators.required],
      motif:['', Validators.required],
      pieceJointe:['', Validators.required],
    });

  }
  ngOnInit(): void {

   // alert(this.ateliers.length);
    this.initmyForm();
  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  onValueChangeAtelier(newValue) {

    this.employeService.getAllByAtelier(this.myFormAdd.value.atelier.id).subscribe(data=>
      this.employes = data

    );
  }
/*  onValueChangeAffaire(newValue) {

    this.articleService.atriclesByProjet(this.myFormAdd.value.projet.id,this.myFormAdd.value.atelier.id).subscribe(data=>
      this.listeArticles = data

    );
  }*/
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
      this.deplacementService.saveFileByIdOF(id, file).subscribe(
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

