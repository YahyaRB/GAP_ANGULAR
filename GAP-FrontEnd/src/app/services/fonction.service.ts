import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import { environment } from '../../environments/environment';
import {Ifonction} from "./Interfaces/ifonction";
import {Iemploye} from "./Interfaces/iemploye";
import {catchError} from "rxjs/operators";

const API_URL = `${environment.apiUrl}api/fonction`;

@Injectable({
  providedIn: 'root'
})
export class FonctionService {
  private httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(private http: HttpClient) {}

  getAllFonctions(): Observable<Ifonction[]> {
    return this.http.get<Ifonction[]>(`${API_URL}/getAll`);
  }

  getFonctionById(id: number): Observable<Ifonction> {
    return this.http.get<Ifonction>(`${API_URL}/getById/${id}`);
  }

  addFonction(data: Ifonction, options: any = {}): Observable<any>{
    return this.http.post<Ifonction>(`${API_URL}/add`, data, {
      responseType: options.responseType || 'json'
    }).pipe(catchError(this.handleError));
  }

  update(data: Iemploye, id: number, options: any = {}): Observable<any> {
    return this.http.put(`${API_URL}/update/${id}`, data, {
      responseType: options.responseType || 'json'
    }).pipe(catchError(this.handleError));
  }
  delete(id: number, options: any = {}): Observable<any> {
    return this.http.delete(`${API_URL}/delete/${id}`, {
      responseType: options.responseType || 'json'
    }).pipe(catchError(this.handleError));
  }
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
