import { Component, OnInit } from '@angular/core';
import { CalculService } from '../../services/calcul.service';
import { AtelierService } from '../../services/atelier.service';
import { TokenStorageService } from '../../Auth/services/token-storage.service';

declare var $: any;

@Component({
    selector: 'app-calculs',
    templateUrl: './calculs.component.html',
    styleUrls: ['./calculs.component.css']
})
export class CalculsComponent implements OnInit {

    calculs: any[] = [];
    ateliers: any[] = [];

    selectedAtelier: number | null = null;
    selectedMonth: number | null = null;
    selectedYear: number = new Date().getFullYear();

    months = [
        { value: 1, name: 'Janvier' },
        { value: 2, name: 'Février' },
        { value: 3, name: 'Mars' },
        { value: 4, name: 'Avril' },
        { value: 5, name: 'Mai' },
        { value: 6, name: 'Juin' },
        { value: 7, name: 'Juillet' },
        { value: 8, name: 'Août' },
        { value: 9, name: 'Septembre' },
        { value: 10, name: 'Octobre' },
        { value: 11, name: 'Novembre' },
        { value: 12, name: 'Décembre' }
    ];

    years: number[] = [];

    currentUser: any;
    errorMessage: string = '';

    searchText: string = '';
    filteredCalculs: any[] = [];
    selectedCalcul: any = null;

    constructor(
        private calculService: CalculService,
        private atelierService: AtelierService,
        private tokenStorage: TokenStorageService
    ) {
        const currentYear = new Date().getFullYear();
        for (let i = currentYear; i >= currentYear - 5; i--) {
            this.years.push(i);
        }
    }

    ngOnInit(): void {
        this.currentUser = this.tokenStorage.getUser();
        this.loadAteliers();
    }

    loadAteliers() {
        this.atelierService.getAll(this.currentUser.id).subscribe(
            data => {
                this.ateliers = data;
            },
            err => {
                console.error(err);
            }
        );
    }

    onCalculer() {
        if (this.selectedAtelier && this.selectedMonth && this.selectedYear) {
            this.calculService.calculerParProjet(this.currentUser.id, this.selectedAtelier, this.selectedMonth, this.selectedYear).subscribe(
                data => {
                    this.calculs = data;
                    this.filteredCalculs = data;
                    document.getElementById('closeModalBtn')?.click();
                },
                err => {
                    console.error(err);
                    this.errorMessage = "Erreur lors du calcul.";
                }
            );
        }
    }

    filterCalculs() {
        if (!this.searchText) {
            this.filteredCalculs = this.calculs;
        } else {
            const term = this.searchText.toLowerCase();
            this.filteredCalculs = this.calculs.filter(c =>
            (c.projet?.designation?.toLowerCase().includes(term) ||
                c.projet?.code?.toLowerCase().includes(term))
            );
        }
    }

    openEmployeeModal(calcul: any) {
        this.selectedCalcul = calcul;
        $('#employeeModal').modal('show');
    }

    exportGlobal() {
        if (!this.selectedAtelier || !this.selectedMonth || !this.selectedYear) {
            return;
        }
        this.calculService.exportCalculs(this.selectedAtelier, this.selectedMonth, this.selectedYear).subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `calculs_${this.selectedMonth}_${this.selectedYear}.xlsx`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        }, error => {
            console.error('Export failed', error);
        });
    }

    exportEmployeeDetails() {
        if (!this.selectedCalcul) {
            return;
        }
        this.calculService.exportDetails(this.selectedCalcul).subscribe(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `details_${this.selectedCalcul.projet.code}.xlsx`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        }, error => {
            console.error('Export details failed', error);
        });
    }

    getEmployeeHours(calcul: any): number {
        return calcul?.empHeurTrav || 0;
    }

    getEmployeePercentage(calcul: any): number {
        if (calcul && calcul.employes && calcul.employes.length > 0) {
            return calcul.pourcHeur / calcul.employes.length;
        }
        return 0;
    }
}
