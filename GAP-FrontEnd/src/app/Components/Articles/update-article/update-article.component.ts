import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ProjetService} from "../../../services/projet.service";
import {ArticleService} from "../../../services/article.service";
import {statutProjet} from "../../../statutProjet";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {RoleService} from "../../../services/role.service";

@Component({
  selector: 'app-update-article',
  templateUrl: './update-article.component.html',
  styleUrls: ['./update-article.component.css']
})
export class UpdateArticleComponent {
  @Output() refreshTable = new EventEmitter<void>();

  @ViewChild('closebutton') closebutton;
  @Input()
  public article:Iarticle;
  myFormUpdate:FormGroup;
  ateliers:Iateliers[]=[];
  projets:Iprojet[]=[];
  units: string[] = ['ENS', 'FT', 'M2', 'M3', 'ML', 'PR', 'UN'];

  constructor(private articleService: ArticleService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private projetService:ProjetService,
              private tokenStorage:TokenStorageService,
              private roleService:RoleService) {
    this.ateliers=this.tokenStorage.getUser().atelier;
  }

  onUpdate() {
    this.myFormUpdate.value.projet=this.projets.find(
      (projet) => (projet.id === this.myFormUpdate.value.projet));
    this.myFormUpdate.value.ateliers=this.ateliers.find(
      (atelier)=>(atelier.id === this.myFormUpdate.value.ateliers));

    this.articleService
      .update(this.myFormUpdate.value, this.article.id,{ responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {

            this.notifyService.showSuccess(data, "Modification Article"); // Notification avec réponse texte
            this.initmyUpdateForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();

        },
        error: (error) => {
          console.error("Erreur lors de la modification :", error);
          this.notifyService.showError("Échec de la modification", "Erreur");
        },
      });
  }


  ngOnChanges(changes: SimpleChanges): void {
    if(this.article){
      this.affectToForm(this.article.id);
      this.projets.push(this.article.projet)
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      ateliers: [null, Validators.required],
      projet: [null, Validators.required],
      unite: [null, Validators.required],
      numPrix: ['', Validators.required],
      quantiteTot: ['', Validators.required],
      designation:['', Validators.required],
    });

  }
  private affectToForm(id:number){
    this.myFormUpdate.setValue({
      ateliers:this.article.ateliers.id ,
      projet: this.article.projet.id,
      unite: this.article.unite,
      numPrix: this.article.numPrix,
      quantiteTot:  this.article.quantiteTot,
      designation: this.article.designation

    });

  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onValueChangeAtelier(newValue) {
    this.projetService.getAllByStaut(statutProjet.ACTIF).subscribe(data=>
      this.projets = data

    );
  }
}





