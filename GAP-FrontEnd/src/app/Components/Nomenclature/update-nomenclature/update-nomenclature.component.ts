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
export class UpdateNomenclatureComponent implements OnChanges {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input() nomenclature: INomenclature;
  @Input() ordreFabrication: IordreFabrication;

  myFormUpdate: FormGroup;

  // Flag pour choisir le mode de saisie
  useModeAbsolu = false;

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

      // Calculer la quantité totale selon le mode choisi
      let quantiteTotale: number;

      if (this.useModeAbsolu) {
        // Mode absolu : utiliser directement la quantité saisie
        quantiteTotale = formData.quantite;
      } else {
        // Mode relatif : multiplier par la quantité de l'OF
        quantiteTotale = this.ordreFabrication.quantite * formData.quantite;
      }

      // Vérifier que la nouvelle quantité totale est cohérente avec les livraisons
      const quantiteLivree = this.nomenclature.quantiteLivre || 0;

      if (quantiteTotale < quantiteLivree) {
        this.notifyService.showError(
          `La quantité totale (${quantiteTotale}) ne peut pas être inférieure à la quantité déjà livrée (${quantiteLivree})`,
          "Erreur de quantité"
        );
        return;
      }

      // Calculer la nouvelle quantité restante
      const quantiteRest = Math.max(0, quantiteTotale - quantiteLivree);

      const nomenclatureData = {
        ...formData,
        quantite: quantiteTotale, // CORRECTION : Utiliser "quantite" au lieu de "quantiteTot"
        quantiteLivre: quantiteLivree, // Garder la quantité déjà livrée
        quantiteRest: quantiteRest, // Recalculer le reste
        ordreFabricationId: this.ordreFabrication.id
      };

      console.log('Données envoyées:', nomenclatureData);

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
      var atelier = this.ordreFabrication.atelier.id;

      if (atelier == 1) {
        this.typesNomenclature = this.ATELIER_ALUMINIUM;
      } else if (atelier == 2) {
        this.typesNomenclature = this.ATELIER_BOIS;
      } else if (atelier == 3) {
        this.typesNomenclature = this.ATELIER_METALLIQUE;
      } else {
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
      quantite: ['', [Validators.required, Validators.min(0.01)]], // Quantité selon le mode choisi
    });
  }

  private affectToForm() {
    if (this.nomenclature && this.ordreFabrication) {
      let quantiteAffichee: number;

      // CORRECTION : Utiliser "quantite" au lieu de "quantiteTot"
      const quantiteTotale = this.nomenclature.quantite || 0;  // ← Changement ici

      if (this.useModeAbsolu) {
        // Mode absolu : afficher la quantité totale directement
        quantiteAffichee = quantiteTotale;
      } else {
        // Mode relatif : calculer la quantité relative (quantité par unité d'OF)
        quantiteAffichee = this.ordreFabrication.quantite > 0
          ? quantiteTotale / this.ordreFabrication.quantite
          : 0;
      }

      // Debug pour comprendre les calculs
      console.log('=== AFFECT TO FORM DEBUG ===');
      console.log('Mode absolu:', this.useModeAbsolu);
      console.log('Nomenclature quantite (DTO):', this.nomenclature.quantite);
      console.log('Nomenclature quantiteRest:', this.nomenclature.quantiteRest);
      console.log('OF quantité:', this.ordreFabrication.quantite);
      console.log('Quantité affichée calculée:', quantiteAffichee);

      // En mode absolu, ne jamais forcer une valeur minimale incorrecte
      // En mode relatif, s'assurer d'une valeur minimale valide
      if (!this.useModeAbsolu && quantiteAffichee === 0) {
        quantiteAffichee = 0.01; // Valeur minimale par défaut seulement en mode relatif
      }

      this.myFormUpdate.patchValue({
        type: this.nomenclature.type || '',
        designation: this.nomenclature.designation || '',
        unite: this.nomenclature.unite || '',
        quantite: this.formatNumber(quantiteAffichee, 4)
      });

      console.log('Valeur finale dans le formulaire:', this.myFormUpdate.get('quantite')?.value);
    }
  }

  // Basculer entre mode absolu et relatif
  toggleMode() {
    this.useModeAbsolu = !this.useModeAbsolu;
    this.affectToForm(); // Recalculer la valeur affichée
  }

  // Obtient le label pour le champ quantité
  getQuantiteLabel(): string {
    return this.useModeAbsolu ? 'Quantité totale' : 'Quantité par unité';
  }

  // Obtient le placeholder pour le champ quantité
  getQuantitePlaceholder(): string {
    if (this.useModeAbsolu) {
      return 'Quantité totale nécessaire';
    } else {
      return 'Quantité nécessaire par unité d\'OF';
    }
  }

  // Obtient le texte d'aide pour le champ quantité
  getQuantiteHelpText(): string {
    if (this.useModeAbsolu) {
      return 'Quantité totale nécessaire pour tout l\'ordre de fabrication';
    } else {
      return 'Quantité nécessaire pour produire 1 unité de l\'ordre de fabrication';
    }
  }

  // Calcule et affiche la quantité totale qui sera enregistrée
  getQuantiteTotaleCalculee(): number {
    const quantiteSaisie = this.myFormUpdate.get('quantite')?.value || 0;

    if (this.useModeAbsolu) {
      return quantiteSaisie;
    } else {
      return this.ordreFabrication ? this.ordreFabrication.quantite * quantiteSaisie : 0;
    }
  }

  // Obtient la quantité relative actuelle (pour affichage)
  getQuantiteRelativeActuelle(): number {
    if (this.nomenclature && this.ordreFabrication && this.ordreFabrication.quantite > 0) {
      return (this.nomenclature.quantite || 0) / this.ordreFabrication.quantite; // CORRECTION
    }
    return 0;
  }

  // Obtient la quantité absolue actuelle
  getQuantiteAbsolueActuelle(): number {
    return this.nomenclature?.quantite || 0; // CORRECTION
  }

  // Vérifie si la nouvelle quantité est cohérente
  isQuantiteCoherente(): boolean {
    const nouvelleQuantiteTotale = this.getQuantiteTotaleCalculee();
    const quantiteLivree = this.nomenclature?.quantiteLivre || 0;
    return nouvelleQuantiteTotale >= quantiteLivree;
  }

  // Obtient le message d'erreur pour les quantités incohérentes
  getMessageErreurQuantite(): string {
    const nouvelleQuantiteTotale = this.getQuantiteTotaleCalculee();
    const quantiteLivree = this.nomenclature?.quantiteLivre || 0;

    if (nouvelleQuantiteTotale < quantiteLivree) {
      return `La quantité totale (${this.formatNumber(nouvelleQuantiteTotale, 2)}) ne peut pas être inférieure à la quantité déjà livrée (${this.formatNumber(quantiteLivree, 2)})`;
    }
    return '';
  }

  // Formater un nombre avec un nombre de décimales spécifique
  private formatNumber(value: number, decimals: number): number {
    return Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
  }

  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string): boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  protected readonly ATELIER_BOIS = TYPE_BOIS;
  protected readonly ATELIER_ALUMINIUM = TYPE_ALUMINIUM;
  protected readonly ATELIER_ELECTRICITE = TYPE_ELECTRICITE;
  protected readonly ATELIER_METALLIQUE = TYPE_METALLIQUE;
}
