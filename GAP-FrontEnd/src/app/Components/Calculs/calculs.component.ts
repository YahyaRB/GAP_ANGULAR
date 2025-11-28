import { Component, OnInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { CalculService } from '../../services/calcul.service';
import { AtelierService } from '../../services/atelier.service';
import { TokenStorageService } from '../../Auth/services/token-storage.service';
import ApexCharts from 'apexcharts';

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

    // Chart properties
    showGlobalChart: boolean = false;
    showEmployeeChart: boolean = false;
    globalChart: ApexCharts | null = null;
    employeeChart: ApexCharts | null = null;

    @ViewChild('globalChartRef') globalChartRef!: ElementRef;
    @ViewChild('employeeChartRef') employeeChartRef!: ElementRef;

    constructor(
        private calculService: CalculService,
        private atelierService: AtelierService,
        private tokenStorage: TokenStorageService,
        private cd: ChangeDetectorRef
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
                    // Reset view to table
                    this.showGlobalChart = false;
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
        this.showEmployeeChart = false;
        $('#employeeModal').modal('show');
    }

    toggleGlobalView() {
        this.showGlobalChart = !this.showGlobalChart;
        if (this.showGlobalChart) {
            this.cd.detectChanges(); // Force view update to create the element
            setTimeout(() => this.renderGlobalChart(), 300);
        }
    }

    toggleEmployeeView() {
        this.showEmployeeChart = !this.showEmployeeChart;
        if (this.showEmployeeChart) {
            this.cd.detectChanges(); // Force view update to create the element
            setTimeout(() => this.renderEmployeeChart(), 300);
        }
    }

    renderGlobalChart() {
        console.log('Rendering Global Chart. Data:', this.filteredCalculs);

        if (this.globalChart) {
            this.globalChart.destroy();
            this.globalChart = null;
        }

        // Use setTimeout to ensure the *ngIf has applied the changes to the DOM
        setTimeout(() => {
            const element = document.getElementById("globalChart");

            if (!element) {
                console.error('Global Chart element not found via getElementById');
                return;
            }

            // Clear the "Loading..." text
            element.innerHTML = "";

            this.createGlobalChart(element);
        }, 300);
    }

    createGlobalChart(element: HTMLElement) {
        try {
            const options = {
                series: [{
                    name: 'Heures',
                    data: this.filteredCalculs.map(c => c.heureTrav)
                }],
                chart: {
                    type: 'bar',
                    height: 350,
                    width: '100%',
                    events: {
                        mounted: (chart: any) => {
                            console.log('Global Chart Mounted');
                        }
                    }
                },
                xaxis: {
                    categories: this.filteredCalculs.map(c => c.projet?.code || c.projet?.designation),
                }
            };

            this.globalChart = new ApexCharts(element, options);
            this.globalChart.render().then(() => {
                console.log('Global Chart Render Promise Resolved');
                window.dispatchEvent(new Event('resize'));
            }).catch((err: any) => {
                console.error('Global Chart Render Error:', err);
            });

        } catch (error) {
            console.error('Error creating global chart:', error);
            alert('Erreur lors de la création du graphique: ' + error);
        }
    }

    renderEmployeeChart() {
        if (this.employeeChart) {
            this.employeeChart.destroy();
            this.employeeChart = null;
        }

        if (!this.selectedCalcul || !this.selectedCalcul.employesCalculs) return;

        setTimeout(() => {
            const element = document.getElementById("employeeChart");

            if (!element) {
                console.error('Employee Chart element not found via getElementById');
                return;
            }

            element.innerHTML = "";
            this.createEmployeeChart(element);
        }, 300);
    }

    createEmployeeChart(element: HTMLElement) {
        try {
            const options = {
                series: this.selectedCalcul.employesCalculs.map((e: any) => e.heures),
                chart: {
                    width: '100%', // Set width to 100% to fill the modal
                    height: 500,   // Set a fixed height for better visibility
                    type: 'pie',
                },
                labels: this.selectedCalcul.employesCalculs.map((e: any) => `${e.nom} ${e.prenom}`),
                legend: {
                    position: 'bottom' // Move legend to bottom to save horizontal space
                },
                responsive: [{
                    breakpoint: 480,
                    options: {
                        chart: {
                            width: 300,
                            height: 300
                        },
                        legend: {
                            position: 'bottom'
                        }
                    }
                }]
            };

            this.employeeChart = new ApexCharts(element, options);
            this.employeeChart.render().then(() => {
                console.log('Employee Chart Render Promise Resolved');
                // Force resize to ensure it fits
                window.dispatchEvent(new Event('resize'));
            }).catch((err: any) => {
                console.error('Employee Chart Render Error:', err);
            });

        } catch (error) {
            console.error('Error creating employee chart:', error);
            alert('Erreur lors de la création du graphique employé: ' + error);
        }
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
