import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AffectationService } from "../../../services/affectation.service";
import { NotificationService } from "../../../services/notification.service";
import { Iateliers } from "../../../services/Interfaces/iateliers";

declare var $: any; // Déclaration jQuery

export interface AffectationPreview {
  tempId: string;
  employeeId: number;
  employeeName: string;
  employeeMatricule: string;
  atelierId: number;
  atelierDesignation: string;
  projetId: number;
  projetCode: string;
  projetDesignation: string;
  articleId: number;
  articleNumPrix: string;
  articleDesignation: string;
  date: Date;
  periode: string;
  nombreHeures: number;
  canModifyHours: boolean;
  hasConflict: boolean;
  conflictMessage?: string;
}
@Component({
  selector: 'app-duplicate-affectation',
  templateUrl: './duplicate-affectation.component.html',
  styleUrls: ['./duplicate-affectation.component.css']
})
export class DuplicateAffectationComponent implements OnInit {
  @Input() ateliers: Iateliers[] = [];
  @Output() refreshTable = new EventEmitter<void>();

  duplicateForm: FormGroup;
  isLoading = false;
  message = '';
  messageType = '';

  // États de l'interface - 3 étapes
  currentStep = 1; // 1: Source, 2: Destination, 3: Prévisualisation
  previewData: AffectationPreview[] = [];
  selectedItems: Set<string> = new Set();
  selectAll = false;

  periodes = [
    { value: 'Matin', label: 'Matin' },
    { value: 'Après-midi', label: 'Après-midi' },
    { value: 'Heures', label: 'Heures' },
    { value: 'Heures_Sup', label: 'Heures Supplémentaires' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private affectationService: AffectationService,
    private notificationService: NotificationService
  ) { }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.duplicateForm = this.formBuilder.group({
      // Étape 1: Source
      atelierId: ['', Validators.required],
      sourceDate: ['', Validators.required],
      sourcePeriod: ['', Validators.required],

      // Étape 2: Destination
      targetDate: ['', Validators.required],
      targetPeriod: ['', Validators.required]
    });
  }

  // Navigation entre les étapes
  goToStep2(): void {
    const step1Valid = this.duplicateForm.get('atelierId')?.valid &&
      this.duplicateForm.get('sourceDate')?.valid &&
      this.duplicateForm.get('sourcePeriod')?.valid;

    if (step1Valid) {
      this.currentStep = 2;
      this.message = '';
    } else {
      this.message = 'Veuillez remplir tous les champs de l\'étape 1';
      this.messageType = 'error';
    }
  }

  goBackToStep1(): void {
    this.currentStep = 1;
    this.message = '';
  }

  goBackToStep2(): void {
    this.currentStep = 2;
    this.message = '';
    this.previewData = [];
    this.selectedItems.clear();
  }

  // Étape 2 -> 3: Prévisualisation
  onPreview(): void {
    const step2Valid = this.duplicateForm.get('targetDate')?.valid &&
      this.duplicateForm.get('targetPeriod')?.valid;

    if (!step2Valid) {
      this.message = 'Veuillez remplir tous les champs de l\'étape 2';
      this.messageType = 'error';
      return;
    }

    this.isLoading = true;
    this.message = '';

    const formValue = this.duplicateForm.value;
    const request = {
      atelierId: formValue.atelierId,
      sourceDate: new Date(formValue.sourceDate),
      targetDate: new Date(formValue.targetDate),
      periodes: [formValue.sourcePeriod], // Utiliser la période source sélectionnée
      targetPeriod: formValue.targetPeriod,
      userId: 1
    };

    console.log('Requête envoyée:', request); // Debug

    this.affectationService.previewDuplication(request).subscribe(
      (data: AffectationPreview[]) => {
        console.log('Données reçues:', data); // Debug

        // Marquer les items avec heures nulles comme ayant un conflit
        data.forEach(item => {
          if (item.canModifyHours && (item.nombreHeures === null || item.nombreHeures === undefined)) {
            item.hasConflict = true;
            item.conflictMessage = 'Veuillez saisir le nombre d\'heures';
          }
        });

        this.previewData = data;
        this.selectedItems = new Set(data.filter(item => !item.hasConflict).map(item => item.tempId));
        this.updateSelectAllState();
        this.currentStep = 3;
        this.isLoading = false;
      },
      (error) => {
        console.error('Erreur complète:', error); // Debug détaillé

        let errorMessage = 'Erreur lors de la prévisualisation';

        if (error.error) {
          if (typeof error.error === 'string') {
            errorMessage += ': ' + error.error;
          } else if (error.error.message) {
            errorMessage += ': ' + error.error.message;
          } else {
            errorMessage += ': ' + JSON.stringify(error.error);
          }
        } else if (error.message) {
          errorMessage += ': ' + error.message;
        } else if (error.status) {
          errorMessage += ': Erreur HTTP ' + error.status;
          if (error.statusText) {
            errorMessage += ' - ' + error.statusText;
          }
        }

        this.message = errorMessage;
        this.messageType = 'error';
        this.isLoading = false;
      }
    );
  }

