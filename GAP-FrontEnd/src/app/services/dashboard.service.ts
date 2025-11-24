import { Injectable } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ProjetService } from './projet.service';
import { AffectationService } from './affectation.service';
import { LivraisonService } from './livraison.service';
import { DeplacementService } from './deplacement.service';
import { ArticleService } from './article.service';
import { EmployeService } from './employe.service';
import { OfService } from './of.service';

export interface DashboardStats {
    totalProjets: number;
    projetsActifs: number;
    projetsTermines: number;
    totalAffectations: number;
    totalLivraisons: number;
    totalDeplacements: number;
    totalArticles: number;
    totalEmployes: number;
    totalOFs: number;
    recentActivities: any[];
}

@Injectable({
    providedIn: 'root'
})
export class DashboardService {

    constructor(
        private http: HttpClient,
        private projetService: ProjetService,
        private affectationService: AffectationService,
        private livraisonService: LivraisonService,
        private deplacementService: DeplacementService,
        private articleService: ArticleService,
        private employeService: EmployeService,
        private ofService: OfService
    ) { }

    getDashboardStats(userId: number, userRole: string): Observable<DashboardStats> {
        // Selon le rôle, on récupère les données appropriées
        const isAdmin = userRole === 'admin';
        const isConsulteur = userRole === 'consulteur';
        const isAgentSaisie = userRole === 'agentSaisie';

        let projets$: Observable<any[]>;
        let affectations$: Observable<any[]>;
        let livraisons$: Observable<any[]>;
        let deplacements$: Observable<any[]>;
        let articles$: Observable<any[]>;
        let employes$: Observable<any[]>;
        let ofs$: Observable<any[]>;

        if (isAdmin || isConsulteur) {
            // Admin et Consulteur voient toutes les données
            projets$ = this.projetService.getAll().pipe(catchError(() => of([])));
            affectations$ = this.affectationService.getAll().pipe(catchError(() => of([])));
            livraisons$ = this.livraisonService.getAll(userId).pipe(catchError(() => of([])));
            deplacements$ = this.deplacementService.getAll().pipe(catchError(() => of([])));
            articles$ = this.articleService.getAll(userId).pipe(catchError(() => of([])));
            employes$ = this.employeService.getAll(userId).pipe(catchError(() => of([])));
            ofs$ = this.ofService.getAll(userId).pipe(catchError(() => of([])));
        } else {
            // Agent de saisie voit uniquement les données de son atelier
            projets$ = this.projetService.getAffairesByAtelier(userId).pipe(catchError(() => of([])));

            // Pour les affectations, on utilise la recherche avec les paramètres de l'atelier
            const today = new Date();
            const startDate = new Date(today.getFullYear(), 0, 1).toISOString().split('T')[0];
            const endDate = new Date(today.getFullYear(), 11, 31).toISOString().split('T')[0];

            affectations$ = this.affectationService.searchAffectation(userId, 0, 0, 0, 0, startDate, endDate)
                .pipe(catchError(() => of([])));

            livraisons$ = this.livraisonService.searchLivraisons(userId, 0, 0, 0, startDate, endDate)
                .pipe(catchError(() => of([])));

            deplacements$ = this.deplacementService.searchDeplacement(userId, 0, 0, 0, '', startDate, endDate)
                .pipe(catchError(() => of([])));

            articles$ = this.articleService.getArticlesByAtelier(userId).pipe(catchError(() => of([])));
            employes$ = this.employeService.getAll(userId).pipe(catchError(() => of([])));
            ofs$ = this.ofService.getAll(userId).pipe(catchError(() => of([])));
        }

        return forkJoin({
            projets: projets$,
            affectations: affectations$,
            livraisons: livraisons$,
            deplacements: deplacements$,
            articles: articles$,
            employes: employes$,
            ofs: ofs$
        }).pipe(
            map(data => {
                const projetsActifs = data.projets.filter((p: any) => p.status === 2 || p.status === '2').length;
                const projetsTermines = data.projets.filter((p: any) => p.status === 1 || p.status === '1').length;

                return {
                    totalProjets: data.projets.length,
                    projetsActifs: projetsActifs,
                    projetsTermines: projetsTermines,
                    totalAffectations: data.affectations.length,
                    totalLivraisons: data.livraisons.length,
                    totalDeplacements: data.deplacements.length,
                    totalArticles: data.articles.length,
                    totalEmployes: data.employes.length,
                    totalOFs: data.ofs.length,
                    recentActivities: this.getRecentActivities(data)
                };
            })
        );
    }

    private getRecentActivities(data: any): any[] {
        const activities: any[] = [];

        // Ajouter les dernières affectations
        if (data.affectations && data.affectations.length > 0) {
            data.affectations.slice(0, 3).forEach((aff: any) => {
                activities.push({
                    type: 'Affectation',
                    description: `Affectation pour ${aff.projet?.designation || 'N/A'}`,
                    date: aff.sysCreationDate || new Date(),
                    icon: 'bx-user-check',
                    color: 'primary'
                });
            });
        }

        // Ajouter les dernières livraisons
        if (data.livraisons && data.livraisons.length > 0) {
            data.livraisons.slice(0, 3).forEach((liv: any) => {
                activities.push({
                    type: 'Livraison',
                    description: `Livraison pour ${liv.projet?.designation || 'N/A'}`,
                    date: liv.sysCreationDate || new Date(),
                    icon: 'bx-package',
                    color: 'success'
                });
            });
        }

        // Ajouter les derniers déplacements
        if (data.deplacements && data.deplacements.length > 0) {
            data.deplacements.slice(0, 3).forEach((dep: any) => {
                activities.push({
                    type: 'Déplacement',
                    description: `Déplacement vers ${dep.destination || 'N/A'}`,
                    date: dep.sysCreationDate || new Date(),
                    icon: 'bx-car',
                    color: 'warning'
                });
            });
        }

        // Trier par date décroissante et limiter à 10
        return activities
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
            .slice(0, 10);
    }
}
