import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AffectationService } from "../../../services/affectation.service";
import { Iateliers } from "../../../services/Interfaces/iateliers";
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

  // États de l'interface
  currentStep = 1; // 1: Configuration, 2: Prévisualisation
  previewData: AffectationPreview[] = [];
  selectedItems: Set<string> = new Set();
  selectAll = false;

  periodes = [
    { value: 'Matin', label: 'Matin' },
    { value: 'Après-midi', label: 'Après-midi' },
    { value: 'Heures', label: 'Heures' },
    { value: 'Heures_Sup', label: 'Heures Supplémentaires' }
  ];

  selectedPeriodes: string[] = [];
  selectAllPeriodes = false;

  constructor(
    private formBuilder: FormBuilder,
    private affectationService: AffectationService
  ) { }

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.duplicateForm = this.formBuilder.group({
      atelierId: ['', Validators.required],
      sourceDate: ['', Validators.required],
      targetDate: ['', Validators.required]
    });
  }

  onPeriodeChange(periode: string, event: any): void {
    if (event.target.checked) {
      this.selectedPeriodes.push(periode);
    } else {
      const index = this.selectedPeriodes.indexOf(periode);
      if (index > -1) {
        this.selectedPeriodes.splice(index, 1);
      }
      this.selectAllPeriodes = false;
    }
    this.updateSelectAllPeriodesState();
  }

  onSelectAllPeriodesChange(event: any): void {
    this.selectAllPeriodes = event.target.checked;
    if (this.selectAllPeriodes) {
      this.selectedPeriodes = ['ALL'];
    } else {
      this.selectedPeriodes = [];
    }
  }

  private updateSelectAllPeriodesState(): void {
    this.selectAllPeriodes = this.selectedPeriodes.length === this.periodes.length;
  }

  // Étape 1: Prévisualisation
  onPreview(): void {
    if (this.duplicateForm.valid && (this.selectedPeriodes.length > 0 || this.selectAllPeriodes)) {
      this.isLoading = true;
      this.message = '';

      const formValue = this.duplicateForm.value;
      const request = {
        atelierId: formValue.atelierId,
        sourceDate: new Date(formValue.sourceDate),
        targetDate: new Date(formValue.targetDate),
        periodes: this.selectAllPeriodes ? ['ALL'] : this.selectedPeriodes,
        userId: 1
      };

      console.log('Requête envoyée:', request); // Debug

      this.affectationService.previewDuplication(request).subscribe(
        (data: AffectationPreview[]) => {
          console.log('Données reçues:', data); // Debug
          this.previewData = data;
          this.selectedItems = new Set(data.filter(item => !item.hasConflict).map(item => item.tempId));
          this.updateSelectAllState();
          this.currentStep = 2;
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
    } else {
      this.message = 'Veuillez remplir tous les champs et sélectionner au moins une période';
      this.messageType = 'error';
    }
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
    if (item.canModifyHours && newHours > 0) {
      item.nombreHeures = newHours;

      // Recalculer les conflits si nécessaire
      if (item.periode !== 'Heures_Sup' && newHours > 9) {
        item.hasConflict = true;
        item.conflictMessage = 'Les heures ne peuvent pas dépasser 9 pour cette période';
        this.selectedItems.delete(item.tempId);
      } else {
        // Réinitialiser le conflit si les heures sont valides
        if (item.conflictMessage === 'Les heures ne peuvent pas dépasser 9 pour cette période') {
          item.hasConflict = false;
          item.conflictMessage = undefined;
        }
      }
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
        this.message = response;
        this.messageType = 'success';
        this.isLoading = false;
        this.refreshTable.emit();

        setTimeout(() => {
          this.resetComponent();
          const modal = document.getElementById('duplicateModal');
          if (modal) {
            (modal as any).modal('hide');
          }
        }, 2000);
      },
      (error) => {
        this.message = error.error || 'Erreur lors de l\'enregistrement';
        this.messageType = 'error';
        this.isLoading = false;
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
    this.selectedPeriodes = [];
    this.selectAllPeriodes = false;
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
