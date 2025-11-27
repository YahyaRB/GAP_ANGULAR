import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-export-progress-modal',
    templateUrl: './export-progress-modal.component.html',
    styleUrls: ['./export-progress-modal.component.css']
})
export class ExportProgressModalComponent {
    @Input() isVisible: boolean = false;
    @Input() currentStep: string = '';
    @Input() progress: number = 0;

    steps = [
        { id: 'fetching', label: ' Récupération des données...', icon: 'download' },
        { id: 'processing', label: ' Traitement des données...', icon: 'cog' },
        { id: 'generating', label: ' Génération du fichier Excel...', icon: 'file-excel' },
        { id: 'saving', label: ' Enregistrement du fichier...', icon: 'save' }
    ];

    getCurrentStepIndex(): number {
        return this.steps.findIndex(step => step.id === this.currentStep);
    }

    isStepCompleted(stepId: string): boolean {
        const currentIndex = this.getCurrentStepIndex();
        const stepIndex = this.steps.findIndex(step => step.id === stepId);
        return stepIndex < currentIndex;
    }

    isStepActive(stepId: string): boolean {
        return stepId === this.currentStep;
    }
}
