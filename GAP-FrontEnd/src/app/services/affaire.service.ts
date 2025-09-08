import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient, HttpEvent, HttpHeaders} from "@angular/common/http";
import {Iaffaire} from "./Interfaces/iaffaire";
import {environment} from "../../environments/environment";
const AUTH_API = 'api/affaire';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class AffaireService {

  constructor(private http: HttpClient) {}

  getAll(): Observable<Iaffaire[]> {
    return this.http.get<Iaffaire[]>(environment.apiUrl + AUTH_API + '/getAll');
  }
  getAffaireById(id:number):Observable<Iaffaire>{
    return this.http.get<Iaffaire>(environment.apiUrl + AUTH_API + '/searchById/'+id);
  }
  getAffairesByUser(id:number):Observable<Iaffaire[]>{
    return this.http.get<Iaffaire[]>(environment.apiUrl + AUTH_API + '/affaireByUser/'+id);
  }
  updateAffaire(affaire:Iaffaire,id:number):Observable<Iaffaire>{
    return this.http.put<Iaffaire>(environment.apiUrl + AUTH_API +"/update/"+id,affaire,httpOptions)
  }
  addAffaire(affaire:Iaffaire):Observable<Iaffaire>{
    return this.http.post<Iaffaire>(environment.apiUrl + AUTH_API +"/add",affaire,httpOptions)
  }

  deleteAffaire(id:number){
    return this.http.delete(environment.apiUrl + AUTH_API +"/delete/"+id)
  }
  affairesByUserAndStatut(id:number,statut:string):Observable<Iaffaire[]>{
    return this.http.get<Iaffaire[]>(environment.apiUrl+AUTH_API+'/affairesByUserAndStatut/'+id+'/'+statut);
  }
  affairesByStatut(statut:string):Observable<Iaffaire[]>{
    return this.http.get<Iaffaire[]>(environment.apiUrl+AUTH_API+'/affairesByStatut/'+statut)
  }
  getAffairesByAtelier(idUser:number):Observable<Iaffaire[]>{
  return this.http.get<Iaffaire[]>(environment.apiUrl+AUTH_API+'/by-atelier/'+idUser)
}
}
