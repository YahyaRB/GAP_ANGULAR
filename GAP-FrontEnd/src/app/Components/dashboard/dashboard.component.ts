import { Component, OnInit } from '@angular/core';
import { DashboardService, DashboardStats } from '../../services/dashboard.service';
import { TokenStorageService } from '../../Auth/services/token-storage.service';
import { ROLES } from '../../Roles';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats = {
    totalProjets: 0,
    projetsActifs: 0,
    projetsTermines: 0,
    totalAffectations: 0,
    totalLivraisons: 0,
    totalDeplacements: 0,
    totalArticles: 0,
    totalEmployes: 0,
    totalOFs: 0,
    recentActivities: []
  };

  loading: boolean = true;
  currentUser: any;
  userRole: string = '';
  isAdmin: boolean = false;
  isConsulteur: boolean = false;
  isAgentSaisie: boolean = false;
  Math = Math;

  // Données pour les graphiques
  chartOptions: any;
  projetsChartOptions: any;
  activitiesChartOptions: any;

  constructor(
    private dashboardService: DashboardService,
    private tokenStorage: TokenStorageService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.tokenStorage.getUser();
    const roles = this.tokenStorage.getRoles();

    // Déterminer le rôle principal
    if (roles.includes(ROLES.ADMIN)) {
      this.userRole = ROLES.ADMIN;
      this.isAdmin = true;
    } else if (roles.includes(ROLES.CONSULTEUR)) {
      this.userRole = ROLES.CONSULTEUR;
      this.isConsulteur = true;
    } else if (roles.includes(ROLES.AGENTSAISIE)) {
      this.userRole = ROLES.AGENTSAISIE;
      this.isAgentSaisie = true;
    }

    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.dashboardService.getDashboardStats(this.currentUser.id, this.userRole)
      .subscribe({
        next: (data) => {
          this.stats = data;
          this.loading = false;
          this.initializeCharts();
        },
        error: (err) => {
          console.error('Erreur lors du chargement des statistiques:', err);
          this.loading = false;
        }
      });
  }

  initializeCharts(): void {
    // Graphique des projets (Actifs vs Terminés)
    this.projetsChartOptions = {
      series: [this.stats.projetsActifs, this.stats.projetsTermines],
      chart: {
        type: 'donut',
        height: 280
      },
      labels: ['Projets Actifs', 'Projets Terminés'],
      colors: ['#4CAF50', '#FF5722'],
      legend: {
        position: 'bottom'
      },
      responsive: [{
        breakpoint: 480,
        options: {
          chart: {
            width: 200
          },
          legend: {
            position: 'bottom'
          }
        }
      }]
    };

    // Graphique des activités
    this.activitiesChartOptions = {
      series: [{
        name: 'Nombre',
        data: [
          this.stats.totalAffectations,
          this.stats.totalLivraisons,
          this.stats.totalDeplacements,
          this.stats.totalOFs
        ]
      }],
      chart: {
        type: 'bar',
        height: 350,
        toolbar: {
          show: false
        }
      },
      plotOptions: {
        bar: {
          borderRadius: 8,
          horizontal: false,
          columnWidth: '55%',
        }
      },
      dataLabels: {
        enabled: false
      },
      xaxis: {
        categories: ['Affectations', 'Livraisons', 'Déplacements', 'OFs']
      },
      colors: ['#2196F3'],
      fill: {
        type: 'gradient',
        gradient: {
          shade: 'light',
          type: 'vertical',
          shadeIntensity: 0.5,
          gradientToColors: ['#64B5F6'],
          inverseColors: false,
          opacityFrom: 0.85,
          opacityTo: 0.85,
        }
      }
    };
  }

  getProgressPercentage(current: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((current / total) * 100);
  }

  formatDate(date: any): string {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRoleDisplayName(): string {
    if (this.isAdmin) return 'Administrateur';
    if (this.isConsulteur) return 'Consulteur';
    if (this.isAgentSaisie) return 'Agent de Saisie';
    return 'Utilisateur';
  }

  getMaxActivity(): number {
    return Math.max(
      this.stats.totalAffectations || 0,
      this.stats.totalLivraisons || 0,
      this.stats.totalDeplacements || 0,
      this.stats.totalOFs || 0,
      1 // Pour éviter la division par zéro
    );
  }
}
