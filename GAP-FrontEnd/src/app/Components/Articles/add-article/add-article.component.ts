import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {NotificationService} from "../../../services/notification.service";
import {ArticleService} from "../../../services/article.service";
import {ProjetService} from "../../../services/projet.service";
import {RoleService} from "../../../services/role.service";
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {statutProjet} from "../../../statutProjet";

@Component({
  selector: 'app-add-article',
  templateUrl: './add-article.component.html',
  styleUrls: ['./add-article.component.css']
})
export class AddArticleComponent implements OnInit{

  @Output() refreshTable = new EventEmitter<void>();

  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  @Input()
  articles:Iarticle[]=[];
  ateliers:Iateliers[]=[];
  projets:Iprojet[]=[];
  units: string[] = ['ENS', 'FT', 'M2', 'M3', 'ML', 'PR', 'UN'];
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private articleService: ArticleService,
              private projetService:ProjetService,
              private roleService: RoleService,
              private tokenStorage:TokenStorageService,

  ) {
  }
  onMaterialGroupChange(event) {}

  onAdd() {
    this.articleService
      .add(this.myFormAdd.value, { responseType: 'text' }) // On force le texte comme réponse
      .subscribe({
        next: (data: any) => {


            this.notifyService.showSuccess(data, "Ajout Article"); // Notification avec réponse texte
            this.initmyForm(); // Réinitialisation du formulaire
            this.closebutton.nativeElement.click(); // Fermeture de la modale
            this.refreshTable.emit();

        },
        error: (error) => {
          console.error("Erreur lors de l'ajout :", error);
          this.notifyService.showError("Échec de l'ajout", "Erreur");
        },
      });
  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      ateliers: [null, Validators.required],
      projet: [null, Validators.required],
      unite: [null, Validators.required],
      numPrix: ['', Validators.required],
      quantiteTot: ['', Validators.required],
      designation:['', Validators.required],

    });

  }
  ngOnInit(): void {
    this.ateliers=this.tokenStorage.getUser().atelier;
    this.initmyForm();
  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  onValueChangeAtelier(newValue) {

    this.projetService.getAllByStaut(statutProjet.ACTIF).subscribe(data=>
      this.projets = data

    );
  }





}


