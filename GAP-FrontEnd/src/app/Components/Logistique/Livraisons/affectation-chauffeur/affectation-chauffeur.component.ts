import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Ilivraison} from "../../../../services/Interfaces/ilivraison";
import {Iateliers} from "../../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../../services/notification.service";
import {ListeLivraisonsComponent} from "../../../Livraison/liste-livraisons/liste-livraisons.component";
import {LivraisonService} from "../../../../services/livraison.service";
import {ProjetService} from "../../../../services/projet.service";
import {RoleService} from "../../../../services/role.service";
import {ListeLivraisonLogComponent} from "../liste-livraison-log/liste-livraison-log.component";
import {DatePipe} from "@angular/common";
import {Ichauffeur} from "../../../../services/Interfaces/ichauffeur";
import {ChauffeurService} from "../../../../services/chauffeur.service";
import {DetailLivraisonService} from "../../../../services/detail-livraison.service";
import {IdetailLivraison} from "../../../../services/Interfaces/idetail-livraison";
declare var $: any;
@Component({
  selector: 'app-affectation-chauffeur',
  templateUrl: './affectation-chauffeur.component.html',
  styleUrls: ['./affectation-chauffeur.component.css'],
  providers: [DatePipe]
})
export class AffectationChauffeurComponent implements OnInit,OnChanges,AfterViewInit {
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
  details:IdetailLivraison[]=[];
  chauffeurs:Ichauffeur[]=[];
  today:string;
  isDetailsOpen = false;
  constructor(private livraisonService: LivraisonService,
              private formBuilder: FormBuilder,
              private detailLivraisonService:DetailLivraisonService,
              private notifyService : NotificationService,
              private datePipe: DatePipe,
              private projetService:ProjetService,
              private chauffeurService:ChauffeurService,
              private roleService:RoleService) {
    this.chauffeurService.getAll().subscribe(data=>
    this.chauffeurs=data
    );

  }

  ngAfterViewInit(): void {

    // Option : ouvrir automatiquement à l’ouverture de la modale
    $('#modalAffectation').on('shown.bs.modal', () => {
      // $('#detailsLivraison').collapse('show'); // décommente si tu veux auto-ouvrir
    });

    // (Option) hooks d’événements pour réagir à l’ouverture/fermeture
    $('#detailsLivraison')
      .on('shown.bs.collapse', () => { /* ... */ })
      .on('hidden.bs.collapse', () => { /* ... */ });
  }

  // Contrôle programmatique avec jQuery
  openDetails(): void {
    $('#detailsLivraison').collapse('show');
  }
  closeDetails(): void {
    $('#detailsLivraison').collapse('hide');
  }
  toggleDetails(): void {
    $('#detailsLivraison').collapse('toggle');
  }

  // TrackBy simple (performances)
  trackByIndex(index: number): number { return index; }


  onUpdate() {
    this.myFormUpdate.value.chauffeur=this.chauffeurs.find((chauffeur)=>chauffeur.id===this.myFormUpdate.value.chauffeur)

    this.livraisonService
      .affectChauffeur(this.myFormUpdate.value, this.livraison.id,{ responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {
          setTimeout(() => {
            this.notifyService.showSuccess(data, "Modification Livraison"); // Notification avec réponse texte
            this.initmyUpdateForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();
          }, 400);
        },
        error: (error) => {
          console.error("Erreur lors de la modification :", error);
          this.notifyService.showError("Échec de la modification", "Erreur");
        },
      });
  }


  ngOnChanges(changes: SimpleChanges): void {
    this.initmyUpdateForm();
    if(this.livraison){
      this.detailLivraisonService.getListeDetailByLivraison(this.livraison.id).subscribe({
        next: (data) => {
          this.details = data;

        },
        error: (err) => {
          console.error('Erreur lors du chargement:', err);

        }
      });
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
      chauffeur: ['', Validators.required],
    });

  }
  private affectUsertoForm(id:number){
    this.myFormUpdate.setValue({
      dateLivraison:this.datePipe.transform(this.livraison.dateLivraison, 'yyyy-MM-dd'),
      atelier: this.livraison.atelier,
      projet: this.livraison.projet,
      chauffeur:this.livraison.chauffeur?this.livraison.chauffeur.id : null

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




