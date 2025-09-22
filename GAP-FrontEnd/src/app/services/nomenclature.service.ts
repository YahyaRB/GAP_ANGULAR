import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { INomenclature } from "./Interfaces/inomenclature";
import { INomenclatureResponse } from "./Interfaces/inomenclature-response";

@Injectable({
  providedIn: 'root'
})
export class NomenclatureService {
  private apiUrl = environment.apiUrl + 'api/nomenclatures';

  private httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  /**
   * Méthode pour récupérer les nomenclatures d'un OF spécifique
   */
  getNomenclaturesByOF(ofId: number): Observable<INomenclature[]> {
    if (!ofId || ofId <= 0) {
      return throwError(() => new Error('ID ordre de fabrication invalide'));
    }

    return this.http.get<INomenclature[]>(`${this.apiUrl}/by-of/${ofId}`, this.httpOptions)
      .pipe(
        retry(2), // Retry 2 fois en cas d'échec
        catchError(this.handleError)
      );
  }

  /**
   * Méthode pour récupérer une nomenclature par ID
   */
  getNomenclatureById(id: number): Observable<INomenclature> {
    if (!id || id <= 0) {
      return throwError(() => new Error('ID nomenclature invalide'));
    }

    return this.http.get<INomenclature>(`${this.apiUrl}/${id}`, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Méthode pour mettre à jour une nomenclature
   */
  updateNomenclature(id: number, nomenclature: Partial<INomenclature>): Observable<INomenclature> {
    if (!id || id <= 0) {
      return throwError(() => new Error('ID nomenclature invalide'));
    }

    return this.http.put<INomenclature>(`${this.apiUrl}/${id}`, nomenclature, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Créer une nouvelle nomenclature
   */
  createNomenclature(nomenclature: Omit<INomenclature, 'id'>,ofId:number): Observable<INomenclature> {
    return this.http.post<INomenclature>(`${this.apiUrl}/save/${ofId}`, nomenclature, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Supprimer une nomenclature
   */
  deleteNomenclature(id: number): Observable<any> {
    if (!id || id <= 0) {
      return throwError(() => new Error('ID nomenclature invalide'));
    }

    return this.http.delete(`${this.apiUrl}/${id}`, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Récupérer le résumé des nomenclatures pour un OF
   */
  getNomenclaturesSummary(ofId: number): Observable<any> {
    if (!ofId || ofId <= 0) {
      return throwError(() => new Error('ID ordre de fabrication invalide'));
    }

    return this.http.get<any>(`${this.apiUrl}/summary/by-of/${ofId}`, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Rechercher des nomenclatures avec filtres
   */
  searchNomenclatures(filters: {
    type?: string;
    designation?: string;
    ofId?: number;
    disponiblesUniquement?: boolean;
  }): Observable<INomenclatureResponse> {
    let params = new URLSearchParams();

    if (filters.type) params.append('type', filters.type);
    if (filters.designation) params.append('designation', filters.designation);
    if (filters.ofId) params.append('ofId', filters.ofId.toString());
    if (filters.disponiblesUniquement) params.append('disponibles', 'true');

    const url = `${this.apiUrl}/search?${params.toString()}`;

    return this.http.get<INomenclatureResponse>(url, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Livrer une nomenclature
   */
  livrerNomenclature(id: number, quantiteLivre: number): Observable<INomenclature> {
    if (!id || id <= 0) {
      return throwError(() => new Error('ID nomenclature invalide'));
    }

    if (!quantiteLivre || quantiteLivre <= 0) {
      return throwError(() => new Error('Quantité à livrer invalide'));
    }

    const payload = { quantiteLivre };

    return this.http.patch<INomenclature>(`${this.apiUrl}/${id}/livrer`, payload, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Récupérer toutes les nomenclatures avec pagination optionnelle
   */
  getAllNomenclatures(page?: number, size?: number): Observable<INomenclatureResponse> {
    let url = this.apiUrl;

    if (page !== undefined && size !== undefined) {
      url += `?page=${page}&size=${size}`;
    }

    return this.http.get<INomenclatureResponse>(url, this.httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Vérifier la disponibilité du service
   */
  checkServiceHealth(): Observable<boolean> {
    return this.http.get<any>(`${this.apiUrl}/health`, this.httpOptions)
      .pipe(
        catchError(() => throwError(() => new Error('Service indisponible')))
      );
  }

  /**
   * Gestion centralisée des erreurs HTTP
   */
  private handleError = (error: HttpErrorResponse) => {
    let errorMessage = 'Une erreur inattendue s\'est produite';

    if (error.error instanceof ErrorEvent) {
      // Erreur côté client ou réseau
      errorMessage = `Erreur réseau: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
          break;
        case 400:
          errorMessage = error.error?.message || 'Requête invalide';
          break;
        case 401:
          errorMessage = 'Non autorisé. Veuillez vous reconnecter.';
          break;
        case 403:
          errorMessage = 'Accès interdit. Vous n\'avez pas les droits nécessaires.';
          break;
        case 404:
          errorMessage = 'Nomenclature non trouvée';
          break;
        case 422:
          errorMessage = error.error?.message || 'Données invalides';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur';
          break;
        case 503:
          errorMessage = 'Service temporairement indisponible';
          break;
        default:
          errorMessage = `Erreur ${error.status}: ${error.error?.message || error.message}`;
      }
    }

    console.error('Erreur NomenclatureService:', {
      status: error.status,
      message: errorMessage,
      error: error.error,
      url: error.url
    });

    return throwError(() => new Error(errorMessage));
  };
}
