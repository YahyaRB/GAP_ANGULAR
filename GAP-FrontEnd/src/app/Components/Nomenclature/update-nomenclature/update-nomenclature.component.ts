import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {INomenclature} from "../../../services/Interfaces/inomenclature";
import {NomenclatureService} from "../../../services/nomenclature.service";
import {TYPE_ALUMINIUM, TYPE_BOIS, TYPE_ELECTRICITE, TYPE_METALLIQUE} from "../../../../TypeNomenclature";

@Component({
  selector: 'app-update-nomenclature',
  templateUrl: './update-nomenclature.component.html',
  styleUrls: ['./update-nomenclature.component.css']
})
export class UpdateNomenclatureComponent implements OnChanges{
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input() nomenclature: INomenclature;
  @Input() ordreFabrication: IordreFabrication;

  myFormUpdate: FormGroup;

  // Types de nomenclature disponibles
  typesNomenclature = [];

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
    private nomenclatureService: NomenclatureService,
    private formBuilder: FormBuilder,
    private notifyService: NotificationService,
    private roleService: RoleService
  ) {}

  onUpdate() {
    if (this.myFormUpdate.valid && this.nomenclature && this.ordreFabrication) {
      const formData = this.myFormUpdate.value;

      // Calcul de la quantité totale : quantiteTot d'OF * quantité relative saisie
      const quantiteTotale = this.ordreFabrication.quantite * formData.quantite;

      // Calculer la nouvelle quantité restante
      const quantiteLivree = this.nomenclature.quantiteLivre || 0;
      const quantiteRest = Math.max(0, quantiteTotale - quantiteLivree);

      const nomenclatureData = {
        ...formData,
        quantite: quantiteTotale, // La quantité totale calculée
        quantiteLivre: quantiteLivree, // Garder la quantité déjà livrée
        quantiteRest: quantiteRest, // Recalculer le reste
        ordreFabricationId: this.ordreFabrication.id
      };

      this.nomenclatureService.updateNomenclature(this.nomenclature.id, nomenclatureData)
        .subscribe({
          next: (data: INomenclature) => {
            this.notifyService.showSuccess("Nomenclature modifiée avec succès", "Modification Nomenclature");
            this.initmyUpdateForm();
            this.closebutton.nativeElement.click();
            this.refreshTable.emit();
          },
          error: (error) => {
            console.error("Erreur lors de la modification :", error);
            this.notifyService.showError("Échec de la modification", "Erreur");
          },
        });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (this.nomenclature && this.ordreFabrication) {
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

      this.affectToForm();
    }
  }

  ngOnInit(): void {
    this.initmyUpdateForm();
  }

  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      type: ['', Validators.required],
      designation: ['', Validators.required],
      unite: ['', Validators.required],
      quantite: ['', [Validators.required, Validators.min(0.01)]], // Quantité relative
    });
  }

  private affectToForm() {
    if (this.nomenclature && this.ordreFabrication) {
      // Calcul de la quantité relative : quantité nomenclature / quantiteTot d'OF
      const quantiteRelative = this.ordreFabrication.quantite > 0
        ? this.nomenclature.quantite / this.ordreFabrication.quantite
        : 0;

      this.myFormUpdate.setValue({
        type: this.nomenclature.type || '',
        designation: this.nomenclature.designation || '',
        unite: this.nomenclature.unite || '',
        quantite: quantiteRelative
      });
    }
  }

  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string): boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  // Calcule et affiche la quantité totale qui sera enregistrée
  getQuantiteTotaleCalculee(): number {
    const quantiteRelative = this.myFormUpdate.get('quantite')?.value || 0;
    return this.ordreFabrication ? this.ordreFabrication.quantite * quantiteRelative : 0;
  }

  // Obtient la quantité relative actuelle (pour affichage)
  getQuantiteRelativeActuelle(): number {
    if (this.nomenclature && this.ordreFabrication && this.ordreFabrication.quantite > 0) {
      return this.nomenclature.quantite / this.ordreFabrication.quantite;
    }
    return 0;
  }

  protected readonly ATELIER_BOIS = TYPE_BOIS
  protected readonly ATELIER_ALUMINIUM = TYPE_ALUMINIUM
  protected readonly ATELIER_ELECTRICITE = TYPE_ELECTRICITE
  protected readonly ATELIER_METALLIQUE = TYPE_METALLIQUE
}
