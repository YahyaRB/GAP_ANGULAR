import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { tap, catchError } from "rxjs/operators";
import { Ichauffeur } from "./Interfaces/ichauffeur";
import { environment } from "../../environments/environment";
import { Ilivraison } from "./Interfaces/ilivraison";
import { IordreFabrication } from "./Interfaces/iordre-fabrication";

const AUTH_API = 'api/ofs';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class OfService {

  constructor(private http: HttpClient) {
  }

  getAll(idUser: number): Observable<IordreFabrication[]> {
    return this.http.get<IordreFabrication[]>(environment.apiUrl + AUTH_API + '/getAll/' + idUser);
  }

  update(data: any, id: number, options: any = {}) {
    return this.http.put(environment.apiUrl + AUTH_API + "/update/" + id, data, {
      responseType: options.responseType || 'json',
    });
  }

  add(data: any, options: any = {}) {
    return this.http.post(environment.apiUrl + AUTH_API + "/add", data, {
      responseType: options.responseType || 'json',
    });
  }

  delete(id: number, options: any = {}) {
    return this.http.delete(environment.apiUrl + AUTH_API + "/delete/" + id, {
      responseType: options.responseType || 'json',
    });
  }

  searchOF(
    idUser: number,
    numOF: string,
    idProjet: number | null,
    idAtelier: number | null,
    idArticle: number | null,
    dateDebut: string | null,
    dateFin: string | null
  ): Observable<IordreFabrication[]> {

    console.log('🔧 Service searchOF appelé avec:', {
      idUser, numOF, idProjet, idAtelier, idArticle, dateDebut, dateFin
    });

    // Construire les paramètres HTTP en évitant les valeurs vides
    let params = new HttpParams();

    // Paramètre obligatoire
    params = params.set('idUser', idUser.toString());

    // Paramètres optionnels - n'ajouter que s'ils ont une valeur valide
    if (numOF && numOF.trim() !== '') {
      params = params.set('idof', numOF.trim());
    }

    if (idProjet !== null && idProjet !== undefined && idProjet > 0) {
      params = params.set('idprojet', idProjet.toString());
    }

    if (idAtelier !== null && idAtelier !== undefined && idAtelier > 0) {
      params = params.set('idatelier', idAtelier.toString());
    }

    if (idArticle !== null && idArticle !== undefined && idArticle > 0) {
      params = params.set('idarticle', idArticle.toString());
    }

    if (dateDebut && dateDebut.trim() !== '') {
      params = params.set('dateDebut', dateDebut.trim());
    }

    if (dateFin && dateFin.trim() !== '') {
      params = params.set('dateFin', dateFin.trim());
    }

    const url = `${environment.apiUrl + AUTH_API}/Search`;

    console.log('🔧 URL finale construite:', url);
    console.log('🔧 Paramètres envoyés:', params.toString());

    return this.http.get<IordreFabrication[]>(url, {
      params: params,
      headers: httpOptions.headers // Correction ici - utiliser httpOptions au lieu de this.httpOptions
    }).pipe(
      tap(data => console.log('🔧 Service - Données retournées:', data)),
      catchError(error => {
        console.error('🔧 Service - Erreur:', error);
        return throwError(() => error);
      })
    );
  }

  saveFileByIdOF(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders();
    return this.http.post(environment.apiUrl + AUTH_API + '/savePjOFById/' + id, formData, { headers });
  }

  upload(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data');
    return this.http.post(environment.apiUrl + AUTH_API + '/upload', formData, { headers });
  }

  generateOf(id: number): Observable<Blob> {
    const url = environment.apiUrl + AUTH_API + '/ImprimerOF/' + id;
    return this.http.get(url, {
      responseType: 'blob',
      headers: new HttpHeaders({ 'Accept': 'application/pdf' })
    });
  }

  findOFByAtelierAndProjet(idAtelier: number, idProjet: number): Observable<IordreFabrication[]> {
    let params = new HttpParams()
      .set('idAtelier', idAtelier)
      .set('idProjet', idProjet);
    return this.http.get<IordreFabrication[]>(environment.apiUrl + AUTH_API + "/findOFByAtelierAndProjet", { params });
  }
}
