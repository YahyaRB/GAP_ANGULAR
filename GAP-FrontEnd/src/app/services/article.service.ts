import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { Iarticle } from "./Interfaces/iarticle";
import { Iprojet } from "./Interfaces/iprojet";
const AUTH_API = 'api/article';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class ArticleService {


  constructor(private http: HttpClient) {
  }

  getAll(idUser: number, page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(environment.apiUrl + AUTH_API + '/getAll/' + idUser, { params });
  }

  getById(id: number): Observable<Iarticle> {
    return this.http.get<Iarticle>(environment.apiUrl + AUTH_API + '/searchById/' + id);
  }

  update(data: any, id: number, options: any = {}) {
    return this.http.put(environment.apiUrl + AUTH_API + "/update/" + id, data, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }


  add(article: Iarticle, options?: any): Observable<any> {
    const url = environment.apiUrl + AUTH_API + "/add";
    return this.http.post(url, article, options);
  }

  delete(id: number, options: any = {}) {
    return this.http.delete(environment.apiUrl + AUTH_API + "/delete/" + id, {
      responseType: options.responseType || 'json', // Définit le type de réponse
    });
  }

  existeByMatricule(matricule: number): Observable<boolean> {
    return this.http.get<boolean>(environment.apiUrl + AUTH_API + "/existeByMatricule/" + matricule);
  }


  atriclesByProjet(
    idprojet: number,
    idatelier: number

  ): Observable<Iarticle[]> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idprojet', idprojet)
      .set('idatelier', idatelier)
    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<Iarticle[]>(environment.apiUrl + AUTH_API + "/findArticles_QteSup_QteOF", { params });
  }
  searchArticle(
    idUser: number,
    numPrix: string,
    designation: string,
    idprojet: number,
    idatelier: number,
    idarticle: number,
    page: number = 0,
    size: number = 10
  ): Observable<any> {
    // Création des paramètres HTTP
    let params = new HttpParams()
      .set('idUser', idUser)
      .set('numPrix', numPrix)
      .set('designation', designation)
      .set('idprojet', idprojet)
      .set('idatelier', idatelier)
      .set('idarticle', idarticle)
      .set('page', page.toString())
      .set('size', size.toString());


    // Envoi de la requête GET à l'API avec les paramètres
    return this.http.get<any>(environment.apiUrl + AUTH_API + "/search", { params });
  }
  getArticlesByAtelier(userId: number): Observable<Iarticle[]> {
    return this.http.get<Iarticle[]>(environment.apiUrl + AUTH_API + '/by-atelier/' + userId);
  }
}
