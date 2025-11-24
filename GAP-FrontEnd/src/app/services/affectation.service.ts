import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { Iaffectation } from "./Interfaces/iaffectation";
import { Ideplacement } from "./Interfaces/ideplacement";
const AUTH_API = 'api/Affectation';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

export interface DuplicationRequest {
  atelierId: number;
  sourceDate: string | Date;
  targetDate: string | Date;
  periodes: string[];
  userId: number;
}
export interface AffectationPreview {
  tempId: string;
  employeeId: number;
  employeeName: string;
  employeeMatricule: string;
  atelierId: number;
  atelierDesignation: string;
  projetId: number;
  projetCode: string;
  projetDesignation: string;
  articleId: number;
  articleNumPrix: string;
  articleDesignation: string;
  date: Date;
  periode: string;
  nombreHeures: number;
  canModifyHours: boolean;
  hasConflict: boolean;
  conflictMessage?: string;
}
@Injectable({
  providedIn: 'root'
})
export class AffectationService {


  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Iaffectation[]> {
    return this.http.get<Iaffectation[]>(environment.apiUrl + AUTH_API + '/getAll');
  }

  getById(id: number): Observable<Iaffectation> {
    return this.http.get<Iaffectation>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }

  update(data: any, id: number, options: any = {}) {
    return this.http.put(environment.apiUrl + AUTH_API + "/update/" + id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  add(payload: any): Observable<string> {
    return this.http.post<string>(environment.apiUrl + AUTH_API + "/add", payload,
      { responseType: 'text' as 'json' } // <— pour récupérer le message texte
    );

  }

  delete(id: number, options: any = {}) {
    return this.http.delete(environment.apiUrl + AUTH_API + "/delete/" + id, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  existeByMatricule(matricule: number): Observable<boolean> {
    return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/existeByMatricule/" + matricule);
  }

  searchAffectation(
    idUser: number,
    idemploye: number,
    idprojet: number,
    idarticle: number,
    idatelier: number,
    dateDebut: string,
    dateFin: string
  ): Observable<Iaffectation[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idUser', idUser)
      .set('idprojet', idprojet)
      .set('idemploye', idemploye)
      .set('idarticle', idarticle)
      .set('idatelier', idatelier)
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<Iaffectation[]>(environment.apiUrl + AUTH_API + "/Search", { params });
  }
  // Méthode pour prévisualiser la duplication
  previewDuplication(request: DuplicationRequest): Observable<AffectationPreview[]> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<AffectationPreview[]>(environment.apiUrl + AUTH_API + "/duplicate/preview", request, { headers });
  }

  // Méthode pour enregistrer les affectations sélectionnées
  saveDuplicatedAffectations(affectations: AffectationPreview[]): Observable<string> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.post<string>(environment.apiUrl + AUTH_API + "/duplicate/save", affectations, {
      headers,
      responseType: 'text' as 'json'
    });
  }

}
