import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {Ideplacement} from "./Interfaces/ideplacement";
import {Ilivraison} from "./Interfaces/ilivraison";
const AUTH_API = 'api/dpl';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class DeplacementService {


  constructor(private http: HttpClient) {
  }

  getAll(): Observable<Ideplacement[]> {
    return this.http.get<Ideplacement[]>(environment.apiUrl + AUTH_API + '/getAll');
  }

  getById(id: number): Observable<Ideplacement> {
    return this.http.get<Ideplacement>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }

  update(data: any, id: number, options: any = {}) {
    return this.http.put(environment.apiUrl + AUTH_API + "/update/" + id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  add(data: any, options: any = {}) {
    return this.http.post(environment.apiUrl + AUTH_API + "/add", data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  delete(id: number, options: any = {}) {
    return this.http.delete(environment.apiUrl + AUTH_API + "/delete/" + id, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  existeByMatricule(matricule: number): Observable<boolean> {
    return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/existeByMatricule/" + matricule);
  }

  existeByNomComplet(nom: string, prenom: string): Observable<boolean> {
    const params = new HttpParams().set('nom', nom).set('prenom', prenom);
    return this.http.get<boolean>(`${environment.apiUrl}${AUTH_API}/existeByNomComplet`, {
      params: params,
      ...httpOptions
    });
  }

  hasDeliveries(idChauffeur: number): Observable<boolean> {
    return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/hasDeliveries/" + idChauffeur);
  }

  searchDeplacement(
    idUser: number,
    idemploye:number,
    idprojet: number,
    idatelier: number,
    motif:string,
    dateDebut: string,
    dateFin: string
  ): Observable<Ideplacement[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idUser', idUser)
      .set('idemploye', idemploye)
      .set('idprojet', idprojet)
      .set('idatelier', idatelier)
      .set('motif', motif)
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<Ideplacement[]>(environment.apiUrl + AUTH_API+"/search", { params });
  }
  saveFileByIdOF(id:number,file: File):Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders();
    // headers.append('Content-Type', 'multipart/form-data');
    return this.http.post(environment.apiUrl+AUTH_API+'/savePjDeplacementById/'+id, formData, { headers });
  }
  upload(file: File):Observable<any> {

    const formData = new FormData();
    formData.append('file', file);

    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data');
    return this.http.post(environment.apiUrl + AUTH_API+'/upload', formData, { headers });
  }
}
