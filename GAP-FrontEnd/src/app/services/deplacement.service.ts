// Version corrigée avec authentification JWT
// Ajoutez ces imports si ils manquent
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError } from 'rxjs/operators';
import { environment } from "../../environments/environment";
import { Ideplacement } from "./Interfaces/ideplacement";

// *** AJOUT : Import du TokenStorageService ***
import { TokenStorageService } from '../Auth/services/token-storage.service';

const AUTH_API = 'api/dpl';

@Injectable({
  providedIn: 'root'
})
export class DeplacementService {

  constructor(
    private http: HttpClient,
    private tokenStorageService: TokenStorageService  // ← AJOUT
  ) { }

  // *** NOUVELLE MÉTHODE : Obtenir les headers avec JWT ***
  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenStorageService.getToken();
    let headers = new HttpHeaders();

    // Log pour diagnostiquer les problèmes de token
    if (!token) {
      console.error('❌ ERREUR: Aucun token JWT trouvé dans sessionStorage');
      console.error('L\'utilisateur doit se reconnecter');
      alert('Votre session a expiré. Veuillez vous reconnecter.');
      return headers;
    }

    // Vérifier que le token a le format JWT correct (3 parties séparées par des points)
    const tokenParts = token.split('.');
    if (tokenParts.length !== 3) {
      console.error('❌ ERREUR: Token JWT invalide - ne contient pas 3 parties');
      console.error('Token reçu:', token);
      alert('Token d\'authentification invalide. Veuillez vous reconnecter.');
      return headers;
    }

    console.log('✅ Token JWT trouvé et valide:', token.substring(0, 20) + '...');
    headers = headers.set('Authorization', `Bearer ${token}`);

