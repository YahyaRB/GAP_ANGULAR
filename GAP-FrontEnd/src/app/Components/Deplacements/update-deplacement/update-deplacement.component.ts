import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {DatePipe} from "@angular/common";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DeplacementService} from "../../../services/deplacement.service";
import {EmployeService} from "../../../services/employe.service";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import {Ideplacement} from "../../../services/Interfaces/ideplacement";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";

@Component({
  selector: 'app-update-deplacement',
  templateUrl: './update-deplacement.component.html',
  styleUrls: ['./update-deplacement.component.css'],
  providers:[DatePipe]
})
export class UpdateDeplacementComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public dpl:Ideplacement;
  myFormUpdate:FormGroup;

  @Input()
  ateliers:Iateliers[]=[];
  employes:Iemploye[]=[];
  projets:Iprojet[]=[];
  today:string;
  fileSelected:any;

  constructor(private deplacementService: DeplacementService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private datePipe: DatePipe,
              private projetService:ProjetService,
              private employeService:EmployeService,
              private roleService:RoleService,
              private tokenStorage:TokenStorageService,
              private snackBar: MatSnackBar) {
    this.ateliers=this.tokenStorage.getUser().atelier;
    this.projetService.getAll().subscribe(data=>this.projets=data);
  }

  onUpdate() {
    // Met à jour le projet sélectionné
    this.myFormUpdate.value.projet = this.projets.find(
      (projet) => projet.id === this.myFormUpdate.value.projet
    );

    // Met à jour l'atelier sélectionné
    this.myFormUpdate.value.atelier = this.ateliers.find(
      (atelier) => atelier.id === this.myFormUpdate.value.atelier
    );

    // Met à jour les employés sélectionnés
    const selectedEmployeeIds = this.myFormUpdate.value.employee || [];
    this.myFormUpdate.value.employee = this.employes.filter(
      (employe) => selectedEmployeeIds.includes(employe.id)
    );

    // Appel au service de mise à jour
    this.deplacementService
      .update(this.myFormUpdate.value, this.dpl.id, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            if(this.fileSelected){
              this.saveFile(data);
            }
            this.notifyService.showSuccess("Déplacement edité avec succées", "Modification Déplacement"); // Notification avec réponse texte
            this.initmyUpdateForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();
            this.fileSelected=[];

        },
        error: (error) => {
          console.error("Erreur lors de la modification :", error);
          this.notifyService.showError("Échec de la modification", "Erreur");
        },
      });
  }


  ngOnChanges(changes: SimpleChanges): void {
    if(this.dpl){
      this.affectToForm(this.dpl.id);

      this.employeService.getAllByAtelier(this.dpl.employee[0].ateliers.id).subscribe(data=>
        this.employes = data

      );
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
      employee: [null, Validators.required],
      nmbJours: ['', Validators.required],
      motif:['', Validators.required],
      pieceJointe:[''],
    });

  }
  private affectToForm(id:number){
    const employeeIds = this.dpl.employee.map(emp => emp.id);
    this.myFormUpdate.setValue({
      atelier:this.dpl.employee[0].ateliers.id ,
      projet: this.dpl.projet.id,
      employee: employeeIds,
      date: this.datePipe.transform(this.dpl.date, 'yyyy-MM-dd'),
      nmbJours: this.dpl.nmbJours,
      motif:this.dpl.motif,
      pieceJointe:""

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onValueChangeAtelier(newValue) {

    if (newValue) {
      this.myFormUpdate.get('employee').setValue(null);
      // Récupérez la liste des employés selon le nouvel atelier
      this.employeService.getAllByAtelier(newValue).subscribe(data => {
        this.employes = data; // Mettez à jour la liste des employés

      }, error => {
        console.error('Erreur lors de la récupération des employés:', error);
      });
    } else {
      // Si aucun atelier n'est sélectionné, videz la liste des employés
      this.employes = [];
    }
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





