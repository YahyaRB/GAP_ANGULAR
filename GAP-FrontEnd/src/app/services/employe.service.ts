import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import {map, Observable, throwError} from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Iemploye } from './Interfaces/iemploye';
import { environment } from '../../environments/environment';

const AUTH_API = 'api/employe';
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class EmployeService {

  constructor(private http: HttpClient) {}

  // Récupérer la liste de tous les employés
  getAll(idUser: number): Observable<Iemploye[]> {
    return this.http.get<Iemploye[]>(`${environment.apiUrl}${AUTH_API}/getAll/${idUser}`)
      .pipe(catchError(this.handleError));
  }
  getAllByAtelier(idAtelier: number): Observable<Iemploye[]> {
    return this.http.get<Iemploye[]>(`${environment.apiUrl}${AUTH_API}/getAllByAtelier/${idAtelier}`)
      .pipe(catchError(this.handleError));
  }
  // Récupérer un employé par son ID
  getById(id: number): Observable<Iemploye> {
    return this.http.get<Iemploye>(`${environment.apiUrl}${AUTH_API}/searchById/${id}`)
      .pipe(catchError(this.handleError));
  }

  // Mettre à jour un employé
  update(data: Iemploye, id: number, options: any = {}): Observable<any> {
    return this.http.put(`${environment.apiUrl}${AUTH_API}/update/${id}`, data, {
      responseType: options.responseType || 'json'
    }).pipe(catchError(this.handleError));
  }

  // Ajouter un nouvel employé
  add(data: Iemploye, options: any = {}): Observable<any> {
    return this.http.post(`${environment.apiUrl}${AUTH_API}/add`, data, {
      responseType: options.responseType || 'json'
    }).pipe(catchError(this.handleError));
  }

  // Supprimer un employé
  delete(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}${AUTH_API}/delete/${id}`, {
      responseType: 'text' // Indique qu'on attend du texte, pas du JSON
    });
  }


  // Vérifier si un matricule existe
  existeByMatricule(matricule: number): Observable<boolean> {
    return this.http.get<boolean>(`${environment.apiUrl}${AUTH_API}/existeByMatricule/${matricule}`)
      .pipe(catchError(this.handleError));
  }

  // Rechercher des employés avec plusieurs critères
  searchEmployes(idUser: number, matricule: string, nom: string, prenom: string, atelier: number, fonction: number): Observable<Iemploye[]> {
    let params = new HttpParams()
      .set('idUser', idUser.toString())
      .set('matricule', matricule)
      .set('nom', nom)
      .set('prenom', prenom)
      .set('atelier', atelier.toString())
      .set('fonction', fonction.toString());

    return this.http.get<Iemploye[]>(`${environment.apiUrl}${AUTH_API}/Search`, { params })
      .pipe(
        map(employes => employes.sort((a, b) => b.id - a.id)), // Tri ascendant par id
        catchError(this.handleError)
      );
  }


  // Gérer les erreurs HTTP
  private handleError(error: any): Observable<never> {
    let errorMessage = 'Une erreur est survenue';
    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      errorMessage = `Erreur serveur: ${error.status} - ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
