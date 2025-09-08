import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Ichauffeur } from './Interfaces/ichauffeur';

const AUTH_API = 'api/chauffeur';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class ChauffeurService {

  constructor(private http: HttpClient) {}

  // Récupérer tous les chauffeurs
  getAll(): Observable<Ichauffeur[]> {
    return this.http.get<Ichauffeur[]>(environment.apiUrl + AUTH_API + '/getAll');
  }

  // Récupérer un chauffeur par son ID
  getById(id: number): Observable<Ichauffeur> {
    return this.http.get<Ichauffeur>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }

  // Mettre à jour un chauffeur
  updateChauffeur(data: any, id: number, options: any = {}): Observable<any> {
    return this.http.put(environment.apiUrl + AUTH_API + '/update/' + id, data, {
      responseType: options.responseType || 'json' // Définit le type de réponse
    });
  }

  // Ajouter un nouveau chauffeur
  addChauffeur(data: any, options: any = {}): Observable<any> {
    return this.http.post(environment.apiUrl + AUTH_API + '/add', data, {
      responseType: options.responseType || 'json' // Définit le type de réponse
    });
  }

  // Supprimer un chauffeur
  deleteChauffeur(id: number, options: any = {}): Observable<any> {
    return this.http.delete(environment.apiUrl + AUTH_API + '/delete/' + id, {
      responseType: options.responseType || 'json' // Définit le type de réponse
    });
  }

  // Vérifier si un matricule existe
  existeByMatricule(matricule: number): Observable<boolean> {
    return this.http.get<boolean>(environment.apiUrl + AUTH_API + '/existeByMatricule/' + matricule);
  }

  // Vérifier si un nom complet existe
  existeByNomComplet(nom: string, prenom: string): Observable<boolean> {
    const params = new HttpParams().set('nom', nom).set('prenom', prenom);
    return this.http.get<boolean>(`${environment.apiUrl}${AUTH_API}/existeByNomComplet`, {
      params: params,
      ...httpOptions
    });
  }





}
