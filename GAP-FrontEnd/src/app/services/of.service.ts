import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {Ichauffeur} from "./Interfaces/ichauffeur";
import {environment} from "../../environments/environment";
import {Ilivraison} from "./Interfaces/ilivraison";
import {IordreFabrication} from "./Interfaces/iordre-fabrication";
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

  getAll(idUser:number): Observable<IordreFabrication[]> {
    return this.http.get<IordreFabrication[]>(environment.apiUrl + AUTH_API + '/getAll/'+idUser);
  }

/*
  getById(id: number): Observable<Ichauffeur> {
    return this.http.get<Ichauffeur>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }
*/

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

  // existeByMatricule(matricule: number): Observable<boolean> {
  //   return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/existeByMatricule/" + matricule);
  // }
  //
  // existeByNomComplet(nom: string, prenom: string): Observable<boolean> {
  //   const params = new HttpParams().set('nom', nom).set('prenom', prenom);
  //   return this.http.get<boolean>(`${environment.apiUrl}${AUTH_API}/existeByNomComplet`, {
  //     params: params,
  //     ...httpOptions
  //   });
  // }
  //
  // hasDeliveries(idChauffeur: number): Observable<boolean> {
  //   return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/hasDeliveries/" + idChauffeur);
  // }

  searchOF(
    idUser: number,
    idof:string,
    idprojet: number,
    idatelier: number,
    idarticle: number,
    dateDebut: string,
    dateFin: string
  ): Observable<IordreFabrication[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idUser', idUser)
      .set('idof', idof)
      .set('idprojet', idprojet)
      .set('idatelier', idatelier)
      .set('idarticle', idarticle)
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);


    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<IordreFabrication[]>(environment.apiUrl + AUTH_API+"/Search", { params });
  }
  saveFileByIdOF(id:number,file: File):Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders();
   // headers.append('Content-Type', 'multipart/form-data');
    return this.http.post(environment.apiUrl+AUTH_API+'/savePjOFById/'+id, formData, { headers });
  }
  upload(file: File):Observable<any> {

    const formData = new FormData();
    formData.append('file', file);

    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data');
    return this.http.post(environment.apiUrl + AUTH_API+'/upload', formData, { headers });
  }
  generateOf(id: number): Observable<Blob> {
    const url = environment.apiUrl + AUTH_API+'/ImprimerOF/'+id;
    return this.http.get(url, {
      responseType: 'blob',
      headers: new HttpHeaders({ 'Accept': 'application/pdf' })
    });
  }
  findOFByAtelierAndProjet(idAtelier:number,idProjet:number): Observable<IordreFabrication[]> {

    let params = new HttpParams()
      .set('idAtelier', idAtelier)
      .set('idProjet', idProjet);
    return this.http.get<IordreFabrication[]>(environment.apiUrl + AUTH_API+"/findOFByAtelierAndProjet", { params });

  }

}
