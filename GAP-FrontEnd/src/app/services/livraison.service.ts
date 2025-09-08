import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {Ilivraison} from "./Interfaces/ilivraison";
import {IordreFabrication} from "./Interfaces/iordre-fabrication";
import {OfProjectQteRest} from "./Interfaces/of-project-qte-rest";
const AUTH_API = 'api/livraison';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class LivraisonService {

  constructor(private http: HttpClient) {}

  getAll(idUser:number): Observable<Ilivraison[]> {
    return this.http.get<Ilivraison[]>(environment.apiUrl + AUTH_API + '/getAll/'+idUser);
  }

  getLivraisonById(id:number):Observable<Ilivraison>{
    return this.http.get<Ilivraison>(environment.apiUrl + AUTH_API + '/searchById/'+id);
  }
  updateLivraison(data: any,id:number, options: any = {}){
    return this.http.put(environment.apiUrl + AUTH_API +"/update/"+id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }
  addLivraison(data: any, options: any = {}) {
    return this.http.post(environment.apiUrl + AUTH_API +"/add", data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }
  affectChauffeur(data: any,id:number, options: any = {}){
    return this.http.put(environment.apiUrl + AUTH_API +"/AffectationChauffeur/"+id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  deleteLivraison(id:number, options: any = {}){
    return this.http.delete(environment.apiUrl + AUTH_API +"/delete/"+id,{
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }
  // Méthode de recherche pour les livraisons
  searchLivraisons(
    idUser: number,
    idprojet: number,
    idchauffeur: number,
    idatelier: number,
    dateDebut: string,
    dateFin: string
  ): Observable<Ilivraison[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idUser', idUser.toString())
      .set('idprojet', idprojet.toString())
      .set('chauffeur', idchauffeur.toString())
      .set('atelier', idatelier.toString())
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<Ilivraison[]>(environment.apiUrl + AUTH_API+"/Search", { params });
  }

  impressionLivraison(livraisonId: number) {
    const url = environment.apiUrl + AUTH_API+"/imprimer"
    return this.http.get(`${url}/${livraisonId}`, {
      responseType: 'blob',
      headers: new HttpHeaders({ 'Accept': 'application/pdf' })
    });
  }
}

