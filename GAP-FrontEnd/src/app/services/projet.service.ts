import { Injectable } from '@angular/core';
import { environment } from "../../environments/environment";
import { Iprojet } from "./Interfaces/iprojet";
import { Observable } from "rxjs";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Iarticle } from "./Interfaces/iarticle";
const AUTH_API = 'api/project';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class ProjetService {

  constructor(private http: HttpClient) { }

  getAll(): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/getAll');
  }

  searchProjets(keyword: string, page: number, size: number): Observable<any> {
    let params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(environment.apiUrl + AUTH_API + '/searchPaginated', { params });
  }
  getAllByStaut(status: number): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/getAllByStatus/' + status);
  }
  getAffaireById(id: number): Observable<Iprojet> {
    return this.http.get<Iprojet>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }
  getAffairesByUser(id: number): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/affaireByUser/' + id);
  }
  updateProjet(data: any, id: number, options: any = {}) {
    return this.http.put(environment.apiUrl + AUTH_API + "/updateProject/" + id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }
  addProjet(data: any, options: any = {}) {
    return this.http.post(environment.apiUrl + AUTH_API + '/addProject', data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }


  deleteProjet(id: number, options: any = {}) {
    return this.http.delete(environment.apiUrl + AUTH_API + "/deleteProject/" + id, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }
  affairesByUserAndStatut(id: number, statut: string): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/affairesByUserAndStatut/' + id + '/' + statut);
  }
  affairesByStatut(statut: string): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/affairesByStatut/' + statut)
  }
  getAffairesByAtelier(userId: number): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/by-atelier/' + userId);
  }
  getAffairesByAtelierDeliverable(atelierId: number): Observable<Iprojet[]> {
    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + '/by-atelier-deliverable/' + atelierId);
  }
  findAffairesByAtelierAndQteArticle_Sup_QteOF(
    idatelier: number

  ): Observable<Iprojet[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idatelier', idatelier)
    // Envoi de la requête GET à l'API avec les paramètres

    return this.http.get<Iprojet[]>(environment.apiUrl + AUTH_API + "/findAffairesByAtelierAndQteArticle_Sup_QteOF", { params });
  }

}
