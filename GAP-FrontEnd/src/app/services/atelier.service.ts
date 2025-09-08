import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient, HttpEvent, HttpHeaders} from "@angular/common/http";
import {Iaffaire} from "./Interfaces/iaffaire";
import {environment} from "../../environments/environment";
import {Iateliers} from "./Interfaces/iateliers";
const AUTH_API = 'api/atelier';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AtelierService {

  constructor(private http: HttpClient) { }
getAll(idUser:number): Observable<Iateliers[]> {
    return this.http.get<Iateliers[]>(environment.apiUrl + AUTH_API + '/getAll/'+idUser);
  }
  getAffaireById(id:number):Observable<Iateliers>{
    return this.http.get<Iateliers>(environment.apiUrl + AUTH_API + '/searchById/'+id);
  }
  getAffairesByUser(id:number):Observable<Iateliers[]>{
    return this.http.get<Iateliers[]>(environment.apiUrl + AUTH_API + '/atelierByUser/'+id);
  }
}
