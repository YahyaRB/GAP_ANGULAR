import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {INomenclature} from "../../../services/Interfaces/inomenclature";
import {NomenclatureService} from "../../../services/nomenclature.service";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {TYPE_ALUMINIUM, TYPE_BOIS, TYPE_ELECTRICITE, TYPE_METALLIQUE} from "../../../../TypeNomenclature";

@Component({
  selector: 'app-add-nomenclature',
  templateUrl: './add-nomenclature.component.html',
  styleUrls: ['./add-nomenclature.component.css']
})
export class AddNomenclatureComponent implements OnChanges{

  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input() ordreFabrication: IordreFabrication;

  myFormAdd: FormGroup;

  // Types de nomenclature disponibles
  typesNomenclature = [
    { value: 'Matière Première', label: 'Matière Première' },
    { value: 'Composant', label: 'Composant' },
    { value: 'Outil', label: 'Outil' },
    { value: 'Consommable', label: 'Consommable' }
  ];

  // Unités disponibles
  unites = [
    { value: 'UN', label: 'Unité' },
    { value: 'KG', label: 'Kilogramme' },
    { value: 'L', label: 'Litre' },
    { value: 'M', label: 'Mètre' },
    { value: 'M2', label: 'Mètre carré' },
    { value: 'M3', label: 'Mètre cube' }
  ];

  constructor(
    private notifyService: NotificationService,
    private formBuilder: FormBuilder,
    private nomenclatureService: NomenclatureService,
    private roleService: RoleService
  ) {
  }

  onAdd() {
    if (this.myFormAdd.valid && this.ordreFabrication) {
      const formData = this.myFormAdd.value;

      // Calcul de la quantité totale : quantiteTot d'OF * quantité saisie
      const quantiteTotale = this.ordreFabrication.quantite * formData.quantite;

      const nomenclatureData = {
        ...formData,
        quantite: quantiteTotale, // La quantité totale calculée
        quantiteLivre: 0, // Initialement 0
        quantiteRest: quantiteTotale, // Même valeur que quantite au départ
        ordreFabricationId: this.ordreFabrication.id
      };

      this.nomenclatureService.createNomenclature(nomenclatureData,this.ordreFabrication.id)
        .subscribe({
          next: (data: INomenclature) => {
            this.notifyService.showSuccess("Nomenclature ajoutée avec succès", "Ajout Nomenclature");
            this.initmyForm();
            this.closebutton.nativeElement.click();
            this.refreshTable.emit();
          },
          error: (error) => {
            console.error("Erreur lors de l'ajout :", error);
            this.notifyService.showError("Échec de l'ajout", "Erreur");
          },
        });
    }
  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      type: ['', Validators.required],
      designation: ['', Validators.required],
      unite: ['', Validators.required],
      quantite: ['', [Validators.required, Validators.min(0.01)]], // Quantité relative (par unité d'OF)
    });
  }

  ngOnInit(): void {

    this.initmyForm();
  }

  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string): boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  // Calcule et affiche la quantité totale qui sera enregistrée
  getQuantiteTotaleCalculee(): number {

    const quantiteRelative = this.myFormAdd.get('quantite')?.value || 0;
    return this.ordreFabrication ? this.ordreFabrication.quantite * quantiteRelative : 0;
  }


  ngOnChanges(changes: SimpleChanges): void {

    if (this.ordreFabrication) {
      var atelier=this.ordreFabrication.atelier.id
      if(atelier==1){
        this.typesNomenclature = this.ATELIER_ALUMINIUM;
      }else if(atelier==2){
        this.typesNomenclature = this.ATELIER_BOIS;
      }else if(atelier==3){
        this.typesNomenclature = this.ATELIER_METALLIQUE;
      }else{
        this.typesNomenclature = this.ATELIER_ELECTRICITE;
      }


    }
  }
  protected readonly ATELIER_BOIS = TYPE_BOIS;
  protected readonly ATELIER_ALUMINIUM = TYPE_ALUMINIUM;
  protected readonly ATELIER_ELECTRICITE = TYPE_ELECTRICITE;
  protected readonly ATELIER_METALLIQUE = TYPE_METALLIQUE;
}