  // Gestion de la sélection dans la table
  onItemSelect(tempId: string, event: any): void {
    if (event.target.checked) {
      this.selectedItems.add(tempId);
    } else {
      this.selectedItems.delete(tempId);
      this.selectAll = false;
    }
    this.updateSelectAllState();
  }

  onSelectAllChange(event: any): void {
    this.selectAll = event.target.checked;
    if (this.selectAll) {
      this.selectedItems = new Set(
        this.previewData.filter(item => !item.hasConflict).map(item => item.tempId)
      );
    } else {
      this.selectedItems.clear();
    }
  }

  private updateSelectAllState(): void {
    const selectableItems = this.previewData.filter(item => !item.hasConflict);
    this.selectAll = selectableItems.length > 0 &&
      selectableItems.every(item => this.selectedItems.has(item.tempId));
  }

  // Modification des heures
  onHoursChange(item: AffectationPreview, newHours: number): void {
    if (item.canModifyHours) {
      // Accepter 0 ou null pour permettre la saisie
      if (newHours === null || newHours === undefined || newHours === 0) {
        item.nombreHeures = null;
        item.hasConflict = true;
        item.conflictMessage = 'Veuillez saisir un nombre d\'heures valide';
        this.selectedItems.delete(item.tempId);
        return;
      }

      if (newHours > 0) {
        item.nombreHeures = newHours;

        // Recalculer les conflits si nécessaire
        if (item.periode !== 'Heures_Sup' && newHours > 9) {
          item.hasConflict = true;
          item.conflictMessage = 'Les heures ne peuvent pas dépasser 9 pour cette période';
          this.selectedItems.delete(item.tempId);
        } else {
          // Réinitialiser le conflit si les heures sont valides
          item.hasConflict = false;
          item.conflictMessage = undefined;
          // Ajouter automatiquement l'item à la sélection
          this.selectedItems.add(item.tempId);
        }
      } else {
        item.hasConflict = true;
        item.conflictMessage = 'Les heures doivent être supérieures à 0';
        this.selectedItems.delete(item.tempId);
      }

      // Mettre à jour l'état du "Sélectionner tout"
      this.updateSelectAllState();
    }
  }

  // Supprimer un élément de la liste
  removeItem(tempId: string): void {
    this.previewData = this.previewData.filter(item => item.tempId !== tempId);
    this.selectedItems.delete(tempId);
    this.updateSelectAllState();
  }

  // Enregistrer les affectations sélectionnées
  onSave(): void {
    const itemsToSave = this.previewData.filter(item => this.selectedItems.has(item.tempId));

    if (itemsToSave.length === 0) {
      this.message = 'Veuillez sélectionner au moins une affectation à enregistrer';
      this.messageType = 'error';
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.affectationService.saveDuplicatedAffectations(itemsToSave).subscribe(
      (response: string) => {
        this.isLoading = false;
        this.refreshTable.emit();

        // Afficher un toaster de succès
        const count = itemsToSave.length;
        this.notificationService.showSuccess(
          `${count} affectation(s) enregistrée(s) avec succès`,
          'Duplication réussie'
        );

        // Réinitialiser le composant
        this.resetComponent();

        // Fermer le modal avec jQuery (Bootstrap)
        setTimeout(() => {
          $('#duplicateModal').modal('hide');
          // Supprimer le backdrop si présent
          $('.modal-backdrop').remove();
          $('body').removeClass('modal-open');
        }, 300);
      },
      (error) => {
        this.message = error.error || 'Erreur lors de l\'enregistrement';
        this.messageType = 'error';
        this.isLoading = false;

        // Afficher aussi un toaster d'erreur
        this.notificationService.showError(
          error.error || 'Erreur lors de l\'enregistrement',
          'Erreur'
        );
      }
    );
  }

  // Navigation
  goBack(): void {
    this.currentStep = 1;
    this.message = '';
    this.previewData = [];
    this.selectedItems.clear();
  }

  onCancel(): void {
    this.resetComponent();
  }

  private resetComponent(): void {
    this.currentStep = 1;
    this.duplicateForm.reset();
    this.previewData = [];
    this.selectedItems.clear();
    this.selectAll = false;
    this.message = '';
    this.messageType = '';
    this.isLoading = false;
  }

  // Utilitaires
  getSelectedCount(): number {
    return this.selectedItems.size;
  }

  getTotalCount(): number {
    return this.previewData.length;
  }

  getConflictCount(): number {
    return this.previewData.filter(item => item.hasConflict).length;
  }

  // TrackBy function pour optimiser le rendu de la liste
  trackByTempId(index: number, item: AffectationPreview): string {
    return item.tempId;
  }
}
