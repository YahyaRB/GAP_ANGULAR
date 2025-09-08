import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {ArticleService} from "../../../services/article.service";
import {NotificationService} from "../../../services/notification.service";
import {ProjetService} from "../../../services/projet.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {RoleService} from "../../../services/role.service";
import {statutProjet} from "../../../statutProjet";
import {Iaffectation} from "../../../services/Interfaces/iaffectation";
import {Iemploye} from "../../../services/Interfaces/iemploye";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {OfService} from "../../../services/of.service";
import {AffectationService} from "../../../services/affectation.service";
import {EmployeService} from "../../../services/employe.service";
import {HttpErrorResponse} from "@angular/common/http";


@Component({
  selector: 'app-update-affectation',
  templateUrl: './update-affectation.component.html',
  styleUrls: ['./update-affectation.component.css']
})
export class UpdateAffectationComponent implements OnChanges,OnInit{
  @Output() refreshTable = new EventEmitter<void>();

  @ViewChild('closebutton') closebutton;
  @Input()
  public affectation:Iaffectation;

  myFormUpdate: FormGroup;
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
  onUpdate() {
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
    this.affectatioService.add(this.myFormUpdate.value).subscribe({
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
    this.myFormUpdate = this.formBuilder.group({
      ateliers: [null, Validators.required],
      projets: [null, Validators.required],
      date: ['', Validators.required],
      employees:[null, Validators.required],
      article: [null, Validators.required],
      periode:[null, Validators.required],
      nombreHeures:['', Validators.required],
    });

  }
  private affectToForm(id:number){
    this.myFormUpdate.setValue({
      ateliers: this.affectation.ateliers?.id,
      projets: this.affectation.projets.id,
      date: this.affectation.date,
      employees:this.affectation.employees.id,
      article: this.affectation.article.id,
      periode:this.affectation.periode,
      nombreHeures:this.affectation.nombreHeures,

    });

    this.employeServie.getAllByAtelier(this.affectation.ateliers.id).subscribe(data=>
      this.listeEmploye=data
    )
    this.projetService.findAffairesByAtelierAndQteArticle_Sup_QteOF(this.affectation.ateliers.id).subscribe(data=>
      this.projets = data
    );
    this.ofService.findOFByAtelierAndProjet(this.affectation.ateliers.id,this.affectation.projets.id).subscribe(data=>
      this.listeOFS = data

    );
  }
  ngOnInit(): void {
    const currentDate = new Date();
    this.today = currentDate.toISOString().split('T')[0]; // Formate la date en YYYY-MM-DD
    this.initmyForm();
  }
  onValueChangeAtelier(newValue) {
    this.myFormUpdate.patchValue({
      projet: null,  // Réinitialise l'affaire
      article: null, // Réinitialise l'article
      employees:null// Réinitialise les employes
    });
    this.employeServie.getAllByAtelier(this.myFormUpdate.value.ateliers.id).subscribe(data=>
      this.listeEmploye=data
    );
    this.projetService.findAffairesByAtelierAndQteArticle_Sup_QteOF(this.myFormUpdate.value.ateliers.id).subscribe(data=>
      this.projets = data

    );
  }
  onValueChangeAffaire(newValue) {
    this.myFormUpdate.patchValue({
      article: null  // Réinitialise l'article
    });
    this.ofService.findOFByAtelierAndProjet(this.myFormUpdate.value.ateliers.id,this.myFormUpdate.value.projets.id).subscribe(data=>
      this.listeOFS = data

    );
  }
  onMaterialGroupChange(event) {}
  onDateChange() {
    const selectedDate = this.myFormUpdate.get('date')?.value;
    if (selectedDate) {
      this.minDate = selectedDate; // Si la date de début est définie, on la prend comme min
    } else {
      this.minDate = this.today; // Sinon, on met la date du jour comme min
    }
  }
  onValueChangePeriode($event: any) {
    const periode = this.myFormUpdate.value.periode;

    this.showNombreHeures = periode === 'Heures' || periode === 'Heures_Sup';

    if (periode === 'Matin') {
      this.myFormUpdate.patchValue({ nombreHeures: 5 });
      this.myFormUpdate.get('nombreHeures')?.clearValidators(); // Supprime la validation
    }else if(periode === 'Après-midi') {
      this.myFormUpdate.patchValue({ nombreHeures: 4 });
      this.myFormUpdate.get('nombreHeures')?.clearValidators(); // Supprime la validation
    } else {
      this.myFormUpdate.patchValue({ nombreHeures: '' });
      this.myFormUpdate.get('nombreHeures')?.setValidators(Validators.required); // Réactive la validation
    }

    this.myFormUpdate.get('nombreHeures')?.updateValueAndValidity(); // Appliquer les modifications
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.affectation){
      this.affectToForm(this.affectation.id);
    }

  }



}