    return headers;
  }

  // *** MÉTHODE CORRIGÉE : downloadSingleOrdreMission avec JWT ***
  downloadSingleOrdreMission(deplacementId: number): void {
    console.log(`Tentative de téléchargement PDF avec auth pour déplacement ID: ${deplacementId}`);

    this.http.get(`${environment.apiUrl}${AUTH_API}/Deplacement/Imprimer/${deplacementId}`, {
      responseType: 'blob',
      observe: 'response',
      headers: this.getAuthHeaders().set('Accept', 'application/pdf')  // ← AJOUT AUTH
    }).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.showError('Aucune donnée reçue du serveur');
          return;
        }

        if (blob.size === 0) {
          this.showError('Le fichier PDF généré est vide');
          return;
        }

        const contentType = response.headers.get('content-type');
        console.log('Content-Type reçu:', contentType);

        if (contentType && !contentType.includes('pdf')) {
          blob.text().then(errorText => {
            this.showError(`Erreur du serveur: ${errorText}`);
          });
          return;
        }

        const fileName = `Ordre_Mission_${deplacementId}.pdf`;
        this.downloadBlobAsFile(blob, fileName);
        this.showSuccess('PDF téléchargé avec succès !');
      },
      error: (error: HttpErrorResponse) => {
        this.handlePdfDownloadError(error);
      }
    });
  }

  // *** MÉTHODE CORRIGÉE : downloadOrdreMission avec JWT ***
  downloadOrdreMission(deplacementId: number): void {
    console.log(`Tentative de téléchargement avec auth pour déplacement ID: ${deplacementId}`);

    this.http.get(`${environment.apiUrl}${AUTH_API}/Deplacement/ImprimerTous/${deplacementId}`, {
      responseType: 'blob',
      observe: 'response',
      headers: this.getAuthHeaders().set('Accept', 'application/pdf, application/zip, application/octet-stream')  // ← AJOUT AUTH
    }).subscribe({
      next: (response) => {
        const blob = response.body;
        if (!blob) {
          this.showError('Aucune donnée reçue du serveur');
          return;
        }

        if (blob.size === 0) {
          this.showError('Le fichier généré est vide');
          return;
        }

        const contentType = response.headers.get('content-type') || blob.type;
        console.log('Content-Type reçu:', contentType);

        if (contentType.includes('text') || contentType.includes('json')) {
          blob.text().then(errorText => {
            this.showError(`Erreur du serveur: ${errorText}`);
          });
          return;
        }

        let fileName: string;
        if (contentType === 'application/zip' || contentType === 'application/octet-stream') {
          fileName = `Ordres_Mission_${deplacementId}.zip`;
        } else {
          fileName = `Ordre_Mission_${deplacementId}.pdf`;
        }

        this.downloadBlobAsFile(blob, fileName);
        this.showSuccess('Fichier téléchargé avec succès !');
      },
      error: (error: HttpErrorResponse) => {
        this.handlePdfDownloadError(error);
      }
    });
  }

  // *** TOUTES LES AUTRES MÉTHODES AVEC AUTHENTIFICATION ***

  getAll(idUser: number): Observable<Ideplacement[]> {
    return this.http.get<Ideplacement[]>(`${environment.apiUrl}${AUTH_API}/getAll/${idUser}`, {
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<Ideplacement> {
    return this.http.get<Ideplacement>(`${environment.apiUrl}${AUTH_API}/searchById/${id}`, {
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  update(data: any, id: number, options: any = {}) {
    return this.http.put(`${environment.apiUrl}${AUTH_API}/update/${id}`, data, {
      responseType: options.responseType || 'json',
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  add(data: any, options: any = {}) {
    return this.http.post(`${environment.apiUrl}${AUTH_API}/add`, data, {
      responseType: options.responseType || 'json',
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  delete(id: number, options: any = {}) {
    return this.http.delete(`${environment.apiUrl}${AUTH_API}/delete/${id}`, {
      responseType: options.responseType || 'json',
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  searchDeplacement(
    idUser: number,
    idemploye: number,
    idprojet: number,
    idatelier: number,
    motif: string,
    dateDebut: string,
    dateFin: string
  ): Observable<Ideplacement[]> {
    let params = new HttpParams()
      .set('idUser', idUser)
      .set('idemploye', idemploye)
      .set('idprojet', idprojet)
      .set('idatelier', idatelier)
      .set('motif', motif)
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    return this.http.get<Ideplacement[]>(`${environment.apiUrl}${AUTH_API}/search`, {
      params,
      headers: this.getAuthHeaders()  // ← AJOUT AUTH
    }).pipe(catchError(this.handleError));
  }

  saveFileByIdOF(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${environment.apiUrl}${AUTH_API}/savePjDeplacementById/${id}`, formData, {
      headers: this.getAuthHeaders()  // ← AJOUT AUTH (sans Content-Type pour FormData)
    }).pipe(catchError(this.handleError));
  }

  upload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${environment.apiUrl}${AUTH_API}/upload`, formData, {
      headers: this.getAuthHeaders()  // ← AJOUT AUTH (sans Content-Type pour FormData)
    }).pipe(catchError(this.handleError));
  }

  // *** MÉTHODES UTILITAIRES (inchangées) ***

  private downloadBlobAsFile(blob: Blob, fileName: string): void {
    try {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      link.style.display = 'none';

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      console.log(`Fichier téléchargé: ${fileName}`);
    } catch (error) {
      console.error('Erreur lors du téléchargement:', error);
      this.showError('Erreur lors de la création du fichier de téléchargement');
    }
  }

  private handlePdfDownloadError(error: HttpErrorResponse): void {
    console.error('Erreur lors du téléchargement:', error);

    let errorMessage = 'Erreur lors du téléchargement du fichier';

    if (error.error instanceof Blob) {
      error.error.text().then(text => {
        this.showError(`Erreur du serveur: ${text}`);
      });
      return;
    }

    switch (error.status) {
      case 400:
        errorMessage = 'Données invalides pour la génération du document';
        break;
      case 401:
        errorMessage = 'Session expirée. Veuillez vous reconnecter.';
        // Optionnel: rediriger vers la page de login
        break;
      case 403:
        errorMessage = 'Vous n\'avez pas l\'autorisation d\'accéder à ce document';
        break;
      case 404:
        errorMessage = 'Document non trouvé. Vérifiez que le déplacement existe et contient des employés';
        break;
      case 500:
        errorMessage = 'Erreur du serveur lors de la génération du document';
        if (error.error && typeof error.error === 'string') {
          errorMessage += ': ' + error.error;
        }
        break;
      case 0:
        errorMessage = 'Erreur de connexion au serveur';
        break;
      default:
        errorMessage = `Erreur HTTP ${error.status}: ${error.message || 'Erreur inconnue'}`;
    }

    this.showError(errorMessage);
  }
  testConnection(): Observable<any> {
    return this.http.get(`${environment.apiUrl}${AUTH_API}/test`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }
  private handleError = (error: HttpErrorResponse): Observable<never> => {
    let errorMessage = 'Une erreur inattendue est survenue';

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur côté client: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 400:
          errorMessage = 'Requête invalide';
          break;
        case 401:
          errorMessage = 'Session expirée. Veuillez vous reconnecter.';
          break;
        case 403:
          errorMessage = 'Accès interdit';
          break;
        case 404:
          errorMessage = 'Ressource non trouvée';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur';
          break;
        default:
          errorMessage = `Erreur HTTP ${error.status}: ${error.message}`;
      }
    }

    console.error('Erreur HTTP:', error);
    return throwError(errorMessage);
  };

  private showError(message: string): void {
    console.error('Erreur:', message);
    alert(`Erreur: ${message}`);
  }

  private showSuccess(message: string): void {
    console.log('Succès:', message);
  }
}
